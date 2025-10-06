# ${artifactId}

A MetaObjects application for building metadata-driven domain objects with automatic code generation and database persistence.

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

### Running the Application

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on http://localhost:8080

### Testing

```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -Dtest="**/*IntegrationTest"
```

## Project Structure

```
${artifactId}/
├── src/main/java/${package}/
│   ├── Application.java              # Spring Boot main class
│   ├── config/
│   │   └── MetaObjectsConfiguration.java  # MetaObjects setup
│   ├── controller/
│   │   ├── UserController.java       # User REST API
│   │   └── OrderController.java      # Order REST API
│   └── service/
│       ├── UserService.java          # User business logic
│       └── OrderService.java         # Order business logic
├── src/main/resources/
│   ├── metadata/
│   │   ├── application-metadata.json # Core domain metadata
│   │   └── database-overlay.json     # Database mappings
│   └── application.properties        # App configuration
└── src/test/java/${package}/
    ├── service/
    │   └── UserServiceTest.java      # Unit tests
    └── integration/
        └── ApplicationIntegrationTest.java  # Integration tests
```

## MetaObjects Features

### Metadata-Driven Development

This project uses JSON metadata to define domain objects:

- **User**: Core user entity with validation and relationships
- **Order**: Order management with lifecycle states
- **OrderItem**: Line items with automatic total calculations

### Core Patterns Demonstrated

1. **ValueMetaObject**: Type-safe field access with automatic validation
2. **MetaIdentity System**: Modern primary/secondary key management
3. **ObjectManagerDB**: Database persistence with connection management
4. **Relationship Management**: Foreign keys and associations
5. **Business Logic**: Order lifecycle and calculation patterns

## API Endpoints

### User Management

```bash
# Create user
POST /api/users
Content-Type: application/json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe"
}

# Get user by ID
GET /api/users/{id}

# Search users by name
GET /api/users/search?name=john

# Update user
PUT /api/users/{id}
{
  "email": "new.email@example.com"
}
```

### Order Management

```bash
# Create order
POST /api/orders
{
  "userId": 1,
  "shippingAddress": "123 Main St, City, State 12345"
}

# Add order item
POST /api/orders/{orderId}/items
{
  "productName": "Wireless Headphones",
  "quantity": 2,
  "unitPrice": 99.99
}

# Confirm order
POST /api/orders/{orderId}/confirm
```

## Database Schema

The application uses H2 in-memory database for development. Database tables are automatically created from metadata definitions:

- **users**: User information with unique constraints
- **orders**: Order headers with status tracking
- **order_items**: Order line items with totals

### Database Access

Development H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:${artifactId}`
- Username: `sa`
- Password: `password`

## Configuration

### MetaObjects Configuration

Key configuration in `application.properties`:

```properties
# MetaObjects settings
metaobjects.auto-create-tables=true
metaobjects.validate-schema=true
metaobjects.cache-metadata=true
```

### Database Configuration

```properties
# Development database (H2)
spring.datasource.url=jdbc:h2:mem:${artifactId}
spring.datasource.username=sa
spring.datasource.password=password
```

## Adding New Entities

### 1. Define Metadata

Create metadata in `src/main/resources/metadata/application-metadata.json`:

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
          "subType": "long",
          "description": "Product ID"
        }
      },
      {
        "field": {
          "name": "name",
          "subType": "string",
          "@required": true,
          "@maxLength": 200,
          "description": "Product name"
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

### 2. Create Service Class

```java
@Service
@Transactional
public class ProductService {

    private final ObjectManagerDB objectManager;
    private final MetaObject productMetaObject;

    public ProductService(ObjectManagerDB objectManager) throws Exception {
        this.objectManager = objectManager;
        this.productMetaObject = MetaDataUtil.findMetaObjectByName("Product", this);
    }

    public ValueMetaObject createProduct(String name, String description) throws Exception {
        try (ObjectConnection connection = objectManager.getConnection()) {
            ValueMetaObject product = new ValueMetaObject(productMetaObject);
            product.setString("name", name);
            product.setString("description", description);
            objectManager.createObject(connection, product);
            return product;
        }
    }
}
```

### 3. Create REST Controller

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Map<String, Object> request) {
        // Implementation
    }
}
```

## Development Tips

### MetaObjects Best Practices

1. **Use ValueMetaObject for new objects**: Provides type-safe field access
2. **Always use try-with-resources**: For ObjectConnection management
3. **Leverage MetaIdentity**: Modern approach for primary/secondary keys
4. **Test with real metadata**: Integration tests validate complete flow

### Testing Patterns

1. **Unit Tests**: Mock MetaObjects components for isolated testing
2. **Integration Tests**: Use @SpringBootTest with @Transactional
3. **Test Data**: Create reusable test fixtures in @BeforeEach methods

### Debugging

1. **Enable SQL logging**: Set `spring.jpa.show-sql=true` for development
2. **MetaObjects logging**: Set `logging.level.com.metaobjects=DEBUG`
3. **H2 Console**: Use web interface to inspect database state

## Architecture

This project demonstrates:

- **Metadata-Driven Architecture**: Domain objects defined in JSON metadata
- **Service Layer Pattern**: Business logic separated from controllers
- **Repository Pattern**: ObjectManagerDB provides data persistence abstraction
- **REST API Design**: Clean HTTP endpoints with proper status codes
- **Transaction Management**: @Transactional ensures data consistency

## Technology Stack

- **MetaObjects Framework**: Metadata-driven development
- **Spring Boot 3.2.0**: Application framework
- **H2 Database**: In-memory database for development
- **Jackson**: JSON serialization
- **JUnit 5**: Testing framework
- **Mockito**: Mocking for unit tests

## Next Steps

1. **Add Security**: Implement Spring Security for authentication
2. **Add Validation**: Extend MetaObjects validators for business rules
3. **Production Database**: Configure PostgreSQL or MySQL
4. **API Documentation**: Add OpenAPI/Swagger documentation
5. **Monitoring**: Add Micrometer metrics and health checks

## Support

For MetaObjects framework documentation and examples, see:
- Project documentation in `.claude/CLAUDE.md`
- MetaObjects GitHub repository
- Example metadata files in `src/main/resources/metadata/`

## License

This project is generated from the MetaObjects archetype.