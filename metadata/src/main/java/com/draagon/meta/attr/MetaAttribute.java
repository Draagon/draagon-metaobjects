package com.draagon.meta.attr;

import com.draagon.meta.*;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * An attribute of any MetaDAta with enhanced validation, metrics, and type safety
 */
//@SuppressWarnings("serial")
public class MetaAttribute<T> extends MetaData implements DataTypeAware<T>, MetaDataValueHandler<T> {
    
    private static final Logger log = LoggerFactory.getLogger(MetaAttribute.class);

    public final static String TYPE_ATTR = "attr";

    private T value = null;
    private DataTypes dataType;
    
    // Enhanced validation chain for attribute-specific validation
    private volatile ValidationChain<MetaAttribute<T>> attributeValidationChain;
    

    /**
     * Constructs the MetaAttribute with enhanced validation and metrics
     */
    public MetaAttribute(String subtype, String name, DataTypes dataType ) {
        super( TYPE_ATTR, subtype, name );
        this.dataType = dataType;
        
        log.debug("Created MetaAttribute: {}:{}:{} with dataType: {}", TYPE_ATTR, subtype, name, dataType);
    }

    /**
     * Gets the primary MetaAttribute class
     */
    @Override
    public Class<MetaAttribute> getMetaDataClass() {
        return MetaAttribute.class;
    }

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
     * Validate this MetaAttribute using enhanced validation
     */
    public ValidationResult validateAttribute() {
        Instant start = Instant.now();
        
        try {
            ValidationResult result = getAttributeValidationChain().validate(this);
            
            
            return result;
        } catch (Exception e) {
            
            log.error("Attribute validation failed for {}: {}", getName(), e.getMessage(), e);
            
            return ValidationResult.builder()
                .addError("Attribute validation failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Get the attribute validation chain (lazy initialization)
     */
    private ValidationChain<MetaAttribute<T>> getAttributeValidationChain() {
        if (attributeValidationChain == null) {
            synchronized (this) {
                if (attributeValidationChain == null) {
                    attributeValidationChain = ValidationChain.<MetaAttribute<T>>builder()
                        .addValidator(createDataTypeValidator())
                        .addValidator(createValueValidator())
                        .addValidator(createLegacyAttributeValidator())
                        .build();
                }
            }
        }
        return attributeValidationChain;
    }
    
    /**
     * Create a data type validator for this attribute
     */
    private Validator<MetaAttribute<T>> createDataTypeValidator() {
        return new Validator<MetaAttribute<T>>() {
            @Override
            public ValidationResult validate(MetaAttribute<T> attribute) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                if (attribute.getDataType() == null) {
                    builder.addError("MetaAttribute must have a data type");
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Create a value validator for this attribute
     */
    private Validator<MetaAttribute<T>> createValueValidator() {
        return new Validator<MetaAttribute<T>>() {
            @Override
            public ValidationResult validate(MetaAttribute<T> attribute) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                // Validate value against data type if value is present
                if (attribute.getValue() != null && attribute.getDataType() != null) {
                    try {
                        // Attempt to convert value to validate compatibility
                        DataConverter.toType(attribute.getDataType(), attribute.getValue());
                    } catch (Exception e) {
                        builder.addError("Value '" + attribute.getValue() + 
                                       "' is not compatible with data type " + attribute.getDataType());
                    }
                }
                
                return builder.build();
            }
        };
    }
    
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
    
    /**
     * Create a legacy validator wrapper for MetaAttribute
     */
    private Validator<MetaAttribute<T>> createLegacyAttributeValidator() {
        return new Validator<MetaAttribute<T>>() {
            @Override
            public ValidationResult validate(MetaAttribute<T> attribute) {
                // Just use the basic validation from the parent
                try {
                    attribute.validate(); // Call existing validate method
                    return ValidationResult.builder().build(); // Success
                } catch (Exception e) {
                    return ValidationResult.builder()
                        .addError("Legacy validation failed: " + e.getMessage())
                        .build();
                }
            }
        };
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
        Instant start = Instant.now();
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
}
