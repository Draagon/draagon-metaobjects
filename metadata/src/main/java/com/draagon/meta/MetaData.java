/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta;

import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetaData<N extends MetaData> implements Cloneable, Serializable {

    public final static String PKG_SEPARATOR = "::";

    /**
     * Separator for package and class names
     * @deprecated Use PKG_SEPARATOR
     */
    public final static String SEPARATOR = PKG_SEPARATOR;

    private final Map<Object, Object> cacheValues = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
    private final CopyOnWriteArrayList<MetaData> children = new CopyOnWriteArrayList<>();

    private final String type;
    private final String subType;
    private final String name;

    private final String shortName;
    private final String pkg;

    private MetaData superData = null;
    // TODO:  Is this meant to be a weak reference for MetaDataLoader only...?
    private WeakReference<MetaData> parentRef = null;
    private MetaDataLoader loader = null;

    /**
     * Constructs the MetaData
     */
    public MetaData(String type, String subType, String name ) {

        if ( type == null ) throw new NullPointerException( "MetaData Type cannot be null" );
        if ( subType == null ) throw new NullPointerException( "MetaData SubType cannot be null" );
        if ( name == null ) throw new NullPointerException( "MetaData Name cannot be null" );

        this.type = type;
        this.subType = subType;
        this.name = name;

        // Cache the shortName and packageName
        int i = name.lastIndexOf(PKG_SEPARATOR);
        if (i >= 0) {
            shortName = name.substring(i + PKG_SEPARATOR.length());
            pkg = name.substring(0, i);
        } else {
            shortName = name;
            pkg = "";
        }
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

    ////////////////////////////////////////////////////
    // SETTER / GETTER METHODS

    /**
     * Get the Base Class for the MetaData
     * @return Class The Java class for the metadata
     * @deprecated Use getTypeName and getSubTypeName for querying child records
     */
    public Class<? extends MetaData> getMetaDataClass() {
        return MetaData.class;
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
    public MetaData getSuperData() {
        return superData;
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
     * @deprecated Use addMetaAttr(attr)
     */
    public void addAttribute(MetaAttribute attr) {
        addMetaAttr(attr);
    }

    /**
     * Sets an attribute of the MetaClass
     * @deprecated Use deleteAttr(attr)
     */
    public void deleteAttribute(String name) throws MetaAttributeNotFoundException {
        deleteAttr(name);
    }

    /**
     * Sets an attribute of the MetaClass
     */
    public N addMetaAttr(MetaAttribute attr) {
        return addChild(attr);
    }

    /**
     * Sets an attribute of the MetaClass
     */
    public void deleteAttr(String name) throws MetaAttributeNotFoundException {
        try {
            deleteChild(name, MetaAttribute.class);
        } catch (MetaDataException e) {
            throw new MetaAttributeNotFoundException("MetaAtribute [" + name + "] not found in [" + toString() + "]", name);
        }
    }

    /**
     * Sets an attribute value of the MetaData
     *
     * @deprecated Use getAttribute(name).setValue*(value)
     */
    public void setAttribute(String name, Object value) {
        MetaAttribute<?> ma = null;
        try {
            ma = (MetaAttribute<?>) getChild(name, MetaAttribute.class);
        } catch (MetaDataNotFoundException e) {
            throw new MetaAttributeNotFoundException("MetaAttribute [" + name + "] was not found in [" + toString() + "]", name);
        }
        
        ma.setValueAsObject(value);
    }

    /**
     * Retrieves an attribute value of the MetaData
     * @deprecated Use getAttr(name).getValueAsString()
     */
    public String getAttribute(String name) throws MetaAttributeNotFoundException {
        return getMetaAttr(name,true).getValueAsString();
    }

    /**
     * Retrieves an attribute value of the MetaData
     * @deprecated Use getAttr(name, includeParentData).getValueAsString()
     */
    public Object getAttribute(String name, boolean includeParentData) throws MetaAttributeNotFoundException {
        return getMetaAttr(name,includeParentData).getValueAsString();
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
     * @deprecated Use hasAttr(name)
     */
    public boolean hasAttribute(String name) {
        return hasMetaAttr(name,true);
    }

    /**
     * Retrieves all attribute names
     * @deprecated Use hasAttr(name,includeParentData)
     */
    public boolean hasAttribute(String name, boolean includeParentData) {
        return hasMetaAttr( name, includeParentData );
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
     * @deprecated Use getMetaAttrs()
     */
    public List<MetaAttribute> getAttributes() {
        return getMetaAttrs(true);
    }

    /**
     * Retrieves all attribute names
     * @deprecated Use getMetaAttrs(includeParentData)
     */
    public List<MetaAttribute> getAttributes( boolean includeParentData ) {
        return getMetaAttrs( includeParentData );
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
    public N addChild(MetaData data) throws InvalidMetaDataException {
        addChild(data, true);
        return (N) this;
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
                        throw new InvalidMetaDataException("MetaData [" + data.toString() + "] with name [" + data.getName() + "] already exists in [" + toString() + "] as [" + d + "]");
                    }
                }
            } catch (MetaDataNotFoundException ignored) {
            }
        }
        
        data.attachParent(this);
        children.add(data);
        flushCaches();
    }

    /**
     * Deletes a child MetaData object of the given class
     */
    public void deleteChildOfType(String type, String name ) {
        MetaData d = getChildOfType(type, name);
        if (d.getParent() == this) {
            children.remove(d);
            flushCaches();
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
            children.remove(d);
            flushCaches();
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
        
        children.remove(data);
        flushCaches();
    }
    
    /**
     * Returns all MetaData children
     */
    public List<MetaData> getChildren() {
        return getChildren(null, true);
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
        children.forEach( d -> {

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

        for (MetaData d : children) {

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
        if ( children.removeIf(d -> type == null || d.isType( type )))
            flushCaches();
    }

    /**
     * Clears all children of the specified MetaData class
     */
    public void clearChildren(Class<? extends MetaData> c) {
        if (children.removeIf(d -> c == null || c.isInstance(d)))
            flushCaches();
    }

    ////////////////////////////////////////////////////
    // DEPRECATED CHILDREN METHODS

    /**
     * @deprecated Only exists for deprecated support
     */
    private String getTypeForClass( Class<?> c ) {
        switch( c.getSimpleName() ) {
            case "MetaAttribute": return MetaAttribute.TYPE_ATTR;
            case "MetaField": return MetaField.TYPE_FIELD;
            case "MetaObject": return MetaObject.TYPE_OBJECT;
            case "MetaView": return MetaView.TYPE_VIEW;
            case "MetaValidator": return MetaValidator.TYPE_VALIDATOR;
            default: throw new IllegalStateException( "These deprecated methods only support MetaAttribute, MetaField, MetaObject, MetaView, and MetaValidator, not ["+c.getSimpleName() + "]");
        }
    }

    ////////////////////////////////////////////////////


    ////////////////////////////////////////////////////
    // MISC METHODS
    
    /**
     * Validates the state of the data in the MetaData object
     */
    public void validate() {

        // Validate the children
        getChildren().forEach( d -> d.validate() );
    }

    /**
     * Overload the MetaData.  Used with overlays
     * @return The wrapped MetaData
     */
    public N overload()  {
        N d = (N) clone();
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

        for (MetaData md : getChildren()) {
            v.addChild((MetaData) md.clone());
        }

        return v;
    }

    /**
     * Create a newInstance of the specified MetaData class given the specified type, subType, and name
     * @return The newly created MetaData instance
     */
    public MetaData newInstanceFromClass( Class<? extends MetaData> c, String typeName, String subTypeName, String fullname) {

        MetaData md;

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
                        try {
                            md = c.getConstructor().newInstance();
                        } catch (NoSuchMethodException e4) {
                            throw new RuntimeException("Could not create new instance of MetaData class [" + getClass() + "], no valid constructor was found" );
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of MetaData class [" + getClass() + "]: " + e.getMessage(), e);
        }

        if (!md.getTypeName().equals(typeName))
            throw new MetaDataException("Expected MetaData type [" + typeName + "], but MetaData instantiated was of type [" + md.getTypeName() + "]: " + md);

        if (!md.getSubTypeName().equals(subTypeName))
            throw new MetaDataException("Expected MetaData subType [" + subTypeName + "], but MetaData instantiated was of subType [" + md.getSubTypeName() + "]: " + md);

        if (!md.getName().equals(fullname))
            throw new MetaDataException("Expected MetaData name [" + fullname + "], but MetaData instantiated was of name [" + md.getName() + "]: " + md);

        return md;
    }

    /**
     * This is called when the MetaData is modified
     */
    protected void flushCaches() {

        // Clear the local cache
        cacheValues.clear();

        // Clear the super data caches
        if ( getSuperData() != null ) getSuperData().flushCaches();
    }

    /**
     * Sets a cache value for this piece of MetaData
     */
    public void setCacheValue(Object key, Object value) {
        //ystem.out.println( "SET [" + key + "] = " + value );
        cacheValues.put(key, value);
    }

    /**
     * Retrieves a cache value for this piece of MetaData
     */
    public Object getCacheValue(Object key) {
        return cacheValues.get(key);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData<?> metaData = (MetaData<?>) o;
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

        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            name = name.substring(i + 1);
        }

        return name + "[" + getTypeName() +":" + getSubTypeName() + "]{" + getName() + "}";
    }

    /**
     * Returns a string representation of the MetaData
     */
    /*private String toInternalString() {

        if (getParent() == null) {
            return getToStringPrefix();
        } else {
            return getToStringPrefix() + "@" + getParent().toInternalString();
        }
    }*/

    /**
     * Returns a string representation of the MetaData
     */
    @Override
    public String toString() {

        if (getParent() == null) {
            return getToStringPrefix();
        } else {
            return getToStringPrefix() + "@" + getParent().toString();
        }
    }
}
