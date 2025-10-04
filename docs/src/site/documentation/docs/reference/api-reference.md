# API Reference

This section provides detailed API documentation for the MetaObjects framework's core classes and interfaces. The APIs follow the READ-OPTIMIZED WITH CONTROLLED MUTABILITY pattern and are designed for high-performance metadata-driven applications.

## Core Metadata Classes

### MetaData

**Package**: `com.metaobjects`
**Purpose**: Base class for all metadata definitions

#### Key Methods

```java
public abstract class MetaData {

    // Core identification
    public String getName()
    public String getType()
    public String getSubType()
    public String getPackage()

    // Hierarchy navigation
    public MetaData getParent()
    public List<MetaData> getChildren()
    public <T extends MetaData> List<T> getChildren(Class<T> type)
    public <T extends MetaData> T getChild(Class<T> type, String name)

    // Attribute access
    public boolean hasMetaAttr(String name)
    public MetaAttribute getMetaAttr(String name)
    public List<MetaAttribute> getMetaAttrs()

    // Type validation
    public boolean isType(String type)
    public boolean isSubType(String subType)
    public boolean isType(String type, String subType)

    // Path and reference
    public String getRef()
    public String getFullyQualifiedName()
    public MetaDataPath getPath()

    // Cached operations (thread-safe)
    protected <T> T useCache(String operation, Supplier<T> computation)
}
```

#### Usage Examples

**Basic Metadata Access**:
```java
MetaData metadata = loader.getMetaObjectByName("User");

// Core properties
String name = metadata.getName();           // "User"
String type = metadata.getType();           // "object"
String subType = metadata.getSubType();     // "pojo"
String packageName = metadata.getPackage(); // "com_example_model"

// Hierarchy navigation
MetaData parent = metadata.getParent();
List<MetaField> fields = metadata.getChildren(MetaField.class);
MetaField idField = metadata.getChild(MetaField.class, "id");
```

**Attribute Access**:
```java
// Check for attributes
if (metadata.hasMetaAttr("dbTable")) {
    MetaAttribute dbTable = metadata.getMetaAttr("dbTable");
    String tableName = dbTable.getValueAsString();
}

// Get all attributes
List<MetaAttribute> allAttrs = metadata.getMetaAttrs();
for (MetaAttribute attr : allAttrs) {
    System.out.println(attr.getName() + " = " + attr.getValueAsString());
}
```

**Type Checking**:
```java
// Type validation
if (metadata.isType("object")) {
    // Handle object metadata
}

if (metadata.isType("field", "string")) {
    // Handle string field metadata
}

// Reference and path information
String ref = metadata.getRef();                    // "object:pojo:User"
String fqn = metadata.getFullyQualifiedName();     // "com_example_model::User"
MetaDataPath path = metadata.getPath();            // Structured path representation
```

### MetaObject

**Package**: `com.metaobjects.object`
**Purpose**: Represents object-level metadata with fields, keys, and validation

#### Key Methods

```java
public class MetaObject extends MetaData {

    // Type constants
    public static final String TYPE_OBJECT = "object";
    public static final String SUBTYPE_BASE = "base";

    // Field access
    public List<MetaField> getMetaFields()
    public MetaField getMetaField(String name)
    public boolean hasMetaField(String name)

    // Key access
    public List<MetaKey> getMetaKeys()
    public List<PrimaryKey> getPrimaryKeys()
    public List<ForeignKey> getForeignKeys()
    public List<SecondaryKey> getSecondaryKeys()

    // Validation
    public List<MetaValidator> getValidators()
    public boolean hasValidation()

    // View access
    public List<MetaView> getViews()
    public MetaView getView(String name)

    // Inheritance
    public String getExtends()
    public List<String> getImplements()
    public boolean isInterface()
    public boolean isAbstract()
}
```

#### Usage Examples

**Field Management**:
```java
MetaObject userObject = loader.getMetaObjectByName("User");

// Access fields
List<MetaField> allFields = userObject.getMetaFields();
MetaField emailField = userObject.getMetaField("email");

if (userObject.hasMetaField("id")) {
    MetaField idField = userObject.getMetaField("id");
    System.out.println("ID field type: " + idField.getSubType());
}
```

**Key Access**:
```java
// Primary keys
List<PrimaryKey> primaryKeys = userObject.getPrimaryKeys();
for (PrimaryKey pk : primaryKeys) {
    List<MetaField> keyFields = pk.getKeyFields();
    System.out.println("Primary key: " + keyFields.stream()
        .map(MetaField::getName)
        .collect(Collectors.joining(", ")));
}

// Foreign keys
List<ForeignKey> foreignKeys = userObject.getForeignKeys();
for (ForeignKey fk : foreignKeys) {
    String referencedObject = fk.getReferencedObjectName();
    System.out.println("References: " + referencedObject);
}
```

**Validation and Views**:
```java
// Validation
if (userObject.hasValidation()) {
    List<MetaValidator> validators = userObject.getValidators();
    for (MetaValidator validator : validators) {
        System.out.println("Validator: " + validator.getType());
    }
}

// Views
List<MetaView> views = userObject.getViews();
MetaView editView = userObject.getView("edit");
```

### MetaField

**Package**: `com.metaobjects.field`
**Purpose**: Represents field-level metadata with type, validation, and constraints

#### Key Methods

```java
public class MetaField extends MetaData {

    // Type constants
    public static final String TYPE_FIELD = "field";
    public static final String SUBTYPE_BASE = "base";

    // Field properties
    public boolean isRequired()
    public Object getDefaultValue()
    public String getDefaultValueAsString()

    // Validation
    public List<MetaValidator> getValidators()
    public List<MetaValidator> getDefaultValidatorList()
    public boolean hasValidation()

    // Type-specific properties (for StringField, IntegerField, etc.)
    public Integer getMaxLength()        // StringField
    public Integer getMinLength()        // StringField
    public String getPattern()           // StringField
    public Integer getMinValue()         // IntegerField
    public Integer getMaxValue()         // IntegerField

    // Database integration
    public String getDbColumn()
    public boolean isSearchable()

    // Key participation
    public boolean isIdField()
    public boolean isPrimaryKey()
    public boolean isForeignKey()
}
```

#### Usage Examples

**Basic Field Information**:
```java
MetaField emailField = userObject.getMetaField("email");

// Core properties
boolean required = emailField.isRequired();
Object defaultValue = emailField.getDefaultValue();
String defaultStr = emailField.getDefaultValueAsString();

// Type checking
if (emailField.isSubType("string")) {
    StringField stringField = (StringField) emailField;
    Integer maxLength = stringField.getMaxLength();
    String pattern = stringField.getPattern();
}
```

**Validation Access**:
```java
// Field-level validation
if (emailField.hasValidation()) {
    List<MetaValidator> validators = emailField.getValidators();

    for (MetaValidator validator : validators) {
        if (validator.getType().equals("required")) {
            System.out.println("Email field is required");
        }
        if (validator.getType().equals("pattern")) {
            System.out.println("Email pattern: " + validator.getPattern());
        }
    }
}
```

**Database Integration**:
```java
// Database mapping
String dbColumn = emailField.getDbColumn();        // "email_address"
boolean searchable = emailField.isSearchable();    // true

// Key information
boolean isId = emailField.isIdField();              // false
boolean isPk = emailField.isPrimaryKey();           // false
boolean isFk = emailField.isForeignKey();           // false
```

## Loader System APIs

### MetaDataLoader

**Package**: `com.metaobjects.loader`
**Purpose**: Abstract base class for metadata loading systems

#### Key Methods

```java
public abstract class MetaDataLoader {

    // Core access methods
    public abstract List<MetaObject> getMetaObjects()
    public abstract MetaObject getMetaObjectByName(String name)
    public abstract List<MetaField> getMetaFields()
    public abstract MetaField getMetaFieldByName(String name)

    // Initialization
    public abstract MetaDataLoader init()
    public boolean isInitialized()

    // Configuration
    public MetaDataLoader setMetaDataClassLoader(ClassLoader classLoader)
    public ClassLoader getMetaDataClassLoader()

    // Hierarchy
    public void addParentLoader(MetaDataLoader parent)
    public List<MetaDataLoader> getParentLoaders()

    // Type registry access
    public MetaDataTypeRegistry getTypeRegistry()

    // Class loading
    public Class<?> loadClass(String className) throws ClassNotFoundException
}
```

#### Usage Examples

**Basic Loader Access**:
```java
MetaDataLoader loader = // ... initialization

// Check initialization status
if (!loader.isInitialized()) {
    loader.init();
}

// Access metadata
List<MetaObject> allObjects = loader.getMetaObjects();
MetaObject userObject = loader.getMetaObjectByName("User");

List<MetaField> allFields = loader.getMetaFields();
MetaField emailField = loader.getMetaFieldByName("email");
```

**Loader Hierarchy**:
```java
// Create hierarchical loaders
MetaDataLoader coreLoader = new FileMetaDataLoader("core");
MetaDataLoader businessLoader = new FileMetaDataLoader("business");

// Set up hierarchy
businessLoader.addParentLoader(coreLoader);

// Child loader can access parent metadata
MetaObject coreType = businessLoader.getMetaObjectByName("CoreType"); // From parent
MetaObject businessType = businessLoader.getMetaObjectByName("BusinessType"); // From child
```

### FileMetaDataLoader

**Package**: `com.metaobjects.loader.file`
**Purpose**: File-based metadata loader with JSON/XML support

#### Key Methods

```java
public class FileMetaDataLoader extends MetaDataLoader {

    // Constants
    public static final String SUBTYPE_FILE = "file";
    public static final String XML_EXTENSION = "*.xml";
    public static final String JSON_EXTENSION = "*.json";

    // Constructors
    public FileMetaDataLoader(String name)
    public FileMetaDataLoader(FileLoaderOptions options, String name)

    // Initialization
    public FileMetaDataLoader init(FileMetaDataSources sources)
    public FileLoaderOptions getLoaderOptions()

    // Configuration
    public void configure(LoaderConfiguration config)
    protected void processSources(String sourceDir, List<String> rawSources)
}
```

#### Usage Examples

**File Loader Creation**:
```java
// Simple file loader
FileMetaDataLoader loader = new FileMetaDataLoader("myLoader");

// With custom options
FileLoaderOptions options = new FileLoaderOptions()
    .setVerbose(true)
    .setCacheEnabled(true);
FileMetaDataLoader loader = new FileMetaDataLoader(options, "customLoader");
```

**Source Configuration**:
```java
// Local file sources
LocalFileMetaDataSources localSources = new LocalFileMetaDataSources(
    "/metadata",  // Base directory
    Arrays.asList("user.json", "product.xml")
);

loader.init(localSources);

// URI sources
List<URI> uris = Arrays.asList(
    URI.create("classpath://metadata/core.json"),
    URI.create("file:///opt/metadata/business.xml")
);
URIFileMetaDataSources uriSources = new URIFileMetaDataSources(uris);

loader.init(uriSources);
```

## Registry and Type System APIs

### MetaDataRegistry

**Package**: `com.metaobjects.registry`
**Purpose**: Central registry for type definitions and constraints

#### Key Methods

```java
public class MetaDataRegistry {

    // Singleton access
    public static MetaDataRegistry getInstance()

    // Type registration
    public void registerType(Class<?> implementationClass,
                           Consumer<TypeDefinition> definition)

    // Type lookup
    public TypeDefinition getTypeDefinition(String type, String subType)
    public TypeDefinition findType(String type, String subType)
    public List<String> getRegisteredTypeNames()
    public boolean hasType(String type, String subType)

    // Constraint system
    public void addValidationConstraint(Constraint constraint)
    public List<Constraint> getAllValidationConstraints()
    public List<PlacementConstraint> getPlacementValidationConstraints()

    // Inheritance support
    public List<TypeDefinition> getTypesByInheritance(String parentType, String parentSubType)
    public boolean isTypeInheritedFrom(String type, String subType,
                                     String parentType, String parentSubType)
}
```

#### Usage Examples

**Type Registration**:
```java
MetaDataRegistry registry = MetaDataRegistry.getInstance();

// Register custom type
registry.registerType(CustomField.class, def -> def
    .type("field").subType("custom")
    .description("Custom field with special behavior")
    .inheritsFrom("field", "base")
    .optionalAttribute("customProperty", "string")
);
```

**Type Lookup**:
```java
// Find type definitions
TypeDefinition stringFieldDef = registry.getTypeDefinition("field", "string");
if (stringFieldDef != null) {
    String description = stringFieldDef.getDescription();
    List<String> allowedAttrs = stringFieldDef.getOptionalAttributes();
}

// Check type existence
if (registry.hasType("field", "custom")) {
    System.out.println("Custom field type is registered");
}

// Get all registered types
List<String> allTypes = registry.getRegisteredTypeNames();
```

**Constraint Access**:
```java
// Get all constraints
List<Constraint> allConstraints = registry.getAllValidationConstraints();

// Get placement constraints specifically
List<PlacementConstraint> placementConstraints = registry.getPlacementValidationConstraints();

for (PlacementConstraint constraint : placementConstraints) {
    System.out.println("Placement rule: " + constraint.getDescription());
}
```

### TypeDefinition

**Package**: `com.metaobjects.registry`
**Purpose**: Immutable definition of a metadata type with attributes and constraints

#### Key Methods

```java
public class TypeDefinition {

    // Core properties
    public String getType()
    public String getSubType()
    public String getDescription()
    public Class<?> getImplementationClass()

    // Inheritance
    public boolean hasInheritance()
    public String getParentType()
    public String getParentSubType()
    public TypeDefinition getParentDefinition()

    // Attributes
    public List<String> getRequiredAttributes()
    public List<String> getOptionalAttributes()
    public List<String> getAllAttributes()
    public boolean hasAttribute(String attributeName)

    // Children
    public List<String> getRequiredChildren()
    public List<String> getOptionalChildren()
    public boolean allowsChild(String childType, String childSubType)

    // Builder support
    public TypeDefinition requiredAttribute(String name, String type)
    public TypeDefinition optionalAttribute(String name, String type)
    public TypeDefinition inheritsFrom(String type, String subType)
}
```

#### Usage Examples

**Type Definition Access**:
```java
TypeDefinition stringFieldDef = registry.getTypeDefinition("field", "string");

// Basic properties
String type = stringFieldDef.getType();                    // "field"
String subType = stringFieldDef.getSubType();              // "string"
String description = stringFieldDef.getDescription();       // "String field..."
Class<?> implClass = stringFieldDef.getImplementationClass(); // StringField.class
```

**Inheritance Information**:
```java
// Check inheritance
if (stringFieldDef.hasInheritance()) {
    String parentType = stringFieldDef.getParentType();        // "field"
    String parentSubType = stringFieldDef.getParentSubType();  // "base"

    TypeDefinition parent = stringFieldDef.getParentDefinition();
    System.out.println("Inherits from: " + parent.getDescription());
}
```

**Attribute Validation**:
```java
// Attribute support
List<String> requiredAttrs = stringFieldDef.getRequiredAttributes();
List<String> optionalAttrs = stringFieldDef.getOptionalAttributes();
List<String> allAttrs = stringFieldDef.getAllAttributes();

// Check specific attribute
if (stringFieldDef.hasAttribute("maxLength")) {
    System.out.println("String fields support maxLength attribute");
}
```

## Constraint System APIs

### Constraint

**Package**: `com.metaobjects.constraint`
**Purpose**: Base interface for all constraint types

#### Key Methods

```java
public interface Constraint {

    // Core constraint methods
    void validate(MetaData metaData, Object value) throws ConstraintViolationException
    String getType()
    String getDescription()
    String getConstraintId()

    // Applicability
    default boolean isApplicableTo(String metaDataType) {
        return true;
    }
}
```

### PlacementConstraint

**Package**: `com.metaobjects.constraint`
**Purpose**: Controls where metadata can be placed in the hierarchy

#### Key Methods

```java
public class PlacementConstraint implements Constraint {

    // Static factory methods
    public static PlacementConstraint allowAttribute(String constraintId, String description,
                                                   String parentType, String parentSubType,
                                                   String attributeSubType, String attributeName)

    public static PlacementConstraint allowAttributeOnAnyField(String constraintId, String description,
                                                             String attributeSubType, String attributeName)

    public static PlacementConstraint forbidAttribute(String constraintId, String description,
                                                    String parentType, String parentSubType,
                                                    String attributeSubType, String attributeName)

    // Constraint evaluation
    public boolean appliesTo(MetaData parent, MetaData child)
    public boolean isAllowed()
    public boolean isForbidden()

    // Pattern access
    public String getParentPattern()
    public String getChildPattern()
}
```

#### Usage Examples

**Creating Placement Constraints**:
```java
// Allow maxLength on string fields
PlacementConstraint maxLengthConstraint = PlacementConstraint.allowAttribute(
    "string.maxLength.allowed",
    "String fields can have maxLength attribute",
    "field", "string",
    "int", "maxLength"
);

// Allow required attribute on any field
PlacementConstraint requiredConstraint = PlacementConstraint.allowAttributeOnAnyField(
    "field.required.allowed",
    "Any field can have required attribute",
    "boolean", "required"
);

// Register with registry
MetaDataRegistry.getInstance().addValidationConstraint(maxLengthConstraint);
MetaDataRegistry.getInstance().addValidationConstraint(requiredConstraint);
```

### AttributeConstraintBuilder (v6.2.6+)

**Package**: `com.metaobjects.registry`
**Purpose**: Fluent API for building sophisticated attribute constraints

#### Key Methods

```java
public class AttributeConstraintBuilder {

    // Core constraint building
    public AttributeConstraintBuilder ofType(String attributeSubType)
    public AttributeConstraintBuilder asSingle()
    public AttributeConstraintBuilder asArray()

    // Validation constraints
    public AttributeConstraintBuilder withEnum(String... allowedValues)
    public AttributeConstraintBuilder withPattern(String regex)
    public AttributeConstraintBuilder withRange(int min, int max)
    public AttributeConstraintBuilder withValidation(Predicate<String> validator)

    // Constraint completion
    public void build()
}
```

#### Usage Examples

```java
// Enhanced type registration with fluent constraints
public static void registerTypes(MetaDataRegistry registry) {
    registry.registerType(PrimaryIdentity.class, def -> def
        .type(TYPE_IDENTITY).subType(SUBTYPE_PRIMARY)
        .description("Primary identity for object identification")
        .inheritsFrom("identity", "base")

        // Fluent constraint definition with AttributeConstraintBuilder
        .optionalAttributeWithConstraints(ATTR_GENERATION)
           .ofType(StringAttribute.SUBTYPE_STRING)
           .asSingle()
           .withEnum(GENERATION_INCREMENT, GENERATION_UUID, GENERATION_ASSIGNED)

        // Array-based attributes with fluent syntax
        .optionalAttributeWithConstraints(ATTR_FIELDS)
           .ofType(StringAttribute.SUBTYPE_STRING)
           .asArray()
    );
}

// Universal @isArray support
public boolean isArrayType() {
    return hasMetaAttr("isArray") &&
           Boolean.parseBoolean(getMetaAttr("isArray").getValueAsString());
}
```

### Enhanced ConstraintEnforcer (v6.2.6+)

**Package**: `com.metaobjects.constraint`
**Purpose**: Attribute-specific constraint validation with enhanced error reporting

#### Key Methods

```java
public class ConstraintEnforcer {

    // Enhanced validation methods
    public static void validateAttribute(MetaData metadata, String attributeName, Object value)
    public static void enforceConstraintsOnAddChild(MetaData parent, MetaData child)

    // Constraint checking
    public static List<Constraint> getApplicableConstraints(MetaData metadata)
    public static boolean hasViolations(MetaData metadata)

    // Error reporting
    public static List<ConstraintViolation> getAllViolations(MetaData metadata)
    public static String formatViolationMessage(ConstraintViolation violation)
}
```

## Code Generation APIs

### GeneratorBase

**Package**: `com.metaobjects.generator`
**Purpose**: Base class for all code generators

#### Key Methods

```java
public abstract class GeneratorBase {

    // Core execution
    public abstract void execute(MetaDataLoader loader) throws GeneratorException

    // Configuration
    public void setArg(String name, String value)
    public String getArg(String name)
    public boolean hasArg(String name)
    public Map<String, String> getArgs()

    // Filtering
    public void addFilter(String filter)
    public List<String> getFilters()
    public boolean matchesFilters(String name)

    // Lifecycle
    protected abstract void parseArgs()
    protected abstract void validateConfiguration()
}
```

### DirectGeneratorBase

**Package**: `com.metaobjects.generator.direct`
**Purpose**: Base class for direct generators that operate without templates

#### Key Methods

```java
public abstract class DirectGeneratorBase extends GeneratorBase {

    // Execution template method
    @Override
    public final void execute(MetaDataLoader loader) throws GeneratorException

    // Abstract methods to implement
    protected abstract GenerationContext createContext(MetaDataLoader loader)
    protected abstract void generateOutput(GenerationContext context) throws GeneratorException

    // Utility methods
    protected boolean shouldGenerate(MetaData metadata)
    protected String getOutputPath(MetaData metadata)
}
```

#### Usage Examples

**Custom Generator Implementation**:
```java
public class CustomDocumentationGenerator extends DirectGeneratorBase {

    private String outputDir;
    private String format = "html";

    @Override
    protected void parseArgs() {
        outputDir = getArg("outputDir");
        if (hasArg("format")) {
            format = getArg("format");
        }
    }

    @Override
    protected GenerationContext createContext(MetaDataLoader loader) {
        return new BaseGenerationContext(loader)
            .withOutputDirectory(outputDir)
            .withFormat(format);
    }

    @Override
    protected void generateOutput(GenerationContext context) throws GeneratorException {
        List<MetaObject> objects = context.getLoader().getMetaObjects();

        for (MetaObject object : objects) {
            if (shouldGenerate(object)) {
                generateDocumentationFor(object, context);
            }
        }
    }

    private void generateDocumentationFor(MetaObject object, GenerationContext context) {
        // Custom documentation generation logic
        String fileName = getOutputPath(object) + "." + format;
        // Write documentation to fileName
    }
}
```

## Exception Hierarchy

### MetaDataException

**Package**: `com.metaobjects`
**Purpose**: Base exception for all metadata-related errors

#### Key Methods

```java
public class MetaDataException extends RuntimeException {

    // Constructors
    public MetaDataException(String message)
    public MetaDataException(String message, Throwable cause)
    public MetaDataException(String message, Optional<MetaDataPath> path,
                           Map<String, Object> context)

    // Enhanced error information
    public Optional<MetaDataPath> getMetaDataPath()
    public Optional<String> getOperation()
    public Map<String, Object> getContext()
    public String getSource()

    // Factory methods
    public static MetaDataException forPath(String message, MetaDataPath path)
    public static MetaDataException forOperation(String message, String operation, Object context)
}
```

### ConstraintViolationException

**Package**: `com.metaobjects.constraint`
**Purpose**: Exception thrown when constraint validation fails

#### Key Methods

```java
public class ConstraintViolationException extends MetaDataException {

    // Constructors
    public ConstraintViolationException(String message, String constraintId, MetaData metadata)
    public ConstraintViolationException(String message, Constraint constraint,
                                      MetaData metadata, Object value)

    // Constraint information
    public String getConstraintId()
    public Optional<Constraint> getConstraint()
    public MetaData getViolatingMetaData()
    public Optional<Object> getAttemptedValue()
}
```

## Utility Classes

### MetaDataPath

**Package**: `com.metaobjects.util`
**Purpose**: Represents structured paths through metadata hierarchy

#### Key Methods

```java
public class MetaDataPath {

    // Factory methods
    public static MetaDataPath of(String... segments)
    public static MetaDataPath of(MetaData metadata)
    public static MetaDataPath fromString(String pathString)

    // Path operations
    public MetaDataPath append(String segment)
    public MetaDataPath append(MetaData metadata)
    public MetaDataPath getParent()
    public String getLastSegment()

    // Path information
    public int getDepth()
    public List<String> getSegments()
    public boolean isEmpty()
    public boolean isAbsolute()

    // String representation
    public String toString()                    // "object.User/field.email"
    public String toQualifiedString()           // "com.example::User.email"
}
```

#### Usage Examples

**Path Creation and Navigation**:
```java
// Create paths
MetaDataPath objectPath = MetaDataPath.of("object", "User");
MetaDataPath fieldPath = objectPath.append("field").append("email");

// Path from metadata
MetaField emailField = userObject.getMetaField("email");
MetaDataPath path = MetaDataPath.of(emailField);  // "object.User/field.email"

// Path operations
MetaDataPath parent = fieldPath.getParent();      // "object.User"
String lastSegment = fieldPath.getLastSegment();  // "email"
int depth = fieldPath.getDepth();                 // 4

// String representations
String pathStr = fieldPath.toString();            // "object.User/field.email"
String qualified = fieldPath.toQualifiedString(); // "com_example_model::User.email"
```

This API reference provides the essential interfaces for working with the MetaObjects framework, enabling developers to build metadata-driven applications with confidence in the type safety and performance characteristics of the system.