# MetaObjects Architecture Guide for Claude AI

## ⚠️ CRITICAL ARCHITECTURAL PRINCIPLE ⚠️

**MetaObjects follows a LOAD-ONCE IMMUTABLE design pattern analogous to Java's Class/Field reflection system:**

- **MetaData objects are loaded once during application startup and remain immutable thereafter**
- **They are permanent in memory for the application lifetime (like Java Class objects)**
- **Thread-safe for concurrent READ operations after loading phase**
- **WeakReferences prevent circular references in permanent object graphs**
- **Caching is appropriate - these objects ARE the cache**
- **State management tracks loading phases, not runtime mutations**

**DO NOT treat MetaData as mutable domain objects - they are immutable metadata definitions like Java Classes.**

### Comparison to Java Reflection
| Java Reflection | MetaObjects Framework |
|----------------|----------------------|
| `Class.forName()` | `MetaDataLoader.load()` |
| `Class.getFields()` | `MetaObject.getMetaFields()` |
| `Field.get(object)` | `MetaField.getValue(object)` |
| Permanent in memory | Permanent MetaData objects |
| Thread-safe reads | Thread-safe metadata access |
| ClassLoader registry | MetaDataLoader registry |

## Core Architecture Overview

MetaObjects implements a sophisticated metadata-driven development framework where application behavior and structure are controlled through metadata definitions rather than hard-coded implementations.

## Module Architecture

### Build Dependency Order
```
metadata → maven-plugin → core → om
```

**Critical**: `metadata` must be built first as it generates models used by `core`.

### Module Responsibilities

#### 1. Metadata Module (`metadata/`)
- **Core metadata models**: `MetaObject`, `MetaField`, `MetaAttribute`
- **Exception hierarchy**: `MetaDataException`, `ValueException`, `InvalidMetaDataException`
- **Base interfaces**: `MetaDataAware`, `DataTypeAware`, `MetaDataValueHandler`
- **Data type system**: `DataTypes` enum, validation framework
- **Key classes**: `MetaAttribute`, `StringAttribute`, `ClassAttribute`

#### 2. Maven Plugin Module (`maven-plugin/`)
- **Code generation**: `MetaDataGeneratorMojo`
- **Build integration**: Maven lifecycle integration
- **Configuration**: `GeneratorParam`, `LoaderParam`
- **MojoSupport**: Interface for MetaDataLoader integration

#### 3. Core Module (`core/`)
- **MetaObject implementations**: `ValueMetaObject`, `DataMetaObject`, `ProxyMetaObject`, `MappedMetaObject`
- **Loader framework**: `FileMetaDataLoader`, `XMLFileMetaDataLoader`
- **IO system**: JSON/XML serialization with Gson integration
- **Generators**: PlantUML, XSD, JSON model generators
- **Validation**: Field and object-level validation framework

#### 4. Object Manager Module (`om/`)
- **Expression system**: `Expression`, `ExpressionGroup`, `ExpressionOperator`
- **Query framework**: `QueryOptions`, `Range`, `SortOrder`
- **Manager integration**: `ManagerAwareMetaObject`

## Key Design Patterns

### 1. Metadata-Driven Architecture
```java
// Objects are defined by metadata, not classes
MetaObject metaObj = loader.getMetaObject("MyObject");
Object instance = metaObj.newInstance();
metaObj.setFieldValue(instance, "fieldName", value);
```

### 2. Type System
```java
public enum DataTypes {
    STRING, INT, LONG, FLOAT, DOUBLE, BOOLEAN, DATE, OBJECT, ARRAY
}
```

### 3. MetaObject Inheritance Hierarchy
```
MetaDataAware
    ├── MetaObject (interface)
        ├── ValueMetaObject (dynamic, public access)
        ├── DataMetaObject (wrapped, protected access)  
        ├── ProxyMetaObject (proxy implementations)
        └── MappedMetaObject (Map-based objects)
```

### 4. Loader Architecture
```
MetaDataLoader (base)
    ├── FileMetaDataLoader (sophisticated parser)
    ├── XMLFileMetaDataLoader (backward compatible)
    └── SimpleLoader (strict TypesConfig/MetaModel)
```

## Critical Components

### MetaObject Interface
- `newInstance()` - Create objects from metadata
- `getFieldValue()/setFieldValue()` - Field access
- `performValidation()` - Object validation
- `getMetaField()` - Field metadata access

### Serialization System
- **JsonModelWriter/JsonMetaDataWriter**: Gson-based JSON serialization
- **XMLSerializationHandler**: Custom XML serialization  
- **TypeAdapters**: MetaObject-aware Gson adapters

### Validation Framework
- **Field-level**: `ArrayValidator`, `StringValidator`
- **Object-level**: `MetaValidator` interface
- **Integration**: Automatic validation on object creation/modification

### Generator Framework
- **PlantUML**: UML diagram generation from metadata
- **XSD**: Schema generation for MetaModel files
- **JSON Models**: Export metadata as JSON
- **Code Generation**: Java interface generation

## Data Flow

### 1. Metadata Loading
```
TypesConfig.xml → MetaDataLoader → MetaObject definitions → Registry
```

### 2. Object Creation
```
MetaObject.newInstance() → Apply defaults → Validation → Object instance
```

### 3. Serialization
```
Object → MetaObject → TypeAdapter → JSON/XML output
```

## Configuration Files

### TypesConfig.xml
Defines metadata structure, validation rules, and object relationships:
```xml
<typesConfig>
    <metaObjects>
        <object name="Person" class="com.example.Person">
            <field name="name" type="STRING" required="true"/>
            <field name="age" type="INT" defaultValue="0"/>
        </object>
    </metaObjects>
</typesConfig>
```

### MetaModel Files
Define specific object instances and their metadata.

## Performance Considerations

### Caching Strategy
- **ArrayValidator**: Caches min/max size values with boolean flags
- **MetaObject Registry**: Singleton pattern for metadata caching
- **Type conversion**: Optimized paths for native types

### Memory Management
- Uses modern Java features (Java 21)
- StringBuilder over StringBuffer for performance
- Lazy loading of metadata where possible

## Error Handling Patterns

### Exception Hierarchy
```
MetaException (base)
    ├── MetaDataException
    │   ├── MetaDataNotFoundException
    │   └── InvalidMetaDataException
    └── ValueException
        ├── ValueNotFoundException
        └── InvalidValueException
```

### Error Recovery
- Graceful fallbacks in parsers
- Comprehensive logging with SLF4J
- Validation with detailed error messages

## Integration Points

### Maven Integration
- Build-time code generation
- Classpath-aware loading (runtime/compile/test)
- OSGi bundle generation

### Spring Boot Compatibility
- Gson-based JSON serialization works with Spring Boot
- Replace Jackson with Gson for MetaObject support

### OSGi Support
- Full OSGi bundle metadata
- Classloader-aware metadata loading
- Dynamic package imports/exports

## Development Guidelines

### Adding New MetaObject Types
1. Extend base MetaObject interface
2. Implement required lifecycle methods
3. Add serialization support
4. Update validation framework
5. Add comprehensive tests

### Creating New Generators
1. Implement Generator interface
2. Register in plugin configuration
3. Add Maven goal support
4. Document output format

### Extending Validation
1. Create validator implementing appropriate interface
2. Register in metadata configuration
3. Add to validation chain
4. Test edge cases thoroughly

## Common Pitfalls

### Core Framework Pitfalls
1. **Build Order**: Always build `metadata` before `core`
2. **Classloader Issues**: Set appropriate classloader for OSGi/Maven contexts
3. **Validation**: Don't skip object-level validation
4. **Serialization**: Use MetaObject-aware serializers for complex objects
5. **Immutability Misunderstanding**: DON'T try to modify MetaData after loading - they are immutable like Java Classes
6. **WeakReference Misuse**: DON'T assume WeakReferences are a problem - they prevent memory leaks in permanent object graphs
7. **Type Safety**: Be aware of unchecked cast warnings - these indicate real type safety issues that need addressing

### React Integration Pitfalls
8. **Custom Infrastructure**: DON'T build custom JSON serializers - use existing `JsonObjectWriter` from IO package
9. **Wrong Module Placement**: DON'T put demo-specific controllers in web module - they belong in demo module
10. **Static JSON Files**: DON'T serve JSON metadata as static files - load via `FileMetaDataLoader` for proper integration
11. **Incorrect API Usage**: DON'T use `insertObject()` - use `createObject()` for ObjectManager operations
12. **Wrong Constructor Calls**: DON'T use single-parameter constructors for exceptions requiring multiple parameters
13. **Missing Dependencies**: DON'T forget to add Spring dependencies to modules using Spring annotations
14. **Metadata Location**: DON'T put JSON metadata in webapp/static - use `/src/main/resources/metadata/` for classpath loading

## Framework Enhancement Status (September 2025)

### ✅ COMPLETED MODERNIZATION PROJECT
The MetaObjects framework has been successfully modernized with comprehensive multi-module enhancements:

- **✅ API Consistency**: Optional-based null-safe access patterns across all modules  
- **✅ Type Safety**: Eliminated unsafe casting patterns throughout framework
- **✅ Builder Patterns**: Fluent configuration APIs for improved developer experience
- **✅ Critical Bug Fixes**: Resolved GeneratorParam.setFilters() bug and deprecated code issues
- **✅ Enhanced Documentation**: 200+ lines of comprehensive JavaDoc with practical examples
- **✅ Zero Regressions**: Full backward compatibility maintained

The framework now provides modern, type-safe APIs while preserving its elegant load-once immutable architecture.

## React MetaView Architecture (September 2025)

### Frontend-Backend Integration Pattern

The React MetaView system follows a proper n-tier architecture that integrates seamlessly with existing MetaObjects patterns:

```
React UI Layer
    ↓ (Redux/React Query)
Spring REST API Layer  
    ↓ (JsonObjectWriter from IO package)
MetaObjects Metadata Layer
    ↓ (FileMetaDataLoader + JsonMetaDataParser)
ObjectManagerDB Data Layer
    ↓ (DerbyDriver)
Database Layer
```

### Key Architectural Principles

#### 1. **Leverage Existing Infrastructure**
- **✅ USE**: `FileMetaDataLoader` with `JsonMetaDataParser` for JSON metadata loading
- **✅ USE**: `JsonObjectWriter` from existing IO package for serialization
- **❌ AVOID**: Building custom JSON serializers or metadata loaders
- **✅ USE**: Existing `ObjectManager` API patterns (`createObject()`, `getObjects()`)

#### 2. **Proper Module Boundaries**
- **Web Module**: Generic React components, TypeScript types, metadata API controllers
- **Demo Module**: Demo-specific controllers, sample data services, JSON metadata definitions
- **Rule**: Controllers referencing demo classes belong in demo module, not web module

#### 3. **JSON Metadata Management**
- **Location**: `/src/main/resources/metadata/*.json` for proper classpath loading
- **Loading**: Via `LocalFileMetaDataSources` in Spring configuration
- **Content**: Rich metadata with React-specific view definitions, validators, and attributes

### Component Architecture

#### React TypeScript Layer
```typescript
// Type-safe metadata definitions
interface MetaField {
  name: string;
  type: FieldType;
  validators: ValidationRule[];
  views: Record<string, MetaView>;
}

// Metadata-driven components
<TextView field={metaField} value={value} mode={ViewMode.EDIT} />
<MetaObjectForm metaObject={storeMetaObject} onSubmit={handleSubmit} />
```

#### Spring API Layer
```java
@Controller
@RequestMapping("/api/metadata")
public class MetaDataApiController {
    @Autowired
    private MetaDataLoader metaDataLoader;
    
    // Uses existing JsonObjectWriter
    StringWriter writer = new StringWriter();
    JsonObjectWriter jsonWriter = new JsonObjectWriter(metaDataLoader, writer);
}
```

#### MetaObjects Integration
```xml
<!-- Spring configuration -->
<bean id="loader" class="com.metaobjects.loader.file.FileMetaDataLoader">
    <constructor-arg>
        <bean class="com.metaobjects.loader.file.FileLoaderOptions">
            <property name="sources">
                <list>
                    <bean class="com.metaobjects.loader.file.LocalFileMetaDataSources">
                        <constructor-arg>
                            <list>
                                <value>metadata/fishstore-metadata.json</value>
                            </list>
                        </constructor-arg>
                    </bean>
                </list>
            </property>
        </bean>
    </constructor-arg>
</bean>
```

### Critical Integration Lessons

1. **Don't Reinvent Infrastructure**: The framework already provides FileMetaDataLoader and JsonObjectWriter - use them
2. **Respect Module Dependencies**: Demo controllers access demo classes, so they belong in demo module
3. **JSON Metadata Location**: Must be in resources/metadata/ for classpath loading via FileMetaDataLoader
4. **API Consistency**: Use correct method signatures (`createObject()` not `insertObject()`, proper constructor parameters)
5. **Spring Integration**: Add proper Spring dependencies to modules that use Spring annotations

### End-to-End Data Flow

1. **JSON Metadata** loaded via FileMetaDataLoader at Spring startup
2. **React Frontend** requests metadata via REST API (`/api/metadata/objects/{name}`)
3. **Spring Controller** uses JsonObjectWriter to serialize MetaObject to JSON
4. **React Components** render forms dynamically based on metadata definitions
5. **Form Submissions** flow back through API to ObjectManagerDB for persistence
6. **Sample Data** created automatically via FishstoreService on initialization

This architecture maintains the elegance of MetaObjects' load-once immutable design while enabling modern React frontend development with full type safety and metadata-driven UI generation.

## Service-Based Context Architecture (v5.2.0)

### Overlay Functionality Restoration

Following the v6.0.0 TypesConfig replacement, critical overlay functionality was restored through a sophisticated service-based context architecture:

#### Service-Based Context Pattern
```java
// Service interface for context-specific metadata creation rules
public interface MetaDataContextProvider {
    String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attrName);
}

// ServiceLoader-based discovery registry
public class MetaDataContextRegistry {
    private final List<MetaDataContextProvider> providers;
    
    // Automatic discovery via ServiceLoader
    private MetaDataContextRegistry() {
        this.providers = ServiceLoader.load(MetaDataContextProvider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());
    }
    
    // Context-aware attribute subtype resolution
    public String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attrName) {
        return providers.stream()
            .map(provider -> provider.getContextSpecificAttributeSubType(parentType, parentSubType, attrName))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
```

#### Core Context Provider Implementation
```java
// Implementation that parses metaobjects.types.xml for context rules
public class CoreMetaDataContextProvider implements MetaDataContextProvider {
    private Map<String, Map<String, String>> contextRules;
    
    // Loads rules like: <type name="key"><child type="attr" subType="stringArray" name="keys"/></type>
    private void loadContextRulesFromTypesXML() {
        InputStream typesXML = getClass().getResourceAsStream("/com/draagon/meta/loader/xml/metaobjects.types.xml");
        // Parse XML and build contextRules map
    }
    
    @Override
    public String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attrName) {
        Map<String, String> typeRules = contextRules.get(parentType + "." + parentSubType);
        return typeRules != null ? typeRules.get(attrName) : null;
    }
}
```

#### Enhanced FileMetaDataParser Integration
```java
// Context-aware attribute creation in FileMetaDataParser
private MetaData createNewMetaData(String packageName, String name, String type, 
                                   String subType, String attrName, boolean isRoot) {
    // Fixed field naming: use simple names for children (like pre-v6.0.0)
    String fullname = isRoot 
        ? packageName + MetaDataLoader.PKG_SEPARATOR + name 
        : name;
    
    // Context-aware attribute subtype resolution
    String contextSubType = MetaDataContextRegistry.getInstance()
        .getContextSpecificAttributeSubType(parentType, parentSubType, attrName);
    
    if (contextSubType != null) {
        subType = contextSubType; // Use context-specific subtype
    }
}
```

### Architectural Benefits

#### Service Discovery Pattern
- **ServiceLoader Integration**: Standard Java service discovery mechanism
- **Extensibility**: New context providers can be added without modifying core framework
- **OSGI Compatibility**: No global static state, proper service-based architecture
- **Separation of Concerns**: Clean separation between type registration and context enhancement

#### Context-Aware Metadata Creation
- **Restored Behavior**: 'keys' attributes under 'key' elements properly default to stringArray type
- **Extensible Rules**: New context rules can be added through additional MetaDataContextProvider implementations
- **Backward Compatibility**: All existing metadata definitions continue to work unchanged

#### Overlay Functionality
- **Secondary Metadata Files**: Can now properly augment existing MetaData models during merge and load operations
- **Field Naming Fixed**: Overlay fields created with correct simple names for child elements  
- **Zero Regression**: All test suites pass successfully with restored functionality

### Service Registration
```xml
<!-- META-INF/services/com.metaobjects.registry.MetaDataContextProvider -->
com.metaobjects.registry.CoreMetaDataContextProvider
```

### Usage Patterns
```java
// Framework automatically discovers and uses context providers
MetaDataContextRegistry registry = MetaDataContextRegistry.getInstance();

// Context-aware attribute creation during metadata loading
String subType = registry.getContextSpecificAttributeSubType("key", null, "keys");
// Returns "stringArray" for 'keys' attributes under 'key' elements

// Extensible through additional providers
public class CustomContextProvider implements MetaDataContextProvider {
    public String getContextSpecificAttributeSubType(String parentType, String parentSubType, String attrName) {
        // Custom context rules for enterprise extensions
        if ("customField".equals(parentType) && "specialAttrib".equals(attrName)) {
            return "customType";
        }
        return null;
    }
}
```

This service-based context architecture demonstrates the framework's resilience and extensibility - when functionality was lost during TypesConfig replacement, the service-based foundation provided clean extension points to restore it without architectural disruption.

## Future Architecture (v4.4.0+)

- **Abstract/Interface MetaData**: Native support for inheritance
- **Enhanced IO**: YAML, HOCON, TOML support
- **Plugin Architecture**: Extensible IO readers/writers
- **Editor Integration**: GUI-based metadata editing
- **Advanced TypedMetaDataLoader**: More sophisticated loading control