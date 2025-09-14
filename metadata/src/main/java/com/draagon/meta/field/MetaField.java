package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.MetaValidatorNotFoundException;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.view.MetaViewNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.Validator;
import com.draagon.meta.validation.MetaDataValidators;
import com.draagon.meta.metrics.MetaDataMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Optional;

/**
 * A MetaField represents a field of an object and is contained within a MetaClass.
 * It functions as both a proxy to get/set data within an object and also handles
 * accessing meta data about a field.
 *
 * @author Doug Mealing
 * @version 2.0
 */
@SuppressWarnings("serial")
public abstract class MetaField<T> extends MetaData  implements DataTypeAware<T>, MetaFieldTypes {

    private static final Logger log = LoggerFactory.getLogger(MetaField.class);

    public final static String TYPE_FIELD = "field";

    public final static String ATTR_VALIDATION = "validation";
    public final static String ATTR_DEFAULT_VIEW = "defaultView";
    public final static String ATTR_DEFAULT_VALUE = "defaultValue";

    private T defaultValue = null;
    private boolean lookedForDefault = false;

    private int length = -1;

    private DataTypes dataType;
    
    // Enhanced field-specific validation chain
    private volatile ValidationChain<MetaField<T>> fieldValidationChain;
    
    // Field-specific metrics
    private final MetaDataMetrics fieldMetrics;

    /**
     * Legacy constructor used in unit tests
     * @param name Name of the metafield
     * @deprecated Use MetaField( subtype, name, dataType )
     */
    public MetaField( String name ) {
        this( "deprecated", name, DataTypes.STRING );
    }

    /**
     * Construct a MetaField with enhanced validation and metrics
     * @param subtype SubType name for the MetaField
     * @param name Name of the MetaField
     * @param dataType The DataTypes enum used for values
     */
    public MetaField(String subtype, String name, DataTypes dataType) {
        super(TYPE_FIELD, subtype, name);
        this.dataType = dataType;
        this.fieldMetrics = new MetaDataMetrics("field:" + name);
        this.fieldMetrics.recordCreation();
        
        log.debug("Created MetaField: {}:{}:{} with dataType: {}", TYPE_FIELD, subtype, name, dataType);
        //addAttributeDef( new AttributeDef( ATTR_LEN, String.class, false, "Length of the field" ));
        //addAttributeDef( new AttributeDef( ATTR_VALIDATION, String.class, false, "Comma delimited list of validators" ));
        //addAttributeDef( new AttributeDef( ATTR_DEFAULT_VALUE, String.class, false, "Default value for the MetaField" ));
    }

    /**
     * Return the older MetaFieldTypes values
     * @deprecated Use getDataType() and the DataTypes Enum
     */
    public int getType() {
        return getDataType().getId();
    }

    /**
     * Gets the primary MetaData class
     */
    @Override
    public final Class<MetaField> getMetaDataClass() {
        return MetaField.class;
    }

    /**
     * Returns the specific MetaClass in which this class is declared.<br>
     * WARNING: This may not return the MetaClass from which this MetaField was retrieved.
     *
     * @return The declaring MetaClass
     */
    public MetaObject getDeclaringObject() {
        if ( getParent() instanceof MetaDataLoader) return null;
        if ( getParent() instanceof MetaObject ) return (MetaObject) getParent();
        throw new InvalidMetaDataException(this, "MetaFields can only be attached to MetaObjects " +
                "or MetaDataLoaders as abstracts");
    }

    /**
     * Sets the Super Field
     */
    public void setSuperField(MetaField superField) {
        setSuperData(superField);
    }

    /**
     * Gets the Super Field
     */
    public MetaField getSuperField() {
        return (MetaField) getSuperData();
    }

    /**
     * Sets an attribute of the MetaClass
     */
    //public MetaField addMetaAttr(MetaAttribute attr) {
    //    return addChild(attr);
    //}

    /**
     * Get an ObjectReference for the MetaField
     */
    //public ObjectReference getFirstObjectReference() {
    //    return (ObjectReference) getFirstChildOfType(ObjectReference.TYPE_OBJECTREF);
    //}

    /**
     * Sets the default field value
     * @deprecated Add a child MetaAttribute with DEFAULT_VALUE
     */
    public void setDefaultValue(T defVal) {

        defaultValue = defVal;

        if (!getValueClass().isInstance(defVal)) {
            // Convert as needed
            defVal = (T) DataConverter.toType(getDataType(), defVal);
            String def = defVal.toString();
        }

        defaultValue = (T) defVal;
    }

    /**
     * Gets the default field value
     */
    public T getDefaultValue() {

        if ( defaultValue == null && !lookedForDefault ) {

            if (hasMetaAttr(MetaField.ATTR_DEFAULT_VALUE)) {
                Object o = getMetaAttr(MetaField.ATTR_DEFAULT_VALUE).getValue();
                defaultValue = convertDefaultValue(o);
            }

            lookedForDefault = true;
        }

        return defaultValue;
    }

    protected T convertDefaultValue(Object o) {
        if (!getValueClass().isInstance(o)) {
            // Convert as needed
            return (T) DataConverter.toType(getDataType(), o);
        } else {
            return (T) o;
        }
    }

    /** Flush the caches and set local flags to false */
    @Override
    protected void flushCaches() {
        lookedForDefault = false;
        super.flushCaches();
    }

    /**
     * Returns the type of value
     */
    @Override
    public DataTypes getDataType() {
        return dataType;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return getDataType().getValueClass();
    }
    
    // ========== ENHANCED FIELD-SPECIFIC METHODS ==========
    
    /**
     * Get field-specific metrics
     */
    public MetaDataMetrics getFieldMetrics() {
        return fieldMetrics;
    }
    
    /**
     * Get field metrics snapshot
     */
    public MetaDataMetrics.MetricsSnapshot getFieldMetricsSnapshot() {
        return fieldMetrics.getSnapshot();
    }
    
    /**
     * Validate this MetaField using enhanced validation
     */
    public ValidationResult validateField() {
        Instant start = Instant.now();
        
        try {
            ValidationResult result = getFieldValidationChain().validate(this);
            
            // Record metrics
            Duration duration = Duration.between(start, Instant.now());
            fieldMetrics.recordValidation(duration, result.isValid());
            
            return result;
        } catch (Exception e) {
            // Record error metrics
            Duration duration = Duration.between(start, Instant.now());
            fieldMetrics.recordValidation(duration, false);
            fieldMetrics.recordError();
            
            log.error("Field validation failed for {}: {}", getName(), e.getMessage(), e);
            
            return ValidationResult.builder()
                .addError("Field validation failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Get the field validation chain (lazy initialization)
     */
    private ValidationChain<MetaField<T>> getFieldValidationChain() {
        if (fieldValidationChain == null) {
            synchronized (this) {
                if (fieldValidationChain == null) {
                    fieldValidationChain = ValidationChain.<MetaField<T>>builder()
                        .addValidator(createDataTypeFieldValidator())
                        .addValidator(createDefaultValueValidator())
                        .addValidator(createDeclaringObjectValidator())
                        .addValidator(createLegacyFieldValidator())
                        .build();
                }
            }
        }
        return fieldValidationChain;
    }
    
    /**
     * Create a data type validator for this field
     */
    private Validator<MetaField<T>> createDataTypeFieldValidator() {
        return new Validator<MetaField<T>>() {
            @Override
            public ValidationResult validate(MetaField<T> field) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                if (field.getDataType() == null) {
                    builder.addError("MetaField must have a data type");
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Create a default value validator for this field
     */
    private Validator<MetaField<T>> createDefaultValueValidator() {
        return new Validator<MetaField<T>>() {
            @Override
            public ValidationResult validate(MetaField<T> field) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                // Validate default value against data type if present
                T defaultVal = field.getDefaultValue();
                if (defaultVal != null && field.getDataType() != null) {
                    try {
                        // Attempt to convert default value to validate compatibility
                        DataConverter.toType(field.getDataType(), defaultVal);
                    } catch (Exception e) {
                        builder.addError("Default value '" + defaultVal + 
                                       "' is not compatible with data type " + field.getDataType());
                    }
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Create a declaring object validator for this field
     */
    private Validator<MetaField<T>> createDeclaringObjectValidator() {
        return new Validator<MetaField<T>>() {
            @Override
            public ValidationResult validate(MetaField<T> field) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                // Validate that field has proper parent relationship
                if (field.getParent() != null && 
                    !(field.getParent() instanceof MetaDataLoader) &&
                    !(field.getParent() instanceof MetaObject)) {
                    builder.addError("MetaField must be attached to MetaObject or MetaDataLoader");
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Safe default value getter with Optional wrapper
     */
    public Optional<T> getDefaultValueSafe() {
        return Optional.ofNullable(getDefaultValue());
    }
    
    /**
     * Check if this field has a default value
     */
    public boolean hasDefaultValue() {
        return getDefaultValue() != null;
    }
    
    /**
     * Enhanced setDefaultValue with validation and tracking
     */
    public void setDefaultValueEnhanced(T defVal) {
        Instant start = Instant.now();
        T oldValue = this.defaultValue;
        
        try {
            setDefaultValue(defVal); // Call existing method
            
            // Record metrics
            fieldMetrics.recordPropertyChange();
            
            log.debug("MetaField {} default value changed from {} to {}", getName(), oldValue, defVal);
            
        } catch (Exception e) {
            // Record error metrics
            fieldMetrics.recordError();
            
            log.error("Failed to set default value for MetaField {}: {}", getName(), e.getMessage(), e);
            throw e; // Re-throw to maintain existing behavior
        }
    }
    
    /**
     * Create a legacy validator wrapper for MetaField
     */
    private Validator<MetaField<T>> createLegacyFieldValidator() {
        return new Validator<MetaField<T>>() {
            @Override
            public ValidationResult validate(MetaField<T> field) {
                // Just use the basic validation from the parent
                try {
                    field.validate(); // Call existing validate method
                    return ValidationResult.builder().build(); // Success
                } catch (Exception e) {
                    return ValidationResult.builder()
                        .addError("Legacy validation failed: " + e.getMessage())
                        .build();
                }
            }
        };
    }

    /** Add Child to the Field */
    //@Override
    //public MetaField addChild(MetaData data) throws InvalidMetaDataException {
    //    return super.addChild( data );
    //}

    /** Wrap the MetaField */
    //@Override
    //public MetaField overload() {
    //    return super.overload();
    //}

    /**
     * Sets the object attribute represented by this MetaField
     */
    protected void setObjectAttribute(Object obj, Object val) {

        // Ensure the data types are accurate
        if (val != null && !getValueClass().isInstance(val))
            throw new InvalidValueException("Invalid value [" + val + "], expected class [" + getValueClass().getName() + "]");

        // Perform validation -- Disabled for performance reasons
        //performValidation( obj, val );

        // Set the value on the object
        getDeclaringObject().setValue(this, obj, val);
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    protected Object getObjectAttribute(Object obj) {
        return getObjectValue(obj);
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    private Object getObjectValue(Object obj) {
        Object val = getDeclaringObject().getValue(this, obj);
        if (!getValueClass().isInstance(val)) {
            val = DataConverter.toType(dataType, val);
        }
        return val;
    }

    ////////////////////////////////////////////////////
    // VIEW METHODS

    /**
     * Whether the named MetaView exists
     */
    public boolean hasView(String name) {
        try {
            getView(name);
            return true;
        } catch (MetaViewNotFoundException e) {
            return false;
        }
    }

    /**
     * Adds a MetaView to this MetaField
     *
     * @param view MetaView to add
     */
    public <T extends MetaField> T addMetaView(MetaView view) {
        addChild(view);
        return (T) this;
    }

    /**
     * Adds a MetaView to this MetaField
     *
     * @param view MetaView to add
     */
    public void addView(MetaView view) {
        addChild(view);
    }

    public Collection<MetaView> getViews() {
        return getChildren(MetaView.class, true);
    }

    public MetaView getDefaultView() {
        if (hasMetaAttr(ATTR_DEFAULT_VIEW))
            return getView(getMetaAttr(ATTR_DEFAULT_VIEW).getValueAsString());
        else
            return getFirstChild(MetaView.class);
    }

    public MetaView getView(String name) {
        try {
            return (MetaView) getChild(name, MetaView.class);
        } catch (MetaDataNotFoundException e) {
            throw new MetaViewNotFoundException("MetaView with name [" + name + "] not found in MetaField [" + toString() + "]", name);
        }
    }

    ////////////////////////////////////////////////////
    // VALIDATOR METHODS

    public void performValidation(Object obj) {
        if ( obj != null ) {
            performValidation(obj, getObjectAttribute(obj));
        } else {
            throw new InvalidValueException("Cannot perform validation on a null object: "+toString());
        }
    }

    protected void performValidation(Object obj, Object val)  {
        // Run the default
        getDefaultValidatorList().forEach(v -> v.validate(obj, val));
    }

    /**
     * Returns validators specified in a 'validation' attribute stringArray
     *  or returns all the validators attached to this MetaField
     * @return List of validators to use for default validation checks
     */
    public List<MetaValidator> getDefaultValidatorList() {

        return useCache( "getDefaultValidatorList()", () -> {

                List<MetaValidator> validators = new ArrayList<MetaValidator>();

                // See if there is a specified list of validators
                if (hasMetaAttr(ATTR_VALIDATION)) {
                    validators = getValidatorList(getMetaAttr(ATTR_VALIDATION).getValueAsString());
                }
                // Otherwise grab all the validators
                else {
                    validators = getValidators();
                }

                return validators;
            });
    }

    /**
     * Whether the named MetaValidator exists
     */
    public boolean hasValidator(String name) {
        try {
            getValidator(name);
            return true;
        } catch (MetaValidatorNotFoundException e) {
            return false;
        }
    }

    public void addMetaValidator(MetaValidator validator) {
        flushCaches();
        addChild(validator);
    }

    public List<MetaValidator> getValidators() {
        return getChildren(MetaValidator.class, true);
    }

    /**
     * This method returns the list of validators based on the
     * comma delimited string name provided
     */
    public List<MetaValidator> getValidatorList(String listAttr)
    {
        return useCache( "getValidatorList()", listAttr, list -> {

            List<MetaValidator> validators = new ArrayList<MetaValidator>();
            while (list != null) {

                String validator = null;

                int i = list.indexOf(',');
                if (i >= 0) {
                    validator = list.substring(0, i).trim();
                    list = list.substring(i + 1);
                } else {
                    validator = list.trim();
                    list = null;
                }

                if (validator.length() > 0)
                    validators.add(getValidator(validator));
            }
            return validators;
        });
    }


    public MetaValidator getValidator(String validatorName) {
        return useCache( "getValidator()", validatorName, name -> {
            return (MetaValidator) getChild(name, MetaValidator.class);
        });
    }

    public void validate() {
        super.validate();
        if ( getName() == null ) throw new MetaDataException( "Name of MetaField was null :" + toString() );
    }

    ////////////////////////////////////////////////////
    // OBJECT SETTER METHODS

    public void setBoolean(Object obj, Boolean value){
        setObject(obj, value );
    }

    public void setByte(Object obj, Byte value){
        setObject(obj, value );
    }

    public void setShort(Object obj, Short value){
        setObject(obj, value );
    }

    public void setInt(Object obj, Integer value){
        setObject(obj, value );
    }

    public void setLong(Object obj, Long value){
        setObject(obj, value );
    }

    public void setFloat(Object obj, Float value ){
        setObject(obj, value );
    }

    public void setDouble(Object obj, Double value){
        setObject(obj, value );
    }

    public void setString(Object obj, String value) {
        setObject(obj, value );
    }

    public void setStringArray(Object obj, List<String> value) {
        setObject(obj, value );
    }

    public void setDate(Object obj, Date value) {
        setObject(obj, value );
    }

    public void setObject(Object obj, Object value) {
        setObjectAttribute(obj, DataConverter.toType(getDataType(), value ));
    }

    public void setObjectArray(Object obj, List<?> value) {
        if ( getDataType() != DataTypes.OBJECT_ARRAY ) throw new InvalidValueException(
                "Cannot set List to non ObjectArray type ["+getDataType()+"]" );
        setObjectAttribute(obj, value);
    }

    public void addToObjectArray(Object o, Object value) {
        if ( value == null ) return;
        List<Object> values = getObjectArray(o);
        if ( values == null ) {
            values = new ArrayList<>();
            setObjectArray(o,values);
        }
        values.add( value );
    }


    ////////////////////////////////////////////////////
    // OBJECT GETTER METHODS

    public Boolean getBoolean(Object obj) {
        return DataConverter.toBoolean(getObjectAttribute(obj));
    }

    public Byte getByte(Object obj) {
        return DataConverter.toByte(getObjectAttribute(obj));
    }

    public Short getShort(Object obj) {
        return DataConverter.toShort(getObjectAttribute(obj));
    }

    public Integer getInt(Object obj) {
        return DataConverter.toInt(getObjectAttribute(obj));
    }

    public Long getLong(Object obj) {
        return DataConverter.toLong(getObjectAttribute(obj));
    }

    public Float getFloat(Object obj) {
        return DataConverter.toFloat(getObjectAttribute(obj));
    }

    public Double getDouble(Object obj) {
        return DataConverter.toDouble(getObjectAttribute(obj));
    }

    public String getString(Object obj) {
        return DataConverter.toString(getObjectAttribute(obj));
    }

    public List<String> getStringArray(Object obj) {
        return DataConverter.toStringArray(getObjectAttribute(obj));
    }

    public Date getDate(Object obj) {
        return DataConverter.toDate(getObjectAttribute(obj));
    }

    public Object getObject(Object obj) {
        return getObjectAttribute(obj);
    }

    public List<Object> getObjectArray(Object obj) {
        return DataConverter.toObjectArray(getObjectAttribute(obj));
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    /** Clone the MetaField */
    @Override
    public Object clone() {
        MetaField mf = (MetaField) super.clone();
        mf.defaultValue = defaultValue;
        mf.lookedForDefault = lookedForDefault;
        mf.length = length;
        return mf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MetaField<?> metaField = (MetaField<?>) o;
        return length == metaField.length &&
                Objects.equals(defaultValue, metaField.defaultValue) &&
                dataType == metaField.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defaultValue, length, dataType);
    }

    /** Get the toString Prefix */
    @Override
    protected String getToStringPrefix() {
        return  super.getToStringPrefix() + "{dataType=" + dataType + ", defaultValue=" + defaultValue + "}";
    }
}
