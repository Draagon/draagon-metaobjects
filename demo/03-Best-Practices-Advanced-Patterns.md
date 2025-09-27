# MetaObjects Best Practices & Advanced Patterns

## Runtime Metadata Loading Patterns

### 1. Service-Level Metadata Loading

**The key power of MetaObjects is runtime metadata loading** - services can adapt to metadata changes without redeployment:

```java
@Service
public class UserService {
    
    private final MetaDataLoader metaDataLoader;
    private final UserRepository userRepository;
    
    public UserService(MetaDataLoader metaDataLoader, UserRepository userRepository) {
        this.metaDataLoader = metaDataLoader;
        this.userRepository = userRepository;
    }
    
    @PostConstruct
    public void initialize() {
        // Load metadata at startup
        metaDataLoader.loadMetaData("metadata/models/user.xml");
        metaDataLoader.loadMetaData("metadata/models/overlays/database.xml");
        
        // Register for metadata changes (if using enterprise repository)
        metaDataLoader.registerChangeListener(this::handleMetadataChange);
    }
    
    public User createUser(Map<String, Object> userData) {
        MetaObject userMeta = metaDataLoader.getMetaObject("User");
        
        // Create user instance from metadata
        User user = (User) userMeta.newInstance();
        
        // Set field values dynamically based on metadata
        for (MetaField field : userMeta.getMetaFields()) {
            String fieldName = field.getName();
            if (userData.containsKey(fieldName)) {
                // Runtime field setting based on metadata
                userMeta.setValue(field, user, userData.get(fieldName));
            }
            
            // Set default values if not provided
            if (field.getDefaultValue() != null && userData.get(fieldName) == null) {
                userMeta.setValue(field, user, field.getDefaultValue());
            }
        }
        
        // Perform validation using metadata
        validateUser(user, userMeta);
        
        return userRepository.save(user);
    }
    
    private void validateUser(User user, MetaObject userMeta) {
        for (MetaField field : userMeta.getMetaFields()) {
            Object value = userMeta.getValue(field, user);
            
            // Run validators defined in metadata
            for (MetaValidator validator : field.getMetaValidators()) {
                ValidationResult result = validator.validate(value);
                if (!result.isValid()) {
                    throw new ValidationException("Field " + field.getName() + ": " + result.getErrorMessage());
                }
            }
        }
    }
    
    private void handleMetadataChange(MetaDataChangeEvent event) {
        log.info("Metadata changed: {}", event.getChangedObjects());
        // Automatically adapt to metadata changes without restart
        // Could trigger cache invalidation, revalidation, etc.
    }
}
```

### 2. Dynamic UI Generation

**Generate UI components dynamically from metadata:**

```java
@RestController
@RequestMapping("/api/forms")
public class DynamicFormController {
    
    private final MetaDataLoader metaDataLoader;
    
    @GetMapping("/{entityName}")
    public FormDefinition generateForm(@PathVariable String entityName, 
                                     @RequestParam(required = false) String overlay) {
        
        MetaObject entity = metaDataLoader.getMetaObject(entityName);
        
        // Load UI overlay if specified
        if (overlay != null) {
            metaDataLoader.loadMetaData("metadata/models/overlays/" + overlay + ".xml");
        }
        
        FormDefinition form = new FormDefinition();
        form.setTitle(entity.getMetaAttr("uiTitle").getValueAsString());
        form.setIcon(entity.getMetaAttr("uiIcon").getValueAsString());
        
        // Generate form fields from metadata
        for (MetaField field : entity.getMetaFields()) {
            if (shouldIncludeInForm(field)) {
                FormField formField = createFormField(field);
                form.addField(formField);
            }
        }
        
        return form;
    }
    
    private FormField createFormField(MetaField field) {
        FormField formField = new FormField();
        formField.setName(field.getName());
        formField.setType(getUIFieldType(field));
        
        // Get UI attributes from metadata
        if (field.hasMetaAttr("uiLabel")) {
            formField.setLabel(field.getMetaAttr("uiLabel").getValueAsString());
        }
        
        if (field.hasMetaAttr("uiPlaceholder")) {
            formField.setPlaceholder(field.getMetaAttr("uiPlaceholder").getValueAsString());
        }
        
        if (field.hasMetaAttr("uiRequired")) {
            formField.setRequired(field.getMetaAttr("uiRequired").getValueAsBoolean());
        }
        
        // Add validation rules from metadata
        for (MetaValidator validator : field.getMetaValidators()) {
            ValidationRule rule = createValidationRule(validator);
            formField.addValidationRule(rule);
        }
        
        return formField;
    }
    
    private String getUIFieldType(MetaField field) {
        // Check for explicit UI type
        if (field.hasMetaAttr("uiType")) {
            return field.getMetaAttr("uiType").getValueAsString();
        }
        
        // Map data type to UI field type
        return switch (field.getDataType().name().toLowerCase()) {
            case "string" -> "text";
            case "int", "long", "double", "float" -> "number";
            case "boolean" -> "checkbox";
            case "date" -> "date";
            case "stringarray" -> "multi-select";
            default -> "text";
        };
    }
}
```

### 3. PII Protection Pattern

**Automatic PII handling based on metadata:**

```java
@Component
public class PIIProtectionService {
    
    private final SecurityContext securityContext;
    
    @EventListener
    public void handleDataAccess(DataAccessEvent event) {
        Object data = event.getData();
        MetaObject metaObject = MetaDataRegistry.findMetaObject(data);
        
        if (metaObject != null) {
            sanitizeForCurrentUser(data, metaObject);
        }
    }
    
    private void sanitizeForCurrentUser(Object data, MetaObject metaObject) {
        for (MetaField field : metaObject.getMetaFields()) {
            // Check if field contains PII
            if (field.hasMetaAttr("pii") && field.getMetaAttr("pii").getValueAsBoolean()) {
                
                // Check user permissions
                if (!securityContext.hasPermission("pii_access")) {
                    // Mask or remove PII data based on metadata
                    Object maskedValue = maskPIIValue(field, metaObject.getValue(field, data));
                    metaObject.setValue(field, data, maskedValue);
                }
                
                // Log PII access for compliance
                auditPIIAccess(field, data);
            }
        }
    }
    
    private Object maskPIIValue(MetaField field, Object value) {
        if (value == null) return null;
        
        String fieldType = field.getDataType().name().toLowerCase();
        String stringValue = value.toString();
        
        return switch (fieldType) {
            case "string" -> {
                if (field.getName().toLowerCase().contains("email")) {
                    yield maskEmail(stringValue);
                } else if (field.getName().toLowerCase().contains("phone")) {
                    yield maskPhone(stringValue);
                } else {
                    yield maskGeneric(stringValue);
                }
            }
            default -> "[REDACTED]";
        };
    }
    
    private String maskEmail(String email) {
        if (email.contains("@")) {
            String[] parts = email.split("@");
            return parts[0].charAt(0) + "***@" + parts[1];
        }
        return "***";
    }
    
    private void auditPIIAccess(MetaField field, Object data) {
        log.info("PII field accessed: {} by user: {}", 
                field.getName(), securityContext.getCurrentUser());
        
        // Send to audit system
        AuditEvent event = AuditEvent.builder()
                .fieldName(field.getName())
                .entityType(data.getClass().getSimpleName())
                .userId(securityContext.getCurrentUserId())
                .timestamp(Instant.now())
                .action("READ")
                .build();
                
        auditService.recordEvent(event);
    }
}
```

## Advanced Metadata Patterns

### 1. Conditional Metadata Loading

**Load different metadata based on environment or tenant:**

```java
@Configuration
public class MetaDataConfiguration {
    
    @Value("${app.environment}")
    private String environment;
    
    @Value("${app.tenant:default}")
    private String tenant;
    
    @Bean
    public MetaDataLoader metaDataLoader() {
        FileMetaDataLoader loader = new FileMetaDataLoader(
            new FileLoaderOptions()
                .addParser("*.xml", XMLMetaDataParser.class)
                .addParser("*.json", JsonMetaDataParser.class)
                .setShouldRegister(true)
                .setStrict(true)
                .setVerbose(false)
        );
        
        // Load base metadata
        loader.loadMetaData("metadata/types/base-types.xml");
        loader.loadMetaData("metadata/models/common.xml");
        loader.loadMetaData("metadata/models/user.xml");
        
        // Load environment-specific overlays
        loadEnvironmentSpecificMetadata(loader, environment);
        
        // Load tenant-specific overlays
        loadTenantSpecificMetadata(loader, tenant);
        
        return loader;
    }
    
    private void loadEnvironmentSpecificMetadata(MetaDataLoader loader, String env) {
        switch (env.toLowerCase()) {
            case "dev" -> {
                loader.loadMetaData("metadata/overlays/dev/database.xml");
                loader.loadMetaData("metadata/overlays/dev/debug.xml");
            }
            case "staging" -> {
                loader.loadMetaData("metadata/overlays/staging/database.xml");
                loader.loadMetaData("metadata/overlays/staging/monitoring.xml");
            }
            case "prod" -> {
                loader.loadMetaData("metadata/overlays/prod/database.xml");
                loader.loadMetaData("metadata/overlays/prod/security.xml");
                loader.loadMetaData("metadata/overlays/prod/compliance.xml");
            }
        }
    }
    
    private void loadTenantSpecificMetadata(MetaDataLoader loader, String tenant) {
        String tenantMetadataPath = "metadata/tenants/" + tenant + "/";
        
        // Load tenant-specific field extensions
        if (resourceExists(tenantMetadataPath + "fields.xml")) {
            loader.loadMetaData(tenantMetadataPath + "fields.xml");
        }
        
        // Load tenant-specific validation rules
        if (resourceExists(tenantMetadataPath + "validations.xml")) {
            loader.loadMetaData(tenantMetadataPath + "validations.xml");
        }
        
        // Load tenant-specific UI customizations
        if (resourceExists(tenantMetadataPath + "ui.xml")) {
            loader.loadMetaData(tenantMetadataPath + "ui.xml");
        }
    }
}
```

### 2. Version-Aware Metadata

**Handle multiple versions of entities:**

```xml
<!-- Version 1 of User entity -->
<metadata package="yourproject::domain::v1">
    <object name="User" type="pojo">
        <attr name="version">1.0</attr>
        <attr name="object">com.yourproject.domain.v1.User</attr>
        
        <field name="id" super="..::common::id"/>
        <field name="username" type="string"/>
        <field name="email" super="..::common::email"/>
    </object>
</metadata>

<!-- Version 2 of User entity -->
<metadata package="yourproject::domain::v2">
    <object name="User" type="pojo">
        <attr name="version">2.0</attr>
        <attr name="object">com.yourproject.domain.v2.User</attr>
        <attr name="migratesFrom">yourproject::domain::v1::User</attr>
        
        <field name="id" super="..::common::id"/>
        <field name="firstName" type="string"/>  <!-- Split username into firstName/lastName -->
        <field name="lastName" type="string"/>
        <field name="email" super="..::common::email"/>
        <field name="phoneNumber" type="string"/> <!-- New field -->
    </object>
</metadata>
```

**Migration service using metadata:**

```java
@Service
public class EntityMigrationService {
    
    private final MetaDataLoader metaDataLoader;
    
    public Object migrateEntity(Object oldEntity, String targetVersion) {
        MetaObject oldMetaObject = MetaDataRegistry.findMetaObject(oldEntity);
        String currentVersion = oldMetaObject.getMetaAttr("version").getValueAsString();
        
        if (currentVersion.equals(targetVersion)) {
            return oldEntity; // No migration needed
        }
        
        // Find target metadata
        String targetPackage = oldMetaObject.getPackage().replace("::v" + currentVersion.charAt(0), "::v" + targetVersion.charAt(0));
        MetaObject targetMetaObject = metaDataLoader.getMetaObject(targetPackage + "::" + oldMetaObject.getName());
        
        // Create new instance
        Object newEntity = targetMetaObject.newInstance();
        
        // Map fields from old to new
        for (MetaField targetField : targetMetaObject.getMetaFields()) {
            String fieldName = targetField.getName();
            
            // Direct field mapping
            if (oldMetaObject.hasMetaField(fieldName)) {
                Object value = oldMetaObject.getValue(oldMetaObject.getMetaField(fieldName), oldEntity);
                targetMetaObject.setValue(targetField, newEntity, value);
            }
            // Custom field mapping based on migration rules
            else {
                Object mappedValue = applyMigrationRule(oldEntity, oldMetaObject, targetField);
                if (mappedValue != null) {
                    targetMetaObject.setValue(targetField, newEntity, mappedValue);
                }
            }
        }
        
        return newEntity;
    }
    
    private Object applyMigrationRule(Object oldEntity, MetaObject oldMetaObject, MetaField targetField) {
        // Example: Split username into firstName/lastName
        if ("firstName".equals(targetField.getName()) && oldMetaObject.hasMetaField("username")) {
            String username = (String) oldMetaObject.getValue(oldMetaObject.getMetaField("username"), oldEntity);
            if (username != null && username.contains(" ")) {
                return username.split(" ")[0];
            }
        }
        
        if ("lastName".equals(targetField.getName()) && oldMetaObject.hasMetaField("username")) {
            String username = (String) oldMetaObject.getValue(oldMetaObject.getMetaField("username"), oldEntity);
            if (username != null && username.contains(" ")) {
                String[] parts = username.split(" ");
                return parts.length > 1 ? parts[1] : "";
            }
        }
        
        return null;
    }
}
```

## Integration with Existing Frameworks

### 1. Spring Boot Integration

**Auto-configuration for Spring Boot:**

```java
@Configuration
@EnableAutoConfiguration
public class MetaObjectsAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MetaDataLoader metaDataLoader(@Value("${metaobjects.metadata.path:metadata}") String metadataPath) {
        return new FileMetaDataLoader(
            new FileLoaderOptions()
                .addParser("*.xml", XMLMetaDataParser.class)  
                .addParser("*.json", JsonMetaDataParser.class)
                .addSources(new LocalFileMetaDataSources(metadataPath))
                .setShouldRegister(true)
        );
    }
    
    @Bean
    public PIIProtectionService piiProtectionService() {
        return new PIIProtectionService();
    }
    
    @Bean
    public MetaDataValidationService validationService() {
        return new MetaDataValidationService();
    }
}
```

### 2. JPA Integration

**Custom JPA repository with metadata awareness:**

```java
@Repository
public class MetaDataAwareRepository<T, ID> {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final Class<T> entityClass;
    private final MetaObject metaObject;
    
    public MetaDataAwareRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.metaObject = MetaDataRegistry.findMetaObject(entityClass);
    }
    
    public List<T> findByDynamicCriteria(Map<String, Object> criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        
        List<Predicate> predicates = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            // Verify field exists in metadata
            if (metaObject.hasMetaField(fieldName)) {
                MetaField field = metaObject.getMetaField(fieldName);
                
                // Get database column name from metadata
                String columnName = fieldName;
                if (field.hasMetaAttr("columnName")) {
                    columnName = field.getMetaAttr("columnName").getValueAsString();
                }
                
                // Create predicate based on field type and value
                predicates.add(createPredicate(cb, root, columnName, value, field));
            }
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
    
    private Predicate createPredicate(CriteriaBuilder cb, Root<T> root, String fieldName, Object value, MetaField field) {
        if (value instanceof String stringValue) {
            // Use LIKE for string fields by default
            return cb.like(cb.lower(root.get(fieldName)), "%" + stringValue.toLowerCase() + "%");
        } else {
            // Use equals for other types
            return cb.equal(root.get(fieldName), value);
        }
    }
    
    public void saveWithValidation(T entity) {
        // Perform metadata-driven validation before saving
        validateEntity(entity);
        entityManager.persist(entity);
    }
    
    private void validateEntity(T entity) {
        for (MetaField field : metaObject.getMetaFields()) {
            Object value = metaObject.getValue(field, entity);
            
            for (MetaValidator validator : field.getMetaValidators()) {
                ValidationResult result = validator.validate(value);
                if (!result.isValid()) {
                    throw new ValidationException("Validation failed for field " + field.getName() + ": " + result.getErrorMessage());
                }
            }
        }
    }
}
```

## Performance Optimization Patterns

### 1. Metadata Caching

**Cache metadata for performance:**

```java
@Service
@Slf4j
public class CachedMetaDataService {
    
    private final LoadingCache<String, MetaObject> metaObjectCache;
    private final LoadingCache<String, FormDefinition> formCache;
    
    public CachedMetaDataService() {
        this.metaObjectCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build(this::loadMetaObject);
                
        this.formCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build(this::generateFormDefinition);
    }
    
    public MetaObject getMetaObject(String name) {
        return metaObjectCache.get(name);
    }
    
    public FormDefinition getFormDefinition(String entityName) {
        return formCache.get(entityName);
    }
    
    @EventListener
    public void handleMetaDataChange(MetaDataChangeEvent event) {
        // Invalidate affected cache entries
        for (String changedObject : event.getChangedObjects()) {
            metaObjectCache.invalidate(changedObject);
            formCache.invalidate(changedObject);
        }
        
        log.info("Invalidated cache for changed metadata: {}", event.getChangedObjects());
    }
}
```

### 2. Lazy Loading Patterns

**Load metadata only when needed:**

```java
@Component
public class LazyMetaDataLoader {
    
    private final Map<String, Supplier<MetaObject>> lazyLoaders = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        // Register lazy loaders for different packages
        registerLazyLoader("user", () -> loadUserMetadata());
        registerLazyLoader("order", () -> loadOrderMetadata());
        registerLazyLoader("product", () -> loadProductMetadata());
    }
    
    private void registerLazyLoader(String key, Supplier<MetaObject> loader) {
        lazyLoaders.put(key, Suppliers.memoize(loader));
    }
    
    public MetaObject getMetaObject(String name) {
        String packageKey = extractPackageKey(name);
        
        Supplier<MetaObject> loader = lazyLoaders.get(packageKey);
        if (loader != null) {
            return loader.get();
        }
        
        throw new IllegalArgumentException("No metadata loader found for: " + name);
    }
    
    private MetaObject loadUserMetadata() {
        log.info("Loading user metadata lazily");
        // Load and return user-related metadata
        return metaDataLoader.loadFromResource("metadata/models/user.xml");
    }
}
```

These patterns demonstrate the full power of MetaObjects beyond simple code generation - enabling runtime adaptation, dynamic UI generation, automatic compliance enforcement, and sophisticated enterprise architecture management.