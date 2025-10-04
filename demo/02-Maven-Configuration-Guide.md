# MetaObjects Maven Configuration Guide

## Overview

This guide shows how to configure Maven projects for MetaObjects code generation. The key is properly setting up the MetaObjects Maven plugin to load metadata and generate code during the Maven build process.

## Project POM Configuration

### 1. Parent POM Setup

**Create a parent `pom.xml` for your MetaObjects project:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.yourcompany</groupId>
    <artifactId>your-project-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <name>Your Project :: Parent</name>
    <description>MetaObjects-powered application</description>
    
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- MetaObjects Version -->
        <metaobjects.version>6.2.6-SNAPSHOT</metaobjects.version>
        
        <!-- Plugin Versions -->
        <maven-compiler-plugin.version>3.13.0</maven-compiler-plugin.version>
        <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
    </properties>
    
    <modules>
        <module>domain</module>
        <module>service</module>
        <module>web</module>
    </modules>
    
    <dependencyManagement>
        <dependencies>
            <!-- MetaObjects Core -->
            <dependency>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-core</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>

            <!-- MetaObjects Metadata -->
            <dependency>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-metadata</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>

            <!-- MetaObjects Object Manager -->
            <dependency>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-om</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>

            <!-- MetaObjects Database Object Manager -->
            <dependency>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-omdb</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <pluginManagement>
            <plugins>
                <!-- MetaObjects Maven Plugin -->
                <plugin>
                    <groupId>com.metaobjects</groupId>
                    <artifactId>metaobjects-maven-plugin</artifactId>
                    <version>${metaobjects.version}</version>
                </plugin>
                
                <!-- Maven Compiler Plugin -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${maven-compiler-plugin.version}</version>
                    <configuration>
                        <release>${maven.compiler.release}</release>
                        <compilerArgs>
                            <arg>--enable-preview</arg>
                        </compilerArgs>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

</project>
```

### 2. Domain Module POM

**Create domain module with code generation (`domain/pom.xml`):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>your-project-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>your-project-domain</artifactId>
    <packaging>jar</packaging>
    
    <name>Your Project :: Domain</name>
    <description>Domain entities and data access layer</description>
    
    <dependencies>
        <!-- MetaObjects Dependencies -->
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-core</artifactId>
        </dependency>

        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-metadata</artifactId>
        </dependency>

        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-omdb</artifactId>
        </dependency>
        
        <!-- JPA/Hibernate for persistence -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.4.4.Final</version>
        </dependency>
        
        <!-- Spring Boot for dependency injection -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>3.2.5</version>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <version>3.2.5</version>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.13.1</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- MetaObjects Code Generation Plugin -->
            <plugin>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-maven-plugin</artifactId>
                <executions>
                    <!-- Generate Domain Objects -->
                    <execution>
                        <id>generate-domain</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <globals>
                                <verbose>false</verbose>
                                <strict>true</strict>
                                <outputDir>${project.build.directory}/generated-sources</outputDir>
                            </globals>
                            <loader>
                                <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
                                <name>domain-loader</name>
                                <sources>
                                    <!-- Load types configuration -->
                                    <source>metadata/types/your-project-types.xml</source>
                                    <!-- Load common fields -->
                                    <source>metadata/models/common.xml</source>
                                    <!-- Load domain entities -->
                                    <source>metadata/models/user.xml</source>
                                    <source>metadata/models/order.xml</source>
                                    <!-- Load database overlay -->
                                    <source>metadata/models/overlays/database.xml</source>
                                </sources>
                            </loader>
                            <generators>
                                <!-- Generate JPA Entities -->
                                <generator>
                                    <classname>com.yourcompany.generator.JPAEntityGenerator</classname>
                                    <args>
                                        <basePackage>com.yourcompany.domain.entity</basePackage>
                                        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                                    </args>
                                    <filters>
                                        <filter>yourproject::domain::*</filter>
                                    </filters>
                                </generator>
                                
                                <!-- Generate DTOs -->
                                <generator>
                                    <classname>com.yourcompany.generator.DTOGenerator</classname>
                                    <args>
                                        <basePackage>com.yourcompany.domain.dto</basePackage>
                                        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                                    </args>
                                    <filters>
                                        <filter>yourproject::domain::*</filter>
                                    </filters>
                                </generator>
                                
                                <!-- Generate Repository Interfaces -->
                                <generator>
                                    <classname>com.yourcompany.generator.RepositoryGenerator</classname>
                                    <args>
                                        <basePackage>com.yourcompany.domain.repository</basePackage>
                                        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                                    </args>
                                    <filters>
                                        <filter>yourproject::domain::*</filter>
                                    </filters>
                                </generator>
                            </generators>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- Add generated sources to build path -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <id>add-generated-source</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${project.build.directory}/generated-sources/java</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

## Custom Generators

### 1. JPA Entity Generator

**Create custom generator for JPA entities:**

```java
package com.yourcompany.generator;

import com.metaobjects.generator.GeneratorBase;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.field.MetaField;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class JPAEntityGenerator extends GeneratorBase {
    
    @Override
    public void execute(MetaDataLoader loader) {
        String basePackage = getArg("basePackage", "com.example.entity");
        String outputDir = getArg("outputDir", "target/generated-sources/java");
        
        // Create output directory
        File dir = new File(outputDir + "/" + basePackage.replace(".", "/"));
        dir.mkdirs();
        
        // Generate entity for each MetaObject
        Collection<MetaObject> objects = loader.getMetaObjects();
        for (MetaObject obj : objects) {
            if (shouldProcess(obj)) {
                generateJPAEntity(obj, basePackage, dir);
            }
        }
    }
    
    private void generateJPAEntity(MetaObject obj, String basePackage, File outputDir) {
        try {
            String className = obj.getName();
            File entityFile = new File(outputDir, className + ".java");
            
            try (FileWriter writer = new FileWriter(entityFile)) {
                // Generate JPA entity class
                writer.write("package " + basePackage + ";\n\n");
                
                // Imports
                writer.write("import jakarta.persistence.*;\n");
                writer.write("import jakarta.validation.constraints.*;\n");
                writer.write("import java.time.LocalDateTime;\n");
                writer.write("import java.util.Objects;\n\n");
                
                // Entity annotation
                writer.write("@Entity\n");
                
                // Table annotation from database overlay
                if (obj.hasMetaAttr("tableName")) {
                    String tableName = obj.getMetaAttr("tableName").getValueAsString();
                    writer.write("@Table(name = \"" + tableName + "\")\n");
                }
                
                // Class declaration
                writer.write("public class " + className + " {\n\n");
                
                // Generate fields
                for (MetaField field : obj.getMetaFields()) {
                    generateJPAField(writer, field);
                }
                
                // Generate constructors
                generateConstructors(writer, className, obj);
                
                // Generate getters and setters
                for (MetaField field : obj.getMetaFields()) {
                    generateGetterSetter(writer, field);
                }
                
                // Generate equals and hashCode
                generateEqualsHashCode(writer, className, obj);
                
                writer.write("}\n");
            }
            
            getLog().info("Generated JPA entity: " + entityFile.getAbsolutePath());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate JPA entity for " + obj.getName(), e);
        }
    }
    
    private void generateJPAField(FileWriter writer, MetaField field) throws IOException {
        String fieldName = field.getName();
        String fieldType = getJavaType(field);
        
        writer.write("    ");
        
        // ID field annotations
        if (field.hasMetaAttr("isKey") && field.getMetaAttr("isKey").getValueAsBoolean()) {
            writer.write("@Id\n    ");
            if ("id".equals(fieldName)) {
                writer.write("@GeneratedValue(strategy = GenerationType.IDENTITY)\n    ");
            }
        }
        
        // Column annotation
        if (field.hasMetaAttr("columnName")) {
            String columnName = field.getMetaAttr("columnName").getValueAsString();
            writer.write("@Column(name = \"" + columnName + "\"");
            
            // Add nullable, unique constraints
            if (hasRequiredValidator(field)) {
                writer.write(", nullable = false");
            }
            
            if (field.hasMetaAttr("unique") && field.getMetaAttr("unique").getValueAsBoolean()) {
                writer.write(", unique = true");
            }
            
            writer.write(")\n    ");
        }
        
        // Validation annotations
        if (hasRequiredValidator(field)) {
            writer.write("@NotNull\n    ");
        }
        
        // String length validation
        if ("string".equals(field.getDataType().name().toLowerCase())) {
            Integer maxLength = getLengthValidatorMax(field);
            if (maxLength != null) {
                writer.write("@Size(max = " + maxLength + ")\n    ");
            }
        }
        
        // Email validation
        if (hasEmailValidation(field)) {
            writer.write("@Email\n    ");
        }
        
        // Field declaration
        writer.write("private " + fieldType + " " + fieldName + ";\n\n");
    }
    
    private void generateConstructors(FileWriter writer, String className, MetaObject obj) throws IOException {
        // Default constructor
        writer.write("    public " + className + "() {}\n\n");
        
        // Constructor with required fields
        Collection<MetaField> requiredFields = getRequiredFields(obj);
        if (!requiredFields.isEmpty()) {
            writer.write("    public " + className + "(");
            
            boolean first = true;
            for (MetaField field : requiredFields) {
                if (!first) writer.write(", ");
                writer.write(getJavaType(field) + " " + field.getName());
                first = false;
            }
            
            writer.write(") {\n");
            
            for (MetaField field : requiredFields) {
                writer.write("        this." + field.getName() + " = " + field.getName() + ";\n");
            }
            
            writer.write("    }\n\n");
        }
    }
    
    private void generateGetterSetter(FileWriter writer, MetaField field) throws IOException {
        String fieldName = field.getName();
        String fieldType = getJavaType(field);
        String capitalizedName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        
        // Getter
        writer.write("    public " + fieldType + " get" + capitalizedName + "() {\n");
        writer.write("        return " + fieldName + ";\n");
        writer.write("    }\n\n");
        
        // Setter
        writer.write("    public void set" + capitalizedName + "(" + fieldType + " " + fieldName + ") {\n");
        writer.write("        this." + fieldName + " = " + fieldName + ";\n");
        writer.write("    }\n\n");
    }
    
    private void generateEqualsHashCode(FileWriter writer, String className, MetaObject obj) throws IOException {
        // Find primary key field for equals/hashCode
        MetaField pkField = null;
        for (MetaField field : obj.getMetaFields()) {
            if (field.hasMetaAttr("isKey") && field.getMetaAttr("isKey").getValueAsBoolean()) {
                pkField = field;
                break;
            }
        }
        
        if (pkField != null) {
            String pkFieldName = pkField.getName();
            
            // equals method
            writer.write("    @Override\n");
            writer.write("    public boolean equals(Object o) {\n");
            writer.write("        if (this == o) return true;\n");
            writer.write("        if (!(o instanceof " + className + ")) return false;\n");
            writer.write("        " + className + " that = (" + className + ") o;\n");
            writer.write("        return Objects.equals(" + pkFieldName + ", that." + pkFieldName + ");\n");
            writer.write("    }\n\n");
            
            // hashCode method
            writer.write("    @Override\n");
            writer.write("    public int hashCode() {\n");
            writer.write("        return Objects.hash(" + pkFieldName + ");\n");
            writer.write("    }\n\n");
        }
    }
    
    // Utility methods
    private String getJavaType(MetaField field) {
        return switch (field.getDataType().name().toLowerCase()) {
            case "string" -> "String";
            case "int", "integer" -> "Integer";
            case "long" -> "Long";
            case "boolean" -> "Boolean";
            case "date" -> "LocalDateTime";
            case "double" -> "Double";
            case "float" -> "Float";
            case "short" -> "Short";
            case "byte" -> "Byte";
            default -> "Object";
        };
    }
    
    private boolean hasRequiredValidator(MetaField field) {
        return field.getMetaValidators().stream()
                .anyMatch(v -> "required".equals(v.getValidatorName()));
    }
    
    private boolean hasEmailValidation(MetaField field) {
        return field.getMetaValidators().stream()
                .anyMatch(v -> "email".equals(v.getValidatorName()) || 
                             ("regex".equals(v.getValidatorName()) && 
                              v.hasMetaAttr("mask") && 
                              v.getMetaAttr("mask").getValueAsString().contains("@")));
    }
    
    private Integer getLengthValidatorMax(MetaField field) {
        return field.getMetaValidators().stream()
                .filter(v -> "length".equals(v.getValidatorName()))
                .filter(v -> v.hasMetaAttr("max"))
                .map(v -> v.getMetaAttr("max").getValueAsInt())
                .findFirst()
                .orElse(null);
    }
    
    private Collection<MetaField> getRequiredFields(MetaObject obj) {
        return obj.getMetaFields().stream()
                .filter(this::hasRequiredValidator)
                .filter(field -> !field.hasMetaAttr("isKey") || !field.getMetaAttr("isKey").getValueAsBoolean())
                .toList();
    }
}
```

## Build Execution

### 1. Maven Lifecycle Integration

The MetaObjects plugin integrates into the Maven build lifecycle:

```bash
# Generate code during compile phase
mvn compile

# Generate code and run tests
mvn test

# Generate code and package
mvn package

# Clean generated sources
mvn clean

# Generate code only
mvn metaobjects:generate
```

### 2. IDE Integration

**IntelliJ IDEA configuration:**

1. Mark `target/generated-sources/java` as "Generated Sources Root"
2. Enable annotation processing
3. Configure Maven auto-import
4. Set up run configurations to regenerate on changes

**VS Code configuration:**

Add to `.vscode/settings.json`:
```json
{
    "java.sources.organizeImports.automaticImportAllow": ["**/target/generated-sources/**"],
    "maven.lifecycle.mappings": {
        "generate-sources": ["metaobjects:generate"]
    }
}
```

### 3. Incremental Builds

Configure incremental code generation:

```xml
<plugin>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <configuration>
        <globals>
            <incrementalBuild>true</incrementalBuild>
            <checksumFile>${project.build.directory}/.metaobjects-checksum</checksumFile>
        </globals>
        <!-- ... rest of configuration -->
    </configuration>
</plugin>
```

This configuration enables efficient builds by only regenerating code when metadata files change.

Continue with the best practices guide for advanced MetaObjects development patterns.