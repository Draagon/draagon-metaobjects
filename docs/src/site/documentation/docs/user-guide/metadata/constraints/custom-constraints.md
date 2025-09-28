# Creating Custom Constraints

MetaObjects provides a flexible constraint system that allows you to create custom validation and placement rules tailored to your specific business requirements. This guide walks you through implementing custom constraints using the framework's extensible architecture.

## When to Create Custom Constraints

Consider creating custom constraints when you need:

- **Business-specific validation rules** not covered by built-in constraints
- **Complex placement logic** for domain-specific metadata relationships
- **Cross-field validation** that requires examining multiple metadata properties
- **Industry-specific patterns** (e.g., financial, healthcare, government regulations)

## Custom Validation Constraints

Validation constraints inherit from `BaseConstraint` and validate values against custom business rules.

### Step 1: Extend BaseConstraint

```java
package com.example.constraints;

import com.metaobjects.constraint.BaseConstraint;
import com.metaobjects.constraint.ConstraintViolationException;
import com.metaobjects.MetaData;

/**
 * Custom constraint for validating email addresses with domain restrictions.
 * Demonstrates business-specific validation beyond simple regex patterns.
 */
public class CorporateEmailConstraint extends BaseConstraint {

    private final Set<String> allowedDomains;
    private final boolean requireTLD;

    public CorporateEmailConstraint(String constraintId, String description,
                                  String targetType, String targetSubType, String targetName,
                                  Set<String> allowedDomains, boolean requireTLD) {
        super(constraintId, description, targetType, targetSubType, targetName);
        this.allowedDomains = new HashSet<>(allowedDomains);
        this.requireTLD = requireTLD;
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (value == null) {
            return; // Null values handled by RequiredConstraint if needed
        }

        String email = value.toString().trim().toLowerCase();

        // Basic email format validation
        if (!isValidEmailFormat(email)) {
            throw new ConstraintViolationException(
                String.format("Value '%s' is not a valid email format for %s '%s'",
                    value, metaData.getType(), metaData.getName()),
                getConstraintId(),
                metaData
            );
        }

        // Domain restriction validation
        String domain = extractDomain(email);
        if (!allowedDomains.isEmpty() && !allowedDomains.contains(domain)) {
            throw new ConstraintViolationException(
                String.format("Email domain '%s' is not in allowed domains %s for %s '%s'",
                    domain, allowedDomains, metaData.getType(), metaData.getName()),
                getConstraintId(),
                metaData
            );
        }

        // TLD requirement validation
        if (requireTLD && !hasValidTLD(domain)) {
            throw new ConstraintViolationException(
                String.format("Email domain '%s' must have valid TLD for %s '%s'",
                    domain, metaData.getType(), metaData.getName()),
                getConstraintId(),
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "corporate-email";
    }

    // Helper methods
    private boolean isValidEmailFormat(String email) {
        return email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    }

    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(atIndex + 1) : "";
    }

    private boolean hasValidTLD(String domain) {
        return domain.contains(".") &&
               domain.substring(domain.lastIndexOf('.') + 1).length() >= 2;
    }
}
```

### Step 2: Factory Methods for Convenience

```java
public class CorporateEmailConstraint extends BaseConstraint {

    // ... previous code ...

    /**
     * Create constraint for corporate email addresses
     * @param constraintId Unique constraint identifier
     * @param allowedDomains Set of allowed email domains
     * @return Configured constraint
     */
    public static CorporateEmailConstraint forCorporateDomains(String constraintId,
                                                             Set<String> allowedDomains) {
        return new CorporateEmailConstraint(
            constraintId,
            "Email must be from corporate domains: " + allowedDomains,
            "field", "string", "email",
            allowedDomains,
            true
        );
    }

    /**
     * Create constraint for any valid email with TLD requirement
     * @param constraintId Unique constraint identifier
     * @return Configured constraint
     */
    public static CorporateEmailConstraint forAnyValidEmail(String constraintId) {
        return new CorporateEmailConstraint(
            constraintId,
            "Email must be valid with proper TLD",
            "field", "string", "email",
            Collections.emptySet(),
            true
        );
    }

    /**
     * Create constraint for specific field name pattern
     * @param constraintId Unique constraint identifier
     * @param fieldNamePattern Pattern for field names (e.g., "*email*", "contactEmail")
     * @param allowedDomains Set of allowed domains
     * @return Configured constraint
     */
    public static CorporateEmailConstraint forFieldPattern(String constraintId,
                                                         String fieldNamePattern,
                                                         Set<String> allowedDomains) {
        return new CorporateEmailConstraint(
            constraintId,
            "Email fields matching '" + fieldNamePattern + "' must use corporate domains",
            "field", "string", fieldNamePattern,
            allowedDomains,
            true
        );
    }
}
```

### Step 3: Complex Cross-Field Validation

For more sophisticated validation that needs to examine multiple metadata properties:

```java
/**
 * Validates that database column names follow naming conventions
 * based on the field type and business rules.
 */
public class DatabaseColumnNamingConstraint extends BaseConstraint {

    private final Map<String, String> fieldTypeToColumnPrefix;
    private final Set<String> reservedWords;
    private final boolean enforceSnakeCase;

    public DatabaseColumnNamingConstraint(String constraintId, String description) {
        super(constraintId, description, "attr", "string", "dbColumn");

        // Initialize field type mappings
        this.fieldTypeToColumnPrefix = Map.of(
            "boolean", "is_",
            "date", "dt_",
            "foreign-key", "fk_"
        );

        // Initialize reserved words
        this.reservedWords = Set.of("user", "order", "group", "table", "index");
        this.enforceSnakeCase = true;
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (value == null) return;

        String columnName = value.toString().trim();

        // Get the parent field to examine its type
        MetaData parentField = metaData.getParent();
        if (parentField == null) return;

        String fieldType = parentField.getSubType();
        String fieldName = parentField.getName();

        // Validate snake_case convention
        if (enforceSnakeCase && !isSnakeCase(columnName)) {
            throw new ConstraintViolationException(
                String.format("Database column '%s' for field '%s' must use snake_case naming",
                    columnName, fieldName),
                getConstraintId(),
                metaData
            );
        }

        // Validate type-specific prefix requirements
        String requiredPrefix = fieldTypeToColumnPrefix.get(fieldType);
        if (requiredPrefix != null && !columnName.startsWith(requiredPrefix)) {
            throw new ConstraintViolationException(
                String.format("Database column '%s' for %s field '%s' should start with '%s'",
                    columnName, fieldType, fieldName, requiredPrefix),
                getConstraintId(),
                metaData
            );
        }

        // Validate against reserved words
        if (reservedWords.contains(columnName.toLowerCase())) {
            throw new ConstraintViolationException(
                String.format("Database column name '%s' is a reserved word and cannot be used",
                    columnName),
                getConstraintId(),
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "database-column-naming";
    }

    private boolean isSnakeCase(String value) {
        return value.matches("^[a-z][a-z0-9_]*$") && !value.endsWith("_");
    }
}
```

## Custom Placement Constraints

For complex placement logic that goes beyond the standard pattern matching:

### Advanced Placement Constraint

```java
/**
 * Custom placement constraint that validates complex business rules
 * for where certain metadata can be placed based on multiple criteria.
 */
public class BusinessRulePlacementConstraint extends PlacementConstraint {

    private final BusinessRuleEvaluator ruleEvaluator;

    public BusinessRulePlacementConstraint(String constraintId, String description,
                                         BusinessRuleEvaluator ruleEvaluator) {
        super(constraintId, description, "*", "*", true); // Base pattern allows everything
        this.ruleEvaluator = ruleEvaluator;
    }

    @Override
    public boolean appliesTo(MetaData parent, MetaData child) {
        // Apply business rule evaluation instead of simple pattern matching
        return ruleEvaluator.shouldApplyRule(parent, child);
    }

    /**
     * Override placement logic with custom business rules
     */
    public void validatePlacement(MetaData parent, MetaData child)
            throws ConstraintViolationException {

        BusinessRuleResult result = ruleEvaluator.evaluatePlacement(parent, child);

        if (!result.isAllowed()) {
            throw new ConstraintViolationException(
                String.format("Business rule violation: %s. %s",
                    result.getReason(), result.getSuggestedAction()),
                getConstraintId(),
                parent
            );
        }
    }

    // Business rule evaluator interface
    public interface BusinessRuleEvaluator {
        boolean shouldApplyRule(MetaData parent, MetaData child);
        BusinessRuleResult evaluatePlacement(MetaData parent, MetaData child);
    }

    // Result class for business rule evaluation
    public static class BusinessRuleResult {
        private final boolean allowed;
        private final String reason;
        private final String suggestedAction;

        public BusinessRuleResult(boolean allowed, String reason, String suggestedAction) {
            this.allowed = allowed;
            this.reason = reason;
            this.suggestedAction = suggestedAction;
        }

        // Getters...
        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
        public String getSuggestedAction() { return suggestedAction; }
    }
}
```

### Example Business Rule Implementation

```java
/**
 * Example business rule: Sensitive fields cannot be placed in objects
 * that are marked as publicly accessible.
 */
public class SensitiveDataPlacementRule implements BusinessRuleEvaluator {

    private final Set<String> sensitiveFieldTypes = Set.of(
        "ssn", "creditCard", "password", "personalId"
    );

    @Override
    public boolean shouldApplyRule(MetaData parent, MetaData child) {
        return parent instanceof MetaObject &&
               child instanceof MetaField &&
               sensitiveFieldTypes.contains(child.getSubType());
    }

    @Override
    public BusinessRuleResult evaluatePlacement(MetaData parent, MetaData child) {
        // Check if parent object is marked as publicly accessible
        if (parent.hasMetaAttr("publicAccess") &&
            Boolean.parseBoolean(parent.getMetaAttr("publicAccess").getValueAsString())) {

            return new BusinessRuleResult(
                false,
                "Sensitive field '" + child.getName() + "' cannot be placed in publicly accessible object '" + parent.getName() + "'",
                "Either remove publicAccess from the object or use a non-sensitive field type"
            );
        }

        return new BusinessRuleResult(true, "Placement allowed", "");
    }
}
```

## Constraint Registration

### Step 1: Create Provider Class

```java
package com.example.constraints;

import com.metaobjects.registry.MetaDataTypeProvider;
import com.metaobjects.registry.MetaDataRegistry;

/**
 * Provider for registering custom business constraints
 */
public class BusinessConstraintsProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Register custom validation constraints
        registry.addValidationConstraint(
            CorporateEmailConstraint.forCorporateDomains(
                "corporate.email.domains",
                Set.of("company.com", "company.org", "subsidiary.com")
            )
        );

        registry.addValidationConstraint(
            new DatabaseColumnNamingConstraint(
                "database.column.naming",
                "Database columns must follow corporate naming standards"
            )
        );

        // Register custom placement constraints
        registry.addValidationConstraint(
            new BusinessRulePlacementConstraint(
                "sensitive.data.placement",
                "Sensitive data placement rules",
                new SensitiveDataPlacementRule()
            )
        );

        // Register industry-specific constraints
        registerFinancialIndustryConstraints(registry);
        registerHealthcareConstraints(registry);
    }

    @Override
    public int getPriority() {
        return 100; // After core constraints (0-50), before application-specific (200+)
    }

    private void registerFinancialIndustryConstraints(MetaDataRegistry registry) {
        // Financial industry specific constraints
        registry.addValidationConstraint(
            new RegexConstraint(
                "financial.account.number",
                "Account numbers must follow financial industry standards",
                "field", "string", "accountNumber",
                "^[0-9]{8,20}$"
            )
        );
    }

    private void registerHealthcareConstraints(MetaDataRegistry registry) {
        // Healthcare industry specific constraints (HIPAA compliance, etc.)
        registry.addValidationConstraint(
            new RequiredConstraint(
                "healthcare.patient.id.required",
                "Patient ID fields are required for healthcare records",
                "field", "*", "patientId"
            )
        );
    }
}
```

### Step 2: Service Discovery Registration

Create the service discovery file:

```title="META-INF/services/com.metaobjects.registry.MetaDataTypeProvider"
com.example.constraints.BusinessConstraintsProvider
```

### Step 3: Test Your Constraints

```java
@Test
public void testCustomConstraints() {
    // Setup registry with custom constraints
    MetaDataRegistry registry = MetaDataRegistry.getInstance();
    new BusinessConstraintsProvider().registerTypes(registry);

    // Create test metadata
    MetaObject user = new MetaObject("User");
    MetaField emailField = new StringField("email");

    // Test constraint enforcement
    try {
        emailField.setMetaAttr("defaultValue", "invalid@wrongdomain.com");
        user.addChild(emailField); // Should trigger constraint validation
        fail("Expected ConstraintViolationException");
    } catch (ConstraintViolationException e) {
        assertThat(e.getMessage()).contains("not in allowed domains");
        assertThat(e.getConstraintId()).isEqualTo("corporate.email.domains");
    }
}
```

## Performance Considerations

### DO: Use Efficient Validation Logic

```java
// ✅ GOOD - Cache expensive computations
public class OptimizedConstraint extends BaseConstraint {
    private final Pattern compiledPattern = Pattern.compile(REGEX);
    private final Set<String> allowedValues = Set.of("val1", "val2", "val3");

    @Override
    public void validate(MetaData metaData, Object value) {
        // Use pre-compiled patterns and sets for O(1) lookups
        if (!allowedValues.contains(value.toString())) {
            // Fast rejection
        }
    }
}
```

### DON'T: Perform Expensive Operations

```java
// ❌ WRONG - Expensive operations during validation
public void validate(MetaData metaData, Object value) {
    // ❌ Don't compile regex every time
    Pattern.compile(someRegex).matcher(value.toString()).matches();

    // ❌ Don't make database calls
    if (!databaseService.isValidValue(value)) {
        throw new ConstraintViolationException(...);
    }

    // ❌ Don't create new collections
    List<String> allowedValues = Arrays.asList("val1", "val2", "val3");
}
```

### DO: Minimize Memory Allocation

```java
// ✅ GOOD - Static constants and efficient data structures
public class EfficientConstraint extends BaseConstraint {
    private static final Set<String> ALLOWED_TYPES = Set.of("type1", "type2");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    // Reuse StringBuilder for complex formatting
    private static final ThreadLocal<StringBuilder> STRING_BUILDER =
        ThreadLocal.withInitial(() -> new StringBuilder(256));
}
```

## Testing Custom Constraints

### Unit Testing Approach

```java
public class CorporateEmailConstraintTest {

    private CorporateEmailConstraint constraint;
    private MetaField testField;

    @Before
    public void setUp() {
        constraint = CorporateEmailConstraint.forCorporateDomains(
            "test.email",
            Set.of("company.com", "subsidiary.com")
        );
        testField = new StringField("email");
    }

    @Test
    public void testValidCorporateEmail() {
        // Should not throw exception
        constraint.validate(testField, "user@company.com");
        constraint.validate(testField, "admin@subsidiary.com");
    }

    @Test
    public void testInvalidDomain() {
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> constraint.validate(testField, "user@external.com")
        );

        assertThat(exception.getMessage()).contains("not in allowed domains");
        assertThat(exception.getConstraintId()).isEqualTo("test.email");
    }

    @Test
    public void testConstraintAppliesTo() {
        MetaField emailField = new StringField("email");
        MetaField nameField = new StringField("name");

        assertTrue(constraint.appliesTo(emailField));
        assertFalse(constraint.appliesTo(nameField));
    }
}
```

### Integration Testing

```java
@Test
public void testConstraintIntegrationWithMetaData() {
    // Register constraint
    MetaDataRegistry registry = MetaDataRegistry.getInstance();
    registry.addValidationConstraint(constraint);

    // Test real metadata construction
    MetaObject user = new MetaObject("User");
    MetaField emailField = new StringField("email");

    // This should trigger constraint validation
    emailField.setMetaAttr("defaultValue", "test@external.com");

    ConstraintViolationException exception = assertThrows(
        ConstraintViolationException.class,
        () -> user.addChild(emailField)
    );

    assertThat(exception.getConstraintId()).isEqualTo("test.email");
}
```

## Best Practices

### 1. Design for Schema Generation

Make your constraints serializable to schema formats:

```java
// ✅ GOOD - Declarative data that can be serialized
public class SerializableConstraint extends BaseConstraint {
    private final List<String> allowedValues;
    private final String regexPattern;
    private final Integer minLength;
    private final Integer maxLength;

    // Getters for schema generation
    public List<String> getAllowedValues() { return allowedValues; }
    public String getRegexPattern() { return regexPattern; }
    public Integer getMinLength() { return minLength; }
    public Integer getMaxLength() { return maxLength; }
}
```

### 2. Provide Clear Error Messages

```java
// ✅ GOOD - Descriptive error messages with suggestions
throw new ConstraintViolationException(
    String.format(
        "Value '%s' for field '%s' violates business rule: %s. " +
        "Allowed values are: %s. Consider using: %s",
        value, fieldName, ruleName, allowedValues, suggestedValue
    ),
    constraintId,
    metaData
);
```

### 3. Use Constraint Hierarchies

```java
// ✅ GOOD - Build constraint hierarchies for reuse
public abstract class BusinessConstraint extends BaseConstraint {
    protected final String businessDomain;

    protected BusinessConstraint(String constraintId, String description,
                               String businessDomain) {
        super(constraintId, description, "*", "*", "*");
        this.businessDomain = businessDomain;
    }
}

public class FinancialConstraint extends BusinessConstraint {
    public FinancialConstraint(String constraintId, String description) {
        super(constraintId, description, "financial");
    }
}
```

### 4. Document Your Constraints

```java
/**
 * Corporate email validation constraint for enterprise environments.
 *
 * <h3>Purpose</h3>
 * Ensures all email fields comply with corporate security policies
 * by restricting domains to approved corporate domains.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Apply to all email fields
 * CorporateEmailConstraint.forCorporateDomains("corp.email",
 *     Set.of("company.com", "subsidiary.com"));
 *
 * // Apply to specific field patterns
 * CorporateEmailConstraint.forFieldPattern("contact.email",
 *     "*Email", Set.of("company.com"));
 * }</pre>
 *
 * <h3>Schema Integration</h3>
 * This constraint generates JSON Schema patterns and XSD restrictions
 * for allowed email domains.
 *
 * @see PlacementConstraint
 * @see BaseConstraint
 * @since 6.2.0
 */
public class CorporateEmailConstraint extends BaseConstraint {
    // Implementation...
}
```

## Next Steps

<div class="grid cards" markdown>

-   :material-shield-check:{ .lg .middle } **Constraint Architecture**

    ---

    Review the complete constraint system design

    [:octicons-arrow-right-24: Constraint Architecture](constraint-architecture.md)

-   :material-tag:{ .lg .middle } **Attribute System**

    ---

    Understand how attributes work with constraints

    [:octicons-arrow-right-24: Attribute Framework](../attributes/attribute-framework.md)

-   :material-code-braces:{ .lg .middle } **Examples**

    ---

    See working examples of constraint usage

    [:octicons-arrow-right-24: Basic Usage Examples](../../../examples/basic-usage.md)

-   :material-cog:{ .lg .middle } **Type System**

    ---

    Learn about type registration and providers

    [:octicons-arrow-right-24: Type System](../type-system.md)

</div>

---

Custom constraints provide the flexibility to enforce domain-specific business rules while maintaining MetaObjects' performance characteristics and schema generation capabilities. The key is designing constraints that are both powerful and serializable to standard schema formats.