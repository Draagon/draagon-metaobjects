package com.draagon.meta.attr;

import com.draagon.meta.*;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.registry.MetaDataRegistry;
import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * An attribute of any MetaDAta with enhanced validation, metrics, and type safety
 */
public class MetaAttribute<T> extends MetaData implements DataTypeAware<T>, MetaDataValueHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(MetaAttribute.class);

    public final static String TYPE_ATTR = "attr";
    public final static String SUBTYPE_BASE = "base";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        // Register base attribute type
        registry.registerType(MetaAttribute.class, def -> def
            .type(TYPE_ATTR).subType(SUBTYPE_BASE)
            .description("Base attribute metadata with common attribute properties")
            .inheritsFrom(MetaData.TYPE_METADATA, MetaData.SUBTYPE_BASE)
            .optionalAttribute(ATTR_IS_ABSTRACT, BooleanAttribute.ATTR_SUBTYPE)
        );

        // Register cross-cutting attribute constraints
        registerCrossCuttingAttributeConstraints(registry);
    }

    private T value = null;
    private DataTypes dataType;
    
    

    /**
     * Constructs the MetaAttribute with enhanced validation and metrics
     */
    public MetaAttribute(String subtype, String name, DataTypes dataType ) {
        super( TYPE_ATTR, subtype, name );
        this.dataType = dataType;
        
        log.debug("Created MetaAttribute: {}:{}:{} with dataType: {}", TYPE_ATTR, subtype, name, dataType);
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class

    /**
     * Sets an attribute of the MetaClass
     */
    //@Override
    //public MetaAttribute addMetaAttr(MetaAttribute attr) {
    //    return addChild(attr);
    //}

    /** Add Child to the Field */
    //@Override
    //public MetaAttribute addChild(MetaData data) throws InvalidMetaDataException {
    //    return super.addChild( data );
    //}

    /** Wrap the MetaAttribute */
    //@Override
    //public MetaAttribute overload() {
    //    return super.overload();
    //}

    /**
     * Returns the DataType for the value
     * @return DataTypes enum
     */
    @Override
    public DataTypes getDataType() {
        return dataType;
    }
    
    // ========== ENHANCED ATTRIBUTE-SPECIFIC METHODS ==========
    
    
    
    
    /**
     * Safe value getter with Optional wrapper
     */
    public Optional<T> getValueSafe() {
        return Optional.ofNullable(value);
    }
    
    /**
     * Check if this attribute has a value
     */
    public boolean hasValue() {
        return value != null;
    }
    
    /**
     * Get value with fallback
     */
    public T getValueOrDefault(T defaultValue) {
        return value != null ? value : defaultValue;
    }
    

    /////////////////////////////////////////////////////////////////////////////////
    // MetaData Value Handler Methods

    /**
     * Sets the value of the MetaAttribute with enhanced tracking
     */
    @Override
    public void setValue( T value ) {
        T oldValue = this.value;
        this.value = value;
        
        
        log.debug("MetaAttribute {} value changed from {} to {}", getName(), oldValue, value);
    }

    /**
     * Returns the value of the MetaAttribute
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Sets the value as a String
     *
     * @param value String value of the attribute
     */
    @Override
    public void setValueAsString(String value) {
        setValueAsObject( value );
    }

    /**
     * Returns the value of the MetaAttribute as a String
     */
    @Override
    public String getValueAsString() {
        return DataConverter.toString( value );
    }

    /**
     * Sets the Value with an Object with enhanced validation and tracking
     * @param value Object value to set
     */
    @Override
    public void setValueAsObject(Object value) {
        T oldValue = this.value;
        
        try {
            this.value = DataConverter.toTypeSafe( dataType, value, (Class<T>) dataType.getValueClass() );
            
            
            log.debug("MetaAttribute {} value converted and set from {} to {}", getName(), oldValue, this.value);
            
        } catch (Exception e) {
            
            log.error("Failed to convert value for MetaAttribute {}: {}", getName(), e.getMessage(), e);
            throw e; // Re-throw to maintain existing behavior
        }
    }

    /**
     * Clone the MetaAttribute
     * @return MetaAttribute clone
     */
    @Override
    public Object clone() {
        MetaAttribute<T> a = (MetaAttribute<T>) super.clone();
        a.value = value;
        return a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MetaAttribute<?> that = (MetaAttribute<?>) o;
        return Objects.equals(value, that.value) &&
                dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, dataType);
    }

    /** Get the toString Prefix */
    @Override
    protected String getToStringPrefix() {
        return  super.getToStringPrefix() + "{dataType=" + dataType + ", value=" + value + "}";
    }
    
    /**
     * Register cross-cutting attribute constraints using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerCrossCuttingAttributeConstraints(MetaDataRegistry registry) {
        try {
            // PLACEMENT CONSTRAINT: Attributes can be placed on any MetaData
            registry.registerPlacementConstraint(
                "attribute.universal.placement",
                "Attributes can be placed on any MetaData",
                (metadata) -> metadata instanceof MetaData,
                (child) -> child instanceof MetaAttribute
            );

            // VALIDATION CONSTRAINT: Attribute names must follow identifier pattern
            registry.registerValidationConstraint(
                "attribute.naming.pattern",
                "Attribute names must follow identifier pattern",
                (metadata) -> metadata instanceof MetaAttribute,
                (metadata, value) -> {
                    String name = metadata.getName();
                    if (name == null) return false;
                    return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                }
            );

            // Registered cross-cutting attribute constraints using consolidated registry

        } catch (Exception e) {
            // Failed to register cross-cutting attribute constraints - logging not available during provider registration
            throw new RuntimeException("Failed to register cross-cutting attribute constraints", e);
        }
    }
}
