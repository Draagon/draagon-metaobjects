package com.draagon.meta.loader.json;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataContextRegistry;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.util.MetaDataUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced JSON MetaData Parser for metadata module
 * Supports both traditional and new compact JSON formats including:
 * - Inline attributes (@-prefixed) 
 * - Array-only format (direct arrays without "children" wrapper)
 * - Robust cross-file reference resolution
 */
public class JsonMetaDataParser {

    private static final Logger log = LoggerFactory.getLogger(JsonMetaDataParser.class);

    // Attribute constants
    protected static final String ATTR_METADATA = "metadata";
    protected static final String ATTR_CHILDREN = "children";
    protected static final String ATTR_TYPE = "type";
    protected static final String ATTR_NAME = "name";
    protected static final String ATTR_PACKAGE = "package";
    protected static final String ATTR_DEFPACKAGE = "defPackage";
    protected static final String ATTR_SUPER = "super";
    protected static final String ATTR_ISABSTRACT = "isAbstract";
    protected static final String ATTR_ISINTERFACE = "isInterface";
    protected static final String ATTR_IMPLEMENTS = "implements";

    // Reserved attributes that should not be converted to MetaAttributes
    protected static final List<String> reservedAttributes = Arrays.asList(
            ATTR_TYPE, ATTR_NAME, ATTR_PACKAGE, ATTR_SUPER, ATTR_ISABSTRACT, 
            ATTR_ISINTERFACE, ATTR_IMPLEMENTS, ATTR_CHILDREN
    );

    private final MetaDataLoader loader;
    private final String filename;
    private String defaultPackageName = "";
    private final Map<String, Integer> nameCounters = new HashMap<>();

    public JsonMetaDataParser(MetaDataLoader loader, String filename) {
        this.loader = loader;
        this.filename = filename;
    }

    /**
     * Load metadata from JSON input stream
     */
    public void loadFromStream(InputStream is) {
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();

            if (!root.has(ATTR_METADATA)) {
                throw new MetaDataException("The root 'metadata' object was not found in file [" + filename + "]");
            }

            JsonObject metadata = root.getAsJsonObject(ATTR_METADATA);

            // Set default package
            String defPkg = "";
            if (metadata.has(ATTR_DEFPACKAGE)) {
                defPkg = parsePackageValue(metadata.get(ATTR_DEFPACKAGE).getAsString());
            } else if (metadata.has(ATTR_PACKAGE)) {
                defPkg = parsePackageValue(metadata.get(ATTR_PACKAGE).getAsString());
            }
            setDefaultPackageName(defPkg);

            // Parse metadata elements - support both children and array-only formats
            if (metadata.has(ATTR_CHILDREN)) {
                JsonElement childrenElement = metadata.get(ATTR_CHILDREN);
                if (childrenElement.isJsonArray()) {
                    parseMetaData(loader, childrenElement.getAsJsonArray(), true);
                }
            } else {
                // Check for array-only format - direct array without "children" wrapper
                for (Map.Entry<String, JsonElement> entry : metadata.entrySet()) {
                    if (!entry.getKey().equals(ATTR_PACKAGE) && !entry.getKey().equals(ATTR_DEFPACKAGE) 
                        && entry.getValue().isJsonArray()) {
                        // Found direct array - this is array-only format
                        parseMetaData(loader, entry.getValue().getAsJsonArray(), true);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new MetaDataException("Error loading MetaData from file [" + filename + "]: " + ex.getMessage(), ex);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    /**
     * Parse metadata array - new format only
     */
    protected void parseMetaData(MetaData parent, JsonArray children, boolean isRoot) {
        for (JsonElement child : children) {
            if (child.isJsonObject()) {
                JsonObject childObj = child.getAsJsonObject();
                
                // New format: {"field": {"name": "...", "type": "..."}}
                String typeName = childObj.keySet().iterator().next();
                JsonObject el = childObj.getAsJsonObject(typeName);

                // Extract metadata properties
                String subTypeName = getValueAsString(el, ATTR_TYPE);
                String name = getValueAsString(el, ATTR_NAME);
                String packageName = getValueAsString(el, ATTR_PACKAGE);
                String superName = getValueAsString(el, ATTR_SUPER);
                Boolean isAbstract = getValueAsBoolean(el, ATTR_ISABSTRACT);
                Boolean isInterface = getValueAsBoolean(el, ATTR_ISINTERFACE);
                String implementsArray = getValueAsString(el, ATTR_IMPLEMENTS);

                // Validate type exists in registry
                if (!getTypeRegistry().hasType(typeName)) {
                    log.warn("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + filename + "]");
                    continue;
                }

                // Create MetaData using proven FileMetaDataParser approach
                MetaData md = createOrOverlayMetaData(isRoot, parent, typeName, subTypeName,
                        name, packageName, superName, isAbstract, isInterface, implementsArray);

                // Handle attributes differently for MetaAttribute vs normal MetaData
                if (md instanceof MetaAttribute) {
                    parseMetaAttributeValue((MetaAttribute) md, el);
                } else {
                    // Parse attributes including inline @-prefixed ones
                    parseAttributes(md, el);

                    // Parse children - support both formats
                    if (el.has(ATTR_CHILDREN)) {
                        JsonElement childrenElement = el.get(ATTR_CHILDREN);
                        if (childrenElement.isJsonArray()) {
                            parseMetaData(md, childrenElement.getAsJsonArray(), false);
                        }
                    } else {
                        // Check for array-only format at this level
                        for (Map.Entry<String, JsonElement> entry : el.entrySet()) {
                            if (!reservedAttributes.contains(entry.getKey()) && 
                                !entry.getKey().startsWith("@") &&
                                entry.getValue().isJsonArray()) {
                                // Found direct array
                                parseMetaData(md, entry.getValue().getAsJsonArray(), false);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create or overlay MetaData - adapted from proven FileMetaDataParser logic
     */
    protected MetaData createOrOverlayMetaData(boolean isRoot, MetaData parent, String typeName, String subTypeName,
                                               String name, String packageName, String superName,
                                               Boolean isAbstract, Boolean isInterface, String implementsArray) {

        if (subTypeName != null && subTypeName.equals("*")) subTypeName = null;

        // Auto-generate name if not provided
        if (name == null || name.equals("")) {
            String namePrefix = (subTypeName != null && !subTypeName.isEmpty()) 
                    ? subTypeName.toLowerCase() 
                    : typeName.toLowerCase();
            name = getNextNamePrefix(parent, typeName, namePrefix);
            
            if (name == null) {
                throw new MetaDataException("MetaData [" + typeName + "] found on parent [" + parent
                        + "] had no name specified and auto-naming failed in file [" + filename + "]");
            }
        }

        // Resolve package name
        if (packageName == null || packageName.trim().isEmpty()) {
            // Inherit package from parent or use default
            if (!isRoot && parent != null && parent.getName().contains(MetaDataLoader.PKG_SEPARATOR)) {
                String parentName = parent.getName();
                int lastSep = parentName.lastIndexOf(MetaDataLoader.PKG_SEPARATOR);
                if (lastSep > 0) {
                    packageName = parentName.substring(0, lastSep);
                } else {
                    packageName = defaultPackageName;
                }
            } else {
                packageName = defaultPackageName;
            }
        } else {
            // Convert relative paths using proven MetaDataUtil approach
            packageName = MetaDataUtil.expandPackageForPath(defaultPackageName, packageName);
        }

        MetaData md = null;

        // Check if MetaData already exists (for overlay scenarios)
        try {
            if (isRoot && packageName.length() > 0) {
                md = parent.getChildOfType(typeName, packageName + MetaDataLoader.PKG_SEPARATOR + name);
            } else {
                md = parent.getChildOfType(typeName, name);
                if (md.getParent() != parent) {
                    md = md.overload();
                    parent.addChild(md);
                }
            }
        } catch (MetaDataNotFoundException e) {
            // Expected - will create new MetaData
        }

        // Create new MetaData if it doesn't exist
        if (md == null) {
            // Get super metadata using proven reference resolution
            MetaData superData = getSuperMetaData(parent, typeName, name, packageName, superName);

            // Create using registry
            md = createNewMetaData(isRoot, parent, typeName, subTypeName, name, packageName, superData);

            // Add to parent
            parent.addChild(md);

            // Set super data
            if (superData != null) {
                md.setSuperData(superData);
            }
        }

        return md;
    }

    /**
     * Get super MetaData using proven FileMetaDataParser approach
     */
    protected MetaData getSuperMetaData(MetaData parent, String typeName, String name, String packageName, String superName) {
        MetaData superData = null;

        if (superName != null && !superName.isEmpty()) {
            // Try package-prefixed lookup first
            try {
                if (superName.indexOf(MetaDataLoader.PKG_SEPARATOR) < 0 && packageName.length() > 0) {
                    superData = loader.getChildOfType(typeName, packageName + MetaDataLoader.PKG_SEPARATOR + superName);
                }
            } catch (MetaDataNotFoundException e) {
                // Expected - try fully qualified resolution
            }

            // Try fully qualified name resolution
            if (superData == null) {
                String fullyQualifiedSuperName = getFullyQualifiedSuperMetaDataName(parent, packageName, superName);
                try {
                    superData = loader.getChildOfType(typeName, fullyQualifiedSuperName);
                } catch (MetaDataNotFoundException e) {
                    throw new MetaDataException("Invalid MetaData [" + typeName + "][" + name + "] on parent [" + parent
                            + "], the SuperClass [" + superName + "] does not exist in file [" + filename + "]");
                }
            }
        }

        return superData;
    }

    /**
     * Get fully qualified super metadata name using MetaDataUtil
     */
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


    /**
     * Create new MetaData using registry
     */
    protected MetaData createNewMetaData(boolean isRoot, MetaData parent, String typeName, String subTypeName, 
                                         String name, String packageName, MetaData superData) {
        
        if (subTypeName != null && subTypeName.isEmpty()) subTypeName = null;

        // Use the Super class type if no type is defined and a super class exists
        if (subTypeName == null && superData != null) {
            subTypeName = superData.getSubTypeName();
        }

        String fullName = (isRoot && !packageName.isEmpty()) 
                ? packageName + MetaDataLoader.PKG_SEPARATOR + name 
                : name;

        MetaData newMetaData = getTypeRegistry().createInstance(typeName, subTypeName, fullName);
        
        if (newMetaData == null) {
            throw new MetaDataException("MetaData [type=" + typeName + "][subType=" + subTypeName + "][name=" + name
                    + "] could not be created by registry in file [" + filename + "]");
        }

        return newMetaData;
    }

    /**
     * Parse attributes including inline @-prefixed ones with type casting
     */
    protected void parseAttributes(MetaData md, JsonObject el) {
        el.entrySet().forEach(entry -> {
            String attrName = entry.getKey();
            if (!reservedAttributes.contains(attrName)) {
                if (attrName.startsWith("@")) {
                    // Inline attribute with type casting
                    parseInlineAttribute(md, attrName, entry.getValue());
                } else {
                    // Regular attribute - check if inline attributes are supported
                    if (supportsInlineAttributes(md)) {
                        // Use inline attribute parsing approach for type conversion
                        parseInlineAttribute(md, "@" + attrName, entry.getValue());
                    } else {
                        // Fallback to simple string attribute
                        String value = entry.getValue().getAsString();
                        createAttributeOnParent(md, attrName, value);
                    }
                }
            }
        });
    }

    /**
     * Parse inline attribute (@-prefixed) with type casting - uses common method
     */
    protected void parseInlineAttribute(MetaData md, String attrName, JsonElement jsonValue) {
        // Remove @ prefix if present
        String cleanAttrName = attrName.startsWith("@") ? attrName.substring(1) : attrName;
        
        // Convert JSON value to string and use common method
        String stringValue = null;
        if (jsonValue != null && !jsonValue.isJsonNull()) {
            if (jsonValue.isJsonPrimitive() && jsonValue.getAsJsonPrimitive().isBoolean()) {
                stringValue = String.valueOf(jsonValue.getAsBoolean());
            } else if (jsonValue.isJsonPrimitive() && jsonValue.getAsJsonPrimitive().isNumber()) {
                stringValue = jsonValue.getAsString();
            } else {
                stringValue = jsonValue.getAsString();
            }
        }
        
        // Use common parseInlineAttribute method
        parseInlineAttribute(md, cleanAttrName, stringValue);
    }

    /**
     * Parse MetaAttribute value
     */
    protected void parseMetaAttributeValue(MetaAttribute attr, JsonObject el) {
        // Implementation for MetaAttribute value parsing
        if (el.has("value")) {
            JsonElement valueElement = el.get("value");
            
            // Handle stringArray attributes specially - convert single strings to Lists
            if (attr.getSubTypeName() != null && attr.getSubTypeName().equals("stringArray")) {
                if (valueElement.isJsonPrimitive()) {
                    // Single string value - convert to single-item list
                    String singleValue = valueElement.getAsString();
                    String[] parts = singleValue.split(",");  // Support comma-separated values
                    attr.setValue(java.util.Arrays.asList(parts));
                } else if (valueElement.isJsonArray()) {
                    // Already an array - convert to string list
                    JsonArray jsonArray = valueElement.getAsJsonArray();
                    java.util.List<String> stringList = new java.util.ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        stringList.add(jsonArray.get(i).getAsString());
                    }
                    attr.setValue(stringList);
                } else {
                    attr.setValue(valueElement.getAsString());
                }
            } else {
                // Regular attribute - use string value
                attr.setValue(valueElement.getAsString());
            }
        }
    }

    /**
     * Create attribute on parent MetaData - improved version from base class
     */
    protected void createAttributeOnParent(MetaData parentMetaData, String attrName, String value) {

        String parentType = parentMetaData.getTypeName();
        String parentSubType = parentMetaData.getSubTypeName();

        // Create attribute using context-aware registry system
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
            
            // Log warning (could be made configurable in future)
            log.warn(errMsg + ": " + e.getMessage());
        }
    }

    /**
     * Generate next sequential name for auto-naming
     */
    protected String getNextNamePrefix(MetaData parent, String typeName, String namePrefix) {
        String key = parent.getName() + "::" + typeName + "::" + namePrefix;
        int counter = nameCounters.getOrDefault(key, 0) + 1;
        nameCounters.put(key, counter);
        return namePrefix + counter;
    }

    // Utility methods
    protected String getValueAsString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : null;
    }

    protected Boolean getValueAsBoolean(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : null;
    }

    protected String parsePackageValue(String pkg) {
        return pkg != null ? pkg.trim() : "";
    }

    protected void setDefaultPackageName(String packageName) {
        this.defaultPackageName = packageName != null ? packageName : "";
    }

    protected MetaDataTypeRegistry getTypeRegistry() {
        return loader.getTypeRegistry();
    }

    protected String getFilename() {
        return filename;
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
    
    /**
     * Cast JSON value to appropriate Java type based on content pattern - uses common casting logic
     */
    protected Object castJsonValueToObject(JsonElement jsonValue) {
        if (jsonValue == null || jsonValue.isJsonNull()) {
            return null;
        }
        
        if (jsonValue.isJsonPrimitive()) {
            if (jsonValue.getAsJsonPrimitive().isBoolean()) {
                return jsonValue.getAsBoolean();
            } else if (jsonValue.getAsJsonPrimitive().isNumber()) {
                // Use common type casting method
                return castStringValueToObject(jsonValue.getAsString());
            } else {
                return jsonValue.getAsString();
            }
        }
        
        // Default to string representation
        return jsonValue.toString();
    }
}