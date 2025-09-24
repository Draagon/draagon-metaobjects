/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.validator.MetaValidatorNotFoundException;
import com.draagon.meta.view.MetaView;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.TypeDefinition;
import com.draagon.meta.registry.ChildRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * MetaField represents a field definition within a MetaObject, functioning as both 
 * a metadata descriptor and a type-safe accessor for object properties.
 * 
 * <p>MetaField follows the same <strong>READ-OPTIMIZED WITH CONTROLLED MUTABILITY</strong> 
 * pattern as other MetaData objects. Field definitions are loaded once during application 
 * startup and then provide ultra-fast, thread-safe access to object properties throughout 
 * the application lifetime.</p>
 * 
 * <h3>Field as Metadata Pattern</h3>
 * <p>Similar to {@code java.lang.reflect.Field}, MetaField serves dual purposes:</p>
 * <ul>
 * <li><strong>Metadata Descriptor</strong>: Defines field name, type, validation rules, display preferences</li>
 * <li><strong>Value Accessor</strong>: Type-safe getter/setter operations on object instances</li>
 * <li><strong>Validation Engine</strong>: Enforces data integrity through constraint system</li>
 * <li><strong>Serialization Guide</strong>: Controls JSON/XML serialization behavior</li>
 * </ul>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Loading Phase - Field definition
 * MetaObject userMeta = loader.getMetaObjectByName("User");
 * MetaField<String> emailField = userMeta.getMetaField("email");
 * 
 * // Runtime Phase - Value operations (thread-safe, high-performance)
 * String email = emailField.getValue(userObject);           // Type-safe get
 * emailField.setValue(userObject, "user@example.com");      // Type-safe set
 * boolean isValid = emailField.validate(userObject);        // Constraint validation
 * }</pre>
 * 
 * <h3>Type Safety</h3>
 * <p>MetaField is parameterized with the expected Java type {@code <T>} for compile-time 
 * type safety. This prevents ClassCastException and provides IDE support for auto-completion.</p>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>Field Lookup</strong>: O(1) cached access from parent MetaObject</li>
 * <li><strong>Value Access</strong>: Direct reflection or optimized accessors</li>
 * <li><strong>Validation</strong>: Cached constraint evaluation</li>
 * <li><strong>Memory</strong>: Permanent field definitions, no per-instance overhead</li>
 * </ul>
 * 
 * @param <T> the Java type that this field represents (String, Integer, etc.)
 * @author Doug Mealing
 * @version 6.0.0
 * @since 1.0
 * @see MetaObject
 * @see DataTypes
 * @see com.draagon.meta.constraint.Constraint
 */
public abstract class MetaField<T> extends MetaData  implements DataTypeAware<T> {

    private static final Logger log = LoggerFactory.getLogger(MetaField.class);

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Field type constant - MetaField owns this concept */
    public static final String TYPE_FIELD = "field";

    /** Base field subtype for inheritance */
    public static final String SUBTYPE_BASE = "base";

    // === FIELD-LEVEL ATTRIBUTE NAME CONSTANTS ===
    // These apply to ALL field types and are inherited by concrete field implementations

    /** Required field marker attribute - MetaField owns this concept */
    public static final String ATTR_REQUIRED = "required";

    /** Default value specification attribute - MetaField owns this concept */
    public static final String ATTR_DEFAULT_VALUE = "defaultValue";

    /** Default view specification attribute - MetaField owns this concept */
    public static final String ATTR_DEFAULT_VIEW = "defaultView";

    // Unified registry self-registration
    /**
     * Register MetaField types using the standardized registerTypes() pattern.
     * This method registers the base field type that other field types inherit from.
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            MetaDataRegistry.registerType(MetaField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_BASE)
                .description("Base field metadata with common field attributes")

                // UNIVERSAL ATTRIBUTES (all MetaData inherit these)
                .optionalAttribute(ATTR_IS_ABSTRACT, "boolean")

                // FIELD-LEVEL ATTRIBUTES (all field types inherit these)
                .optionalAttribute(ATTR_REQUIRED, BooleanAttribute.SUBTYPE_BOOLEAN)
                .optionalAttribute(ATTR_DEFAULT_VALUE, "string")
                .optionalAttribute(ATTR_DEFAULT_VIEW, StringAttribute.SUBTYPE_STRING)

                // ACCEPTS ANY ATTRIBUTES, VALIDATORS AND VIEWS (all field types inherit these)
                .optionalChild(MetaAttribute.TYPE_ATTR, "*")
                .optionalChild(MetaValidator.TYPE_VALIDATOR, "*")
                .optionalChild(MetaView.TYPE_VIEW, "*")
            );

            log.debug("Registered base MetaField type with unified registry");

            // Register cross-cutting field constraints using consolidated registry
            registerCrossCuttingFieldConstraints(registry);

        } catch (Exception e) {
            log.error("Failed to register MetaField type with unified registry", e);
        }
    }

    /**
     * Alternative registerTypes() method with no parameters for backward compatibility.
     */
    public static void registerTypes() {
        registerTypes(MetaDataRegistry.getInstance());
    }

    // Static registration block - automatically registers the base field type when class is loaded
    static {
        try {
            registerTypes(MetaDataRegistry.getInstance());
        } catch (Exception e) {
            log.error("Failed to register base MetaField type during class loading", e);
        }
    }

    private T defaultValue = null;
    private boolean lookedForDefault = false;

    private int length = -1;

    private DataTypes dataType;
    
    


    /**
     * Construct a MetaField with enhanced validation and metrics
     * @param subtype SubType name for the MetaField
     * @param name Name of the MetaField
     * @param dataType The DataTypes enum used for values
     */
    public MetaField(String subtype, String name, DataTypes dataType) {
        super(TYPE_FIELD, subtype, name);
        this.dataType = dataType;
        
        log.debug("Created MetaField: {}:{}:{} with dataType: {}", TYPE_FIELD, subtype, name, dataType);
    }


    // Note: getMetaDataClass() is now inherited from MetaData base class

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
            return DataConverter.toTypeSafe(getDataType(), o, (Class<T>) getValueClass());
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
     * Get the expected Java class type for a given attribute on this field type.
     * This method consults the MetaDataRegistry to determine what Java type an
     * attribute should be converted to during parsing.
     *
     * @param attributeName the name of the attribute (e.g., "required", "maxLength", "dbColumn")
     * @return the expected Java class for the attribute, or String.class if not found
     */
    public Class<?> getExpectedAttributeType(String attributeName) {
        try {
            MetaDataRegistry registry = getLoader().getTypeRegistry();

            // Get the type definition for this specific field type
            TypeDefinition typeDef = registry.getTypeDefinition(this.getType(), this.getSubType());
            if (typeDef != null) {
                // Look up the child requirement for this attribute
                ChildRequirement attrReq = typeDef.getChildRequirement(attributeName);
                if (attrReq != null && "attr".equals(attrReq.getExpectedType())) {
                    // Map the attribute subType to Java class
                    return mapAttributeSubTypeToJavaClass(attrReq.getExpectedSubType());
                }
            }

            // Fallback to String for unknown attributes
            return String.class;

        } catch (Exception e) {
            log.debug("Registry lookup failed for attribute [{}] on [{}], defaulting to String: {}",
                attributeName, this.getClass().getSimpleName(), e.getMessage());
            return String.class;
        }
    }
    
    /**
     * Map attribute subType to Java class for type-safe parsing.
     * This method maps the registry's attribute subType definitions to actual Java classes.
     *
     * @param subType The subType from the registry (e.g., "boolean", "int", "string")
     * @return Java class for the subType, defaults to String.class
     */
    private Class<?> mapAttributeSubTypeToJavaClass(String subType) {
        if (subType == null) {
            return String.class;
        }

        switch (subType.toLowerCase()) {
            case "boolean":
                return Boolean.class;
            case "int":
            case "integer":
                return Integer.class;
            case "long":
                return Long.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "string":
            default:
                return String.class;
        }
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
            // Set the default value directly (replaces the removed deprecated method)
            this.defaultValue = defVal;
            
            if (defVal != null && !getValueClass().isInstance(defVal)) {
                // Convert as needed
                this.defaultValue = DataConverter.toTypeSafe(getDataType(), defVal, (Class<T>) getValueClass());
            }
            
            
            log.debug("MetaField {} default value changed from {} to {}", getName(), oldValue, defVal);
            
        } catch (Exception e) {
            
            log.error("Failed to set default value for MetaField {}: {}", getName(), e.getMessage(), e);
            throw e; // Re-throw to maintain existing behavior
        }
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
        return findView(name).isPresent();
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
     * Adds a MetaView to this MetaField (type-safe version)
     * @param view MetaView to add
     * @return This MetaField instance for method chaining
     */
    public MetaField<T> addMetaViewSafe(MetaView view) {
        addChild(view);
        return this;
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
            throw MetaDataNotFoundException.forView(name, this);
        }
    }

    /**
     * Find a MetaView by name using modern Optional-based API.
     * 
     * <p>This method provides safe, null-free access to views associated with this field.
     * Views control how field values are displayed, formatted, or rendered in different contexts.</p>
     * 
     * @param name the name of the view to find
     * @return Optional containing the MetaView if found, empty Optional otherwise
     * @since 5.1.0
     * @see #requireView(String)
     * @see #hasView(String)
     */
    public Optional<MetaView> findView(String name) {
        return findChild(name, MetaView.class);
    }

    /**
     * Require a MetaView by name, throwing an exception if not found.
     * 
     * <p>This method is useful when you know a view must exist and want to fail fast
     * if it's missing. Use {@link #findView(String)} for safer optional access.</p>
     * 
     * @param name the name of the view to retrieve
     * @return the MetaView with the specified name
     * @throws MetaDataNotFoundException if no view with the given name exists
     * @since 5.1.0
     * @see #findView(String)
     */
    public MetaView requireView(String name) {
        return findView(name)
            .orElseThrow(() -> MetaDataNotFoundException.forView(name, this));
    }

    /**
     * Get all views associated with this field as a Stream for functional operations.
     * 
     * <p>This method enables functional programming patterns like filtering, mapping,
     * and collecting views based on various criteria.</p>
     * 
     * <p><b>Example usage:</b><br>
     * {@code field.getViewsStream().filter(v -> v.isType("html")).collect(toList())}</p>
     * 
     * @return Stream of all MetaView objects associated with this field
     * @since 5.1.0
     * @see #getViews()
     */
    public Stream<MetaView> getViewsStream() {
        return findChildren(MetaView.class);
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
     * Returns all validators attached to this MetaField.
     * Validation is now calculated based on actual MetaValidator children,
     * eliminating the need for explicit validation attribute configuration.
     *
     * @return List of validators to use for default validation checks
     */
    public List<MetaValidator> getDefaultValidatorList() {

        return useCache( "getDefaultValidatorList()", () -> {
                // Always use all MetaValidator children - no more attribute-based validation
                return getValidators();
            });
    }

    /**
     * Whether the named MetaValidator exists
     */
    public boolean hasValidator(String name) {
        return findValidator(name).isPresent();
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

    /**
     * Find a MetaValidator by name using modern Optional-based API.
     * 
     * <p>This method provides safe, null-free access to validators associated with this field.
     * Validators are used to enforce business rules and data integrity constraints on field values.</p>
     * 
     * @param name the name of the validator to find
     * @return Optional containing the MetaValidator if found, empty Optional otherwise
     * @since 5.1.0
     * @see #requireValidator(String)
     * @see #hasValidator(String)
     */
    public Optional<MetaValidator> findValidator(String name) {
        return findChild(name, MetaValidator.class);
    }

    /**
     * Require a MetaValidator by name, throwing an exception if not found.
     * 
     * <p>This method is useful when you know a validator must exist and want to fail fast
     * if it's missing. Use {@link #findValidator(String)} for safer optional access.</p>
     * 
     * @param name the name of the validator to retrieve
     * @return the MetaValidator with the specified name
     * @throws MetaValidatorNotFoundException if no validator with the given name exists
     * @since 5.1.0
     * @see #findValidator(String)
     */
    public MetaValidator requireValidator(String name) {
        return findValidator(name)
            .orElseThrow(() -> new MetaValidatorNotFoundException(
                "MetaValidator '" + name + "' not found in MetaField '" + getName() + "'", name));
    }

    /**
     * Get all validators associated with this field as a Stream for functional operations.
     * 
     * <p>This method enables functional programming patterns for working with validators,
     * such as filtering by type, collecting specific validators, or applying transformations.</p>
     * 
     * <p><b>Example usage:</b><br>
     * {@code field.getValidatorsStream().filter(v -> v.isRequired()).count()}</p>
     * 
     * @return Stream of all MetaValidator objects associated with this field
     * @since 5.1.0
     * @see #getValidators()
     */
    public Stream<MetaValidator> getValidatorsStream() {
        return findChildren(MetaValidator.class);
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
    
    /**
     * Register cross-cutting field constraints that apply to all field types using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerCrossCuttingFieldConstraints(MetaDataRegistry registry) {
        try {
            // VALIDATION CONSTRAINT: Field naming patterns (allow package-qualified names)
            registry.registerValidationConstraint(
                "field.naming.pattern",
                "Field names must follow identifier pattern or be package-qualified",
                (metadata) -> metadata instanceof MetaField,
                (metadata, value) -> {
                    String name = metadata.getName();
                    if (name == null) return false;

                    // Allow package-qualified names (with ::)
                    if (name.contains("::")) {
                        String[] parts = name.split("::");
                        for (String part : parts) {
                            if (!part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        // Simple names must follow identifier pattern
                        return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                    }
                }
            );

            // PLACEMENT CONSTRAINT: All fields CAN have required attribute
            registry.registerPlacementConstraint(
                "field.required.placement",
                "Fields can optionally have required attribute",
                (metadata) -> metadata instanceof MetaField,
                (child) -> child instanceof com.draagon.meta.attr.BooleanAttribute &&
                          child.getName().equals(ATTR_REQUIRED)
            );

            log.debug("Registered cross-cutting field constraints using consolidated registry");

        } catch (Exception e) {
            log.error("Failed to register cross-cutting field constraints", e);
        }
    }
}
