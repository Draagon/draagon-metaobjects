package com.draagon.meta.registry;

import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.field.*;
import com.draagon.meta.validator.*;
import com.draagon.meta.view.MetaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core MetaData type provider that registers all built-in field, validator, and view types.
 * 
 * <p>This provider is automatically discovered via ServiceLoader and registers all the
 * standard MetaData implementations that ship with the framework:</p>
 * 
 * <ul>
 *   <li><strong>Field Types:</strong> string, int, long, double, boolean, date, etc.</li>
 *   <li><strong>Validator Types:</strong> required, regex, numeric, length, array</li>
 *   <li><strong>View Types:</strong> Basic view implementations</li>
 * </ul>
 * 
 * <p>This replaces the need to update individual classes with static registration blocks,
 * providing a centralized and maintainable approach to type registration.</p>
 * 
 * @since 6.0.0
 */
public class CoreMetaDataTypeProvider implements MetaDataTypeProvider {
    
    private static final Logger log = LoggerFactory.getLogger(CoreMetaDataTypeProvider.class);
    
    @Override
    public void registerTypes(MetaDataTypeRegistry registry) {
        log.debug("Registering core MetaData types");
        
        // Register field types
        registerFieldTypes(registry);
        
        // Register validator types
        registerValidatorTypes(registry);
        
        // Register view types
        registerViewTypes(registry);
        
        log.info("Registered all core MetaData types");
    }
    
    @Override
    public void enhanceValidation(MetaDataTypeRegistry registry) {
        log.debug("Enhancing validation for core types");
        
        // Add common field validation
        registry.enhanceValidationChain(
            MetaDataTypeId.pattern("field", "*"),
            metaData -> {
                // Basic field validation - ensure name is valid
                if (metaData.getName() == null || metaData.getName().trim().isEmpty()) {
                    return ValidationResult.withError("Field name cannot be null or empty");
                }
                return ValidationResult.success();
            }
        );
        
        // Add validator-specific validation  
        registry.enhanceValidationChain(
            MetaDataTypeId.pattern("validator", "*"),
            metaData -> {
                // Ensure validators have proper configuration
                if (metaData.getName() == null || metaData.getName().trim().isEmpty()) {
                    return ValidationResult.withError("Validator name cannot be null or empty");
                }
                return ValidationResult.success();
            }
        );
        
        log.debug("Enhanced validation for core types");
    }
    
    /**
     * Register all field types
     */
    private void registerFieldTypes(MetaDataTypeRegistry registry) {
        // Primitive field types
        registry.registerHandler(new MetaDataTypeId("field", "string"), StringField.class);
        registry.registerHandler(new MetaDataTypeId("field", "int"), IntegerField.class);
        registry.registerHandler(new MetaDataTypeId("field", "long"), LongField.class);
        registry.registerHandler(new MetaDataTypeId("field", "short"), ShortField.class);
        registry.registerHandler(new MetaDataTypeId("field", "byte"), ByteField.class);
        registry.registerHandler(new MetaDataTypeId("field", "double"), DoubleField.class);
        registry.registerHandler(new MetaDataTypeId("field", "float"), FloatField.class);
        registry.registerHandler(new MetaDataTypeId("field", "boolean"), BooleanField.class);
        registry.registerHandler(new MetaDataTypeId("field", "date"), DateField.class);
        
        // Complex field types
        registry.registerHandler(new MetaDataTypeId("field", "stringArray"), StringArrayField.class);
        registry.registerHandler(new MetaDataTypeId("field", "object"), ObjectField.class);
        registry.registerHandler(new MetaDataTypeId("field", "objectArray"), ObjectArrayField.class);
        registry.registerHandler(new MetaDataTypeId("field", "class"), ClassField.class);
        
        log.debug("Registered {} field types", 12);
    }
    
    /**
     * Register all validator types
     */
    private void registerValidatorTypes(MetaDataTypeRegistry registry) {
        registry.registerHandler(new MetaDataTypeId("validator", "required"), RequiredValidator.class);
        registry.registerHandler(new MetaDataTypeId("validator", "regex"), RegexValidator.class);
        registry.registerHandler(new MetaDataTypeId("validator", "numeric"), NumericValidator.class);
        registry.registerHandler(new MetaDataTypeId("validator", "length"), LengthValidator.class);
        registry.registerHandler(new MetaDataTypeId("validator", "array"), ArrayValidator.class);
        
        log.debug("Registered {} validator types", 5);
    }
    
    /**
     * Register all view types
     */
    private void registerViewTypes(MetaDataTypeRegistry registry) {
        // Register base view type
        registry.registerHandler(new MetaDataTypeId("view", "base"), MetaView.class);
        
        // Note: Specific view implementations (TextView, etc.) are in the web module
        // and will be registered by WebMetaDataTypeProvider
        
        log.debug("Registered {} view types", 1);
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority - register core types first
    }
    
    @Override
    public String getDescription() {
        return "Core MetaData Type Provider - registers built-in field, validator, and view types";
    }
}