/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader.xml;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.util.xml.XMLFileReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import com.draagon.meta.field.StringField;
//import com.draagon.meta.object.value.ValueMetaObject;
//import org.xml.sax.InputSource;
//import org.xml.sax.ErrorHandler;

/**
 * Meta Class loader for XML files
 */
public class XMLFileMetaDataLoader extends MetaDataLoader {

    private static final long serialVersionUID = 6952160679341572048L;
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

        public Class<? extends MetaData> getDefaultType() {
            if ( defaultType == null ) return null;
            return get( defaultType );
        }
    }
    
    private String typesRef = null;
    private boolean typesLoaded = false;
    private final ConcurrentHashMap<String, MetaDataTypes> typesMap = new ConcurrentHashMap<String,MetaDataTypes>();
    private String sourceDir = null;
    private final List<String> sources = new ArrayList<String>();

    public XMLFileMetaDataLoader() {
    }

    /*public void setSourcePaths(List<String> sourcePaths) {
        // TODO:  Read all XML files in the path
        //this.sources = sources;
    }*/

    public void setSourceDir( String dir ) {
        sourceDir = dir;
        if ( !sourceDir.endsWith( "/" )) sourceDir += "/";
    }

    public String getSourceDir() {
        return sourceDir;
    }
    
    public void setSource( String source ) {
        this.sources.add( source );
    }
    
    public void setSources(List<String> sources) {
        this.sources.addAll( sources );
    }

    public List<String> getSources() {
        return sources;
    }

    public void setTypesRef(String types) {
        if ( types.isEmpty() ) types = null;
        typesRef = types;
    }

    public String getTypesRef() {
        return typesRef;
    }

    /** Initialize with the metadata source being set */
    public void init( String source ) {
        setSource( source );
        init();
    }

    /** Initialize with the metadata sources being set */
    public void init( List<String> sources ) {
        setSources( sources );
        init();
    }

    @Override
    public void init() {

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
        for (Iterator<String> i = sources.iterator(); i.hasNext();) {
            String source = i.next();
            loadFromFile(source);
        }
    }

    /**
     * Loads the specified resource
     */
    public void loadTypesFromFile(String file) {
        
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
            is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                log.error("Types XML file [" + file + "] does not exist");
                throw new MetaException("The Types XML item file [" + file + "] was not found");
            }
        }

        try {
            loadTypesFromStream(is);
        } catch (MetaException e) {
            log.error("Meta Types XML [" + file + "]: " + e.getMessage());
            throw new MetaException("The Types XML file [ " + file + "] could not be loaded: " + e.getMessage(), e);
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
                    throw new MetaException( "Element section [" + name + "] has no 'class' attribute specified");
                }
                
                try {
                    Class<? extends MetaData> baseClass = (Class<? extends MetaData>) Class.forName(clazz);
                    
                    // Create a new MetaDataTypes and add to the mapping
                    mdts = new MetaDataTypes( baseClass );
                    typesMap.put( name, mdts );
                }
                catch( ClassNotFoundException ex ) {
                    throw new MetaException( "Element section [" + name + "] has an invalid 'class' attribute: " + ex.getMessage(), ex );
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
                    throw new MetaException("Type of section [" + section + "] with name [" + name + "] has invalid class: " + e.getMessage());
                    //log.warn( "Type of section [" + section + "] with name [" + name + "] has unknown class: " + e.getMessage() );
                }
            }
        //}
    }

    public void loadFromFile(String file) throws MetaException {

        if ( file.endsWith( ".xml" )) {
            loadFromXMLFile( file );
        } else if ( file.endsWith( ".bundle" )){
            loadFromBundleFile( file );
        } else {
            log.error( "Unknown metadata file type [" + file + "], so ignoring..." );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected void loadFromBundleFile(String file) throws MetaException {

        try {
            LineNumberReader in = new LineNumberReader( new InputStreamReader(getInputStream(file)));

            // Read each line in the file, and attempt to load it (including bundles)
            String line;
            while( (line = in.readLine()) != null ) {
                if (!line.trim().isEmpty()) {
                    loadFromFile( line.trim() );
                }
            }

            // Close the bundle
            in.close();
        } catch( IOException e ) {
            throw new MetaException( "Error reading metadata bundle [" + file + "]: " + e.getMessage(), e );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected void loadFromXMLFile(String file) throws MetaException {
        
        // LOAD THE XML FILE
        if (file == null) {
            throw new MetaException("The Meta XML file was not specified");
        }

        try {
            InputStream is = getInputStream(file);
            loadFromStream(is);
        } 
        catch (MetaException e) {
            throw new MetaException("The Meta XML file [" + file + "] could not be loaded: " + e.getMessage(), e);
        }
    }

    /** Get the InputStream for the file */
    protected InputStream getInputStream( String file ) {

        InputStream is = null;

        // See if the filename exists
        String fn = (sourceDir==null) ? file : sourceDir + file;
        File f = new File(fn);

        if (f.exists()) {
            try {
                is = new FileInputStream(f);
            } catch (Exception e) {
                log.error("Can not read Metadata file [" + file + "]: " + e.getMessage());
                throw new MetaException("Can not read Metadata file [" + file + "]: " + e.getMessage(), e);
            }
        }
        else {
            is = getClass().getClassLoader().getResourceAsStream(file);
            if (is == null) {
                log.error("Metadata file [" + file + "] was not found");
                throw new MetaException("The Metadata file [" + file + "] was not found");
            }
        }

        return is;
    }


    /**
     * Loads all the classes specified in the Filename
     */
    public void loadFromStream(InputStream is) throws MetaException {
        
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

        // If no types found, then load the default types
        if (!typesLoaded) {
            loadTypesFromFile("com/draagon/meta/loader/meta.types.xml");
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
            if (packageName == null || packageName.trim().length() == 0) {
                packageName = defaultPackageName;
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
                            superData = getMetaDataByName( types.baseClass, superStr );
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
                        }
                        else {
                            c = types.getDefaultType();
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
                    md = (MetaData) c.getConstructor(String.class).newInstance( fullname );
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
        if ( md.hasAttribute( MetaField.ATTR_DEF_VAL )) {
            md.setDefaultValue( String.valueOf( md.getAttribute( MetaField.ATTR_DEF_VAL )));
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
               md.addAttribute( new StringAttribute(attrName, value ));
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

                // If an Element just pass it in for parsing
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

    /*protected void parseClasses(String defaultPackageName, Element element) throws MetaException, SAXException {
        
        for (Element itemElement : getElementsOfNames(element, new String[]{"metaclass", "class"})) {
            // get the item name
            String name = itemElement.getAttribute(ATTR_NAME);
            if (name == null || name.equals("")) {
                throw new MetaException("MetaClass had no name specfied in XML");
            }

            String packageName = itemElement.getAttribute("package");
            if (packageName == null || packageName.trim().length() == 0) {
                packageName = defaultPackageName;
            }

            // Load or get the MetaClass
            MetaObject mc = null;
            boolean isNewClass = false;

            try {
                if (packageName.length() > 0) {
                    mc = getMetaData(packageName + MetaObject.SEPARATOR + name);
                } else {
                    mc = getMetaData(name);
                }
            } catch (MetaObjectNotFoundException e) {
            }

            if (mc == null) {
                isNewClass = true;

                // Set the SuperClass if one is defined
                MetaObject superClass = null;
                String superStr = itemElement.getAttribute(ATTR_SUPER);

                // If a super class was specified
                if (superStr.length() > 0) {
                    // Try to find it with the name prepended if not fully qualified
                    try {
                        if (superStr.indexOf(MetaObject.SEPARATOR) < 0 && packageName.length() > 0) {
                            superClass = getMetaData(packageName + MetaObject.SEPARATOR + superStr);
                        }
                    } catch (MetaObjectNotFoundException e) {
                        log.debug("Could not find MetaClass [" + packageName + MetaObject.SEPARATOR + superStr + "], assuming fully qualified");
                    }

                    if (superClass == null) {
                        try {
                            superClass = getMetaData(superStr);
                        } catch (MetaObjectNotFoundException e) {
                            log.error("Invalid MetaClass [" + mc + "], the SuperClass [" + superStr + "] does not exist");
                            throw new MetaException("Invalid MetaClass [" + mc + "], the SuperClass [" + superStr + "] does not exist");
                        }
                    }
                }

                // get the class reference and create the class
                String typeName = itemElement.getAttribute(ATTR_TYPE);

                Class<?> c = null;
                try {
                    // Attempt to load the referenced class
                    if (typeName.equals("")) {
                        if (superClass != null) {
                            c = superClass.getClass();
                        } else //c = BeanMetaClass.class;
                        {
                            throw new MetaException("MetaClass [" + name + "] has no type defined");
                        }
                    } else {
                        c = (Class<?>) mClassTypes.get(typeName);
                        if (c == null) {
                            throw new MetaException("MetaClass type [" + typeName + "] was not recognized");
                        }
                    }

                    mc = (MetaObject) c.getConstructor(String.class).newInstance(packageName + MetaObject.SEPARATOR + name);
                    //mc = (MetaClass) c.newInstance();
                } catch (MetaException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("Invalid MetaClass [" + c.getName() + "]: " + e.getMessage());
                    throw new MetaException("Invalid MetaClass [" + c.getName() + "]", e);
                }

                // Set the name and package name
                //mc.setName( packageName + MetaClass.SEPARATOR + name );
                //mc.setPackage( packageName );

                mc.setSuperClass(superClass);
            }

            // Parse and set the Attributes
            Collection<MetaAttribute> attrs = parseAttributes(itemElement);
            for (Iterator<MetaAttribute> j = attrs.iterator(); j.hasNext();) {
                mc.addAttribute(j.next());
            }

            // Parse the fields
            parseFields(mc, itemElement);

            // Get the default view settings
            Collection<MetaView> def_views = parseViews(name, itemElement, null);

            // If not views were defined, then use the default views
            if (def_views.size() > 0) {
                for (MetaField f : mc.getMetaFields()) {
                    for (MetaView v : def_views) {
                        if (!f.hasView(v.getName())) {
                            f.addMetaView((MetaView) v.clone());
                        }
                    }
                }
            }

            // Add the MetaClass to the loader
            if (isNewClass) {
                addMetaClass(mc);
            }
        }
    }

    protected void parseFields(MetaObject mc, Element e) throws MetaException, SAXException {
        for (Element fieldElement : getElementsOfNames(e, new String[]{"metafield", "field"})) {
            String name = fieldElement.getAttribute(ATTR_NAME);
            String type = fieldElement.getAttribute(ATTR_TYPE);
            String def = fieldElement.getAttribute("default");

            //int type = 0;

            // Get the field name
            if (name == null || name.equals("")) {
                throw new MetaException("Field in MetaClass [" + mc + "] had no name");
            }

            MetaField field = null;
            boolean isNewField = false;

            Class<?> classType = null;

            // If the type is defined, then load the class
            if (!type.equals("")) {
                classType = mFieldTypes.get(type);
                if (classType == null) {
                    throw new MetaException("MetaField [" + name + "] has type [" + type + "] that is not recognized");
                }
            }

            // If a field with that name exists, then load it
            if (mc.hasMetaField(name)) {
                field = mc.getMetaField(name);
            }

            // If there is no type or validator, then throw an exception
            if (classType == null && field == null) {
                throw new MetaException("MetaField [" + name + "] of MetaClass [" + mc + "] had no type");
            }

            // If the field exists and is of the same class, then wrap it
            if (field != null
                    && (field.getClass() == classType || classType == null)) {
                if (field.getDeclaringMetaClass() != mc) {
                    field = (MetaField) field.wrap();
                    isNewField = true;
                }
            } // Otherwise, create a new field
            else {
                if (field != null) {
                    throw new MetaException("MetaField [" + name + "] of MetaClass [" + mc + "] is already defined as type [" + field.getClass() + "]");
                }

                try {
                    field = (MetaField) classType.getConstructor(String.class).newInstance(name);
                    //field.setName( name );
                    isNewField = true;
                } catch (Exception ex) {
                    throw new RuntimeException("Instantiation exception creating new MetaField of Class [" + classType + "]", ex);
                }
            }


            // Parse and set the Attributes
            Collection<MetaAttribute> attrs = parseAttributes(fieldElement);
            for (Iterator<MetaAttribute> a = attrs.iterator(); a.hasNext();) {
                field.addAttribute(a.next());
            }

            // Parse the Views
            for (MetaView tv : parseViews(name + "@" + mc.getName(), fieldElement, field)) {
                if (!field.hasView(tv.getName()) || tv.getDeclaringMetaField() != field) {
                    field.addMetaView(tv);
                }

                //ystem.out.println( "VIEW: " + tv );
            }

            // Add the Validators
            for (MetaValidator tv : parseValidators(name + "@" + mc.getName(), fieldElement, field)) {
                if (!field.hasValidator(tv.getName())) {
                    field.addMetaValidator(tv);
                }
            }

            // Set the default value
            if (def != null && def.length() > 0) {
                field.setDefaultValue(def);
            }

            // If it's a new field, then add it
            if (isNewField) {
                mc.addMetaField(field);
            }
        }
    }

    protected Collection<MetaView> parseViews(String parent, Element e, MetaField mf)
            throws MetaException, SAXException {
        try {
            Class.forName("com.draagon.web.meta.view.WebView");
        } catch (ClassNotFoundException cnfe) {
            // The meta-web package is missing, so assume EJB server mode
            log.debug("No WebView found, assuming server-side installation");

            return new ArrayList<MetaView>();
        }

        ArrayList<MetaView> vv = new ArrayList<MetaView>();

        for (Element viewElement : getElementsOfNames(e, new String[]{"metaview", "view"})) {
            String vname = viewElement.getAttribute(ATTR_NAME);
            String vtype = viewElement.getAttribute(ATTR_TYPE);

            if (vname.equals("")) {
                throw new MetaException("View Element [" + e.getNodeName() + "] had no name defined");
            }

            if (vtype.equals("")) {
                throw new MetaException("MetaView [" + vname + "] of [" + parent + "] had no type defined");
            }

            MetaView view = null;
            boolean isNewView = false;

            Class<?> classType = null;

            // If the type is defined, then load the class
            if (!vtype.equals("")) {
                classType = mViewTypes.get(vtype);
                if (classType == null) {
                    log.warn("MetaView [" + vname + "] of [" + parent + "] has type [" + vtype + "] that is not recognized");
                    //throw new MetaException( "MetaValidator [" + name + "] of [" + parent + "] has type [" + type+ "] that is not recognized" );
                    continue;
                }
            }

            // If a view with that name exists, then load it
            if (mf != null && mf.hasView(vname) && classType == null) {
                view = mf.getView(vname);
            }

            // If there is no type or validator, then throw an exception
            if (classType == null && view == null) {
                throw new MetaException("View [" + vname + "] MetaField [" + mf + "] had no type");
            }

            // If the validator exists and is of the same class, then wrap it
            if (view != null
                    && (view.getClass() == classType || classType == null)) {
                if (view.getDeclaringMetaField() != mf) {
                    view = (MetaView) view.wrap();
                    isNewView = true;
                }
            } // Otherwise, create a new view
            else {
                try {
                    view = (MetaView) classType.getConstructor(String.class).newInstance(vname);
                    //view.setName( vname );
                    isNewView = true;
                } catch (Exception ex) {
                    throw new RuntimeException("Instantiation exception creating new MetaView of Class [" + classType + "]", ex);
                }
            }

            Collection<MetaAttribute> attrs = parseAttributes(viewElement);

            for (Iterator<MetaAttribute> j = attrs.iterator(); j.hasNext();) {
                view.addAttribute(j.next());
            }

            if (isNewView) {
                vv.add(view);
            }
        }

        return vv;
    }

    protected Collection<MetaValidator> parseValidators(String parent, Element e, MetaField mf)
            throws MetaException, SAXException {
        ArrayList<MetaValidator> vv = new ArrayList<MetaValidator>();

        for (Element el : getElementsOfNames(e, new String[]{"metavalidator", "validator"})) {
            String name = el.getAttribute(ATTR_NAME);
            String type = el.getAttribute(ATTR_TYPE);

            if (name.equals("")) {
                throw new MetaException("Validator Element [" + e.getNodeName() + "] had no name defined");
            }

            if (type.equals("")) {
                throw new MetaException("MetaValidator [" + name + "] of [" + parent + "] had no type defined");
            }

            Collection<MetaAttribute> attrs = parseAttributes(el);

            MetaValidator validator = null;
            boolean isNewValidator = false;

            Class<?> classType = null;

            // If the type is defined, then load the class
            if (!type.equals("")) {
                classType = mValidatorTypes.get(type);
                if (classType == null) {
                    log.warn("MetaValidator [" + name + "] of [" + parent + "] has type [" + type + "] that is not recognized");
                    //throw new MetaException( "MetaValidator [" + name + "] of [" + parent + "] has type [" + type+ "] that is not recognized" );
                    continue;
                }
            }

            // If a validator with that name exists, then load it
            if (mf.hasValidator(name)) {
                validator = mf.getValidator(name);
            }

            // If there is no type or validator, then throw an exception
            if (classType == null && validator == null) {
                throw new MetaException("Validator [" + name + "] MetaField [" + mf + "] had no type");
            }

            // If the validator exists and is of the same class, then wrap it
            if (validator != null
                    && (validator.getClass() == classType || classType == null)) {
                if (validator.getDeclaringMetaField() != mf) {
                    validator = (MetaValidator) validator.wrap();
                    isNewValidator = true;
                }
            } // Otherwise, create a new validator
            else {
                try {
                    validator = (MetaValidator) classType.getConstructor(String.class).newInstance(name);
                    //validator.setName( name );
                    isNewValidator = true;
                } catch (Exception ex) {
                    throw new RuntimeException("Illegal access exception creating new MetaValidator of Class [" + classType + "]", ex);
                }
            }

            // Set the attributes
            for (Iterator<MetaAttribute> j = attrs.iterator(); j.hasNext();) {
                validator.addAttribute(j.next());
            }

            if (isNewValidator) {
                vv.add(validator);
            }
        }

        return vv;
    }

    protected Collection<MetaAttribute> parseAttributes(Element e) throws MetaException, SAXException {
        
        ArrayList<MetaAttribute> attrs = new ArrayList<MetaAttribute>();

        for (Element el : getElementsOfName(e, "attr")) {
            // Get the attribute name
            String name = el.getAttribute(ATTR_NAME);
            String type = el.getAttribute(ATTR_TYPE);

            if (name.equals("")) {
                throw new MetaException("Attribute Element [" + e.getNodeName() + "] had no name defined");
            }

            Class<?> c = null;
            if (type.equals("")) {
                c = StringAttribute.class;
            } else {
                c = mAttrTypes.get(type);
                if (c == null) {
                    throw new MetaException("MetaAttribute [" + name + "] has type [" + type + "] that is not recognized");
                }
            }

            MetaAttribute attr = null;
            try {
                attr = (MetaAttribute) c.getConstructor(String.class).newInstance(name);
                //attr.setName( name );
            } catch (Exception ex) {
                log.error("Invalid MetaAttribute [" + name + "]: " + ex.getMessage());
                throw new MetaException("Invalid MetaAttribute [" + name + "]", ex);
            }

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
                        attr.setValue(((CharacterData) nv).getData());
                        break;

                    // If an Element just pass it in for parsing
                    case Node.ELEMENT_NODE:
                        attr.setValue(nv);
                        break;

                    // If just text, then pass it in
                    case Node.TEXT_NODE:
                        attr.setValue(nv.getNodeValue());
                        break;

                    default:
                        log.warn("Unsupported Node Type for node [" + nv + "]");
                }
            }

            // Add the attribute
            attrs.add(attr);
        }

        return attrs;
    }*/

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
