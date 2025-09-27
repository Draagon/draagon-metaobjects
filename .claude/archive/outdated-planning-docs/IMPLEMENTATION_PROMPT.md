# Implementation Prompt: Unified MetaData Registry Architecture

## 📋 **Context: What You're Implementing**

You are implementing a major architectural unification for the MetaObjects framework that eliminates dual registry patterns and integrates child requirements into type definitions. This is a sophisticated metadata-driven development framework that follows a **READ-OPTIMIZED WITH CONTROLLED MUTABILITY** pattern similar to Java's Class/Field reflection system.

## 🎯 **Your Mission**

**Unify the dual MetaDataTypeRegistry implementations and eliminate constraint coupling issues by creating a single registry with integrated generic child requirements.**

## 🔍 **Current Problems You're Solving**

### **Problem 1: Dual MetaDataTypeRegistry Pattern**
```java
// TWO DIFFERENT REGISTRIES EXIST:
com.metaobjects.type.MetaDataTypeRegistry          // Singleton, uses registerType()
com.metaobjects.registry.MetaDataTypeRegistry      // Service-based, uses registerHandler()
```

### **Problem 2: Fragmented Constraint Management**
```java
// CURRENT PROBLEMATIC PATTERN:
static {
    // 1. Register type in one registry
    MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
    registry.registerHandler(typeId, StringField.class);
    
    // 2. Register constraints in separate registry
    ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
    constraintRegistry.addConstraint(placementConstraint);
}
```

### **Problem 3: Overly Broad Placement Rules**
```java
// CURRENT WRONG PATTERN - StringAttribute.java
PlacementConstraint attributePlacement = new PlacementConstraint(
    "stringattr.placement",
    "StringAttribute can be placed under any MetaData type", // TOO BROAD!
    (parent) -> parent instanceof MetaData, // WRONG - too permissive
    (child) -> child instanceof StringAttribute
);
```

## 🏗️ **Solution Architecture You're Implementing**

### **Key Design Principles**

1. **Constants From Source Classes** - No central constants class, each class owns its identity
2. **Parent-Defines-Children** - Parent types define what children they require/accept
3. **Generic Child Requirements** - Support any MetaData type as children (not just attributes)
4. **Enhanced Static Registration** - Single registration point with fluent API
5. **Service-Based Extensions** - Cross-cutting concerns via service providers

### **Target Architecture**

```java
// UNIFIED REGISTRY with integrated child requirements
public class MetaDataRegistry {
    // UNIFIED: Single registry with both singleton and service-based access
    private final Map<MetaDataTypeId, TypeDefinition> typeDefinitions;
    private final Map<String, List<ChildRequirement>> globalRequirements;
    
    // UNIFIED REGISTRATION API
    public static void registerType(Class<? extends MetaData> clazz, 
                                   Consumer<TypeDefinitionBuilder> configurator) { ... }
    
    public static class TypeDefinitionBuilder {
        public TypeDefinitionBuilder type(String type) { ... }
        public TypeDefinitionBuilder subType(String subType) { ... }
        
        // GENERIC CHILD REQUIREMENTS (any MetaData type)
        public TypeDefinitionBuilder requiredChild(String childType, String childSubType, String childName) { ... }
        public TypeDefinitionBuilder optionalChild(String childType, String childSubType, String childName) { ... }
        
        // CONVENIENCE METHOD for common attribute case
        public TypeDefinitionBuilder optionalAttribute(String attrName, String attrSubType) { ... }
    }
}
```

## 🔧 **Implementation Steps**

### **STEP 1: Create Enhanced MetaDataRegistry (Week 1)**

**File: `metadata/src/main/java/com/draagon/meta/registry/MetaDataRegistry.java`**

Replace the existing service-based registry with unified version:

```java
package com.metaobjects.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Unified registry for MetaData type definitions with integrated child requirements.
 * Replaces dual registry pattern with single registration point.
 */
public class MetaDataRegistry {
    
    private static volatile MetaDataRegistry instance;
    private static final Object INSTANCE_LOCK = new Object();
    
    private final ServiceRegistry serviceRegistry;
    private final Map<MetaDataTypeId, TypeDefinition> typeDefinitions = new ConcurrentHashMap<>();
    private final Map<String, List<ChildRequirement>> globalRequirements = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    
    // UNIFIED ACCESS PATTERNS
    public static MetaDataRegistry getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new MetaDataRegistry();
                }
            }
        }
        return instance;
    }
    
    public MetaDataRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    public MetaDataRegistry() {
        this(ServiceRegistryFactory.getDefault());
    }
    
    // UNIFIED REGISTRATION API
    public static void registerType(Class<? extends MetaData> clazz, 
                                   Consumer<TypeDefinitionBuilder> configurator) {
        TypeDefinitionBuilder builder = new TypeDefinitionBuilder(clazz);
        configurator.accept(builder);
        getInstance().register(builder.build());
    }
    
    // CHILD REQUIREMENT LOOKUP (for schema generation and validation)
    public List<ChildRequirement> getChildRequirements(String parentType, String parentSubType) { ... }
    public ChildRequirement getChildRequirement(String parentType, String parentSubType, String childName) { ... }
    
    // SERVICE-BASED EXTENSIONS
    public void addGlobalChildRequirement(String parentType, String parentSubType, ChildRequirement requirement) { ... }
}
```

**File: `metadata/src/main/java/com/draagon/meta/registry/TypeDefinitionBuilder.java`**

```java
public class TypeDefinitionBuilder {
    private final Class<? extends MetaData> implementationClass;
    private String type;
    private String subType;
    private String description;
    private final Map<String, ChildRequirement> childRequirements = new HashMap<>();
    
    public TypeDefinitionBuilder(Class<? extends MetaData> clazz) {
        this.implementationClass = clazz;
    }
    
    public TypeDefinitionBuilder type(String type) {
        this.type = type;
        return this;
    }
    
    public TypeDefinitionBuilder subType(String subType) {
        this.subType = subType;
        return this;
    }
    
    public TypeDefinitionBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    // GENERIC CHILD REQUIREMENTS (any MetaData type)
    public TypeDefinitionBuilder requiredChild(String childType, String childSubType, String childName) {
        childRequirements.put(childName, new ChildRequirement(childName, childType, childSubType, true));
        return this;
    }
    
    public TypeDefinitionBuilder optionalChild(String childType, String childSubType, String childName) {
        childRequirements.put(childName, new ChildRequirement(childName, childType, childSubType, false));
        return this;
    }
    
    // CONVENIENCE METHOD for common attribute case
    public TypeDefinitionBuilder optionalAttribute(String attrName, String attrSubType) {
        return optionalChild("attr", attrSubType, attrName);
    }
    
    // WILDCARD SUPPORT for flexible matching
    public TypeDefinitionBuilder optionalChild(String childType, String childSubType) {
        return optionalChild(childType, childSubType, "*"); // Any name
    }
    
    public TypeDefinition build() {
        return new TypeDefinition(implementationClass, type, subType, description, childRequirements);
    }
}
```

**File: `metadata/src/main/java/com/draagon/meta/registry/ChildRequirement.java`**

```java
public class ChildRequirement {
    private final String name;           // "pattern", "email", "*" for any
    private final String expectedType;   // "field", "attr", "validator", "*" for any
    private final String expectedSubType; // "string", "int", "*" for any  
    private final boolean required;
    
    public ChildRequirement(String name, String expectedType, String expectedSubType, boolean required) {
        this.name = name;
        this.expectedType = expectedType;
        this.expectedSubType = expectedSubType;
        this.required = required;
    }
    
    // WILDCARD MATCHING SUPPORT
    public boolean matches(String childType, String childSubType, String childName) {
        return matchesPattern(this.expectedType, childType) &&
               matchesPattern(this.expectedSubType, childSubType) &&
               matchesPattern(this.name, childName);
    }
    
    private boolean matchesPattern(String pattern, String value) {
        return "*".equals(pattern) || pattern.equals(value);
    }
    
    // Getters...
}
```

### **STEP 2: Update Core MetaData Classes (Week 2)**

**File: `metadata/src/main/java/com/draagon/meta/field/MetaField.java`**

```java
public abstract class MetaField extends MetaData {
    // FIELD CONSTANTS (owned by this class)
    public static final String TYPE_FIELD = "field";
    public static final String ATTR_REQUIRED = "required";
    public static final String ATTR_DEFAULT_VALUE = "defaultValue";
    
    static {
        MetaDataRegistry.registerType(MetaField.class, def -> def
            .type(TYPE_FIELD).subType("base")
            .description("Base field metadata")
            
            // COMMON FIELD ATTRIBUTES (all field types inherit these)
            .optionalAttribute(ATTR_REQUIRED, "boolean")
            .optionalAttribute(ATTR_DEFAULT_VALUE, "string")
        );
    }
}
```

**File: `metadata/src/main/java/com/draagon/meta/field/StringField.java`**

```java
public class StringField extends PrimitiveField<String> {
    public static final String SUBTYPE_STRING = "string";
    public static final String ATTR_PATTERN = "pattern";
    
    static {
        MetaDataRegistry.registerType(StringField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_STRING)
            .description("String field with pattern validation")
            
            // STRING-SPECIFIC ATTRIBUTES
            .optionalAttribute(ATTR_PATTERN, "string")
            // Inherits: required, defaultValue from MetaField
        );
    }
    
    // Remove existing static registration block completely
}
```

**File: `metadata/src/main/java/com/draagon/meta/field/IntegerField.java`**

```java
public class IntegerField extends PrimitiveField<Integer> {
    public static final String SUBTYPE_INT = "int";
    public static final String ATTR_MIN_VALUE = "minValue";
    public static final String ATTR_MAX_VALUE = "maxValue";
    
    static {
        MetaDataRegistry.registerType(IntegerField.class, def -> def
            .type(TYPE_FIELD).subType(SUBTYPE_INT)
            .description("Integer field with range validation")
            
            // NUMERIC-SPECIFIC ATTRIBUTES
            .optionalAttribute(ATTR_MIN_VALUE, "int")
            .optionalAttribute(ATTR_MAX_VALUE, "int")
            // Inherits: required, defaultValue from MetaField
        );
    }
    
    // Remove existing static registration block completely
}
```

**File: `metadata/src/main/java/com/draagon/meta/object/MetaObject.java`**

```java
public class MetaObject extends MetaData {
    public static final String TYPE_OBJECT = "object";
    
    static {
        MetaDataRegistry.registerType(MetaObject.class, def -> def
            .type(TYPE_OBJECT).subType("base")
            .description("Object containing fields and attributes")
            
            // OBJECTS CONTAIN FIELDS (any field type, any name)
            .optionalChild("field", "*", "*")
            
            // OBJECTS CAN HAVE ATTRIBUTES
            .optionalAttribute("description", "string")
        );
    }
}
```

**File: `metadata/src/main/java/com/draagon/meta/attr/StringAttribute.java`**

```java
public class StringAttribute extends MetaAttribute<String> {
    public static final String TYPE_ATTR = "attr";
    public static final String SUBTYPE_STRING = "string";
    
    static {
        MetaDataRegistry.registerType(StringAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_STRING)
            .description("String attribute value")
            // NO CHILD REQUIREMENTS - just registers identity
        );
    }
    
    // Remove existing placement constraint registration completely
}
```

### **STEP 3: Remove Dual Registry Pattern (Week 2)**

**File: `metadata/src/main/java/com/draagon/meta/type/MetaDataTypeRegistry.java`**

**ACTION: DELETE THIS FILE** - It's the obsolete singleton registry

**File: `metadata/src/main/java/com/draagon/meta/constraint/ConstraintRegistry.java`**

**ACTION: DELETE THIS FILE** - Constraints now integrated into type definitions

**Files to Update:**
- Remove all imports of `com.metaobjects.type.MetaDataTypeRegistry`
- Remove all imports of `com.metaobjects.constraint.ConstraintRegistry`
- Remove all `ConstraintRegistry.getInstance().addConstraint()` calls

### **STEP 4: Create Service-Based Extensions (Week 3)**

**File: `metadata/src/main/java/com/draagon/meta/registry/DatabaseAttributeProvider.java`**

```java
public class DatabaseAttributeProvider implements MetaDataTypeProvider {
    public static final String ATTR_DB_COLUMN = "dbColumn";
    public static final String ATTR_DB_TABLE = "dbTable";
    public static final String ATTR_DB_NULLABLE = "dbNullable";
    
    @Override
    public void enhanceTypes(MetaDataRegistry registry) {
        // ALL FIELD TYPES can have database column attributes
        registry.addGlobalChildRequirement("field", "*",
            new ChildRequirement(ATTR_DB_COLUMN, "attr", "string", false));
            
        registry.addGlobalChildRequirement("field", "*", 
            new ChildRequirement(ATTR_DB_NULLABLE, "attr", "boolean", false));
            
        // ALL OBJECT TYPES can have database table attributes  
        registry.addGlobalChildRequirement("object", "*",
            new ChildRequirement(ATTR_DB_TABLE, "attr", "string", false));
    }
}
```

### **STEP 5: Enhanced Validation (Week 4)**

**File: `metadata/src/main/java/com/draagon/meta/MetaData.java`**

Update the `addChild()` method:

```java
public void addChild(MetaData child) {
    // VALIDATE: Does this parent accept this child?
    ChildRequirement req = MetaDataRegistry.getInstance()
        .getChildRequirement(this.getType(), this.getSubType(), child.getName());
        
    if (req == null) {
        // CHECK WILDCARD MATCHES
        List<ChildRequirement> allReqs = MetaDataRegistry.getInstance()
            .getChildRequirements(this.getType(), this.getSubType());
            
        boolean matches = allReqs.stream()
            .anyMatch(r -> r.matches(child.getType(), child.getSubType(), child.getName()));
            
        if (!matches) {
            throw new MetaDataException(String.format(
                "%s.%s does not accept child '%s' of type %s.%s. Supported children: %s",
                this.getType(), this.getSubType(), child.getName(),
                child.getType(), child.getSubType(), getSupportedChildrenDescription()));
        }
    } else {
        // VALIDATE SPECIFIC REQUIREMENT
        if (!req.matches(child.getType(), child.getSubType(), child.getName())) {
            throw new MetaDataException(String.format(
                "Child '%s' expects %s.%s but got %s.%s",
                child.getName(), req.getExpectedType(), req.getExpectedSubType(),
                child.getType(), child.getSubType()));
        }
    }
    
    super.addChild(child);
}
```

### **STEP 6: Schema Generation Enhancement (Week 4)**

**File: `codegen/src/main/java/com/draagon/meta/generator/direct/metadata/file/xsd/MetaDataFileXSDWriter.java`**

Update to use child requirements:

```java
private void generateElementSchema(String elementType, String elementSubType) {
    TypeDefinition def = MetaDataRegistry.getInstance().getTypeDefinition(elementType, elementSubType);
    List<ChildRequirement> children = def.getChildRequirements();
    
    // GENERATE XSD ELEMENTS for each child requirement
    for (ChildRequirement child : children) {
        if (child.getExpectedType().equals("attr")) {
            // Generate attribute element in XSD
            generateAttributeElement(child.getName(), child.getExpectedSubType(), child.isRequired());
        } else {
            // Generate child element in XSD  
            generateChildElement(child.getExpectedType(), child.getExpectedSubType(), child.isRequired());
        }
    }
}
```

## 🧪 **Testing Requirements**

### **STEP 7: Update Tests**

**File: `metadata/src/test/java/com/draagon/meta/registry/UnifiedRegistryTest.java`** (NEW)

```java
public class UnifiedRegistryTest {
    
    @Test
    public void testSingleRegistrationPoint() {
        // Verify StringField registration includes pattern attribute
        TypeDefinition def = MetaDataRegistry.getInstance().getTypeDefinition("field", "string");
        assertNotNull(def);
        
        ChildRequirement patternReq = def.getChildRequirement("pattern");
        assertNotNull(patternReq);
        assertEquals("attr", patternReq.getExpectedType());
        assertEquals("string", patternReq.getExpectedSubType());
        assertFalse(patternReq.isRequired());
    }
    
    @Test
    public void testChildValidation() {
        StringField stringField = new StringField("email");
        StringAttribute patternAttr = new StringAttribute("pattern");
        
        // Should succeed - StringField accepts pattern attribute
        assertDoesNotThrow(() -> stringField.addChild(patternAttr));
        
        StringAttribute invalidAttr = new StringAttribute("invalidAttribute");
        
        // Should fail - StringField doesn't accept invalidAttribute
        assertThrows(MetaDataException.class, () -> stringField.addChild(invalidAttr));
    }
    
    @Test
    public void testServiceBasedExtensions() {
        // Verify database service adds global requirements
        List<ChildRequirement> fieldReqs = MetaDataRegistry.getInstance()
            .getChildRequirements("field", "string");
            
        boolean hasDbColumn = fieldReqs.stream()
            .anyMatch(req -> req.getName().equals("dbColumn"));
        assertTrue(hasDbColumn);
    }
}
```

### **STEP 8: Validation Tests**

Run these commands to verify implementation:

```bash
# Compile everything
mvn clean compile

# Run tests
mvn test

# Verify specific tests
cd metadata && mvn test -Dtest=UnifiedRegistryTest
```

**Expected Results:**
- All existing tests continue to pass
- No compilation errors
- New unified registry functionality works
- Child validation prevents invalid placements
- Schema generation uses child requirements

## ✅ **Success Criteria**

### **Technical Validation**
- [ ] Single MetaDataRegistry class replaces dual pattern
- [ ] All MetaData classes use unified registration API
- [ ] Child requirements integrated (no separate ConstraintRegistry)
- [ ] Constants owned by appropriate classes (no central constants)
- [ ] Generic child requirements support any MetaData type
- [ ] Service-based extensions work for cross-cutting concerns
- [ ] All existing tests continue to pass
- [ ] Better error messages with supported children lists

### **Code Quality Validation**
- [ ] No `com.metaobjects.type.MetaDataTypeRegistry` imports remain
- [ ] No `ConstraintRegistry` imports or usage remain
- [ ] Each class owns its own constants (TYPE_*, SUBTYPE_*, ATTR_*)
- [ ] Parent types define what children they accept
- [ ] Child types just register their identity
- [ ] No overly broad placement rules (like "any MetaData")

### **Functional Validation**
- [ ] StringField accepts pattern attribute but rejects invalid attributes
- [ ] MetaObject can contain MetaField children
- [ ] EmailValidator requires specific pattern attribute
- [ ] Database service adds cross-cutting attributes to all field types
- [ ] Schema generation knows required/optional children
- [ ] Inline attribute parsing works with type validation

## 🚨 **Critical Implementation Notes**

### **Architecture Compliance**
- **Read-Optimized Pattern**: This is a READ-OPTIMIZED framework like Java Class/Field reflection
- **OSGI Compatibility**: Maintain WeakHashMap patterns and service discovery
- **Thread Safety**: Registry access must be thread-safe for concurrent reads
- **Performance**: Optimize for heavy read access, infrequent registration

### **Migration Strategy**
- **Backward Compatibility**: Maintain deprecated wrapper methods during transition
- **Gradual Migration**: Update classes incrementally, test continuously
- **Service Discovery**: Ensure OSGI service loading continues to work

### **Error Handling**
- **Rich Error Messages**: Include supported children lists in validation errors
- **Context Preservation**: Maintain metadata paths in exceptions
- **Validation Context**: Use ValidationContext for detailed error reporting

## 🎯 **Final Deliverables**

1. **Unified MetaDataRegistry** - Single registry with integrated child requirements
2. **Updated Core Classes** - All MetaData classes using new registration pattern
3. **Service Providers** - Database/security cross-cutting attribute providers
4. **Enhanced Validation** - Child requirement validation in addChild()
5. **Schema Generation** - XSD/JSON generators using child requirements
6. **Comprehensive Tests** - Verify all functionality and edge cases
7. **Migration Complete** - No dual registry references remain

**This implementation will eliminate the dual registry pattern, integrate child requirements with type definitions, and provide a clean foundation for schema generation and service-based extensions while maintaining the flexibility needed for a power-user metadata framework.**