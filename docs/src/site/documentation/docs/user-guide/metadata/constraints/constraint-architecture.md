# Constraint System Architecture

The MetaObjects constraint system provides comprehensive validation and structural integrity for metadata definitions through a sophisticated two-tier constraint architecture. This system enforces rules during metadata construction, ensuring data quality and structural consistency in real-time.

## Core Design Principles

The constraint system is built on several key architectural principles that align with MetaObjects' READ-OPTIMIZED WITH CONTROLLED MUTABILITY pattern:

### :material-shield-check: **Real-Time Enforcement**

Constraints are enforced **during metadata construction**, not as an afterthought validation step:

```java
// Constraints checked during addChild() operations
MetaObject user = new MetaObject("User");
MetaField invalidField = new MetaField("invalid::name");  // Contains ::
user.addChild(invalidField);  // ❌ ConstraintViolationException thrown here
```

### :material-lightning-bolt: **Performance Optimized**

The constraint system follows MetaObjects' performance characteristics:

- **Loading Phase**: Full constraint validation (100ms-1s one-time cost)
- **Runtime Phase**: No constraint overhead (metadata is immutable)
- **Memory Efficient**: Constraints loaded once, cached permanently

### :material-cog: **Extensible by Design**

Constraints use a provider-based registration system that allows for clean extensibility:

```java
// Custom constraints integrated seamlessly
public class BusinessRuleConstraint extends BaseConstraint {
    // Custom business logic validation
}
```

## Two-Tier Constraint Architecture

MetaObjects uses a sophisticated two-tier system that separates **structural** constraints from **value** constraints:

```mermaid
graph TB
    subgraph "Constraint System"
        A[Placement Constraints] --> C[ConstraintEnforcer]
        B[Validation Constraints] --> C
    end

    subgraph "Placement Constraints"
        D["'X CAN be placed under Y'"]
        E[Parent-Child Relationships]
        F[Structural Rules]
    end

    subgraph "Validation Constraints"
        G["'X must have valid Y'"]
        H[Value Validation]
        I[Business Rules]
    end

    C --> J[MetaData.addChild()]
    C --> K[Real-Time Enforcement]

    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style C fill:#e8f5e8
```

### Tier 1: Placement Constraints

**Purpose**: Define **"What can be placed where"** in the metadata hierarchy.

**Pattern**: `PlacementConstraint` determines structural relationships between parent and child metadata.

```java
// Example: "String fields CAN have maxLength attributes"
PlacementConstraint maxLengthPlacement = PlacementConstraint.allowAttribute(
    "field.string.maxLength",
    "String fields can have maxLength attribute",
    MetaField.TYPE_FIELD,     // Parent type: field
    StringField.SUBTYPE_STRING,  // Parent subtype: string
    IntAttribute.SUBTYPE_INT,    // Attribute type: int
    StringField.ATTR_MAX_LENGTH  // Attribute name: maxLength
);
```

### Tier 2: Validation Constraints

**Purpose**: Define **"What values are valid"** for metadata properties.

**Pattern**: `BaseConstraint` subclasses validate actual values against business rules.

```java
// Example: "Field names must follow identifier pattern"
RegexConstraint fieldNaming = new RegexConstraint(
    "field.naming.pattern",
    "Field names must follow identifier pattern",
    MetaField.TYPE_FIELD,  // Target: field
    "*",                   // Any subtype
    "*",                   // Any name
    "^[a-zA-Z][a-zA-Z0-9_]*$"  // Regex pattern
);
```

## Constraint Types

### PlacementConstraint: Structural Rules

`PlacementConstraint` uses pattern matching to define where metadata can be placed in the hierarchy:

#### Pattern Syntax

```java
// Pattern components
"type.subtype[name]"

// Examples:
"field.string"           // Any string field
"field.*"               // Any field type
"attr.int[maxLength]"   // Specific maxLength int attribute
"object.pojo"           // Specific pojo object
"*"                     // Matches anything
```

#### Factory Methods for Common Patterns

```java
// Allow attribute on specific field type
PlacementConstraint.allowAttribute(
    "field.string.pattern",
    "String fields can have pattern attribute",
    MetaField.TYPE_FIELD,
    StringField.SUBTYPE_STRING,
    StringAttribute.SUBTYPE_STRING,
    StringField.ATTR_PATTERN
);

// Allow attribute on any field type
PlacementConstraint.allowAttributeOnAnyField(
    "field.any.required",
    "Any field can be required",
    BooleanAttribute.SUBTYPE_BOOLEAN,
    MetaField.ATTR_REQUIRED
);

// Allow child type under parent
PlacementConstraint.allowChildType(
    "object.contains.fields",
    "Objects can contain fields",
    MetaObject.TYPE_OBJECT, "*",
    MetaField.TYPE_FIELD, "*"
);
```

#### Enforcement Logic

PlacementConstraints use an **open policy** - if any constraint allows the placement, it's permitted:

```java
// Open Policy Example
List<PlacementConstraint> applicable = findApplicableConstraints(parent, child);

boolean allowed = false;
for (PlacementConstraint constraint : applicable) {
    if (constraint.isAllowed()) {
        allowed = true;  // Any allowing constraint permits the placement
        break;
    }
}
```

### ValidationConstraint: Value Rules

Validation constraints inherit from `BaseConstraint` and validate actual values:

#### Built-in Validation Types

**RequiredConstraint:**
```java
RequiredConstraint fieldNameRequired = new RequiredConstraint(
    "field.name.required",
    "Field names are required",
    MetaField.TYPE_FIELD, "*", "name"
);

// Validates that field.name is not null or empty
```

**RegexConstraint:**
```java
RegexConstraint identifierPattern = new RegexConstraint(
    "identifier.pattern",
    "Names must be valid identifiers",
    "*", "*", "name",
    "^[a-zA-Z][a-zA-Z0-9_]*$"
);

// Validates values against regex pattern
```

**LengthConstraint:**
```java
LengthConstraint nameLength = new LengthConstraint(
    "name.length",
    "Names must be 1-64 characters",
    "*", "*", "name",
    1, 64  // min, max length
);
```

**EnumConstraint:**
```java
EnumConstraint validTypes = new EnumConstraint(
    "field.type.values",
    "Field types must be from allowed set",
    MetaField.TYPE_FIELD, "*", "type",
    Arrays.asList("string", "int", "long", "double", "boolean", "date")
);
```

#### Target Pattern Matching

Validation constraints use declarative targeting instead of functional predicates:

```java
public abstract class BaseConstraint implements Constraint {
    protected final String targetType;     // "field", "object", "attr", "*"
    protected final String targetSubType;  // "string", "int", "*"
    protected final String targetName;     // "maxLength", "required", "*"

    public boolean appliesTo(MetaData metaData) {
        return matchesPattern(metaData.getType(), targetType) &&
               matchesPattern(metaData.getSubType(), targetSubType) &&
               matchesPattern(metaData.getName(), targetName);
    }
}
```

## Unified Enforcement Architecture

The `ConstraintEnforcer` provides a unified enforcement mechanism that processes both constraint types in a single pass:

### Single Enforcement Loop

```java
public void enforceConstraintsOnAddChild(MetaData parent, MetaData child) {
    // UNIFIED: Single loop through all constraints
    List<Constraint> allConstraints = metaDataRegistry.getAllValidationConstraints();

    // Process placement constraints first
    for (Constraint constraint : allConstraints) {
        if (constraint instanceof PlacementConstraint) {
            PlacementConstraint pc = (PlacementConstraint) constraint;
            if (pc.appliesTo(parent, child)) {
                // Apply placement logic
            }
        }
    }

    // Process validation constraints
    for (Constraint constraint : allConstraints) {
        if (constraint instanceof BaseConstraint) {
            BaseConstraint vc = (BaseConstraint) constraint;
            if (vc.appliesTo(child)) {
                vc.validate(child, child.getName());
            }
        }
    }
}
```

### Performance Benefits

The unified approach provides significant performance improvements:

- **3x fewer constraint checking calls** per operation
- **No duplicate constraint processing**
- **Single cache lookup** instead of multiple storage checks
- **Elimination of empty list iterations**

## Constraint Registration

Constraints are registered through the integrated MetaDataRegistry system:

### Programmatic Registration

```java
public class CoreConstraintsProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register placement constraints
        registry.addValidationConstraint(
            PlacementConstraint.allowAttributeOnAnyField(
                "field.required",
                "Any field can be required",
                BooleanAttribute.SUBTYPE_BOOLEAN,
                MetaField.ATTR_REQUIRED
            )
        );

        // Register validation constraints
        registry.addValidationConstraint(
            new RegexConstraint(
                "field.naming.pattern",
                "Field names must be identifiers",
                MetaField.TYPE_FIELD, "*", "name",
                "^[a-zA-Z][a-zA-Z0-9_]*$"
            )
        );
    }

    @Override
    public int getPriority() {
        return 0; // Core constraints loaded first
    }
}
```

### Service Discovery Integration

```
META-INF/services/com.metaobjects.registry.MetaDataTypeProvider:
com.metaobjects.core.CoreConstraintsProvider
com.metaobjects.database.DatabaseConstraintsProvider
```

## Error Handling and Context

The constraint system provides rich error context for debugging and user feedback:

### ConstraintViolationException

```java
public class ConstraintViolationException extends MetaDataException {
    private final String constraintId;
    private final MetaData violatingMetaData;
    private final Optional<String> suggestedFix;

    public ConstraintViolationException(String message, String constraintId,
                                      MetaData metaData) {
        super(message, Optional.of(MetaDataPath.from(metaData)));
        this.constraintId = constraintId;
        this.violatingMetaData = metaData;
    }
}
```

### Rich Error Messages

```java
// Example error message
"Field name 'invalid::name' violates constraint 'field.naming.pattern':
 Field names must follow identifier pattern '^[a-zA-Z][a-zA-Z0-9_]*$'.
 Consider using 'invalidName' instead."
```

### Debugging Support

```java
// Constraint checking can be disabled for testing
ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();
enforcer.disableConstraintChecking("test-context");

// Detailed logging available
log.debug("Enforcing {} constraints for adding [{}] to [{}]",
    allConstraints.size(), child.toString(), parent.toString());
```

## Schema Generation Integration

The constraint system is designed to integrate seamlessly with schema generators:

### JSON Schema Integration

```java
// Placement constraints become JSON Schema patterns
{
  "properties": {
    "fields": {
      "type": "array",
      "items": {
        "properties": {
          "maxLength": {
            "type": "integer",
            "minimum": 1
          }
        }
      }
    }
  }
}
```

### XSD Schema Integration

```xml
<!-- Validation constraints become XSD restrictions -->
<xs:simpleType name="identifierType">
  <xs:restriction base="xs:string">
    <xs:pattern value="^[a-zA-Z][a-zA-Z0-9_]*$"/>
    <xs:minLength value="1"/>
    <xs:maxLength value="64"/>
  </xs:restriction>
</xs:simpleType>
```

## Performance Characteristics

The constraint system maintains MetaObjects' performance expectations:

### Constraint Loading

- **One-time cost**: Constraints loaded during registry initialization
- **Memory resident**: Constraints cached permanently like metadata
- **Service discovery**: Provider-based loading with priority ordering

### Constraint Enforcement

- **Loading phase only**: Constraints only enforced during metadata construction
- **Zero runtime cost**: No constraint overhead during normal metadata access
- **Thread-safe**: No synchronization needed for constraint checking

### Memory Usage

- **Minimal overhead**: ~1-5MB for typical constraint sets
- **Efficient patterns**: String patterns vs functional predicates
- **Schema friendly**: Constraints serializable to XSD/JSON Schema

## Extensibility Guidelines

### DO: Use Declarative Patterns

```java
// ✅ GOOD - Declarative pattern matching
new RegexConstraint("email.format", "Must be valid email",
    "field", "string", "email", "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
```

### DON'T: Use Functional Predicates

```java
// ❌ WRONG - Functional predicates not serializable to schemas
new CustomConstraint("email.format", "Must be valid email",
    metadata -> metadata instanceof StringField && "email".equals(metadata.getName()),
    value -> validateEmail(value));
```

### DO: Extend BaseConstraint

```java
// ✅ GOOD - Extend base constraint for validation rules
public class CreditCardConstraint extends BaseConstraint {
    public void validate(MetaData metaData, Object value) {
        // Custom validation logic
    }
}
```

### DO: Use Factory Methods

```java
// ✅ GOOD - Use placement constraint factory methods
PlacementConstraint.allowAttributeOnAnyField(
    "field.database.column",
    "Any field can have database column mapping",
    StringAttribute.SUBTYPE_STRING,
    "dbColumn"
);
```

## Next Steps

Now that you understand the constraint architecture, explore these areas:

<div class="grid cards" markdown>

-   :material-wrench:{ .lg .middle } **Custom Constraints**

    ---

    Learn to create your own constraint types

    [:octicons-arrow-right-24: Custom Constraints](custom-constraints.md)

-   :material-tag:{ .lg .middle } **Attribute System**

    ---

    Understand how attributes work with constraints

    [:octicons-arrow-right-24: Attribute Framework](../attributes/attribute-framework.md)

-   :material-sitemap:{ .lg .middle } **Type System**

    ---

    See how constraints integrate with type registration

    [:octicons-arrow-right-24: Type System](../type-system.md)

-   :material-code-braces:{ .lg .middle } **Examples**

    ---

    Working examples of constraint usage

    [:octicons-arrow-right-24: Constraint Examples](../../../examples/basic-usage.md)

</div>

---

The constraint system provides the foundation for MetaObjects' data integrity and structural validation, ensuring that metadata definitions are correct, consistent, and comply with business rules from the moment they are created.