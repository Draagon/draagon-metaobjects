package com.draagon.meta.loader.file.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataReader;

import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;
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

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;

public class XMLMetaDataReader extends MetaDataReader {

    private static Log log = LogFactory.getLog(XMLMetaDataReader.class);

    public XMLMetaDataReader( InputStream is ) {
        super( is );
    }


    /**
     * Loads all the classes specified in the Filename
     */
    public void loadTypesFromStream(InputStream is) {

        Document doc = null;

        try {
            doc = XMLFileReader.loadFromStream(is);
        } catch (IOException e) {
            log.error("IO Error loading Types XML: " + e.getMessage());
            throw new MetaException("IO Error loading Types XML: " + e.getMessage(), e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }

        //////////////////////////////////////////////////////
        // PARSE THE TYPES XML

        try {
            // Look for the <items> element
            Collection<Element> elements = getElementsOfName(doc, "types");
            if (elements.isEmpty()) {
                throw new MetaException("The root 'types' element was not found");
            }

            Element el = elements.iterator().next();

            loadAllTypes(el);

        } catch (SAXException e) {
            throw new MetaException("Parse error loading MetaData: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all elements within <types> that have a sub element <type>
     */
    private List<Element> getElementsWithType(Element n ) {

        List<Element> elements = new ArrayList<Element>();

        // Get the elements under <type>
        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {

                // Look for a sub element with <type>
                NodeList list2 = node.getChildNodes();
                for (int i2 = 0; i2 < list2.getLength(); i2++) {
                    Node node2 = list2.item(i2);
                    if (node2 instanceof Element
                            && node2.getNodeName().equals( ATTR_TYPE )) {

                        // A <type> element was found, so add this name
                        elements.add( (Element) node );
                        break;
                    }
                }
            }
        }

        return elements;
    }

    /**
     * Loads the specified group types
     */
    protected synchronized void loadAllTypes(Element el) throws MetaException, SAXException {

        // Get all elements that have <type> elements
        for( Element e : getElementsWithType( el )) {

            String name = e.getNodeName();

            // Get the MetaDataTypes with the specified element name
            FileMetaDataLoader.MetaDataTypes mdts = typesMap.get( name );

            // If it doesn't exist, then create it and check for the "class" attribute
            if ( mdts == null ) {

                // Get the base class for the given element
                String clazz = e.getAttribute( "class" );
                if ( clazz == null || clazz.isEmpty() ) {
                    throw new MetaException( "MetaData Type [" + name + "] has no 'class' attribute specified");
                }

                try {
                    Class<? extends MetaData> baseClass = (Class<? extends MetaData>) Class.forName(clazz);

                    // Create a new MetaDataTypes and add to the mapping
                    mdts = new FileMetaDataLoader.MetaDataTypes( baseClass );
                    typesMap.put( name, mdts );
                }
                catch( ClassNotFoundException ex ) {
                    throw new MetaException( "MetaData Type [" + name + "] has an invalid 'class' attribute: " + ex.getMessage(), ex );
                }
            }

            // Load all the types for the specific element type
            loadTypes( e, mdts );
        }
    }

    /**
     * Loads the specified group types
     */
    protected void loadTypes(Element el, FileMetaDataLoader.MetaDataTypes typesMap)
            throws MetaException, SAXException {

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
                typesMap.put(name, tcl, "true".equals( def ));
            }
            catch (ClassNotFoundException e) {
                throw new MetaException("MetaData Type [" + section + "] with name [" + name + "] has invalid class: " + e.getMessage());
                //log.warn( "Type of section [" + section + "] with name [" + name + "] has unknown class: " + e.getMessage() );
            }
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    public void loadFromStream(InputStream is) throws MetaException {

        checkState();

        Document doc = null;

        try {
            doc = XMLFileReader.loadFromStream(is);
        }
        catch (IOException e) {
            log.error("IO Error loading Meta XML: " + e.getMessage());
            throw new MetaException("IO Error loading Meta XML: " + e.getMessage(), e);
        }
        finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }

        ////////////////////////////////////////////////////////
        // LOAD DEFAULT TYPES

        // If no types found, then load the default types, if it exists
        if (!typesLoaded) {
            String types = getDefaultMetaDataTypes();
            if ( types != null ) {
                loadTypesFromFile(getDefaultMetaDataTypes());
            }
        }

        //////////////////////////////////////////////////////
        // PARSE THE ITEMS XML BLOCK

        try {
            // Look for the <items> element
            Collection<Element> elements = getElementsOfName(doc, "metadata");
            if (elements.isEmpty()) {
                throw new MetaException("The root 'meta' element was not found");
            }

            Element itemdocElement = elements.iterator().next();

            String defaultPackageName = itemdocElement.getAttribute("package");
            if (defaultPackageName == null || defaultPackageName.trim().length() == 0) {
                defaultPackageName = "";
            }
            //throw new MetaException( "The Meta XML had no 'package' attribute defined" );

            // Load the types
            String types = itemdocElement.getAttribute("types");
            if (types != null && types.length() > 0) {
                loadTypesFromFile(types);
            }

            // Load any types specified in the Metadata XML
            try {
                // Look for the <items> element
                Collection<Element> typeElements = getElementsOfName(itemdocElement, "types");
                if (typeElements.size() > 0) {
                    Element typeEl = typeElements.iterator().next();

                    String typeFile = typeEl.getAttribute("file");
                    if (typeFile.length() > 0) {
                        loadTypesFromFile(typeFile);
                    } else {
                        loadAllTypes(typeEl); // Load inner tags
                    }
                }
            } catch (SAXException e) {
                throw new MetaException("Parse error loading MetaData: " + e.getMessage(), e);
            }

            // Parse the metadata elements
            parseMetaData( defaultPackageName, this, itemdocElement, true );
        }
        catch (SAXException e) {
            throw new MetaException("Parse error loading MetaData: " + e.getMessage(), e);
        }
    }

    protected void parseMetaData(String defaultPackageName, MetaData parent, Element element, boolean isRoot ) throws SAXException {

        // Iterate through all elements
        for ( Element el : getElements( element )) {

            String nodeName = el.getNodeName();

            // Get the MetaDataTypes map for this element
            FileMetaDataLoader.MetaDataTypes types = typesMap.get( nodeName );
            if ( types == null ) {
                // TODO:  What is the best behavior here?
                log.warn( "Ignoring '" + nodeName + "' element found on parent: " + parent );
                continue;
            }

            // Get the item name
            String name = el.getAttribute(ATTR_NAME);
            if (name == null || name.equals("")) {
                throw new MetaException("MetaData [" + nodeName + "] had no name specfied in XML");
            }

            // Get the packaging name
            String packageName = el.getAttribute("package");
            if (packageName == null || packageName.trim().isEmpty()) {
                // If not found, then use the default
                packageName = defaultPackageName;
            } else {
                // Convert any relative paths to the full package path
                packageName = expandPackageForPath( defaultPackageName, packageName );
            }

            // Load or get the MetaData
            MetaData md = null;
            boolean isNew = false;

            try {
                if ( isRoot && packageName.length() > 0 ) {
                    md = parent.getChild( packageName + PKG_SEPARATOR + name, types.baseClass );
                } else {
                    md = parent.getChild( name, types.baseClass );

                    // If it's not a child from the same parent, we need to wrap it
                    if ( md.getParent() != parent ) {
                        md = md.overload();
                        isNew = true;
                    }
                }
            } catch (MetaDataNotFoundException e) {
            }

            // If this MetaData doesn't exist yet, then we need to create it
            if (md == null) {

                isNew = true;

                // Set the SuperClass if one is defined
                MetaData superData = null;
                String superStr = el.getAttribute(ATTR_SUPER);

                // If a super class was specified
                if (superStr.length() > 0) {

                    // Try to find it with the name prepended if not fully qualified
                    try {
                        if (superStr.indexOf(PKG_SEPARATOR) < 0 && packageName.length() > 0) {
                            superData = getMetaDataByName( types.baseClass, packageName + PKG_SEPARATOR + superStr );
                        }
                    } catch (MetaObjectNotFoundException e) {
                        log.debug( "Could not find MetaData [" + packageName + PKG_SEPARATOR + superStr + "], assuming fully qualified" );
                    }

                    // Try to find it by the provided name in the 'super' attribute
                    if (superData == null) {
                        try {
                            superData = getMetaDataByName( types.baseClass, MetaDataUtil.expandPackageForMetaDataRef( packageName, superStr ));
                        } catch (MetaObjectNotFoundException e) {
                            log.error("Invalid MetaData [" + nodeName + "][" + md + "], the SuperClass [" + superStr + "] does not exist");
                            throw new MetaException("Invalid MetaData [" + nodeName + "][" + md + "], the SuperClass [" + superStr + "] does not exist");
                        }
                    }
                }
                // Check to make sure people arent' defining attributes when it shouldn't
                else {
                    String s = el.getAttribute(ATTR_SUPER);
                    if ( s != null && !s.isEmpty() ) {
                        log.warn( "Attribute 'super' defined on MetaData [" + nodeName + "][" + name + "] under parent [" + parent + "], but should not be as metadata with that name already existed" );
                    }
                }

                // get the class reference and create the class
                String typeName = el.getAttribute(ATTR_TYPE);
                if ( typeName.isEmpty() ) typeName = null;

                Class<? extends MetaData> c = null;
                try {
                    // Attempt to load the referenced class
                    if (typeName == null ) {

                        // Use the Super class type if no type is defined and a super class exists
                        if (superData != null) {
                            c = superData.getClass();
                            typeName = superData.getSubTypeName();
                        }
                        else {
                            typeName = types.getDefaultType();
                            c = types.getDefaultTypeClass();
                            if ( c == null ) {
                                throw new MetaException("MetaData [" + nodeName + "][" + name + "] has no type defined and baseClass [" + types.baseClass.getName() + "] had no default specified");
                            }
                        }
                    }
                    else {
                        c = (Class<? extends MetaData>) types.get(typeName);
                        if (c == null) {
                            throw new MetaException("MetaData [" + nodeName + "] had type [" + typeName + "], but it was not recognized");
                        }
                    }

                    // Figure out the full name for the element, needs package prefix if root
                    String fullname = name;
                    if ( isRoot ) fullname = packageName + PKG_SEPARATOR + fullname;

                    // Create the object
                    md = newInstanceFromClass( c, nodeName, typeName, fullname );

                    if ( !md.getTypeName().equals( nodeName ))
                        throw new MetaDataException( "Expected MetaData type ["+nodeName+"], but MetaData instantiated was of type [" + md.getTypeName() + "]: " + md );

                    if ( !md.getSubTypeName().equals( typeName ))
                        throw new MetaDataException( "Expected MetaData subType ["+typeName+"], but MetaData instantiated was of subType [" + md.getSubTypeName() + "]: " + md );

                    if ( !md.getName().equals( fullname ))
                        throw new MetaDataException( "Expected MetaData name ["+fullname+"], but MetaData instantiated was of name [" + md.getName() + "]: " + md );

                    //md = (MetaData) c.getConstructor(String.class, String.class, String.class).newInstance( nodeName, typeName, fullname );
                }
                catch (MetaException e) {
                    throw e;
                }
                catch (Exception e) {
                    log.error("Invalid MetaData [" + nodeName + "][" + c.getName() + "]: " + e.getMessage());
                    throw new MetaException("Invalid MetaData [" + nodeName + "][" + c.getName() + "]", e);
                }

                // Set the super data class if one exists
                if ( superData != null ) {
                    md.setSuperData(superData);
                }
            }

            // Add the MetaData to the loader
            // NOTE:  Add it first to ensure the correct parent is set
            if (isNew) {
                parent.addChild(md);
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
                parseMetaData( packageName, md, el, false );
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
                md.addAttribute(sa);
            }
        }
    }

    /**
     * Parse the MetaAttribute Value
     */
    protected void parseMetaAttributeValue( MetaAttribute attr, Element el ) {

        ///////////////////////////////////////////////////
        // Get the Node value

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
                    attr.setValueAsString(((java.lang.CharacterData) nv).getData());
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
                    log.warn("Unsupported Node Type for node [" + nv + "]");
            }
        }
    }

    /**
     * Returns a collection of child elements for the given element
     */
    protected Collection<Element> getElements(Element e) {

        ArrayList<Element> elements = new ArrayList<Element>();
        if (e == null)  return elements;

        NodeList list = e.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                elements.add((Element) node);
            }
        }

        return elements;
    }

    /**
     * Returns a collection of child elements of the given name
     */
    protected Collection<Element> getElementsOfName(Node n, String name) {
        ArrayList<Element> elements = new ArrayList<>();
        if (n == null) {
            return elements;
        }

        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element
                    && node.getNodeName().equals(name)) {
                elements.add((Element) node);
            }
        }

        return elements;
    }

}
