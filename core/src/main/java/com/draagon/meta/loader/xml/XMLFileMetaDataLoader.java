/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.util.xml.XMLFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;


/**
 * Meta Class loader for XML files
 */
public class XMLFileMetaDataLoader extends MetaDataLoader {

    private static Log log = LogFactory.getLog(XMLFileMetaDataLoader.class);
    
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_SUPER = "super";
    
    private static List<String> reservedAttributes = new ArrayList<String>();
        static {
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUPER );
    }
        
    /** Used to store the MetaData types and respective Java classes */
    public static class MetaDataTypes {
        
        public final Class<? extends MetaData> baseClass;
        private final Map<String,Class<? extends MetaData>> classes = new HashMap<String,Class<? extends MetaData>>();
        private String defaultType = null;

        public MetaDataTypes( Class<? extends MetaData> baseClass ) {
            this.baseClass = baseClass;
        }
        
        public void put( String name, Class<? extends MetaData> clazz, boolean def ) {
            classes.put( name, clazz );
            if ( def ) defaultType = name;
        }
        
        public Class<? extends MetaData> get( String name ) {
            return classes.get( name );
        }

        public Class<? extends MetaData> getDefaultTypeClass() {
            if ( defaultType == null ) return null;
            return get( defaultType );
        }

        public String getDefaultType() {
            return defaultType;
        }
    }
    
    private String typesRef = null;
    private boolean typesLoaded = false;
    private final ConcurrentHashMap<String, MetaDataTypes> typesMap = new ConcurrentHashMap<String,MetaDataTypes>();
    private String sourceDir = null;
    private final List<MetaDataSources> sources = new ArrayList<MetaDataSources>();

    public XMLFileMetaDataLoader() {
        this( "xml-" + System.currentTimeMillis() );
    }

    public XMLFileMetaDataLoader( String name ) {
        super( "xml", name );
    }

    public String getDefaultMetaDataTypes() {
        return "com/draagon/meta/loader/meta.types.xml";
    }

    public void setSourceDir( String dir ) {
        sourceDir = dir;
        if ( !sourceDir.endsWith( "/" )) sourceDir += "/";
    }

    public String getSourceDir() {
        return sourceDir;
    }
    
    public void addSources( MetaDataSources sources ) {
        this.sources.add( sources );
    }
    
    //public void setSources(List<MetaDataSources> sources) {
    //    this.sources.addAll( sources );
    //}

    /*public List<MetaDataInputSource> getSources() {
        return sources;
    }*/

    public void setTypesRef(String types) {
        if ( types.isEmpty() ) types = null;
        typesRef = types;
    }

    public String getTypesRef() {
        return typesRef;
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources ) {
        addSources( sources );
        return init();
    }

    /** Initialize with the metadata source being set */
    public MetaDataLoader init( MetaDataSources sources, boolean shouldRegister ) {
        addSources( sources );
        if ( shouldRegister ) register();
        return init();
    }
    /** Initialize with the metadata sources being set */
    /*public void init( List<MetaDataInputSource> sources ) {
        setSources( sources );
        init();
    }*/

    @Override
    public MetaDataLoader init() {

        if ( sources == null || sources.isEmpty() ) {
            throw new IllegalStateException("No Metadata Sources defined");
        }

        super.init();

        try {
            if ( !typesLoaded && getTypesRef() != null ) {
                loadTypesFromFile( getTypesRef() );
            }
        } catch (MetaException e) {
            log.error("Could not load metadata types [" + getTypesRef() + "]: " + e.getMessage());
            throw new IllegalStateException("Could not load metadata types [" + getTypesRef() + "]", e);
        }

        // Load all the Meta sources
        for (MetaDataSources s : sources ) {
            for ( String data : s.getSourceData() ) {
                loadFromStream( new ByteArrayInputStream( data.getBytes() ));
            }
        }

        return this;
    }

    /**
     * Lookup the specified class by name, include the classloaders provided by the metadata sources
     * NOTE:  This was done to handle OSGi and other complex ClassLoader scenarios
     */
    @Override
    public Class<?> loadClass( String className ) throws ClassNotFoundException {

        for (MetaDataSources s : sources ) {
            try {
                return s.getClass().getClassLoader().loadClass(className);
            } catch( ClassNotFoundException e ) {
                // Do nothing
            }
        }

        // Use the default class loader
        return super.loadClass( className );
    }

    /**
     * Loads the specified resource
     */
    public void loadTypesFromFile(String file) {
        loadTypesFromFile( this.getClass().getClassLoader(), file );
    }

    /**
     * Loads the specified resource
     */
    public void loadTypesFromFile( ClassLoader cl, String file) {

        checkState();
        
        // LOAD THE TYPES XML FILE
        if (file == null) {
            throw new IllegalArgumentException( "The Types XML reference file was not specified" );
        }

        InputStream is = null;

        // See if the filename exists
        String fn = (sourceDir==null) ? file : sourceDir + file;
        File f = new File(fn);

        if (f.exists()) {
            try {
                is = new FileInputStream(f);
            } catch (Exception e) {
                log.error("Can not read Types XML file [" + file + "]: " + e.getMessage());
                throw new MetaException("Can not read Types XML file [" + file + "]: " + e.getMessage(), e);
            }
        } else {
            is = cl.getResourceAsStream(file);
            if (is == null) {
                log.error("Types XML file [" + file + "] does not exist");
                throw new MetaException("The Types XML item file [" + file + "] was not found");
            }
        }

        try {
            loadTypesFromStream(is);
        } catch (MetaException e) {
            log.error("Meta Types XML [" + file + "]: " + e.getMessage());
            throw new MetaException("The Types XML file [" + file + "] could not be loaded: " + e.getMessage(), e);
        }

        typesLoaded = true;
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
    private List<Element> getElementsWithType( Element n ) {

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
            MetaDataTypes mdts = typesMap.get( name );
            
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
                    mdts = new MetaDataTypes( baseClass );
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
    protected void loadTypes(Element el, MetaDataTypes typesMap)
            throws MetaException, SAXException {
        
        String section = el.getNodeName();
        
        //Collection<Element> c = getElementsOfName(root, section);

        // Iterate through each section grouping (should just be 1)
        //for (Element el : c) {
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
        //}
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
            MetaDataTypes types = typesMap.get( nodeName );
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
                        md = md.wrap();
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

                Class<?> c = null;
                try {
                    // Attempt to load the referenced class
                    if (typeName == null ) {

                        // Use the Super class type if no type is defined and a super class exists
                        if (superData != null) {
                            c = superData.getClass();
                            typeName = superData.getSubType();
                        }
                        else {
                            typeName = types.getDefaultType();
                            c = types.getDefaultTypeClass();
                            if ( c == null ) {
                                throw new MetaException("MetaData [" + nodeName + "][" + name + "] has no type defined and baseClass [" + types.baseClass.getName() + "] had no default specified");
                            }
                        }
                        // Default to StringAttribute if no type is defined for a MetaAttribute
                        /*else if ( types.baseClass.isAssignableFrom( MetaAttribute.class )) {
                            c = StringAttribute.class;
                        }
                        // Default to ValueObject if no type is defined for a MetaObject
                        else if ( types.baseClass.isAssignableFrom( MetaObject.class )) {
                            c = ValueMetaObject.class;
                        }
                        // Default to StringField if no type is defined for a MetaField
                        else if ( types.baseClass.isAssignableFrom( MetaField.class )) {
                            c = StringField.class;
                        }
                        // Otherwise throw an error
                        else {
                            throw new MetaException("MetaData [" + nodeName + "][" + name + "] has no type defined on baseClass [" + types.baseClass.getName() + "]");
                        }*/
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

                    // Try for type, subtype, name string parameters first
                    try {
                        md = (MetaData) c.getDeclaredConstructor( String.class, String.class, String.class ).newInstance( nodeName, typeName, fullname );
                    } catch( NoSuchMethodException ex ) {}

                    // Try for subtype, name string parameters second
                    try {
                        if ( md == null ) md = (MetaData) c.getDeclaredConstructor( String.class, String.class ).newInstance( typeName, fullname );
                    } catch( NoSuchMethodException ex ) {}

                    // Try just the name string parameter third
                    try {
                        if ( md == null ) md = (MetaData) c.getDeclaredConstructor( String.class ).newInstance( fullname );
                    } catch( NoSuchMethodException ex ) {}

                    // Try for now parameters last
                    try {
                        if ( md == null ) md = (MetaData) c.getDeclaredConstructor().newInstance();
                    } catch( NoSuchMethodException ex ) {}

                    if ( md == null )
                        throw new MetaDataException("No valid constructor was found for MetaData class [" + c.getName() + "]");

                    if ( !md.getType().equals( nodeName ))
                        throw new MetaDataException( "Expected MetaData type ["+nodeName+"], but MetaData instantiated was of type [" + md.getType() + "]: " + md );

                    if ( !md.getSubType().equals( typeName ))
                        throw new MetaDataException( "Expected MetaData subType ["+typeName+"], but MetaData instantiated was of subType [" + md.getSubType() + "]: " + md );

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

                // Set the name and package name
                //mc.setName( packageName + MetaData.SEPARATOR + name );
                //mc.setPackage( packageName );

                // Set the super data class if one exists
                if ( superData != null ) {
                    md.setSuperData(superData);
                }
            }

            // Parse and set the Attributes
            //Collection<MetaAttribute> attrs = parseAttributes(el);
            //for (Iterator<MetaAttribute> j = attrs.iterator(); j.hasNext();) {
            //    md.addAttribute(j.next());
            //}

            // Parse the fields
            //parseFields(md, el);

            // Add the MetaData to the loader
            // NOTE:  Add it first to ensure the correct parent is set
            if (isNew) {
                parent.addChild(md);
            }
            
            // Different behavior if it's a MetaAttribute
            if ( md instanceof MetaAttribute ) {
                parseMetaAttributeValue( (MetaAttribute) md, el );
            }
            // otherwide, parse as normal recursively
            else {
                // Parse any extra attributes
                parseAttributes( md, el );
                
                // Parse the sub elements
                parseMetaData( packageName, md, el, false );

                // If it's a MetaField, set the defaultValue too
                if ( md instanceof MetaField) {
                    setDefaultValue( (MetaField) md );
                }
            }
        }
    }

    /** Set the default value on the MetaField */
    protected void setDefaultValue(MetaField md) {
        if (md.hasAttr(MetaField.ATTR_DEFAULT_VALUE)) {
            md.setDefaultValue( md.getAttr(MetaField.ATTR_DEFAULT_VALUE).getValueAsString() );
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
                    attr.setValueAsString(((CharacterData) nv).getData());
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
        ArrayList<Element> elements = new ArrayList<Element>();
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

    /**
     * Returns a collection of child elements of the given name
     */
    protected Collection<Element> getElementsOfNames(Node n, String[] names) {
        ArrayList<Element> elements = new ArrayList<Element>();
        if (n == null) {
            return elements;
        }

        NodeList list = n.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                for (int j = 0; j < names.length; j++) {
                    if (node.getNodeName().equals(names[ j])) {
                        elements.add((Element) node);
                    }
                }
            }
        }

        return elements;
    }
}
