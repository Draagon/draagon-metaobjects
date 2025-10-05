package com.metaobjects.loader.parser;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataException;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.BooleanAttribute;
// StringArrayAttribute removed - using StringAttribute with @isArray instead
import com.metaobjects.field.MetaField;
import com.metaobjects.identity.MetaIdentity;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.TypeDefinition;
import com.metaobjects.registry.ChildRequirement;
import com.metaobjects.relationship.MetaRelationship;
import com.metaobjects.util.MetaDataUtil;
// JSON imports moved to JsonMetaDataParser - BaseMetaDataParser should be format-agnostic
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.metaobjects.util.MetaDataUtil.expandPackageForPath;

/**
 * Abstract BaseMetaDataParser for reading metadata from source files.
 * v6.0.0: Updated to use unified MetaDataRegistry instead of TypesConfig
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

    // Ensure critical attribute types are loaded early
    static {
        try {
            // Force class loading and static block execution for StringArrayAttribute
            Class.forName("com.metaobjects.attr.StringArrayAttribute");
            log.debug("Forced loading of StringArrayAttribute class");
        } catch (ClassNotFoundException e) {
            log.error("Failed to load StringArrayAttribute class", e);
        }
    }

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
    public final static String ATTR_OVERLAY         = "overlay";

    protected static List<String> reservedAttributes = new ArrayList<>();
    static {
        reservedAttributes.add( ATTR_PACKAGE );
        reservedAttributes.add( ATTR_NAME );
        reservedAttributes.add( ATTR_CLASS );
        //reservedAttributes.add( ATTR_TYPES );
        reservedAttributes.add( ATTR_CHILDREN );
        // CHANGED: Using ATTR_SUBTYPE instead of ATTR_TYPE for metadata files
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

    /** Get the MetaDataRegistry from the loader - v6.0.0: Replaces TypesConfig */
    public MetaDataRegistry getTypeRegistry() {
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
                                                Boolean isAbstract, Boolean isInterface, String implementsArray, 
                                                Boolean isOverlay ) {

        if ( subTypeName != null && subTypeName.equals("*")) subTypeName = null;

        // v6.0.0: Enhanced auto-naming logic with validation rules
        if (name == null || name.equals("")) {
            // Validation rule: Abstract metadata must have names specified
            if (Boolean.TRUE.equals(isAbstract)) {
                throw new MetaDataException("Abstract MetaData [type=" + typeName + "][subType=" + subTypeName 
                    + "] must have a name specified in file [" + getFilename() + "]");
            }
            
            // Validation rule: Object and field types require names (unless overlay)
            if (("object".equals(typeName) || "field".equals(typeName)) && !Boolean.TRUE.equals(isOverlay)) {
                throw new MetaDataException("MetaData [type=" + typeName + "][subType=" + subTypeName 
                    + "] requires a name to be specified in file [" + getFilename() + "]");
            }
            
            // Auto-naming only allowed for validator and view types (and not abstract at root)
            if (("validator".equals(typeName) || "view".equals(typeName)) && 
                !(isRoot && Boolean.TRUE.equals(isAbstract))) {
                // Generate sequential name based on subtype if available, otherwise use type
                String namePrefix = (subTypeName != null && !subTypeName.isEmpty()) 
                        ? subTypeName.toLowerCase() 
                        : typeName.toLowerCase();
                name = getNextNamePrefix(parent, typeName, namePrefix);
                
                if ( name == null ) throw new MetaDataException("Auto-naming failed for MetaData [" +typeName+ "] on parent [" +parent
                        + "] in file ["+getFilename()+"]");
            } else if (!Boolean.TRUE.equals(isOverlay)) {
                // All other types require explicit names (unless overlay)
                throw new MetaDataException("MetaData [type=" + typeName + "][subType=" + subTypeName 
                    + "] requires a name to be specified in file [" + getFilename() + "]");
            }
        }

        // Load or get the MetaData
        MetaData md = null;

        
        if (packageName == null || packageName.trim().isEmpty()) {
            // If not found, then use the default
            // For child elements, inherit package from parent instead of root document
    
            // Only apply package inheritance for specific cases, not for object children like fields
            if (!isRoot && parent != null && parent.getName().contains(MetaDataLoader.PKG_SEPARATOR)
                && shouldInheritPackageFromParent(parent, typeName)) {
                // Extract package from parent's full name (everything before the last separator)
                String parentName = parent.getName();
                int lastSep = parentName.lastIndexOf(MetaDataLoader.PKG_SEPARATOR);
                if (lastSep > 0) {
                    packageName = parentName.substring(0, lastSep);
                } else {
                    packageName = getDefaultPackageName();
                }
            } else {
                // For fields within objects, use no package to get simple names
                if ("field".equals(typeName) && parent != null && "object".equals(parent.getType())) {
                    packageName = null;
                } else {
                    packageName = getDefaultPackageName();
                }
            }
        } else {
            // Convert any relative paths to the full package path
            packageName = expandPackageForPath( getDefaultPackageName(), packageName );
        }
        

        // Enhanced overlay logic with explicit overlay support
        // v6.0.0: Try to find existing MetaData by name for overlay operations
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
            
            // If overlay was explicitly requested but existing metadata found, that's expected
            if (Boolean.TRUE.equals(isOverlay)) {
                // Explicit overlay - existing metadata must be found
                // This is the expected case - continue with overlay
            }
            
        }
        catch (MetaDataNotFoundException e) {
            // Handle cases where metadata wasn't found
            if (Boolean.TRUE.equals(isOverlay)) {
                // Explicit overlay was requested but no existing metadata found - this is an error
                throw new MetaDataException("Overlay operation requested for MetaData [type=" + typeName 
                    + "][subType=" + subTypeName + "][name=" + name + "] but no existing metadata found to overlay in file [" 
                    + getFilename() + "]");
            }
            // If not overlay, continue to create new metadata below
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

    /**
     * Determines whether a child element should inherit the package from its parent.
     * Generally, fields within objects should have simple names, not inherit the object's package.
     * But validators within fields might inherit the field's package.
     */
    protected boolean shouldInheritPackageFromParent(MetaData parent, String childTypeName) {
        // Fields within objects should have simple names
        if ("field".equals(childTypeName) && parent != null && "object".equals(parent.getType())) {
            return false;
        }

        // Validators within fields can inherit the field's package
        if ("validator".equals(childTypeName) && parent != null && "field".equals(parent.getType())) {
            return true;
        }

        // Attributes generally don't inherit packages
        if ("attr".equals(childTypeName)) {
            return false;
        }

        // Default to false for safety - elements should explicitly specify packages if needed
        return false;
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

        String expandedName = MetaDataUtil.expandPackageForMetaDataRef(basePackage, superName);

        return expandedName;
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
            subTypeName = superData.getSubType();
        }
        
        // SubType is required for creating new metadata instances (unless inherited from super or using override)
        if (subTypeName == null) {
            throw new MetaDataException("No subType specified for type [" + typeName + "] for MetaData [name=" + name + "] in file [" + getFilename() + "]. Either specify a 'subType' attribute, use 'super=\"...\"' to inherit type, or use 'override=true' for overlay operations.");
        }

        // v6.0.0: Create MetaData instance using registry
        // Use fully qualified name for elements that have a package for global lookup
        // Simple names only for elements without packages
        String fullname;
        if (packageName != null && !packageName.isEmpty()) {
            fullname = packageName + MetaDataLoader.PKG_SEPARATOR + name;
        } else {
            fullname = name;
        }

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

    /** v6.3.0: MetaData-instance-driven approach using getChild() and getDataType() */
    protected void createAttributeOnParent(MetaData parentMetaData, String attrName, String value) {
        // Delegate to the correct parseInlineAttribute method
        parseInlineAttribute(parentMetaData, attrName, value);
    }

    /**
     * Check if a MetaData type supports inline attributes (attr type has default subType)
     */
    protected boolean supportsInlineAttributes(MetaData md) {
        // Inline attributes are supported in the unified registry architecture
        return true;
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
     * Parse inline attribute using correct MetaData type definition approach
     * Looks up the expected attribute definition from the parent's type definition
     */
    protected void parseInlineAttribute(MetaData parentMetaData, String attrName, String stringValue) {
        try {
            // Special handling for isArray as native property (without @)
            if ("isArray".equals(attrName)) {
                handleNativeIsArrayProperty(parentMetaData, stringValue);
                return;
            }

            // Step 1: Get the type definition for this parent metadata
            String parentType = parentMetaData.getType();
            String parentSubType = parentMetaData.getSubType();

            TypeDefinition typeDef = getTypeRegistry().getTypeDefinition(parentType, parentSubType);
            if (typeDef == null) {
                throw new MetaDataException("No type definition found for [" + parentType + ":" + parentSubType + "]");
            }

            // Step 2: Look up the expected attribute subtype from the type definition
            String expectedAttributeSubType = getExpectedAttributeSubTypeFromRegistry(typeDef, attrName);

            // Step 2.5: Override with value-based inference for ALL attributes when registry doesn't have specific info
            boolean isJsonArray = false;
            if (stringValue != null && "string".equals(expectedAttributeSubType)) {
                // Handle JSON arrays - convert to comma-delimited format and mark as array
                if (stringValue.trim().startsWith("[") && stringValue.trim().endsWith("]")) {
                    stringValue = convertJsonArrayToCommaDelimited(stringValue);
                    isJsonArray = true;
                }

                // If the registry returned "string" as default, try to infer a more specific type from the value
                if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
                    expectedAttributeSubType = "boolean";
                } else if (stringValue.matches("-?\\d+")) {
                    // Check if value is within Integer range vs Long range
                    try {
                        Integer.parseInt(stringValue);
                        expectedAttributeSubType = "int";
                    } catch (NumberFormatException e) {
                        // Too large for Integer, try Long
                        try {
                            Long.parseLong(stringValue);
                            expectedAttributeSubType = "long";
                        } catch (NumberFormatException ex) {
                            // Keep as string if it doesn't fit in Long either
                        }
                    }
                } else if (stringValue.matches("-?\\d*\\.\\d+([eE][+-]?\\d+)?")) {
                    // Support both regular decimal and scientific notation (e.g., 1.23E-45)
                    expectedAttributeSubType = "double";
                }
                // Otherwise, keep "string" as the default
            }

            // Step 3: Skip additional validation here - let the constraint system handle it later
            // This follows the architectural principle of not hardcoding restrictions in parsing logic

            // Step 4: Create the actual MetaAttribute instance with the expected type
            MetaAttribute actualAttr = (MetaAttribute) getTypeRegistry().createInstance(
                MetaAttribute.TYPE_ATTR, expectedAttributeSubType, attrName);

            if (actualAttr != null) {
                parentMetaData.addChild(actualAttr);
                actualAttr.setValueAsString(stringValue);

                // If this was originally a JSON array, set native isArray property
                if (isJsonArray && actualAttr instanceof MetaAttribute) {
                    ((MetaAttribute<?>) actualAttr).setArray(true);
                    log.debug("Set native isArray=true on MetaAttribute: {}", attrName);
                }

                log.debug("Created attribute [{}] with expected subtype [{}] and value [{}] on [{}:{}:{}] in file [{}]",
                    attrName, expectedAttributeSubType, stringValue, parentType, parentSubType,
                    parentMetaData.getName(), getFilename());
            } else {
                log.warn("Failed to create MetaAttribute [{}] of subtype [{}] - type not registered", attrName, expectedAttributeSubType);
            }

        } catch (Exception e) {
            String errMsg = "Failed to create inline attribute [" + attrName + "] on [" +
                parentMetaData.getType() + ":" + parentMetaData.getSubType() + ":" + parentMetaData.getName() +
                "] in file [" + getFilename() + "]";

            if (getLoader().getLoaderOptions().isStrict()) {
                throw new MetaDataException(errMsg + ": " + e.getMessage(), e);
            } else {
                log.warn(errMsg + ": " + e.getMessage());
            }
        }
    }





    /**
     * Convert JSON array string to comma-delimited format
     * Example: ["id","name"] -> "id,name"
     */
    protected String convertJsonArrayToCommaDelimited(String jsonArrayString) {
        try {
            // Parse as JSON array
            if (jsonArrayString.trim().startsWith("[") && jsonArrayString.trim().endsWith("]")) {
                // Simple parsing for basic JSON arrays
                String content = jsonArrayString.trim().substring(1, jsonArrayString.trim().length() - 1);

                // Split by comma and clean up quotes
                String[] elements = content.split(",");
                List<String> cleanedElements = new ArrayList<>();

                for (String element : elements) {
                    String cleaned = element.trim();
                    // Remove surrounding quotes if present
                    if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                    }
                    cleanedElements.add(cleaned);
                }

                return String.join(",", cleanedElements);
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON array [{}], returning as-is: {}", jsonArrayString, e.getMessage());
        }

        return jsonArrayString; // Return original if parsing fails
    }

    /**
     * Handle isArray as a native property on MetaField and MetaAttribute
     */
    protected void handleNativeIsArrayProperty(MetaData parentMetaData, String stringValue) {
        boolean isArrayValue = Boolean.parseBoolean(stringValue);

        if (parentMetaData instanceof com.metaobjects.field.MetaField) {
            com.metaobjects.field.MetaField<?> field = (com.metaobjects.field.MetaField<?>) parentMetaData;
            field.setArray(isArrayValue);
            log.debug("Set isArray={} on MetaField: {}", isArrayValue, field.getName());
        } else if (parentMetaData instanceof com.metaobjects.attr.MetaAttribute) {
            com.metaobjects.attr.MetaAttribute<?> attribute = (com.metaobjects.attr.MetaAttribute<?>) parentMetaData;
            attribute.setArray(isArrayValue);
            log.debug("Set isArray={} on MetaAttribute: {}", isArrayValue, attribute.getName());
        } else {
            log.warn("isArray property not supported on MetaData type: {} ({})",
                parentMetaData.getClass().getSimpleName(), parentMetaData.getName());
        }
    }

    // determineJsonArrayElementType() moved to JsonMetaDataParser

    // getAttributeSubTypeFromMetaData() removed - replaced with value-based pattern recognition in determineAttributeSubTypeFromValue()
    
    // getExpectedJavaTypeFromMetaData() removed - replaced with MetaAttribute-first approach
    
    // Hardcoded attribute type mapping methods removed - replaced with MetaAttribute-first approach

    // getAttributeTypeFromRegistry() and convertAttributeTypeStringToClass() removed - replaced with MetaAttribute-first approach

    // convertStringToExpectedType() removed - MetaAttribute handles its own value conversion

    // parseJsonStringArray() moved to JsonMetaDataParser

    /**
     * Determine appropriate attribute subtype from detected value type (legacy method)
     */
    protected String getAttributeSubTypeFromValue(Object value) {
        if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Integer) {
            return "int";
        } else if (value instanceof Double) {
            return "double";
        } else if (value instanceof Long) {
            return "long";
        } else {
            return "string";
        }
    }

    /**
     * Gets the expected attribute subtype from the type definition registry.
     * This follows the user's guidance to use the MetaData registry rather than guessing.
     */
    protected String getExpectedAttributeSubTypeFromRegistry(TypeDefinition typeDef, String attrName) {
        // First try to get a specific child requirement for this attribute name
        ChildRequirement specificReq = typeDef.getChildRequirement(attrName);
        if (specificReq != null && "attr".equals(specificReq.getExpectedType())) {
            return specificReq.getExpectedSubType();
        }

        // If no specific requirement, use intelligent attribute name inference
        // Boolean attributes - these should always be boolean regardless of field type
        if ("isAbstract".equals(attrName) || "required".equals(attrName) ||
            "nullable".equals(attrName) || "hasJpa".equals(attrName) ||
            "hasValidation".equals(attrName) || "hasAuditing".equals(attrName)) {
            return "boolean";
        }

        // Integer attributes - these should always be int regardless of field type
        if ("maxLength".equals(attrName) || "minLength".equals(attrName) ||
            "precision".equals(attrName) || "scale".equals(attrName)) {
            return "int";
        }

        // Field-specific attributes that should match the field's data type (except defaultValue which is special)
        if ("maxValue".equals(attrName) || "minValue".equals(attrName) || "priority".equals(attrName)) {

            // Get the parent field type to determine the matching attribute type
            String parentType = typeDef.getType();
            String parentSubType = typeDef.getSubType();

            if ("field".equals(parentType)) {
                if ("int".equals(parentSubType)) {
                    return "int";
                } else if ("long".equals(parentSubType)) {
                    return "long";
                } else if ("double".equals(parentSubType)) {
                    return "double";
                } else if ("float".equals(parentSubType)) {
                    return "float";
                } else if ("string".equals(parentSubType)) {
                    return "string";
                }
            }
        }

        // If no specific requirement, check for wildcard requirements
        // Look through all child requirements to find wildcard attribute support
        for (ChildRequirement req : typeDef.getChildRequirements()) {
            if ("attr".equals(req.getExpectedType()) && "*".equals(req.getName())) {
                // Found wildcard attribute support - default to string
                return "string";
            }
        }

        // Fallback to string if no attribute support found
        return "string";
    }

    /**
     * Determines the appropriate attribute subtype based on well-known attribute names and values.
     * This is a temporary approach until we have proper MetaData-driven type determination.
     * @deprecated Use getExpectedAttributeSubTypeFromRegistry instead
     */
    @Deprecated
    protected String determineAttributeSubType(String attrName, String stringValue) {
        // Boolean attributes
        if ("isAbstract".equals(attrName) || "required".equals(attrName) ||
            "nullable".equals(attrName) || "hasJpa".equals(attrName) ||
            "hasValidation".equals(attrName) || "hasAuditing".equals(attrName)) {
            return "boolean";
        }

        // Integer attributes
        if ("maxLength".equals(attrName) || "minLength".equals(attrName) ||
            "min".equals(attrName) || "max".equals(attrName) ||
            "precision".equals(attrName) || "scale".equals(attrName)) {
            return "int";
        }

        // Try to infer from value if it looks like a boolean or number
        if (stringValue != null) {
            if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
                return "boolean";
            }
            try {
                Integer.parseInt(stringValue);
                return "int";
            } catch (NumberFormatException e) {
                // Not a number, continue with string
            }
        }

        // Default to string
        return "string";
    }


    // isJsonArray() moved to JsonMetaDataParser
}