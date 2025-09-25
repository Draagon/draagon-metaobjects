# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

MetaObjects is a comprehensive Java suite for **metadata-driven development**, featuring a completely modular architecture (v5.2.0+) designed for modern enterprise software development. It provides sophisticated control over applications beyond traditional model-driven development techniques.

**Key Facts:**
- **Current Version:** 5.2.0-SNAPSHOT (Modular Architecture)
- **Java Version:** Java 17 LTS (Production Ready)
- **Build Tool:** Maven 3.9+
- **License:** Apache License 2.0

## Essential Build Commands

### Full Project Build
```bash
# Clean build of all modules
mvn clean compile

# Run all tests across modules
mvn test

# Package all modules
mvn package

# Full clean build with packaging
mvn clean package
```

### Module-Specific Building
```bash
# Build specific module (respect dependency order)
mvn -pl metadata clean compile
mvn -pl core clean compile
mvn -pl web clean compile

# Build with dependencies
mvn -pl core -am clean compile
```

### Testing Commands
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn -pl metadata test
mvn -pl core test

# Run single test class
mvn -Dtest=VehicleMetadataTest test

# Run with detailed output
mvn test -X
```

### Web Module (React/TypeScript)
```bash
# Navigate to web module
cd web

# Install dependencies
npm install

# Development mode with TypeScript watch
npm run dev

# Build TypeScript and CSS
npm run build

# Run Jest tests
npm run test

# Lint TypeScript/React code
npm run lint
npm run lint:fix
```

### Code Generation
```bash
# Generate code using Maven plugin
mvn com.draagon:metaobjects-maven-plugin:generate

# Clean and regenerate
mvn clean compile com.draagon:metaobjects-maven-plugin:generate
```

### Examples and Demos
```bash
# Run basic example (core functionality)
cd examples/basic-example && mvn compile exec:java

# Run Spring integration example
cd examples/spring-example && mvn compile exec:java

# Run OSGi example
cd examples/osgi-example && mvn compile exec:java
```

## High-Level Architecture

MetaObjects follows a **READ-OPTIMIZED WITH CONTROLLED MUTABILITY** design pattern, analogous to Java's Class/Field reflection system with dynamic class loading.

### Core Architectural Principles
1. **Loading Phase vs Runtime Phase**: MetaData objects are loaded once during application startup and optimized for heavy read access
2. **Permanent Memory Residence**: Like Java Class objects, MetaData stays in memory for the application lifetime
3. **Thread-Safe Reads**: No synchronization needed for read operations (99.9% of use cases)
4. **Copy-on-Write Updates**: Infrequent updates use atomic reference swapping to maintain read performance

### Modular Architecture (v5.2.0+)

#### Core Modules
- **`metadata/`** - Core metadata definitions and constraint system
- **`core/`** - File-based metadata loading and core functionality

#### Code Generation
- **`codegen-base/`** - Base code generation framework
- **`codegen-mustache/`** - Mustache template-based code generation
- **`codegen-plantuml/`** - PlantUML diagram generation
- **`maven-plugin/`** - Maven integration for build-time code generation

#### Framework Integration
- **`core-spring/`** - Spring Framework integration
- **`web-spring/`** - Spring Web integration with REST controllers

#### Object Management
- **`om/`** - Object Manager for metadata-driven object persistence
- **`omdb/`** - Database Object Manager (SQL databases)
- **`omnosql/`** - NoSQL Object Manager

#### Web & Demo
- **`web/`** - React TypeScript components and web utilities
- **`demo/`** - Demo applications with complete examples
- **`examples/`** - Comprehensive usage examples for all scenarios

### Build Dependencies (Critical Order)
```
metadata → codegen-* → core → *-spring → om → web → demo → examples
```

### MetaData Type System
The framework uses a service-based type registry with these key components:

#### MetaDataLoader Pattern (ClassLoader Analogy)
```java
// LOADING PHASE - Once at startup
MetaDataLoader loader = new SimpleLoader("myLoader");
loader.setSourceURIs(Arrays.asList(URI.create("metadata.json")));
loader.init(); // Loads ALL metadata into permanent memory

// RUNTIME PHASE - Read-only operations
MetaObject userMeta = loader.getMetaObjectByName("User");  // O(1) lookup
MetaField field = userMeta.getMetaField("email");          // Cached access
```

#### Cache Strategy
- **ConcurrentHashMap**: Permanent cache for core metadata lookups
- **WeakHashMap**: Computed cache for derived calculations (OSGi-compatible)
- **Dual Strategy**: Balances performance with memory cleanup in long-running applications

### Constraint System (v5.2.0+)
Constraints are self-registered programmatically via static initializers in MetaData classes:

```java
@MetaDataType(type = "field", subType = "string")
public class StringField extends PrimitiveField<String> {
    static {
        // Self-registration with constraint setup
        MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
        registry.registerHandler(
            new MetaDataTypeId(TYPE_FIELD, SUBTYPE_STRING), 
            StringField.class
        );
        setupStringFieldConstraints();
    }
}
```

## Development Patterns

### Thread-Safety Guidelines
- **Loading Phase**: Use synchronization for writes/construction
- **Runtime Phase**: No synchronization needed for reads (immutable after loading)
- **Update Phase**: Copy-on-write for infrequent metadata updates

### Critical Architectural Rules
1. **DO NOT** treat MetaData as frequently mutable domain objects
2. **DO NOT** replace WeakHashMap with strong references (breaks OSGi compatibility)
3. **DO NOT** create new MetaDataLoader instances frequently
4. **DO** follow ClassLoader patterns for caching and lifecycle management

### Testing with Shared Registry
All tests inherit from `SharedRegistryTestBase` to prevent registry conflicts:
```java
@IsolatedTest // For tests that must manipulate registry directly
public class MyTest extends SharedRegistryTestBase {
    // Test implementation
}
```

### Spring Integration Patterns
```java
@Bean
@Singleton
public MetaDataLoader applicationMetaDataLoader() {
    SimpleLoader loader = new SimpleLoader("app-metadata");
    loader.setSourceURIs(getMetadataSourceURIs());
    loader.init(); // Heavy one-time cost
    return loader; // Permanent application bean
}
```

### React MetaView Integration
- Backend serves metadata as JSON via Spring REST controllers
- Frontend uses TypeScript MetaView components for automatic form generation
- State management with Redux Toolkit and React Query

### Inline Attribute Support (v5.2.0+)
Reduce metadata verbosity with inline attributes:

**JSON Format (@ prefixed):**
```json
{
  "field": {
    "name": "email",
    "type": "string",
    "@required": true,
    "@maxLength": 255
  }
}
```

**XML Format (no prefix):**
```xml
<field name="email" type="string" required="true" maxLength="255" />
```

## OSGi Compatibility

The framework is designed for OSGi environments with dynamic bundle loading/unloading:
- Service discovery uses ServiceLoader pattern
- WeakReference patterns prevent memory leaks
- Bundle classloader cleanup supported
- Service registrations cleaned up automatically

## Security & Modernization (2024-2025)

**Comprehensive modernization completed:**
- ✅ **Security**: CVE-2015-7501 & CVE-2015-6420 vulnerabilities eliminated
- ✅ **Java 17 LTS**: Production-ready migration from Java 21
- ✅ **Dependencies**: Updated to secure versions (Spring 5.3.39, Commons Lang3 3.18.0)
- ✅ **Code Quality**: 341 lines of deprecated/vulnerable code removed
- ✅ **CI/CD**: GitHub Actions modernized with security improvements
- ✅ **Test Success**: 117+ tests passing with 100% success rate

## Important Notes from CLAUDE.md

When working with this codebase, remember:

1. **Read-Optimized Architecture**: This is NOT a typical data access pattern - optimize for heavy reads, rare updates
2. **ClassLoader Analogy**: MetaDataLoader operates like Java's ClassLoader with permanent memory residence
3. **Constraint System Integration**: Constraints are programmatically self-registered, no external JSON files
4. **Shared Registry Pattern**: Tests use static shared registry to prevent conflicts
5. **OSGi Bundle Management**: WeakHashMap design is intentional for bundle lifecycle support
6. **Performance Expectations**: 
   - Loading Phase: 100ms-1s (one-time cost)
   - Runtime Reads: 1-10μs (cached, lock-free)
   - Concurrent Readers: Unlimited (no lock contention)

The architecture is sophisticated and follows enterprise patterns for metadata-driven development with high-performance read characteristics.