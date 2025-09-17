package com.draagon.meta.registry;

/**
 * Service provider interface for registering MetaData types and enhancing validation.
 * 
 * <p>Implementations of this interface are discovered automatically by the
 * {@link MetaDataTypeRegistry} via the {@link ServiceRegistry}. This enables
 * dynamic type registration and validation enhancement without configuration files.</p>
 * 
 * <h3>Implementation Guidelines:</h3>
 * 
 * <p><strong>For Java ServiceLoader:</strong> Add your implementation to 
 * {@code META-INF/services/com.draagon.meta.registry.MetaDataTypeProvider}</p>
 * 
 * <p><strong>For OSGI:</strong> Register as a service in your bundle activator or 
 * use declarative services with {@code @Component}</p>
 * 
 * <h3>Example Implementation:</h3>
 * <pre>{@code
 * public class CoreMetaDataTypeProvider implements MetaDataTypeProvider {
 *     
 *     @Override
 *     public void registerTypes(MetaDataTypeRegistry registry) {
 *         // Register field types
 *         registry.registerHandler(
 *             new MetaDataTypeId("field", "string"), 
 *             StringField.class
 *         );
 *         registry.registerHandler(
 *             new MetaDataTypeId("field", "int"), 
 *             IntegerField.class
 *         );
 *         
 *         // Register view types
 *         registry.registerHandler(
 *             new MetaDataTypeId("view", "text"), 
 *             TextView.class
 *         );
 *     }
 *     
 *     @Override
 *     public void enhanceValidation(MetaDataTypeRegistry registry) {
 *         // Add validation enhancement for all field types
 *         registry.enhanceValidationChain(
 *             MetaDataTypeId.pattern("field", "*"),
 *             new FieldNameValidator()
 *         );
 *     }
 * }
 * }</pre>
 * 
 * <h3>Plugin Extension Example:</h3>
 * <pre>{@code
 * public class CurrencyExtensionProvider implements MetaDataTypeProvider {
 *     
 *     @Override  
 *     public void registerTypes(MetaDataTypeRegistry registry) {
 *         // Register new currency field type
 *         registry.registerHandler(
 *             new MetaDataTypeId("field", "currency"), 
 *             CurrencyField.class
 *         );
 *         
 *         // Register account object type
 *         registry.registerHandler(
 *             new MetaDataTypeId("object", "account"),
 *             AccountObject.class
 *         );
 *     }
 *     
 *     @Override
 *     public void enhanceValidation(MetaDataTypeRegistry registry) {
 *         // Enhance existing field validation with currency rules
 *         registry.enhanceValidationChain(
 *             MetaDataTypeId.pattern("field", "*"),
 *             new CurrencyFieldValidator()
 *         );
 *         
 *         // Add account-specific validation rules
 *         registry.enhanceValidationChain(
 *             new MetaDataTypeId("object", "account"),
 *             new AccountObjectValidator()
 *         );
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
public interface MetaDataTypeProvider {
    
    /**
     * Register MetaData types with the registry.
     * 
     * <p>This method is called during registry initialization to register
     * all types that this provider supports. Each type should be registered
     * with its corresponding handler class.</p>
     * 
     * <p><strong>Important:</strong> This method may be called multiple times,
     * so implementations should be idempotent (safe to call repeatedly).</p>
     * 
     * @param registry The registry to register types with
     */
    void registerTypes(MetaDataTypeRegistry registry);
    
    /**
     * Register default subtypes for primary types.
     * 
     * <p>This method is called after {@link #registerTypes(MetaDataTypeRegistry)} 
     * to allow providers to declare default subtypes for their registered types.
     * Later registrations override earlier ones (last-wins semantics).</p>
     * 
     * <p>Example usage:</p>
     * <pre>{@code
     * @Override
     * public void registerDefaults(MetaDataTypeRegistry registry) {
     *     // StringField declares itself as default for "field" type
     *     registry.registerDefaultSubType("field", "string");
     *     registry.registerDefaultSubType("object", "pojo");
     *     registry.registerDefaultSubType("view", "base");
     * }
     * }</pre>
     * 
     * <p><strong>Override Semantics:</strong> If multiple providers register
     * defaults for the same type, the last one wins. This allows plugins to
     * override core defaults with custom implementations.</p>
     * 
     * <p><strong>Important:</strong> This method may be called multiple times,
     * so implementations should be idempotent.</p>
     * 
     * @param registry The registry to register default subtypes with
     */
    default void registerDefaults(MetaDataTypeRegistry registry) {
        // Default implementation does nothing - providers can override
    }
    
    /**
     * Enhance validation chains for existing types.
     * 
     * <p>This method is called after all types have been registered and allows
     * providers to add additional validation to existing types. This enables
     * plugins to enhance the validation of core types with domain-specific rules.</p>
     * 
     * <p>Use {@link com.draagon.meta.MetaDataTypeId#pattern(String, String)} to create
     * wildcard patterns for matching multiple types:</p>
     * 
     * <ul>
     *   <li>{@code MetaDataTypeId.pattern("field", "*")} - All field types</li>
     *   <li>{@code MetaDataTypeId.pattern("*", "string")} - All string subtypes</li>
     *   <li>{@code MetaDataTypeId.pattern("*", "*")} - All types</li>
     * </ul>
     * 
     * <p><strong>Important:</strong> This method may be called multiple times,
     * so implementations should be idempotent.</p>
     * 
     * @param registry The registry to enhance validation for
     */
    void enhanceValidation(MetaDataTypeRegistry registry);
    
    /**
     * Get the priority of this provider (optional).
     * 
     * <p>Lower numbers = higher priority. Providers with higher priority
     * are processed first during registration. Default priority is 100.</p>
     * 
     * <p>Use this to ensure certain types are registered before others,
     * or to override default implementations with custom ones.</p>
     * 
     * @return Priority value (lower = higher priority)
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Get a description of this provider (optional).
     * 
     * <p>Used for logging and debugging purposes.</p>
     * 
     * @return Human-readable description
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}