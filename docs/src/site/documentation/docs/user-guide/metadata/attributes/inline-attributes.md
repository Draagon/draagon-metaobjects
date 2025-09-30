# Inline Attributes

Inline attributes are a key feature of MetaObjects that dramatically reduce metadata verbosity by allowing attributes to be specified directly within metadata definitions rather than as separate child elements. This feature provides approximately **60% reduction** in JSON file size for attribute-heavy metadata.

## Overview

Traditional metadata systems require verbose nested structures for attributes. MetaObjects' inline attribute syntax provides a clean, readable alternative that maintains full type safety and validation.

### Traditional vs Inline Syntax

**Traditional Verbose Format:**
```json
{
  "field": {
    "name": "email",
    "subType": "string",
    "children": [
      {
        "attr": {
          "name": "required",
          "subType": "boolean",
          "value": true
        }
      },
      {
        "attr": {
          "name": "maxLength",
          "subType": "int",
          "value": 255
        }
      },
      {
        "attr": {
          "name": "pattern",
          "subType": "string",
          "value": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"
        }
      }
    ]
  }
}
```

**Modern Inline Format:**
```json
{
  "field": {
    "name": "email",
    "subType": "string",
    "@required": true,
    "@maxLength": 255,
    "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"
  }
}
```

**Reduction**: From 22 lines to 8 lines (64% reduction)

## Format Support

MetaObjects supports inline attributes in both JSON and XML formats with different syntax conventions:

### JSON Format (@-prefixed)

JSON inline attributes use the `@` prefix to distinguish them from standard metadata properties:

```json
{
  "metadata": {
    "package": "com_example_model",
    "children": [
      {
        "object": {
          "name": "User",
          "subType": "pojo",
          "@dbTable": "users",
          "@auditable": true,
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long",
                "@required": true,
                "@dbColumn": "user_id"
              }
            },
            {
              "field": {
                "name": "email",
                "subType": "string",
                "@required": true,
                "@maxLength": 255,
                "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$",
                "@dbColumn": "email_address",
                "@unique": true
              }
            },
            {
              "field": {
                "name": "status",
                "subType": "string",
                "@required": true,
                "@defaultValue": "active",
                "@allowedValues": ["active", "inactive", "pending"]
              }
            },
            {
              "key": {
                "name": "primary",
                "subType": "primary",
                "@keys": ["id"],
                "@autoIncrementStrategy": "sequential"
              }
            }
          ]
        }
      }
    ]
  }
}
```

#### JSON Syntax Rules

1. **Prefix**: All inline attributes must start with `@`
2. **Naming**: Attribute names follow standard identifier patterns (no spaces, special chars)
3. **Values**: JSON values (strings, numbers, booleans, arrays)
4. **Placement**: Can appear anywhere within the metadata object definition

### XML Format (Direct Attributes)

XML inline attributes use standard XML attribute syntax without prefixes:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata package="com_example_model">
  <children>
    <object name="User" subType="pojo" dbTable="users" auditable="true">
      <children>
        <field name="id" subType="long"
               required="true"
               dbColumn="user_id" />

        <field name="email" subType="string"
               required="true"
               maxLength="255"
               pattern="^[\w._%+-]+@[\w.-]+\.[A-Za-z]{2,}$"
               dbColumn="email_address"
               unique="true" />

        <field name="status" subType="string"
               required="true"
               defaultValue="active"
               allowedValues="active,inactive,pending" />

        <key name="primary" subType="primary"
             keys="id"
             autoIncrementStrategy="sequential" />
      </children>
    </object>
  </children>
</metadata>
```

#### XML Syntax Rules

1. **No Prefix**: XML attributes use standard XML syntax
2. **Value Types**: All values are strings that get type-converted
3. **Arrays**: Comma-separated values for array types
4. **Escaping**: Standard XML escaping for special characters

## Automatic Type Conversion

MetaObjects automatically converts inline attribute values to appropriate typed attributes based on the expected attribute type:

### Type Detection System

The parser uses intelligent type detection to create the correct attribute types:

```java
// Type-aware parsing system
public Class<?> getExpectedAttributeType(String attributeName) {
    switch (attributeName) {
        case "required":
        case "unique":
        case "auditable":
        case "isPrimaryKey":
            return Boolean.class;

        case "maxLength":
        case "minLength":
        case "displayOrder":
        case "retentionDays":
            return Integer.class;

        case "minValue":
        case "maxValue":
            return Long.class;

        case "scale":
        case "precision":
            return Double.class;

        default:
            return String.class;
    }
}
```

### Conversion Examples

**Boolean Conversion:**
```json
"@required": true        → BooleanAttribute("required", true)
"@unique": false         → BooleanAttribute("unique", false)
```

**Integer Conversion:**
```json
"@maxLength": 255        → IntAttribute("maxLength", 255)
"@displayOrder": 10      → IntAttribute("displayOrder", 10)
```

**String Conversion:**
```json
"@dbColumn": "email_address"  → StringAttribute("dbColumn", "email_address")
"@pattern": "^[a-zA-Z]+$"     → StringAttribute("pattern", "^[a-zA-Z]+$")
```

**Array Conversion:**
```json
"@allowedValues": ["active", "inactive"]  → StringArrayAttribute("allowedValues", ["active", "inactive"])
"@tags": ["user", "contact"]             → StringArrayAttribute("tags", ["user", "contact"])
```

### XML String-to-Type Conversion

XML attributes are parsed as strings and converted to appropriate types:

```xml
<!-- Boolean conversion -->
required="true"          → BooleanAttribute("required", true)
auditable="false"        → BooleanAttribute("auditable", false)

<!-- Numeric conversion -->
maxLength="255"          → IntAttribute("maxLength", 255)
scale="2.5"              → DoubleAttribute("scale", 2.5)

<!-- Array conversion (comma-separated) -->
allowedValues="active,inactive,pending"  → StringArrayAttribute("allowedValues", ["active", "inactive", "pending"])
```

## Parser Implementation

### BaseMetaDataParser Integration

The inline attribute system is implemented in the `BaseMetaDataParser` class, providing consistent behavior across JSON and XML parsers:

```java
public abstract class BaseMetaDataParser {

    /**
     * Parse inline attributes with type-aware conversion
     */
    protected void parseInlineAttribute(MetaData md, String attrName, String stringValue) {
        // Determine expected type from metadata field information
        String attributeSubType = getAttributeSubTypeFromMetaData(md, attrName);
        Class<?> expectedType = getExpectedJavaTypeFromMetaData(md, attrName);

        // Convert string value to expected type
        Object castedValue = convertStringToExpectedType(stringValue, expectedType);
        String finalValue = castedValue != null ? castedValue.toString() : null;

        // Create typed attribute
        createInlineAttributeWithDetectedType(md, attrName, finalValue, attributeSubType);
    }

    /**
     * Create appropriate attribute type based on detected type
     */
    private void createInlineAttributeWithDetectedType(MetaData md, String attrName,
                                                      String value, String subType) {
        MetaAttribute attr = switch (subType) {
            case "boolean" -> new BooleanAttribute(attrName);
            case "int" -> new IntAttribute(attrName);
            case "long" -> new LongAttribute(attrName);
            case "double" -> new DoubleAttribute(attrName);
            case "stringarray" -> new StringArrayAttribute(attrName);
            default -> new StringAttribute(attrName);
        };

        attr.setValueAsString(value);
        md.addChild(attr);
    }
}
```

### JSON Parser Implementation

The `JsonMetaDataParser` extends the base parser with JSON-specific handling:

```java
public class JsonMetaDataParser extends BaseMetaDataParser {

    public static final String JSON_ATTR_PREFIX = "@";

    protected void parseJsonObject(JsonObject jsonObj, MetaData parent) {
        for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (key.startsWith(JSON_ATTR_PREFIX)) {
                // Handle inline attribute
                String attrName = key.substring(1); // Remove @ prefix
                parseInlineAttributeFromJson(parent, attrName, value);
            } else {
                // Handle regular metadata property
                parseMetadataProperty(parent, key, value);
            }
        }
    }

    private void parseInlineAttributeFromJson(MetaData md, String attrName, JsonElement value) {
        String stringValue = convertJsonElementToString(value);
        parseInlineAttribute(md, attrName, stringValue);
    }
}
```

### XML Parser Implementation

The `XMLMetaDataParser` handles XML attribute syntax:

```java
public class XMLMetaDataParser extends BaseMetaDataParser {

    protected void parseXmlElement(Element element, MetaData parent) {
        // Parse XML attributes as inline attributes
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();

            // Skip standard metadata attributes (name, type, etc.)
            if (!isStandardMetadataAttribute(attrName)) {
                parseInlineAttribute(parent, attrName, attrValue);
            }
        }

        // Parse child elements
        parseChildElements(element, parent);
    }
}
```

## Validation and Error Handling

### Parse-Time Validation

Inline attributes are validated immediately during parsing to provide fast feedback:

```java
public void validateInlineAttributeUsage(MetaData md, String attrName) {
    // Check if attribute type is registered
    String defaultSubType = registry.getDefaultSubType("attr");
    if (defaultSubType == null) {
        throw new MetaDataException(
            "Inline attributes require attr type default subType to be registered. " +
            "Ensure AttributeTypesMetaDataProvider is loaded."
        );
    }

    // Validate attribute naming pattern
    if (!attrName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
        throw new MetaDataException(
            String.format("Invalid inline attribute name '%s'. " +
                         "Must follow identifier pattern: ^[a-zA-Z][a-zA-Z0-9_]*$", attrName)
        );
    }
}
```

### Error Context

Rich error messages help developers debug inline attribute issues:

```java
try {
    parseInlineAttribute(metaData, attrName, value);
} catch (Exception e) {
    throw new MetaDataException(
        String.format("Failed to parse inline attribute '@%s' with value '%s' on %s '%s': %s",
                     attrName, value, metaData.getType(), metaData.getName(), e.getMessage()),
        e,
        Optional.of(MetaDataPath.from(metaData).append("@" + attrName))
    );
}
```

## Best Practices

### 1. Use Consistent Naming Conventions

```json
// ✅ GOOD - Consistent camelCase naming
{
  "@required": true,
  "@maxLength": 255,
  "@dbColumn": "email_address",
  "@displayName": "Email Address"
}

// ❌ AVOID - Inconsistent naming
{
  "@Required": true,
  "@max_length": 255,
  "@db-column": "email_address"
}
```

### 2. Group Related Attributes

```json
// ✅ GOOD - Logical grouping for readability
{
  "field": {
    "name": "email",
    "type": "string",

    // Validation attributes
    "@required": true,
    "@maxLength": 255,
    "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$",

    // Database attributes
    "@dbColumn": "email_address",
    "@unique": true,
    "@indexed": true,

    // UI attributes
    "@displayName": "Email Address",
    "@helpText": "Enter a valid email address",
    "@displayOrder": 10
  }
}
```

### 3. Use Type-Appropriate Values

```json
// ✅ GOOD - Proper JSON types
{
  "@required": true,           // Boolean, not "true"
  "@maxLength": 255,           // Number, not "255"
  "@allowedValues": ["a", "b"] // Array, not "a,b"
}

// ❌ AVOID - String values for non-string types
{
  "@required": "true",         // Should be boolean
  "@maxLength": "255",         // Should be number
  "@allowedValues": "a,b"      // Should be array
}
```

### 4. Validate Regex Patterns

```json
// ✅ GOOD - Properly escaped regex
{
  "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"
}

// ❌ AVOID - Unescaped backslashes
{
  "@pattern": "^[\w._%+-]+@[\w.-]+\.[A-Za-z]{2,}$"
}
```

### 5. Use Comments for Complex Patterns

```json
{
  "field": {
    "name": "phoneNumber",
    "type": "string",

    // International phone number pattern (E.164 format)
    "@pattern": "^\\+[1-9]\\d{1,14}$",
    "@maxLength": 15,

    // Example: +1234567890
    "@defaultValue": "+1234567890"
  }
}
```

## Common Attribute Patterns

### Database Mapping

```json
{
  "object": {
    "name": "User",
    "subType": "pojo",
    "@dbTable": "users",
    "@dbSchema": "public",
    "@auditable": true
  }
}

{
  "field": {
    "name": "email",
    "subType": "string",
    "@dbColumn": "email_address",
    "@dbType": "VARCHAR(255)",
    "@nullable": false,
    "@unique": true,
    "@indexed": true
  }
}
```

### Validation Rules

```json
{
  "field": {
    "name": "age",
    "subType": "int",
    "@required": true,
    "@minValue": 0,
    "@maxValue": 150,
    "@defaultValue": 18
  }
}

{
  "field": {
    "name": "password",
    "subType": "string",
    "@required": true,
    "@minLength": 8,
    "@maxLength": 128,
    "@pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"
  }
}
```

### UI Configuration

```json
{
  "field": {
    "name": "email",
    "subType": "string",
    "@displayName": "Email Address",
    "@helpText": "Enter your primary email address",
    "@placeholder": "user@example.com",
    "@uiComponent": "email-input",
    "@displayOrder": 10,
    "@cssClasses": ["form-control", "required-field"],
    "@readonly": false,
    "@hidden": false
  }
}
```

### Business Rules

```json
{
  "field": {
    "name": "ssn",
    "subType": "string",
    "@required": true,
    "@pattern": "^\\d{3}-\\d{2}-\\d{4}$",
    "@encrypted": true,
    "@piiData": true,
    "@retentionDays": 2555,
    "@accessLevel": "confidential"
  }
}
```

## Performance Implications

### Parsing Performance

Inline attributes provide significant parsing performance benefits:

- **Reduced object creation**: Fewer nested objects to parse
- **Direct assignment**: Values assigned directly during parsing
- **Smaller memory footprint**: Less complex object graph

### Runtime Performance

- **Cached access**: Attributes cached using MetaObjects' cache strategy
- **Type-safe retrieval**: No runtime type conversion needed
- **Optimized lookups**: Direct hash map access for attribute retrieval

### Memory Usage

- **60% reduction**: In JSON file size for attribute-heavy metadata
- **Fewer objects**: Less memory pressure during loading
- **Efficient storage**: Typed attribute values stored directly

## Migration from Verbose Format

### Automated Conversion

You can convert existing verbose metadata to inline format:

```java
public class MetadataFormatConverter {

    public JsonObject convertToInlineFormat(JsonObject verbose) {
        JsonObject result = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : verbose.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if ("children".equals(key) && value.isJsonArray()) {
                JsonArray children = value.getAsJsonArray();
                convertChildrenToInline(result, children);
            } else {
                result.add(key, value);
            }
        }

        return result;
    }

    private void convertChildrenToInline(JsonObject parent, JsonArray children) {
        JsonArray newChildren = new JsonArray();

        for (JsonElement child : children) {
            if (isAttributeObject(child)) {
                // Convert attr object to inline attribute
                convertAttributeToInline(parent, child.getAsJsonObject());
            } else {
                // Keep non-attribute children
                newChildren.add(child);
            }
        }

        if (newChildren.size() > 0) {
            parent.add("children", newChildren);
        }
    }
}
```

### Manual Conversion Steps

1. **Identify attribute children**: Look for `{"attr": {...}}` objects
2. **Extract attribute properties**: Get name, type, and value
3. **Convert to inline syntax**: Use `@name: value` format
4. **Remove attr objects**: Delete the verbose attribute definitions
5. **Validate result**: Ensure parsing works correctly

## Troubleshooting

### Common Issues

**Issue**: Parse error "Invalid inline attribute name"
```
Solution: Ensure attribute names follow identifier pattern: ^[a-zA-Z][a-zA-Z0-9_]*$
Example: Use "maxLength" not "max-length" or "max_length"
```

**Issue**: Type conversion error
```
Solution: Use appropriate JSON types for values:
- Booleans: true/false (not "true"/"false")
- Numbers: 255 (not "255")
- Arrays: ["a", "b"] (not "a,b")
```

**Issue**: Attribute not recognized in XML
```
Solution: Ensure XML attribute doesn't conflict with standard metadata attributes
(name, type, subType, package). Use custom names like "dbColumn", "maxLength".
```

**Issue**: Missing attribute type registration
```
Solution: Ensure AttributeTypesMetaDataProvider is loaded before parsing.
Check that registry.getDefaultSubType("attr") returns a valid subtype.
```

## Next Steps

<div class="grid cards" markdown>

-   :material-tag:{ .lg .middle } **Attribute Framework**

    ---

    Understand the complete attribute type system

    [:octicons-arrow-right-24: Attribute Framework](attribute-framework.md)

-   :material-shield-check:{ .lg .middle } **Constraints**

    ---

    Learn how constraints validate inline attributes

    [:octicons-arrow-right-24: Constraint Architecture](../constraints/constraint-architecture.md)

-   :material-file-code:{ .lg .middle } **JSON Processing**

    ---

    Explore JSON metadata parsing and serialization

    [:octicons-arrow-right-24: JSON Processing](../io/json-processing.md)

-   :material-code-braces:{ .lg .middle } **Examples**

    ---

    See working examples with inline attributes

    [:octicons-arrow-right-24: Basic Usage](../../../examples/basic-usage.md)

</div>

---

Inline attributes provide a clean, efficient way to define metadata with rich attribute information while maintaining full type safety and validation capabilities. The 60% reduction in verbosity makes metadata more readable and maintainable without sacrificing any functionality.