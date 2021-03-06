package com.draagon.meta.loader.file.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;

import com.draagon.meta.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XMLMetaDataParser extends XMLMetaDataParserBase {

    private static Log log = LogFactory.getLog(XMLMetaDataParser.class);

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

            TypeConfig typeConfig = getOrCreateTypeConfig( name, e.getAttribute( ATTR_CLASS ));
            if ( e.hasAttribute( ATTR_DEFSUBTYPE)) typeConfig.setDefaultSubType( e.getAttribute( ATTR_DEFSUBTYPE ));
            if ( e.hasAttribute( ATTR_DEFNAME)) typeConfig.setDefaultName( e.getAttribute( ATTR_DEFNAME ));
            if ( e.hasAttribute( ATTR_DEFNAMEPREFIX)) typeConfig.setDefaultNamePrefix( e.getAttribute( ATTR_DEFNAMEPREFIX ));
            loadChildren( typeConfig, e ).forEach( c-> typeConfig.addTypeChildConfig(c));

            // Load all the types for the specific element type
            loadSubTypes( e, typeConfig );
        }
    }

    protected List<ChildConfig> loadChildren(TypeConfig tc, Element el) {
        List<ChildConfig> children = new ArrayList<>();
        List<Element> childrenEl = getElementsOfName(el, "children");
        if ( !childrenEl.isEmpty() ) {
            for (Element ec : getElementsOfName(childrenEl.iterator().next(), "child")) {
                ChildConfig cc = tc.createChildConfig( ec.getAttribute(ATTR_TYPE), ec.getAttribute(ATTR_SUBTYPE), ec.getAttribute(ATTR_NAME));
                if ( ec.hasAttribute("nameAliases"))    cc.setNameAliases( Arrays.asList( ec.getAttribute( "nameAliases").split(",")));
                //if ( ec.hasAttribute("required"))           cc.setRequired( Boolean.parseBoolean( ec.getAttribute( "required")));
                //if ( ec.hasAttribute("autoCreate"))         cc.setAutoCreate( Boolean.parseBoolean( ec.getAttribute( "autoCreate")));
                //if ( ec.hasAttribute("defaultValue"))       cc.setDefaultValue( ec.getAttribute( "defaultValue"));
                //if ( ec.hasAttribute("minValue"))           cc.setMinValue( Integer.parseInt( ec.getAttribute( "minValue")));
                //if ( ec.hasAttribute("maxValue"))           cc.setMaxValue( Integer.parseInt( ec.getAttribute( "maxValue")));
                //if ( ec.hasAttribute("inlineAttr"))         cc.setInlineAttr( ec.getAttribute( "inlineAttr"));
                //if ( ec.hasAttribute("inlineAttrName"))     cc.setInlineAttrName( ec.getAttribute( "inlineAttrName"));
                //if ( ec.hasAttribute("inlineAttrValueMap")) cc.setInlineAttrValueMap( ec.getAttribute( "inlineAttrValueMap"));
                children.add( cc );
            }
        }
        return children;
    }

    /**
     * Loads the specified group types
     */
    protected void loadSubTypes(Element el, TypeConfig typeConfig) throws MetaDataException, SAXException {

        Collection<Element> subTypeElements = getElementsOfName(el, ATTR_SUBTYPE);

        // Iterate through each type
        for (Element typeEl : subTypeElements) {

            String name = typeEl.getAttribute(ATTR_NAME);
            String tclass = typeEl.getAttribute("class");
            String def = typeEl.getAttribute("default");

            if (name.length() == 0) {
                throw new MetaDataException("SubType of Type [" + typeConfig.getName() + "] has no 'name' attribute specified");
            }

            // Add the type class with the specified name
            typeConfig.addSubTypeConfig(name, tclass);

            // Load subtypes
            loadChildren( typeConfig, typeEl ).forEach( c-> typeConfig.addSubTypeChild( name, c));

            // Update info msg if verbose
            if ( getLoader().getLoaderOptions().isVerbose() ) {
                // Increment the # of subtypes
                info.incType(typeConfig.getName());
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

            // NOTE:  This exists for backwards compatibility
            // TODO:  Handle this based on a configuration of the level of error messages
            if ( getTypesConfig().getTypeByName( typeName ) == null ) {
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
            MetaData md = createOrOverlayMetaData( isRoot, parent, typeName, subTypeName, name, packageName, superName);

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
