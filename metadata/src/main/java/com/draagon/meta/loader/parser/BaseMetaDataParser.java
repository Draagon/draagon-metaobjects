package com.draagon.meta.loader.parser;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataContextRegistry;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.draagon.meta.util.MetaDataUtil.expandPackageForPath;

/**
 * Abstract BaseMetaDataParser for reading metadata from source files.
 * v6.0.0: Updated to use service-based MetaDataTypeRegistry instead of TypesConfig
 * 
 * <p>This base class provides common functionality for parsing metadata from various
 * file formats (JSON, XML, etc.) and converting them into MetaData objects. Subclasses
 * implement the specific parsing logic for their respective file formats.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Managing file parsing context and configuration</li>
 *   <li>Handling type configuration and metadata creation</li>
 *   <li>Providing common parsing utilities and error handling</li>
 *   <li>Tracking parsing statistics and information</li>
 * </ul>
 * 
 * @author Draagon Software
 * @since 4.4.0
 */
public abstract class BaseMetaDataParser {

    private static final Logger log = LoggerFactory.getLogger(BaseMetaDataParser.class);

    public final static String ATTR_METADATA        = "metadata";
    //public final static String ATTR_TYPESCONFIG     = "typesConfig";
    //public final static String ATTR_TYPES           = "types";
    public final static String ATTR_PACKAGE         = "package";
    public final static String ATTR_DEFPACKAGE      = "defaultPackage";
    public final static String ATTR_CHILDREN        = "children";
    public final static String ATTR_NAME            = "name";
    public final static String ATTR_DEFNAME         = "defaultName";
    public final static String ATTR_DEFNAMEPREFIX   = "defaultNamePrefix";
    public final static String ATTR_CLASS           = "class";
    public final static String ATTR_TYPE            = "type";
    public final static String ATTR_SUBTYPE         = "subType";
    public final static String ATTR_SUBTYPES        = "subTypes";
    public final static String ATTR_DEFSUBTYPE      = "defaultSubType";
    public final static String ATTR_SUPER           = "super";
    public final static String ATTR_VALUE           = "value";
    public final static String ATTR_ISABSTRACT      = "_isAbstract";
    public final static String ATTR_ISINTERFACE     = "isInterface";
    public final static String ATTR_IMPLEMENTS      = "implements";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_PACKAGE );
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_CLASS );
        //reservedAttributes.add( ATTR_TYPES );
        reservedAttributes.add( ATTR_CHILDREN );
        reservedAttributes.add( ATTR_TYPE );
        reservedAttributes.add( ATTR_SUBTYPE );
        reservedAttributes.add( ATTR_SUBTYPES );
        reservedAttributes.add( ATTR_SUPER );
        reservedAttributes.add( ATTR_VALUE );
        reservedAttributes.add( ATTR_ISABSTRACT );
        reservedAttributes.add( ATTR_ISINTERFACE );
        reservedAttributes.add( ATTR_IMPLEMENTS );
    }

    protected MetaDataLoader loader;
    protected String filename;
    protected String defaultPackageName = "";

    protected class ParserInfoMsg {

        public final Map<String,Integer> types = new TreeMap <>();
        public final Map<String,Integer> data = new TreeMap <>();

        public ParserInfoMsg() {}
        public int incType( String n ) { return incMap( types, n ); }
        public int incData( String n ) { return incMap( data, n ); }


        public int incMap( Map<String,Integer> map, String n ) {
            synchronized(map) {
                if ( map.get(n) == null ) {
                    map.put(n, 1);
                    return 1;
                } else {
                    Integer i = map.get(n);
                    map.put(n, ++i);
                    return i;
                }
            }
        }
    }

    protected ParserInfoMsg info = new ParserInfoMsg();

    /** Create the BaseMetaDataParser */
    protected BaseMetaDataParser(MetaDataLoader loader, String filename ) {
        if (loader == null) {
            throw new IllegalArgumentException("MetaDataLoader cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        this.loader = loader;
        this.filename = filename;
    }

    /** Return the MetaDataLoader */
    public MetaDataLoader getLoader() {
        return this.loader;
    }

    /** Return the filename being loaded */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the default package name for metadata objects parsed from this file.
     * 
     * @param defPkg the default package name to use, may be null or empty
     */
    protected void setDefaultPackageName(String defPkg ) {
        this.defaultPackageName = (defPkg != null) ? defPkg.trim() : "";
    }

    /**
     * Gets the default package name for metadata objects parsed from this file.
     * 
     * @return the default package name, may be null or empty
     */
    protected String getDefaultPackageName() {
        return defaultPackageName;
    }

    /**
     * Loads and parses metadata models from the provided input stream.
     * 
     * <p>This is the main parsing method that subclasses must implement to handle
     * their specific file format. The implementation should read from the stream
     * and populate the loader with parsed metadata objects.</p>
     * 
     * @param is the input stream containing the metadata to parse
     * @throws MetaDataException if parsing fails due to invalid format or content
     */
    public abstract void loadFromStream( InputStream is );

    /** Get the MetaDataTypeRegistry from the loader - v6.0.0: Replaces TypesConfig */
    public MetaDataTypeRegistry getTypeRegistry() {
        return this.loader.getTypeRegistry();
    }

    /**
     * Validate Type Configuration - v6.0.0: Uses registry to validate type existence
     * @param typeName Name of the metadata type
     * @param typeClass MetaData type class (ignored in v6.0.0 - registry handles class resolution)
     * @return true if type is valid and can be created
     */
    protected boolean validateTypeConfig(String typeName, String typeClass) {

        if ( typeName == null || typeName.isEmpty() ) {
            throw new MetaDataException( "MetaData Type was null or empty ["+typeName+"] in file [" +getFilename()+ "]");
        }

        // v6.0.0: Registry automatically discovers and validates types
        // No need to explicitly create or register types - they're discovered via ServiceLoader
        return true; // Type validation happens during actual instance creation in registry
    }

    /** Get the default package from the element */
    protected String parsePackageValue( String value ) {

        String defaultPackageName = value;
        if (defaultPackageName == null || defaultPackageName.trim().length() == 0) {
            defaultPackageName = "";
        }
        return defaultPackageName;
    }

    protected String getNextNamePrefix( MetaData parent, String typeName, String prefix ) {
        int i = 1;
        for( MetaData md : (List<MetaData>) parent.getChildrenOfType( typeName, true )) {
            if ( md.getName().startsWith( prefix )) {
                try {
                    int n = Integer.parseInt(md.getName().substring(prefix.length()));
                    if ( n >= i ) i = n+1;
                }
                catch( NumberFormatException ignore ) {}
            }
        }
        return prefix + i;
    }

    /** Create or Overlay the MetaData */
    protected MetaData createOrOverlayMetaData( boolean isRoot,
                                                MetaData parent, String typeName, String subTypeName,
                                                String name, String packageName, String superName,
                                                Boolean isAbstract, Boolean isInterface, String implementsArray ) {

        if ( subTypeName != null && subTypeName.equals("*")) subTypeName = null;

        // v6.0.0: Type validation handled by registry during creation
        // Simplified auto-naming logic without TypeConfig dependency
        if (name == null || name.equals("")) {
            // Generate sequential name based on subtype if available, otherwise use type
            String namePrefix = (subTypeName != null && !subTypeName.isEmpty()) 
                    ? subTypeName.toLowerCase() 
                    : typeName.toLowerCase();
            name = getNextNamePrefix(parent, typeName, namePrefix);
            
            if ( name == null ) throw new MetaDataException("MetaData [" +typeName+ "] found on parent [" +parent
                    + "] had no name specified and auto-naming failed in file ["+getFilename()+"]");
        }

        // Load or get the MetaData
        MetaData md = null;

        
        if (packageName == null || packageName.trim().isEmpty()) {
            // If not found, then use the default
            // For child elements, inherit package from parent instead of root document
            if (!isRoot && parent != null && parent.getName().contains(MetaDataLoader.PKG_SEPARATOR)) {
                // Extract package from parent's full name (everything before the last separator)
                String parentName = parent.getName();
                int lastSep = parentName.lastIndexOf(MetaDataLoader.PKG_SEPARATOR);
                if (lastSep > 0) {
                    packageName = parentName.substring(0, lastSep);
                } else {
                    packageName = getDefaultPackageName();
                }
            } else {
                packageName = getDefaultPackageName();
            }
        } else {
            // Convert any relative paths to the full package path
            packageName = expandPackageForPath( getDefaultPackageName(), packageName );
        }
        

        // Check if the metadata described already existed, and if so create and overloaded version
        // v6.0.0: Try to find existing MetaData by name (without class constraint)
        try {
            String searchName = (isRoot && packageName.length() > 0) 
                ? packageName + MetaDataLoader.PKG_SEPARATOR + name 
                : name;
                
            
            if ( isRoot && packageName.length() > 0 ) {
                md = parent.getChildOfType( typeName, packageName + MetaDataLoader.PKG_SEPARATOR + name );
            } else {
                md = parent.getChildOfType( typeName, name );

                // If it's not a child from the same parent, we need to wrap it
                if ( md.getParent() != parent ) {
                    md = md.overload();
                    parent.addChild(md);
                }
            }
            
        }
        catch (MetaDataNotFoundException e) {
            // Handle cases where it's bad that it wasn't found
        }

        // If this MetaData doesn't exist yet, then we need to create it
        if (md == null) {

            // Get the super metadata if it exists
            MetaData superData = getSuperMetaData(parent, typeName, name, packageName, superName);

            // Create the new MetaData using registry
            md = createNewMetaData(isRoot, parent, typeName, subTypeName, name, packageName, superData);

            // Add to the parent metadata
            parent.addChild(md);

            // Set the super data class if one exists
            if (superData != null) {
                md.setSuperData(superData);
            }
        }

        return md;
    }

    /** Get the Super MetaData if it exists - v6.0.0: Updated to use registry */
    protected MetaData getSuperMetaData(MetaData parent, String typeName, String name, String packageName, String superName ) {

        MetaData superData = null;

        // If a super class was specified
        if (superName != null && !superName.isEmpty()) {

            // Try to find it with the name prepended if not fully qualified
            try {
                if (superName.indexOf(MetaDataLoader.PKG_SEPARATOR) < 0 && packageName.length() > 0) {
                    // v6.0.0: Search by type and name instead of class constraint
                    superData = getLoader().getChildOfType(typeName, packageName + MetaDataLoader.PKG_SEPARATOR + superName );
                }
            } catch (MetaDataNotFoundException e) {
                // This is expected behavior - try fallback to fully qualified name resolution
                log.debug("Could not find MetaData [" + packageName + MetaDataLoader.PKG_SEPARATOR + superName + "], assuming fully qualified");
            }

            // Try to find it by the provided name in the 'super' attribute
            if (superData == null) {
                String fullyQualifiedSuperName = getFullyQualifiedSuperMetaDataName(parent, packageName, superName);
                try {
                    // v6.0.0: Search by type and name instead of class constraint
                    superData = getLoader().getChildOfType(typeName, fullyQualifiedSuperName);
                }
                catch (MetaDataNotFoundException e) {
                    //log.info( "packageName="+packageName+", parentPkg="+(parent==null?null:parent.getPackage())
                    //        +", pkg="+pkg+", superName="+superName+", sn="+sn);
                    //log.error("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent+"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                    throw new MetaDataException("Invalid MetaData [" +typeName+ "][" +name+ "] on parent ["+parent
                            +"], the SuperClass [" + superName + "] does not exist in file ["+getFilename()+"]");
                }
            }
        }
        // Check to make sure people arent' defining attributes when it shouldn't
        else {
            if (superName != null && !superName.isEmpty()) {
                logErrorOnce( parent, "getSuperMetaData("+typeName+","+name+")",
                        "Attribute 'super' defined on MetaData [" +typeName+ "][" +name+ "] under parent [" +parent
                                + "], but should not be as metadata with that name already existed: file ["+getFilename()+"]");
            }
        }

        return superData;
    }

    /** Get the fully qualified metadata name for the Super MetaData */
    protected String getFullyQualifiedSuperMetaDataName(MetaData parent, String packageName, String superName) {

        // For super references, we need the appropriate package context:
        // - For nested elements (validators, views inside fields), use the containing hierarchy's package
        // - For top-level objects, use the object's own package
        // The MetaDataUtil.findPackageForMetaData traverses up the hierarchy to find the right context
        String basePackage = MetaDataUtil.findPackageForMetaData(parent);
        if (basePackage == null || basePackage.isEmpty()) {
            // Fallback to the packageName if parent hierarchy doesn't provide context
            basePackage = packageName;
        }
        return MetaDataUtil.expandPackageForMetaDataRef(basePackage, superName);
    }

    /** Determine if the packageName should change based on the parent metadata */
    protected boolean shouldUseParentPackage( MetaData parent, String packageName ) {
        // TODO:  This may need to be refactored
        return parent != null
                && !(parent instanceof MetaDataLoader)
                && !parent.getPackage().isEmpty()
                && !parent.getPackage().equals( packageName );
    }

    /** Create new MetaData */
    /** v6.0.0: Create MetaData using registry system instead of TypesConfig */
    protected MetaData createNewMetaData(boolean isRoot, MetaData parent, String typeName, String subTypeName, String name, String packageName, MetaData superData) {

        if (subTypeName != null && subTypeName.isEmpty()) subTypeName = null;

        // Use the Super class type if no type is defined and a super class exists
        if (subTypeName == null && superData != null) {
            subTypeName = superData.getSubTypeName();
        }

        // v6.0.0: Create MetaData instance using registry
        // Only use fully qualified name for root elements, simple name for children (like pre-v6.0.0)
        String fullname = isRoot 
            ? packageName + MetaDataLoader.PKG_SEPARATOR + name 
            : name;
            
        MetaData newMetaData = getTypeRegistry().createInstance(typeName, subTypeName, fullname);
        
        if (newMetaData == null) {
            throw new MetaDataException("MetaData [type=" + typeName + "][subType=" + subTypeName + "][name=" + name
                    + "] could not be created by registry in file [" + getFilename() + "]");
        }

        return newMetaData;
    }


    /** v6.0.0: Simplified - no longer using ChildConfig constraints */
    protected String getSubTypeFromChildConfigs( String parentType, String parentSubType, String typeName, String name ) {
        // v6.0.0: Registry system handles subtype resolution during creation
        // Return null to let registry determine appropriate subtype
        return null;
    }


    /** v6.0.0: Simplified - registry validates acceptable types during creation */
    protected void verifyAcceptableChild( String parentType, String parentSubType, String type, String subType, String name ) {
        // v6.0.0: Registry system validates acceptable child types during MetaData creation
        // No explicit validation needed here - registry will fail creation if type is invalid
        
        // Only log debug info for troubleshooting in strict mode
        if ( getLoader().getLoaderOptions().isStrict() ) {
            log.debug("Creating child [{}:{}] with name [{}] on parent [{}:{}] in file [{}]", 
                     type, subType, name, parentType, parentSubType, getFilename());
        }
    }

    protected void logErrorOnce( MetaData parent, String KEY, String errMsg ) {

        if ( log.isErrorEnabled() ) {
            Object v = parent.getCacheValue( KEY );
            if ( v == null ) {
                log.error( errMsg );
                parent.setCacheValue( KEY, Boolean.TRUE );
            }
        }
    }


    protected void logWarnOnce( MetaData parent, String KEY, String errMsg ) {

        if ( log.isWarnEnabled() ) {
            Object v = parent.getCacheValue( KEY );
            if ( v == null ) {
                log.warn( errMsg );
                parent.setCacheValue( KEY, Boolean.TRUE );
            }
        }
    }

    /** v6.0.0: Removed - no longer using ChildConfig matching system */
    protected Object findBestChildConfigMatch(Object parentTypeConfig, String parentType, String parentSubType, String type, String subType, String name ) {
        // v6.0.0: Registry system handles child type validation during creation
        // Return null - no longer using ChildConfig matching
        return null;
    }

    /** v6.0.0: Context-aware attribute creation using MetaDataContextRegistry */
    protected void createAttributeOnParent(MetaData parentMetaData, String attrName, String value) {

        String parentType = parentMetaData.getTypeName();
        String parentSubType = parentMetaData.getSubTypeName();

        // v6.0.0: Create attribute using context-aware registry system
        MetaAttribute attr = null;
        
        try {
            // Use context registry to determine appropriate attribute subtype based on parent context
            String subType = MetaDataContextRegistry.getInstance()
                    .getContextSpecificAttributeSubType(parentType, parentSubType, attrName);
            
            attr = (MetaAttribute) getTypeRegistry().createInstance(
                MetaAttribute.TYPE_ATTR, subType, attrName);
            
            if (attr != null) {
                parentMetaData.addChild(attr);
                
                // Handle array format for StringArrayAttribute types
                if ("stringarray".equals(subType) && !value.startsWith("[")) {
                    // Convert single value to array format for StringArrayAttribute
                    attr.setValueAsString("[" + value + "]");
                    log.debug("Auto-created context-aware stringarray attribute [{}] on parent [{}:{}:{}] in file [{}]", 
                             attrName, parentType, parentSubType, parentMetaData.getName(), getFilename());
                } else {
                    attr.setValueAsString(value);
                    log.debug("Auto-created context-aware attribute [{}] with subtype [{}] on parent [{}:{}:{}] in file [{}]", 
                             attrName, subType, parentType, parentSubType, parentMetaData.getName(), getFilename());
                }
            }
            
        } catch (Exception e) {
            String errMsg = "Failed to create MetaAttribute [" + attrName + "] on parent record ["
                    + parentType + ":" + parentSubType + ":" + parentMetaData.getName() + "] in file [" + getFilename() + "]";
            
            if (getLoader().getLoaderOptions().isStrict()) {
                throw new MetaDataException(errMsg + ": " + e.getMessage(), e);
            } else {
                logWarnOnce(parentMetaData, "createAttributeOnParent(" + attrName + ")", errMsg + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check if a MetaData type supports inline attributes (attr type has default subType)
     */
    protected boolean supportsInlineAttributes(MetaData md) {
        try {
            return getTypeRegistry().getDefaultSubType("attr") != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cast string value to appropriate Java type based on content pattern
     */
    protected Object castStringValueToObject(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
            return Boolean.parseBoolean(stringValue);
        }
        
        // Try to parse as number
        try {
            // Try int first
            if (stringValue.matches("-?\\d+")) {
                return Integer.parseInt(stringValue);
            }
            // Try double for decimal numbers
            if (stringValue.matches("-?\\d*\\.\\d+")) {
                return Double.parseDouble(stringValue);
            }
        } catch (NumberFormatException e) {
            // Not a number, continue as string
        }
        
        // Default to string
        return stringValue;
    }

    /**
     * Parse inline attribute with type casting support
     */
    protected void parseInlineAttribute(MetaData md, String attrName, String stringValue) {
        // Check if inline attributes are supported
        if (!supportsInlineAttributes(md)) {
            log.warn("Inline attributes not supported - no attr type default subType registered");
            return;
        }
        
        // Cast string value to appropriate Java type
        Object castedValue = castStringValueToObject(stringValue);
        String finalValue = castedValue != null ? castedValue.toString() : null;
        
        // Create the attribute using existing infrastructure
        createAttributeOnParent(md, attrName, finalValue);
        
        log.debug("Created inline attribute [{}] with value [{}] on [{}:{}:{}] in file [{}]", 
            attrName, finalValue, md.getTypeName(), md.getSubTypeName(), md.getName(), getFilename());
    }
}