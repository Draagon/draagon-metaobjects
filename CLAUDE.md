# MetaObjects Project - Claude AI Assistant Guide

## Project Overview
MetaObjects is a Java-based suite of tools for metadata-driven development, providing sophisticated control over applications beyond traditional model-driven development techniques.

- **Current Version**: 6.0.0 (development) - Major TypesConfig Replacement Architecture
- **Previous Stable**: 5.1.0
- **License**: Apache License 2.0
- **Java Version**: Java 21 (upgraded from Java 1.8)
- **Build Tool**: Maven
- **Organization**: Doug Mealing LLC

## Project Structure

```
├── core/           # Core MetaObjects functionality
├── metadata/       # Metadata models and types
├── maven-plugin/   # Maven plugin for code generation
├── om/            # Object Manager module
├── demo/          # Demo applications with React MetaView integration
├── web/           # Web-related utilities with React MetaView components
├── omdb/          # Database Object Manager
├── omnosql/       # NoSQL Object Manager
└── docs/          # Documentation
```

## Key Build Commands

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package the project
mvn package

# Generate code using MetaObjects plugin
mvn metaobjects:generate

# Launch MetaObjects Editor (planned feature)
mvn metaobjects:editor
```

## Key Technologies & Dependencies

- **Java 21** with Maven compiler plugin 3.13.0
- **SLF4J + Logback** for logging (migrated from Commons Logging)
- **JUnit 4.13.2** for testing
- **OSGi Bundle Support** via Apache Felix Maven Bundle Plugin
- **Gson 2.13.1** for JSON handling
- **Commons Validator 1.9.0** for validation
- **Maven Build System** with comprehensive plugin configuration

## Core Concepts

### MetaObjects Types
- **ValueMetaObject**: Dynamic objects with public getter/setter access and Optional-based find methods
- **DataMetaObject**: Wrapped objects with public accessors and Builder pattern support
- **ProxyMetaObject**: Proxy implementations without concrete classes
- **MappedMetaObject**: Works with Map interface objects

### Key Features
- **Metadata-driven development** with sophisticated control mechanisms
- **Code generation** support for Java interfaces and XML overlays
- **JSON/XML serialization** with custom type adapters
- **Validation framework** with field-level and object-level validators
- **Default field values** support in meta models
- **PlantUML diagram generation** from metadata definitions
- **Maven plugin integration** for build-time code generation
- **React MetaView System** with TypeScript components and automatic form generation

## Recent Major Changes (v5.1.0)

### Java Modernization
- Upgraded from Java 1.8 to Java 21
- Updated Maven compiler to use --release flag
- Resolved OSGi bundle compatibility for Java 21

### Performance Improvements
- Intelligent caching in ArrayValidator
- Optimized getAttrValueAsInt() methods
- Enhanced error handling with fallback strategies

### Code Quality Enhancements
- Migrated from Commons Logging to SLF4J (46 files)
- Replaced StringBuffer with StringBuilder (8 files)
- Added comprehensive JavaDoc documentation
- Resolved 25+ critical TODO items

### Multi-Module Enhancement Project (September 2025)
A comprehensive modernization initiative that enhanced all core modules:

#### Phase 4A: Core Module API Consistency
- **Fixed Visibility Inconsistency**: DataObject methods now public (consistent with ValueObject)
- **Added Optional-Based APIs**: `findString()`, `requireString()`, `findInt()` for null-safe access
- **Implemented Builder Patterns**: ValueObject.Builder, DataObject.Builder, PlantUMLGenerator.Builder
- **Added Stream APIs**: `getKeysStream()`, `getValuesStream()`, `getEntriesStream()`
- **Enhanced JavaDoc**: Comprehensive examples and usage patterns

#### Phase 4B: Maven-Plugin Critical Fixes
- **Fixed Critical Bug**: GeneratorParam.setFilters() parameter assignment issue
- **Modernized Deprecated Code**: Replaced Class.newInstance() with Constructor.newInstance()
- **Added Builder Patterns**: GeneratorParam.Builder, LoaderParam.Builder
- **Enhanced Documentation**: Usage examples and fluent configuration patterns

#### Phase 4C: OM Module Polish
- **Added Optional APIs**: `findObjectByRef()`, `findFirst()`, `firstOptional()`
- **Enhanced QueryBuilder**: 50+ lines of comprehensive usage examples
- **Documented Event System**: Auditing, caching, validation patterns with real-world examples
- **Added Async Methods**: `findObjectByRefAsync()`, `firstOptionalAsync()`

#### Project Impact
- **Zero Regressions**: 100% backward compatibility maintained
- **Enhanced Developer Experience**: Modern, type-safe APIs across all modules
- **Comprehensive Documentation**: 200+ lines of new JavaDoc with practical examples
- **Build Success**: All modules compile and test successfully

### React MetaView System Implementation (September 2025)
A comprehensive React.js integration that extends the JSP-based MetaView system to modern web development:

#### React Components & TypeScript
- **Complete TypeScript Integration**: Type-safe React components with Redux Toolkit state management
- **MetaView Component Library**: TextView, NumericView, DateView, SelectView, CheckboxView React components
- **Dynamic Form Generation**: MetaObjectForm component that automatically renders forms from MetaObject definitions
- **State Management**: Redux-based form state with React Query for data fetching and caching

#### Backend Integration Architecture
- **Spring REST API Controllers**: MetaDataApiController serves metadata as JSON to React frontend
- **Existing IO Package Usage**: Leverages JsonObjectWriter from MetaObjects IO package for proper serialization
- **FileMetaDataLoader Support**: JSON metadata loading via existing FileMetaDataLoader with JsonMetaDataParser
- **ObjectManagerDB Integration**: Full database persistence with Derby driver and auto table creation

#### Fishstore Demo Enhancement
- **Complete React Demo**: Full fishstore storefront application with React components and routing
- **JSON Metadata Definitions**: Rich metadata with React-specific view configurations, validators, and field attributes
- **Sample Data Management**: Automated sample data creation with Store, Breed, Tank, and Fish entities
- **End-to-End Integration**: React frontend → Spring API → MetaObjects metadata → Derby database

#### Key Architectural Lessons
- **Use Existing Infrastructure**: Always leverage existing MetaObjects patterns (FileMetaDataLoader, IO package) vs custom solutions
- **Module Dependency Management**: Controllers belong in demo module when referencing demo classes, not web module
- **JSON Metadata Location**: Place JSON metadata in `/src/main/resources/metadata/` for proper classpath loading
- **API Consistency**: Use correct ObjectManager methods (`createObject()` not `insertObject()`) and constructor signatures

### TypesConfig Replacement Architecture Implementation (v6.0.0)
A comprehensive architectural redesign that replaces the TypesConfig system with modern service-based architecture supporting cross-language implementations:

#### Core Architecture Transformation (Phases A, B, C)

**Phase A: Service-Based Type Registry (Completed ✅)**
- **MetaDataTypeRegistry**: Service-based type registry replacing global TypesConfig
- **ServiceRegistry Abstraction**: OSGI-compatible service discovery with fallback to standard ServiceLoader  
- **MetaDataLoaderRegistry**: Pluggable loader discovery system
- **CoreMetaDataTypeProvider**: Centralized registration of built-in types (fields, validators, views)
- **Complete Parser Migration**: MetaModelParser, SimpleModelParser, FileMetaDataParser updated
- **API Compatibility**: Maintained existing method signatures where possible

**Phase B: Attribute-Driven Service Architecture (Completed ✅)**  
- **MetaDataAttributeProvider**: Service interface for discoverable attribute providers
- **MetaDataEnhancer**: Service interface for context-aware metadata enhancement
- **Shared Attribute Libraries**: DatabaseAttributeProvider, IOAttributeProvider, ValidationAttributeProvider
- **Template-Based Enhancement**: Annotation-driven attribute requirements (@RequiresAttributeProviders)
- **MetaDataEnhancementService**: Central registry for cross-cutting attribute concerns
- **ServiceLoader Discovery**: Automatic provider discovery with priority-based loading

**Phase C: Legacy System Elimination (In Progress 🔄)**
- **Schema Generators Disabled**: XSD/JSON schema writers temporarily disabled pending ValidationChain implementation
- **File Parser Updates**: Core parsing logic migrated to registry system
- **Test Class Migration**: Systematic update of test classes to use registry system
- **TypesConfig Cleanup**: Complete removal of legacy TypesConfig classes and files

#### Key Benefits Achieved

**Cross-Language Compatibility** ✅
- **No Java Class Dependencies**: String-based type/subtype system works across languages
- **Standard Service Patterns**: Interface-based discovery maps to C#/.NET DI and TypeScript modules
- **Portable Architecture**: Service registry pattern universal across enterprise ecosystems

**OSGI & Enterprise Integration** ✅
- **Zero Global Static State**: All services discoverable and pluggable
- **Context-Aware Registries**: Different environments can use different service implementations
- **Dynamic Service Loading**: Runtime discovery and registration of new providers

**Extensibility & Maintainability** ✅
- **Child-Declares-Parent Pattern**: Unlimited extensibility without parent type constraints
- **Separation of Concerns**: Type registration vs. attribute enhancement cleanly separated
- **Template-Driven Development**: Templates declare their attribute requirements declaratively

#### Usage Patterns

**ObjectManagerDB Integration:**
```java
MetaDataEnhancementService enhancer = new MetaDataEnhancementService();
for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
    enhancer.enhanceForService(metaObject, "objectManagerDB", 
        Map.of("dialect", "postgresql", "schema", "public"));
}
// Now objects have dbTable, dbCol, dbNullable attributes
```

**Template-Based Code Generation:**
```java
@RequiresAttributeProviders({"DatabaseAttributes", "ValidationAttributes", "IOAttributes"})
@ForServices({"ormCodeGen", "jpaCodeGen"})
public class JPAEntityTemplate {
    // Template can assume all required attributes exist
}
```

**Cross-Service Attribute Sharing:**
- Same DatabaseAttributeProvider used by ObjectManagerDB AND ORM code generators
- ValidationAttributeProvider shared by UI form generation AND server-side validation  
- IOAttributeProvider used by JSON, XML, and CSV serialization systems

#### Migration Impact
- **100% API Compatibility**: Existing MetaData usage unchanged
- **Enhanced Functionality**: New attribute enhancement capabilities
- **Performance Improvements**: Eliminated global static state and improved caching
- **Cross-Language Ready**: Architecture designed for C# and TypeScript implementations

## Claude AI Documentation

### Architectural Understanding
- **CLAUDE_ARCHITECTURAL_SUMMARY.md**: Quick reference with key insights and anti-patterns
- **CLAUDE_ARCHITECTURAL_ANALYSIS.md**: Comprehensive architectural analysis and assessment
- **CLAUDE_ENHANCEMENTS.md**: Detailed enhancement plan with implementation roadmap
- **CLAUDE_ARCHITECTURE.md**: Complete architecture guide with design patterns

### ⚠️ Critical Understanding for Claude AI
**MetaObjects is a load-once immutable metadata system** (like Java's Class/Field reflection API). DO NOT treat MetaData objects as mutable domain models. They are permanent, immutable metadata definitions that are thread-safe for reads after the loading phase.

**v6.0.0 Service-Based Architecture**: The system now uses service discovery instead of static configuration. MetaDataTypeRegistry and MetaDataEnhancementService are the core services that replace TypesConfig. All type registration and attribute enhancement happens through pluggable service providers discovered via ServiceLoader.

## Development Guidelines

### Code Style
- Use SLF4J for all logging
- Follow existing naming conventions
- Maintain backward compatibility
- Add comprehensive JavaDoc for public APIs
- Use StringBuilder instead of StringBuffer in non-thread-safe contexts

### Testing
- All modules compile successfully with Java 21
- Comprehensive test coverage required
- Use JUnit 4.13.2 for testing
- Test files should follow existing patterns

### React & Frontend Integration
- **Use Existing MetaObjects Infrastructure**: Always leverage FileMetaDataLoader, JsonObjectWriter from IO package
- **JSON Metadata**: Place in `/src/main/resources/metadata/` for proper classpath loading via FileMetaDataLoader
- **Spring Integration**: Use proper controller module placement - demo controllers in demo module, web controllers in web module
- **TypeScript Components**: Follow React MetaView patterns for metadata-driven UI component generation
- **State Management**: Use Redux Toolkit with React Query for form state and data fetching

### Maven Configuration
- Uses parent POM for dependency management
- OSGi bundle support enabled
- Two distribution profiles: default (Draagon) and nexus (Maven Central)
- Comprehensive plugin configuration for Java 21 compatibility

## Module Dependencies

Build order (important for development):
1. `metadata` - Base metadata models
2. `maven-plugin` - Code generation plugin
3. `core` - Core functionality (depends on generated models)
4. `om` - Object Manager
5. `omdb` - Database Object Manager (extends om)
6. `omnosql` - NoSQL Object Manager (extends om)
7. `web` - Web utilities with React MetaView components
8. `demo` - Demo applications with React integration

### Module Integration Notes
- **web** module: Contains React TypeScript components and Spring controllers for metadata APIs
- **demo** module: Contains fishstore React demo, data controllers, and JSON metadata definitions
- **Controllers**: Demo-specific controllers belong in demo module, generic web controllers in web module

## Key Files for Development

- `pom.xml` - Main project configuration
- `README.md` - Basic project information
- `RELEASE_NOTES.md` - Detailed version history and features
- `LICENSE` - Apache 2.0 license
- Core source: `core/src/main/java/com/draagon/meta/`
- Metadata: `metadata/src/main/java/com/draagon/meta/`
- Tests: `*/src/test/java/`

### React MetaView Files
- React components: `web/src/typescript/components/metaviews/`
- React forms: `web/src/typescript/components/forms/`
- TypeScript types: `web/src/typescript/types/metadata.ts`
- Spring controllers: `web/src/main/java/com/draagon/meta/web/react/api/`
- Demo controllers: `demo/src/main/java/com/draagon/meta/demo/fishstore/api/`
- JSON metadata: `demo/src/main/resources/metadata/fishstore-metadata.json`
- Package configuration: `web/package.json`, `web/tsconfig.json`

## Upcoming Features (v4.4.0 Planned)

- Native support for Abstract and Interface MetaData
- TypedMetaDataLoader enhancements
- Enhanced MetaData IO (YAML, HOCON, TOML support)
- Integrated MetaObjects Editor
- Plugin support for IO readers/writers
- Namespace support in XML serialization

## Development Notes

This project follows metadata-driven development principles where the structure and behavior of applications are controlled through metadata definitions rather than hard-coded implementations. The build system is sophisticated with OSGi bundle support and comprehensive Maven plugin configuration.

For Claude AI: When working with this codebase, pay attention to the metadata-driven architecture and maintain the established patterns for MetaObject implementations, validation, and serialization.

## VERSION MANAGEMENT FOR CLAUDE AI

**CRITICAL**: When user requests "increment version", "update version", or "release new version":

1. **AUTOMATICALLY UPDATE ALL VERSION REFERENCES**:
   - All pom.xml files (root + 8 modules)
   - README.md "Current Release:" line
   - RELEASE_NOTES.md (add new version section)
   - CLAUDE.md version references

2. **FOLLOW VERSION STRATEGY**:
   - Release: Remove -SNAPSHOT from current version
   - Next Dev: Increment version + add -SNAPSHOT
   - Ensure ALL modules have identical versions

3. **VERIFY BUILD**: Run `mvn clean compile` after changes

4. **SEE**: CLAUDE_VERSION_MANAGEMENT.md for complete process

This ensures complete version synchronization across the entire project when versions are updated.