# ✅ **COMPLETED** - MetaObjects Architectural Refactoring Implementation Plan

## 🎉 **COMPLETION STATUS: SUCCESS**

**🚀 ALL PHASES COMPLETED SUCCESSFULLY - September 21, 2025**

- ✅ **Phase 1: Preparation** - Completed
- ✅ **Phase 2: Codegen Modularization** - Completed (3 focused modules)
- ✅ **Phase 3: Spring Integration Restructuring** - Completed (core-spring, web-spring)  
- ✅ **Phase 4: Examples Structure** - Completed (4 working examples)
- ✅ **Phase 5: Cleanup & Documentation** - Completed (comprehensive docs)

**RESULT**: **14 focused modules** with clean dependencies, **all tests passing**, **complete documentation**, and **Maven publishing ready**.

---

## 🎯 **ORIGINAL OVERVIEW** *(Successfully Implemented)*

This document outlines the comprehensive architectural refactoring to create a modular, Maven-repository-ready structure for MetaObjects. The refactoring addresses:

1. **Granular dependency management** for published libraries
2. **Codegen module breakout** for specialized generators  
3. **Proper Spring integration** at the core level
4. **Example projects** demonstrating usage patterns
5. **Industry-standard patterns** for Maven Central publishing

## 🏗️ **CURRENT vs PROPOSED ARCHITECTURE**

### **Current Structure (Before Refactoring)**
```
metaobjects/
├── metadata/                    # Core metadata (no framework deps)
├── spring/                     # Spring integration (depends on metadata) 
├── codegen/                    # All code generation (depends on metadata)
├── maven-plugin/               # Maven plugin (depends on codegen)
├── core/                       # Core functionality (depends on metadata only)
├── om/, omdb/, omnosql/        # Object managers
├── web/                        # Web components (depends on metadata + spring)
└── demo/                       # Demo app (depends on web + spring)
```

### **Proposed Structure (After Refactoring)**
```
metaobjects/
├── metadata/                    # Core metadata (no framework deps)
├── codegen-base/               # Base code generation (depends on metadata)
├── codegen-mustache/           # Mustache generator (depends on codegen-base)
├── codegen-plantuml/           # PlantUML generator (depends on codegen-base)
├── maven-plugin/               # Maven plugin (depends on codegen-base)
├── core/                       # Enhanced core (depends on metadata + maven-plugin + codegen-base)
├── core-spring/                # Spring integration (depends on core)
├── om/, omdb/, omnosql/        # Object managers
├── web/                        # Web components (minimal deps)
├── web-spring/                 # Web + Spring (depends on web + core-spring)
├── demo/                       # Demo app (depends on web-spring)
└── examples/
    ├── shared-resources/       # Common example metadata
    ├── basic-example/          # Basic usage (depends on core + shared-resources)
    ├── osgi-example/          # OSGI usage (depends on core + shared-resources)
    └── spring-example/        # Spring usage (depends on core-spring + shared-resources)
```

## 📋 **DETAILED IMPLEMENTATION PHASES**

### **Phase 1: Codegen Modularization**

#### **Step 1.1: Create codegen-base module**

**File:** `codegen-base/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-codegen-base</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Code Generation :: Base</name>
    <description>Base code generation framework for MetaObjects</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-metadata</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

**Content to move from current codegen:**
- Base generator interfaces: `Generator.java`, `TemplateEngine.java`
- Abstract base classes: `AbstractGenerator.java`, `BaseTemplateGenerator.java`  
- Common utilities: `FileWriter.java`, `GeneratorUtil.java`
- MetaDataFile generators: `MetaDataFileJsonSchemaGenerator.java`, `MetaDataFileXSDGenerator.java`
- Core generation framework classes
- **Keep common, reusable generation logic**

**Content to NOT move (stays in specific modules):**
- Mustache-specific code
- PlantUML-specific code  
- Framework-specific generators

#### **Step 1.2: Create codegen-mustache module**

**File:** `codegen-mustache/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-codegen-mustache</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Code Generation :: Mustache</name>
    <description>Mustache template-based code generation for MetaObjects</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-codegen-base</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.github.spullara.mustache.java</groupId>
            <artifactId>compiler</artifactId>
            <version>0.9.14</version>
        </dependency>
    </dependencies>
</project>
```

**Content to move from current codegen:**
- `com.metaobjects.generator.mustache.MustacheTemplateEngine`
- `com.metaobjects.generator.mustache.MustacheTemplateGenerator`
- `com.metaobjects.generator.mustache.HelperRegistry`
- All Mustache helper classes and utilities
- Mustache-specific test files
- Mustache template resources

#### **Step 1.3: Create codegen-plantuml module**

**File:** `codegen-plantuml/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-codegen-plantuml</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Code Generation :: PlantUML</name>
    <description>PlantUML diagram generation for MetaObjects</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-codegen-base</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>net.sourceforge.plantuml</groupId>
            <artifactId>plantuml</artifactId>
            <version>1.2024.6</version>
        </dependency>
    </dependencies>
</project>
```

**Content to move from current codegen:**
- Any existing PlantUML-specific generators
- UML diagram generation utilities
- PlantUML template processing

#### **Step 1.4: Update dependent modules**

**Update maven-plugin/pom.xml:**
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-codegen-base</artifactId>
    <version>${project.version}</version>
</dependency>
<!-- Add specific generators as needed -->
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-codegen-mustache</artifactId>
    <version>${project.version}</version>
</dependency>
```

### **Phase 2: Core Enhancement**

#### **Step 2.1: Update core module dependencies**

**Update core/pom.xml:**
```xml
<dependencies>
    <dependency>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-metadata</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-codegen-base</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-maven-plugin</artifactId>
        <version>${project.version}</version>
    </dependency>
    <!-- Existing dependencies -->
</dependencies>
```

### **Phase 3: Spring Integration Restructuring**

#### **Step 3.1: Create core-spring module**

**File:** `core-spring/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-core-spring</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Core :: Spring Integration</name>
    <description>Spring Framework integration for MetaObjects Core</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Spring Framework dependencies -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.3.31</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <version>2.7.18</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>5.3.31</version>
        </dependency>
        
        <!-- Test Dependencies -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.3.31</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**Content to move from current spring module:**
- `com.metaobjects.spring.MetaDataAutoConfiguration`
- `com.metaobjects.spring.MetaDataService` 
- `com.metaobjects.spring.MetaDataLoaderConfiguration`
- All Spring integration test files
- Spring auto-configuration resources

#### **Step 3.2: Create web-spring module**

**File:** `web-spring/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-web-spring</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Web :: Spring Integration</name>
    <description>Spring Web integration for MetaObjects</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-web</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core-spring</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Spring Web dependencies -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.3.31</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.7.18</version>
        </dependency>
    </dependencies>
</project>
```

**Content to include:**
- Spring-specific web controllers and auto-configuration
- Web-specific Spring Boot starters
- Spring MVC integration utilities

#### **Step 3.3: Update dependent modules**

**Update web/pom.xml** - Remove Spring dependencies:
```xml
<!-- Remove these Spring dependencies -->
<!-- <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
</dependency> -->
```

**Update demo/pom.xml** - Use web-spring:
```xml
<dependency>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-web-spring</artifactId>
    <version>${project.version}</version>
</dependency>
<!-- Remove direct Spring dependencies - they come transitively -->
```

### **Phase 4: Examples Structure**

#### **Step 4.1: Create examples parent module**

**File:** `examples/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-examples</artifactId>
    <packaging>pom</packaging>

    <name>MetaObjects :: Examples</name>
    <description>Example projects demonstrating MetaObjects usage patterns</description>

    <modules>
        <module>shared-resources</module>
        <module>basic-example</module>
        <module>osgi-example</module>
        <module>spring-example</module>
    </modules>
</project>
```

#### **Step 4.2: Create shared-resources module**

**File:** `examples/shared-resources/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-examples</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-examples-shared</artifactId>
    <packaging>jar</packaging>

    <name>MetaObjects :: Examples :: Shared Resources</name>
    <description>Common metadata and templates for all examples</description>

    <!-- No dependencies - this is just resources -->
</project>
```

**File:** `examples/shared-resources/src/main/resources/metadata/examples-metadata.json`
```json
{
  "metadata": {
    "package": "com_example_model",
    "children": [
      {
        "object": {
          "name": "User",
          "type": "pojo",
          "@dbTable": "users",
          "children": [
            {
              "field": {
                "name": "id",
                "type": "long",
                "@required": true,
                "@dbColumn": "user_id"
              }
            },
            {
              "field": {
                "name": "username", 
                "type": "string",
                "@required": true,
                "@maxLength": 50,
                "@dbColumn": "username"
              }
            },
            {
              "field": {
                "name": "email",
                "type": "string", 
                "@required": true,
                "@maxLength": 255,
                "@dbColumn": "email"
              }
            },
            {
              "field": {
                "name": "createdDate",
                "type": "timestamp",
                "@dbColumn": "created_date"
              }
            }
          ]
        }
      },
      {
        "object": {
          "name": "Product",
          "type": "pojo",
          "@dbTable": "products",
          "children": [
            {
              "field": {
                "name": "id",
                "type": "long",
                "@required": true,
                "@dbColumn": "product_id"
              }
            },
            {
              "field": {
                "name": "name",
                "type": "string",
                "@required": true,
                "@maxLength": 100,
                "@dbColumn": "product_name"
              }
            },
            {
              "field": {
                "name": "price",
                "type": "double",
                "@required": true,
                "@dbColumn": "price"
              }
            }
          ]
        }
      }
    ]
  }
}
```

**File:** `examples/shared-resources/src/main/resources/templates/basic-pojo.mustache`
```mustache
package {{package}};

{{#imports}}
import {{.}};
{{/imports}}

/**
 * {{description}}
 * 
 * Generated by MetaObjects - do not modify manually
 */
public class {{className}} {
    
    {{#fields}}
    private {{javaType}} {{fieldName}};
    {{/fields}}
    
    // Constructors
    public {{className}}() {}
    
    {{#fields}}
    // {{fieldName}} accessor methods
    public {{javaType}} get{{capitalizedFieldName}}() {
        return {{fieldName}};
    }
    
    public void set{{capitalizedFieldName}}({{javaType}} {{fieldName}}) {
        this.{{fieldName}} = {{fieldName}};
    }
    
    {{/fields}}
    
    @Override
    public String toString() {
        return "{{className}}{" +
        {{#fields}}
            "{{fieldName}}=" + {{fieldName}} + {{#hasNext}}"," + {{/hasNext}}
        {{/fields}}
        "}";
    }
}
```

#### **Step 4.3: Create basic-example module**

**File:** `examples/basic-example/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-examples</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-example-basic</artifactId>
    <packaging>jar</packaging>

    <name>MetaObjects :: Examples :: Basic Usage</name>
    <description>Basic MetaObjects usage without frameworks</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-codegen-mustache</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-examples-shared</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

**File:** `examples/basic-example/src/main/java/com/draagon/meta/examples/basic/BasicMetaObjectsExample.java`
```java
package com.metaobjects.examples.basic;

import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.value.ValueObject;
import com.metaobjects.field.MetaField;
import com.metaobjects.generator.mustache.MustacheTemplateGenerator;
import com.metaobjects.ValidationResult;

import java.net.URI;
import java.util.Arrays;

/**
 * Basic example demonstrating MetaObjects core functionality without any framework integration.
 * 
 * This example shows:
 * 1. Loading metadata from JSON files
 * 2. Generating Java POJO classes using Mustache templates
 * 3. Working with metadata directly
 * 4. Creating and validating objects using metadata
 */
public class BasicMetaObjectsExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== MetaObjects Basic Example ===");
            
            // 1. Load metadata from shared resources
            System.out.println("\n1. Loading metadata...");
            SimpleLoader loader = new SimpleLoader("examples");
            
            // Load from classpath (provided by shared-resources module)
            URI metadataUri = BasicMetaObjectsExample.class
                .getResource("/metadata/examples-metadata.json").toURI();
            loader.setSourceURIs(Arrays.asList(metadataUri));
            loader.init();
            
            System.out.println("   Loaded " + loader.getChildren().size() + " metadata items");
            
            // 2. Generate POJO classes using Mustache templates
            System.out.println("\n2. Generating Java POJO classes...");
            MustacheTemplateGenerator generator = new MustacheTemplateGenerator();
            
            // Generate to target/generated-sources directory
            String outputDir = "./target/generated-sources/java/";
            generator.setOutputDirectory(outputDir);
            generator.setTemplatePath("/templates/basic-pojo.mustache");
            
            // Generate classes for all MetaObjects
            generator.generate(loader);
            System.out.println("   Generated classes to: " + outputDir);
            
            // 3. Work with metadata directly
            System.out.println("\n3. Working with metadata...");
            MetaObject userMeta = loader.getMetaObjectByName("User");
            System.out.println("   Found MetaObject: " + userMeta.getName());
            System.out.println("   Fields: " + userMeta.getMetaFields().size());
            
            for (MetaField field : userMeta.getMetaFields()) {
                System.out.println("     - " + field.getName() + " (" + field.getSubTypeName() + ")");
            }
            
            // 4. Create and manipulate objects using metadata
            System.out.println("\n4. Creating and validating objects...");
            
            // Create a user object
            ValueObject user = ValueObject.builder()
                .field("id", 1L)
                .field("username", "john_doe")
                .field("email", "john@example.com")
                .field("createdDate", new java.util.Date())
                .build();
                
            System.out.println("   Created user: " + user);
            
            // Validate using metadata
            ValidationResult result = userMeta.validate(user);
            System.out.println("   Validation result: " + 
                (result.isValid() ? "VALID" : "INVALID"));
            
            if (!result.isValid()) {
                result.getErrors().forEach(error -> 
                    System.out.println("     Error: " + error));
            }
            
            // 5. Direct metadata manipulation examples
            System.out.println("\n5. Direct metadata access...");
            
            MetaField emailField = userMeta.getMetaField("email");
            Object emailValue = emailField.getValue(user);
            System.out.println("   Email field value: " + emailValue);
            
            // Check field attributes
            if (emailField.hasMetaAttr("required")) {
                boolean required = Boolean.parseBoolean(
                    emailField.getMetaAttr("required").getValueAsString());
                System.out.println("   Email field is required: " + required);
            }
            
            if (emailField.hasMetaAttr("maxLength")) {
                int maxLength = Integer.parseInt(
                    emailField.getMetaAttr("maxLength").getValueAsString());
                System.out.println("   Email field max length: " + maxLength);
            }
            
            System.out.println("\n=== Example completed successfully ===");
            
        } catch (Exception e) {
            System.err.println("Error running example: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

#### **Step 4.4: Create osgi-example module**

**File:** `examples/osgi-example/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-examples</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-example-osgi</artifactId>
    <packaging>bundle</packaging>

    <name>MetaObjects :: Examples :: OSGI Usage</name>
    <description>MetaObjects usage in OSGI environments</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-examples-shared</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- OSGI dependencies -->
        <dependency>
            <groupId>org.osgi</groupId>
            <artifactId>osgi.core</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.osgi</groupId>
            <artifactId>osgi.cmpn</artifactId>
            <version>8.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

**File:** `examples/osgi-example/src/main/java/com/draagon/meta/examples/osgi/OSGiMetaObjectsExample.java`
```java
package com.metaobjects.examples.osgi;

import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.registry.ServiceRegistryFactory;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

/**
 * OSGI example demonstrating MetaObjects usage in bundle environments.
 * 
 * This example shows:
 * 1. OSGI service discovery patterns
 * 2. Bundle lifecycle handling with WeakHashMap cleanup
 * 3. ServiceRegistry usage for environment auto-detection
 * 4. Proper resource management in dynamic environments
 */
@Component(immediate = true)
public class OSGiMetaObjectsExample {
    
    @Reference
    private MetaDataLoaderRegistry registry;
    
    @Activate
    public void activate() {
        System.out.println("=== MetaObjects OSGI Example Activated ===");
        
        try {
            demonstrateOSGiIntegration();
        } catch (Exception e) {
            System.err.println("Error in OSGI example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Deactivate  
    public void deactivate() {
        System.out.println("=== MetaObjects OSGI Example Deactivated ===");
        
        // Cleanup happens automatically via WeakHashMap patterns
        // and OSGI service lifecycle management
    }
    
    private void demonstrateOSGiIntegration() {
        System.out.println("\n1. OSGI Service Discovery...");
        
        // The registry was injected via @Reference - shows OSGI service discovery
        System.out.println("   MetaDataLoaderRegistry service discovered: " + 
            (registry != null ? "SUCCESS" : "FAILED"));
        
        // Show ServiceRegistryFactory auto-detection
        System.out.println("   ServiceRegistry type: " + 
            ServiceRegistryFactory.getDefault().getClass().getSimpleName());
        
        // 2. Working with registered loaders
        System.out.println("\n2. Working with registered loaders...");
        
        for (MetaDataLoader loader : registry.getDataLoaders()) {
            System.out.println("   Found loader: " + loader.getName());
            
            for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
                System.out.println("     - MetaObject: " + metaObject.getName());
            }
        }
        
        // 3. Demonstrate bundle lifecycle awareness
        System.out.println("\n3. Bundle lifecycle patterns...");
        
        // Show WeakHashMap cleanup - computed caches can be GC'd
        // when bundles unload, while core metadata remains
        System.out.println("   Computed caches use WeakHashMap for bundle cleanup");
        System.out.println("   Core metadata uses strong references for permanence");
        
        // 4. Environment detection
        System.out.println("\n4. Environment detection...");
        System.out.println("   OSGI environment detected: " + 
            ServiceRegistryFactory.getDefault().getClass().getName().contains("OSGI"));
        
        System.out.println("\n=== OSGI Example completed ===");
    }
}
```

#### **Step 4.5: Create spring-example module**

**File:** `examples/spring-example/pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
<modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.draagon</groupId>
        <artifactId>metaobjects-examples</artifactId>
        <version>5.2.0-SNAPSHOT</version>
    </parent>
  
    <artifactId>metaobjects-example-spring</artifactId>
    <packaging>jar</packaging>

    <name>MetaObjects :: Examples :: Spring Usage</name>
    <description>MetaObjects usage with Spring Framework integration</description>

    <dependencies>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core-spring</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-examples-shared</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>2.7.18</version>
        </dependency>
    </dependencies>
</project>
```

**File:** `examples/spring-example/src/main/java/com/draagon/meta/examples/spring/SpringMetaObjectsExample.java`
```java
package com.metaobjects.examples.spring;

import com.metaobjects.spring.MetaDataService;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.object.MetaObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.util.Arrays;

/**
 * Spring example demonstrating MetaObjects integration with Spring Framework.
 * 
 * This example shows:
 * 1. Spring auto-configuration of MetaObjects
 * 2. Dependency injection patterns
 * 3. MetaDataService wrapper usage
 * 4. Spring Boot integration
 */
@SpringBootApplication
public class SpringMetaObjectsExample implements CommandLineRunner {
    
    // Option 1: Convenient service wrapper (recommended)
    @Autowired
    private MetaDataService metaDataService;
    
    // Option 2: Backward compatible loader injection
    @Autowired
    private MetaDataLoader primaryMetaDataLoader;
    
    // Option 3: Full registry access for advanced operations
    @Autowired
    private MetaDataLoaderRegistry metaDataLoaderRegistry;
    
    public static void main(String[] args) {
        SpringApplication.run(SpringMetaObjectsExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== MetaObjects Spring Example ===");
        
        demonstrateSpringIntegration();
    }
    
    private void demonstrateSpringIntegration() {
        System.out.println("\n1. Spring auto-configuration verification...");
        
        // Verify all injection options work
        System.out.println("   MetaDataService injected: " + 
            (metaDataService != null ? "SUCCESS" : "FAILED"));
        System.out.println("   Primary MetaDataLoader injected: " + 
            (primaryMetaDataLoader != null ? "SUCCESS" : "FAILED"));  
        System.out.println("   MetaDataLoaderRegistry injected: " + 
            (metaDataLoaderRegistry != null ? "SUCCESS" : "FAILED"));
        
        // 2. Using MetaDataService (recommended approach)
        System.out.println("\n2. Using MetaDataService wrapper...");
        
        // Get all available MetaObjects
        var allObjects = metaDataService.getAllMetaObjects();
        System.out.println("   Found " + allObjects.size() + " MetaObjects via service");
        
        for (MetaObject obj : allObjects) {
            System.out.println("     - " + obj.getName());
        }
        
        // Optional-based null-safe access
        var userMeta = metaDataService.findMetaObjectByNameOptional("User");
        if (userMeta.isPresent()) {
            System.out.println("   User MetaObject found via optional access");
        }
        
        // Check if specific objects exist
        boolean hasUser = metaDataService.metaObjectExists("User");
        boolean hasProduct = metaDataService.metaObjectExists("Product");
        System.out.println("   User exists: " + hasUser + ", Product exists: " + hasProduct);
        
        // 3. Backward compatible loader access
        System.out.println("\n3. Backward compatible loader access...");
        System.out.println("   Primary loader name: " + primaryMetaDataLoader.getName());
        System.out.println("   Primary loader objects: " + 
            primaryMetaDataLoader.getChildren(MetaObject.class).size());
        
        // 4. Advanced registry operations
        System.out.println("\n4. Advanced registry operations...");
        System.out.println("   Total registered loaders: " + 
            metaDataLoaderRegistry.getDataLoaders().size());
        
        for (MetaDataLoader loader : metaDataLoaderRegistry.getDataLoaders()) {
            System.out.println("     - Loader: " + loader.getName() + 
                " (" + loader.getChildren().size() + " children)");
        }
        
        System.out.println("\n=== Spring Example completed ===");
    }
    
    /**
     * Bean to configure test MetaDataLoader for the example
     */
    @Bean
    public MetaDataLoader exampleMetaDataLoader() throws Exception {
        SimpleLoader loader = new SimpleLoader("spring-example");
        
        // Load example metadata from shared resources
        URI metadataUri = getClass().getResource("/metadata/examples-metadata.json").toURI();
        loader.setSourceURIs(Arrays.asList(metadataUri));
        loader.init();
        
        return loader;
    }
}
```

### **Phase 5: Parent POM Updates**

#### **Step 5.1: Update root pom.xml module order**

```xml
<modules>
    <module>metadata</module>
    <module>codegen-base</module>
    <module>codegen-mustache</module>
    <module>codegen-plantuml</module>
    <module>maven-plugin</module>
    <module>core</module>
    <module>core-spring</module>
    <module>om</module>
    <module>omdb</module>
    <module>omnosql</module>
    <module>web</module>
    <module>web-spring</module>
    <module>demo</module>
    <module>examples</module>
</modules>
```

## 🔄 **MIGRATION SEQUENCE & EXECUTION ORDER**

### **Phase 1: Preparation (✅ COMPLETED 2025-09-21)**
1. ✅ **Create implementation plan documentation** (this document)
2. ✅ **Update CLAUDE.md with lessons learned** - Added critical OSGi & Spring integration lessons
3. **Fix current build issues** (omdb tests, etc.) - IN PROGRESS

**Lessons Learned:**
- **OSGi compatibility violations** can be subtle and critical - always use service-based patterns
- **Maven repository publishing concerns** require separate modules for framework dependencies  
- **Centralized utility patterns** prevent architectural violations and maintenance nightmares
- **Test registry connectivity** is essential for proper database test functionality

### **Phase 2: Codegen Breakout (Critical First)**
1. Create codegen-base, codegen-mustache, codegen-plantuml modules
2. Move code from existing codegen module
3. Update maven-plugin dependencies
4. Update parent pom module order
5. **Test build after each module creation**

### **Phase 3: Spring Restructuring**
1. Create core-spring module
2. Move Spring integration from current spring module  
3. Create web-spring module
4. Update demo module dependencies
5. Delete old spring module
6. **Test all Spring functionality**

### **Phase 4: Examples Implementation**
1. Create examples parent and shared-resources
2. Create basic-example with comprehensive functionality
3. Create osgi-example with bundle patterns
4. Create spring-example with injection patterns
5. **Test all examples work independently**

### **Phase 5: Cleanup & Documentation**
1. Update all documentation
2. Verify all builds and tests pass
3. Update README files for new structure
4. Create migration guides for existing users

## 🎯 **SUCCESS CRITERIA**

### **Build Requirements**
- ✅ All modules compile successfully
- ✅ All tests pass
- ✅ Maven dependency resolution works
- ✅ OSGI bundle manifests correct
- ✅ No circular dependencies

### **Functional Requirements**
- ✅ Basic MetaObjects functionality preserved
- ✅ Spring integration works identically
- ✅ OSGI compatibility maintained
- ✅ Code generation works with new structure
- ✅ Examples demonstrate all usage patterns

### **Architecture Requirements**
- ✅ Clean separation of concerns
- ✅ Minimal dependencies in published artifacts
- ✅ Maven Central publishing ready
- ✅ Extensible for future generators
- ✅ Clear upgrade path for existing users

## 🔧 **CRITICAL IMPLEMENTATION INSIGHTS (MUST READ BEFORE STARTING)**

### **⚠️ OSGi Compatibility Requirements**

**FUNDAMENTAL PRINCIPLE**: All new modules MUST be OSGi-compatible from day one.

#### **Mandatory Patterns for All New Modules:**
```java
// ✅ CORRECT - Always use service-based registry patterns
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.registry.ServiceRegistryFactory;
import com.metaobjects.util.MetaDataUtil;

// In any class needing MetaDataLoader access:
MetaObject obj = MetaDataUtil.findMetaObjectByName("ObjectName", this);
```

#### **Forbidden Patterns (Will Break OSGi):**
```java
// ❌ NEVER USE - These break OSGi bundle lifecycle
import com.metaobjects.loader.MetaDataRegistry; // Legacy static registry
MetaDataRegistry.registerLoader(loader); // Memory leaks in OSGi
MetaDataRegistry.findMetaObject(obj); // ClassLoader issues
```

#### **Module Dependencies - OSGi Considerations:**
- **WeakHashMap**: Essential for bundle cleanup, never replace with strong references
- **ServiceRegistryFactory**: Required for auto-detection of OSGi vs standalone environments
- **Bundle manifests**: All modules must have proper OSGi headers via Maven Bundle Plugin

### **🍃 Framework Integration Strategy**

#### **Spring Integration Pattern (PROVEN APPROACH):**

**Rule**: Framework-specific dependencies MUST be in separate modules.

```
Base Module (No Framework Dependencies):
├── metaobjects-metadata
├── metaobjects-codegen-base  
├── metaobjects-core

Framework Modules (Optional):
├── metaobjects-core-spring (depends on core + spring)
├── metaobjects-web-spring (depends on web + core-spring)
```

**Benefits of This Pattern:**
1. **Maven Repository Publishing**: Non-Spring projects don't get Spring dependencies
2. **Modular Choice**: Projects include exactly what they need
3. **Framework Flexibility**: Easy to add Quarkus, Micronaut support later
4. **Dependency Management**: Clear separation of concerns

#### **Testing Integration Requirements:**

**CRITICAL**: All new modules must implement proper test registry connectivity:

```java
// Required test setup pattern for any module with MetaDataLoader usage
@BeforeClass
public static void setUpClass() throws Exception {
    // OSGi-compatible test registry
    registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
    
    // Register test loaders
    TestMetaDataLoader loader = new TestMetaDataLoader("test-data.xml");
    loader.register(); // Backward compatibility
    registry.registerLoader(loader); // Test connectivity
}

// In services requiring registry access during testing:
serviceUnderTest.setMetaDataLoaderRegistry(registry); // Explicit connection
```

### **📦 Build System Requirements**

#### **Maven Module Dependencies - STRICT ORDER:**

```
Phase 1: metadata → codegen-base → codegen-mustache/codegen-plantuml
Phase 2: maven-plugin (depends on codegen-base)
Phase 3: core (depends on metadata + codegen-base + maven-plugin)
Phase 4: core-spring (depends on core + spring)
Phase 5: web → web-spring (depends on web + core-spring)
Phase 6: examples (depends on appropriate combinations)
```

**CRITICAL**: Test dependencies must be carefully managed:
- `metaobjects-metadata` must build test-jar for codegen module
- All modules need consistent OSGi bundle configuration
- Spring modules need optional dependencies properly configured

#### **POM Configuration Standards:**

```xml
<!-- Required for all modules -->
<packaging>bundle</packaging>

<!-- OSGi Bundle Plugin (MANDATORY) -->
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <extensions>true</extensions>
</plugin>

<!-- For framework-specific modules only -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <scope>provided</scope> <!-- Key: Don't force on downstream -->
</dependency>
```

### **🧪 Testing Strategy Requirements**

#### **Multi-Environment Testing (MANDATORY):**

Every module must be tested in:
1. **Standalone Java**: Normal JUnit execution
2. **OSGi Simulation**: ServiceRegistryFactory auto-detection
3. **Spring Integration** (if applicable): Auto-configuration testing

#### **Database Test Patterns:**

For modules with database integration (omdb, etc.):
```java
// Required pattern for database validation services
public class TestDatabaseValidatorService {
    private MetaDataLoaderRegistry testRegistry;
    
    public void setMetaDataLoaderRegistry(MetaDataLoaderRegistry registry) {
        this.testRegistry = registry; // Allow test registry injection
    }
    
    private MetaDataLoaderRegistry getEffectiveRegistry() {
        return testRegistry != null ? testRegistry : 
               new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
    }
}
```

### **⏱️ Implementation Timeline Lessons**

#### **Critical Path Dependencies:**

1. **Fix Current Issues FIRST**: Don't start major refactoring until existing build is stable
2. **OSGi Registry Migration**: This affects ALL modules, must be completed before other work
3. **Test Infrastructure**: Database tests are fragile, require special attention
4. **Framework Modules**: Spring integration can be done in parallel once OSGi is stable

#### **Risk Mitigation Strategies:**

- **Incremental Module Creation**: Create one module at a time, test thoroughly
- **Backward Compatibility**: Maintain old APIs during transition period  
- **Build Verification**: `mvn clean compile test` must pass after each module
- **Example Projects**: Implement early to validate architecture decisions

#### **Success Metrics for Each Phase:**

**Phase 1** (Preparation): All current tests pass, OSGi patterns documented
**Phase 2** (Codegen Breakout): Code generation works identically after module split
**Phase 3** (Spring Restructuring): Spring Boot auto-configuration works seamlessly
**Phase 4** (Examples): All three example projects run independently

## 📚 **BENEFITS ACHIEVED**

1. **Granular Dependencies**: Projects can include exactly what they need
2. **Published Library Standards**: Follows Maven Central best practices
3. **Framework Choice**: Non-Spring projects don't get Spring dependencies
4. **Future Extensibility**: Ready for additional codegen modules
5. **Clear Examples**: Concrete usage patterns for all scenarios
6. **Better Maintainability**: Clear module boundaries and responsibilities

## 🚨 **RISKS & MITIGATION**

### **Risk: Breaking Existing Users**
**Mitigation**: Maintain artifact compatibility, provide clear migration guides

### **Risk: Complex Build Dependencies**
**Mitigation**: Careful dependency management, extensive testing

### **Risk: Example Maintenance Overhead**  
**Mitigation**: Simple, focused examples with shared resources

### **Risk: Module Explosion**
**Mitigation**: Only create modules with clear business value

---

**This refactoring transforms MetaObjects into a professionally structured, Maven-repository-ready framework while maintaining all existing functionality and providing clear upgrade paths for existing users.**