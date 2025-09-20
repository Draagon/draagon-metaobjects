package com.draagon.meta.registry;

import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.field.*;
import com.draagon.meta.key.*;
import com.draagon.meta.validator.*;
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
 *   <li><strong>Key Types:</strong> primary, foreign, secondary keys</li>
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
        
        // Register base types (abstract versions that can be extended)
        registerBaseTypes(registry);
        
        // Register field types
        registerFieldTypes(registry);
        
        // Register key types
        registerKeyTypes(registry);
        
        // Register validator types
        registerValidatorTypes(registry);
        
        // Register view types
        registerViewTypes(registry);
        
        // Register object types
        registerObjectTypes(registry);
        
        log.info("Registered all core MetaData types");
    }
    
    @Override
    public void enhanceValidation(MetaDataTypeRegistry registry) {
        // ValidationChain system has been replaced with constraint system
        // Constraints are now enforced during metadata construction
        log.debug("Validation enhancement skipped - using constraint system instead");
    }
    
    @Override
    public void registerDefaults(MetaDataTypeRegistry registry) {
        log.debug("Registering default subtypes for core types");
        
        // StringField declares itself as default for "field" type
        registry.registerDefaultSubType("field", "string");
        
        // Default object type is "pojo" 
        registry.registerDefaultSubType("object", "pojo");
        
        // Default view type is "base"
        registry.registerDefaultSubType("view", "base");
        
        // Default validator is "required"
        registry.registerDefaultSubType("validator", "required");
        
        // Default key type is "primary"
        registry.registerDefaultSubType("key", "primary");
        
        // Default attr type is "string"
        registry.registerDefaultSubType("attr", "string");
        
        log.debug("Registered default subtypes for core types");
    }
    
    /**
     * Register additional missing types that are needed by XML metadata parsing.
     * These handle cases where XML metadata references types not yet registered.
     */
    private void registerBaseTypes(MetaDataTypeRegistry registry) {
        log.debug("Registered {} additional base types", 0);
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
     * Register all key types - core common metadata patterns for primary keys, foreign keys, etc.
     */
    private void registerKeyTypes(MetaDataTypeRegistry registry) {
        registry.registerHandler(new MetaDataTypeId("key", "primary"), PrimaryKey.class);
        registry.registerHandler(new MetaDataTypeId("key", "foreign"), ForeignKey.class);
        registry.registerHandler(new MetaDataTypeId("key", "secondary"), SecondaryKey.class);
        
        log.debug("Registered {} key types", 3);
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
        // Range validator uses NumericValidator for now (can handle min/max ranges)
        registry.registerHandler(new MetaDataTypeId("validator", "range"), NumericValidator.class);
        // Options validator uses RegexValidator for now (can validate against comma-separated options)
        registry.registerHandler(new MetaDataTypeId("validator", "options"), RegexValidator.class);
        // Pattern validator uses RegexValidator for pattern matching
        registry.registerHandler(new MetaDataTypeId("validator", "pattern"), RegexValidator.class);
        // Date range validator uses NumericValidator for date range validation
        registry.registerHandler(new MetaDataTypeId("validator", "daterange"), NumericValidator.class);
        
        log.debug("Registered {} validator types", 9);
    }
    
    /**
     * Register all view types
     */
    private void registerViewTypes(MetaDataTypeRegistry registry) {
        // Register base view type
        registry.registerHandler(new MetaDataTypeId("view", "base"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        
        // Register common view types used in metadata
        registry.registerHandler(new MetaDataTypeId("view", "numeric"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("view", "text"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("view", "textarea"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("view", "slider"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("view", "currency"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("view", "date"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        
        // Note: Specific view implementations (TextView, etc.) are in the web module
        // and will be registered by WebMetaDataTypeProvider
        
        log.debug("Registered {} view types", 7);
    }
    
    /**
     * Register object types 
     */
    private void registerObjectTypes(MetaDataTypeRegistry registry) {
        // Register MetaModel object type using MappedMetaObject for JSON metadata reading
        registry.registerHandler(new MetaDataTypeId("metaObject", "metaModel"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        
        // Register domain object types for JSON metadata parsing
        registry.registerHandler(new MetaDataTypeId("object", "proxy"), 
            com.draagon.meta.object.proxy.ProxyMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("object", "map"), 
            com.draagon.meta.object.mapped.MappedMetaObject.class);
        registry.registerHandler(new MetaDataTypeId("object", "pojo"), 
            com.draagon.meta.object.pojo.PojoMetaObject.class);
        // Register "value" subtype for environments where CoreObjectTypeProvider isn't available
        try {
            @SuppressWarnings("unchecked")
            Class<? extends com.draagon.meta.MetaData> valueObjectClass = 
                (Class<? extends com.draagon.meta.MetaData>) Class.forName("com.draagon.meta.object.value.ValueMetaObject");
            registry.registerHandler(new MetaDataTypeId("object", "value"), valueObjectClass);
            log.debug("Registered object.value type from core module");
        } catch (ClassNotFoundException e) {
            // ValueMetaObject not available - skip registration
            log.debug("ValueMetaObject not available, skipping object.value registration");
        }
        
        // Register default object type (when subType is null/empty) - use pojo as default
        registry.registerHandler(new MetaDataTypeId("object", "default"), 
            com.draagon.meta.object.pojo.PojoMetaObject.class);
        
        // Register attribute types for JSON metadata parsing
        registry.registerHandler(new MetaDataTypeId("attr", "string"), 
            com.draagon.meta.attr.StringAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "boolean"), 
            com.draagon.meta.attr.BooleanAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "int"), 
            com.draagon.meta.attr.IntAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "stringarray"), 
            com.draagon.meta.attr.StringArrayAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "properties"), 
            com.draagon.meta.attr.PropertiesAttribute.class);
        registry.registerHandler(new MetaDataTypeId("attr", "class"), 
            com.draagon.meta.attr.ClassAttribute.class);
        
        log.debug("Registered {} object types and {} attribute types", 6, 6);
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority - register core types first
    }
    
    @Override
    public String getDescription() {
        return "Core MetaData Type Provider - registers built-in field, validator, view, and object types";
    }
}