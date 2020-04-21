package com.draagon.meta.loader.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.config.MetaDataConfig;
import com.draagon.meta.loader.config.TypeConfig;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.xml.XMLMetaDataParserBase;
import com.draagon.meta.util.xml.XMLFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LegacyXMLMetaDataParser extends XMLMetaDataParserBase {

    private static Log log = LogFactory.getLog(LegacyXMLMetaDataParser.class);

    public final static String ATTR_METADATA    = "metadata";
    public final static String ATTR_TYPES       = "types";
    public final static String ATTR_PACKAGE     = "package";
    public final static String ATTR_NAME        = "name";
    public final static String ATTR_CLASS       = "class";
    public final static String ATTR_TYPE        = "type";
    public final static String ATTR_SUPER       = "super";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_PACKAGE );
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_CLASS );
        reservedAttributes.add( ATTR_TYPES );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUPER );
    }

    public LegacyXMLMetaDataParser(FileMetaDataLoader loader, String filename ) {
        super( loader, filename );
    }

    /**
     * Loads the specified group types
     */
    protected void loadAllTypes( Element el) throws MetaException, SAXException {

        // Get all elements that have <type> elements
        for( Element e: getElements( el )) {

            // Load all the types for the specific element type
            loadSubTypes( e, getOrCreateTypeConfig( e.getNodeName(), e.getAttribute( ATTR_CLASS ) ));
        }
    }

    /**
     * Loads the specified group types
     */
    protected void loadSubTypes(Element el, TypeConfig typeConfig) throws MetaException, SAXException {

        String section = el.getNodeName();

        Collection<Element> typeCol = getElementsOfName(el, "type");

        // Iterate through each type
        for (Element typeEl : typeCol) {

            String name = typeEl.getAttribute(ATTR_NAME);
            String tclass = typeEl.getAttribute("class");
            String def = typeEl.getAttribute("default");

            if (name.length() == 0) {
                throw new MetaException("Type of section [" + section + "] has no 'name' attribute specified");
            }

            try {
                Class<MetaData> tcl = (Class<MetaData>) Class.forName(tclass);

                // Add the type class with the specified name
                typeConfig.addSubType(name, tcl, "true".equals( def ));
            }
            catch (ClassNotFoundException e) {
                throw new MetaException("MetaData file ["+getFilename()+"] has Type:SubType [" +section+":"+name+ "] with invalid class: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    @Override
    public MetaDataConfig loadFromStream( InputStream is ) throws MetaException {

        Document doc = null;

        try {
            doc = XMLFileReader.loadFromStream(is);

            //////////////////////////////////////////////////////
            // PARSE THE ITEMS XML BLOCK

            // Look for the <types> element
            Collection<Element> elements = getElementsOfName(doc, ATTR_TYPES );
            if (!elements.isEmpty()) {
                loadAllTypes(elements.iterator().next());
            }

            else {
                // Look for the <items> element
                elements = getElementsOfName(doc, "metadata");
                if (elements.isEmpty()) {
                    throw new MetaException("The root 'meta' element was not found in file [" + getFilename() + "]");
                }

                Element pkgEl = elements.iterator().next();

                // Load any types specified in the Metadata XML
                Collection<Element> typeElements = getElementsOfName(pkgEl, ATTR_TYPES);
                if (typeElements.size() > 0) {
                    loadAllTypes(typeElements.iterator().next()); // Load inner tags
                }

                // Set default package name
                String defPkg = parsePackageValue(pkgEl.getAttribute(ATTR_PACKAGE));
                setDefaultPackageName(defPkg);

                // Parse the metadata elements
                parseMetaData(getLoader(), pkgEl, true);
            }
        }
        catch (SAXException e) {
            throw new MetaException("Parse error loading MetaData from file ["+getFilename()+"]: " + e.getMessage(), e);
        }
        catch (IOException e) {
            log.error("Error loading MetaData as XML from ["+getFilename()+"]: " + e.getMessage());
            throw new MetaException("Error loading Meta XML from ["+getFilename()+"]: " + e.getMessage(), e);
        }
        finally {
            try { is.close(); } catch (Exception e) {}
        }

        return getConfig();
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
            if ( getConfig().getTypesConfig().getType( typeName ) == null ) {
                if (isRoot) log.warn("Unknown type [" +typeName+ "] found on loader [" +getLoader().getName()+ "] in file [" +getFilename()+ "]");
                else log.warn("Unknown type [" +typeName+ "] found on parent metadata [" +parent+ "] in file [" +getFilename()+ "]");
                continue;
            }

            // Create MetaData
            MetaData md = createOrOverlayMetaData( isRoot, parent, typeName, subTypeName, name, packageName, superName);

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

                // TODO:  This should be replaced by the ruleset for handling attributes in the future
                StringAttribute sa = new StringAttribute( attrName );
                sa.setValue( value );
                md.addMetaAttr(sa);
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

    /**
     * Gets all elements within <types> that have a sub element <type>
     */
    /*protected List<Element> getChildElementsWithName( Element n, String name ) {

        Element e = getFirstChildElement( n );
        if ( e != null )
            return getElementsOfName( e, name );
        else
            return new ArrayList<>();
    }*/
}
