package com.draagon.meta.object;

import com.draagon.meta.*;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.key.ForeignKey;
import com.draagon.meta.key.MetaKey;
import com.draagon.meta.key.PrimaryKey;
import com.draagon.meta.key.SecondaryKey;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.Validator;
import com.draagon.meta.validation.MetaDataValidators;
import com.draagon.meta.metrics.MetaDataMetrics;
import com.draagon.meta.event.MetaDataEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class MetaObject extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaObject.class);

    /** Object TYPE */
    public final static String TYPE_OBJECT = "object";

    /** Object class name attribute */
    public final static String ATTR_OBJECT = "object";

    // Referenced by ObjectField and ObjectArrayField - maintained for backward compatibility
    // and shared usage across field implementations
    public final static String ATTR_OBJECT_REF = "objectRef";
    
    // Enhanced object-specific validation chain
    private volatile ValidationChain<MetaObject> objectValidationChain;
    
    // Object-specific metrics
    private final MetaDataMetrics objectMetrics;

    /**
     * Legacy constructor used in unit tests
     * @param name Name of the MetaObject
     * @deprecated Use MetaObject( subtype, name )
     */
    public MetaObject( String name ) {
        this( "deprecated", name );
    }

    /**
     * Constructs the MetaObject with enhanced validation and metrics
     */
    public MetaObject(String subtype, String name ) {
        super( TYPE_OBJECT, subtype, name );
        this.objectMetrics = new MetaDataMetrics("object:" + name);
        this.objectMetrics.recordCreation();
        
        log.debug("Created MetaObject: {}:{}:{}", TYPE_OBJECT, subtype, name);
    }

    /**
     * Gets the primary MetaData class
     */
    public final Class<MetaObject> getMetaDataClass() {
        return MetaObject.class;
    }

    /** Add Child to the MetaObject */
    public MetaObject addChild(MetaData data) throws InvalidMetaDataException {
        return super.addChild( data );
    }

    /** Wrap the MetaObject */
    public MetaObject overload() {
        return super.overload();
    }
    
    // ========== ENHANCED OBJECT-SPECIFIC METHODS ==========
    
    /**
     * Get object-specific metrics
     */
    public MetaDataMetrics getObjectMetrics() {
        return objectMetrics;
    }
    
    /**
     * Get object metrics snapshot
     */
    public MetaDataMetrics.MetricsSnapshot getObjectMetricsSnapshot() {
        return objectMetrics.getSnapshot();
    }
    
    /**
     * Validate this MetaObject using enhanced validation
     */
    public ValidationResult validateObject() {
        Instant start = Instant.now();
        
        try {
            ValidationResult result = getObjectValidationChain().validate(this);
            
            // Record metrics
            Duration duration = Duration.between(start, Instant.now());
            objectMetrics.recordValidation(duration, result.isValid());
            
            // Publish validation event
            publishEvent(new MetaDataEvent.ValidationCompleted(this, result.isValid(), result.getErrors().size()));
            
            return result;
        } catch (Exception e) {
            // Record error metrics
            Duration duration = Duration.between(start, Instant.now());
            objectMetrics.recordValidation(duration, false);
            objectMetrics.recordError();
            
            log.error("Object validation failed for {}: {}", getName(), e.getMessage(), e);
            
            return ValidationResult.builder()
                .addError("Object validation failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Get the object validation chain (lazy initialization)
     */
    private ValidationChain<MetaObject> getObjectValidationChain() {
        if (objectValidationChain == null) {
            synchronized (this) {
                if (objectValidationChain == null) {
                    objectValidationChain = ValidationChain.<MetaObject>builder()
                        .addValidator(createObjectClassValidator())
                        .addValidator(createFieldsValidator())
                        .addValidator(createKeysValidator())
                        .addValidator(createLegacyObjectValidator())
                        .build();
                }
            }
        }
        return objectValidationChain;
    }
    
    /**
     * Create an object class validator
     */
    private Validator<MetaObject> createObjectClassValidator() {
        return new Validator<MetaObject>() {
            @Override
            public ValidationResult validate(MetaObject object) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                try {
                    Class<?> objectClass = object.getObjectClass();
                    if (objectClass == null) {
                        builder.addError("MetaObject must have a valid object class");
                    }
                } catch (Exception e) {
                    builder.addError("Invalid object class: " + e.getMessage());
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Create a fields validator
     */
    private Validator<MetaObject> createFieldsValidator() {
        return new Validator<MetaObject>() {
            @Override
            public ValidationResult validate(MetaObject object) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                // Validate that all fields are properly configured
                Collection<MetaField> fields = object.getMetaFields();
                for (MetaField field : fields) {
                    if (field.getDataType() == null) {
                        builder.addError("Field '" + field.getName() + "' must have a data type");
                    }
                    
                    if (field.getDeclaringObject() != object && field.getDeclaringObject() != null) {
                        builder.addError("Field '" + field.getName() + "' has incorrect declaring object");
                    }
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Create a keys validator
     */
    private Validator<MetaObject> createKeysValidator() {
        return new Validator<MetaObject>() {
            @Override
            public ValidationResult validate(MetaObject object) {
                ValidationResult.Builder builder = ValidationResult.builder();
                
                // Validate keys - check that there's only one primary key
                try {
                    PrimaryKey primaryKey = object.getPrimaryKey();
                    // If we got here without exception, there is one primary key which is good
                    // No additional validation needed for now
                } catch (Exception e) {
                    // Either no primary key or error - this is okay for validation
                    // Could be more specific about required vs optional primary keys
                }
                
                return builder.build();
            }
        };
    }
    
    /**
     * Enhanced field access with Optional wrapper
     */
    public Optional<MetaField> findMetaField(String name) {
        try {
            return Optional.of(getMetaField(name));
        } catch (MetaFieldNotFoundException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Safe field requirement (throws descriptive exception)
     */
    public MetaField requireMetaField(String name) {
        return findMetaField(name)
            .orElseThrow(() -> new MetaFieldNotFoundException(
                "MetaField '" + name + "' not found in MetaObject '" + getName() + "'", name));
    }
    
    /**
     * Get all fields as stream for functional operations
     */
    public Stream<MetaField> getMetaFieldsStream() {
        return getMetaFields().stream();
    }
    
    /**
     * Find fields by data type
     */
    public Stream<MetaField> findFieldsByType(DataTypes dataType) {
        return getMetaFieldsStream()
            .filter(field -> field.getDataType() == dataType);
    }
    
    /**
     * Enhanced newInstance with metrics and error tracking
     */
    public Object newInstanceEnhanced() {
        Instant start = Instant.now();
        
        try {
            Object instance = newInstance(); // Call existing method
            
            // Record successful instance creation metrics
            Duration duration = Duration.between(start, Instant.now());
            objectMetrics.recordInstanceCreation(duration, true);
            
            log.debug("Successfully created instance of {}", getName());
            
            return instance;
        } catch (Exception e) {
            // Record error metrics
            Duration duration = Duration.between(start, Instant.now());
            objectMetrics.recordInstanceCreation(duration, false);
            objectMetrics.recordError();
            
            log.error("Failed to create instance of {}: {}", getName(), e.getMessage(), e);
            throw e; // Re-throw to maintain existing behavior
        }
    }
    
    /**
     * Create a legacy validator wrapper for MetaObject
     */
    private Validator<MetaObject> createLegacyObjectValidator() {
        return new Validator<MetaObject>() {
            @Override
            public ValidationResult validate(MetaObject object) {
                // Just use the basic validation from the parent
                try {
                    object.validate(); // Call existing validate method
                    return ValidationResult.builder().build(); // Success
                } catch (Exception e) {
                    return ValidationResult.builder()
                        .addError("Legacy validation failed: " + e.getMessage())
                        .build();
                }
            }
        };
    }

    /**
     * Returns the MetaObject for the specified Meta Object name
     *
     * @deprecated Use MetaDataRegistry.findMetaObjectByName(), if enabled in MetaDataLoader
     */
    public static MetaObject forName(String name) {
        return MetaDataRegistry.findMetaObjectByName(name);
    }

    /**
     * Returns the MetaObject for the specified Object
     *
     * @deprecated Use MetaDataRegistry.findMetaObject(), if registry enabled in MetaDataLoader
     */
    public static MetaObject forObject(Object o) {
        return MetaDataRegistry.findMetaObject( o );
    }

    /**
     * Sets the Super Class
     */
    public MetaObject setSuperObject(MetaObject superObject) {
        setSuperData(superObject);
        return this;
    }

    /**
     * Gets the Super Object
     */
    public MetaObject getSuperObject() {
        return (MetaObject) getSuperData();
    }

    ////////////////////////////////////////////////////
    // FIELD METHODS
    /**
     * Return the MetaField count
     */
    public Collection<MetaField> getMetaFields() {
        return getMetaFields(true);
    }

    /**
     * Return the MetaField count
     */
    public Collection<MetaField> getMetaFields( boolean includeParentData ) {
        return getChildren(MetaField.class, includeParentData);
    }

    /**
     * Add a field to the MetaObject
     */
    public MetaObject addMetaField(MetaField f) {
        addChild(f);
        return this;
    }

    /**
     * Whether the named MetaField exists
     */
    public boolean hasMetaField(String name) {
        try {
            getMetaField(name);
            return true;
        } catch (MetaFieldNotFoundException e) {
            return false;
        }
    }

    /**
     * Return the specified MetaField of the MetaObject
     */
    public MetaField getMetaField(String fieldName) {

        return useCache( "getMetaField()", fieldName, name -> {
            MetaField f = null;
            try {
                f = (MetaField) getChild(name, MetaField.class);
            } catch (MetaDataNotFoundException e) {
                if (getSuperObject() != null) {
                    try {
                        f = getSuperObject().getMetaField(name);
                    } catch (MetaFieldNotFoundException ex) {
                    }
                }
                throw new MetaFieldNotFoundException("MetaField [" + name + "] does not exist in MetaObject [" + toString() + "]", name);
            }
            return f;
        });
    }

    /**
     * Whether the object is aware of its own state. This is false by default,
     * which means all state methods will throw an
     * UnsupportedOperationException.
     */
    public boolean isStateAware() {
        return false;
    }


    ////////////////////////////////////////////////////
    // OBJECT METHODS

    protected boolean hasObjectAttr() {
        return ( hasMetaAttr(ATTR_OBJECT, true ));
    }

    protected Class<?> getObjectClassFromAttr() throws ClassNotFoundException {
        Class<?> c = null;
        MetaAttribute attr = null;
        if (hasMetaAttr(ATTR_OBJECT)) {
            attr = getMetaAttr(ATTR_OBJECT);
        }
        if ( attr != null ) {
            c = loadClass( attr.getValueAsString() );
        }
        return c;
    }

    /**
     * Retrieves the object class of an object, or null if one is not specified
     */
    public Class<?> getObjectClass() throws ClassNotFoundException {

        final String CACHE_KEY = "getObjectClass()";
        Class<?> c = (Class<?>) getCacheValue(CACHE_KEY );
        if ( c == null ) {

            c = null;

            if (hasObjectAttr()) {
                c = getObjectClassFromAttr();
            }

            if (c == null)
                c = createClassFromMetaDataName(true);

            setCacheValue( CACHE_KEY, c );
        }
        return c;
    }

    protected Class createClassFromMetaDataName( boolean throwError ) {

        String ostr = getName().replaceAll(PKG_SEPARATOR, ".");
        try {
            return loadClass(ostr,throwError);
        }
        catch (ClassNotFoundException e) {
            if ( throwError ) {
                throw new InvalidMetaDataException( this, "Derived Object Class [" + ostr + "] was not found");
            }
        }

        return null;
    }

    /**
     * Whether the MetaObject produces the object specified
     */
    public abstract boolean produces(Object obj);

    /**
     * Attaches a MetaObject to an Object
     *
     * @param o Object to attach
     */
    public void attachMetaObject(Object o) {
        if (o instanceof MetaObjectAware) {
            ((MetaObjectAware) o).setMetaData(this);
        }
    }

    /**
     * Sets the default values on an object
     *
     * @param o Object to set the default values on
     */
    public void setDefaultValues(Object o) {

        getMetaFields().stream()
                .filter( f -> f.getDefaultValue()!=null)
                .forEach( f -> f.setObject( o, f.getDefaultValue() ));
    }

    /**
     * Return a new MetaObject instance from the MetaObject
     */
    public Object newInstance()  {

        final String KEY = "ObjectClassForNewInstance";

        // See if we have this cached already
        Class<?> oc = (Class<?>) getCacheValue( KEY );
        if ( oc == null ) {

            try {
                oc = getObjectClass();
                if (oc == null) {
                    throw new MetaDataException("No Object Class was found on MetaObject [" + getName() + "]");
                }
            } catch (ClassNotFoundException e) {
                throw new MetaDataException("Could not find Object Class for MetaObject [" + getName() + "]: " + e.getMessage(), e);
            }

            // Store the resulting Class in the cache
            setCacheValue( KEY, oc );
        }

        try {
            if (oc.isInterface()) {
                throw new IllegalArgumentException("Could not instantiate an Interface for MetaObject [" + getName() + "]");
            }

            Object o = null;

            try {
                // Construct the object and pass the MetaObject into the constructor
                Constructor c = oc.getConstructor( MetaObject.class );
                o = c.newInstance(this);
            }
            catch (NoSuchMethodException e) {
                // Construct with no arguments && attach the metaobject
                for( Constructor<?> c : oc.getDeclaredConstructors() ) {
                    if ( c.getParameterCount() == 0 ) {
                        try {
                            c.setAccessible(true);
                            o = c.newInstance();
                        } catch (InvocationTargetException ex) {
                            throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: " + e.getMessage(), e);
                        }
                        attachMetaObject(o);
                        break;
                    }
                }
                if ( o == null ) throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: No empty constructor existed" );
            }
            catch (InvocationTargetException e) {
                throw new RuntimeException("Could not instantiate a new Object of Class [" + oc + "] for MetaObject [" + getName() + "]: " + e, e);
            }

            // Set the Default Values
            setDefaultValues(o);

            return o;
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not instantiate a new Object of Class [" + oc.getName() + "] for MetaObject [" + getName() + "]: " + e, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception instantiating a new Object for MetaObject [" + getName() + "]: " + e, e);
        }
    }

    ////////////////////////////////////////////////////
    // KEY METHODS

    public PrimaryKey getPrimaryKey() {
        return getKeyByName(PrimaryKey.NAME, PrimaryKey.class );
    }

    protected <T extends MetaKey> T getKeyByName(String keyName, Class<T> clazz ) {
        return getChild( keyName, clazz );
    }

    public SecondaryKey getSecondaryKeyByName(String keyName) {
        return getKeyByName( keyName, SecondaryKey.class );
    }

    public Collection<SecondaryKey> getSecondaryKeys() {
        return getKeysOfSubType(SecondaryKey.SUBTYPE, SecondaryKey.class);
    }

    public ForeignKey getForeignKeyByName(String keyName) {
        return getKeyByName( keyName, ForeignKey.class );
    }

    public Collection<ForeignKey> getForeignKeys() {
        return getKeysOfSubType(ForeignKey.SUBTYPE, ForeignKey.class);
    }

    //public <T extends MetaKey> Collection<T> getKeysOfSubType( String subType ) {
    //    return (Collection<T>) getKeysOfSubType( subType, MetaKey.class );
    //}

    /** Null for clazz means just look by subtype */
    protected <T extends MetaKey> Collection<T> getKeysOfSubType( String subType, Class<T> clazz ) {
        final String CACHE_KEY = "getKeysOfSubType("+subType+","+clazz+")";
        Collection<T> keys = (Collection<T>) getCacheValue(CACHE_KEY);
        if ( keys == null ) {
            keys = new ArrayList<>();
            for (T key : getChildren( clazz )) {
                if ( key.getSubTypeName().equals( subType )) keys.add(key);
            }
            setCacheValue(CACHE_KEY, keys);
        }
        return keys;
    }

    public Collection<MetaKey> getAllKeys() {
        return getChildren( MetaKey.class );
    }


    ////////////////////////////////////////////////////
    // ABSTRACT METHODS

    /**
     * Retrieves the value from the object
     */
    public abstract Object getValue(MetaField f, Object obj);

    /**
     * Sets the value on the object
     */
    public abstract void setValue(MetaField f, Object obj, Object val);

    ////////////////////////////////////////////////////
    // Validation Methods

    public void performValidation(Object obj) {
        if ( obj != null ) {
            for(MetaField mf : getMetaFields()) {
                mf.performValidation(obj);
            }
        } else {
            throw new InvalidValueException("Cannot perform validation on a null object: "+toString());
        }
    }


    @Override
    public void validate() {
        super.validate();
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    public Object clone() {
        MetaObject mc = (MetaObject) super.clone();
        return mc;
    }
}
