# Quick Start Guide

Get up and running with MetaObjects in just a few minutes. This guide will walk you through creating your first metadata definition, loading it, and using it in your application.

## Prerequisites

- **Java 17 LTS** or higher
- **Maven 3.9+** (or Gradle with equivalent configuration)
- Basic knowledge of Java development

## Step 1: Add Dependencies

Choose the appropriate dependency based on your project type:

=== "Plain Java Project"

    ```xml title="pom.xml"
    <dependencies>
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-core</artifactId>
            <version>6.2.5-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```

=== "Spring Boot Project"

    ```xml title="pom.xml"
    <dependencies>
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-core-spring</artifactId>
            <version>6.2.5-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```

=== "Code Generation Only"

    ```xml title="pom.xml"
    <dependencies>
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-codegen-mustache</artifactId>
            <version>6.2.5-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <plugins>
        <plugin>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-maven-plugin</artifactId>
            <version>6.2.5-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals><goal>generate</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
    ```

## Step 2: Create Your First Metadata

Create a JSON metadata file that defines a simple User object:

```json title="src/main/resources/metadata/user-metadata.json"
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
                "name": "email",
                "type": "string",
                "@required": true,
                "@maxLength": 255,
                "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$",
                "@dbColumn": "email_address"
              }
            },
            {
              "field": {
                "name": "firstName",
                "type": "string",
                "@maxLength": 100,
                "@dbColumn": "first_name"
              }
            },
            {
              "field": {
                "name": "lastName",
                "type": "string",
                "@maxLength": 100,
                "@dbColumn": "last_name"
              }
            },
            {
              "field": {
                "name": "createdAt",
                "type": "date",
                "@dbColumn": "created_at"
              }
            }
          ]
        }
      }
    ]
  }
}
```

!!! tip "Inline Attributes"
    Notice the `@` prefix on attributes like `@required`, `@maxLength`, etc. This is MetaObjects' inline attribute syntax that makes metadata more readable and concise.

## Step 3: Load and Use Metadata

Create a simple Java application that loads and uses the metadata:

=== "Plain Java"

    ```java title="src/main/java/com/example/QuickStartExample.java"
    package com.example;

    import com.metaobjects.loader.simple.SimpleLoader;
    import com.metaobjects.object.MetaObject;
    import com.metaobjects.field.MetaField;
    import com.metaobjects.attr.MetaAttribute;

    import java.net.URI;
    import java.util.Arrays;

    public class QuickStartExample {

        public static void main(String[] args) throws Exception {
            // 1. Create and initialize the metadata loader
            SimpleLoader loader = new SimpleLoader("user-metadata");
            loader.setSourceURIs(Arrays.asList(
                URI.create("classpath:metadata/user-metadata.json")
            ));
            loader.init();

            // 2. Get the User metadata
            MetaObject userMeta = loader.getMetaObjectByName("User");
            System.out.println("Loaded metadata for: " + userMeta.getName());

            // 3. Explore the metadata structure
            System.out.println("Database table: " +
                userMeta.getMetaAttr("dbTable").getValueAsString());

            // 4. Examine fields
            System.out.println("\nFields:");
            for (MetaField field : userMeta.getChildren(MetaField.class)) {
                System.out.printf("  %s (%s)",
                    field.getName(),
                    field.getSubTypeName()
                );

                // Check for database column mapping
                if (field.hasMetaAttr("dbColumn")) {
                    System.out.printf(" -> %s",
                        field.getMetaAttr("dbColumn").getValueAsString()
                    );
                }

                // Check for validation attributes
                if (field.hasMetaAttr("required")) {
                    System.out.print(" [REQUIRED]");
                }

                if (field.hasMetaAttr("maxLength")) {
                    System.out.printf(" [MAX:%s]",
                        field.getMetaAttr("maxLength").getValueAsString()
                    );
                }

                System.out.println();
            }

            // 5. Access specific field metadata
            MetaField emailField = userMeta.getMetaField("email");
            if (emailField.hasMetaAttr("pattern")) {
                System.out.println("\nEmail validation pattern: " +
                    emailField.getMetaAttr("pattern").getValueAsString());
            }
        }
    }
    ```

=== "Spring Boot"

    ```java title="src/main/java/com/example/Application.java"
    package com.example;

    import com.metaobjects.object.MetaObject;
    import com.metaobjects.field.MetaField;
    import com.metaobjects.spring.MetaDataService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class Application implements CommandLineRunner {

        @Autowired
        private MetaDataService metaDataService;

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }

        @Override
        public void run(String... args) throws Exception {
            // MetaDataService automatically discovers and loads metadata
            MetaObject userMeta = metaDataService.getMetaObjectByName("User")
                .orElseThrow(() -> new RuntimeException("User metadata not found"));

            System.out.println("Loaded metadata for: " + userMeta.getName());

            // Use the metadata same as plain Java example above
            System.out.println("Database table: " +
                userMeta.getMetaAttr("dbTable").getValueAsString());

            // ... rest of exploration code
        }
    }
    ```

    ```yaml title="src/main/resources/application.yml"
    metaobjects:
      metadata:
        sources:
          - "classpath:metadata/user-metadata.json"
        auto-discovery: true
    ```

## Step 4: Run the Example

Run your application and you should see output like:

```
Loaded metadata for: User
Database table: users

Fields:
  id (long) -> user_id [REQUIRED]
  email (string) -> email_address [REQUIRED] [MAX:255]
  firstName (string) -> first_name [MAX:100]
  lastName (string) -> last_name [MAX:100]
  createdAt (date) -> created_at

Email validation pattern: ^[\w._%+-]+@[\w.-]+\.[A-Za-z]{2,}$
```

## What Just Happened?

Congratulations! You've just:

1. **Defined metadata** for a User object in JSON format
2. **Loaded the metadata** using MetaObjects' SimpleLoader
3. **Accessed metadata programmatically** to inspect object structure
4. **Used inline attributes** for validation and database mapping
5. **Explored the metadata** to understand field types and constraints

## Key Concepts Demonstrated

### :material-database: **Metadata Definition**
- **JSON format** with hierarchical structure
- **Inline attributes** using `@` prefix for concise syntax
- **Type system** with built-in field types (string, long, date)

### :material-cog: **Loader Pattern**
- **SimpleLoader** for JSON metadata files
- **URI-based** source specification
- **Classpath resource** loading

### :material-code-braces: **Runtime Access**
- **Type-safe** metadata exploration
- **Attribute access** for validation and mapping rules
- **Field iteration** and inspection

## Next Steps

Now that you have the basics working, explore these areas:

<div class="grid cards" markdown>

-   :material-school:{ .lg .middle } **Core Concepts**

    ---

    Understand the fundamental architecture and patterns

    [:octicons-arrow-right-24: Core Concepts](core-concepts.md)

-   :material-database:{ .lg .middle } **Metadata Guide**

    ---

    Learn about the complete metadata system

    [:octicons-arrow-right-24: Metadata Foundation](../user-guide/metadata/metadata-foundation.md)

-   :material-code-tags:{ .lg .middle } **Code Generation**

    ---

    Generate Java classes from your metadata

    [:octicons-arrow-right-24: Code Generation](../user-guide/codegen/generator-architecture.md)

-   :material-puzzle:{ .lg .middle } **Examples**

    ---

    Explore more comprehensive examples

    [:octicons-arrow-right-24: Examples](../examples/basic-usage.md)

</div>

## Common Issues

### ClassPath Resource Not Found

If you get `FileNotFoundException`, ensure your metadata file is in the correct location:

```
src/main/resources/metadata/user-metadata.json
```

### JSON Parsing Errors

Validate your JSON syntax and ensure:
- Proper escaping of regex patterns in `@pattern` attributes
- Correct nesting of `metadata` → `children` → object/field structures

### Missing Dependencies

If you get `ClassNotFoundException`, verify you have the correct MetaObjects dependencies for your use case.

---

Ready to dive deeper? Continue with [Core Concepts](core-concepts.md) to understand the architecture behind what you just built!