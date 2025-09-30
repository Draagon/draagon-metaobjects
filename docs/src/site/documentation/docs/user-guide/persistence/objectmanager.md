# ObjectManager Persistence Framework

The ObjectManager framework provides metadata-driven persistence for MetaObjects, supporting both SQL databases (ObjectManagerDB) and NoSQL databases (ObjectManagerNoSQL). This guide covers setup, configuration, and usage patterns.

## Core Architecture

ObjectManager follows the MetaObjects READ-OPTIMIZED design pattern:
- **MetaData defines persistence structure** (tables, columns, keys)
- **ObjectManager maps MetaData to database operations**
- **Domain objects use MetaData for type-safe persistence**

## ObjectManagerDB (SQL Databases)

### Basic Setup

```java
// 1. Initialize MetaData Loader
FileMetaDataLoader loader = new FileMetaDataLoader(options, "persistenceLoader");
loader.init();
loader.register(); // Register for MetaDataUtil discovery

// 2. Initialize Database
EmbeddedDataSource dataSource = new EmbeddedDataSource();
dataSource.setDatabaseName("memory:myapp");
dataSource.setCreateDatabase("create");

// 3. Initialize ObjectManagerDB
ObjectManagerDB objectManager = new ObjectManagerDB();
objectManager.setDriverClass("com.metaobjects.manager.db.driver.DerbyDriver");
objectManager.setDataSource(dataSource);

// 4. Auto-create Database Schema
MetaClassDBValidatorService validator = new MetaClassDBValidatorService();
validator.setObjectManager(objectManager);
validator.setAutoCreate(true);

MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
registry.registerLoader(loader);
validator.setMetaDataLoaderRegistry(registry);
validator.init(); // Creates tables based on MetaData
```

### Supported Database Drivers

```java
// Derby (Embedded)
objectManager.setDriverClass("com.metaobjects.manager.db.driver.DerbyDriver");

// MySQL
objectManager.setDriverClass("com.metaobjects.manager.db.driver.MySQLDriver");

// PostgreSQL
objectManager.setDriverClass("com.metaobjects.manager.db.driver.PostgreSQLDriver");

// SQL Server
objectManager.setDriverClass("com.metaobjects.manager.db.driver.SQLServerDriver");

// Oracle
objectManager.setDriverClass("com.metaobjects.manager.db.driver.OracleDriver");
```

## PrimaryKey Metadata Requirements

⚠️ **CRITICAL**: All persistent objects MUST have proper PrimaryKey metadata with auto-increment strategy.

### Correct PrimaryKey Pattern

```json title="user-metadata.json"
{
  "metadata": {
    "package": "myapp",
    "children": [
      {
        "object": {
          "name": "User",
          "subType": "managed",
          "@dbTable": "users",
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long",
                "@dbColumn": "user_id"
              }
            },
            {
              "field": {
                "name": "username",
                "subType": "string",
                "@dbColumn": "username",
                "@required": true
              }
            },
            {
              "key": {
                "name": "primary",
                "subType": "primary",
                "@keys": ["id"],
                "@autoIncrementStrategy": "sequential"
              }
            }
          ]
        }
      }
    ]
  }
}
```

### Auto-Increment Strategies

| Strategy | Description | Use Case |
|----------|-------------|----------|
| `"sequential"` | Database auto-increment | Derby IDENTITY, MySQL AUTO_INCREMENT, PostgreSQL SERIAL |
| `"uuid"` | UUID generation | String primary keys, distributed systems |
| `"none"` | Manual assignment | Custom ID generation logic |

### Database Overlay Pattern

Separate business logic from database-specific configuration:

**base-metadata.json** (Business Logic):
```json
{
  "metadata": {
    "package": "myapp",
    "children": [
      {
        "object": {
          "name": "User",
          "subType": "managed",
          "children": [
            {
              "field": {
                "name": "id",
                "subType": "long"
              }
            },
            {
              "field": {
                "name": "username",
                "subType": "string",
                "@required": true,
                "@maxLength": 50
              }
            },
            {
              "key": {
                "name": "primary",
                "subType": "primary",
                "@keys": ["id"]
              }
            }
          ]
        }
      }
    ]
  }
}
```

**db-overlay.json** (Database Configuration):
```json
{
  "metadata": {
    "package": "myapp",
    "children": [
      {
        "object": {
          "name": "User",
          "@dbTable": "APP_USERS",
          "children": [
            {
              "field": {
                "name": "id",
                "@dbColumn": "USER_ID"
              }
            },
            {
              "field": {
                "name": "username",
                "@dbColumn": "USER_NAME"
              }
            },
            {
              "key": {
                "name": "primary",
                "@autoIncrementStrategy": "sequential"
              }
            }
          ]
        }
      }
    ]
  }
}
```

## CRUD Operations

### Create Operations

```java
ObjectConnection connection = objectManager.getConnection();
try {
    // Create domain object
    User user = new User();

    // REQUIRED: Assign MetaData before persistence
    MetaObject userMeta = loader.getMetaObjectByName("myapp::User");
    user.setMetaData(userMeta);

    // Set field values
    user.setUsername("john_doe");
    user.setEmail("john@example.com");

    // Persist to database
    objectManager.createObject(connection, user);

    // Auto-increment ID is assigned by database
    System.out.println("Created user with ID: " + user.getId());

} finally {
    connection.close();
}
```

### Read Operations

```java
ObjectConnection connection = objectManager.getConnection();
try {
    MetaObject userMeta = loader.getMetaObjectByName("myapp::User");

    // Get all users
    Collection<User> allUsers = objectManager.getObjects(connection, userMeta);

    // Get user by ID
    User user = (User) objectManager.getObject(connection, userMeta, 1L);

    // Query with criteria
    List<User> activeUsers = objectManager.getObjects(connection, userMeta,
        "WHERE active = true");

} finally {
    connection.close();
}
```

### Update Operations

```java
ObjectConnection connection = objectManager.getConnection();
try {
    MetaObject userMeta = loader.getMetaObjectByName("myapp::User");
    User user = (User) objectManager.getObject(connection, userMeta, 1L);

    // Modify fields
    user.setEmail("new_email@example.com");

    // Update in database
    objectManager.updateObject(connection, user);

} finally {
    connection.close();
}
```

### Delete Operations

```java
ObjectConnection connection = objectManager.getConnection();
try {
    MetaObject userMeta = loader.getMetaObjectByName("myapp::User");
    User user = (User) objectManager.getObject(connection, userMeta, 1L);

    // Delete from database
    objectManager.deleteObject(connection, user);

} finally {
    connection.close();
}
```

## Advanced Features

### Foreign Key Relationships

```json
{
  "object": {
    "name": "Order",
    "subType": "managed",
    "@dbTable": "orders",
    "children": [
      {
        "field": {
          "name": "id",
          "subType": "long",
          "@dbColumn": "order_id"
        }
      },
      {
        "field": {
          "name": "userId",
          "subType": "long",
          "@dbColumn": "user_id"
        }
      },
      {
        "key": {
          "name": "primary",
          "subType": "primary",
          "@keys": ["id"],
          "@autoIncrementStrategy": "sequential"
        }
      },
      {
        "key": {
          "name": "userKey",
          "subType": "foreign",
          "@keys": ["userId"],
          "@foreignObjectRef": "myapp::User"
        }
      }
    ]
  }
}
```

### Database Schema Validation

```java
// Enable strict validation
validator.setStrictMode(true);

// Custom validation rules
validator.addCustomValidator(new TableNamingValidator());

// Validate existing database
ValidationResult result = validator.validateDatabase(connection);
if (!result.isValid()) {
    System.out.println("Schema validation failed: " + result.getErrors());
}
```

### Connection Pooling

```java
// Configure connection pool
ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
pooledDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/myapp");
pooledDataSource.setUser("username");
pooledDataSource.setPassword("password");
pooledDataSource.setMinPoolSize(5);
pooledDataSource.setMaxPoolSize(20);

objectManager.setDataSource(pooledDataSource);
```

## Spring Integration

### Configuration

```java
@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

    @Bean
    public MetaDataLoader metaDataLoader() throws Exception {
        FileMetaDataLoader loader = new FileMetaDataLoader(options, "springLoader");
        loader.init();
        loader.register();
        return loader;
    }

    @Bean
    public ObjectManagerDB objectManager(DataSource dataSource) {
        ObjectManagerDB om = new ObjectManagerDB();
        om.setDriverClass("com.metaobjects.manager.db.driver.PostgreSQLDriver");
        om.setDataSource(dataSource);
        return om;
    }

    @Bean
    public MetaClassDBValidatorService dbValidator(ObjectManagerDB objectManager,
                                                   MetaDataLoader loader) {
        MetaClassDBValidatorService validator = new MetaClassDBValidatorService();
        validator.setObjectManager(objectManager);
        validator.setAutoCreate(true);

        MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        registry.registerLoader(loader);
        validator.setMetaDataLoaderRegistry(registry);

        return validator;
    }
}
```

### Service Layer

```java
@Service
@Transactional
public class UserService {

    @Autowired
    private ObjectManagerDB objectManager;

    @Autowired
    private MetaDataLoader loader;

    public User createUser(String username, String email) {
        ObjectConnection connection = objectManager.getConnection();
        try {
            User user = new User();
            user.setMetaData(loader.getMetaObjectByName("myapp::User"));
            user.setUsername(username);
            user.setEmail(email);

            objectManager.createObject(connection, user);
            return user;

        } finally {
            connection.close();
        }
    }

    public List<User> findAllUsers() {
        ObjectConnection connection = objectManager.getConnection();
        try {
            MetaObject userMeta = loader.getMetaObjectByName("myapp::User");
            return new ArrayList<>(objectManager.getObjects(connection, userMeta));

        } finally {
            connection.close();
        }
    }
}
```

## Performance Optimization

### Caching Strategies

```java
// Enable ObjectManager caching
objectManager.setCacheEnabled(true);
objectManager.setCacheSize(1000);

// Configure metadata caching
loader.enableCaching(true);
```

### Batch Operations

```java
ObjectConnection connection = objectManager.getConnection();
try {
    // Begin batch
    objectManager.beginBatch(connection);

    for (User user : users) {
        user.setMetaData(userMeta);
        objectManager.createObject(connection, user);
    }

    // Execute batch
    objectManager.executeBatch(connection);

} finally {
    connection.close();
}
```

### SQL Query Optimization

```java
// Use prepared statements
String sql = "SELECT * FROM users WHERE department = ? AND active = ?";
List<User> users = objectManager.executeQuery(connection, userMeta, sql,
    "Engineering", true);

// Enable query caching
objectManager.setQueryCacheEnabled(true);
```

## Troubleshooting

### Common Issues

**"Attempt to modify an identity column"**
- **Cause**: Missing or incorrect PrimaryKey metadata
- **Solution**: Ensure `@autoIncrementStrategy: "sequential"` is specified

**"No MetaData found for object"**
- **Cause**: `setMetaData()` not called before persistence
- **Solution**: Always call `object.setMetaData(metaObject)` before CRUD operations

**"Table does not exist"**
- **Cause**: Database schema not created
- **Solution**: Ensure `MetaClassDBValidatorService.setAutoCreate(true)` and `init()` called

**"Type not registered"**
- **Cause**: MetaDataTypeProvider not loaded
- **Solution**: Check META-INF/services configuration and provider priority

### Debug Logging

```java
// Enable SQL logging
objectManager.setLogSQL(true);

// Enable MetaData loading debug
loader.setVerbose(true);

// Enable constraint debugging
registry.setDebugConstraints(true);
```

## Migration Guide

### From Version 5.x to 6.x

1. **Update PrimaryKey metadata**: Add `@autoIncrementStrategy` to all primary keys
2. **Replace @isId attributes**: Use proper PrimaryKey metadata instead
3. **Update constraint definitions**: Migrate from JSON to provider-based registration
4. **Test auto-increment**: Verify database ID generation works correctly

### From Direct JDBC

1. **Create MetaData definitions**: Define objects, fields, and keys in JSON
2. **Replace SQL with ObjectManager**: Use CRUD operations instead of direct SQL
3. **Add MetaData assignment**: Call `setMetaData()` on domain objects
4. **Configure database validation**: Use `MetaClassDBValidatorService` for schema management

This documentation provides comprehensive coverage of the ObjectManager persistence framework. For additional examples, see the demo applications and test cases in the MetaObjects repository.