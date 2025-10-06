# MetaObjects

MetaObjects is a comprehensive suite of tools for **metadata-driven development**, providing sophisticated control over applications beyond traditional model-driven development techniques. Version 6.3.1+ features a **completely modular architecture** with revolutionary **fluent constraint system** and **native isArray property** designed for modern software development practices.

## 🚀 **Modern Modular Architecture (v6.3.1+)**

MetaObjects Core has been refactored into **9 focused modules** that provide the foundation for metadata-driven development:

### **Core Modules**
- **`metaobjects-metadata`** - Core metadata definitions and revolutionary fluent constraint system
- **`metaobjects-core`** - File-based metadata loading and core functionality

### **Code Generation**
- **`metaobjects-codegen-base`** - Base code generation framework
- **`metaobjects-codegen-mustache`** - Mustache template-based code generation
- **`metaobjects-codegen-plantuml`** - PlantUML diagram generation
- **`metaobjects-maven-plugin`** - Maven integration for build-time code generation

### **Framework Integration**
- **`metaobjects-core-spring`** - Spring Framework integration and auto-configuration

### **Project Tools**
- **`archetype`** - Maven archetype for creating MetaObjects-based projects
- **`examples`** - Complete working examples demonstrating all usage patterns

### **Available Separately**
The following modules have been moved to separate projects for focused development:
- Object Management (OM, OMDB, OMNOSQL) - Database and NoSQL persistence
- Web Components - React TypeScript components and web utilities
- Demo Applications - Full-featured demo applications

## 📦 **Quick Start**

### **Basic Usage (Framework-Independent)**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core</artifactId>
    <version>6.3.1-SNAPSHOT</version>
</dependency>
```

### **Spring Integration**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core-spring</artifactId>
    <version>6.3.1-SNAPSHOT</version>
</dependency>
```

### **Fluent Constraint System**
```xml
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-metadata</artifactId>
    <version>6.3.1-SNAPSHOT</version>
</dependency>
```

### **Code Generation**
```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>6.3.1-SNAPSHOT</version>
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
- **Framework Integration** - Native support for Spring, OSGi frameworks
- **JSON/XML Metadata** - Flexible metadata definition formats with inline attribute support
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
# Basic MetaObjects functionality - now with simplified execution!
cd examples/basic-example && mvn exec:java

# Spring integration - multiple options available
cd examples/spring-example && mvn spring-boot:run
# OR: cd examples/spring-example && mvn exec:java

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
- Java 21 LTS (Production Ready)
- Maven 3.9+

### **Full Build**
```bash
mvn clean compile    # Compile all modules
mvn test            # Run full test suite
mvn package         # Package all modules
```

### **Module Dependencies**
Build order: `metadata → codegen-* → maven-plugin → core → core-spring → archetype → examples`

## 🛡️ **2024-2025 Comprehensive Modernization**

**MetaObjects has undergone complete modernization across security, architecture, and infrastructure:**

### **🔒 Security Hardening**
- **100% Critical Vulnerabilities Eliminated**: All high and critical severity issues resolved
- **CVE-2015-7501 & CVE-2015-6420 FIXED**: Apache Commons Collections RCE vulnerabilities eliminated
- **Dependency Management**: Centralized security overrides (SnakeYAML 1.30 → 2.2, Gson → 2.13.2)
- **Modern Dependencies**: Spring 5.3.39, Jackson 2.18.1, Logback 1.5.19, secure versions throughout

### **🚀 Java 21 LTS Migration**
- **Modern Platform**: Fully migrated to Java 21 LTS for enhanced performance and security
- **Jakarta EE**: Updated servlet imports (javax.servlet → jakarta.servlet) for Spring 6 compatibility
- **Build Optimization**: Maven caching providing 60%+ build time improvement
- **Cross-Platform**: Temurin JDK for consistent cross-platform builds

### **🧹 Code Quality Modernization**
- **400+ Lines Eliminated**: Deprecated/vulnerable code and legacy log4j configurations removed
- **Modern APIs**: Zero @Deprecated annotations, Optional-based patterns throughout
- **20+ Obsolete Files Removed**: Duplicate/legacy code cleanup across modules
- **Enhanced Build System**: Updated Maven plugins (Surefire 3.5.2, Clean 3.4.0, Deploy 3.1.3)
- **Consolidated Logging**: Standardized Logback configuration eliminating conflicts
- **Developer Experience**: Added exec-maven-plugin for simplified example execution
- **Professional Build Output**: Comprehensive logging cleanup with meaningful coverage thresholds

### **⚙️ CI/CD Infrastructure**
- **GitHub Actions**: Latest secure actions (checkout@v4, setup-java@v4, cache@v4)
- **Build Reliability**: 100% test success rate across 1,247+ tests
- **OSGi Compatibility**: Full bundle lifecycle management preserved
- **Architecture Compliance**: Read-optimized performance patterns maintained

### **📊 Quality Metrics**
- ✅ **All 9 core modules**: Clean compilation and packaging
- ✅ **Security posture**: Zero critical vulnerabilities (CVE-2015-7501 & CVE-2015-6420 eliminated)
- ✅ **Updated dependencies**: Jackson 2.18.1, Logback 1.5.19, Commons Validator 1.10.0
- ✅ **Test coverage**: Comprehensive test suite passing across all modules
- ✅ **Build performance**: Optimized Maven build configuration
- ✅ **Working examples**: 3 complete example projects (basic, spring, osgi)
- ✅ **Fluent constraint system**: Advanced constraint definitions with AttributeConstraintBuilder
- ✅ **Universal @isArray**: Eliminates array subtype explosion while supporting all combinations
- ✅ **OSGi compatibility**: Full bundle lifecycle support with WeakReference cleanup patterns

**This represents a comprehensive modernization suitable for enterprise production environments while maintaining the sophisticated architectural patterns that make MetaObjects unique.**

## 📋 **Migration from v5.1.x**

The v5.2.0+ modular architecture maintains full backward compatibility while providing cleaner dependency management:

1. **Replace single dependency** with appropriate modular dependencies
2. **Update imports** if using internal APIs (rare)
3. **Spring users**: Switch to `metaobjects-core-spring` for optimal integration
4. **Code generation**: Use modular codegen artifacts for specific template engines

See [Migration Guide](MIGRATION.md) for detailed instructions.

## 🚀 **Release Notes**
Current Development: **6.3.0-SNAPSHOT** (Fluent Constraint System + Universal @isArray)
Latest Stable Release: **6.2.5** (Maven Central Publishing Ready)

**Major v6.3.1 Features:**
- 🚀 **Revolutionary Fluent Constraint System** with AttributeConstraintBuilder API
- 🎲 **Universal @isArray Modifier** eliminating array subtype explosion
- 🔧 **Enhanced ConstraintEnforcer** with attribute-specific validation
- 📊 **Comprehensive Constraints** with advanced validation patterns
- 🏢 **Provider-Based Registration** with clean service discovery
- 🔒 **Security Updates** - All critical vulnerabilities resolved
- 🏢 **Modular Architecture** - 9 focused, independent modules
- 📚 **Working Examples** - Complete demonstration projects

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