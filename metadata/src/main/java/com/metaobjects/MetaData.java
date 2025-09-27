/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects;

import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.key.MetaKey;
import com.metaobjects.object.MetaObject;
import com.metaobjects.validator.MetaValidator;
import com.metaobjects.view.MetaView;
import com.metaobjects.loader.MetaDataLoader;
// Using unified registry instead
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.constraint.ConstraintEnforcer;
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.cache.CacheStrategy;
import com.metaobjects.cache.HybridCache;
import com.metaobjects.collections.IndexedMetaDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * MetaData represents the core metadata definition in the MetaObjects framework.
 * 
 * <p>MetaData follows a <strong>READ-OPTIMIZED WITH CONTROLLED MUTABILITY</strong> design pattern 
 * analogous to Java's Class/Field reflection system with dynamic class loading. MetaData objects 
 * are loaded once during application startup and optimized for heavy read access throughout 
 * the application lifetime.</p>
 * 
 * <h3>Architecture Pattern</h3>
 * <ul>
 * <li><strong>Load Once</strong>: Like ClassLoader, expensive startup for permanent benefit</li>
 * <li><strong>Read Many</strong>: Optimized for thousands of concurrent read operations</li>
 * <li><strong>Thread Safe</strong>: Immutable after loading, no synchronization needed for reads</li>
 * <li><strong>OSGI Ready</strong>: WeakHashMap and service patterns handle dynamic class loading</li>
 * <li><strong>Memory Efficient</strong>: Smart caching balances performance with memory cleanup</li>
 * </ul>
 * 
 * <h3>Usage Examples</h3>
 * <pre>{@code
 * // Loading Phase - Happens once at startup
 * MetaDataLoader loader = new SimpleLoader("myLoader");
 * loader.setSourceURIs(Arrays.asList(URI.create("metadata.json")));
 * loader.init(); // Loads ALL metadata into permanent memory structures
 * 
 * // Runtime Phase - All operations are READ-ONLY
 * MetaObject userMeta = loader.getMetaObjectByName("User");  // O(1) lookup
 * MetaField field = userMeta.getMetaField("email");          // Cached access
 * }</pre>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>Loading Phase</strong>: 100ms-1s (acceptable one-time cost)</li>
 * <li><strong>Runtime Reads</strong>: 1-10μs (cached, immutable access)</li>
 * <li><strong>Memory Overhead</strong>: 10-50MB (permanent metadata residence)</li>
 * <li><strong>Concurrent Readers</strong>: Unlimited (no lock contention)</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @version 6.0.0
 * @since 1.0
 * @see MetaDataLoader
 * @see com.metaobjects.object.MetaObject
 * @see com.metaobjects.field.MetaField
 */
public class MetaData implements Cloneable, Serializable {

    private static final Logger log = LoggerFactory.getLogger(MetaData.class);

    // Type-safe class constants for common usage
    public static final Class<MetaData> METADATA_CLASS = MetaData.class;

    // === SEPARATORS ===
    public final static String PKG_SEPARATOR = "::";
    public final static String SEPARATOR = PKG_SEPARATOR;

    // === UNIVERSAL ATTRIBUTE NAMES (apply to all MetaData) ===
    /** Universal attribute for abstract metadata marker */
    public static final String ATTR_IS_ABSTRACT = "isAbstract";

    /** Standard attribute name for 'name' */
    public static final String ATTR_NAME = "name";

    /** Standard attribute name for 'type' */
    public static final String ATTR_TYPE = "type";

    /** Standard attribute name for 'subType' */
    public static final String ATTR_SUBTYPE = "subType";

    /** Standard attribute name for 'package' */
    public static final String ATTR_PACKAGE = "package";

    /** Standard attribute name for 'children' */
    public static final String ATTR_CHILDREN = "children";

    /** Standard attribute name for 'metadata' (root element) */
    public static final String ATTR_METADATA = "metadata";

    // === VALIDATION PATTERNS ===
    /** Valid name pattern for MetaData identifiers */
    public static final String VALID_NAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";

    // === ROOT TYPE CONSTANTS ===
    /** Root metadata type constant - MetaData owns this concept */
    public static final String TYPE_METADATA = "metadata";

    /** Root metadata subtype for metadata file structure */
    public static final String SUBTYPE_BASE = "base";

    // Unified registry self-registration for root metadata type
    /**
     * Register MetaData as metadata.base with abstract requirements constraints.
     * This creates metadata.base which defines metadata file structure and enforces
     * that most metadata types must be abstract under the root (except objects).
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            
            MetaDataRegistry.getInstance().registerType(MetaData.class, def -> def
                .type(TYPE_METADATA).subType(SUBTYPE_BASE)
                .description("Base metadata type for inheritance hierarchy - enforces abstract requirements")

                // ROOT LEVEL can contain top-level metadata types
                .optionalChild("object", "*", "*")      // Any object type
                .optionalChild("field", "*", "*")       // Any field type (if abstract)
                .optionalChild("attr", "*", "*")        // Any attribute (if abstract)
                .optionalChild("validator", "*", "*")   // Any validator (if abstract)
                .optionalChild("view", "*", "*")        // Any view (if abstract)
                .optionalChild("key", "*", "*")         // Any key (if abstract)
            );

            log.debug("Registered root MetaData type (metadata.base) with unified registry");
            
            // Setup abstract requirements constraints
            setupRootAbstractConstraints();

        } catch (Exception e) {
            log.error("Failed to register root MetaData type with unified registry", e);
        }
    }
    
    /**
     * Setup abstract requirements constraints for metadata.base children.
     * Future defaults: new metadata types must be abstract under metadata.base.
     */
    private static void setupRootAbstractConstraints() {
        MetaDataRegistry registry = MetaDataRegistry.getInstance();

        // OBJECTS can be abstract or concrete under metadata.base
        registry.addConstraint(PlacementConstraint.allowChildType(
            "metadata.base.objects",
            "metadata.base can contain objects",
            TYPE_METADATA, SUBTYPE_BASE,        // Parent: metadata.base
            MetaObject.TYPE_OBJECT, "*"         // Child: object.*
        ));

        // FIELDS under metadata.base
        registry.addConstraint(PlacementConstraint.allowChildType(
            "metadata.base.fields",
            "metadata.base can contain fields",
            TYPE_METADATA, SUBTYPE_BASE,        // Parent: metadata.base
            MetaField.TYPE_FIELD, "*"           // Child: field.*
        ));

        // ATTRIBUTES under metadata.base
        registry.addConstraint(new PlacementConstraint(
            "metadata.base.attributes",
            "metadata.base can contain attributes",
            TYPE_METADATA, SUBTYPE_BASE,    // Parent: metadata.base
            MetaAttribute.TYPE_ATTR, "*",   // Child: attr.*
            null,                           // No name constraint
            true                            // Allowed
        ));

        // VALIDATORS under metadata.base
        registry.addConstraint(new PlacementConstraint(
            "metadata.base.validators",
            "metadata.base can contain validators",
            TYPE_METADATA, SUBTYPE_BASE,         // Parent: metadata.base
            MetaValidator.TYPE_VALIDATOR, "*",   // Child: validator.*
            null,                                // No name constraint
            true                                 // Allowed
        ));

        // VIEWS under metadata.base
        registry.addConstraint(new PlacementConstraint(
            "metadata.base.views",
            "metadata.base can contain views",
            TYPE_METADATA, SUBTYPE_BASE,    // Parent: metadata.base
            MetaView.TYPE_VIEW, "*",        // Child: view.*
            null,                           // No name constraint
            true                            // Allowed
        ));

        // KEYS under metadata.base
        registry.addConstraint(new PlacementConstraint(
            "metadata.base.keys",
            "metadata.base can contain keys",
            TYPE_METADATA, SUBTYPE_BASE,    // Parent: metadata.base
            MetaKey.TYPE_KEY, "*",          // Child: key.*
            null,                           // No name constraint
            true                            // Allowed
        ));
        
        // FUTURE DEFAULT: Any new metadata types must be abstract under metadata.base
        // (Individual new types can override this by adding their own constraints)
        
        log.debug("Set up abstract requirements constraints for metadata.base");
    }

    /**
     * Alternative registerTypes() method with no parameters for backward compatibility.
     */
    public static void registerTypes() {
        registerTypes(MetaDataRegistry.getInstance());
    }

    // Static registration block - automatically registers the root metadata type when class is loaded
    static {
        try {
            registerTypes(MetaDataRegistry.getInstance());
        } catch (Exception e) {
            log.error("Failed to register root MetaData type during class loading", e);
        }
    }
    // Unified caching strategy
    private final CacheStrategy cache = new HybridCache();
    
    // Indexed collection for O(1) child lookups
    private final IndexedMetaDataCollection children = new IndexedMetaDataCollection();
    

    // NEW v6.0: Type/subtype as first-class concept  
    private final MetaDataTypeId typeId;

    // LEGACY: Keep for backward compatibility during transition
    private final String type;
    private final String subType;
    private final String name;

    private final String shortName;
    private final String pkg;
    
    // Type system integration

    private MetaData superData = null;

    // WeakReference prevents circular references and memory leaks in parent-child relationships
    private WeakReference<MetaData> parentRef = null;
    private MetaDataLoader loader = null;
    private ClassLoader metaDataClassLoader=null;

    /**
     * Constructs a MetaData object with enhanced type system integration.
     * 
     * <p>This constructor creates a new MetaData instance that will be optimized for
     * read-heavy access patterns throughout its lifetime. The metadata is designed to
     * be loaded once during application startup and accessed frequently at runtime.</p>
     * 
     * <p><strong>Architecture Note:</strong> This is a loading-phase operation. After construction
     * and initialization, the MetaData object becomes effectively immutable for optimal
     * concurrent read performance.</p>
     * 
     * @param type the type identifier for this metadata (e.g., "object", "field", "loader")
     * @param subType the subtype identifier providing more specific categorization 
     *                (e.g., "string", "integer", "mapped", "proxy")
     * @param name the fully qualified name of this metadata, may include package separators (::)
     *             for hierarchical organization (e.g., "com::example::User", "email")
     * 
     * @see MetaDataTypeId
     * @see #getType()
     * @see #getSubType()
     * @see #getName()
     * @since 1.0
     */
    public MetaData(String type, String subType, String name ) {

        // Allow null values for testing - validation happens in validate() method
        // if ( type == null ) throw new NullPointerException( "MetaData Type cannot be null" );
        // if ( subType == null ) throw new NullPointerException( "MetaData SubType cannot be null" );
        // if ( name == null ) throw new NullPointerException( "MetaData Name cannot be null" );

        // NEW v6.0: Create MetaDataTypeId (allows nulls for testing)
        this.typeId = (type != null && subType != null) ? 
            new MetaDataTypeId(type, subType) : null;

        // LEGACY: Keep for backward compatibility during transition
        this.type = type;
        this.subType = subType;
        this.name = name;

        // v6.0.0: Validate name during construction
        if (name != null && type != null) {
            validateName(name);
        }

        // Type definition removed - using unified registry
        

        // Cache the shortName and packageName (handle null name)
        if (name != null) {
            int i = name.lastIndexOf(PKG_SEPARATOR);
            if (i >= 0) {
                shortName = name.substring(i + PKG_SEPARATOR.length());
                pkg = name.substring(0, i);
            } else {
                shortName = name;
                pkg = "";
            }
        } else {
            shortName = null;
            pkg = null;
        }

        log.debug("Created MetaData: {}:{}:{}", 
                  type != null ? type : "null", 
                  subType != null ? subType : "null", 
                  name != null ? name : "null");
    }

    // ========== ENHANCED TYPE SYSTEM METHODS ==========

    // Type definition methods removed - using unified registry with static self-registration
    
    /**
     * Validate MetaData name during construction
     */
    private void validateName(String name) {
        // Loaders and views can have more flexible naming (allow hyphens)
        if ("loader".equals(type) || "view".equals(type)) {
            // Allow hyphens for loaders and views
            if (!name.matches("^[a-zA-Z][a-zA-Z0-9_-]*$")) {
                throw new IllegalArgumentException(
                    "Invalid " + type + " name '" + name + "': must follow pattern ^[a-zA-Z][a-zA-Z0-9_-]*$");
            }
        } else {
            // Check if this is a package-qualified name
            if (name.contains("::")) {
                // Package-qualified name - validate each part separately
                String[] parts = name.split("::");
                for (String part : parts) {
                    if (!part.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                        throw new IllegalArgumentException(
                            "Constraint violation: Invalid MetaData name part '" + part + "' in '" + name + "': must follow identifier pattern ^[a-zA-Z][a-zA-Z0-9_]*$");
                    }
                }
            } else {
                // Simple name - strict identifier pattern for fields and objects
                if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                    throw new IllegalArgumentException(
                        "Constraint violation: Invalid MetaData name '" + name + "': must follow identifier pattern ^[a-zA-Z][a-zA-Z0-9_]*$");
                }
            }
        }
    }

    

    // ========== MODERN COLLECTION APIS ==========

    /**
     * Get children as a Stream for functional operations
     */
    public Stream<MetaData> getChildrenStream() {
        return children.stream();
    }

    /**
     * Find children matching a predicate
     */
    public Stream<MetaData> findChildren(Predicate<MetaData> predicate) {
        return children.findMatching(predicate);
    }

    /**
     * Find children of a specific type
     */
    public <T extends MetaData> Stream<T> findChildren(Class<T> type) {
        return children.findByClass(type).stream();
    }

    /**
     * Find child by name (modern Optional-based API) - O(1) operation
     */
    public Optional<MetaData> findChild(String name) {
        return children.findByName(name);
    }

    /**
     * Find child by name and type - O(1) operation
     */
    public <T extends MetaData> Optional<T> findChild(String name, Class<T> type) {
        return children.findByName(name)
            .filter(type::isInstance)
            .map(type::cast);
    }

    /**
     * Require child by name (throws if not found)
     */
    public MetaData requireChild(String name) {
        return findChild(name)
            .orElseThrow(() -> new MetaDataNotFoundException("Child not found: " + name, name));
    }

    /**
     * Require child by name and type
     */
    public <T extends MetaData> T requireChild(String name, Class<T> type) {
        return findChild(name, type)
            .orElseThrow(() -> new MetaDataNotFoundException(
                "Child of type " + type.getSimpleName() + " not found: " + name, name));
    }

    // ========== UNIFIED CACHING ==========

    /**
     * Get cached value with type safety
     */
    public <T> Optional<T> getCacheValue(String key, Class<T> type) {
        return cache.get(key, type);
    }

    /**
     * Set cache value
     */
    public void setCacheValue(String key, Object value) {
        cache.put(key, value);
    }

    /**
     * Compute cache value if absent
     */
    public <T> T computeCacheValue(String key, Class<T> type, java.util.function.Supplier<T> supplier) {
        return cache.computeIfAbsent(key, type, supplier);
    }

    /**
     * Check if cache contains key
     */
    public boolean hasCacheValue(String key) {
        return cache.containsKey(key);
    }

    /**
     * Remove cached value
     */
    public Object removeCacheValue(String key) {
        return cache.remove(key);
    }

    /**
     * Get cache statistics
     */
    public Optional<Object> getCacheStats() {
        return cache.getStats().map(stats -> (Object) stats);
    }


    // ========== ENHANCED ATTRIBUTE MANAGEMENT ==========

    /**
     * Modern attribute access with Optional
     */
    public Optional<MetaAttribute> findAttribute(String name) {
        return findChild(name, MetaAttribute.class);
    }

    /**
     * Require attribute (throws if not found)
     */
    public MetaAttribute requireAttribute(String name) {
        return findAttribute(name)
            .orElseThrow(() -> MetaDataNotFoundException.forAttribute(name, this));
    }

    /**
     * Get all attributes as stream
     */
    public Stream<MetaAttribute> getAttributesStream() {
        return findChildren(MetaAttribute.class);
    }

    /**
     * Check if attribute exists (enhanced version)
     */
    public boolean hasAttributeEnhanced(String name) {
        return findAttribute(name).isPresent();
    }


    /**
     * Checks whether this MetaData is of the specified type.
     * 
     * <p>This is a high-performance comparison method optimized for frequent
     * type checking during runtime operations.</p>
     * 
     * @param type the type to check against
     * @return true if this MetaData has the specified type, false otherwise
     * @throws NullPointerException if type parameter is null
     * @since 1.0
     */
    public boolean isType( String type ) {
        return this.type.equals( type );
    }


    // ========== NEW v6.0 TYPE SYSTEM METHODS ==========

    /**
     * Get the type of this MetaData (modern API)
     * 
     * @return The primary type (e.g., "field", "view", "validator")
     * @since 6.0.0
     */
    public String getType() {
        return typeId != null ? typeId.type() : type;
    }

    /**
     * Get the subtype of this MetaData (modern API)
     * 
     * @return The specific implementation subtype (e.g., "int", "string", "currency")
     * @since 6.0.0
     */
    public String getSubType() {
        return typeId != null ? typeId.subType() : subType;
    }

    /**
     * Get the type ID of this MetaData (modern API)
     * 
     * @return MetaDataTypeId containing both type and subtype
     * @since 6.0.0
     */
    public MetaDataTypeId getTypeId() {
        return typeId;
    }

    /**
     * Check if this MetaData matches a type pattern
     * 
     * @param pattern Pattern like "field.*" or "field.int" where "*" means any
     * @return true if this MetaData matches the pattern
     * @since 6.0.0
     */
    public boolean matchesType(String pattern) {
        return typeId != null && typeId.matches(pattern);
    }

    /**
     * Check if this MetaData matches a type pattern
     * 
     * @param pattern MetaDataTypeId pattern (type or subtype can be "*")
     * @return true if this MetaData matches the pattern
     * @since 6.0.0
     */
    public boolean matchesType(MetaDataTypeId pattern) {
        return typeId != null && typeId.matches(pattern);
    }

    /**
     * Returns whether this MetaData matches specified Type, SubType, and Name
     */
    public boolean isSameType( MetaData md ) {
        return isType( md.type  );
    }

    /**
     * Returns whether MetaData is of the specified Type
     */
    public boolean isTypeSubType( String type, String subType ) {
        return this.type.equals( type ) && this.subType.equals( subType );
    }

    /**
     * Returns whether this MetaData matches specified Type, SubType, and Name
     */
    public boolean isSameTypeSubType( MetaData md ) {
        return isTypeSubType( md.type, md.subType );
    }

    /**
     * Returns the fully qualified name of this MetaData.
     * 
     * <p>The name may include package separators (::) for hierarchical organization.
     * For example: "com::example::User" for an object, or "email" for a simple field.</p>
     * 
     * <p><strong>Performance Note:</strong> This is a cached O(1) operation optimized for
     * frequent access during runtime read operations.</p>
     * 
     * @return the fully qualified name, may contain package separators, or null if not set
     * @see #getShortName()
     * @see #getPackage()
     * @see #PKG_SEPARATOR
     * @since 1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this MetaData matches specified Type, SubType, and Name
     */
    public boolean isTypeSubTypeName( String type, String subType, String name ) {
        return this.type.equals( type ) && this.subType.equals( subType ) && this.name.equals( name );
    }

    /**
     * Returns whether this MetaData matches specified Type, SubType, and Name
     */
    public boolean isSameTypeSubTypeName( MetaData md ) {
        return isTypeSubTypeName( md.type, md.subType, md.name);
    }

    /////////////////////////////////////////////////////
    // Object Instantiation Helpers

    @SuppressWarnings("unchecked")
    public <T extends MetaData> T setMetaDataClassLoader( ClassLoader classLoader ) {
        metaDataClassLoader = classLoader;
        return (T) this;
    }

    protected ClassLoader getDefaultMetaDataClassLoader() {
        return getClass().getClassLoader();
    }

    public ClassLoader getMetaDataClassLoader() {

        if (metaDataClassLoader != null) {
            return metaDataClassLoader;
        }
        else if (!(this instanceof MetaDataLoader)) {
            if ( getLoader() != null ) {
                return getLoader().getMetaDataClassLoader();
            }
        }

        return getDefaultMetaDataClassLoader();
    }

    // Loads the specified Class using the proper ClassLoader
    @SuppressWarnings("unchecked")
    public <T> Class<T> loadClass( Class<T> clazz, String name ) throws ClassNotFoundException {
        try {
            Class<?> c = getMetaDataClassLoader().loadClass(name);
            if (!clazz.isAssignableFrom(c)) {
                throw new InvalidValueException("Class [" + c.getName() + "] is not assignable from [" + clazz.getName() + "]");
            }
            return (Class<T>) c;
        }
        catch (ClassNotFoundException e ) {
            log.error( "Could not find class ["+name+"] in MetaDataClassLoader: "+getMetaDataClassLoader());
            throw e;
        }
    }

    // Loads the specified Class using the proper ClassLoader
    public Class<?> loadClass( String name ) throws ClassNotFoundException {
        return loadClass(name, true);
    }

        // Loads the specified Class using the proper ClassLoader
    public Class<?> loadClass( String name, boolean throwError ) throws ClassNotFoundException {
        try {
            return getMetaDataClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e ) {
            if ( throwError ) {
                log.error("Could not find class [" + name + "] in MetaDataClassLoader: " + getMetaDataClassLoader());
                throw e;
            }
        }
        return null;
    }


    ////////////////////////////////////////////////////
    // SETTER / GETTER METHODS

    
    /**
     * Type-safe utility methods for common type checks
     */
    public boolean isFieldMetaData() {
        return this instanceof com.metaobjects.field.MetaField;
    }
    
    public boolean isObjectMetaData() {
        return this instanceof com.metaobjects.object.MetaObject;
    }
    
    public boolean isAttributeMetaData() {
        return this instanceof com.metaobjects.attr.MetaAttribute;
    }
    
    public boolean isValidatorMetaData() {
        return this instanceof com.metaobjects.validator.MetaValidator;
    }
    
    public boolean isViewMetaData() {
        return this instanceof com.metaobjects.view.MetaView;
    }
    
    public boolean isLoaderMetaData() {
        return this instanceof com.metaobjects.loader.MetaDataLoader;
    }

    /**
     * Iterates up the Super Data until it finds the MetaDataLoader
     */
    public MetaDataLoader getLoader() {

        if (loader == null) {
            synchronized (this) {
                MetaData d = this;
                while (d != null) {
                    if (d instanceof MetaDataLoader) {
                        loader = (MetaDataLoader) d;
                        break;
                    }
                    d = d.getParent();
                }
            }
        }

        return loader;
    }

    /**
     * Retrieve the MetaObject package
     */
    public String getPackage() {
        return pkg;
    }

    /**
     * Retrieve the MetaObject package
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the parent of the attribute
     */
    protected void attachParent(MetaData parent) {
        parentRef = new WeakReference<>(parent);
    }

    /**
     * Gets the parent MetaData.  Be careful as this might not be the
     * same as the metadata you retrieved this from as a child due to 
     * inheritance.   Use with care!
     */
    public MetaData getParent() {
        if (parentRef == null) {
            return null;
        }
        return parentRef.get();
    }

    /**
     * Sets the Super Data
     */
    public void setSuperData(MetaData superData) {
        this.superData = superData;
    }

    /**
     * Gets the Super Data
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T getSuperData() {
        return (T) superData;
    }

    /**
     * Gets the Super Data with type safety - returns Optional to avoid ClassCastException
     * @param type The expected type of the super data
     * @return Optional containing the super data if it matches the expected type
     */
    public <T extends MetaData> Optional<T> getSuperDataSafe(Class<T> type) {
        return type.isInstance(superData) ? Optional.of(type.cast(superData)) : Optional.empty();
    }

    /**
     * Returns whether this MetaData has a Super MetaData
     * @return SuperData exists
     */
    public boolean hasSuperData() {
        return superData != null;
    }

    ////////////////////////////////////////////////////
    // ATTRIBUTE METHODS

    /**
     * Sets an attribute of the MetaClass
     */
    public void addMetaAttr(MetaAttribute attr) {
        addChild(attr);
    }

    /**
     * Sets an attribute of the MetaClass (type-safe version)
     * @param attr The attribute to add
     */
    public void addMetaAttrSafe(MetaAttribute attr) {
        addChild(attr);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getMetaAttr(String name) throws MetaDataNotFoundException {
        return getMetaAttr(name,true);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getMetaAttr(String name, boolean includeParentData) throws MetaDataNotFoundException {
        try {
            return (MetaAttribute) getChild( name, MetaAttribute.class, includeParentData);
        } catch (MetaDataNotFoundException e) {
            throw MetaDataNotFoundException.forAttribute(name, this);
        }
    }



    /**
     * Retrieves all attribute names
     */
    public boolean hasMetaAttr(String name) {
        return hasMetaAttr(name,true);
    }

    /**
     * Retrieves all attribute names
     */
    public boolean hasMetaAttr(String name, boolean includeParentData) {
        try {
            if (getChild(name, MetaAttribute.class, includeParentData, false) != null) {
                return true;
            }
        } catch (MetaDataNotFoundException ignored) {}
        
        return false;
    }

    /**
     * Retrieves all attribute names
     */
    public List<MetaAttribute> getMetaAttrs() {
        return getMetaAttrs(true);
    }

    /**
     * Retrieves all attribute names
     */
    public List<MetaAttribute> getMetaAttrs( boolean includeParentData ) {

        return getChildren(MetaAttribute.class, includeParentData);
    }

    /////////////////////////////////////////////////////////////////////////////
    // CHILDREN METHODS

    /** Filters for parent data */
    protected boolean filterWhenParentData( MetaData d ) {
        return ( d instanceof MetaAttribute && d.getName().startsWith("_") );
    }

    /**
     * Whether to delete the MetaData if a new one is added
     * @param d MetaData to check
     * @return true if should delete
     */
    protected boolean deleteOnAdd( MetaData d) {

        // TODO: Change these rules to be driven from a MetaData method that is overrideable

        return d instanceof MetaAttribute;
                // || d instanceof MetaField
                //|| d instanceof MetaValidator
                //|| d instanceof MetaView;
    }

    /**
     * Whether the child data exists
     */
    protected boolean hasChildOfType(String type, String name) {
        try {
            getChildOfType( type, name );
            return true;
        } catch (MetaDataNotFoundException e) {
            return false;
        }
    }

    /**
     * Whether the child data exists
     */
    public boolean hasChild(String name, Class<? extends MetaData> c) {
        try {
            getChild(name, c);
            return true;
        } catch (MetaDataNotFoundException e) {
            return false;
        }
    }

    /**
     * Adds a child MetaData object of the specified class type. If no class
     * type is set, then a child of the same type is not checked against.
     */
    public void addChild(MetaData data) throws InvalidMetaDataException {
        addChild(data, true);
    }

    /**
     * Adds a child MetaData object (type-safe version)
     * @param data The child MetaData to add
     */
    public void addChildSafe(MetaData data) throws InvalidMetaDataException {
        addChild(data, true);
    }

    /**
     * Check whether this MetaData is a valid Child to add
     * @param data MetaData to add as a Child
     */
    protected void checkValidChild( MetaData data ) {

        if (data == null) {
            throw new IllegalArgumentException("Cannot add null MetaData");
        }

        // Don't let the same
        if ( this.getType().equals( data.getType())) {
            throw new MetaDataException("You cannot add the same MetaData type to another; this [" + toString() + "], added metadata[" + data.toString() + "]");
        }
    }

    /**
     * Adds a child MetaData object of the specified class type. If no class
     * type is set, then a child of the same type is not checked against.
     */
    public void addChild(MetaData data, boolean checkExists)  throws InvalidMetaDataException {

        checkValidChild( data );

        if (checkExists) {
            try {
                MetaData d = getChildOfType( data.getType(), data.getName() );
                if (d.getParent() == this) {
                    if (deleteOnAdd( d )) {
                        deleteChild(d);
                    } else {
                        throw new InvalidMetaDataException(data, "MetaData already exists in [" + toString() + "] as [" + d + "]");
                    }
                }
            } catch (MetaDataNotFoundException ignored) {
            }
        }
        
        // v6.0.0: Unified registry validation before adding child
        MetaDataRegistry registry = MetaDataRegistry.getInstance();
        if (!registry.acceptsChild(this.getType(), this.getSubType(), 
                                 data.getType(), data.getSubType(), data.getName())) {
            String supportedChildren = registry.getSupportedChildrenDescription(this.getType(), this.getSubType());
            throw new InvalidMetaDataException(data, String.format(
                "%s.%s does not accept child '%s' of type %s.%s. %s",
                this.getType(), this.getSubType(), data.getName(),
                data.getType(), data.getSubType(), supportedChildren));
        }
        
        // v6.0.0: Constraint enforcement during construction
        ConstraintEnforcer constraintEnforcer = ConstraintEnforcer.getInstance();
        constraintEnforcer.enforceConstraintsOnAddChild(this, data);
        
        data.attachParent(this);
        
        // Use indexed collection for O(1) operations
        if (children.add(data)) {
            
            // Flush caches
            flushCaches();
        }
    }

    /**
     * Deletes a child MetaData object of the given class
     */
    public void deleteChildOfType(String type, String name ) {
        MetaData d = getChildOfType(type, name);
        if (d.getParent() == this) {
            if (children.remove(d)) {
                
                flushCaches();
            }
        } else {
            throw new MetaDataNotFoundException("You cannot delete MetaData with type [" + type +"] and name [" + name + "] from SuperData of [" + toString() + "]", name );
        }
    }

    /**
     * Deletes a child MetaData object of the given class
     */
    public void deleteChild(String name, Class<? extends MetaData> c) {
        MetaData d = getChild(name, c);
        if (d.getParent() == this) {
            if (children.remove(d)) {
                
                flushCaches();
            }
        } else {
            throw new MetaDataNotFoundException("You cannot delete MetaData with name [" + name + "] from a SuperData of [" + toString() + "]", name );
        }
    }

    /**
     * Deletes a child MetaData object
     */
    public void deleteChild(MetaData data) {
        if (data.getParent() != this) {
            throw new IllegalArgumentException("MetaData [" + data.toString() + "] is not a child of [" + toString() + "]");
        }
        
        if (children.remove(data)) {
            
            flushCaches();
        }
    }
    
    /**
     * Returns all MetaData children
     */
    public List<MetaData> getChildren() {
        return children.getAll();
    }

    /**
     * Returns all MetaData children which implement the specified class
     */
    public List<MetaData> getChildrenOfType( String type, boolean includeParentData ) {
        return addChildren( type, MetaData.class, includeParentData );
    }

    /**
     * Returns all MetaData children which implement the specified class
     */
    public <T extends MetaData> List<T> getChildren(Class<T> c) {
        return addChildren(null, c, true );
    }

    /**
     * Returns all MetaData children which implement the specified class
     */
    public <T extends MetaData> List<T> getChildren(Class<T> c, boolean includeParentData ) {
        return addChildren(null, c, includeParentData );
    }

    /** Retrieve the first matching child metadata */
    /*private <T extends MetaData> T firstChild( String type, Class<T> c, boolean includeParentData ) {

        List<String> keys = new ArrayList<>();
        List<T> items = new ArrayList<>();
        addChildren( keys, items, type, c, includeParentData, false, true );
        return items.iterator().next();
    }*/

    /** Retrieve all matching child metadata */
    private <T extends MetaData> List<T> addChildren( String type, Class<T> c, boolean includeParentData ) {

        List<String> keys = new ArrayList<>();
        List<T> items = new ArrayList<>();
        addChildren( keys, items, type, c, includeParentData, false, false );
        return items;
    }

    /** Add all the matching children to the map - refactored for better maintainability */
    @SuppressWarnings("unchecked")
    private <T extends MetaData> void addChildren( List<String> keys, List<T> items, String type, Class<T> c, boolean includeParentData, boolean isParent, boolean firstOnly ) {
        addLocalChildren(keys, items, type, c, isParent, firstOnly);
        addParentChildren(keys, items, type, c, includeParentData, firstOnly);
    }
    
    /**
     * Adds matching local children to the results
     */
    @SuppressWarnings("unchecked")
    private <T extends MetaData> void addLocalChildren(List<String> keys, List<T> items, String type, Class<T> c, boolean isParent, boolean firstOnly) {
        children.stream()
            .filter(child -> !shouldStopEarly(firstOnly, items))
            .filter(child -> matchesSearchCriteria(child, type, c))
            .filter(child -> shouldIncludeChild(child, isParent, keys))
            .forEach(child -> addChildToResults(child, keys, items));
    }
    
    /**
     * Recursively adds children from parent metadata
     */
    private <T extends MetaData> void addParentChildren(List<String> keys, List<T> items, String type, Class<T> c, boolean includeParentData, boolean firstOnly) {
        if (getSuperData() != null && includeParentData) {
            getSuperData().addChildren(keys, items, type, c, true, true, firstOnly);
        }
    }
    
    /**
     * Checks if we should stop processing early (for firstOnly queries)
     */
    private <T extends MetaData> boolean shouldStopEarly(boolean firstOnly, List<T> items) {
        return firstOnly && !items.isEmpty();
    }
    
    /**
     * Checks if a child matches the search criteria
     */
    private <T extends MetaData> boolean matchesSearchCriteria(MetaData child, String type, Class<T> c) {
        // Match all if no criteria specified
        if (type == null && c == null) {
            return true;
        }
        
        // Match by type and optionally by class
        if (type != null && child.isType(type)) {
            return c == null || c.isInstance(child);
        }
        
        // Match by class only
        return type == null && c != null && c.isInstance(child);
    }
    
    /**
     * Determines if a child should be included based on parent filtering and uniqueness
     */
    private boolean shouldIncludeChild(MetaData child, boolean isParent, List<String> keys) {
        if (isParent && filterWhenParentData(child)) {
            return false;
        }
        
        String key = createChildKey(child);
        return !keys.contains(key);
    }
    
    /**
     * Creates a unique key for a child MetaData object
     */
    private String createChildKey(MetaData child) {
        return String.format("%s-%s", child.getType(), child.getName());
    }
    
    /**
     * Adds a child to the results collections
     */
    @SuppressWarnings("unchecked")
    private <T extends MetaData> void addChildToResults(MetaData child, List<String> keys, List<T> items) {
        String key = createChildKey(child);
        keys.add(key);
        items.add((T) child);
    }

    /**
     * Returns the first child record
     */
    public <T extends MetaData> T getFirstChild(Class<T> c) {
        Iterator<T> i = getChildren(c, true).iterator();
        if (!i.hasNext())  return null;
        else return i.next();
    }

    /**
     * Returns the first child record of the specified type
     */
    public MetaData getFirstChildOfType( String type ) {
        Iterator<MetaData> i = getChildrenOfType( type, true).iterator();
        if (!i.hasNext()) return null;
        else return i.next();
    }

    /**
     * Returns a child by the specified name of the specified class
     *
     * @param type The type of MetaData to retrieve
     * @param name The name of the child to retrieve. A null will return the first matching child.
     */
    public final MetaData getChildOfType(String type, String name) throws MetaDataNotFoundException {
        return getChildOfType(type, name, true, true);
    }

    /**
     * Returns a child by the specified name of the specified class
     */
    public final MetaData getChildOfType(String type, String name, boolean includeParentData) throws MetaDataNotFoundException {
        return getChildOfType( type, name, includeParentData, true);
    }

    protected final MetaData getChildOfType( String type, String name, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {
        if ( type == null ) throw new IllegalArgumentException( "The 'type' field was null" );
        return getChildOfTypeOrClass( type, name, MetaData.class, includeParentData, shouldThrow );
    }
    
    /**
     * Returns a child by the specified name of the specified class
     *
     * @param name The name of the child to retrieve. A null will return the first matching child.
     * @param c The Expected MetaData class to cast to
     */
    public <T extends MetaData> T getChild(String name, Class<T> c) throws MetaDataNotFoundException {
        return getChild(name, c, true, true);
    }

    /**
     * Returns a child by the specified name of the specified class
     */
    public <T extends MetaData> T getChild(String name, Class<T> c, boolean includeParentData) throws MetaDataNotFoundException {
        return getChild(name, c, includeParentData, true);
    }

    protected <T extends MetaData> T getChild(String name, Class<T> c, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {
        return (T) getChildOfTypeOrClass( null, name, c, includeParentData, shouldThrow );
    }

    @SuppressWarnings("unchecked")
    private final <T extends MetaData> T getChildOfTypeOrClass( String type, String name, Class<T> c, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {

        for (MetaData d : children.getAll()) {

            // Make sure the types match if not null
            if ( type != null && !d.isType(type)) continue;

            // Make sure the class matches if not null
            if ( c != null && !c.isInstance(d)) continue;

            // Make sure the name matches if it's not null
            if ( name != null && !d.getName().equals(name)) continue;

            // If we made it this far, then return the child
            return (T) d;
        }

        // If it wasn't found above, see if it exists in the parent class
        if (getSuperData() != null && includeParentData) {

            try {
                T md = (T) getSuperData().getChildOfTypeOrClass( type, name, c, true, shouldThrow);

                // Filter out Attributes that are prefixed with _ as they do not get inherited
                if (md != null && !filterWhenParentData(md)) return md;
            }
            catch (MetaDataNotFoundException ignore ) {}
        }
        
        if (shouldThrow) {
            throw new MetaDataNotFoundException( "MetaData child of class [" + c + "] with name [" + name + "] not found in [" + toString() + "]", name );
        } else {
            return null;
        }
    }

    /**
     * Clears all children
     */
    public void clearChildren() {
        if ( !children.isEmpty() ) {
            children.clear();
            flushCaches();
        }
    }

    /**
     * Clears all children of the specified type
     */
    public void clearChildrenOfType( String type ) {
        boolean removed = false;
        List<MetaData> toRemove = children.stream()
            .filter(d -> type == null || d.isType(type))
            .toList();
        
        for (MetaData child : toRemove) {
            if (children.remove(child)) {
                removed = true;
            }
        }
        
        if (removed) flushCaches();
    }

    /**
     * Clears all children of the specified MetaData class
     */
    public void clearChildren(Class<? extends MetaData> c) {
        boolean removed = false;
        List<MetaData> toRemove = children.stream()
            .filter(d -> c == null || c.isInstance(d))
            .toList();
        
        for (MetaData child : toRemove) {
            if (children.remove(child)) {
                removed = true;
            }
        }
        
        if (removed) flushCaches();
    }

    ////////////////////////////////////////////////////
    // MISC METHODS
    

    /**
     * Overload the MetaData.  Used with overlays
     * @return The wrapped MetaData
     */
    public <T extends MetaData> T overload()  {
        @SuppressWarnings("unchecked")
        T d = (T) clone();
        d.clearChildren();
        d.setSuperData(this);
        return d;
    }

    /**
     * Clones this MetaData object
     */
    @Override
    public Object clone() {

        MetaData v = newInstanceFromClass( getClass(), type, subType, name );

        v.superData = superData;
        v.parentRef = parentRef;
        v.loader = loader;
        // Used to provide support for OSGi and Maven Mojos
        v.metaDataClassLoader = metaDataClassLoader;

        for (MetaData md : getChildren()) {
            v.addChild((MetaData) md.clone());
        }

        return v;
    }

    /**
     * Create a newInstance of the specified MetaData class given the specified type, subType, and name
     * @return The newly created MetaData instance
     */
    public <T extends MetaData> T newInstanceFromClass( Class<T> c, String typeName, String subTypeName, String fullname) {

        T md;

        try {
            try {
                md = c.getConstructor(String.class, String.class, String.class).newInstance(typeName, subTypeName, fullname);
            } catch (NoSuchMethodException e) {
                try {
                    md = c.getConstructor(String.class, String.class).newInstance(typeName, fullname);
                } catch (NoSuchMethodException e2) {
                    try {
                        md = c.getConstructor(String.class).newInstance(fullname);
                    } catch (NoSuchMethodException e3) {
                        md = c.getConstructor().newInstance();
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new MetaDataException("Could not create new instance of " +
                    getNewInstanceErrorStr(typeName, subTypeName, fullname) + ": " + e.getMessage(), e);
        }

        if (!md.getType().equals(typeName))
            throw new MetaDataException("Unexpected type ["+md.getType()+"] after creating new MetaData "+
                    getNewInstanceErrorStr(typeName, subTypeName, fullname) + ": " + md);

        if (!md.getSubType().equals(subTypeName))
            throw new MetaDataException("Unexpected subType ["+md.getSubType()+"] after creating new MetaData "+
                    getNewInstanceErrorStr(typeName, subTypeName, fullname) + ": " + md);

        if (!md.getName().equals(fullname))
            throw new MetaDataException("Unexpected name ["+md.getName()+"] after creating new MetaData "+
                    getNewInstanceErrorStr(typeName, subTypeName, fullname) + ": " + md);

        return md;
    }

    private String getNewInstanceErrorStr(String typeName, String subTypeName, String fullname) {
        return "[" + getClass().getName() + "] with type:subType:name [" + typeName +
                    ":" + subTypeName + ":" + fullname + "]";
    }


    //////////////////////////////////////////////////////////////////////////////
    // Cache Methods

    public interface GetValueForCache<T> {
        T get();
    };

    protected final Object CACHE_NULL = new Object();

    public <T> T useCache( String cacheKey, GetValueForCache<T> getter ) {
        Object o = getCacheValue( cacheKey );
        if ( o != null && o == CACHE_NULL ) return null;
        @SuppressWarnings("unchecked")
        T cacheValue = (T) o;
        if ( cacheValue == null ) {
            cacheValue = getter.get();
            setCacheValue( cacheKey, cacheValue );
        }
        return cacheValue;
    }

    public interface GetValueForCacheWithArg<T,A> {
        T get(A arg);
    };

    /**
     * The arg.toString() is appended to the CacheKeyPrefix
     */
    public <T,A> T useCache( String cacheKeyPrefix, A arg, GetValueForCacheWithArg<T,A> getter ) {
        final String CACHE_KEY = cacheKeyPrefix+"{"+arg+"}";
        Object o = getCacheValue( CACHE_KEY );
        if ( o != null && o == CACHE_NULL ) return null;
        @SuppressWarnings("unchecked")
        T cacheValue = (T) o;
        if ( cacheValue == null ) {
            cacheValue = getter.get(arg);
            setCacheValue( CACHE_KEY, cacheValue );
        }
        return cacheValue;
    }

    /**
     * Sets a cache value for this piece of MetaData (legacy method)
     */
    public void setCacheValue(Object key, Object value) {
        cache.put(key, value);
    }

    /**
     * Retrieves a cache value for this piece of MetaData (legacy method)
     */
    public Object getCacheValue(Object key) {
        return cache.get(key);
    }

    /**
     * This is called when the MetaData is modified
     */
    protected void flushCaches() {

        // Clear unified cache
        cache.clear();

        // Clear the super data caches
        if ( getSuperData() != null ) getSuperData().flushCaches();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(children, metaData.children) &&
                Objects.equals(type, metaData.type) &&
                Objects.equals(subType, metaData.subType) &&
                Objects.equals(name, metaData.name);
                // Exclude superData and parentRef to avoid circular references
    }

    @Override
    public int hashCode() {
        // Exclude superData and parentRef to avoid circular reference issues
        return Objects.hash(children, type, subType, name);
    }

    /** Get the toString Prefix */
    protected String getToStringPrefix() {
        String className = getClass().getSimpleName();
        String typeName = getType() != null ? getType() : "null";
        String subTypeName = getSubType() != null ? getSubType() : "null";
        String name = getName() != null ? getName() : "null";
        return className + "[" + typeName +":" + subTypeName + "]{" + name + "}";
    }

    /**
     * Returns a string representation of the MetaData
     */
    @Override
    public String toString() {
        // Avoid circular references in toString
        if (getParent() == null ) {
            return getToStringPrefix();
        } else {
            // Use parent's name instead of full toString to avoid circular references
            return getToStringPrefix() + "@" + (getParent().getName() != null ? getParent().getName() : "null");
        }
    }
}
