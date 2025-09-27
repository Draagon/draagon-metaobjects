# Enhanced PlacementConstraint with Constant-Based API

## Overview

The PlacementConstraint class has been enhanced with type-safe constructors and static factory methods that use existing constants instead of error-prone string literals.

## Key Improvements

### ✅ BEFORE vs AFTER Comparison

**BEFORE (Error-prone string literals):**
```java
// ❌ Old way - string concatenation with potential typos
registry.addConstraint(new PlacementConstraint(
    "stringfield.maxlength.placement",
    "StringField can have maxLength attribute",
    "field.string",           // Could have typos
    "attr.int[maxLength]",    // Could have typos
    true
));
```

**AFTER (Type-safe with constants):**
```java
// ✅ New way - compile-time checked constants
registry.addConstraint(PlacementConstraint.allowAttribute(
    "stringfield.maxlength.placement",
    "StringField can have maxLength attribute",
    TYPE_FIELD, SUBTYPE_STRING,           // Parent: field.string
    IntAttribute.SUBTYPE_INT, ATTR_MAX_LENGTH    // Child: attr.int[maxLength]
));
```

## New Constructor

```java
public PlacementConstraint(String constraintId, String description,
                          String parentType, String parentSubType,
                          String childType, String childSubType, String childName,
                          boolean allowed)
```

**Parameters:**
- `parentType` - Use constants like `MetaField.TYPE_FIELD`
- `parentSubType` - Use constants like `StringField.SUBTYPE_STRING`
- `childType` - Use constants like `MetaAttribute.TYPE_ATTR`
- `childSubType` - Use constants like `StringAttribute.SUBTYPE_STRING`
- `childName` - Use constants like `StringField.ATTR_MAX_LENGTH`

## Static Factory Methods

### 1. `allowAttribute()` - Allow specific attribute on specific parent

```java
PlacementConstraint constraint = PlacementConstraint.allowAttribute(
    "stringfield.maxlength",
    "StringField can have maxLength attribute",
    MetaField.TYPE_FIELD, StringField.SUBTYPE_STRING,    // Parent: field.string
    IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH // Child: attr.int[maxLength]
);
```

### 2. `allowAttributeOnAnyField()` - Allow attribute on any field type

```java
PlacementConstraint constraint = PlacementConstraint.allowAttributeOnAnyField(
    "field.required",
    "Any field can have required attribute",
    BooleanAttribute.SUBTYPE_BOOLEAN, MetaField.ATTR_REQUIRED
);
// Produces: parent="field.*", child="attr.boolean[required]"
```

### 3. `allowAttributeOnAnyObject()` - Allow attribute on any object type

```java
PlacementConstraint constraint = PlacementConstraint.allowAttributeOnAnyObject(
    "object.dbTable",
    "Any object can have dbTable attribute",
    StringAttribute.SUBTYPE_STRING, "dbTable"
);
// Produces: parent="object.*", child="attr.string[dbTable]"
```

### 4. `allowChildType()` - Allow child type under parent (no name constraint)

```java
PlacementConstraint constraint = PlacementConstraint.allowChildType(
    "metadata.fields",
    "Metadata can contain fields",
    "metadata", "base",               // Parent: metadata.base
    MetaField.TYPE_FIELD, "*"         // Child: field.*
);
```

### 5. `forbidAttribute()` - Forbid specific attribute

```java
PlacementConstraint constraint = PlacementConstraint.forbidAttribute(
    "object.maxlength.forbidden",
    "Objects cannot have maxLength attribute",
    MetaObject.TYPE_OBJECT, "*",
    IntAttribute.SUBTYPE_INT, StringField.ATTR_MAX_LENGTH
);
```

## Constants Reference

### Type Constants
```java
MetaField.TYPE_FIELD        // "field"
MetaObject.TYPE_OBJECT      // "object"
MetaAttribute.TYPE_ATTR     // "attr"
```

### Field Subtype Constants
```java
StringField.SUBTYPE_STRING         // "string"
IntegerField.SUBTYPE_INT           // "int"
LongField.SUBTYPE_LONG             // "long"
DoubleField.SUBTYPE_DOUBLE         // "double"
BooleanField.SUBTYPE_BOOLEAN       // "boolean"
```

### Attribute Subtype Constants
```java
StringAttribute.SUBTYPE_STRING     // "string"
IntAttribute.SUBTYPE_INT           // "int"
LongAttribute.SUBTYPE_LONG         // "long"
DoubleAttribute.SUBTYPE_DOUBLE     // "double"
BooleanAttribute.SUBTYPE_BOOLEAN   // "boolean"
```

### Attribute Name Constants
```java
// MetaField level attributes
MetaField.ATTR_REQUIRED            // "required"
MetaField.ATTR_DEFAULT_VALUE       // "defaultValue"
MetaField.ATTR_DEFAULT_VIEW        // "defaultView"

// StringField specific attributes
StringField.ATTR_PATTERN           // "pattern"
StringField.ATTR_MAX_LENGTH        // "maxLength"
StringField.ATTR_MIN_LENGTH        // "minLength"
```

## Usage Patterns

### Pattern 1: Field-Specific Attributes
```java
// StringField can have pattern attribute
PlacementConstraint.allowAttribute(
    "stringfield.pattern",
    "StringField can have pattern attribute",
    TYPE_FIELD, SUBTYPE_STRING,
    StringAttribute.SUBTYPE_STRING, ATTR_PATTERN
);

// StringField can have maxLength attribute
PlacementConstraint.allowAttribute(
    "stringfield.maxlength",
    "StringField can have maxLength attribute",
    TYPE_FIELD, SUBTYPE_STRING,
    IntAttribute.SUBTYPE_INT, ATTR_MAX_LENGTH
);
```

### Pattern 2: Cross-Cutting Attributes
```java
// Any field can have required attribute
PlacementConstraint.allowAttributeOnAnyField(
    "field.required",
    "Fields can have required attribute",
    BooleanAttribute.SUBTYPE_BOOLEAN, ATTR_REQUIRED
);
```

### Pattern 3: Type Containment Rules
```java
// Objects can contain fields
PlacementConstraint.allowChildType(
    "object.fields",
    "Objects can contain fields",
    TYPE_OBJECT, "*",
    TYPE_FIELD, "*"
);
```

## Unified with ChildRequirement

PlacementConstraint and ChildRequirement can now use the same constants:

```java
// PlacementConstraint (for validation)
PlacementConstraint placement = PlacementConstraint.allowAttribute(
    "stringfield.maxlength",
    "StringField can have maxLength",
    TYPE_FIELD, SUBTYPE_STRING,
    IntAttribute.SUBTYPE_INT, ATTR_MAX_LENGTH
);

// ChildRequirement (for schema generation)
ChildRequirement child = ChildRequirement.optional(
    ATTR_MAX_LENGTH,                // Same constant!
    TYPE_ATTR,                      // Same constant!
    IntAttribute.SUBTYPE_INT        // Same constant!
);
```

## Benefits

1. **Type Safety**: Compile-time checking prevents typos
2. **Constant Reuse**: Uses existing constants from MetaField, StringField, etc.
3. **Clean API**: Static factory methods for common patterns
4. **Unified Approach**: Works seamlessly with ChildRequirement
5. **Backward Compatible**: Legacy string-based constructor still works
6. **Easy Migration**: Can migrate usage incrementally

## Migration Strategy

1. **Immediate**: Use new factory methods for new constraints
2. **Gradual**: Update existing high-frequency usage sites
3. **Eventually**: Deprecate string-based constructor
4. **Future**: Consider merging ChildRequirement functionality