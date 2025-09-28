# Built-in Generators

The MetaObjects codegen framework includes several powerful built-in generators that cover common code generation scenarios. These generators follow the Direct Generator pattern and operate directly from the metadata registry without requiring metadata files.

## Generator Categories

### Schema Validation Generators

These generators create schema files for validating metadata structure and data instances.

#### JSON Schema Generator

**Class**: `MetaDataFileJsonSchemaGenerator`
**Purpose**: Creates JSON Schema files that validate metadata file structure

The JSON Schema generator reads constraint definitions from the metadata registry and generates JSON Schema v2020-12 files that can validate metadata JSON files during development.

**Configuration Options**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `schemaVersion` | string | `https://json-schema.org/draft/2020-12/schema` | JSON Schema version |
| `schemaId` | string | auto-generated | Schema ID URI |
| `title` | string | `MetaData File JSON Schema` | Schema title |
| `description` | string | `JSON Schema for validating MetaData file structure` | Schema description |

**Maven Plugin Configuration**:
```xml
<generator>
    <classname>com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/schemas</outputDir>
        <outputFilename>metadata-schema.json</outputFilename>
        <title>Custom MetaData Schema</title>
        <schemaId>https://mycompany.com/schemas/metadata.json</schemaId>
    </args>
</generator>
```

**Generated Schema Features**:
- Validates metadata file structure (`{"metadata": {"children": [...]}}`)
- Enforces constraint-based naming patterns (`^[a-zA-Z][a-zA-Z0-9_]*$`)
- Type validation for field and object definitions
- Required property enforcement
- Length restrictions and format validation

**Output Example**:
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://metaobjects.com/schemas/metadata.json",
  "title": "MetaData File JSON Schema",
  "description": "JSON Schema for validating MetaData file structure",
  "type": "object",
  "properties": {
    "metadata": {
      "$ref": "#/$defs/metadata"
    }
  },
  "required": ["metadata"],
  "$defs": {
    "metadata": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "pattern": "^[a-zA-Z][a-zA-Z0-9_]*$"
        }
      }
    }
  }
}
```

#### XSD Schema Generator

**Class**: `MetaDataFileXSDGenerator`
**Purpose**: Creates XML Schema Definition files for validating metadata XML structure

The XSD generator creates comprehensive XML Schema files that validate metadata XML files and enforce the same constraint-based validation as the JSON Schema generator.

**Configuration Options**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `targetNamespace` | string | `http://metaobjects.com/schema` | XML target namespace |
| `schemaVersion` | string | `6.0.0` | Schema version |
| `includeInlineAttributes` | boolean | `true` | Support inline attribute syntax |

**Maven Plugin Configuration**:
```xml
<generator>
    <classname>com.metaobjects.generator.direct.metadata.file.xsd.MetaDataFileXSDGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/schemas</outputDir>
        <outputFilename>metadata-schema.xsd</outputFilename>
        <targetNamespace>https://mycompany.com/metadata</targetNamespace>
        <includeInlineAttributes>true</includeInlineAttributes>
    </args>
</generator>
```

**Generated XSD Features**:
- Validates XML metadata file structure
- Supports inline attribute syntax validation
- Complex type definitions for metadata elements
- Constraint-based pattern restrictions
- Namespace-aware validation

### Documentation Generators

These generators create human-readable and AI-consumable documentation from the metadata registry.

#### AI Documentation Generator

**Class**: `MetaDataAIDocumentationGenerator`
**Purpose**: Creates comprehensive JSON documentation optimized for AI consumption

This generator produces detailed documentation designed for AI systems, including inheritance hierarchies, extension points, and implementation guidance.

**Configuration Options**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `version` | string | `6.1.0` | Documentation version |
| `includeInheritance` | boolean | `true` | Include inheritance hierarchy analysis |
| `includeImplementationDetails` | boolean | `true` | Include class mapping details |
| `includeExtensionGuidance` | boolean | `true` | Include plugin development guidance |
| `includeCrossLanguageInfo` | boolean | `false` | Include C#/TypeScript examples |

**Maven Plugin Configuration**:
```xml
<generator>
    <classname>com.metaobjects.generator.direct.metadata.ai.MetaDataAIDocumentationGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/docs</outputDir>
        <outputFilename>metaobjects-ai-docs.json</outputFilename>
        <includeInheritance>true</includeInheritance>
        <includeExtensionGuidance>true</includeExtensionGuidance>
        <includeCrossLanguageInfo>false</includeCrossLanguageInfo>
    </args>
</generator>
```

**Generated Documentation Structure**:
```json
{
  "metaObjectsDocumentation": {
    "version": "6.1.0",
    "generatedTimestamp": "2024-01-15T10:30:00Z",
    "typeSystem": {
      "inheritanceHierarchy": {
        "field.base": {
          "children": ["field.string", "field.int", "field.long"],
          "attributes": ["required", "defaultValue"],
          "description": "Base field type with common validation"
        }
      },
      "extensionPoints": [
        {
          "baseType": "field.base",
          "description": "Extend for custom field types",
          "exampleImplementation": "CurrencyField extends PrimitiveField"
        }
      ]
    }
  }
}
```

**Use Cases**:
- AI-powered development assistance
- Automated code generation guidance
- Plugin development documentation
- Framework architecture analysis

#### HTML Documentation Generator

**Class**: `MetaDataHtmlDocumentationGenerator`
**Purpose**: Creates professional, responsive HTML documentation for human consumption

This generator produces comprehensive, searchable HTML documentation with modern styling and navigation.

**Configuration Options**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `version` | string | `6.2.0` | Documentation version |
| `title` | string | `MetaObjects Framework Documentation` | Document title |
| `includeInheritance` | boolean | `true` | Include inheritance visualization |
| `includeExamples` | boolean | `true` | Include usage examples |
| `includeExtensionGuide` | boolean | `true` | Include plugin development guide |

**Maven Plugin Configuration**:
```xml
<generator>
    <classname>com.metaobjects.generator.direct.metadata.html.MetaDataHtmlDocumentationGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/site</outputDir>
        <outputFilename>framework-docs.html</outputFilename>
        <title>My Project MetaObjects Documentation</title>
        <version>1.0.0</version>
        <includeExamples>true</includeExamples>
    </args>
</generator>
```

**Generated Documentation Features**:
- Responsive design with sidebar navigation
- Type hierarchy visualization with inheritance relationships
- Detailed type definitions with examples
- Search functionality and cross-references
- Professional developer-friendly styling
- Plugin development guides

### Code Generators

These generators create executable code from metadata definitions.

#### Java Code Generator

**Class**: `JavaCodeGenerator`
**Purpose**: Generates Java interfaces and classes from MetaObject definitions

The Java code generator creates type-safe Java code with proper naming conventions, inheritance, and method signatures.

**Configuration Options**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | string | `interface` | Generate interfaces or classes |
| `pkgPrefix` | string | empty | Package name prefix |
| `pkgSuffix` | string | empty | Package name suffix |
| `namePrefix` | string | empty | Class name prefix |
| `nameSuffix` | string | empty | Class name suffix |
| `addArrayMethods` | boolean | `false` | Generate array utility methods |
| `addKeyMethods` | boolean | `false` | Generate key field methods |

**Supported Types**:
- `interface` - Java interfaces (default)
- `class` - Java classes with implementation stubs

**Maven Plugin Configuration**:
```xml
<generator>
    <classname>com.metaobjects.generator.direct.object.javacode.JavaCodeGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
        <type>interface</type>
        <pkgPrefix>com.mycompany</pkgPrefix>
        <pkgSuffix>model</pkgSuffix>
        <addArrayMethods>true</addArrayMethods>
        <addKeyMethods>true</addKeyMethods>
    </args>
</generator>
```

**Generated Java Features**:
- PascalCase class naming from metadata names
- Type-safe field accessor methods
- Proper Java package declarations
- Inheritance relationship preservation
- JPA annotation support (when metadata includes database attributes)
- Validation method generation
- Array utility methods for list fields
- Primary key and foreign key accessor methods

**Example Generated Interface**:
```java
package com.mycompany.model;

import javax.persistence.*;
import java.util.List;

/**
 * Generated interface for User metadata
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
public interface User {

    @Id
    @Column(name = "user_id")
    Long getId();
    void setId(Long id);

    @Column(name = "username", nullable = false, length = 50)
    String getUsername();
    void setUsername(String username);

    @Column(name = "email", nullable = false, length = 255)
    String getEmail();
    void setEmail(String email);

    // Array utility methods (when addArrayMethods=true)
    List<String> getEmailArray();
    void setEmailArray(List<String> emails);

    // Key methods (when addKeyMethods=true)
    boolean hasId();
    boolean isIdKey();
}
```

## Generator Extensions and Customization

### Service Provider Registration

All built-in generators register additional attributes that can be used in metadata definitions to control generation behavior.

**JSON Schema Attributes**:
```java
// Available on object.base and field.base
"jsonSchemaVersion", "jsonSchemaId", "jsonTitle", "jsonDescription"
"jsonFormat", "jsonPattern", "jsonEnum", "jsonMinimum", "jsonMaximum"
```

**AI Documentation Attributes**:
```java
// Available on object.base and field.base
"aiVersion", "aiDescription", "aiBusinessRule", "aiUsageContext"
"aiValidationRules", "aiExamples", "aiConstraints", "aiExtensionGuidance"
```

**HTML Documentation Attributes**:
```java
// Available on object.base and field.base
"htmlTitle", "htmlDescription", "htmlExample", "htmlUsagePattern"
"htmlExtensionGuide", "htmlSeeAlso", "htmlSinceVersion", "htmlDeprecated"
```

### Using Generator Attributes in Metadata

You can use these attributes in your metadata definitions to control generator behavior:

```json
{
  "metadata": {
    "children": [
      {
        "object": {
          "name": "User",
          "type": "pojo",
          "@htmlTitle": "User Account Management",
          "@htmlDescription": "Core user entity with authentication support",
          "@aiBusinessRule": "Users must have unique email addresses",
          "@jsonTitle": "User Schema Definition",
          "children": [
            {
              "field": {
                "name": "email",
                "type": "string",
                "@required": true,
                "@maxLength": 255,
                "@htmlExample": "user@example.com",
                "@aiValidationRules": "Must be valid email format with domain validation",
                "@jsonPattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
              }
            }
          ]
        }
      }
    ]
  }
}
```

### Performance Characteristics

All built-in generators are optimized for the MetaObjects READ-OPTIMIZED architecture:

| Generator Type | Memory Usage | Execution Time | Output Size |
|----------------|--------------|----------------|-------------|
| JSON Schema | 5-15MB | 100-500ms | 50-200KB |
| XSD Schema | 5-15MB | 150-600ms | 100-500KB |
| AI Documentation | 10-25MB | 200-800ms | 500KB-2MB |
| HTML Documentation | 15-30MB | 300-1200ms | 1-5MB |
| Java Code | 5-20MB | 100-400ms/class | 2-10KB/class |

**Registry Access Pattern**:
All generators use cached registry access that leverages the permanent metadata residence pattern, ensuring consistent sub-second generation times even for large metadata sets.

## Integration with Build Systems

### Maven Integration

The built-in generators integrate seamlessly with the MetaObjects Maven plugin:

```xml
<plugin>
    <groupId>com.draagon</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>6.2.0</version>
    <executions>
        <execution>
            <id>generate-schemas</id>
            <phase>process-classes</phase>
            <goals><goal>generate</goal></goals>
            <configuration>
                <loader>
                    <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
                </loader>
                <generators>
                    <!-- JSON Schema Generation -->
                    <generator>
                        <classname>com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator</classname>
                        <args>
                            <outputDir>${project.build.directory}/schemas</outputDir>
                            <outputFilename>metadata-schema.json</outputFilename>
                        </args>
                    </generator>

                    <!-- Java Code Generation -->
                    <generator>
                        <classname>com.metaobjects.generator.direct.object.javacode.JavaCodeGenerator</classname>
                        <args>
                            <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                            <type>interface</type>
                            <pkgPrefix>com.myproject</pkgPrefix>
                            <addKeyMethods>true</addKeyMethods>
                        </args>
                    </generator>

                    <!-- Documentation Generation -->
                    <generator>
                        <classname>com.metaobjects.generator.direct.metadata.html.MetaDataHtmlDocumentationGenerator</classname>
                        <args>
                            <outputDir>${project.build.directory}/site</outputDir>
                            <outputFilename>metadata-docs.html</outputFilename>
                            <title>My Project Metadata Documentation</title>
                        </args>
                    </generator>
                </generators>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Gradle Integration

For Gradle builds, you can invoke generators programmatically:

```groovy
task generateMetadataSchemas {
    doLast {
        // Create loader and registry
        def loader = new com.metaobjects.loader.file.FileMetaDataLoader()
        loader.init()

        // Generate JSON Schema
        def jsonGenerator = new com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator()
        jsonGenerator.setArg("outputDir", "${buildDir}/schemas")
        jsonGenerator.setArg("outputFilename", "metadata-schema.json")
        jsonGenerator.execute(loader)

        // Generate Java code
        def javaGenerator = new com.metaobjects.generator.direct.object.javacode.JavaCodeGenerator()
        javaGenerator.setArg("outputDir", "${buildDir}/generated-sources/java")
        javaGenerator.setArg("type", "interface")
        javaGenerator.execute(loader)
    }
}

compileJava.dependsOn generateMetadataSchemas
```

## Best Practices

### Generator Selection Guidelines

**Choose JSON Schema Generator when**:
- You need runtime validation of metadata files
- Working with JSON-based metadata exclusively
- Integrating with JSON Schema validation tools
- Building developer tooling with validation support

**Choose XSD Generator when**:
- You need XML metadata validation
- Working with XML-based tooling pipelines
- Requiring namespace-aware validation
- Integrating with enterprise XML validation systems

**Choose AI Documentation Generator when**:
- Building AI-powered development tools
- Creating automated code analysis systems
- Generating training data for AI models
- Building plugin discovery and recommendation systems

**Choose HTML Documentation Generator when**:
- Creating human-readable framework documentation
- Building developer portals and documentation sites
- Providing searchable reference documentation
- Creating onboarding materials for new developers

**Choose Java Code Generator when**:
- Building type-safe domain objects from metadata
- Generating JPA entities from database metadata
- Creating contract interfaces for service APIs
- Building code-first development workflows

### Performance Optimization

**For Large Metadata Sets**:
- Use filtered generation when possible
- Generate documentation in separate Maven executions
- Consider parallel generation for independent outputs
- Monitor memory usage during generation

**For CI/CD Pipelines**:
- Cache generator outputs when metadata hasn't changed
- Use incremental generation when supported
- Separate schema generation from code generation phases
- Consider artifact publishing for reusable schemas

### Multi-Module Projects

**Coordinate generation across modules**:
```xml
<!-- In parent POM -->
<properties>
    <metadata.output.dir>${project.build.directory}/shared-schemas</metadata.output.dir>
</properties>

<!-- In metadata module -->
<generator>
    <classname>com.metaobjects.generator.direct.metadata.file.json.MetaDataFileJsonSchemaGenerator</classname>
    <args>
        <outputDir>${metadata.output.dir}</outputDir>
        <outputFilename>shared-metadata-schema.json</outputFilename>
    </args>
</generator>

<!-- In API module -->
<generator>
    <classname>com.metaobjects.generator.direct.object.javacode.JavaCodeGenerator</classname>
    <args>
        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
        <type>interface</type>
    </args>
</generator>
```

This approach ensures schema consistency across multiple modules while allowing each module to generate the code artifacts it needs.