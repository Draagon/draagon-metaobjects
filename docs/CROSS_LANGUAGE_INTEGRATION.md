# MetaObjects Cross-Language Template Integration Guide

## Overview

This document provides comprehensive integration details for implementing the Mustache-based template system across Java, C#, and TypeScript. Each language implementation maintains architectural consistency while leveraging language-specific best practices and libraries.

## Architecture Consistency Across Languages

### Shared Interface Contract

All language implementations follow this consistent interface:

```
ITemplateEngine
├── generateCode(template, metaObject) → string
├── registerHelper(name, function) → void
├── loadTemplate(path) → Template
└── validateTemplate(template) → ValidationResult

IHelperRegistry  
├── register(name, helper) → void
├── get(name) → Helper
├── contains(name) → boolean
└── getAll() → Map<string, Helper>
```

### Common Template Structure

All languages use identical template definition format:

```yaml
name: "Template Name"
targetLanguage: "java|csharp|typescript"
template: |
  # Mustache template content - IDENTICAL across languages
helpers: ["helper1", "helper2"]  # Language-specific implementations
```

## Java Implementation Details

### Dependencies and Setup

**Maven Configuration (`pom.xml`):**

```xml
<dependencies>
    <!-- JMustache - Best performing Java Mustache implementation -->
    <dependency>
        <groupId>com.github.spullara.mustache.java</groupId>
        <artifactId>compiler</artifactId>
        <version>0.9.10</version>
    </dependency>
    
    <!-- Jackson for YAML template definitions -->
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
        <version>2.15.2</version>
    </dependency>
    
    <!-- MetaObjects dependencies -->
    <dependency>
        <groupId>com.draagon.meta</groupId>
        <artifactId>metadata</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

### Core Java Implementation

**Java Template Engine (`MustacheTemplateEngine.java`):**

```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.github.mustachejava.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MustacheTemplateEngine implements ITemplateEngine {
    
    private final MustacheFactory mustacheFactory;
    private final HelperRegistry helperRegistry;
    private final Map<String, Mustache> templateCache;
    
    public MustacheTemplateEngine() {
        this.mustacheFactory = new DefaultMustacheFactory();
        this.helperRegistry = new JavaHelperRegistry();
        this.templateCache = new ConcurrentHashMap<>();
    }
    
    @Override
    public String generateCode(TemplateDefinition template, MetaObject metaObject) {
        try {
            // Get or compile template
            Mustache mustache = getCompiledTemplate(template);
            
            // Create context with helper functions
            Map<String, Object> context = createContext(metaObject);
            
            // Execute template
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context);
            
            return writer.toString();
            
        } catch (Exception e) {
            throw new TemplateException("Failed to generate code", e);
        }
    }
    
    private Mustache getCompiledTemplate(TemplateDefinition template) {
        return templateCache.computeIfAbsent(template.getName(), 
            name -> mustacheFactory.compile(
                new StringReader(template.getTemplate()), 
                name
            )
        );
    }
    
    private Map<String, Object> createContext(MetaObject metaObject) {
        Map<String, Object> context = new HashMap<>();
        
        // Basic MetaObject properties
        context.put("className", metaObject.getName());
        context.put("packageName", getPackageName(metaObject));
        context.put("fullName", metaObject.getFullName());
        
        // Process fields with helper functions
        List<Map<String, Object>> fields = metaObject.getFields().stream()
            .map(this::createFieldContext)
            .collect(Collectors.toList());
        context.put("fields", fields);
        
        // Add lambda helpers for template use
        addLambdaHelpers(context);
        
        return context;
    }
    
    private void addLambdaHelpers(Map<String, Object> context) {
        // Capitalize helper
        context.put("capitalize", (Mustache.Lambda) (frag, out) -> {
            String text = frag.execute().trim();
            out.write(helperRegistry.get("capitalize").apply(text).toString());
        });
        
        // Java type helper
        context.put("javaType", (Mustache.Lambda) (frag, out) -> {
            String dataType = frag.execute().trim();
            out.write(helperRegistry.get("javaType").apply(dataType).toString());
        });
    }
}
```

**Java Helper Registry (`JavaHelperRegistry.java`):**

```java
package com.draagon.meta.generator.mustache;

import com.draagon.meta.field.MetaField;
import org.apache.commons.lang3.StringUtils;
import java.util.function.Function;

public class JavaHelperRegistry extends BaseHelperRegistry {
    
    @Override
    protected void registerLanguageSpecificHelpers() {
        // Java-specific type mappings
        register("javaType", this::getJavaType);
        register("javaImport", this::getJavaImport);
        register("javaDefault", this::getJavaDefault);
        register("isJavaPrimitive", this::isJavaPrimitive);
        
        // JPA-specific helpers
        register("jpaColumnType", this::getJpaColumnType);
        register("jpaAnnotations", this::getJpaAnnotations);
        
        // Spring-specific helpers
        register("springValidation", this::getSpringValidation);
    }
    
    private Object getJavaType(Object input) {
        if (input instanceof MetaField) {
            MetaField field = (MetaField) input;
            return mapDataTypeToJava(field.getDataType());
        }
        if (input instanceof String) {
            return mapDataTypeToJava((String) input);
        }
        return "Object";
    }
    
    private String mapDataTypeToJava(String dataType) {
        switch (dataType.toLowerCase()) {
            case "string": return "String";
            case "int": case "integer": return "Integer";
            case "long": return "Long";
            case "boolean": return "Boolean";
            case "double": return "Double";
            case "float": return "Float";
            case "date": return "java.time.LocalDate";
            case "datetime": return "java.time.LocalDateTime";
            case "decimal": return "java.math.BigDecimal";
            case "uuid": return "java.util.UUID";
            default: return "Object";
        }
    }
    
    private Object getJavaImport(Object input) {
        String javaType = getJavaType(input).toString();
        if (javaType.contains(".")) {
            return javaType;
        }
        return null; // No import needed for primitives
    }
    
    private Object isJavaPrimitive(Object input) {
        String javaType = getJavaType(input).toString();
        return Arrays.asList("int", "long", "boolean", "double", "float", "char", "byte", "short")
                .contains(javaType.toLowerCase());
    }
}
```

### Java Build Integration

**Maven Plugin Configuration:**

```xml
<plugin>
    <groupId>com.draagon.meta</groupId>
    <artifactId>metaobjects-maven-plugin</artifactId>
    <version>${project.version}</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <generator>mustache</generator>
                <templatePath>src/main/resources/templates/</templatePath>
                <outputDirectory>target/generated-sources/metaobjects</outputDirectory>
                <packagePrefix>com.example.generated</packagePrefix>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## C# Implementation Details

### Dependencies and Setup

**NuGet Package Configuration (`.csproj`):**

```xml
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <TargetFramework>net6.0</TargetFramework>
  </PropertyGroup>
  
  <ItemGroup>
    <!-- Stubble - High performance .NET Mustache implementation -->
    <PackageReference Include="Stubble.Core" Version="1.10.8" />
    <PackageReference Include="Stubble.Extensions.Loaders" Version="1.10.8" />
    
    <!-- YAML Configuration -->
    <PackageReference Include="YamlDotNet" Version="13.1.1" />
    
    <!-- JSON.NET for complex object handling -->
    <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
  </ItemGroup>
</Project>
```

### Core C# Implementation

**C# Template Engine (`MustacheTemplateEngine.cs`):**

```csharp
using Stubble.Core.Builders;
using Stubble.Core.Contexts;
using Stubble.Core.Renderers.StringRenderer;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace MetaObjects.CodeGen.Mustache
{
    public class MustacheTemplateEngine : ITemplateEngine
    {
        private readonly StubbleVisitorRenderer _renderer;
        private readonly IHelperRegistry _helperRegistry;
        private readonly Dictionary<string, string> _templateCache;
        
        public MustacheTemplateEngine()
        {
            _renderer = new StubbleBuilder()
                .Configure(settings => {
                    settings.SetIgnoreCaseOnKeyLookup(true);
                    settings.SetMaxRecursionDepth(512);
                })
                .Build();
                
            _helperRegistry = new CSharpHelperRegistry();
            _templateCache = new Dictionary<string, string>();
        }
        
        public string GenerateCode(TemplateDefinition template, MetaObject metaObject)
        {
            try
            {
                // Create context with helper functions
                var context = CreateContext(metaObject);
                
                // Render template
                return _renderer.Render(template.Template, context);
            }
            catch (Exception ex)
            {
                throw new TemplateException($"Failed to generate code: {ex.Message}", ex);
            }
        }
        
        private object CreateContext(MetaObject metaObject)
        {
            var context = new Dictionary<string, object>
            {
                ["className"] = metaObject.Name,
                ["namespaceName"] = GetNamespaceName(metaObject),
                ["fullName"] = metaObject.FullName,
                ["fields"] = metaObject.Fields.Select(CreateFieldContext).ToList()
            };
            
            // Add helper functions as lambda expressions
            AddLambdaHelpers(context);
            
            return context;
        }
        
        private void AddLambdaHelpers(Dictionary<string, object> context)
        {
            // C# specific helpers using lambda expressions
            context["capitalize"] = new Func<string, object>(text => 
                _helperRegistry.Get("capitalize").Invoke(text));
                
            context["csharpType"] = new Func<object, object>(input => 
                _helperRegistry.Get("csharpType").Invoke(input));
                
            context["pascalCase"] = new Func<string, object>(text => 
                _helperRegistry.Get("pascalCase").Invoke(text));
        }
        
        private string GetNamespaceName(MetaObject metaObject)
        {
            return metaObject.HasAttribute("namespace") 
                ? metaObject.GetAttributeValue("namespace").ToString()
                : "Generated.Models";
        }
    }
}
```

**C# Helper Registry (`CSharpHelperRegistry.cs`):**

```csharp
using System;
using System.Collections.Generic;
using System.Linq;

namespace MetaObjects.CodeGen.Mustache
{
    public class CSharpHelperRegistry : BaseHelperRegistry
    {
        protected override void RegisterLanguageSpecificHelpers()
        {
            // C# specific type mappings
            Register("csharpType", GetCSharpType);
            Register("csharpDefault", GetCSharpDefault);
            Register("csharpUsing", GetCSharpUsing);
            Register("isNullable", IsNullableType);
            
            // Entity Framework specific
            Register("efColumnType", GetEfColumnType);
            Register("efAnnotations", GetEfAnnotations);
            
            // ASP.NET specific
            Register("aspNetValidation", GetAspNetValidation);
            Register("pascalCase", ToPascalCase);
        }
        
        private object GetCSharpType(object input)
        {
            var dataType = ExtractDataType(input);
            
            return dataType?.ToLowerInvariant() switch
            {
                "string" => "string",
                "int" or "integer" => "int",
                "long" => "long",
                "boolean" => "bool",
                "double" => "double",
                "float" => "float",
                "date" => "DateTime",
                "datetime" => "DateTime",
                "decimal" => "decimal",
                "uuid" => "Guid",
                _ => "object"
            };
        }
        
        private object GetCSharpDefault(object input)
        {
            var csharpType = GetCSharpType(input).ToString();
            
            return csharpType switch
            {
                "string" => "string.Empty",
                "int" => "0",
                "long" => "0L",
                "bool" => "false",
                "double" => "0.0",
                "float" => "0.0f",
                "DateTime" => "DateTime.MinValue",
                "decimal" => "0m",
                "Guid" => "Guid.Empty",
                _ => "null"
            };
        }
        
        private object ToPascalCase(object input)
        {
            if (input?.ToString() is string text && !string.IsNullOrEmpty(text))
            {
                return char.ToUpperInvariant(text[0]) + text.Substring(1);
            }
            return input?.ToString() ?? string.Empty;
        }
        
        private object IsNullableType(object input)
        {
            // Check if field is marked as nullable/optional
            if (input is MetaField field)
            {
                return !field.HasAttribute("required") || 
                       !bool.Parse(field.GetAttributeValue("required")?.ToString() ?? "false");
            }
            return true;
        }
    }
}
```

### C# Build Integration

**MSBuild Integration (`MetaObjects.targets`):**

```xml
<Project>
  <UsingTask TaskName="MetaObjectsCodeGenTask" AssemblyFile="$(MSBuildThisFileDirectory)MetaObjects.CodeGen.dll" />
  
  <Target Name="GenerateMetaObjectsCode" BeforeTargets="CoreCompile">
    <MetaObjectsCodeGenTask 
      TemplateEngine="mustache"
      TemplatePath="$(ProjectDir)Templates\"
      MetadataPath="$(ProjectDir)Metadata\"
      OutputPath="$(IntermediateOutputPath)Generated\"
      Namespace="$(RootNamespace).Generated" />
  </Target>
  
  <ItemGroup>
    <Compile Include="$(IntermediateOutputPath)Generated\**\*.cs" />
  </ItemGroup>
</Project>
```

## TypeScript Implementation Details

### Dependencies and Setup

**NPM Configuration (`package.json`):**

```json
{
  "name": "metaobjects-codegen",
  "version": "1.0.0",
  "dependencies": {
    "mustache": "^4.2.0",
    "js-yaml": "^4.1.0",
    "typescript": "^5.0.0"
  },
  "devDependencies": {
    "@types/mustache": "^4.2.0",
    "@types/js-yaml": "^4.0.0",
    "@types/node": "^18.0.0"
  }
}
```

### Core TypeScript Implementation

**TypeScript Template Engine (`MustacheTemplateEngine.ts`):**

```typescript
import * as Mustache from 'mustache';
import * as yaml from 'js-yaml';
import { ITemplateEngine, TemplateDefinition, MetaObject, HelperRegistry } from './types';

export class MustacheTemplateEngine implements ITemplateEngine {
    private readonly helperRegistry: HelperRegistry;
    private readonly templateCache: Map<string, string> = new Map();
    
    constructor() {
        this.helperRegistry = new TypeScriptHelperRegistry();
    }
    
    generateCode(template: TemplateDefinition, metaObject: MetaObject): string {
        try {
            // Create context with helper functions
            const context = this.createContext(metaObject);
            
            // Configure Mustache
            Mustache.tags = ['{{', '}}'];
            
            // Render template
            return Mustache.render(template.template, context, {}, {
                escape: (text: string) => text // Disable HTML escaping for code generation
            });
            
        } catch (error) {
            throw new Error(`Failed to generate code: ${error.message}`);
        }
    }
    
    private createContext(metaObject: MetaObject): any {
        const context = {
            className: metaObject.name,
            packageName: this.getPackageName(metaObject),
            fullName: metaObject.fullName,
            fields: metaObject.fields.map(field => this.createFieldContext(field)),
            
            // Helper functions as lambda expressions
            capitalize: () => (text: string, render: Function) => 
                this.helperRegistry.get('capitalize')!(render(text)),
                
            camelCase: () => (text: string, render: Function) => 
                this.helperRegistry.get('camelCase')!(render(text)),
                
            typeScriptType: () => (input: any, render: Function) => 
                this.helperRegistry.get('typeScriptType')!(render(input))
        };
        
        return context;
    }
    
    private createFieldContext(field: any): any {
        return {
            name: field.name,
            typeScriptType: this.helperRegistry.get('typeScriptType')!(field),
            isOptional: this.helperRegistry.get('isOptional')!(field),
            camelCaseName: this.helperRegistry.get('camelCase')!(field.name),
            hasDefaultValue: field.attributes?.defaultValue !== undefined,
            defaultValue: field.attributes?.defaultValue,
            description: field.description || `Property ${field.name}`
        };
    }
    
    private getPackageName(metaObject: MetaObject): string {
        return metaObject.attributes?.package || 'generated';
    }
}
```

**TypeScript Helper Registry (`TypeScriptHelperRegistry.ts`):**

```typescript
import { BaseHelperRegistry } from './BaseHelperRegistry';

export class TypeScriptHelperRegistry extends BaseHelperRegistry {
    
    protected registerLanguageSpecificHelpers(): void {
        // TypeScript specific type mappings
        this.register('typeScriptType', this.getTypeScriptType.bind(this));
        this.register('typeScriptDefault', this.getTypeScriptDefault.bind(this));
        this.register('typeScriptImport', this.getTypeScriptImport.bind(this));
        this.register('isOptional', this.isOptional.bind(this));
        
        // React specific helpers
        this.register('reactPropType', this.getReactPropType.bind(this));
        this.register('getInputType', this.getInputType.bind(this));
        
        // Validation helpers
        this.register('hasValidation', this.hasValidation.bind(this));
        this.register('getValidationRules', this.getValidationRules.bind(this));
    }
    
    private getTypeScriptType(input: any): string {
        const dataType = this.extractDataType(input);
        
        switch (dataType?.toLowerCase()) {
            case 'string': return 'string';
            case 'int':
            case 'integer':
            case 'long':
            case 'double':
            case 'float':
            case 'decimal': return 'number';
            case 'boolean': return 'boolean';
            case 'date':
            case 'datetime': return 'Date';
            case 'uuid': return 'string';
            case 'array': return 'any[]';
            case 'object': return 'any';
            default: return 'any';
        }
    }
    
    private getTypeScriptDefault(input: any): string {
        const tsType = this.getTypeScriptType(input);
        
        switch (tsType) {
            case 'string': return "''";
            case 'number': return '0';
            case 'boolean': return 'false';
            case 'Date': return 'new Date()';
            case 'any[]': return '[]';
            case 'any': return 'undefined';
            default: return 'undefined';
        }
    }
    
    private isOptional(input: any): boolean {
        if (typeof input === 'object' && input.attributes) {
            return !input.attributes.required || input.attributes.isOptional === 'true';
        }
        return true;
    }
    
    private getInputType(input: any): string {
        const tsType = this.getTypeScriptType(input);
        
        switch (tsType) {
            case 'string': return 'text';
            case 'number': return 'number';
            case 'boolean': return 'checkbox';
            case 'Date': return 'datetime-local';
            default: return 'text';
        }
    }
    
    private getReactPropType(input: any): string {
        const tsType = this.getTypeScriptType(input);
        const isOptional = this.isOptional(input);
        
        return isOptional ? `${tsType} | undefined` : tsType;
    }
}
```

### TypeScript Build Integration

**Webpack Plugin Configuration (`webpack.config.js`):**

```javascript
const MetaObjectsCodeGenPlugin = require('./plugins/MetaObjectsCodeGenPlugin');

module.exports = {
    plugins: [
        new MetaObjectsCodeGenPlugin({
            templateEngine: 'mustache',
            templatePath: './src/templates/',
            metadataPath: './src/metadata/',
            outputPath: './src/generated/',
            watch: process.env.NODE_ENV === 'development'
        })
    ]
};
```

**Custom Webpack Plugin (`MetaObjectsCodeGenPlugin.js`):**

```javascript
const { MustacheTemplateEngine } = require('./MustacheTemplateEngine');

class MetaObjectsCodeGenPlugin {
    constructor(options) {
        this.options = options;
        this.templateEngine = new MustacheTemplateEngine();
    }
    
    apply(compiler) {
        compiler.hooks.beforeCompile.tapAsync('MetaObjectsCodeGen', (params, callback) => {
            this.generateCode().then(() => callback()).catch(callback);
        });
    }
    
    async generateCode() {
        // Load templates and metadata
        // Generate code using template engine
        // Write output files
    }
}

module.exports = MetaObjectsCodeGenPlugin;
```

## Cross-Language Template Sharing

### Shared Template Repository Structure

```
templates/
├── shared/                    # Cross-language templates
│   ├── entity.mustache       # Basic entity (works in all languages)
│   ├── dto.mustache          # Data transfer object
│   └── interface.mustache    # Interface/contract definition
├── java/                     # Java-specific templates
│   ├── jpa-entity.mustache
│   ├── spring-controller.mustache
│   └── repository.mustache
├── csharp/                   # C#-specific templates
│   ├── ef-entity.mustache
│   ├── controller.mustache
│   └── service.mustache
└── typescript/               # TypeScript-specific templates
    ├── interface.mustache
    ├── react-component.mustache
    └── service.mustache
```

### Template Versioning and Compatibility

**Template Compatibility Matrix:**

| Template Version | Java Support | C# Support | TypeScript Support |
|-----------------|-------------|------------|-------------------|
| 1.0.x           | ✅ JDK 8+   | ✅ .NET 5+  | ✅ TS 4.0+        |
| 1.1.x           | ✅ JDK 11+  | ✅ .NET 6+  | ✅ TS 4.5+        |
| 2.0.x           | ✅ JDK 17+  | ✅ .NET 7+  | ✅ TS 5.0+        |

### Cross-Language Testing Strategy

**Shared Test Cases (`test-cases.yaml`):**

```yaml
testCases:
  - name: "Basic Entity Generation"
    metadata: "test-user.json"
    expectedOutputs:
      java: "User.java"
      csharp: "User.cs"
      typescript: "User.ts"
    
  - name: "JPA/EF Entity Generation"
    metadata: "test-user-with-db.json"
    expectedOutputs:
      java: "UserEntity.java"
      csharp: "UserEntity.cs"
      typescript: "UserInterface.ts"
```

This cross-language integration approach ensures that the same template definitions can generate appropriate code for each target language while maintaining consistency in the generated output structure and functionality.