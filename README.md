# MetaObjects

MetaObjects is a comprehensive suite of tools for **metadata-driven development**, providing sophisticated control over applications beyond traditional model-driven development techniques. Version 5.2.0+ features a **completely modular architecture** designed for modern software development practices.

## 🚀 **New Modular Architecture (v5.2.0+)**

MetaObjects has been completely refactored into focused, independent modules that can be used individually or combined as needed:

### **Core Modules**
- **`metaobjects-metadata`** - Core metadata definitions and constraint system
- **`metaobjects-core`** - File-based metadata loading and core functionality

### **Code Generation**
- **`metaobjects-codegen-base`** - Base code generation framework
- **`metaobjects-codegen-mustache`** - Mustache template-based code generation
- **`metaobjects-codegen-plantuml`** - PlantUML diagram generation
- **`metaobjects-maven-plugin`** - Maven integration for build-time code generation

### **Framework Integration**
- **`metaobjects-core-spring`** - Spring Framework integration
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

## 📦 **Quick Start**

### **Basic Usage (Framework-Independent)**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-core</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

### **Spring Integration**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-core-spring</artifactId>
    <version>5.2.0-SNAPSHOT</version>
</dependency>
```

### **Code Generation**
```xml
<plugin>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>5.2.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>
```

## 🎯 **Key Features**

- **Metadata-Driven Development** - Define object structures, validation, and relationships through metadata
- **Cross-Language Code Generation** - Generate Java, C#, TypeScript from metadata definitions
- **Framework Integration** - Native support for Spring, OSGi, and web frameworks
- **Type-Safe Constraints** - Comprehensive validation and constraint system
- **JSON/XML Metadata** - Flexible metadata definition formats with inline attribute support
- **React MetaView System** - TypeScript components for metadata-driven UIs
- **Database Integration** - Direct database mapping and persistence
- **OSGi Compatible** - Full bundle lifecycle support with WeakReference cleanup patterns

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
- Java 21+
- Maven 3.9+

### **Full Build**
```bash
mvn clean compile    # Compile all modules
mvn test            # Run full test suite
mvn package         # Package all modules
```

### **Module Dependencies**
Build order: `metadata → codegen-* → core → *-spring → om → web → demo → examples`

## 📋 **Migration from v5.1.x**

The v5.2.0+ modular architecture maintains full backward compatibility while providing cleaner dependency management:

1. **Replace single dependency** with appropriate modular dependencies
2. **Update imports** if using internal APIs (rare)
3. **Spring users**: Switch to `metaobjects-core-spring` for optimal integration
4. **Code generation**: Use modular codegen artifacts for specific template engines

See [Migration Guide](MIGRATION.md) for detailed instructions.

## 🚀 **Release Notes**
Current Development: **5.2.0-SNAPSHOT** (Modular Architecture)  
Latest Stable Release: **5.1.0**

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