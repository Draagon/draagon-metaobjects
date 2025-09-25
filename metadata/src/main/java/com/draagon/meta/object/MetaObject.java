package com.draagon.meta.object;

import com.draagon.meta.*;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.key.ForeignKey;
import com.draagon.meta.key.MetaKey;
import com.draagon.meta.key.PrimaryKey;
import com.draagon.meta.key.SecondaryKey;
import com.draagon.meta.registry.MetaDataRegistry;
import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
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

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Object type constant - MetaObject owns this concept */
    public static final String TYPE_OBJECT = "object";

    /** Base object subtype for inheritance */
    public static final String SUBTYPE_BASE = "base";

    // === OBJECT-LEVEL ATTRIBUTE NAME CONSTANTS ===
    // These apply to ALL object types and are inherited by concrete object implementations
    // ATTR_IS_ABSTRACT is imported from MetaData (universal attribute)

    /** Object inheritance specification attribute - MetaObject owns this concept */
    public static final String ATTR_EXTENDS = "extends";

    /** Interface implementation specification attribute - MetaObject owns this concept */
    public static final String ATTR_IMPLEMENTS = "implements";

    /** Interface marker attribute - MetaObject owns this concept */
    public static final String ATTR_IS_INTERFACE = "isInterface";

    // === OBJECT-SPECIFIC ATTRIBUTES ===
    /** Object reference attribute for composition */
    public static final String ATTR_OBJECT_REF = "objectRef";

    /** Object description attribute */
    public static final String ATTR_DESCRIPTION = "description";

    /** Object type attribute for composition */
    public static final String ATTR_OBJECT = "object";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(MetaObject.class, def -> def
                .type(TYPE_OBJECT).subType(SUBTYPE_BASE)
                .description("Base object metadata with common object attributes")
                .inheritsFrom("metadata", "base")

                // UNIVERSAL ATTRIBUTES (all MetaData inherit these)
                .optionalAttribute(ATTR_IS_ABSTRACT, "boolean")

                // OBJECT-LEVEL ATTRIBUTES (all object types inherit these)
                .optionalAttribute(ATTR_EXTENDS, "string")
                .optionalAttribute(ATTR_IMPLEMENTS, "string")
                .optionalAttribute(ATTR_IS_INTERFACE, "boolean")

                // OBJECT-SPECIFIC ATTRIBUTES
                .optionalAttribute(ATTR_DESCRIPTION, "string")
                .optionalAttribute(ATTR_OBJECT, "string")
                .optionalAttribute(ATTR_OBJECT_REF, "string")

                // OBJECTS CONTAIN FIELDS (any field type, any name)
                .optionalChild("field", "*", "*")

                // OBJECTS CAN CONTAIN OTHER OBJECTS (composition)
                .optionalChild("object", "*", "*")

                // OBJECTS CAN CONTAIN KEYS
                .optionalChild("key", "*", "*")

                // OBJECTS CAN CONTAIN ATTRIBUTES
                .optionalChild("attr", "*", "*")

                // OBJECTS CAN CONTAIN VALIDATORS
                .optionalChild("validator", "*", "*")

                // OBJECTS CAN CONTAIN VIEWS
                .optionalChild("view", "*", "*")
            );
            
            log.debug("Registered base MetaObject type with unified registry");
            
            // Register cross-cutting object constraints using consolidated registry
            registerCrossCuttingObjectConstraints(MetaDataRegistry.getInstance());
            
        } catch (Exception e) {
            log.error("Failed to register MetaObject type with unified registry", e);
        }
    }

    /**
     * Constructs the MetaObject with enhanced validation and metrics
     */
    public MetaObject(String subtype, String name ) {
        super( TYPE_OBJECT, subtype, name );
        
        log.debug("Created MetaObject: {}:{}:{}", TYPE_OBJECT, subtype, name);
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class


    /** Create an overloaded copy of the MetaObject */
    public MetaObject overload() {
        return (MetaObject) super.overload();
    }
    
    // ========== ENHANCED OBJECT-SPECIFIC METHODS ==========
    
    
    
    
    
    
    /**
     * Find a MetaField by name using modern Optional-based API.
     * 
     * <p>This method provides safe, null-free access to fields defined in this MetaObject.
     * Fields represent the structure and behavior of object properties, including data types,
     * validation rules, and display preferences.</p>
     * 
     * @param name the name of the field to find
     * @return Optional containing the MetaField if found, empty Optional otherwise
     * @since 5.1.0
     * @see #requireMetaField(String)
     * @see #hasMetaField(String)
     */
    public Optional<MetaField> findMetaField(String name) {
        return findChild(name, MetaField.class);
    }
    
    /**
     * Require a MetaField by name, throwing an exception if not found.
     * 
     * <p>This method is useful when you know a field must exist and want to fail fast
     * if it's missing. Use {@link #findMetaField(String)} for safer optional access.</p>
     * 
     * @param name the name of the field to retrieve
     * @return the MetaField with the specified name
     * @throws MetaDataNotFoundException if no field with the given name exists
     * @since 5.1.0
     * @see #findMetaField(String)
     */
    public MetaField requireMetaField(String name) {
        return findMetaField(name)
            .orElseThrow(() -> MetaDataNotFoundException.forField(name, this));
    }
    
    /**
     * Get all fields as a Stream for functional operations.
     * 
     * <p>This method enables functional programming patterns for working with fields,
     * such as filtering by type, collecting specific fields, or applying transformations.</p>
     * 
     * <p><b>Example usage:</b><br>
     * {@code object.getMetaFieldsStream().filter(f -> f.isRequired()).collect(toList())}</p>
     * 
     * @return Stream of all MetaField objects defined in this MetaObject
     * @since 5.1.0
     * @see #getMetaFields()
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
            
            
            log.debug("Successfully created instance of {}", getName());
            
            return instance;
        } catch (Exception e) {
            
            log.error("Failed to create instance of {}: {}", getName(), e.getMessage(), e);
            throw e; // Re-throw to maintain existing behavior
        }
    }
    



    /**
     * Sets the Super Class
     */
    public void setSuperObject(MetaObject superObject) {
        setSuperData(superObject);
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
        } catch (MetaDataNotFoundException e) {
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
                    } catch (MetaDataNotFoundException ex) {
                    }
                }
                throw MetaDataNotFoundException.forField(name, this);
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
                if ( key.getSubType().equals( subType )) keys.add(key);
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


    
    
    

    ////////////////////////////////////////////////////
    // MISC METHODS

    public Object clone() {
        MetaObject mc = (MetaObject) super.clone();
        return mc;
    }
    
    /**
     * Register cross-cutting object constraints that apply to all object types using consolidated registry
     *
     * @param registry The MetaDataRegistry to use for constraint registration
     */
    private static void registerCrossCuttingObjectConstraints(MetaDataRegistry registry) {
        try {

            // PLACEMENT CONSTRAINT: Objects CAN contain fields
            registry.registerPlacementConstraint(
                "object.fields.placement",
                "Objects can contain fields",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof MetaField
            );

            // PLACEMENT CONSTRAINT: Objects CAN contain attributes
            registry.registerPlacementConstraint(
                "object.attributes.placement",
                "Objects can contain attributes",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof MetaAttribute
            );

            // PLACEMENT CONSTRAINT: Objects CAN contain keys
            registry.registerPlacementConstraint(
                "object.keys.placement",
                "Objects can contain keys (primary, foreign, secondary)",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof MetaKey
            );

            // PLACEMENT CONSTRAINT: Objects CAN contain validators
            registry.registerPlacementConstraint(
                "object.validators.placement",
                "Objects can contain validators",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof com.draagon.meta.validator.MetaValidator
            );

            // PLACEMENT CONSTRAINT: Objects CAN contain views
            registry.registerPlacementConstraint(
                "object.views.placement",
                "Objects can contain views",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof com.draagon.meta.view.MetaView
            );

            // PLACEMENT CONSTRAINT: Objects CAN contain nested objects
            registry.registerPlacementConstraint(
                "object.nested.placement",
                "Objects can contain nested objects",
                (metadata) -> metadata instanceof MetaObject,
                (child) -> child instanceof MetaObject
            );

            // VALIDATION CONSTRAINT: Unique field names within object (applies to all objects)
            registry.registerValidationConstraint(
                "object.field.uniqueness",
                "Field names must be unique within an object",
                (metadata) -> metadata instanceof MetaObject,
                (metadata, value) -> {
                    if (metadata instanceof MetaObject) {
                        MetaObject obj = (MetaObject) metadata;
                        var fieldNames = obj.getChildren(MetaField.class).stream()
                            .map(field -> field.getName())
                            .collect(java.util.stream.Collectors.toSet());
                        var fieldList = obj.getChildren(MetaField.class);
                        return fieldNames.size() == fieldList.size(); // No duplicates
                    }
                    return true;
                }
            );

            // VALIDATION CONSTRAINT: Object names must follow identifier pattern
            registry.registerValidationConstraint(
                "object.naming.pattern",
                "Object names must follow identifier pattern",
                (metadata) -> metadata instanceof MetaObject,
                (metadata, value) -> {
                    String name = metadata.getName();
                    if (name == null) return false;
                    return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
                }
            );
            log.debug("Registered cross-cutting object constraints using consolidated registry");

        } catch (Exception e) {
            log.error("Failed to register cross-cutting object constraints", e);
        }
    }
}
