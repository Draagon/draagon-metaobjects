# MetaObjects Examples

This module provides comprehensive examples demonstrating MetaObjects usage patterns across different scenarios and frameworks.

## 🎯 **Example Structure**

### **shared-resources**
Common metadata and templates used across all examples:
- **Metadata**: `examples-metadata.json` with User and Product objects
- **Templates**: Shared Mustache templates for code generation
- **Dependencies**: Used by all other example modules

### **basic-example** 
Core MetaObjects functionality without framework dependencies:
- **Metadata loading**: SimpleLoader with JSON parsing
- **Object creation**: ValueObject with MetaObject association
- **Field validation**: Metadata-driven validation patterns
- **Attribute access**: Reading field metadata and attributes

### **spring-example**
Spring Framework integration patterns:
- **Auto-configuration**: MetaDataAutoConfiguration for Spring Boot
- **Service injection**: MetaDataService, MetaDataLoader, MetaDataLoaderRegistry
- **Spring patterns**: Dependency injection and bean management
- **API testing**: Manual Spring integration verification

### **osgi-example**
OSGi bundle lifecycle and service discovery:
- **Service registry**: Auto-detection of OSGi vs standalone environments
- **Bundle lifecycle**: WeakHashMap cleanup patterns for bundle unloading
- **Registry patterns**: MetaDataLoaderRegistry for cross-bundle access
- **Resource management**: Proper cleanup and memory management

## 🚀 **Running the Examples**

### **Prerequisites**
- Java 21+
- Maven 3.9+

### **Build Examples**
```bash
# Build all examples
cd examples && mvn clean compile

# Or build specific example
cd examples/basic-example && mvn clean compile
```

### **Run Individual Examples**

#### **Basic Example**
```bash
cd examples/basic-example
mvn compile exec:java -Dexec.mainClass="com.draagon.meta.examples.basic.BasicMetaObjectsExample"
```

**What it demonstrates:**
- Loading metadata from JSON files
- Creating ValueObject instances
- Field validation using metadata
- Direct metadata access and attribute checking

#### **Spring Example**
```bash
cd examples/spring-example  
mvn compile exec:java -Dexec.mainClass="com.draagon.meta.examples.spring.SpringMetaObjectsExample"
```

**What it demonstrates:**
- Spring class loading verification
- MetaDataAutoConfiguration availability
- Spring integration patterns
- Manual Spring testing (bypasses Spring Boot complexities)

#### **OSGi Example**
```bash
cd examples/osgi-example
mvn compile exec:java -Dexec.mainClass="com.draagon.meta.examples.osgi.OSGiMetaObjectsExample"
```

**What it demonstrates:**
- ServiceRegistry auto-detection
- MetaDataLoaderRegistry patterns
- Bundle lifecycle simulation
- Cross-loader MetaObject discovery

## 📋 **Example Output**

### **Basic Example Output**
```
=== MetaObjects Basic Example ===

1. Loading metadata...
   Loaded 2 metadata items

2. Code generation capability available...
   (Code generation available via MustacheTemplateGenerator)

3. Working with metadata...
   Found User with package: com_example_model::User
   Fields: 4
     - id (long)
     - username (string)
     - email (string)  
     - createdDate (date)

4. Creating and validating objects...
   Created user: [ValueObject:com_example_model::User]{id:1,username:john_doe,email:john@example.com,createdDate:...}
   Validation result: VALID

5. Direct metadata access...
   Email field value: john@example.com
   Email field max length: 255

=== Example completed successfully ===
```

### **Spring Example Output**
```
=== Testing Spring Integration Manually ===

1. Manual Spring integration test...
   MetaDataAutoConfiguration class loaded: SUCCESS
   MetaDataService class loaded: SUCCESS
   MetaDataLoaderConfiguration class loaded: SUCCESS

2. Basic MetaObjects functionality...
   Loaded 2 metadata items
   Found User MetaObject: com_example_model::User
   User has 4 fields

=== Manual Spring integration test completed ===
```

### **OSGi Example Output**
```
=== MetaObjects OSGI Example Activated ===

1. OSGI Service Discovery...
   MetaDataLoaderRegistry created successfully
   ServiceRegistry type: StandardServiceRegistry

2. Creating and registering MetaDataLoader...
   Registered loader: osgiExample

3. Working with registered loaders...
   Found loader: osgiExample
     - MetaObject: com_example_model::User
     - MetaObject: com_example_model::Product

4. Bundle lifecycle patterns...
   Computed caches use WeakHashMap for bundle cleanup
   Registry handles 1 loaders

5. Environment detection...
   OSGI environment detected: false
   Service registry implementation: com.draagon.meta.registry.StandardServiceRegistry

=== MetaObject Lookup Demo ===
Found User MetaObject: com_example_model::User
Found Product MetaObject: com_example_model::Product

=== OSGI Example completed ===
```

## 📚 **Learning Path**

### **1. Start with Basic Example**
Understand core MetaObjects concepts:
- Metadata loading and parsing
- Object creation and manipulation
- Field validation and attributes

### **2. Explore Spring Example**
Learn framework integration:
- Spring auto-configuration
- Dependency injection patterns
- Service wrappers and convenience APIs

### **3. Examine OSGi Example**
Understand enterprise patterns:
- Service registry patterns
- Bundle lifecycle management
- Memory management and cleanup

## 🔧 **Common Patterns Demonstrated**

### **Metadata Loading**
```java
// Consistent pattern across examples
SimpleLoader loader = new SimpleLoader("example");
URI metadataUri = // ... various URI loading approaches
loader.setSourceURIs(Arrays.asList(metadataUri));
loader.init();
```

### **Object Creation**
```java
// MetaObject-associated ValueObject
MetaObject userMeta = loader.getMetaObjectByName("com_example_model::User");
ValueObject user = new ValueObject(userMeta);
user.put("id", 1L);
user.put("username", "john_doe");
```

### **Field Validation**
```java
// Metadata-driven validation
for (MetaField field : userMeta.getMetaFields()) {
    if (field.hasMetaAttr("required")) {
        boolean required = Boolean.parseBoolean(
            field.getMetaAttr("required").getValueAsString());
        // Validate field presence
    }
}
```

### **Registry Operations**
```java
// Cross-framework registry usage
MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(
    ServiceRegistryFactory.getDefault());
registry.registerLoader(loader);
MetaObject found = registry.findMetaObjectByName("com_example_model::User");
```

## 🛠️ **Customizing Examples**

### **Modifying Metadata**
Edit `shared-resources/src/main/resources/metadata/examples-metadata.json`:
```json
{
  "metadata": {
    "package": "com_example_model",
    "children": [
      {
        "object": {
          "name": "YourObject",
          "type": "pojo",
          "@dbTable": "your_table"
        }
      }
    ]
  }
}
```

### **Adding New Examples**
1. Create new module under `examples/`
2. Add dependency on `shared-resources`
3. Follow existing patterns for consistency
4. Update this README with new example

### **Framework Integration**
Examples demonstrate integration patterns for:
- **Pure Java**: No framework dependencies
- **Spring**: Dependency injection and auto-configuration
- **OSGi**: Bundle lifecycle and service discovery
- **Web**: (See web-spring module for React integration)

## 🔍 **Troubleshooting**

### **Common Issues**

#### **URI Loading Problems**
Examples use temporary file approach to handle jar: URI schemes:
```java
// Pattern used across examples
java.net.URL resourceUrl = getClass().getResource("/metadata/examples-metadata.json");
java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("examples-metadata", ".json");
try (java.io.InputStream is = resourceUrl.openStream()) {
    java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
}
```

#### **Package-Qualified Names**
MetaObjects use package-qualified names:
```java
// Correct lookup pattern
MetaObject userMeta = loader.getMetaObjectByName("com_example_model::User");
// NOT: loader.getMetaObjectByName("User")
```

#### **Attribute Type Warnings**
Some attributes may show type warnings in logs - this is expected for certain configurations and doesn't affect functionality.

## 🎯 **Next Steps**

After exploring the examples:
1. **Choose your integration**: Core, Spring, OSGi based on your needs
2. **Design your metadata**: Create JSON/XML metadata for your domain
3. **Generate code**: Use Maven plugin for code generation
4. **Build applications**: Leverage metadata-driven patterns in your projects

---

**These examples provide the foundation for building sophisticated metadata-driven applications with MetaObjects.**