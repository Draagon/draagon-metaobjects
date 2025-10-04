# MetaObjects Documentation

Welcome to the comprehensive documentation for **MetaObjects**, a sophisticated framework for metadata-driven development that provides unprecedented control over your applications beyond traditional model-driven development techniques.

<div class="grid cards" markdown>

-   :material-rocket-launch:{ .lg .middle } **Quick Start**

    ---

    Get up and running with MetaObjects in minutes with our step-by-step guide

    [:octicons-arrow-right-24: Getting Started](getting-started/quick-start.md)

-   :material-cog:{ .lg .middle } **User Guide**

    ---

    Comprehensive documentation covering all MetaObjects features and modules

    [:octicons-arrow-right-24: User Guide](user-guide/metadata/metadata-foundation.md)

-   :material-code-braces:{ .lg .middle } **API Reference**

    ---

    Complete API documentation for all MetaObjects modules and classes

    [:octicons-arrow-right-24: API Reference](api-reference/metadata.md)

-   :material-puzzle:{ .lg .middle } **Examples**

    ---

    Working examples and tutorials for common use cases and integration patterns

    [:octicons-arrow-right-24: Examples](examples/basic-usage.md)

</div>

## What is MetaObjects?

MetaObjects is a comprehensive suite of tools designed for **metadata-driven development**, offering:

### :material-database: **Metadata-First Architecture**

Define your object structures, validation rules, and relationships through rich metadata definitions rather than scattered annotations or configuration files.

```json
{
  "metadata": {
    "children": [
      {
        "object": {
          "name": "User",
          "type": "pojo",
          "@dbTable": "users",
          "children": [
            {
              "field": {
                "name": "email",
                "type": "string",
                "@required": true,
                "@maxLength": 255,
                "@pattern": "^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"
              }
            }
          ]
        }
      }
    ]
  }
}
```

### :material-code-tags: **Cross-Language Code Generation**

Generate type-safe code for multiple languages from a single metadata definition:

- **Java**: POJOs, JPA entities, builders, validators
- **TypeScript**: Interfaces, classes, React components
- **C#**: Classes, entities, validation attributes
- **SQL**: Database schemas, constraints, indexes

### :material-spring: **Framework Integration**

Native support for popular frameworks without forcing dependencies:

=== "Spring Boot"

    ```xml
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-core-spring</artifactId>
        <version>6.2.6-SNAPSHOT</version>
    </dependency>
    ```

=== "Plain Java"

    ```xml
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-core</artifactId>
        <version>6.2.6-SNAPSHOT</version>
    </dependency>
    ```

=== "OSGi Bundle"

    ```xml
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-metadata</artifactId>
        <version>6.2.6-SNAPSHOT</version>
    </dependency>
    ```

### :material-shield-check: **Type-Safe Constraints**

Comprehensive validation and constraint system that enforces rules at metadata definition time:

- **Naming patterns** and identifier validation
- **Required attributes** and data type enforcement
- **Uniqueness constraints** and relationship validation
- **Custom business rules** through extensible constraint providers

## Key Features

### :material-lightning-bolt: **Performance Optimized**

Built on a **READ-OPTIMIZED WITH CONTROLLED MUTABILITY** pattern analogous to Java's Class/Field reflection system:

- **Load once** during application startup (100ms-1s)
- **Read thousands of times** with microsecond performance (1-10μs)
- **Thread-safe** concurrent access without synchronization overhead
- **Memory efficient** with intelligent caching strategies (10-50MB)

### :material-puzzle-outline: **Modular Architecture**

Independent modules that can be used individually or combined as needed:

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| `metaobjects-metadata` | Core metadata definitions | None |
| `metaobjects-core` | File loading and parsing | metadata |
| `metaobjects-codegen-*` | Code generation engines | metadata, core |
| `metaobjects-*-spring` | Spring integrations | Optional |

### :material-power-plug: **Extensible Plugin System**

Create custom field types, validators, and generators through a clean provider-based registration system:

```java
public class CurrencyField extends PrimitiveField<BigDecimal> {
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(CurrencyField.class, def -> def
            .type("field").subType("currency")
            .inheritsFrom("field", "base")
            .optionalAttribute("precision", "int")
            .optionalAttribute("currencyCode", "string")
        );
    }
}
```

## Architecture Highlights

### :material-memory: **Memory Model**

MetaObjects follows a **ClassLoader pattern** where metadata is loaded once at startup and remains in memory for the application lifetime, similar to how `java.lang.Class` objects work.

### :material-sync: **OSGI Compatible**

Full support for OSGi bundle lifecycle with proper cleanup patterns:

- **ServiceLoader discovery** for dynamic service registration
- **WeakReference patterns** for bundle unloading cleanup
- **Service registry integration** for enterprise environments

### :material-speedometer: **Concurrent Read Optimization**

Designed for high-throughput applications with massive concurrent read access:

- **Immutable after loading** - no synchronization needed for reads
- **ConcurrentHashMap** for high-frequency lookups
- **Lock-free algorithms** in runtime read paths

## Getting Started

Ready to experience metadata-driven development? Choose your path:

<div class="grid cards" markdown>

-   :material-school:{ .lg .middle } **New to MetaObjects?**

    ---

    Start with our comprehensive getting started guide

    [:octicons-arrow-right-24: Quick Start Guide](getting-started/quick-start.md)

-   :material-spring:{ .lg .middle } **Spring Developer?**

    ---

    Jump into Spring-specific integration patterns

    [:octicons-arrow-right-24: Spring Integration](developer-guide/integration/spring-integration.md)

-   :material-code-tags:{ .lg .middle } **Code Generation?**

    ---

    Learn about template-based code generation

    [:octicons-arrow-right-24: Code Generation](user-guide/codegen/generator-architecture.md)

-   :material-puzzle:{ .lg .middle } **Plugin Development?**

    ---

    Extend MetaObjects with custom types and generators

    [:octicons-arrow-right-24: Plugin Guide](developer-guide/plugins/creating-extensions.md)

</div>

## MetaObjects Ecosystem

This documentation covers the **core platform**. For the complete MetaObjects experience:

<div class="grid cards" markdown>

-   :material-web:{ .lg .middle } **MetaObjects Platform**

    ---

    Enterprise solutions, case studies, and business context

    [:octicons-arrow-right-24: metaobjects.com](https://metaobjects.com)

-   :material-account-supervisor:{ .lg .middle } **Developer Portal**

    ---

    Community hub, tutorials, and developer resources

    [:octicons-arrow-right-24: metaobjects.dev](https://metaobjects.dev)

-   :material-account:{ .lg .middle } **About the Creator**

    ---

    Meet Doug Mealing, architect of the MetaObjects framework

    [:octicons-arrow-right-24: dougmealing.com](https://dougmealing.com)

</div>

## Community & Support

- **GitHub Repository**: [metaobjectsdev/metaobjects-core](https://github.com/metaobjectsdev/metaobjects-core)
- **Issue Tracker**: Report bugs and request features
- **Discussions**: Community support and questions
- **License**: Apache License 2.0

---

*MetaObjects is actively developed and maintained with comprehensive modernization completed in 2024-2025, including security hardening, Java 17 LTS migration, and complete code quality modernization.*