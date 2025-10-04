# MetaObjects Application Guide: ${artifactId}

## 🎯 **YOUR PROJECT OVERVIEW**

**Generated Project**: `${groupId}.${artifactId}`
**Package**: `${package}`
**Version**: `${version}`
**Framework**: MetaObjects v6.2.6 with Spring Boot 3.2.0

This project was generated using the MetaObjects application archetype and includes everything needed to build a complete metadata-driven application with database persistence.

---

## 🏗️ **YOUR PROJECT STRUCTURE**

```
${artifactId}/
├── .claude/
│   └── CLAUDE.md                          # This comprehensive guide
├── pom.xml                                # Complete Maven configuration
├── src/main/
│   ├── java/${packageInPathFormat}/
│   │   ├── ${artifactId}Application.java        # Spring Boot application
│   │   ├── config/
│   │   │   └── MetaObjectsConfiguration.java    # MetaObjects setup
│   │   ├── domain/                               # Generated domain objects
│   │   │   ├── User.java                         # Generated from metadata
│   │   │   ├── Order.java                        # Generated from metadata
│   │   │   └── OrderItem.java                    # Generated from metadata
│   │   ├── service/                              # Business logic layer
│   │   │   ├── UserService.java                 # User CRUD operations
│   │   │   └── OrderService.java                # Order CRUD operations
│   │   ├── controller/                           # REST API layer
│   │   │   ├── UserController.java              # User REST endpoints
│   │   │   └── OrderController.java             # Order REST endpoints
│   │   └── dto/                                  # Data transfer objects
│   │       ├── CreateUserRequest.java           # User creation DTO
│   │       ├── UpdateUserRequest.java           # User update DTO
│   │       └── OrderItemData.java               # Order item DTO
│   └── resources/
│       ├── application.properties                # Spring configuration
│       ├── metadata/                             # MetaObjects metadata
│       │   ├── application-metadata.json        # YOUR CORE DOMAIN MODEL
│       │   └── database-overlay.json            # YOUR DATABASE MAPPING
│       └── templates/                            # Custom Mustache templates
│           └── java-domain-object.mustache      # Domain object template
└── src/test/
    └── java/${packageInPathFormat}/
        ├── ${artifactId}ApplicationTest.java    # Application tests
        ├── service/                              # Service layer tests
        │   ├── UserServiceTest.java             # User service tests
        │   └── OrderServiceTest.java            # Order service tests
        └── integration/                          # Integration tests
            └── DatabaseIntegrationTest.java     # Database integration tests
```

---

## 🚀 **GETTING STARTED WITH YOUR PROJECT**

### **1. Generate Domain Objects from Metadata**
```bash
cd ${artifactId}
mvn metaobjects:generate
```

### **2. Compile and Run Your Application**
```bash
mvn spring-boot:run
```

### **3. Test Your Generated REST API**
```bash
# List all users
curl http://localhost:8080/api/users

# Create a new user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","firstName":"John","lastName":"Doe"}'

# Get user by ID
curl http://localhost:8080/api/users/1

# List all orders
curl http://localhost:8080/api/orders
```

### **4. Access H2 Database Console**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:${artifactId}
Username: sa
Password: password
```

---

## 📋 **YOUR METADATA FILES**

### **Core Domain Model**: `src/main/resources/metadata/application-metadata.json`

This file defines your User, Order, and OrderItem entities with proper MetaIdentity patterns:

- **User Entity**: Primary identity with auto-increment, secondary identities for username/email uniqueness
- **Order Entity**: Primary identity, relationship to User, proper decimal handling for amounts
- **OrderItem Entity**: Primary identity, relationship to Order, computed line totals

### **Database Mapping**: `src/main/resources/metadata/database-overlay.json`

This file maps your entities to H2 database tables:

- **Table Names**: users, orders, order_items
- **Column Mappings**: Proper snake_case database columns
- **Index Names**: Explicit index naming for performance

---

## 🔧 **CUSTOMIZING YOUR PROJECT**

### **Adding a New Entity (Example: Product)**

**Step 1**: Add Product metadata to `application-metadata.json`:
```json
{
  "object": {
    "name": "Product",
    "subType": "managed",
    "description": "Product catalog item",
    "children": [
      {
        "field": {
          "name": "id",
          "subType": "long"
        }
      },
      {
        "field": {
          "name": "name",
          "subType": "string",
          "@required": true,
          "@maxLength": 200
        }
      },
      {
        "field": {
          "name": "description",
          "subType": "string",
          "@maxLength": 1000
        }
      },
      {
        "field": {
          "name": "price",
          "subType": "decimal",
          "@precision": 10,
          "@scale": 2,
          "@minValue": "0.00"
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
          "name": "product_pk",
          "subType": "primary",
          "fields": ["id"],
          "@generation": "increment"
        }
      }
    ]
  }
}
```

**Step 2**: Add Product database mapping to `database-overlay.json`:
```json
{
  "object": {
    "name": "Product",
    "@dbTable": "products",
    "children": [
      {
        "field": {
          "name": "id",
          "@dbColumn": "product_id"
        }
      },
      {
        "field": {
          "name": "name",
          "@dbColumn": "product_name"
        }
      },
      {
        "field": {
          "name": "description",
          "@dbColumn": "description"
        }
      },
      {
        "field": {
          "name": "price",
          "@dbColumn": "price"
        }
      },
      {
        "field": {
          "name": "isActive",
          "@dbColumn": "is_active"
        }
      }
    ]
  }
}
```

**Step 3**: Generate the Product domain class:
```bash
mvn metaobjects:generate
```

**Step 4**: Create ProductService following the existing UserService pattern:
```java
package ${package}.service;

import ${package}.domain.Product;
import com.metaobjects.manager.db.ObjectManagerDB;
import com.metaobjects.object.ValueMetaObject;
import com.metaobjects.MetaObject;
import com.metaobjects.util.MetaDataUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ObjectManagerDB objectManager;
    private final MetaObject productMetaObject;

    public ProductService(ObjectManagerDB objectManager) throws Exception {
        this.objectManager = objectManager;
        this.productMetaObject = MetaDataUtil.findMetaObjectByName("Product", this);
    }

    public ValueMetaObject createProduct(String name, String description, BigDecimal price) throws Exception {
        // Implementation following UserService pattern
    }

    public List<ValueMetaObject> findActiveProducts() throws Exception {
        // Implementation following UserService pattern
    }
}
```

**Step 5**: Create ProductController following the existing UserController pattern:
```java
package ${package}.controller;

import ${package}.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    // Implementation following UserController pattern
}
```

---

## 🎯 **UNDERSTANDING YOUR GENERATED CODE**

### **Domain Objects**: `src/main/java/${packageInPathFormat}/domain/`

These classes are **generated automatically** from your metadata:

- **User.java**: Complete JPA entity with validation annotations
- **Order.java**: Order entity with decimal handling and relationships
- **OrderItem.java**: Order item entity with computed fields

**Key Features**:
- JPA annotations based on database overlay
- Bean validation constraints from metadata
- Proper equals/hashCode based on identity
- Clean toString implementations

### **Service Layer**: `src/main/java/${packageInPathFormat}/service/`

These classes demonstrate **MetaObjects patterns**:

- **ObjectManagerDB integration**: Direct database operations using metadata
- **MetaObject lookups**: Dynamic object resolution by name
- **ValueMetaObject usage**: Type-safe field access with metadata validation
- **Transaction management**: Proper Spring transaction boundaries

### **REST Controllers**: `src/main/java/${packageInPathFormat}/controller/`

These classes provide **complete CRUD APIs**:

- **Standard HTTP methods**: GET, POST, PUT, DELETE
- **Proper status codes**: 200, 201, 404, etc.
- **JSON serialization**: Automatic ValueMetaObject to JSON conversion
- **Validation**: Bean validation integration

---

## 🔍 **METAOBJECTS PATTERNS IN YOUR PROJECT**

### **Identity System (Modern Approach)**

Your project uses the **modern MetaIdentity system** (not deprecated MetaKey):

```json
{
  "identity": {
    "name": "user_pk",
    "subType": "primary",
    "fields": ["id"],
    "@generation": "increment",
    "description": "Primary key for User table"
  }
}
```

**Key Benefits**:
- **Auto-generation**: Database handles ID assignment
- **Composite support**: Multiple fields in identity
- **Type safety**: Proper field validation
- **Relationship clarity**: Named identities for foreign keys

### **Relationship Patterns**

Your project demonstrates **proper relationship modeling**:

```java
// In OrderService - finding orders for a user
public List<ValueMetaObject> findOrdersForUser(Long userId) throws Exception {
    String whereClause = "user_id = ? ORDER BY order_date DESC";
    Object[] parameters = {userId};
    return objectManager.loadObjects(connection, orderMetaObject, whereClause, parameters);
}

// In OrderService - finding items for an order
public List<ValueMetaObject> findOrderItemsForOrder(Long orderId) throws Exception {
    String whereClause = "order_id = ?";
    Object[] parameters = {orderId};
    return objectManager.loadObjects(connection, orderItemMetaObject, whereClause, parameters);
}
```

### **Validation Integration**

Your metadata drives **automatic validation**:

- **@required**: Fields cannot be null
- **@maxLength**: String length validation
- **@pattern**: Regular expression validation
- **@minValue/@maxValue**: Numeric range validation

These constraints are enforced both in:
- **Generated domain objects** (Bean validation annotations)
- **MetaObjects framework** (Metadata validation)
- **Database schema** (Column constraints)

---

## 💾 **DATABASE INTEGRATION**

### **Your Database Configuration**

The project is configured with **H2 in-memory database** for immediate testing:

```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:${artifactId}
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
```

### **Switching to Production Database**

To use PostgreSQL (example):

**Step 1**: Update dependencies in `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Step 2**: Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/${artifactId}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Step 3**: Update ObjectManagerDB configuration in `MetaObjectsConfiguration.java`:
```java
objectManager.setDriverClass("com.metaobjects.manager.db.driver.PostgreSQLDriver");
```

### **Schema Management**

Your project includes **automatic table creation**:

```java
// MetaObjectsConfiguration.java
validator.setAutoCreate(true); // Creates missing tables automatically
```

For production, consider:
- **Flyway**: Database migration management
- **Liquibase**: Database change management
- **Manual DDL**: Generated from metadata schemas

---

## 🧪 **TESTING YOUR APPLICATION**

### **Unit Tests**: `src/test/java/${packageInPathFormat}/service/`

Your project includes **complete service tests**:

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldGenerateIdAndPersist() throws Exception {
        // Test auto-generation of IDs
        // Verify database persistence
        // Check constraint validation
    }
}
```

### **Integration Tests**: `src/test/java/${packageInPathFormat}/integration/`

Your project includes **database integration tests**:

```java
@SpringBootTest
@Transactional
class DatabaseIntegrationTest {

    @Test
    void metaObjectsSchemaCreation_ShouldCreateAllTables() throws Exception {
        // Verify table creation from metadata
        // Test relationship constraints
        // Validate index creation
    }
}
```

### **API Testing**

Test your REST endpoints:

```bash
# Create test script
cat > test-api.sh << 'EOF'
#!/bin/bash
echo "Testing ${artifactId} API..."

# Create user
USER_ID=$(curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","firstName":"Test","lastName":"User"}' \
  | jq -r '.id')

echo "Created user with ID: $USER_ID"

# Get user
curl -s http://localhost:8080/api/users/$USER_ID | jq '.'

# Create order
ORDER_ID=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"orderNumber\":\"ORD-1234567890\",\"totalAmount\":99.99,\"status\":\"PENDING\"}" \
  | jq -r '.id')

echo "Created order with ID: $ORDER_ID"

# List orders for user
curl -s "http://localhost:8080/api/orders?userId=$USER_ID" | jq '.'
EOF

chmod +x test-api.sh
./test-api.sh
```

---

## 📈 **PERFORMANCE OPTIMIZATION**

### **MetaObjects Performance Patterns**

Your project follows **MetaObjects performance best practices**:

```java
// ✅ GOOD - Cache MetaDataLoader instances
@Bean
@Singleton
public FileMetaDataLoader metaDataLoader() throws Exception {
    // Loaded once at startup, cached permanently
}

// ✅ GOOD - Reuse MetaObject references
private final MetaObject userMetaObject; // Cached at service startup

// ✅ GOOD - Batch operations when possible
public void createMultipleUsers(List<CreateUserRequest> requests) throws Exception {
    try (ObjectConnection connection = objectManager.getConnection()) {
        for (CreateUserRequest request : requests) {
            // Process in single transaction
        }
    }
}
```

### **Database Performance**

Your project includes **database optimization patterns**:

- **Connection pooling**: Configured via Spring Boot
- **Transaction management**: Service-level boundaries
- **Index creation**: Driven by metadata identities
- **Query optimization**: Proper WHERE clause usage

### **Memory Management**

Your project follows **MetaObjects memory patterns**:

- **WeakHashMap caches**: Automatic cleanup during memory pressure
- **Permanent metadata**: Loaded once, cached forever
- **OSGI compatibility**: Bundle-safe classloader patterns

---

## 🔧 **TROUBLESHOOTING**

### **Common Issues in Your Project**

#### **"MetaData not found" Errors**
```
MetaDataNotFoundException: No MetaObject found with name 'User'
```

**Solution**: Check metadata file loading in `MetaObjectsConfiguration.java`:
```java
// Verify paths are correct
loader.addSourceURI(getClass().getResource("/metadata/application-metadata.json").toURI());
loader.addSourceURI(getClass().getResource("/metadata/database-overlay.json").toURI());
```

#### **Database Connection Issues**
```
Unable to connect to database: jdbc:h2:mem:${artifactId}
```

**Solution**: Check H2 configuration in `application.properties`:
- Verify database URL format
- Check H2 dependency is included
- Ensure Spring Boot autoconfiguration is enabled

#### **Code Generation Issues**
```
Unable to generate domain objects from metadata
```

**Solution**: Run generation manually and check output:
```bash
mvn metaobjects:generate -X  # Debug output
mvn clean compile           # Full rebuild
```

#### **Identity Generation Issues**
```
SQLException: Attempt to modify an identity column 'ID'
```

**Solution**: Check identity configuration in metadata:
- Ensure `@generation": "increment"` is set
- Verify primary identity exists
- Don't manually set ID values for auto-generated fields

### **Debug Configuration**

Add to `application.properties` for detailed logging:
```properties
# MetaObjects debug logging
logging.level.com.metaobjects=DEBUG
logging.level.${package}=DEBUG

# SQL logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# H2 database logging
logging.level.org.h2=DEBUG
```

---

## 📚 **NEXT STEPS FOR YOUR PROJECT**

### **Immediate Enhancements**

1. **Add Product Entity**: Follow the example in this guide
2. **Implement Product-Order Relationships**: Many-to-many through OrderItem
3. **Add User Authentication**: Spring Security integration
4. **Create Web UI**: React frontend with MetaObjects components

### **Production Readiness**

1. **Switch Database**: PostgreSQL, MySQL, or SQL Server
2. **Add Monitoring**: Spring Boot Actuator + Micrometer
3. **Implement Caching**: Redis or Hazelcast integration
4. **Security Hardening**: HTTPS, authentication, authorization
5. **Container Deployment**: Docker + Kubernetes

### **MetaObjects Advanced Features**

1. **Dynamic Metadata Updates**: Runtime metadata modification
2. **Custom Code Generation**: Specialized Mustache templates
3. **Multi-Database Support**: Different databases per entity
4. **Event-Driven Architecture**: MetaObjects with message queues
5. **Microservices Patterns**: MetaObjects in distributed systems

---

## 🤝 **GETTING HELP**

### **Claude Code Integration**

This guide is optimized for **Claude Code assistance**. When asking Claude for help:

✅ **Include context**: "I'm working on the ${artifactId} MetaObjects project..."
✅ **Reference files**: "Based on my application-metadata.json and UserService.java..."
✅ **Be specific**: "Following the pattern in OrderService, how do I..."
✅ **Share errors**: Include complete stack traces and configuration

### **Example Claude Interactions**

```
Q: "How do I add a Category entity that has a many-to-many relationship with Product?"

A: "Based on your ${artifactId} project structure, I can see you're following
MetaObjects patterns. Here's how to add Category with many-to-many Product relationship:

1. Add Category metadata to your application-metadata.json following your
   existing User/Order pattern...
2. Create a ProductCategory junction entity following your OrderItem pattern...
3. Update your ProductService following the relationship patterns in OrderService..."
```

### **Support Resources**

- **Framework Documentation**: https://docs.metaobjects.com
- **GitHub Issues**: https://github.com/metaobjectsdev/metaobjects-core/issues
- **Community Examples**: https://github.com/metaobjectsdev/metaobjects-examples
- **This Project**: All examples and patterns included in your generated code

---

## 🏆 **PROJECT SUCCESS METRICS**

### **Immediate Success Indicators**

✅ **Application starts**: `mvn spring-boot:run` works without errors
✅ **Database creates**: H2 tables created automatically
✅ **API responds**: All REST endpoints return valid responses
✅ **Tests pass**: Unit and integration tests execute successfully
✅ **Code generates**: Domain objects update when metadata changes

### **Development Velocity Indicators**

✅ **New entities**: Can add new entities in minutes, not hours
✅ **Database changes**: Schema updates driven by metadata changes
✅ **API expansion**: New endpoints follow established patterns
✅ **Testing**: Comprehensive test coverage with minimal boilerplate
✅ **Documentation**: Self-documenting through metadata and generation

### **Architecture Quality Indicators**

✅ **Separation of concerns**: Clear boundaries between layers
✅ **Metadata consistency**: Single source of truth for domain model
✅ **Database independence**: Easy database switching
✅ **Framework independence**: Clean architecture patterns
✅ **Maintenance efficiency**: Changes propagate automatically through generation

---

**🚀 Your ${artifactId} MetaObjects application is ready for amazing development! 🚀**

**Key Success Factors:**
- **Metadata-driven development** eliminates boilerplate
- **Automatic code generation** maintains consistency
- **Database integration** handles persistence complexity
- **Spring Boot integration** provides production features
- **Comprehensive testing** ensures reliability
- **Claude Code integration** accelerates development

**Start building features immediately - the foundation is complete!**