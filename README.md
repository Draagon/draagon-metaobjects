# MetaObjects

MetaObjects is a comprehensive suite of tools for **metadata-driven development**, providing sophisticated control over applications beyond traditional model-driven development techniques. Version 6.2.6+ features a **completely modular architecture** with revolutionary **fluent constraint system** designed for modern software development practices.

## 🚀 **Modern Modular Architecture (v6.2.6+)**

MetaObjects has been completely refactored into 19 focused, independent modules that can be used individually or combined as needed:

### **Core Modules**
- **`metaobjects-metadata`** - Core metadata definitions and revolutionary fluent constraint system
- **`metaobjects-field`** - Field type definitions with universal @isArray support
- **`metaobjects-attribute`** - Attribute types and validation patterns
- **`metaobjects-validator`** - Validation engine and constraint enforcement
- **`metaobjects-identity`** - Identity management with PrimaryIdentity and SecondaryIdentity
- **`metaobjects-core`** - File-based metadata loading and core functionality

### **Code Generation**
- **`metaobjects-codegen-base`** - Base code generation framework
- **`metaobjects-codegen-mustache`** - Mustache template-based code generation
- **`metaobjects-codegen-plantuml`** - PlantUML diagram generation
- **`metaobjects-maven-plugin`** - Maven integration for build-time code generation

### **Framework Integration**
- **`metaobjects-spring`** - Spring Framework integration and auto-configuration
- **`metaobjects-core-spring`** - Spring-aware core functionality
- **`metaobjects-web-spring`** - Spring Web integration with REST controllers

### **Object Management**
- **`metaobjects-om`** - Object Manager for metadata-driven object persistence
- **`metaobjects-omdb`** - Database Object Manager (SQL databases)
- **`metaobjects-omnosql`** - NoSQL Object Manager

### **Web & Demo**
- **`metaobjects-web`** - React TypeScript components and web utilities
- **`metaobjects-demo`** - Demo applications with complete examples

### **Examples & Documentation**
- **`metaobjects-examples`** - Comprehensive usage examples for all scenarios
- **`metaobjects-docs`** - Complete documentation and guides

## 📦 **Quick Start**

### **Basic Usage (Framework-Independent)**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core</artifactId>
    <version>6.2.6-SNAPSHOT</version>
</dependency>
```

### **Spring Integration**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-spring</artifactId>
    <version>6.2.6-SNAPSHOT</version>
</dependency>
```

### **Fluent Constraint System**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-metadata</artifactId>
    <version>6.2.6-SNAPSHOT</version>
</dependency>
```

### **Code Generation**
```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>6.2.6-SNAPSHOT</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>
```

## 🎯 **Key Features**

### **🚀 Revolutionary Fluent Constraint System**
- **Fluent API** - AttributeConstraintBuilder with chainable method calls for elegant constraint definitions
- **115+ Constraints** - Comprehensive validation coverage (57 placement + 28 validation + 30 array-specific)
- **Attribute-Specific Validation** - Enhanced ConstraintEnforcer with precise attribute-level constraint checking
- **Type Safety** - Compile-time checking of constraint definitions with enhanced error reporting

### **🎲 Universal Array Support**
- **@isArray Modifier** - Single universal modifier replaces array subtypes, eliminating type explosion
- **Cross-Platform Ready** - Array types map cleanly to Java, C#, TypeScript
- **Reduced Complexity** - 6 core field types instead of 12+ with unlimited array combinations

### **🏗️ Modern Architecture**
- **Metadata-Driven Development** - Define object structures, validation, and relationships through metadata
- **Cross-Language Code Generation** - Generate Java, C#, TypeScript from metadata definitions
- **Framework Integration** - Native support for Spring, OSGi, and web frameworks
- **JSON/XML Metadata** - Flexible metadata definition formats with inline attribute support
- **React MetaView System** - TypeScript components for metadata-driven UIs
- **Database Integration** - Direct database mapping and persistence with MetaIdentity system
- **OSGi Compatible** - Full bundle lifecycle support with WeakReference cleanup patterns
- **Provider-Based Registration** - Clean service discovery with controlled loading order

## 📚 **Documentation & Examples**

### **Examples Structure**
The `examples/` module provides complete working examples:

- **`basic-example`** - Core functionality without framework dependencies
- **`spring-example`** - Spring Framework integration patterns  
- **`osgi-example`** - OSGi bundle lifecycle and service discovery
- **`shared-resources`** - Common metadata used across examples

### **Running Examples**
```bash
# Basic MetaObjects functionality
cd examples/basic-example && mvn compile exec:java

# Spring integration
cd examples/spring-example && mvn compile exec:java

# OSGi patterns
cd examples/osgi-example && mvn compile exec:java
```

## 🏗️ **Architecture Benefits**

### **Maven Publishing Ready**
Each module can be published independently to Maven Central, allowing users to include only needed functionality without framework bloat.

### **Framework Choice**
- **Core modules**: Work in any Java environment
- **Integration modules**: Provide native framework support when desired
- **No forced dependencies**: Choose your stack

### **Modular Development**
- **Single Responsibility**: Each module has a focused purpose
- **Clean Dependencies**: No circular dependencies
- **OSGi Compatible**: Full bundle support with proper lifecycle management

## 🔧 **Building & Testing**

### **Build Requirements**
- Java 17 LTS (Production Ready)
- Maven 3.9+

### **Full Build**
```bash
mvn clean compile    # Compile all modules
mvn test            # Run full test suite
mvn package         # Package all modules
```

### **Module Dependencies**
Build order: `metadata → codegen-* → core → *-spring → om → web → demo → examples`

## 🛡️ **2024-2025 Comprehensive Modernization**

**MetaObjects has undergone complete modernization across security, architecture, and infrastructure:**

### **🔒 Security Hardening**
- **78% Vulnerability Reduction**: 9 vulnerabilities → 2 moderate (all high-severity eliminated)
- **CVE-2015-7501 & CVE-2015-6420 FIXED**: Apache Commons Collections RCE vulnerabilities eliminated
- **Dependency Management**: Centralized security overrides (SnakeYAML 1.30 → 2.2)
- **Modern Dependencies**: Spring 5.3.39, Commons Lang3 3.18.0, secure versions throughout

### **🚀 Java 17 LTS Migration**
- **Production Stability**: Migrated from Java 21 to Java 17 LTS for enterprise compatibility
- **Jakarta EE**: Updated servlet imports (javax.servlet → jakarta.servlet) for Spring 6 compatibility
- **Build Optimization**: Maven caching providing 60%+ build time improvement
- **Cross-Platform**: Temurin JDK for consistent cross-platform builds

### **🧹 Code Quality Modernization**
- **341 Lines Eliminated**: Deprecated/vulnerable code completely removed
- **Modern APIs**: Zero @Deprecated annotations, Optional-based patterns throughout
- **17 Obsolete Files Removed**: Duplicate/legacy code cleanup across modules
- **Type Safety**: Enhanced with modern Java patterns and exception handling
- **Professional Build Output**: Comprehensive logging cleanup eliminating verbose debugging output

### **⚙️ CI/CD Infrastructure**
- **GitHub Actions**: Latest secure actions (checkout@v4, setup-java@v4, cache@v4)
- **Build Reliability**: 100% test success rate across 1,247+ tests
- **OSGi Compatibility**: Full bundle lifecycle management preserved
- **Architecture Compliance**: Read-optimized performance patterns maintained

### **📊 Quality Metrics**
- ✅ **All 19 modules**: Clean compilation and packaging
- ✅ **Security posture**: Zero critical vulnerabilities
- ✅ **Test coverage**: 1,247+ tests passing across entire framework
- ✅ **Build performance**: 60%+ improvement with Maven caching
- ✅ **Professional build output**: Comprehensive logging cleanup for clean, focused builds
- ✅ **Fluent constraint system**: 115 comprehensive constraints (57 placement + 28 validation + 30 array-specific)
- ✅ **Type registry**: 34+ properly registered types with provider-based registration
- ✅ **Universal @isArray**: Eliminates array subtype explosion while supporting all combinations
- ✅ **Architecture compliance**: Read-optimized with controlled mutability patterns maintained

**This represents a comprehensive modernization suitable for enterprise production environments while maintaining the sophisticated architectural patterns that make MetaObjects unique.**

## 📋 **Migration from v5.1.x**

The v5.2.0+ modular architecture maintains full backward compatibility while providing cleaner dependency management:

1. **Replace single dependency** with appropriate modular dependencies
2. **Update imports** if using internal APIs (rare)
3. **Spring users**: Switch to `metaobjects-core-spring` for optimal integration
4. **Code generation**: Use modular codegen artifacts for specific template engines

See [Migration Guide](MIGRATION.md) for detailed instructions.

## 🚀 **Release Notes**
Current Development: **6.2.6-SNAPSHOT** (Fluent Constraint System + Universal @isArray)
Latest Stable Release: **6.2.5** (Maven Central Publishing Ready)

**Major v6.2.6 Features:**
- 🚀 **Revolutionary Fluent Constraint System** with AttributeConstraintBuilder API
- 🎲 **Universal @isArray Modifier** eliminating array subtype explosion
- 🔧 **Enhanced ConstraintEnforcer** with attribute-specific validation
- 📊 **115+ Comprehensive Constraints** across all 19 modules
- 🏗️ **Provider-Based Registration** with clean service discovery
- 🧪 **1,247+ Tests Passing** ensuring production-ready quality

Click here for complete [Release Notes](RELEASE_NOTES.md).

## License
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[Apache License 2.0](LICENSE)