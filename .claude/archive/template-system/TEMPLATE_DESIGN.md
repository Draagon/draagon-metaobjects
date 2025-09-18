# Cross-Language Template Design

## Overview
Template definitions that can be shared across Java, C#, and TypeScript implementations of MetaObjects.

## Template Definition Format

### Basic Structure
```yaml
template:
  metadata:
    name: "entity-class"
    version: "1.0.0"
    description: "Generates entity classes"
    author: "MetaObjects"
    
  requirements:
    metaObjectTypes: ["object"]
    attributes: ["dbTable", "validation"]
    services: ["codeGen"]
    
  variables:
    className: "${metaObject.name}"
    packageName: "${metaObject.attr.package}"
    tableName: "${metaObject.attr.dbTable}"
    
  sections:
    - name: "class-declaration"
      pattern: "${targetLanguage.classDeclaration}"
      variables:
        className: "${className}"
        baseClass: "${metaObject.attr.baseClass}"
        
    - name: "properties"
      pattern: "${targetLanguage.propertyDeclaration}"
      forEach: "${metaObject.fields}"
      variables:
        fieldName: "${field.name}"
        fieldType: "${targetLanguage.mapType(field.dataType)}"
        
    - name: "constructor"
      pattern: "${targetLanguage.constructor}"
      conditional: "${metaObject.attr.generateConstructor}"
```

### Language-Specific Mappings
Templates reference language-neutral patterns that each implementation maps to target syntax:

```yaml
languageMappings:
  java:
    classDeclaration: "public class ${className} extends ${baseClass} {"
    propertyDeclaration: "private ${fieldType} ${fieldName};"
    constructor: "public ${className}() {}"
    
  csharp:
    classDeclaration: "public class ${className} : ${baseClass} {"
    propertyDeclaration: "public ${fieldType} ${fieldName} { get; set; }"
    constructor: "public ${className}() {}"
    
  typescript:
    classDeclaration: "export class ${className} extends ${baseClass} {"
    propertyDeclaration: "${fieldName}: ${fieldType};"
    constructor: "constructor() { super(); }"
```

## Key Design Principles

1. **Language Neutral**: Templates define WHAT to generate, not HOW
2. **Pattern-Based**: Use symbolic patterns that each language implements
3. **Variable Resolution**: MetaObject properties map to template variables
4. **Conditional Logic**: Templates can include conditional sections
5. **Iteration Support**: Templates can iterate over collections (fields, etc.)

## Template Engine Contract

Each language implementation must provide:
- Template parser (JSON/YAML)
- Variable resolver (MetaObject → variables)
- Language mappings (patterns → target syntax)
- Type mapper (MetaObjects types → target types)
- Code formatter (target language formatting)

## Integration with v6.0.0

Templates integrate with service architecture:
- Template discovery via ServiceLoader
- Attribute requirements through MetaDataEnhancementService
- Service-specific template variants
- Plugin-based language mapping providers