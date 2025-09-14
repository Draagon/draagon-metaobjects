package com.draagon.meta;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.type.MetaDataTypeDefinition;
import com.draagon.meta.type.MetaDataTypeRegistry;
import com.draagon.meta.cache.CacheStrategy;
import com.draagon.meta.cache.HybridCache;
import com.draagon.meta.collections.IndexedMetaDataCollection;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.MetaDataValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MetaData implements Cloneable, Serializable {

    private static final Logger log = LoggerFactory.getLogger(MetaData.class);

    public final static String PKG_SEPARATOR = "::";
    public final static String SEPARATOR = PKG_SEPARATOR;

    // Unified caching strategy
    private final CacheStrategy cache = new HybridCache();
    
    // Indexed collection for O(1) child lookups
    private final IndexedMetaDataCollection children = new IndexedMetaDataCollection();
    
    
    
    // Validation chain
    private volatile ValidationChain<MetaData> validationChain;

    private final String type;
    private final String subType;
    private final String name;

    private final String shortName;
    private final String pkg;
    
    // Type system integration
    private volatile MetaDataTypeDefinition typeDefinition;

    private MetaData overloadedMetaData = null;
    private MetaData superData = null;

    // TODO:  Is this meant to be a weak reference for MetaDataLoader only...?
    private WeakReference<MetaData> parentRef = null;
    private MetaDataLoader loader = null;
    private ClassLoader metaDataClassLoader=null;

    /**
     * Constructs the MetaData with enhanced type system integration
     */
    public MetaData(String type, String subType, String name ) {

        if ( type == null ) throw new NullPointerException( "MetaData Type cannot be null" );
        if ( subType == null ) throw new NullPointerException( "MetaData SubType cannot be null" );
        if ( name == null ) throw new NullPointerException( "MetaData Name cannot be null" );

        this.type = type;
        this.subType = subType;
        this.name = name;

        // Initialize type definition (lazy loading to avoid circular dependencies)
        this.typeDefinition = null;
        

        // Cache the shortName and packageName
        int i = name.lastIndexOf(PKG_SEPARATOR);
        if (i >= 0) {
            shortName = name.substring(i + PKG_SEPARATOR.length());
            pkg = name.substring(0, i);
        } else {
            shortName = name;
            pkg = "";
        }

        log.debug("Created MetaData: {}:{}:{}", type, subType, name);
    }

    // ========== ENHANCED TYPE SYSTEM METHODS ==========

    /**
     * Get the type definition for this MetaData (modern approach)
     */
    public Optional<MetaDataTypeDefinition> getTypeDefinition() {
        if (typeDefinition == null) {
            synchronized (this) {
                if (typeDefinition == null) {
                    try {
                        typeDefinition = MetaDataTypeRegistry.getInstance()
                            .getType(type)
                            .orElse(null);
                    } catch (Exception e) {
                        log.debug("Could not load type definition for {}: {}", type, e.getMessage());
                    }
                }
            }
        }
        return Optional.ofNullable(typeDefinition);
    }

    /**
     * Check if this MetaData type is registered in the type system
     */
    public boolean hasRegisteredType() {
        return getTypeDefinition().isPresent();
    }

    /**
     * Validate this MetaData using the enhanced validation framework
     */
    public ValidationResult validateEnhanced() {
        Instant start = Instant.now();
        
        try {
            ValidationResult result = getValidationChain().validate(this);
            
            
            return result;
        } catch (Exception e) {
            
            log.error("Validation failed for {}: {}", getName(), e.getMessage(), e);
            
            return ValidationResult.builder()
                .addError("Validation failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Get the validation chain (lazy initialization)
     */
    private ValidationChain getValidationChain() {
        if (validationChain == null) {
            synchronized (this) {
                if (validationChain == null) {
                    validationChain = ValidationChain.<MetaData>builder()
                        .addValidator(MetaDataValidators.typeSystemValidator())
                        .addValidator(MetaDataValidators.childrenValidator())
                        .addValidator(MetaDataValidators.legacyValidator())
                        .build();
                }
            }
        }
        return validationChain;
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
            .orElseThrow(() -> new MetaAttributeNotFoundException(
                "MetaAttribute '" + name + "' not found in '" + toString() + "'", name));
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
     * Returns the Type of this piece of MetaData
     */
    public String getTypeName() {
        return type;
    }

    /**
     * Returns whether MetaData is of the specified Type
     */
    public boolean isType( String type ) {
        return this.type.equals( type );
    }

    /**
     * Returns the SubType of this piece of MetaData
     */
    public String getSubTypeName() {
        return subType;
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
     * Returns the Name of this piece of MetaData
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
    public <T> Class<T> loadClass( Class<T> clazz, String name ) throws ClassNotFoundException {
        try {
            Class c = getMetaDataClassLoader().loadClass(name);
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
    public Class loadClass( String name ) throws ClassNotFoundException {
        return loadClass(name, true);
    }

        // Loads the specified Class using the proper ClassLoader
    public Class loadClass( String name, boolean throwError ) throws ClassNotFoundException {
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
     * Get the Base Class for the MetaData
     * @return Class The Java class for the metadata
     */
    public <T extends MetaData> Class<T> getMetaDataClass() {
        return (Class<T>) MetaData.class;
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
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T addMetaAttr(MetaAttribute attr) {
        addChild(attr);
        return (T) this;
    }

    /**
     * Sets an attribute of the MetaClass and returns this MetaData (type-safe version)
     * @param attr The attribute to add
     * @return This MetaData instance for method chaining
     */
    public MetaData addMetaAttrSafe(MetaAttribute attr) {
        addChild(attr);
        return this;
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getMetaAttr(String name) throws MetaAttributeNotFoundException {
        return getMetaAttr(name,true);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getMetaAttr(String name, boolean includeParentData) throws MetaAttributeNotFoundException {
        try {
            return (MetaAttribute) getChild( name, MetaAttribute.class, includeParentData);
        } catch (MetaDataNotFoundException e) {
            throw new MetaAttributeNotFoundException( "MetaAtribute [" + name + "] not found in [" + toString() + "]", name );
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
    @SuppressWarnings("unchecked")
    public <T extends MetaData> T addChild(MetaData data) throws InvalidMetaDataException {
        addChild(data, true);
        return (T) this;
    }

    /**
     * Adds a child MetaData object and returns this MetaData (type-safe version)
     * @param data The child MetaData to add
     * @return This MetaData instance for method chaining
     */
    public MetaData addChildSafe(MetaData data) throws InvalidMetaDataException {
        addChild(data, true);
        return this;
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
        if ( this.getTypeName().equals( data.getTypeName())) {
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
                MetaData d = getChildOfType( data.getTypeName(), data.getName() );
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
    private <T extends MetaData> T firstChild( String type, Class<T> c, boolean includeParentData ) {

        List<String> keys = new ArrayList<>();
        List<T> items = new ArrayList<>();
        addChildren( keys, items, type, c, includeParentData, false, true );
        return items.iterator().next();
    }

    /** Retrieve all matching child metadata */
    private <T extends MetaData> List<T> addChildren( String type, Class<T> c, boolean includeParentData ) {

        List<String> keys = new ArrayList<>();
        List<T> items = new ArrayList<>();
        addChildren( keys, items, type, c, includeParentData, false, false );
        return items;
    }

    /** Add all the matching children to the map */
    private <T extends MetaData> void addChildren( List<String> keys, List<T> items, String type, Class<T> c, boolean includeParentData, boolean isParent, boolean firstOnly ) {

        // Get all the local children
        children.stream().forEach( d -> {

            // If only getting the first one, then exit
            if ( firstOnly && items.size() > 0 ) return;

            // TODO: Use Stream and filters
            // Filter on the search criteria
            if ((type == null && c == null )
                    || ( type != null && d.isType(type) && ( c==null || c.isInstance(d)))
                    || ( type == null && c.isInstance(d))) {

                // TODO:  Make the key part of the MetaData class
                String key = new StringBuilder( d.getTypeName())
                        //.append('-').append( d.getSubTypeName() )
                        .append('-').append( d.getName() ).toString();

                // TODO: Add part of stream filters
                // If this is a parent, then filter; only add if it didn't already exist
                if ( (!isParent || !filterWhenParentData( d ))
                        && !keys.contains( key )) {

                    keys.add( key );
                    items.add( (T) d);
                }
            }
        });

        // Recursively add the super metadata's children
        if (getSuperData() != null && includeParentData) {
            getSuperData().addChildren( keys, items, type, c, true, true, firstOnly );
        }
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
     * Validates the state of the data in the MetaData object
     */
    public void validate() throws InvalidMetaDataException {
        // Validate the children
        getChildren().forEach( d -> d.validate() );
    }

    /**
     * Overload the MetaData.  Used with overlays
     * @return The wrapped MetaData
     */
    public <T extends MetaData> T overload()  {
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

        if (!md.getTypeName().equals(typeName))
            throw new MetaDataException("Unexpected type ["+md.getTypeName()+"] after creating new MetaData "+
                    getNewInstanceErrorStr(typeName, subTypeName, fullname) + ": " + md);

        if (!md.getSubTypeName().equals(subTypeName))
            throw new MetaDataException("Unexpected subType ["+md.getSubTypeName()+"] after creating new MetaData "+
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
                type.equals(metaData.type) &&
                subType.equals(metaData.subType) &&
                name.equals(metaData.name) &&
                Objects.equals(superData, metaData.superData) &&
                Objects.equals(parentRef, metaData.parentRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, type, subType, name, superData, parentRef);
    }

    /** Get the toString Prefix */
    protected String getToStringPrefix() {
        String name = getClass().getSimpleName();
        return name + "[" + getTypeName() +":" + getSubTypeName() + "]{" + getName() + "}";
    }

    /**
     * Returns a string representation of the MetaData
     */
    @Override
    public String toString() {

        if (getParent() == null ) {
            return getToStringPrefix();
        } else {
            return getToStringPrefix() + "@" + getParent().toString();
        }
    }
}
