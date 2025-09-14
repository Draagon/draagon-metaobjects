# MetaObjects Framework: API Usage Patterns and Examples

## Table of Contents
1. [Core API Patterns](#core-api-patterns)
2. [MetaData Access Patterns](#metadata-access-patterns)
3. [MetaObject Field Management](#metaobject-field-management)
4. [MetaField Views and Validators](#metafield-views-and-validators)
5. [Type-Safe Utilities](#type-safe-utilities)
6. [Best Practices](#best-practices)
7. [Migration Guide](#migration-guide)

---

## Core API Patterns

### The find/require/stream Pattern

The MetaObjects framework follows a consistent API pattern for accessing child objects:

- **`find*()`** - Returns `Optional<T>` for safe access
- **`require*()`** - Returns `T` directly or throws descriptive exception
- **`get*Stream()`** - Returns `Stream<T>` for functional operations
- **`has*()`** - Returns `boolean` for existence checks

```java
// Safe optional access
Optional<MetaField> field = metaObject.findMetaField("name");
if (field.isPresent()) {
    // Use the field
    System.out.println("Field type: " + field.get().getDataType());
}

// Require pattern - fails fast if missing
try {
    MetaField requiredField = metaObject.requireMetaField("id");
    // Field is guaranteed to exist here
} catch (MetaFieldNotFoundException e) {
    // Handle missing field
}

// Stream operations for functional programming
List<MetaField> stringFields = metaObject.getMetaFieldsStream()
    .filter(f -> f.getDataType() == DataTypes.STRING)
    .collect(Collectors.toList());

// Existence check
if (metaObject.hasMetaField("email")) {
    // Field exists, safe to access
}
```

---

## MetaData Access Patterns

### Working with MetaData Hierarchies

```java
// Find child by name and type
Optional<MetaObject> childObject = parentMetaData.findChild("User", MetaObject.class);

// Get all children of a specific type
Stream<MetaObject> allObjects = parentMetaData.findChildren(MetaObject.class);

// Collect children with criteria
List<MetaField> requiredFields = metaData.getChildrenStream()
    .filter(child -> child instanceof MetaField)
    .map(child -> (MetaField) child)
    .filter(field -> field.hasAttribute("required"))
    .collect(Collectors.toList());
```

### Attribute Management

```java
// Safe attribute access
Optional<MetaAttribute> displayAttr = field.findAttribute("display");
displayAttr.ifPresent(attr -> {
    System.out.println("Display name: " + attr.getValueAsString());
});

// Required attribute access
try {
    MetaAttribute requiredAttr = field.requireAttribute("validation");
    // Use the attribute
} catch (MetaAttributeNotFoundException e) {
    // Handle missing attribute
}

// Get all attributes as stream
field.getAttributesStream()
    .filter(attr -> attr.getName().startsWith("ui_"))
    .forEach(attr -> processUIAttribute(attr));
```

---

## MetaObject Field Management

### Field Discovery and Access

```java
public class UserObjectExample {
    
    public void demonstrateFieldAccess(MetaObject userObject) {
        // Modern API - Safe access
        Optional<MetaField> nameField = userObject.findMetaField("name");
        nameField.ifPresent(field -> {
            System.out.println("Name field type: " + field.getDataType());
        });
        
        // Modern API - Required access
        MetaField idField = userObject.requireMetaField("id");
        
        // Find fields by criteria
        List<MetaField> requiredFields = userObject.getMetaFieldsStream()
            .filter(this::isRequired)
            .collect(Collectors.toList());
            
        // Find fields by data type
        List<MetaField> stringFields = userObject.findFieldsByType(DataTypes.STRING)
            .collect(Collectors.toList());
    }
    
    private boolean isRequired(MetaField field) {
        return field.findValidator("required").isPresent();
    }
}
```

### Field Validation Patterns

```java
public class FieldValidationExample {
    
    public void validateFieldConfiguration(MetaField field) {
        // Check if field has validators
        if (field.hasValidator("required")) {
            System.out.println("Field is required");
        }
        
        // Get all validators for analysis
        List<MetaValidator> validators = field.getValidatorsStream()
            .collect(Collectors.toList());
            
        // Find specific validator types
        Optional<MetaValidator> lengthValidator = field.findValidator("length");
        lengthValidator.ifPresent(validator -> {
            // Configure length validation
        });
        
        // Require critical validators
        try {
            MetaValidator requiredValidator = field.requireValidator("required");
            // Validator is guaranteed to exist
        } catch (MetaValidatorNotFoundException e) {
            // Handle missing critical validator
        }
    }
}
```

---

## MetaField Views and Validators

### View Management

```java
public class ViewManagementExample {
    
    public void configureFieldViews(MetaField field) {
        // Safe view access
        Optional<MetaView> htmlView = field.findView("html");
        htmlView.ifPresent(view -> {
            // Configure HTML view
            System.out.println("HTML template: " + view.getTemplate());
        });
        
        // Get all views for processing
        field.getViewsStream()
            .filter(view -> view.isType("mobile"))
            .forEach(view -> configureMobileView(view));
            
        // Require specific view
        try {
            MetaView defaultView = field.requireView("default");
            // View is guaranteed to exist
        } catch (MetaViewNotFoundException e) {
            // Create default view if missing
            createDefaultView(field);
        }
    }
    
    private void configureMobileView(MetaView view) {
        // Mobile-specific view configuration
    }
    
    private void createDefaultView(MetaField field) {
        // Create and add default view
    }
}
```

### Validator Configuration

```java
public class ValidatorExample {
    
    public void setupFieldValidation(MetaField field) {
        // Add required validator if missing
        if (!field.hasValidator("required")) {
            field.addMetaValidator(new RequiredValidator("required"));
        }
        
        // Configure length validation for string fields
        if (field.getDataType() == DataTypes.STRING) {
            Optional<MetaValidator> lengthValidator = field.findValidator("length");
            if (lengthValidator.isEmpty()) {
                field.addMetaValidator(new LengthValidator("length"));
            }
        }
        
        // Process all validators
        field.getValidatorsStream()
            .forEach(validator -> {
                System.out.println("Validator: " + validator.getName());
                // Configure validator
            });
    }
}
```

---

## Type-Safe Utilities

### Using MetaDataCasting

```java
import com.draagon.meta.util.MetaDataCasting;

public class CastingExample {
    
    public void safeCastingExample(MetaData metadata) {
        // Safe casting with Optional
        Optional<MetaField> field = MetaDataCasting.safeCast(metadata, MetaField.class);
        field.ifPresent(f -> {
            System.out.println("Field name: " + f.getName());
        });
        
        // Required casting (throws if fails)
        try {
            MetaObject object = MetaDataCasting.requireCast(metadata, MetaObject.class);
            // Object is guaranteed to be MetaObject
        } catch (MetaDataException e) {
            System.err.println("Cast failed: " + e.getMessage());
        }
        
        // Filter stream by type
        List<MetaField> fields = MetaDataCasting.filterByType(
            parentMetaData.getChildrenStream(), 
            MetaField.class
        ).collect(Collectors.toList());
    }
}
```

### Using TypedMetaDataAccess

```java
import com.draagon.meta.util.TypedMetaDataAccess;

public class TypedAccessExample {
    
    public void typedAccessExample(MetaObject userObject) {
        // Type-safe field access
        Optional<MetaField> nameField = TypedMetaDataAccess.findField(userObject, "name");
        
        // Required field access
        MetaField idField = TypedMetaDataAccess.requireField(userObject, "id");
        
        // Get all fields with proper typing
        List<MetaField> allFields = TypedMetaDataAccess.getFields(userObject);
        
        // Attribute access
        nameField.ifPresent(field -> {
            Optional<MetaAttribute> displayAttr = TypedMetaDataAccess.findAttribute(field, "display");
            displayAttr.ifPresent(attr -> {
                System.out.println("Display: " + attr.getValueAsString());
            });
        });
    }
}
```

---

## Best Practices

### 1. Prefer Modern APIs

```java
// ✅ GOOD - Use modern Optional-based APIs
Optional<MetaField> field = metaObject.findMetaField("name");
field.ifPresent(f -> processField(f));

// ❌ AVOID - Legacy exception-based APIs (where available)
try {
    MetaField field = metaObject.getMetaField("name");
    processField(field);
} catch (MetaFieldNotFoundException e) {
    // Handle missing field
}
```

### 2. Use Streams for Collection Operations

```java
// ✅ GOOD - Functional programming with streams
List<String> fieldNames = metaObject.getMetaFieldsStream()
    .filter(field -> field.isRequired())
    .map(MetaField::getName)
    .sorted()
    .collect(Collectors.toList());

// ❌ AVOID - Imperative iteration
List<String> fieldNames = new ArrayList<>();
for (MetaField field : metaObject.getMetaFields()) {
    if (field.isRequired()) {
        fieldNames.add(field.getName());
    }
}
Collections.sort(fieldNames);
```

### 3. Handle Missing Elements Gracefully

```java
// ✅ GOOD - Graceful handling with defaults
String displayName = field.findAttribute("display")
    .map(MetaAttribute::getValueAsString)
    .orElse(field.getName()); // Fallback to field name

// ✅ GOOD - Fail fast when element is critical
MetaValidator requiredValidator = field.requireValidator("required");
```

### 4. Use Type-Safe Utilities

```java
// ✅ GOOD - Use utility classes for common patterns
Optional<MetaField> field = MetaDataCasting.safeCast(child, MetaField.class);
List<MetaField> fields = TypedMetaDataAccess.getFields(metaObject);

// ❌ AVOID - Manual casting and type checking
if (child instanceof MetaField) {
    MetaField field = (MetaField) child;
    // Process field
}
```

---

## Migration Guide

### From Legacy APIs to Modern APIs

#### Field Access Migration

```java
// OLD - Exception-based access
try {
    MetaField field = metaObject.getMetaField("name");
    processField(field);
} catch (MetaFieldNotFoundException e) {
    handleMissingField();
}

// NEW - Optional-based access
Optional<MetaField> field = metaObject.findMetaField("name");
field.ifPresentOrElse(
    this::processField,
    this::handleMissingField
);

// NEW - Required access when you need to fail fast
try {
    MetaField field = metaObject.requireMetaField("name");
    processField(field);
} catch (MetaFieldNotFoundException e) {
    handleMissingField();
}
```

#### View Access Migration

```java
// OLD - hasView() with exception handling
if (field.hasView("html")) {
    try {
        MetaView view = field.getView("html");
        configureView(view);
    } catch (MetaViewNotFoundException e) {
        // This should not happen since hasView() returned true
    }
}

// NEW - Modern Optional-based access
field.findView("html").ifPresent(this::configureView);

// NEW - Required access for critical views
try {
    MetaView view = field.requireView("html");
    configureView(view);
} catch (MetaViewNotFoundException e) {
    createDefaultView(field);
}
```

#### Validator Access Migration

```java
// OLD - Manual iteration and casting
for (MetaData child : field.getChildren()) {
    if (child instanceof MetaValidator) {
        MetaValidator validator = (MetaValidator) child;
        if ("required".equals(validator.getName())) {
            configureValidator(validator);
        }
    }
}

// NEW - Stream-based access
field.getValidatorsStream()
    .filter(v -> "required".equals(v.getName()))
    .findFirst()
    .ifPresent(this::configureValidator);

// NEW - Direct access
field.findValidator("required").ifPresent(this::configureValidator);
```

---

## Performance Considerations

### Efficient Operations

```java
// ✅ EFFICIENT - O(1) lookup using modern APIs
Optional<MetaField> field = metaObject.findMetaField("name");

// ✅ EFFICIENT - Stream operations with early termination
boolean hasRequiredFields = metaObject.getMetaFieldsStream()
    .anyMatch(field -> field.hasValidator("required"));

// ✅ EFFICIENT - Batch operations
Map<String, MetaField> fieldMap = metaObject.getMetaFieldsStream()
    .collect(Collectors.toMap(MetaField::getName, Function.identity()));
```

### Avoiding Common Performance Pitfalls

```java
// ❌ INEFFICIENT - Repeated lookups in loops
for (String fieldName : fieldNames) {
    if (metaObject.hasMetaField(fieldName)) {
        MetaField field = metaObject.requireMetaField(fieldName); // Second lookup!
        processField(field);
    }
}

// ✅ EFFICIENT - Single lookup per field
for (String fieldName : fieldNames) {
    metaObject.findMetaField(fieldName).ifPresent(this::processField);
}
```

---

## Conclusion

The enhanced MetaObjects API provides:

- **Type Safety**: Compile-time validation and runtime safety
- **Consistency**: Uniform patterns across all access methods
- **Performance**: Efficient O(1) lookups and stream operations
- **Flexibility**: Optional-based access with functional programming support
- **Maintainability**: Clear, self-documenting code patterns

By following these patterns and examples, you can write more robust, maintainable, and efficient code with the MetaObjects framework.