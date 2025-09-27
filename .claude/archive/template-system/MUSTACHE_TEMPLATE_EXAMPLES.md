# MetaObjects Mustache Template Examples

## Overview

This document provides comprehensive examples of Mustache templates for MetaObjects cross-language code generation. These templates demonstrate the power and flexibility of the Mustache + Helper Functions approach for generating sophisticated code across Java, C#, and TypeScript.

## Template Structure and Conventions

### Template Definition Format

All templates follow this YAML structure:

```yaml
name: "Template Name"
version: "1.0.0"
description: "Template description"
targetLanguage: "java|csharp|typescript"
outputFileExtension: "java|cs|ts"
packagePath: true|false
requirements:
  attributes: ["attr1", "attr2"]  # Required MetaObject attributes
  helpers: ["helper1", "helper2"] # Required helper functions
template: |
  # Mustache template content here
partials:
  partial_name: |
    # Reusable template partial
```

## Java Template Examples

### 1. JPA Entity Template

**File: `templates/java/jpa-entity.mustache.yaml`**

```yaml
name: "JPA Entity Template"
version: "1.0.0"
description: "Generate JPA entities with database annotations"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
requirements:
  attributes: ["dbTable"]
  helpers: ["capitalize", "javaType", "dbColumnName", "isIdField", "isNullable"]
template: |
  package {{packageName}};
  
  import javax.persistence.*;
  import java.io.Serializable;
  {{#imports}}
  import {{.}};
  {{/imports}}
  
  /**
   * JPA Entity for {{className}}
   * Generated from MetaObject: {{fullName}}
   */
  @Entity
  @Table(name = "{{dbTableName}}")
  public class {{className}} implements Serializable {
      
      private static final long serialVersionUID = 1L;
      
      {{#fields}}
      {{#isIdField}}
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      {{/isIdField}}
      @Column(name = "{{dbColumnName}}"{{^isNullable}}, nullable = false{{/isNullable}})
      private {{javaType}} {{name}};
      
      {{/fields}}
      
      // Default constructor
      public {{className}}() {}
      
      // Full constructor
      public {{className}}({{#fields}}{{^isIdField}}{{javaType}} {{name}}{{^isLast}}, {{/isLast}}{{/isIdField}}{{/fields}}) {
          {{#fields}}
          {{^isIdField}}
          this.{{name}} = {{name}};
          {{/isIdField}}
          {{/fields}}
      }
      
      {{#fields}}
      // Getter for {{name}}
      public {{javaType}} {{#capitalize}}get{{name}}{{/capitalize}}() {
          return this.{{name}};
      }
      
      // Setter for {{name}}
      public void {{#capitalize}}set{{name}}{{/capitalize}}({{javaType}} {{name}}) {
          this.{{name}} = {{name}};
      }
      
      {{/fields}}
      
      // Static factory method using MetaObjects
      public static {{className}} newInstance() {
          com.metaobjects.object.MetaObject metaObject = 
              com.metaobjects.loader.MetaDataRegistry.findMetaObject("{{fullName}}");
          if (metaObject == null) {
              throw new IllegalStateException("MetaObject not found: {{fullName}}");
          }
          return new {{className}}();
      }
      
      @Override
      public boolean equals(Object obj) {
          if (this == obj) return true;
          if (obj == null || getClass() != obj.getClass()) return false;
          {{className}} that = ({{className}}) obj;
          {{#fields}}
          {{#isIdField}}
          return Objects.equals({{name}}, that.{{name}});
          {{/isIdField}}
          {{/fields}}
      }
      
      @Override
      public int hashCode() {
          {{#fields}}
          {{#isIdField}}
          return Objects.hash({{name}});
          {{/isIdField}}
          {{/fields}}
      }
      
      @Override
      public String toString() {
          return "{{className}}{" +
              {{#fields}}
              "{{name}}=" + {{name}} +
              {{^isLast}}", " +{{/isLast}}
              {{/fields}}
              '}';
      }
  }
```

### 2. ValueObject Extension Template

**File: `templates/java/valueobject-extension.mustache.yaml`**

```yaml
name: "ValueObject Extension Template"
version: "1.0.0"
description: "Generate classes extending ValueObject with dynamic property access"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
requirements:
  helpers: ["capitalize", "javaType"]
template: |
  package {{packageName}};
  
  import com.metaobjects.object.value.ValueObject;
  import com.metaobjects.object.MetaObject;
  import com.metaobjects.loader.MetaDataRegistry;
  {{#imports}}
  import {{.}};
  {{/imports}}
  
  /**
   * ValueObject extension for {{className}}
   * Uses dynamic property access via MetaObjects
   */
  public class {{className}} extends ValueObject {
      
      private static final String META_OBJECT_NAME = "{{fullName}}";
      
      // Static factory method that finds MetaObject by full name
      public static {{className}} newInstance() {
          MetaObject metaObject = MetaDataRegistry.findMetaObject(META_OBJECT_NAME);
          if (metaObject == null) {
              throw new IllegalStateException("MetaObject not found: " + META_OBJECT_NAME);
          }
          return new {{className}}(metaObject);
      }
      
      // Constructor with MetaObject
      public {{className}}(MetaObject metaObject) {
          super(metaObject);
      }
      
      // Type-safe property accessors using dynamic get/set methods
      {{#fields}}
      
      /**
       * Get {{name}} property
       * @return {{javaType}} value or null if not set
       */
      public {{javaType}} {{#capitalize}}get{{name}}{{/capitalize}}() {
          return ({{javaType}}) getAttrValue("{{name}}");
      }
      
      /**
       * Set {{name}} property
       * @param {{name}} the {{javaType}} value to set
       */
      public void {{#capitalize}}set{{name}}{{/capitalize}}({{javaType}} {{name}}) {
          setAttrValue("{{name}}", {{name}});
      }
      
      /**
       * Check if {{name}} property is set
       * @return true if {{name}} has a value
       */
      public boolean {{#capitalize}}has{{name}}{{/capitalize}}() {
          return hasAttr("{{name}}");
      }
      
      {{/fields}}
      
      // Fluent builder methods for method chaining
      {{#fields}}
      
      /**
       * Fluent setter for {{name}}
       * @param {{name}} the {{javaType}} value to set
       * @return this instance for method chaining
       */
      public {{className}} {{name}}({{javaType}} {{name}}) {
          {{#capitalize}}set{{name}}{{/capitalize}}({{name}});
          return this;
      }
      
      {{/fields}}
      
      /**
       * Create a copy of this object with all current values
       * @return new {{className}} instance with copied values
       */
      public {{className}} copy() {
          {{className}} copy = newInstance();
          {{#fields}}
          if ({{#capitalize}}has{{name}}{{/capitalize}}()) {
              copy.{{#capitalize}}set{{name}}{{/capitalize}}({{#capitalize}}get{{name}}{{/capitalize}}());
          }
          {{/fields}}
          return copy;
      }
      
      /**
       * Get the MetaObject name for this class
       * @return the fully qualified MetaObject name
       */
      public static String getMetaObjectName() {
          return META_OBJECT_NAME;
      }
  }
```

### 3. Spring Repository Template

**File: `templates/java/spring-repository.mustache.yaml`**

```yaml
name: "Spring Repository Template"
version: "1.0.0"
description: "Generate Spring Data JPA repositories"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
requirements:
  helpers: ["capitalize", "javaType", "getIdType", "getIdFieldName"]
template: |
  package {{packageName}}.repository;
  
  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.data.jpa.repository.Query;
  import org.springframework.data.repository.query.Param;
  import org.springframework.stereotype.Repository;
  import {{packageName}}.{{className}};
  import java.util.List;
  import java.util.Optional;
  
  /**
   * Spring Data JPA repository for {{className}}
   * Generated from MetaObject: {{fullName}}
   */
  @Repository
  public interface {{className}}Repository extends JpaRepository<{{className}}, {{idType}}> {
      
      {{#fields}}
      {{^isIdField}}
      {{#isSearchable}}
      /**
       * Find {{className}} entities by {{name}}
       * @param {{name}} the {{javaType}} to search for
       * @return list of matching entities
       */
      List<{{className}}> findBy{{#capitalize}}{{name}}{{/capitalize}}({{javaType}} {{name}});
      
      /**
       * Find first {{className}} entity by {{name}}
       * @param {{name}} the {{javaType}} to search for
       * @return optional containing the first match
       */
      Optional<{{className}}> findFirstBy{{#capitalize}}{{name}}{{/capitalize}}({{javaType}} {{name}});
      
      {{/isSearchable}}
      {{/isIdField}}
      {{/fields}}
      
      /**
       * Custom query to find active {{className}} entities
       * @return list of active entities
       */
      @Query("SELECT e FROM {{className}} e WHERE e.active = true")
      List<{{className}}> findAllActive();
      
      /**
       * Count entities by status
       * @param status the status to count
       * @return count of entities with the given status
       */
      long countByStatus(String status);
  }
```

## C# Template Examples

### 4. Entity Framework Entity Template

**File: `templates/csharp/ef-entity.mustache.yaml`**

```yaml
name: "Entity Framework Entity Template"
version: "1.0.0"
description: "Generate Entity Framework entities with database annotations"
targetLanguage: "csharp"
outputFileExtension: "cs"
packagePath: true
requirements:
  attributes: ["dbTable"]
  helpers: ["capitalize", "csharpType", "dbColumnName", "isIdField", "isNullable"]
template: |
  using System;
  using System.ComponentModel.DataAnnotations;
  using System.ComponentModel.DataAnnotations.Schema;
  {{#imports}}
  using {{.}};
  {{/imports}}
  
  namespace {{namespaceName}}
  {
      /// <summary>
      /// Entity Framework entity for {{className}}
      /// Generated from MetaObject: {{fullName}}
      /// </summary>
      [Table("{{dbTableName}}")]
      public class {{className}}
      {
          {{#fields}}
          {{#isIdField}}
          /// <summary>
          /// Primary key for {{className}}
          /// </summary>
          [Key]
          [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
          {{/isIdField}}
          [Column("{{dbColumnName}}")]
          {{^isNullable}}
          [Required]
          {{/isNullable}}
          public {{csharpType}} {{#capitalize}}{{name}}{{/capitalize}} { get; set; }
          
          {{/fields}}
          
          /// <summary>
          /// Default constructor
          /// </summary>
          public {{className}}()
          {
          }
          
          /// <summary>
          /// Constructor with required parameters
          /// </summary>
          public {{className}}({{#fields}}{{^isIdField}}{{csharpType}} {{name}}{{^isLast}}, {{/isLast}}{{/isIdField}}{{/fields}})
          {
              {{#fields}}
              {{^isIdField}}
              this.{{#capitalize}}{{name}}{{/capitalize}} = {{name}};
              {{/isIdField}}
              {{/fields}}
          }
          
          /// <summary>
          /// Static factory method using MetaObjects
          /// </summary>
          /// <returns>New instance of {{className}}</returns>
          public static {{className}} NewInstance()
          {
              var metaObject = MetaDataRegistry.FindMetaObject("{{fullName}}");
              if (metaObject == null)
              {
                  throw new InvalidOperationException("MetaObject not found: {{fullName}}");
              }
              return new {{className}}();
          }
          
          /// <summary>
          /// Equality comparison based on ID
          /// </summary>
          public override bool Equals(object obj)
          {
              if (obj is {{className}} other)
              {
                  {{#fields}}
                  {{#isIdField}}
                  return {{#capitalize}}{{name}}{{/capitalize}} == other.{{#capitalize}}{{name}}{{/capitalize}};
                  {{/isIdField}}
                  {{/fields}}
              }
              return false;
          }
          
          /// <summary>
          /// Hash code based on ID
          /// </summary>
          public override int GetHashCode()
          {
              {{#fields}}
              {{#isIdField}}
              return {{#capitalize}}{{name}}{{/capitalize}}.GetHashCode();
              {{/isIdField}}
              {{/fields}}
          }
          
          /// <summary>
          /// String representation of the entity
          /// </summary>
          public override string ToString()
          {
              return $"{{className}} {{ {{#fields}}{{#capitalize}}{{name}}{{/capitalize}} = {{{#capitalize}}{{name}}{{/capitalize}}}{{^isLast}}, {{/isLast}}{{/fields}} }}";
          }
      }
  }
```

## TypeScript Template Examples

### 5. TypeScript Interface Template

**File: `templates/typescript/interface.mustache.yaml`**

```yaml
name: "TypeScript Interface Template"
version: "1.0.0"
description: "Generate TypeScript interfaces with optional properties"
targetLanguage: "typescript"
outputFileExtension: "ts"
packagePath: false
requirements:
  helpers: ["camelCase", "typeScriptType", "isOptional"]
template: |
  {{#imports}}
  import { {{.}} } from './{{.}}';
  {{/imports}}
  
  /**
   * TypeScript interface for {{className}}
   * Generated from MetaObject: {{fullName}}
   */
  export interface {{className}} {
    {{#fields}}
    /**
     * {{description}}
     */
    {{#camelCase}}{{name}}{{/camelCase}}{{#isOptional}}?{{/isOptional}}: {{typeScriptType}};
    {{/fields}}
  }
  
  /**
   * Factory function to create {{className}} instances
   */
  export function create{{className}}(data: Partial<{{className}}>): {{className}} {
    return {
      {{#fields}}
      {{#camelCase}}{{name}}{{/camelCase}}: data.{{#camelCase}}{{name}}{{/camelCase}}{{#hasDefaultValue}} ?? {{defaultValue}}{{/hasDefaultValue}},
      {{/fields}}
    } as {{className}};
  }
  
  /**
   * Type guard to check if object is {{className}}
   */
  export function is{{className}}(obj: any): obj is {{className}} {
    return obj !== null && 
           typeof obj === 'object' &&
           {{#fields}}
           {{^isOptional}}
           '{{#camelCase}}{{name}}{{/camelCase}}' in obj &&
           {{/isOptional}}
           {{/fields}}
           true;
  }
  
  /**
   * Validation function for {{className}}
   */
  export function validate{{className}}(obj: {{className}}): string[] {
    const errors: string[] = [];
    
    {{#fields}}
    {{^isOptional}}
    if (obj.{{#camelCase}}{{name}}{{/camelCase}} === undefined || obj.{{#camelCase}}{{name}}{{/camelCase}} === null) {
      errors.push('{{#camelCase}}{{name}}{{/camelCase}} is required');
    }
    {{/isOptional}}
    {{#hasValidation}}
    if (obj.{{#camelCase}}{{name}}{{/camelCase}} !== undefined && !{{validationFunction}}(obj.{{#camelCase}}{{name}}{{/camelCase}})) {
      errors.push('{{#camelCase}}{{name}}{{/camelCase}} {{validationMessage}}');
    }
    {{/hasValidation}}
    {{/fields}}
    
    return errors;
  }
  
  /**
   * MetaObject name constant
   */
  export const META_OBJECT_NAME = '{{fullName}}';
```

### 6. React Component Template

**File: `templates/typescript/react-component.mustache.yaml`**

```yaml
name: "React Component Template"
version: "1.0.0"
description: "Generate React components for MetaObject forms"
targetLanguage: "typescript"
outputFileExtension: "tsx"
packagePath: false
requirements:
  helpers: ["camelCase", "capitalize", "typeScriptType", "getInputType"]
template: |
  import React, { useState, useEffect } from 'react';
  import { {{className}} } from '../types/{{className}}';
  {{#imports}}
  import { {{.}} } from '{{path}}';
  {{/imports}}
  
  interface {{className}}FormProps {
    initialData?: Partial<{{className}}>;
    onSubmit: (data: {{className}}) => void;
    onCancel?: () => void;
    readonly?: boolean;
  }
  
  /**
   * React form component for {{className}}
   * Generated from MetaObject: {{fullName}}
   */
  export const {{className}}Form: React.FC<{{className}}FormProps> = ({
    initialData = {},
    onSubmit,
    onCancel,
    readonly = false
  }) => {
    const [formData, setFormData] = useState<Partial<{{className}}>>(initialData);
    const [errors, setErrors] = useState<Record<string, string>>({});
    
    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      
      // Validate form data
      const validationErrors = validate{{className}}(formData as {{className}});
      if (validationErrors.length > 0) {
        const errorMap: Record<string, string> = {};
        validationErrors.forEach(error => {
          const fieldName = error.split(' ')[0];
          errorMap[fieldName] = error;
        });
        setErrors(errorMap);
        return;
      }
      
      setErrors({});
      onSubmit(formData as {{className}});
    };
    
    const handleInputChange = (field: keyof {{className}}, value: any) => {
      if (readonly) return;
      
      setFormData(prev => ({
        ...prev,
        [field]: value
      }));
      
      // Clear error for this field
      if (errors[field as string]) {
        setErrors(prev => ({
          ...prev,
          [field as string]: ''
        }));
      }
    };
    
    return (
      <form onSubmit={handleSubmit} className="{{#camelCase}}{{name}}{{/camelCase}}-form">
        <h2>{{className}} Form</h2>
        
        {{#fields}}
        {{^isIdField}}
        <div className="form-group">
          <label htmlFor="{{#camelCase}}{{name}}{{/camelCase}}">
            {{#capitalize}}{{name}}{{/capitalize}}
            {{^isOptional}}<span className="required">*</span>{{/isOptional}}
          </label>
          
          {{#isSelectField}}
          <select
            id="{{#camelCase}}{{name}}{{/camelCase}}"
            value={formData.{{#camelCase}}{{name}}{{/camelCase}} || ''}
            onChange={(e) => handleInputChange('{{#camelCase}}{{name}}{{/camelCase}}', e.target.value)}
            disabled={readonly}
            className={errors.{{#camelCase}}{{name}}{{/camelCase}} ? 'error' : ''}
          >
            <option value="">Select {{#capitalize}}{{name}}{{/capitalize}}</option>
            {{#selectOptions}}
            <option value="{{value}}">{{label}}</option>
            {{/selectOptions}}
          </select>
          {{/isSelectField}}
          
          {{#isTextArea}}
          <textarea
            id="{{#camelCase}}{{name}}{{/camelCase}}"
            value={formData.{{#camelCase}}{{name}}{{/camelCase}} || ''}
            onChange={(e) => handleInputChange('{{#camelCase}}{{name}}{{/camelCase}}', e.target.value)}
            disabled={readonly}
            className={errors.{{#camelCase}}{{name}}{{/camelCase}} ? 'error' : ''}
            rows={4}
          />
          {{/isTextArea}}
          
          {{^isSelectField}}
          {{^isTextArea}}
          <input
            type="{{getInputType}}"
            id="{{#camelCase}}{{name}}{{/camelCase}}"
            value={formData.{{#camelCase}}{{name}}{{/camelCase}} || ''}
            onChange={(e) => handleInputChange('{{#camelCase}}{{name}}{{/camelCase}}', e.target.value)}
            disabled={readonly}
            className={errors.{{#camelCase}}{{name}}{{/camelCase}} ? 'error' : ''}
          />
          {{/isTextArea}}
          {{/isSelectField}}
          
          {errors.{{#camelCase}}{{name}}{{/camelCase}} && (
            <span className="error-message">{errors.{{#camelCase}}{{name}}{{/camelCase}}}</span>
          )}
        </div>
        
        {{/isIdField}}
        {{/fields}}
        
        <div className="form-actions">
          {!readonly && (
            <button type="submit" className="btn btn-primary">
              Save {{className}}
            </button>
          )}
          
          {onCancel && (
            <button type="button" onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
          )}
        </div>
      </form>
    );
  };
  
  export default {{className}}Form;
```

## Advanced Template Features

### 7. Template with Partials

**File: `templates/java/complex-entity.mustache.yaml`**

```yaml
name: "Complex Entity with Partials"
version: "1.0.0"
description: "Demonstrates template partials for code reuse"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
partials:
  constructor: |
    // Default constructor
    public {{className}}() {}
    
    // Constructor with required fields
    public {{className}}({{#requiredFields}}{{javaType}} {{name}}{{^isLast}}, {{/isLast}}{{/requiredFields}}) {
        {{#requiredFields}}
        this.{{name}} = {{name}};
        {{/requiredFields}}
    }
  
  getters_setters: |
    {{#fields}}
    public {{javaType}} {{#capitalize}}get{{name}}{{/capitalize}}() {
        return this.{{name}};
    }
    
    public void {{#capitalize}}set{{name}}{{/capitalize}}({{javaType}} {{name}}) {
        this.{{name}} = {{name}};
    }
    
    {{/fields}}

template: |
  package {{packageName}};
  
  public class {{className}} {
      {{#fields}}
      private {{javaType}} {{name}};
      {{/fields}}
      
      {{>constructor}}
      
      {{>getters_setters}}
  }
```

### 8. Conditional Template Logic

**File: `templates/java/conditional-entity.mustache.yaml`**

```yaml
name: "Entity with Conditional Logic"
version: "1.0.0"
description: "Shows complex conditional logic in templates"
targetLanguage: "java"
outputFileExtension: "java"
packagePath: true
template: |
  package {{packageName}};
  
  {{#hasAuditing}}
  import java.time.LocalDateTime;
  {{/hasAuditing}}
  {{#hasJpa}}
  import javax.persistence.*;
  {{/hasJpa}}
  {{#hasValidation}}
  import javax.validation.constraints.*;
  {{/hasValidation}}
  
  {{#hasJpa}}
  @Entity
  @Table(name = "{{dbTableName}}")
  {{/hasJpa}}
  public class {{className}} {{#extendsClass}}extends {{extendsClass}}{{/extendsClass}} {{#implementsInterfaces}}implements {{#interfaces}}{{name}}{{^isLast}}, {{/isLast}}{{/interfaces}}{{/implementsInterfaces}} {
      
      {{#fields}}
      {{#isIdField}}
      {{#hasJpa}}
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      {{/hasJpa}}
      {{/isIdField}}
      {{#hasJpa}}
      @Column(name = "{{dbColumnName}}")
      {{/hasJpa}}
      {{#hasValidation}}
      {{#isRequired}}
      @NotNull
      {{/isRequired}}
      {{#hasLength}}
      @Size(max = {{maxLength}})
      {{/hasLength}}
      {{/hasValidation}}
      private {{javaType}} {{name}};
      
      {{/fields}}
      
      {{#hasAuditing}}
      @Column(name = "created_at")
      private LocalDateTime createdAt;
      
      @Column(name = "updated_at")  
      private LocalDateTime updatedAt;
      {{/hasAuditing}}
  }
```

## Testing Templates

### 9. Template Test Metadata

**File: `codegen/src/test/resources/template-test-metadata.json`**

```json
{
  "metadata": [
    {
      "type": "object",
      "name": "User",
      "package": "com.example.model",
      "attributes": {
        "dbTable": "users",
        "hasAuditing": "true",
        "hasJpa": "true",
        "hasValidation": "true"
      },
      "fields": [
        {
          "name": "id",
          "dataType": "long",
          "attributes": {
            "isId": "true",
            "dbColumn": "user_id"
          }
        },
        {
          "name": "username",
          "dataType": "string",
          "attributes": {
            "required": "true",
            "dbColumn": "username",
            "maxLength": "50",
            "isSearchable": "true"
          }
        },
        {
          "name": "email",
          "dataType": "string",
          "attributes": {
            "required": "true",
            "dbColumn": "email_address",
            "maxLength": "255",
            "isSearchable": "true"
          }
        },
        {
          "name": "age",
          "dataType": "int",
          "attributes": {
            "dbColumn": "user_age",
            "isOptional": "true"
          }
        },
        {
          "name": "status",
          "dataType": "string",
          "attributes": {
            "dbColumn": "status",
            "isSelectField": "true",
            "selectOptions": "[{\"value\":\"active\",\"label\":\"Active\"},{\"value\":\"inactive\",\"label\":\"Inactive\"}]"
          }
        }
      ]
    }
  ]
}
```

These templates demonstrate the full power of the Mustache + Helper Functions approach, showing how the same template structure can generate sophisticated code while remaining readable and maintainable across different programming languages.