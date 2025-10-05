package com.metaobjects.loader.parser.json;

import com.metaobjects.MetaData;
import com.metaobjects.MetaDataException;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.loader.parser.BaseMetaDataParser;
import com.metaobjects.loader.parser.MetaDataFileParser;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.util.MetaDataUtil;
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
public class JsonMetaDataParser extends BaseMetaDataParser implements MetaDataFileParser {

    private static final Logger log = LoggerFactory.getLogger(JsonMetaDataParser.class);

    // Attribute constants
    protected static final String ATTR_METADATA = "metadata";
    protected static final String ATTR_CHILDREN = "children";
    // REMOVED: local ATTR_TYPE - using ATTR_SUBTYPE from BaseMetaDataParser
    protected static final String ATTR_NAME = "name";
    protected static final String ATTR_PACKAGE = "package";
    protected static final String ATTR_DEFPACKAGE = "defPackage";
    protected static final String ATTR_SUPER = "super";
    protected static final String ATTR_ISABSTRACT = "isAbstract";
    protected static final String ATTR_ISINTERFACE = "isInterface";
    protected static final String ATTR_IMPLEMENTS = "implements";
    protected static final String ATTR_OVERLAY = "overlay";

    /** Attribute prefix for inline JSON attributes */
    public static final String JSON_ATTR_PREFIX = "@";

    // Reserved attributes that should not be converted to MetaAttributes
    protected static final List<String> reservedAttributes = Arrays.asList(
            ATTR_SUBTYPE, ATTR_NAME, ATTR_PACKAGE, ATTR_SUPER, ATTR_ISABSTRACT,
            ATTR_ISINTERFACE, ATTR_IMPLEMENTS, ATTR_CHILDREN, ATTR_OVERLAY
    );

    private final Map<String, Integer> nameCounters = new HashMap<>();

    public JsonMetaDataParser(MetaDataLoader loader, String filename) {
        super(loader, filename);
    }

    /**
     * Load metadata from JSON input stream
     */
    public void loadFromStream(InputStream is) {
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();

            if (!root.has(ATTR_METADATA)) {
                throw new MetaDataException("The root 'metadata' object was not found in file [" + getFilename() + "]");
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
                    parseMetaData(getLoader(), childrenElement.getAsJsonArray(), false);
                }
            } else {
                // Check for array-only format - direct array without "children" wrapper
                for (Map.Entry<String, JsonElement> entry : metadata.entrySet()) {
                    if (!entry.getKey().equals(ATTR_PACKAGE) && !entry.getKey().equals(ATTR_DEFPACKAGE)
                        && entry.getValue().isJsonArray()) {
                        // Found direct array - this is array-only format
                        parseMetaData(getLoader(), entry.getValue().getAsJsonArray(), false);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new MetaDataException("Error loading MetaData from file [" + getFilename() + "]: " + ex.getMessage(), ex);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    /**
     * Parse metadata array - new format only
     */
    protected void parseMetaData(MetaData parent, JsonArray children, boolean isRoot) {
        // FIXED: Don't create implicit objects for top-level fields
        // Fields should be added directly to the loader with their proper package context
        // This enables cross-file references like "..::common::id" to work correctly

        for (JsonElement child : children) {
            if (child.isJsonObject()) {
                JsonObject childObj = child.getAsJsonObject();

                // New format: {"field": {"name": "...", "subType": "..."}}
                String typeName = childObj.keySet().iterator().next();
                JsonObject el = childObj.getAsJsonObject(typeName);

                // Extract metadata properties
                String subTypeName = getValueAsString(el, ATTR_SUBTYPE);
                String name = getValueAsString(el, ATTR_NAME);
                String packageName = getValueAsString(el, ATTR_PACKAGE);
                String superName = getValueAsString(el, ATTR_SUPER);
                Boolean isAbstract = getValueAsBoolean(el, ATTR_ISABSTRACT);
                Boolean isInterface = getValueAsBoolean(el, ATTR_ISINTERFACE);
                String implementsArray = getValueAsString(el, ATTR_IMPLEMENTS);
                Boolean isOverlay = getValueAsBoolean(el, ATTR_OVERLAY);
                Boolean isOverride = getValueAsBoolean(el, "override");

                MetaData md;
                
                // Handle override case - reference existing metadata by name
                if (Boolean.TRUE.equals(isOverride)) {
                    // Override case: look up existing metadata by name
                    try {
                        md = parent.getChildOfType(typeName, name);
                        log.debug("Found existing metadata for override: {} in parent {}", name, parent.getName());
                    } catch (Exception e) {
                        log.warn("Override requested but metadata [{}] not found in parent [{}] in file [{}]", 
                                name, parent.getName(), getFilename());
                        continue;
                    }
                } else {
                    // Normal case: validate type and create/overlay metadata
                    if (!getTypeRegistry().hasType(typeName)) {
                        log.warn("Unknown type [" + typeName + "] found on parent metadata [" + parent + "] in file [" + getFilename() + "]");
                        continue;
                    }

                    // Create MetaData using proven FileMetaDataParser approach
                    md = createOrOverlayMetaData(isRoot, parent, typeName, subTypeName,
                            name, packageName, superName, isAbstract, isInterface, implementsArray, isOverlay);
                }

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
     * Check if the children array has field or identity elements that need an implicit object wrapper
     */
    protected boolean hasFieldElements(JsonArray children) {
        for (JsonElement child : children) {
            if (child.isJsonObject()) {
                JsonObject childObj = child.getAsJsonObject();
                for (String key : childObj.keySet()) {
                    if ("field".equals(key) || "identity".equals(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }





    /**
     * Parse attributes including inline @-prefixed ones with type casting
     * Enforces @ prefix requirement for all MetaAttributes in JSON format
     */
    protected void parseAttributes(MetaData md, JsonObject el) {
        el.entrySet().forEach(entry -> {
            String attrName = entry.getKey();
            if (!reservedAttributes.contains(attrName)) {
                if (attrName.startsWith("@")) {
                    // Inline attribute with @ prefix - correct format
                    parseInlineAttribute(md, attrName, entry.getValue());
                } else {
                    // No @ prefix - this violates JSON inline attribute requirements
                    if (getLoader().getLoaderOptions().isStrict()) {
                        throw new MetaDataException("JSON inline attributes must use @ prefix. " +
                            "Found attribute [" + attrName + "] without @ prefix on [" +
                            md.getType() + ":" + md.getSubType() + ":" + md.getName() + "] in file [" + getFilename() + "]. " +
                            "Change to [@" + attrName + "] to fix this issue.");
                    } else {
                        log.warn("JSON attribute [{}] should use @ prefix: @{} on [{}:{}:{}] in file [{}]",
                            attrName, attrName, md.getType(), md.getSubType(), md.getName(), getFilename());
                        // Still try to process it as an attribute
                        parseInlineAttribute(md, attrName, entry.getValue());
                    }
                }
            }
        });
    }

    /**
     * Parse inline attribute (@-prefixed) with MetaField-aware type conversion
     */
    protected void parseInlineAttribute(MetaData md, String attrName, JsonElement jsonValue) {
        // Remove @ prefix if present
        String cleanAttrName = attrName.startsWith("@") ? attrName.substring(1) : attrName;
        
        // Skip null values
        if (jsonValue == null || jsonValue.isJsonNull()) {
            return;
        }
        
        // Convert JSON value to string for processing by base parser
        String stringValue = jsonValueToString(jsonValue);
        
        // Delegate to base parser which will use MetaField type information
        super.parseInlineAttribute(md, cleanAttrName, stringValue);
    }
    
    /**
     * Convert JSON value to string representation, preserving JSON array format for base parser detection
     */
    private String jsonValueToString(JsonElement jsonValue) {
        if (jsonValue.isJsonPrimitive()) {
            if (jsonValue.getAsJsonPrimitive().isBoolean()) {
                return String.valueOf(jsonValue.getAsBoolean());
            } else if (jsonValue.getAsJsonPrimitive().isNumber()) {
                return jsonValue.getAsString();
            } else {
                return jsonValue.getAsString();
            }
        } else if (jsonValue.isJsonArray()) {
            // Preserve JSON array format for base parser array detection
            return jsonValue.toString();
        } else {
            // Other complex JSON value -> JSON representation
            return jsonValue.toString();
        }
    }

    /**
     * Convert JSON array to comma-delimited string format
     */
    private String convertJsonArrayToCommaDelimited(JsonArray jsonArray) {
        if (jsonArray.size() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (i > 0) {
                result.append(",");
            }

            JsonElement element = jsonArray.get(i);
            if (element.isJsonPrimitive()) {
                String value = element.getAsString();
                // Add the raw value without quotes - StringArrayAttribute will handle parsing
                result.append(value);
            } else {
                // For non-primitive elements, convert to string representation
                result.append(element.toString());
            }
        }

        return result.toString();
    }


    /**
     * Parse MetaAttribute value
     */
    protected void parseMetaAttributeValue(MetaAttribute attr, JsonObject el) {
        // Implementation for MetaAttribute value parsing
        if (el.has("value")) {
            JsonElement valueElement = el.get("value");
            
            // Handle stringArray attributes specially - convert single strings to Lists
            if (attr.getSubType() != null && attr.getSubType().equals("stringArray")) {
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
     * Create StringArray attribute on parent MetaData for keys and similar attributes
     */
    protected void createStringArrayAttributeOnParent(MetaData parentMetaData, String attrName, String[] values) {
        try {
            // Create StringArrayAttribute using the factory method with comma-separated string
            String commaSeparatedValues = String.join(",", values);
            com.metaobjects.attr.StringArrayAttribute attr = com.metaobjects.attr.StringArrayAttribute.create(attrName, commaSeparatedValues);
            parentMetaData.addChild(attr);
            
            log.debug("Successfully created StringArrayAttribute [{}] with values [{}] on parent [{}:{}:{}]", 
                attrName, commaSeparatedValues, parentMetaData.getType(), parentMetaData.getSubType(), parentMetaData.getName());
                
        } catch (Exception e) {
            throw new MetaDataException("Failed to create StringArrayAttribute [" + attrName + "] on parent [" + 
                parentMetaData.getType() + ":" + parentMetaData.getSubType() + ":" + parentMetaData.getName() + 
                "] in file [" + getFilename() + "]: " + e.getMessage(), e);
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

    
    @Override
    public MetaDataLoader getLoader() {
        return loader;
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

    /**
     * Check if a string value represents a JSON array (starts with [ and ends with ])
     */
    protected boolean isJsonArray(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmed = value.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]");
    }

    /**
     * Parse JSON string array and convert to comma-delimited format for StringAttribute storage
     * Example: '["id"]' -> "id", '["basketId", "fruitId"]' -> "basketId,fruitId"
     */
    protected String parseJsonStringArray(String stringValue) {
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return null;
        }

        try {
            // Parse as JSON array: ["id"] or ["basketId", "fruitId"]
            JsonElement element = JsonParser.parseString(stringValue);
            if (element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();
                return convertJsonArrayToCommaDelimited(jsonArray);
            }
        } catch (Exception e) {
            // Backward compatibility: if not valid JSON, treat as literal string
            log.debug("Could not parse as JSON array, treating as literal string: {}", stringValue);
        }

        // Fallback for escaped JSON strings: "[\"id\"]" -> "id"
        if (stringValue.startsWith("[\"") && stringValue.endsWith("\"]")) {
            return stringValue.substring(2, stringValue.length() - 2);
        }

        return stringValue; // Return as-is for other cases
    }

}