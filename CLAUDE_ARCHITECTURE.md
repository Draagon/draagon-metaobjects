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

1. **Build Order**: Always build `metadata` before `core`
2. **Classloader Issues**: Set appropriate classloader for OSGi/Maven contexts
3. **Validation**: Don't skip object-level validation
4. **Serialization**: Use MetaObject-aware serializers for complex objects
5. **Immutability Misunderstanding**: DON'T try to modify MetaData after loading - they are immutable like Java Classes
6. **WeakReference Misuse**: DON'T assume WeakReferences are a problem - they prevent memory leaks in permanent object graphs
7. **Type Safety**: Be aware of unchecked cast warnings - these indicate real type safety issues that need addressing

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

## Future Architecture (v4.4.0+)

- **Abstract/Interface MetaData**: Native support for inheritance
- **Enhanced IO**: YAML, HOCON, TOML support
- **Plugin Architecture**: Extensible IO readers/writers
- **Editor Integration**: GUI-based metadata editing
- **Advanced TypedMetaDataLoader**: More sophisticated loading control