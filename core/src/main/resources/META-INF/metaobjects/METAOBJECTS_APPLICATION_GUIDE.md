# MetaObjects Application Development Guide

## 🚀 **COMPREHENSIVE GUIDE TO BUILDING METAOBJECTS APPLICATIONS**

This guide provides everything needed to build a complete application using the MetaObjects framework, from metadata design to database persistence.

### **Framework Version**: 6.2.6+
### **Architecture**: READ-OPTIMIZED WITH CONTROLLED MUTABILITY
### **Persistence**: ObjectManagerDB with Modern Identity System

---

## 📋 **TABLE OF CONTENTS**

1. [Quick Start Setup](#quick-start-setup)
2. [Project Structure](#project-structure)
3. [Metadata Design Patterns](#metadata-design-patterns)
4. [Identity & Relationship System](#identity--relationship-system)
5. [Code Generation Setup](#code-generation-setup)
6. [Database Integration](#database-integration)
7. [Complete Working Example](#complete-working-example)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

---

## 🏗️ **QUICK START SETUP**

### **1. Maven Dependencies**

**Core Dependencies (Required):**
```xml
<dependencies>
    <!-- Core MetaObjects Framework -->
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-core</artifactId>
        <version>6.2.6</version>
    </dependency>

    <!-- Database Persistence -->
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-omdb</artifactId>
        <version>6.2.6</version>
    </dependency>

    <!-- Code Generation (Build Time) -->
    <dependency>
        <groupId>com.metaobjects</groupId>
        <artifactId>metaobjects-codegen-mustache</artifactId>
        <version>6.2.6</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Optional Framework Integrations:**
```xml
<!-- Spring Integration (if using Spring) -->
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-core-spring</artifactId>
    <version>6.2.6</version>
</dependency>

<!-- Web Components (if building web UI) -->
<dependency>
    <groupId>com.metaobjects</groupId>
    <artifactId>metaobjects-web-spring</artifactId>
    <version>6.2.6</version>
</dependency>
```

### **2. Maven Plugin Configuration**

```xml
<build>
    <plugins>
        <!-- MetaObjects Code Generation Plugin -->
        <plugin>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-maven-plugin</artifactId>
            <version>6.2.6</version>
            <executions>
                <execution>
                    <id>generate-domain-objects</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <loader>
                            <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
                            <name>app-metadata</name>
                            <sourceURIs>
                                <sourceURI>classpath:metadata/application-metadata.json</sourceURI>
                            </sourceURIs>
                        </loader>
                        <generators>
                            <generator>
                                <classname>com.metaobjects.generator.mustache.java.JavaDomainObjectGenerator</classname>
                                <args>
                                    <outputDir>${project.basedir}/src/main/java</outputDir>
                                    <packageName>com.mycompany.domain</packageName>
                                </args>
                            </generator>
                        </generators>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Add generated sources to compilation -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <version>3.5.0</version>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>${project.basedir}/src/main/java</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 📁 **PROJECT STRUCTURE**

```
my-metaobjects-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mycompany/
│   │   │       ├── domain/           # Generated domain objects
│   │   │       ├── service/          # Business logic
│   │   │       ├── repository/       # Data access
│   │   │       └── Application.java  # Main application
│   │   └── resources/
│   │       ├── metadata/
│   │       │   ├── application-metadata.json  # Core metadata
│   │       │   └── database-overlay.json      # DB-specific attributes
│   │       ├── templates/            # Custom Mustache templates (optional)
│   │       └── application.properties
│   └── test/
│       └── java/
└── .claude/
    └── CLAUDE.md                     # This file for Claude Code assistance
```

---

## 🎯 **METADATA DESIGN PATTERNS**

### **Modern JSON Metadata Structure**

**File: `src/main/resources/metadata/application-metadata.json`**

```json
{
  "metadata": {
    "package": "com_mycompany_domain",
    "children": [
      {
        "object": {
          "name": "User",
          "subType": "managed",
          "description": "Application user with authentication",
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long",
                "description": "Unique user identifier"
              }
            },
            {
              "field": {
                "name": "username",
                "subType": "string",
                "@required": true,
                "@maxLength": 50,
                "@pattern": "^[a-zA-Z][a-zA-Z0-9_]*$"
              }
            },
            {
              "field": {
                "name": "email",
                "subType": "string",
                "@required": true,
                "@maxLength": 255,
                "@pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
              }
            },
            {
              "field": {
                "name": "firstName",
                "subType": "string",
                "@maxLength": 100
              }
            },
            {
              "field": {
                "name": "lastName",
                "subType": "string",
                "@maxLength": 100
              }
            },
            {
              "field": {
                "name": "createdAt",
                "subType": "timestamp",
                "@generation": "auto"
              }
            },
            {
              "field": {
                "name": "isActive",
                "subType": "boolean",
                "@defaultValue": "true"
              }
            },
            {
              "identity": {
                "name": "user_pk",
                "subType": "primary",
                "fields": ["id"],
                "@generation": "increment",
                "description": "Primary key for User table"
              }
            },
            {
              "identity": {
                "name": "username_uk",
                "subType": "secondary",
                "fields": ["username"],
                "description": "Unique constraint on username"
              }
            },
            {
              "identity": {
                "name": "email_uk",
                "subType": "secondary",
                "fields": ["email"],
                "description": "Unique constraint on email"
              }
            }
          ]
        }
      },
      {
        "object": {
          "name": "Order",
          "subType": "managed",
          "description": "Customer order with items",
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long"
              }
            },
            {
              "field": {
                "name": "userId",
                "subType": "long",
                "@required": true,
                "description": "Reference to User who placed order"
              }
            },
            {
              "field": {
                "name": "orderNumber",
                "subType": "string",
                "@required": true,
                "@maxLength": 20,
                "@pattern": "^ORD-[0-9]{10}$"
              }
            },
            {
              "field": {
                "name": "orderDate",
                "subType": "timestamp",
                "@generation": "auto"
              }
            },
            {
              "field": {
                "name": "totalAmount",
                "subType": "decimal",
                "@precision": 10,
                "@scale": 2,
                "@minValue": "0.00"
              }
            },
            {
              "field": {
                "name": "status",
                "subType": "string",
                "@required": true,
                "@maxLength": 20,
                "@defaultValue": "PENDING"
              }
            },
            {
              "identity": {
                "name": "order_pk",
                "subType": "primary",
                "fields": ["id"],
                "@generation": "increment"
              }
            },
            {
              "identity": {
                "name": "order_number_uk",
                "subType": "secondary",
                "fields": ["orderNumber"]
              }
            }
          ]
        }
      },
      {
        "object": {
          "name": "OrderItem",
          "subType": "managed",
          "description": "Individual item within an order",
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long"
              }
            },
            {
              "field": {
                "name": "orderId",
                "subType": "long",
                "@required": true
              }
            },
            {
              "field": {
                "name": "productName",
                "subType": "string",
                "@required": true,
                "@maxLength": 200
              }
            },
            {
              "field": {
                "name": "quantity",
                "subType": "int",
                "@required": true,
                "@minValue": 1
              }
            },
            {
              "field": {
                "name": "unitPrice",
                "subType": "decimal",
                "@precision": 10,
                "@scale": 2,
                "@minValue": "0.00"
              }
            },
            {
              "field": {
                "name": "lineTotal",
                "subType": "decimal",
                "@precision": 10,
                "@scale": 2,
                "@computed": "quantity * unitPrice"
              }
            },
            {
              "identity": {
                "name": "order_item_pk",
                "subType": "primary",
                "fields": ["id"],
                "@generation": "increment"
              }
            }
          ]
        }
      }
    ]
  }
}
```

---

## 🔗 **IDENTITY & RELATIONSHIP SYSTEM**

### **Modern Identity Architecture**

**MetaObjects uses a sophisticated Identity system (replaces deprecated MetaKey):**

#### **Identity Types**

1. **Primary Identity (`subType: "primary"`)**
   - **ONE per object** - main identifier for database persistence
   - **Required for ObjectManagerDB** - enables CRUD operations
   - **Generation strategies**: `increment`, `uuid`, `assigned`

2. **Secondary Identity (`subType: "secondary"`)**
   - **Multiple allowed** - business keys, unique constraints, indexes
   - **Used for queries** - alternate lookup patterns
   - **No generation** - typically assigned by application

#### **Identity Generation Strategies**

```json
{
  "identity": {
    "name": "user_pk",
    "subType": "primary",
    "fields": ["id"],
    "@generation": "increment",     // Auto-incrementing integer
    "description": "Database-generated primary key"
  }
}

{
  "identity": {
    "name": "session_pk",
    "subType": "primary",
    "fields": ["sessionId"],
    "@generation": "uuid",          // UUID/GUID generation
    "description": "UUID-based session identifier"
  }
}

{
  "identity": {
    "name": "lookup_pk",
    "subType": "primary",
    "fields": ["code"],
    "@generation": "assigned",      // Application assigns value
    "description": "Application-controlled identifier"
  }
}
```

#### **Composite Identities**

```json
{
  "identity": {
    "name": "user_role_pk",
    "subType": "primary",
    "fields": ["userId", "roleId"],  // Multiple fields
    "@generation": "assigned",       // Application controlled
    "description": "Composite primary key for junction table"
  }
}
```

### **Relationship Patterns**

#### **One-to-Many Relationship**

```json
{
  "object": {
    "name": "User",
    "children": [
      {
        "relationship": {
          "name": "orders",
          "subType": "reference",
          "description": "Orders placed by this user",
          "@targetObject": "Order",
          "@cardinality": "one-to-many",
          "@sourceIdentity": "user_pk",
          "@targetField": "userId"
        }
      }
    ]
  }
}
```

#### **Many-to-One Relationship**

```json
{
  "object": {
    "name": "Order",
    "children": [
      {
        "relationship": {
          "name": "user",
          "subType": "reference",
          "description": "User who placed this order",
          "@targetObject": "User",
          "@cardinality": "many-to-one",
          "@sourceField": "userId",
          "@targetIdentity": "user_pk"
        }
      }
    ]
  }
}
```

#### **One-to-Many with Cascade**

```json
{
  "object": {
    "name": "Order",
    "children": [
      {
        "relationship": {
          "name": "orderItems",
          "subType": "reference",
          "description": "Items in this order",
          "@targetObject": "OrderItem",
          "@cardinality": "one-to-many",
          "@sourceIdentity": "order_pk",
          "@targetField": "orderId",
          "@cascade": "delete"
        }
      }
    ]
  }
}
```

---

## ⚙️ **CODE GENERATION SETUP**

### **Mustache Template Configuration**

**Built-in Templates**: MetaObjects includes production-ready Mustache templates for:
- **Java Domain Objects** with JPA annotations
- **Spring Repository interfaces**
- **REST Controllers** with CRUD operations
- **TypeScript interfaces** for frontend
- **Database DDL scripts**

#### **Custom Template Override (Optional)**

**File: `src/main/resources/templates/java-domain-object.mustache`**

```mustache
package {{packageName}};

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * {{description}}
 * Generated by MetaObjects v6.2.6
 */
@Entity
@Table(name = "{{dbTable}}")
public class {{name}} {

    {{#fields}}
    {{#isIdField}}
    @Id
    {{#hasGeneration}}
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    {{/hasGeneration}}
    {{/isIdField}}
    @Column(name = "{{dbColumn}}"{{#hasMaxLength}}, length = {{maxLength}}{{/hasMaxLength}}{{#isRequired}}, nullable = false{{/isRequired}})
    {{#hasValidation}}
    {{#isRequired}}
    @NotNull
    {{/isRequired}}
    {{#hasMaxLength}}
    @Size(max = {{maxLength}})
    {{/hasMaxLength}}
    {{#hasPattern}}
    @Pattern(regexp = "{{pattern}}")
    {{/hasPattern}}
    {{/hasValidation}}
    private {{javaType}} {{name}};

    {{/fields}}

    // Constructors
    public {{name}}() {}

    {{#fields}}
    // {{description}}
    public {{javaType}} get{{capitalizedName}}() {
        return {{name}};
    }

    public void set{{capitalizedName}}({{javaType}} {{name}}) {
        this.{{name}} = {{name}};
    }

    {{/fields}}

    // equals and hashCode based on identity
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        {{name}} that = ({{name}}) obj;
        return Objects.equals({{primaryKeyField}}, that.{{primaryKeyField}});
    }

    @Override
    public int hashCode() {
        return Objects.hash({{primaryKeyField}});
    }

    @Override
    public String toString() {
        return "{{name}}{" +
            {{#fields}}
            "{{name}}=" + {{name}} +
            {{/fields}}
            '}';
    }
}
```

### **Generation Execution**

```bash
# Generate domain objects
mvn metaobjects:generate

# Or integrate with build process
mvn clean compile
```

---

## 🗄️ **DATABASE INTEGRATION**

### **Database Overlay Configuration**

**File: `src/main/resources/metadata/database-overlay.json`**

```json
{
  "metadata": {
    "package": "com_mycompany_domain",
    "children": [
      {
        "object": {
          "name": "User",
          "@dbTable": "users",
          "children": [
            {
              "field": {
                "name": "id",
                "@dbColumn": "user_id"
              }
            },
            {
              "field": {
                "name": "username",
                "@dbColumn": "username"
              }
            },
            {
              "field": {
                "name": "email",
                "@dbColumn": "email_address"
              }
            },
            {
              "field": {
                "name": "firstName",
                "@dbColumn": "first_name"
              }
            },
            {
              "field": {
                "name": "lastName",
                "@dbColumn": "last_name"
              }
            },
            {
              "field": {
                "name": "createdAt",
                "@dbColumn": "created_at"
              }
            },
            {
              "field": {
                "name": "isActive",
                "@dbColumn": "is_active"
              }
            },
            {
              "identity": {
                "name": "user_pk",
                "@dbIndexName": "pk_users"
              }
            },
            {
              "identity": {
                "name": "username_uk",
                "@dbIndexName": "uk_users_username"
              }
            },
            {
              "identity": {
                "name": "email_uk",
                "@dbIndexName": "uk_users_email"
              }
            }
          ]
        }
      },
      {
        "object": {
          "name": "Order",
          "@dbTable": "orders",
          "children": [
            {
              "field": {
                "name": "id",
                "@dbColumn": "order_id"
              }
            },
            {
              "field": {
                "name": "userId",
                "@dbColumn": "user_id"
              }
            },
            {
              "field": {
                "name": "orderNumber",
                "@dbColumn": "order_number"
              }
            },
            {
              "field": {
                "name": "orderDate",
                "@dbColumn": "order_date"
              }
            },
            {
              "field": {
                "name": "totalAmount",
                "@dbColumn": "total_amount"
              }
            },
            {
              "field": {
                "name": "status",
                "@dbColumn": "order_status"
              }
            },
            {
              "identity": {
                "name": "order_pk",
                "@dbIndexName": "pk_orders"
              }
            },
            {
              "identity": {
                "name": "order_number_uk",
                "@dbIndexName": "uk_orders_number"
              }
            }
          ]
        }
      }
    ]
  }
}
```

### **ObjectManagerDB Configuration**

#### **Application Setup**

```java
package com.mycompany.config;

import com.metaobjects.manager.db.ObjectManagerDB;
import com.metaobjects.manager.db.driver.*;
import com.metaobjects.manager.db.validator.MetaClassDBValidatorService;
import com.metaobjects.loader.file.FileMetaDataLoader;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.registry.ServiceRegistryFactory;

import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource; // or your preferred database

@Configuration
public class MetaObjectsConfiguration {

    @Bean
    public FileMetaDataLoader metaDataLoader() throws Exception {
        FileMetaDataLoader loader = new FileMetaDataLoader("app-metadata");

        // Load core application metadata
        loader.addSourceURI(getClass().getResource("/metadata/application-metadata.json").toURI());

        // Load database overlay
        loader.addSourceURI(getClass().getResource("/metadata/database-overlay.json").toURI());

        loader.init();
        return loader;
    }

    @Bean
    public DataSource dataSource() {
        // Example with Derby - replace with your database
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("myapp");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }

    @Bean
    public ObjectManagerDB objectManagerDB(DataSource dataSource) {
        ObjectManagerDB objectManager = new ObjectManagerDB();

        // Configure database driver (Derby example)
        objectManager.setDriverClass("com.metaobjects.manager.db.driver.DerbyDriver");
        objectManager.setDataSource(dataSource);

        return objectManager;
    }

    @Bean
    public MetaClassDBValidatorService dbValidator(
            ObjectManagerDB objectManagerDB,
            FileMetaDataLoader metaDataLoader) {

        MetaClassDBValidatorService validator = new MetaClassDBValidatorService();
        validator.setObjectManager(objectManagerDB);
        validator.setAutoCreate(true); // Automatically create missing tables

        // Connect to metadata loader
        MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        registry.registerLoader(metaDataLoader);
        validator.setMetaDataLoaderRegistry(registry);

        return validator;
    }
}
```

#### **Service Layer with ObjectManagerDB**

```java
package com.mycompany.service;

import com.metaobjects.manager.db.ObjectManagerDB;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.object.ValueMetaObject;
import com.metaobjects.MetaObject;
import com.metaobjects.util.MetaDataUtil;

@Service
@Transactional
public class UserService {

    private final ObjectManagerDB objectManager;
    private final MetaObject userMetaObject;

    public UserService(ObjectManagerDB objectManager) throws Exception {
        this.objectManager = objectManager;
        this.userMetaObject = MetaDataUtil.findMetaObjectByName("User", this);
    }

    public ValueMetaObject createUser(String username, String email, String firstName, String lastName)
            throws Exception {

        try (ObjectConnection connection = objectManager.getConnection()) {
            // Create new user object
            ValueMetaObject user = new ValueMetaObject(userMetaObject);

            // Set field values
            user.setString("username", username);
            user.setString("email", email);
            user.setString("firstName", firstName);
            user.setString("lastName", lastName);
            user.setBoolean("isActive", true);
            user.setTimestamp("createdAt", LocalDateTime.now());

            // Persist to database (ID auto-generated)
            objectManager.createObject(connection, user);

            return user;
        }
    }

    public ValueMetaObject findUserById(Long userId) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            ValueMetaObject user = new ValueMetaObject(userMetaObject);
            user.setLong("id", userId);

            return objectManager.loadObject(connection, user);
        }
    }

    public ValueMetaObject findUserByUsername(String username) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            // Use secondary identity for lookup
            ValueMetaObject user = new ValueMetaObject(userMetaObject);
            user.setString("username", username);

            return objectManager.loadObject(connection, user, "username_uk");
        }
    }

    public List<ValueMetaObject> findActiveUsers() throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            String whereClause = "is_active = ?";
            Object[] parameters = {true};

            return objectManager.loadObjects(connection, userMetaObject, whereClause, parameters);
        }
    }

    public void updateUser(ValueMetaObject user) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            objectManager.updateObject(connection, user);
        }
    }

    public void deleteUser(Long userId) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            ValueMetaObject user = new ValueMetaObject(userMetaObject);
            user.setLong("id", userId);

            objectManager.deleteObject(connection, user);
        }
    }
}
```

#### **Relationship Handling**

```java
@Service
@Transactional
public class OrderService {

    private final ObjectManagerDB objectManager;
    private final MetaObject orderMetaObject;
    private final MetaObject orderItemMetaObject;

    public OrderService(ObjectManagerDB objectManager) throws Exception {
        this.objectManager = objectManager;
        this.orderMetaObject = MetaDataUtil.findMetaObjectByName("Order", this);
        this.orderItemMetaObject = MetaDataUtil.findMetaObjectByName("OrderItem", this);
    }

    @Transactional
    public ValueMetaObject createOrderWithItems(Long userId, String orderNumber,
            List<OrderItemData> items) throws Exception {

        try (ObjectConnection connection = objectManager.getConnection()) {
            // Create order
            ValueMetaObject order = new ValueMetaObject(orderMetaObject);
            order.setLong("userId", userId);
            order.setString("orderNumber", orderNumber);
            order.setTimestamp("orderDate", LocalDateTime.now());
            order.setString("status", "PENDING");

            // Calculate total
            BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setDecimal("totalAmount", total);

            // Save order (gets auto-generated ID)
            objectManager.createObject(connection, order);
            Long orderId = order.getLong("id");

            // Create order items
            for (OrderItemData itemData : items) {
                ValueMetaObject orderItem = new ValueMetaObject(orderItemMetaObject);
                orderItem.setLong("orderId", orderId);
                orderItem.setString("productName", itemData.getProductName());
                orderItem.setInt("quantity", itemData.getQuantity());
                orderItem.setDecimal("unitPrice", itemData.getUnitPrice());
                orderItem.setDecimal("lineTotal",
                    itemData.getUnitPrice().multiply(new BigDecimal(itemData.getQuantity())));

                objectManager.createObject(connection, orderItem);
            }

            return order;
        }
    }

    public List<ValueMetaObject> findOrdersForUser(Long userId) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            String whereClause = "user_id = ? ORDER BY order_date DESC";
            Object[] parameters = {userId};

            return objectManager.loadObjects(connection, orderMetaObject, whereClause, parameters);
        }
    }

    public List<ValueMetaObject> findOrderItemsForOrder(Long orderId) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            String whereClause = "order_id = ?";
            Object[] parameters = {orderId};

            return objectManager.loadObjects(connection, orderItemMetaObject, whereClause, parameters);
        }
    }
}
```

---

## 💼 **COMPLETE WORKING EXAMPLE**

### **Main Application Class**

```java
package com.mycompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MyMetaObjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMetaObjectsApplication.class, args);
    }
}
```

### **REST Controller Example**

```java
package com.mycompany.controller;

import com.mycompany.service.UserService;
import com.metaobjects.object.ValueMetaObject;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ValueMetaObject createUser(@RequestBody CreateUserRequest request) throws Exception {
        return userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName()
        );
    }

    @GetMapping("/{id}")
    public ValueMetaObject getUser(@PathVariable Long id) throws Exception {
        return userService.findUserById(id);
    }

    @GetMapping
    public List<ValueMetaObject> getActiveUsers() throws Exception {
        return userService.findActiveUsers();
    }

    @PutMapping("/{id}")
    public ValueMetaObject updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request)
            throws Exception {
        ValueMetaObject user = userService.findUserById(id);

        if (request.getFirstName() != null) {
            user.setString("firstName", request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setString("lastName", request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setString("email", request.getEmail());
        }

        userService.updateUser(user);
        return user;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) throws Exception {
        userService.deleteUser(id);
    }
}
```

### **Application Properties**

```properties
# Database Configuration (H2 Example)
spring.datasource.url=jdbc:h2:mem:metaobjects-app
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# H2 Console (Development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# MetaObjects Configuration
metaobjects.auto-create-tables=true
metaobjects.validate-schema=true

# Logging
logging.level.com.metaobjects=DEBUG
logging.level.com.mycompany=DEBUG
```

---

## 🏆 **BEST PRACTICES**

### **1. Metadata Organization**

✅ **DO:**
- Use meaningful package names (`com_mycompany_domain`)
- Separate core metadata from database-specific overlays
- Include comprehensive descriptions for all objects and fields
- Use consistent naming conventions

❌ **DON'T:**
- Mix application logic with metadata definitions
- Use spaces or special characters in metadata names
- Create overly complex inheritance hierarchies

### **2. Identity Design**

✅ **DO:**
- Always include one primary identity per managed object
- Use `increment` generation for most primary keys
- Create secondary identities for business keys and unique constraints
- Use descriptive identity names (`user_pk`, `email_uk`)

❌ **DON'T:**
- Create objects without primary identities
- Use composite primary keys unless absolutely necessary
- Forget to specify generation strategies

### **3. Field Design**

✅ **DO:**
- Use appropriate field types (`decimal` for money, `timestamp` for dates)
- Include validation constraints (`@required`, `@maxLength`, `@pattern`)
- Provide default values where appropriate
- Use meaningful field names

❌ **DON'T:**
- Use `string` for everything
- Skip validation constraints
- Use overly long field names

### **4. Database Integration**

✅ **DO:**
- Use database overlays for DB-specific attributes
- Enable auto-table creation in development
- Use connection pooling in production
- Implement proper transaction boundaries

❌ **DON'T:**
- Put database attributes in core metadata
- Skip database validation setup
- Ignore connection lifecycle management

### **5. Code Generation**

✅ **DO:**
- Keep generated code in version control
- Customize templates for your coding standards
- Regenerate after metadata changes
- Use build automation

❌ **DON'T:**
- Manually edit generated code
- Skip regeneration after metadata updates
- Mix generated and hand-written code in same files

### **6. Performance Optimization**

✅ **DO:**
- Cache MetaDataLoader instances (they're heavy to create)
- Use connection pooling for database access
- Batch operations when possible
- Monitor query performance

❌ **DON'T:**
- Create new MetaDataLoader instances frequently
- Ignore connection leaks
- Perform N+1 queries

---

## 🔧 **TROUBLESHOOTING**

### **Common Issues & Solutions**

#### **1. "MetaData not found" Errors**
```
Caused by: MetaDataNotFoundException: No MetaObject found with name 'User'
```

**Solution:**
- Verify metadata file is in classpath (`src/main/resources/metadata/`)
- Check package name matches (`com_mycompany_domain::User`)
- Ensure FileMetaDataLoader is properly configured and initialized

#### **2. "Non-resolvable parent POM" During Build**
```
Non-resolvable parent POM for com.metaobjects:metaobjects-examples:6.2.6-SNAPSHOT
```

**Solution:**
- Add `<relativePath>../pom.xml</relativePath>` to parent POM references
- Verify MetaObjects version matches across all dependencies

#### **3. Identity Generation Issues**
```
SQLException: Attempt to modify an identity column 'ID'
```

**Solution:**
- Ensure primary identity has `@generation` attribute
- Use `increment` for auto-generated database IDs
- Don't manually set ID values for auto-generated fields

#### **4. Database Connection Issues**
```
Unable to find database driver for: com.metaobjects.manager.db.driver.H2Driver
```

**Solution:**
- Add database driver dependency to Maven
- Verify driver class name matches your database
- Check database URL and credentials

#### **5. Code Generation Template Issues**
```
Unable to resolve template: java-domain-object.mustache
```

**Solution:**
- Verify template exists in `src/main/resources/templates/`
- Check template name in generator configuration
- Use built-in templates first, then customize

### **Debug Configuration**

```properties
# Enable detailed MetaObjects logging
logging.level.com.metaobjects=DEBUG
logging.level.com.metaobjects.loader=TRACE
logging.level.com.metaobjects.manager=TRACE

# SQL logging for database debugging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## 📚 **ADDITIONAL RESOURCES**

### **Framework Documentation**
- **Official Docs**: https://docs.metaobjects.com
- **GitHub Repository**: https://github.com/metaobjectsdev/metaobjects-core
- **Maven Central**: https://central.sonatype.com/artifact/com.metaobjects/metaobjects-core

### **Community Examples**
- **Sample Applications**: https://github.com/metaobjectsdev/metaobjects-examples
- **Tutorial Videos**: https://youtube.com/metaobjects
- **Community Forum**: https://discuss.metaobjects.com

### **Migration Guides**
- **v5.x → v6.x Migration**: See `MIGRATION.md` in project root
- **MetaKey → MetaIdentity**: See architecture documentation

---

## 🤝 **GETTING HELP**

### **Claude Code Integration**
This guide is optimized for Claude Code assistance. When asking Claude for help:

✅ **Include context**: "I'm building a MetaObjects application..."
✅ **Reference this guide**: "Following the MetaObjects application guide..."
✅ **Be specific**: "My metadata defines User object with primary identity..."
✅ **Share relevant files**: Include metadata JSON and configuration

### **Support Channels**
- **GitHub Issues**: Bug reports and feature requests
- **Discussion Forum**: Architecture questions and best practices
- **Commercial Support**: Enterprise consulting available

---

**This guide covers the complete MetaObjects application development lifecycle. Follow the patterns and examples provided for production-ready applications with proper metadata design, code generation, and database persistence.**

---

## 📋 **METADATA REFERENCE QUICK CARD**

### **Core Field Types**
- `string` - Text data with length/pattern validation
- `int` - Integer numbers (-2B to +2B range)
- `long` - Long integers (large range)
- `decimal` - High precision numbers (financial)
- `double` - Floating point numbers
- `boolean` - True/false values
- `timestamp` - Date and time
- `date` - Date only
- `time` - Time only

### **Identity Patterns**
```json
{"identity": {"name": "pk", "subType": "primary", "fields": ["id"], "@generation": "increment"}}
{"identity": {"name": "uk", "subType": "secondary", "fields": ["code"]}}
```

### **Relationship Patterns**
```json
{"relationship": {"name": "items", "subType": "reference", "@targetObject": "Item", "@cardinality": "one-to-many"}}
```

### **Validation Attributes**
- `@required` - Field cannot be null/empty
- `@maxLength` - Maximum string length
- `@minLength` - Minimum string length
- `@pattern` - Regular expression validation
- `@minValue` - Minimum numeric value
- `@maxValue` - Maximum numeric value
- `@defaultValue` - Default field value

### **Database Attributes**
- `@dbTable` - Database table name
- `@dbColumn` - Database column name
- `@dbIndexName` - Database index name
- `@generation` - ID generation strategy

---

**🚀 Ready to build amazing applications with MetaObjects! 🚀**