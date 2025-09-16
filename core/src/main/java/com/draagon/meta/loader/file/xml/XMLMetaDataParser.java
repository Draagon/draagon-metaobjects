package com.draagon.meta.loader.file.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
// v6.0.0: TypesConfig and related classes replaced with service-based registry system
import com.draagon.meta.loader.file.FileMetaDataLoader;

import com.draagon.meta.util.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XMLMetaDataParser extends XMLMetaDataParserBase {

    private static final Logger log = LoggerFactory.getLogger(XMLMetaDataParser.class);

    public XMLMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    /**
     * Loads all the classes specified in the Filename
     */
    @Override
    public void loadFromStream( InputStream is ) throws MetaDataException {

        Document doc = null;

        try {
            /*byte [] b = new byte[40];
            int i = is.read(b);
            String s = new String(b);
            if (true)
                throw new MetaDataException( "---------- " + s);*/
            doc = XMLUtil.loadFromStream(is);

            //////////////////////////////////////////////////////
            // PARSE THE ITEMS XML BLOCK

            // Look for the <typesConfig> element
            Collection<Element> elements = getElementsOfName(doc, ATTR_TYPESCONFIG);
            if ( !elements.isEmpty() ) {

                Element typesConfigEl = elements.iterator().next();

                // Load any types specified in the Metadata XML
                Collection<Element> typeElements = getElementsOfName(typesConfigEl, ATTR_TYPES);
                if (typeElements.size() > 0) {
                    loadAllTypes(typeElements.iterator().next()); // Load inner tags
                }
            }
            // Look for the <metadata> element
            else {
                elements = getElementsOfName(doc, ATTR_METADATA);
                if (elements.isEmpty()) {
                    throw new MetaDataException("The root '"+ATTR_METADATA+"' or '"+ATTR_DEFPACKAGE+"' element was not found in file [" + getFilename() + "]");
                }

                Element pkgEl = elements.iterator().next();

                // Set default package name
                String defPkg = "";
                if (pkgEl.hasAttribute(ATTR_DEFPACKAGE)) defPkg = parsePackageValue(pkgEl.getAttribute(ATTR_DEFPACKAGE));
                else if (pkgEl.hasAttribute(ATTR_PACKAGE)) defPkg = parsePackageValue(pkgEl.getAttribute(ATTR_PACKAGE));
                setDefaultPackageName(defPkg);

                // Parse the metadata elements
                parseMetaData(getLoader(), pkgEl, true);
            }
        }
        catch (SAXException e) {
            throw new MetaDataException("Parse error loading MetaData from file ["+getFilename()+"]: " + e.getMessage(), e);
        }
        catch (IOException e) {
            throw new MetaDataException("Error loading Meta XML from ["+getFilename()+"]: " + e.getMessage(), e);
        }
        finally {
            try { is.close(); } catch (Exception ignore) {}
        }

        if ( getLoader().getLoaderOptions().isVerbose() ) {
            log.info("---------------------------------------------------\n"
                    +"METADATA - FILE   : " + getFilename() + "\n"
                    +"         - TYPES  : " + info.types.toString() + "\n"
                    +"         - DATA   : " + info.data.toString()  + "\n"
                    +"---------------------------------------------------");
        }
    }

    /**
     * Loads the specified group types
     */
    protected void loadAllTypes( Element el) throws MetaDataException, SAXException {

        // Get all elements that have <type> elements
        for( Element e: getElementsOfName(el, ATTR_TYPE)) {

            String name = e.getAttribute(ATTR_NAME);
            if (name.length() == 0) {
                throw new MetaDataException("Type has no 'name' attribute specified in file [" +getFilename()+ "]");
            }

            // v6.0.0: Registry-based type validation instead of TypesConfig
            validateTypeConfig(name, e.getAttribute( ATTR_CLASS ));
            // Note: TypeConfig configuration attributes (defSubType, defName, defNamePrefix) are no longer needed
            // in the service-based registry system - these are now handled by type providers directly

            // Load all the types for the specific element type
            loadSubTypes( e, name );
        }
    }

    // v6.0.0: ChildConfig system removed - child validation now handled by registry and enhancement services

    /**
     * Loads the specified group types
     */
    protected void loadSubTypes(Element el, String typeName) throws MetaDataException, SAXException {

        Collection<Element> subTypeElements = getElementsOfName(el, ATTR_SUBTYPE);

        // Iterate through each type
        for (Element typeEl : subTypeElements) {

            String name = typeEl.getAttribute(ATTR_NAME);
            String tclass = typeEl.getAttribute("class");

            if (name.length() == 0) {
                throw new MetaDataException("SubType of Type [" + typeName + "] has no 'name' attribute specified");
            }

            // v6.0.0: Registry-based subtype validation instead of TypesConfig
            validateTypeConfig(name, tclass);

            // Update info msg if verbose
            if ( getLoader().getLoaderOptions().isVerbose() ) {
                // Increment the # of subtypes
                info.incType(typeName);
            }
        }
    }

    /** Parse the metadata */
    protected void parseMetaData( MetaData parent, Element element, boolean isRoot ) throws SAXException {

        // Iterate through all elements
        for ( Element el : getElements( element )) {

            String typeName     = el.getNodeName();
            String subTypeName  = el.getAttribute(ATTR_TYPE);
            String name         = el.getAttribute(ATTR_NAME);
            String packageName  = el.getAttribute(ATTR_PACKAGE);
            String superName    = el.getAttribute(ATTR_SUPER);
            Boolean isAbstract  = Boolean.parseBoolean( el.getAttribute(ATTR_ISABSTRACT));
            Boolean isInterface = Boolean.parseBoolean( el.getAttribute(ATTR_ISINTERFACE));
            String implementsArray = el.getAttribute(ATTR_IMPLEMENTS);

            // NOTE:  This exists for backwards compatibility
            // Handle unknown types based on strict mode configuration
            if ( !getTypeRegistry().hasType( typeName ) ) {
                if ( getLoader().getLoaderOptions().isStrict() ) {
                    throw new MetaDataException("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                } else {
                    if (isRoot)
                        log.warn("Unknown type [" + typeName + "] found on loader [" + getLoader().getName() + "] in file [" + getFilename() + "]");
                    else
                        log.warn("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                    continue;
                }
            }

            // Create MetaData
            MetaData md = createOrOverlayMetaData( isRoot,
                    parent, typeName, subTypeName,
                    name, packageName, superName,
                    isAbstract, isInterface, implementsArray);

            // Update info msg if verbose
            if ( getLoader().getLoaderOptions().isVerbose() ) {
                // Increment the # of subtypes
                info.incData( typeName );
            }

            // Different behavior if it's a MetaAttribute
            if ( md instanceof MetaAttribute) {
                parseMetaAttributeValue( (MetaAttribute) md, el );
            }
            // otherwide, parse as normal recursively
            else {
                // Parse any extra attributes
                parseAttributes( md, el );

                // Parse the sub elements
                parseMetaData( md, el, false );
            }
        }
    }

    /**
     * Parses actual element attributes and adds them as StringAttributes
     */
    protected void parseAttributes( MetaData md, Element el ) {

        NamedNodeMap attrs = el.getAttributes();
        for( int i = 0; i < attrs.getLength(); i++ ) {

            Node n = attrs.item( i );
            String attrName = n.getNodeName();
            if ( !reservedAttributes.contains( attrName )) {

                String value = n.getNodeValue();
                createAttributeOnParent(md, attrName, value);
            }
        }
    }

    /**
     * Parse the MetaAttribute Value
     */
    protected void parseMetaAttributeValue( MetaAttribute attr, Element el ) {

        // Get the first node
        Node nv = el.getFirstChild();

        // Loop through and ignore the comments
        while (nv != null && nv.getNodeType() == Node.COMMENT_NODE) {
            nv.getNextSibling();
        }

        // If a valid node exists, then get the data
        if (nv != null) {
            switch (nv.getNodeType()) {

                // If CDATA just set the whole thing
                case Node.CDATA_SECTION_NODE:
                    attr.setValueAsString(((CDATASection) nv).getData());
                    break;

                // If an Element just pass it in for parsing (for when a field can process XML elements)
                case Node.ELEMENT_NODE:
                    attr.setValue(nv);
                    break;

                // If just text, then pass it in
                case Node.TEXT_NODE:
                    attr.setValueAsString(nv.getNodeValue());
                    break;

                default:
                    log.warn("Unsupported Node Type for node [" + nv + "] in file ["+getFilename()+"]");
            }
        }
    }
}
