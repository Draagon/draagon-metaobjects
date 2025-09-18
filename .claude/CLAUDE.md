# MetaObjects Project - Claude AI Assistant Guide

## ⚠️ CRITICAL ARCHITECTURAL PRINCIPLE ⚠️

**MetaObjects follows a LOAD-ONCE IMMUTABLE design pattern analogous to Java's Class/Field reflection system:**

- **MetaData objects are loaded once during application startup and remain immutable thereafter**
- **They are permanent in memory for the application lifetime (like Java Class objects)**
- **Thread-safe for concurrent READ operations after loading phase**
- **DO NOT treat MetaData as mutable domain objects - they are immutable metadata definitions**

### Framework Analogy
| Java Reflection | MetaObjects Framework |
|----------------|----------------------|
| `Class.forName()` | `MetaDataLoader.load()` |
| `Class.getFields()` | `MetaObject.getMetaFields()` |
| `Field.get(object)` | `MetaField.getValue(object)` |
| Permanent in memory | Permanent MetaData objects |
| Thread-safe reads | Thread-safe metadata access |
| ClassLoader registry | MetaDataTypeRegistry |

## Project Overview

MetaObjects is a Java-based suite of tools for metadata-driven development, providing sophisticated control over applications beyond traditional model-driven development techniques.

- **Current Version**: 5.2.0 (development) 
- **Java Version**: Java 21
- **Build Tool**: Maven
- **License**: Apache License 2.0

## Current Architecture (v6.0.0+)

### Service-Based Type Registry
- **MetaDataTypeRegistry**: Service-based type registry (replaces TypesConfig)
- **MetaDataEnhancementService**: Context-aware metadata enhancement
- **ServiceLoader Discovery**: OSGI-compatible service discovery
- **Cross-Language Ready**: String-based types work across Java/C#/TypeScript

### Project Structure
```
├── metadata/           # Base metadata models and types
├── codegen/           # Code generation libraries (v6.0.0+)
├── maven-plugin/      # Maven plugin for code generation
├── core/              # Core MetaObjects functionality  
├── om/                # Object Manager module
├── omdb/              # Database Object Manager
├── omnosql/           # NoSQL Object Manager
├── web/               # React MetaView components
├── demo/              # Demo applications with React integration
└── docs/              # Documentation
```

### Build Dependencies (CRITICAL ORDER)
```
metadata → codegen → maven-plugin → core → om → omdb/omnosql → web → demo
```

**Build the project**: `mvn clean compile`

## Core Concepts

### MetaObjects Types
- **ValueMetaObject**: Dynamic objects with public getter/setter access
- **DataMetaObject**: Wrapped objects with protected access + Builder patterns
- **ProxyMetaObject**: Proxy implementations without concrete classes
- **MappedMetaObject**: Works with Map interface objects

### Key Features
- **Metadata-driven development** with sophisticated control mechanisms
- **Cross-language code generation** (Java, C#, TypeScript)
- **JSON/XML serialization** with custom type adapters
- **Validation framework** with field-level and object-level validators
- **React MetaView System** with TypeScript components
- **Enhanced error reporting** with hierarchical paths and context

## Current Service Architecture Patterns

### MetaDataTypeRegistry Usage
```java
// v6.0.0+ pattern - service-based type registry
MetaDataTypeRegistry registry = ServiceRegistry.getInstance()
    .getService(MetaDataTypeRegistry.class);
    
// Register custom types
registry.registerType("customField", CustomField.class);
```

### MetaDataEnhancement Usage  
```java
// Attribute enhancement for cross-cutting concerns
MetaDataEnhancementService enhancer = new MetaDataEnhancementService();
for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
    enhancer.enhanceForService(metaObject, "objectManagerDB", 
        Map.of("dialect", "postgresql", "schema", "public"));
}
// Objects now have dbTable, dbCol, dbNullable attributes
```

### Modern API Patterns
```java
// Optional-based APIs (v5.1.0+)
Optional<String> name = metaObject.findString("name");
String email = metaObject.requireString("email"); // throws if missing

// Builder patterns
ValueObject obj = ValueObject.builder()
    .field("name", "John")
    .field("age", 30)
    .build();

// Stream APIs
metaObject.getFieldsStream()
    .filter(field -> field.getType() == DataTypes.STRING)
    .forEach(field -> processField(field));
```

## React MetaView Integration

### Backend API Pattern
```java
// Spring REST controllers serve metadata as JSON
@RestController
public class MetaDataApiController {
    
    @GetMapping("/api/metadata/{objectName}")
    public ResponseEntity<String> getMetaObjectJson(@PathVariable String objectName) {
        MetaObject metaObject = loader.getMetaObject(objectName);
        return ResponseEntity.ok(JsonObjectWriter.write(metaObject));
    }
}
```

### Frontend Component Pattern
```typescript
// TypeScript MetaView components
import { MetaViewRenderer } from './components/metaviews/MetaViewRenderer';
import { MetaObjectForm } from './components/forms/MetaObjectForm';

// Automatic form generation from metadata
<MetaObjectForm 
    metaObjectName="User"
    onSubmit={handleSubmit}
    validation={true}
/>
```

### JSON Metadata Location
- Place JSON metadata in `/src/main/resources/metadata/` for proper classpath loading
- Use FileMetaDataLoader with JsonMetaDataParser for existing infrastructure

## Development Guidelines

### Code Style & Patterns
- Use **SLF4J** for all logging (migrated from Commons Logging)
- Follow **Builder patterns** for complex object creation
- Use **Optional-based APIs** for safe null handling
- Maintain **backward compatibility** in all changes
- Add **comprehensive JavaDoc** for public APIs

### Testing
- Use **JUnit 4.13.2** for testing
- All modules must compile successfully with Java 21
- Test files should follow existing patterns in `*/src/test/java/`

### Module Integration
- **codegen module**: Contains all code generation functionality
- **web module**: React TypeScript components and generic Spring controllers  
- **demo module**: Fishstore demo, data controllers, and JSON metadata
- **Controllers**: Demo-specific controllers → demo module, generic → web module

### React & Frontend Development
- **Use Existing Infrastructure**: Leverage FileMetaDataLoader, JsonObjectWriter from IO package
- **Spring Integration**: Proper controller module placement based on functionality
- **TypeScript Components**: Follow React MetaView patterns for metadata-driven UI
- **State Management**: Redux Toolkit with React Query for form state and data fetching

## Enhanced Error Reporting (v5.2.0+)

### Rich Exception Context
```java
try {
    // MetaObjects operation
} catch (MetaDataException e) {
    Optional<MetaDataPath> path = e.getMetaDataPath();
    Optional<String> operation = e.getOperation();
    Map<String, Object> context = e.getContext();
    
    // Enhanced error messages include context and alternatives
    // "Field 'invalidField' not found in User: Available: age, email, name"
}
```

### Factory Method Patterns
```java
// Context-rich exception creation
ObjectNotFoundException.forId(userId, metaObject, "userLookup");
PersistenceException.forSave(entity, metaObject, sqlException);
GeneratorException.forTemplate("user.java.vm", metaObject, templateException);
```

## Key Build Commands

```bash
# Build entire project
mvn clean compile

# Run tests
mvn test

# Package project
mvn package

# Generate code using MetaObjects plugin
mvn metaobjects:generate

# Build specific module (respects dependency order)
cd metadata && mvn compile
cd core && mvn compile
```

## Maven Configuration
- Parent POM manages dependency versions
- OSGi bundle support enabled via Apache Felix Maven Bundle Plugin
- Java 21 compatibility with --release flag
- Two distribution profiles: default (Draagon) and nexus (Maven Central)

## Critical Files for Development

### Source Code
- Core source: `core/src/main/java/com/draagon/meta/`
- Metadata models: `metadata/src/main/java/com/draagon/meta/`
- Tests: `*/src/test/java/`

### React MetaView System
- React components: `web/src/typescript/components/metaviews/`
- React forms: `web/src/typescript/components/forms/`
- TypeScript types: `web/src/typescript/types/metadata.ts`
- Spring controllers: `web/src/main/java/com/draagon/meta/web/react/api/`
- Demo controllers: `demo/src/main/java/com/draagon/meta/demo/fishstore/api/`
- JSON metadata: `demo/src/main/resources/metadata/fishstore-metadata.json`

### Configuration
- `pom.xml` - Main project configuration
- `package.json` & `tsconfig.json` - React/TypeScript configuration (web module)
- `README.md` - Basic project information
- `RELEASE_NOTES.md` - Version history

## Key Technologies & Dependencies

- **Java 21** with Maven compiler plugin 3.13.0
- **SLF4J + Logback** for logging
- **JUnit 4.13.2** for testing
- **OSGi Bundle Support** via Apache Felix Maven Bundle Plugin
- **Gson 2.13.1** for JSON handling
- **Commons Validator 1.9.0** for validation
- **React + TypeScript** for frontend components
- **Redux Toolkit** for state management

## VERSION MANAGEMENT FOR CLAUDE AI

**CRITICAL**: When user requests "increment version", "update version", or "release new version":

1. **AUTOMATICALLY UPDATE ALL VERSION REFERENCES**:
   - All pom.xml files (root + 8 modules)
   - README.md "Current Release:" line  
   - RELEASE_NOTES.md (add new version section)
   - This CLAUDE.md version references

2. **FOLLOW VERSION STRATEGY**:
   - Release: Remove -SNAPSHOT from current version
   - Next Dev: Increment version + add -SNAPSHOT
   - Ensure ALL modules have identical versions

3. **VERIFY BUILD**: Run `mvn clean compile` after changes

This ensures complete version synchronization across the entire project when versions are updated.