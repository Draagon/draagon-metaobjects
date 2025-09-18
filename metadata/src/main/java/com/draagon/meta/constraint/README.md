# MetaObjects Constraint System v6.0.0

## Overview

The MetaObjects Constraint System provides a flexible, extensible way to validate metadata structure during construction. This system replaces the previous ValidatorChain approach with a service-based constraint architecture that supports:

- **Abstract constraint definitions** that can be reused across multiple metadata types
- **Reference-based constraint files** to avoid duplication and enable modular constraint libraries
- **Real-time constraint enforcement** during metadata construction (addChild/setAttribute)
- **Graceful degradation** for unknown constraint types in enterprise scenarios
- **Type and subtype specific targeting** for precise constraint application

## Key Components

### Core Classes

- **`ConstraintRegistry`**: Central registry for discovering and loading constraint definitions
- **`ConstraintDefinitionParser`**: Parses JSON constraint files with abstract definitions and specific instances
- **`ConstraintEnforcer`**: Enforces constraints during metadata construction operations
- **`Constraint`**: Interface for constraint implementations 
- **`ConstraintFactory`**: Interface for creating constraint instances from parameters

### Built-in Constraint Types

- **`required`**: Validates that fields are not null or empty
- **`pattern`**: Validates strings match regex patterns with optional flags
- **`length`**: Validates string length within min/max bounds
- **`range`**: Validates numeric values within min/max bounds (inclusive/exclusive)
- **`enum`**: Validates values are from predefined allowed sets (case sensitive/insensitive)

## Constraint Definition Format

Constraint files use JSON format with three main sections:

### References Section
```json
{
  "references": [
    "META-INF/constraints/core-constraints.json",
    "META-INF/constraints/other-constraints.json"
  ]
}
```

### Abstract Definitions Section
```json
{
  "abstracts": [
    {
      "id": "identifier-pattern",
      "type": "pattern", 
      "description": "Standard identifier pattern for metadata names",
      "parameters": {
        "pattern": "^[a-zA-Z][a-zA-Z0-9_]*$",
        "flags": "i"
      }
    }
  ]
}
```

### Constraint Instances Section
```json
{
  "constraints": [
    {
      "targetType": "object",
      "targetSubType": "*", 
      "targetName": "*",
      "abstractRef": "identifier-pattern"
    },
    {
      "targetType": "attr",
      "targetSubType": "string",
      "targetName": "defaultValue",
      "type": "length",
      "parameters": {
        "max": 255
      }
    }
  ]
}
```

## Target Matching Rules

Constraints are applied based on target matching:

- **targetType**: Must match exactly (e.g., "object", "field", "attr", "validator")
- **targetSubType**: `null` or `"*"` means any subtype, otherwise exact match required
- **targetName**: `null` or `"*"` means any name, otherwise exact match required

## Usage Examples

### Loading Constraints
```java
ConstraintRegistry registry = ConstraintRegistry.getInstance();
registry.loadConstraintsFromResource("META-INF/constraints/core-constraints.json");
```

### Enabling/Disabling Constraint Checking
```java
ConstraintEnforcer enforcer = ConstraintEnforcer.getInstance();

// Disable globally
enforcer.setConstraintCheckingEnabled(false);

// Disable for specific metadata type
enforcer.setConstraintCheckingEnabled("object", false);
```

### Custom Constraint Factory
```java
public class CustomConstraintFactory implements ConstraintFactory {
    @Override
    public Constraint createConstraint(Map<String, Object> parameters) {
        return new CustomConstraint(parameters);
    }
    
    @Override
    public String getConstraintType() {
        return "custom";
    }
}

// Register the factory
ConstraintRegistry.getInstance().registerConstraintFactory("custom", new CustomConstraintFactory());
```

## Integration with MetaData Construction

Constraints are automatically enforced during metadata construction:

```java
// This will trigger constraint checking
ValueMetaObject object = new ValueMetaObject("value", "User");  // Name pattern checked
StringField field = new StringField("name");                   // Name pattern checked  
object.addChild(field);                                         // Constraints enforced

StringAttribute attr = new StringAttribute("defaultValue");     // Name pattern checked
attr.setValueAsString("too long value...");                    // Length constraint checked
field.addChild(attr);                                          // Constraints enforced
```

## Constraint Files in Different Modules

### Core Module: `META-INF/constraints/core-constraints.json`
- Basic identifier patterns
- Standard length constraints  
- Common validation patterns

### Database Module: `META-INF/constraints/database-constraints.json`
- Database-safe identifier patterns
- SQL data type validation
- Database-specific length limits
- References core constraints to avoid duplication

### Web Module: `META-INF/constraints/web-constraints.json`
- HTML input type validation
- CSS class name patterns
- URL validation patterns
- References core constraints

## Graceful Degradation

The system handles unknown constraint types gracefully:

1. Unknown constraint types are logged as warnings
2. Unknown constraints are tracked in `getUnknownConstraintTypes()`  
3. System continues operation without failing
4. Constraints can be registered dynamically to handle unknown types

## Error Handling

Constraint violations throw `ConstraintViolationException` which includes:
- Constraint type that was violated
- Violating value
- Validation context with operation and metadata information
- Detailed error messages for debugging

These are wrapped in `InvalidMetaDataException` when thrown from `addChild()` operations.

## Performance Considerations

- Constraint definitions are cached after first load
- Constraint checking can be disabled globally or per-type
- Constraint factories use lazy initialization
- Unknown constraint types are tracked to avoid repeated warnings

## Migration from ValidatorChain

The new constraint system replaces ValidatorChain with these benefits:

1. **Clear Terminology**: "Constraints" for metadata structure, "Validators" for data values
2. **Modular Design**: Abstract definitions enable constraint reuse across libraries
3. **Real-time Validation**: Constraints checked during construction, not after loading
4. **Enterprise Ready**: Graceful degradation for unknown constraints
5. **Cross-language Support**: JSON configuration works across Java/C#/TypeScript

## Example: Database Attribute Constraints

```java
// Load database constraints (which reference core constraints)
registry.loadConstraintsFromResource("META-INF/constraints/database-constraints.json");

// This will be validated against database-safe identifier pattern and length constraints
StringAttribute dbTable = new StringAttribute("dbTable");
dbTable.setValueAsString("users");  // Valid database table name

// This will be validated against SQL data type enum
StringAttribute dbType = new StringAttribute("dbType"); 
dbType.setValueAsString("VARCHAR");  // Valid SQL data type

// This will fail validation - not a valid SQL data type
StringAttribute invalidType = new StringAttribute("dbType");
invalidType.setValueAsString("INVALID_TYPE");  // Throws ConstraintViolationException
```

This approach enables metadata libraries to define their own constraints while reusing common constraint logic from core libraries.