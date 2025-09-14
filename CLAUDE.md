# MetaObjects Project - Claude AI Assistant Guide

## Project Overview
MetaObjects is a Java-based suite of tools for metadata-driven development, providing sophisticated control over applications beyond traditional model-driven development techniques.

- **Current Version**: 5.1.0 (latest stable: 5.1.0)
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
├── demo/          # Demo applications (commented out in build)
├── web/           # Web-related utilities (commented out in build)
├── omdb/          # Database Object Manager (commented out in build)
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
- **ValueMetaObject**: Dynamic objects with public getter/setter access
- **DataMetaObject**: Wrapped objects with protected accessors
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

## Claude AI Documentation

### Architectural Understanding
- **CLAUDE_ARCHITECTURAL_SUMMARY.md**: Quick reference with key insights and anti-patterns
- **CLAUDE_ARCHITECTURAL_ANALYSIS.md**: Comprehensive architectural analysis and assessment
- **CLAUDE_ENHANCEMENTS.md**: Detailed enhancement plan with implementation roadmap
- **CLAUDE_ARCHITECTURE.md**: Complete architecture guide with design patterns

### ⚠️ Critical Understanding for Claude AI
**MetaObjects is a load-once immutable metadata system** (like Java's Class/Field reflection API). DO NOT treat MetaData objects as mutable domain models. They are permanent, immutable metadata definitions that are thread-safe for reads after the loading phase.

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

## Key Files for Development

- `pom.xml` - Main project configuration
- `README.md` - Basic project information
- `RELEASE_NOTES.md` - Detailed version history and features
- `LICENSE` - Apache 2.0 license
- Core source: `core/src/main/java/com/draagon/meta/`
- Metadata: `metadata/src/main/java/com/draagon/meta/`
- Tests: `*/src/test/java/`

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