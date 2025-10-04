package com.metaobjects.object;

import com.metaobjects.*;
import com.metaobjects.attr.BooleanAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.constraint.RegexConstraint;
import com.metaobjects.constraint.UniquenessConstraint;
import com.metaobjects.field.MetaField;
import com.metaobjects.identity.MetaIdentity;
import com.metaobjects.identity.PrimaryIdentity;
import com.metaobjects.identity.SecondaryIdentity;
import com.metaobjects.relationship.MetaRelationship;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.validator.MetaValidator;
import com.metaobjects.view.MetaView;
import static com.metaobjects.MetaData.ATTR_IS_ABSTRACT;
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

    /**
     * Register MetaObject type and constraints with registry.
     * Called by ObjectTypesMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MetaObject.class, def -> {
            // ✅ FLUENT CONSTRAINTS WITH CONSTANTS
            def.type(TYPE_OBJECT).subType(SUBTYPE_BASE)
               .description("Base object metadata with common object attributes")
               .inheritsFrom(MetaData.TYPE_METADATA, MetaData.SUBTYPE_BASE);

            // Configure each attribute separately to avoid method chaining conflicts
            // UNIVERSAL ATTRIBUTES (all MetaData inherit these)
            def.optionalAttributeWithConstraints(ATTR_IS_ABSTRACT).ofType(BooleanAttribute.SUBTYPE_BOOLEAN).asSingle();

            // OBJECT-LEVEL ATTRIBUTES (all object types inherit these)
            def.optionalAttributeWithConstraints(ATTR_EXTENDS).ofType(StringAttribute.SUBTYPE_STRING).asSingle();
            def.optionalAttributeWithConstraints(ATTR_IMPLEMENTS).ofType(StringAttribute.SUBTYPE_STRING).asArray();  // ✅ MULTIPLE INTERFACES SUPPORT
            def.optionalAttributeWithConstraints(ATTR_IS_INTERFACE).ofType(BooleanAttribute.SUBTYPE_BOOLEAN).asSingle();

            // OBJECT-SPECIFIC ATTRIBUTES
            def.optionalAttributeWithConstraints(ATTR_DESCRIPTION).ofType(StringAttribute.SUBTYPE_STRING).asSingle();
            def.optionalAttributeWithConstraints(ATTR_OBJECT).ofType(StringAttribute.SUBTYPE_STRING).asSingle();
            def.optionalAttributeWithConstraints(ATTR_OBJECT_REF).ofType(StringAttribute.SUBTYPE_STRING).asSingle();

            // OBJECTS CONTAIN FIELDS (any field type, any name)
            def.optionalChild(MetaField.TYPE_FIELD, "*", "*");

            // OBJECTS CAN CONTAIN OTHER OBJECTS (composition)
            def.optionalChild(MetaObject.TYPE_OBJECT, "*", "*");

            // OBJECTS CAN CONTAIN IDENTITIES (primary and secondary)
            def.optionalChild(MetaIdentity.TYPE_IDENTITY, "*", "*");

            // OBJECTS CAN CONTAIN ATTRIBUTES
            def.optionalChild(MetaAttribute.TYPE_ATTR, "*", "*");

            // OBJECTS CAN CONTAIN VALIDATORS
            def.optionalChild(MetaValidator.TYPE_VALIDATOR, "*", "*");

            // OBJECTS CAN CONTAIN VIEWS
            def.optionalChild(MetaView.TYPE_VIEW, "*", "*");

            // OBJECTS CAN CONTAIN RELATIONSHIPS
            def.optionalChild(MetaRelationship.TYPE_RELATIONSHIP, "*", "*");
        });

        if (log != null) {
            log.debug("Registered base MetaObject type with unified registry");
        }

        // Register cross-cutting object constraints using consolidated registry
        registerCrossCuttingObjectConstraints(registry);
    }

    /**
     * Constructs the MetaObject with enhanced validation and metrics
     */
    public MetaObject(String subtype, String name ) {
        super( TYPE_OBJECT, subtype, name );
        
        if (log != null) {
            log.debug("Created MetaObject: {}:{}:{}", TYPE_OBJECT, subtype, name);
        }
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
            
            
            if (log != null) {
                log.debug("Successfully created instance of {}", getName());
            }

            return instance;
        } catch (Exception e) {

            if (log != null) {
                log.error("Failed to create instance of {}: {}", getName(), e.getMessage(), e);
            }
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

    // ✅ MIGRATED: getPrimaryKey() removed - use field.isPrimaryKey() on individual fields instead

    // ✅ MIGRATED: MetaKey methods removed - use MetaRelationship and field attributes instead
    // - For primary keys: Use field.isPrimaryKey()
    // - For secondary keys: Use field.isSecondaryKey()
    // - For foreign keys: Use AssociationRelationship children


    ////////////////////////////////////////////////////
    // RELATIONSHIP METHODS

    /**
     * Get all relationships defined in this MetaObject
     */
    public Collection<MetaRelationship> getRelationships() {
        return getRelationships(true);
    }

    /**
     * Get all relationships, optionally including inherited relationships
     */
    public Collection<MetaRelationship> getRelationships(boolean includeParentData) {
        return getChildren(MetaRelationship.class, includeParentData);
    }

    /**
     * Add a relationship to the MetaObject
     */
    public MetaObject addRelationship(MetaRelationship relationship) {
        addChild(relationship);
        return this;
    }

    /**
     * Check if a named relationship exists
     */
    public boolean hasRelationship(String name) {
        try {
            getRelationship(name);
            return true;
        } catch (MetaDataNotFoundException e) {
            return false;
        }
    }

    /**
     * Get a specific relationship by name
     */
    public MetaRelationship getRelationship(String relationshipName) {
        return useCache("getRelationship()", relationshipName, name -> {
            MetaRelationship relationship = null;
            try {
                relationship = (MetaRelationship) getChild(name, MetaRelationship.class);
            } catch (MetaDataNotFoundException e) {
                if (getSuperObject() != null) {
                    try {
                        relationship = getSuperObject().getRelationship(name);
                    } catch (MetaDataNotFoundException ex) {
                        // Expected - relationship not found in parent either
                    }
                }
                if (relationship == null) {
                    throw MetaDataNotFoundException.forRelationship(name, this);
                }
            }
            return relationship;
        });
    }

    /**
     * Find a relationship by name using Optional-based API
     */
    public Optional<MetaRelationship> findRelationship(String name) {
        try {
            return Optional.of(getRelationship(name));
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Get relationships by cardinality ("one" or "many")
     */
    public Collection<MetaRelationship> getRelationshipsByCardinality(String cardinality) {
        return useCache("getRelationshipsByCardinality()", cardinality, card -> {
            Collection<MetaRelationship> filtered = new ArrayList<>();
            for (MetaRelationship rel : getRelationships()) {
                if (card.equals(rel.getCardinality())) {
                    filtered.add(rel);
                }
            }
            return filtered;
        });
    }

    /**
     * Get relationships by semantic type ("composition", "aggregation", "association")
     */
    public Collection<MetaRelationship> getRelationshipsBySemanticType(String semanticType) {
        return useCache("getRelationshipsBySemanticType()", semanticType, type -> {
            Collection<MetaRelationship> filtered = new ArrayList<>();
            for (MetaRelationship rel : getRelationships()) {
                if (type.equals(rel.getSubType())) {
                    filtered.add(rel);
                }
            }
            return filtered;
        });
    }

    /**
     * Get relationships that target a specific object
     */
    public Collection<MetaRelationship> getRelationshipsByTarget(String targetObject) {
        return useCache("getRelationshipsByTarget()", targetObject, target -> {
            Collection<MetaRelationship> filtered = new ArrayList<>();
            for (MetaRelationship rel : getRelationships()) {
                if (target.equals(rel.getTargetObject())) {
                    filtered.add(rel);
                }
            }
            return filtered;
        });
    }

    /**
     * Get composition relationships (semantic type = "composition")
     */
    public Collection<MetaRelationship> getCompositionRelationships() {
        return getRelationshipsBySemanticType("composition");
    }

    /**
     * Get aggregation relationships (semantic type = "aggregation")
     */
    public Collection<MetaRelationship> getAggregationRelationships() {
        return getRelationshipsBySemanticType("aggregation");
    }

    /**
     * Get association relationships (semantic type = "association")
     */
    public Collection<MetaRelationship> getAssociationRelationships() {
        return getRelationshipsBySemanticType("association");
    }

    // === IDENTITY METHODS ===

    /**
     * Get all identities (primary and secondary)
     */
    public Collection<MetaIdentity> getIdentities() {
        return getChildren(MetaIdentity.class);
    }

    /**
     * Get all identities, optionally including inherited identities
     */
    public Collection<MetaIdentity> getIdentities(boolean includeParentData) {
        return getChildren(MetaIdentity.class, includeParentData);
    }

    /**
     * Add an identity to the MetaObject
     */
    public MetaObject addIdentity(MetaIdentity identity) {
        addChild(identity);
        return this;
    }

    /**
     * Get the primary identity for this object
     * @return the primary identity, or null if none defined
     */
    public PrimaryIdentity getPrimaryIdentity() {
        return useCache("getPrimaryIdentity()", () -> {
            Collection<PrimaryIdentity> primaries = getChildren(PrimaryIdentity.class);
            return primaries.isEmpty() ? null : primaries.iterator().next();
        });
    }

    /**
     * Get all secondary identities for this object
     */
    public Collection<SecondaryIdentity> getSecondaryIdentities() {
        return useCache("getSecondaryIdentities()", () -> {
            return getChildren(SecondaryIdentity.class);
        });
    }

    /**
     * Check if a named identity exists
     */
    public boolean hasIdentity(String name) {
        try {
            getIdentity(name);
            return true;
        } catch (MetaDataNotFoundException e) {
            return false;
        }
    }

    /**
     * Get a specific identity by name
     */
    public MetaIdentity getIdentity(String identityName) {
        return useCache("getIdentity()", identityName, name -> {
            MetaIdentity identity = null;
            try {
                identity = (MetaIdentity) getChild(name, MetaIdentity.class);
            } catch (MetaDataNotFoundException e) {
                if (getSuperObject() != null) {
                    try {
                        identity = getSuperObject().getIdentity(name);
                    } catch (MetaDataNotFoundException ex) {
                        // Expected - identity not found in parent either
                    }
                }
                if (identity == null) {
                    throw MetaDataNotFoundException.forIdentity(name, this);
                }
            }
            return identity;
        });
    }

    /**
     * Find an identity by name using Optional-based API
     */
    public Optional<MetaIdentity> findIdentity(String name) {
        try {
            return Optional.of(getIdentity(name));
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Find a primary identity by name using Optional-based API
     */
    public Optional<PrimaryIdentity> findPrimaryIdentity(String name) {
        try {
            MetaIdentity identity = getIdentity(name);
            if (identity instanceof PrimaryIdentity) {
                return Optional.of((PrimaryIdentity) identity);
            }
            return Optional.empty();
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Find a secondary identity by name using Optional-based API
     */
    public Optional<SecondaryIdentity> findSecondaryIdentity(String name) {
        try {
            MetaIdentity identity = getIdentity(name);
            if (identity instanceof SecondaryIdentity) {
                return Optional.of((SecondaryIdentity) identity);
            }
            return Optional.empty();
        } catch (MetaDataNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Get one-to-one relationships (cardinality = "one")
     */
    public Collection<MetaRelationship> getOneToOneRelationships() {
        return getRelationshipsByCardinality(MetaRelationship.CARDINALITY_ONE);
    }

    /**
     * Get one-to-many relationships (cardinality = "many")
     */
    public Collection<MetaRelationship> getOneToManyRelationships() {
        return getRelationshipsByCardinality(MetaRelationship.CARDINALITY_MANY);
    }

    /**
     * Get relationships as a Stream for functional operations
     */
    public Stream<MetaRelationship> getRelationshipsStream() {
        return getRelationships().stream();
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
            registry.addConstraint(new PlacementConstraint(
                "object.fields.placement",
                "Objects can contain fields",
                TYPE_OBJECT, "*",               // Parent: object.*
                MetaField.TYPE_FIELD, "*",      // Child: field.*
                null,                           // No name constraint
                true                            // Allowed
            ));

            // PLACEMENT CONSTRAINT: Objects CAN contain attributes
            registry.addConstraint(new PlacementConstraint(
                "object.attributes.placement",
                "Objects can contain attributes",
                TYPE_OBJECT, "*",               // Parent: object.*
                MetaAttribute.TYPE_ATTR, "*",   // Child: attr.*
                null,                           // No name constraint
                true                            // Allowed
            ));

            // ✅ MIGRATED: MetaKey constraint removed - keys are now handled via:
            // - Field attributes (isPrimaryKey, isSecondaryKey)
            // - MetaRelationship children (for foreign keys)

            // PLACEMENT CONSTRAINT: Objects CAN contain validators
            registry.addConstraint(new PlacementConstraint(
                "object.validators.placement",
                "Objects can contain validators",
                TYPE_OBJECT, "*",               // Parent: object.*
                MetaValidator.TYPE_VALIDATOR, "*",  // Child: validator.*
                null,                           // No name constraint
                true                            // Allowed
            ));

            // PLACEMENT CONSTRAINT: Objects CAN contain views
            registry.addConstraint(new PlacementConstraint(
                "object.views.placement",
                "Objects can contain views",
                TYPE_OBJECT, "*",               // Parent: object.*
                MetaView.TYPE_VIEW, "*",        // Child: view.*
                null,                           // No name constraint
                true                            // Allowed
            ));

            // PLACEMENT CONSTRAINT: Objects CAN contain nested objects
            registry.addConstraint(new PlacementConstraint(
                "object.nested.placement",
                "Objects can contain nested objects",
                TYPE_OBJECT, "*",               // Parent: object.*
                TYPE_OBJECT, "*",               // Child: object.*
                null,                           // No name constraint
                true                            // Allowed
            ));

            // PLACEMENT CONSTRAINT: Objects CAN contain relationships
            registry.addConstraint(new PlacementConstraint(
                "object.relationships.placement",
                "Objects can contain relationships",
                TYPE_OBJECT, "*",                       // Parent: object.*
                MetaRelationship.TYPE_RELATIONSHIP, "*", // Child: relationship.*
                null,                                   // No name constraint
                true                                    // Allowed
            ));

            // UNIQUENESS CONSTRAINT: Unique field names within object (using standard constraint pattern)
            registry.addConstraint(UniquenessConstraint.forFieldNames(
                "object.field.uniqueness",
                "Field names must be unique within an object",
                TYPE_OBJECT, "*"
            ));

            // VALIDATION CONSTRAINT: Object names must follow identifier pattern
            registry.addConstraint(new RegexConstraint(
                "object.naming.pattern",
                "Object names must follow identifier pattern",
                "object",                   // Target type
                "*",                        // Any subtype
                "*",                        // Any object name
                "^[a-zA-Z][a-zA-Z0-9_]*$",  // Identifier pattern
                false                       // Don't allow null (required)
            ));
            if (log != null) {
                log.debug("Registered cross-cutting object constraints using consolidated registry");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register cross-cutting object constraints", e);
            }
        }
    }
}
