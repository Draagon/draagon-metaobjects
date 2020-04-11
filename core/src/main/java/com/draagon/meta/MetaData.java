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
import java.util.stream.Collectors;

public class MetaData implements Cloneable, Serializable {

    public final static String PKG_SEPARATOR = "::";

    /**
     * Separator for package and class names
     * @deprecated Use PKG_SEPARATOR
     */
    public final static String SEPARATOR = PKG_SEPARATOR;

    private final Map<Object, Object> cacheValues = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
    private final CopyOnWriteArrayList<MetaData> children = new CopyOnWriteArrayList<MetaData>();

    private final String type;
    private final String subType;
    private final String name;

    private MetaData superData = null;
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
    }

    /**
     * Returns the Type of this piece of MetaData
     */
    public String getType() {
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
    public String getSubType() {
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
     * @deprecated Use getType and getSubType for querying child records
     */
    public Class<? extends MetaData> getMetaDataClass() {
        return MetaData.class;
    }

    /**
     * Iterates up the Super Data until it finds the MetaDataLoader
     */
    public synchronized MetaDataLoader getLoader() {

        if (loader == null) {
            MetaData d = this;
            while (d != null) {
                if (d instanceof MetaDataLoader) {
                    loader = (MetaDataLoader) d;
                    break;
                }
                d = d.getParent();
            }
        }

        return loader;
    }

    /**
     * Retrieve the MetaObject package
     */
    public String getPackage() {
        // TODO:  Add caching
        String name = getName();
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf(SEPARATOR);
        if (i >= 0) {
            return name.substring(0, i);
        }
        return "";
    }

    /**
     * Retrieve the MetaObject package
     */
    public String getShortName() {
        // TODO:  Add caching
        String name = getName();
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf(SEPARATOR);
        if (i >= 0) {
            return name.substring(i + SEPARATOR.length());
        }
        return name;
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

    ////////////////////////////////////////////////////
    // ATTRIBUTE METHODS

    /**
     * Sets an attribute of the MetaClass
     * @deprecated Use addAttr(attr)
     */
    public void addAttribute(MetaAttribute attr) {
        addAttr(attr);
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
    public void addAttr(MetaAttribute attr) {
        addChild(attr);
    }

    /**
     * Sets an attribute of the MetaClass
     */
    public void deleteAttr(String name) throws MetaAttributeNotFoundException {
        try {
            deleteChild(name, MetaAttribute.class);
        } catch (MetaException e) {
            throw new MetaAttributeNotFoundException("MetaAtribute [" + name + "] not found in [" + toInternalString() + "]", name);
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
            throw new MetaAttributeNotFoundException("MetaAttribute [" + name + "] was not found in [" + toInternalString() + "]", name);
        }
        
        ma.setValueAsObject(value);
    }

    /**
     * Retrieves an attribute value of the MetaData
     * @deprecated Use getAttr(name).getValueAsString()
     */
    public String getAttribute(String name) throws MetaAttributeNotFoundException {
        return getAttr(name,true).getValueAsString();
    }

    /**
     * Retrieves an attribute value of the MetaData
     * @deprecated Use getAttr(name, includeParentData).getValueAsString()
     */
    public Object getAttribute(String name, boolean includeParentData) throws MetaAttributeNotFoundException {
        return getAttr(name,true).getValueAsString();
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getAttr(String name) throws MetaAttributeNotFoundException {
        return getAttr(name,true);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public MetaAttribute getAttr(String name, boolean includeParentData) throws MetaAttributeNotFoundException {
        try {
            return (MetaAttribute) getChild( name, MetaAttribute.class, includeParentData);
        } catch (MetaDataNotFoundException e) {
            throw new MetaAttributeNotFoundException( "MetaAtribute [" + name + "] not found in [" + toInternalString() + "]", name );
        }
    }

    /**
     * Retrieves all attribute names
     * @deprecated Use hasAttr(name)
     */
    public boolean hasAttribute(String name) {
        return hasAttr(name,true);
    }

    /**
     * Retrieves all attribute names
     * @deprecated Use hasAttr(name,includeParentData)
     */
    public boolean hasAttribute(String name, boolean includeParentData) {
        return hasAttr( name, includeParentData );
    }

    /**
     * Retrieves all attribute names
     */
    public boolean hasAttr(String name) {
        return hasAttr(name,true);
    }

    /**
     * Retrieves all attribute names
     */
    public boolean hasAttr(String name, boolean includeParentData) {
        try {
            if (getChild(name, MetaAttribute.class, includeParentData, false) != null) {
                return true;
            }
        } catch (MetaDataNotFoundException ignored) {}
        
        return false;
    }
    /**
     * Retrieves all attribute names
     * @deprecated Use getAttrs()
     */
    public Collection<MetaAttribute> getAttributes() {
        return getAttrs(true);
    }

    /**
     * Retrieves all attribute names
     * @deprecated Use getAttrs(includeParentData)
     */
    public Collection<MetaAttribute> getAttributes( boolean includeParentData ) {
        return getAttrs( includeParentData );
    }

    /**
     * Retrieves all attribute names
     */
    public Collection<MetaAttribute> getAttrs() {
        return getAttrs(true);
    }

    /**
     * Retrieves all attribute names
     */
    public Collection<MetaAttribute> getAttrs( boolean includeParentData ) {

        return getChildren(MetaAttribute.class, includeParentData).stream()
                .map(MetaAttribute.class::cast)
                .collect(Collectors.toList());
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

        return d instanceof MetaAttribute
                // || d instanceof MetaField
                || d instanceof MetaValidator
                || d instanceof MetaView;
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
    protected boolean hasChild(String name, Class<? extends MetaData> c) {
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
     * Adds a child MetaData object of the specified class type. If no class
     * type is set, then a child of the same type is not checked against.
     */
    public void addChild(MetaData data, boolean checkExists)  throws InvalidMetaDataException {
        
        if (data == null) {
            throw new IllegalArgumentException("Cannot add null MetaData");
        }
        
        if (checkExists) {
            try {
                MetaData d = getChildOfType( data.getType(), data.getName() );
                if (d.getParent() == this) {
                    if (deleteOnAdd( d )) {
                        deleteChild(d);
                    } else {
                        throw new InvalidMetaDataException("MetaData [" + data.toInternalString() + "] with name [" + data.getName() + "] already exists in [" + toInternalString() + "] as [" + d + "]");
                    }
                }
            } catch (MetaDataNotFoundException ignored) {
            }
        }
        
        data.attachParent(this);
        children.add(data);
    }

    /**
     * Deletes a child MetaData object of the given class
     */
    public void deleteChildOfType(String type, String name ) {
        MetaData d = getChildOfType(type, name);
        if (d.getParent() == this) {
            children.remove(d);
        } else {
            throw new MetaDataNotFoundException("You cannot delete MetaData with type [" + type +"] and name [" + name + "] from SuperData of [" + toInternalString() + "]", name );
        }
    }

    /**
     * Deletes a child MetaData object of the given class
     */
    public void deleteChild(String name, Class<? extends MetaData> c) {
        MetaData d = getChild(name, c);
        if (d.getParent() == this) {
            children.remove(d);
        } else {
            throw new MetaDataNotFoundException("You cannot delete MetaData with name [" + name + "] from a SuperData of [" + toInternalString() + "]", name );
        }
    }

    /**
     * Deletes a child MetaData object
     */
    public void deleteChild(MetaData data) {
        if (data.getParent() != this) {
            throw new IllegalArgumentException("MetaData [" + data.toInternalString() + "] is not a child of [" + toInternalString() + "]");
        }
        
        children.remove(data);
    }
    
    /**
     * Returns all MetaData children
     */
    protected Collection<MetaData> getChildren() {
        return getChildren(null, true);
    }

    /**
     * Returns all MetaData children which implement the specified class
     */
    protected Collection<MetaData> getChildrenOfType( String type, boolean includeParentData ) {

        List<MetaData> al = children.stream()
                .filter( d -> type == null || d.isType( type ))
                .collect(Collectors.toList());

        return addChildren( al, MetaData.class, includeParentData );
    }

    /**
     * Returns all MetaData children which implement the specified class
     */
    protected <T extends MetaData> Collection<T> getChildren(Class<T> c, boolean includeParentData ) {

        // Get all the local children
        List<T> al = new ArrayList<T>();
        children.forEach( d -> {
            if (c == null || c.isInstance(d)) al.add((T) d );
        });

        return addChildren( al, c, includeParentData );
    }


    protected <T extends MetaData> Collection<T> addChildren( List<T> al, Class<T> c, boolean includeParentData ) {

        // Add the super class's children
        if (getSuperData() != null && includeParentData) {

            for (MetaData d : getSuperData().getChildren(c, true)) {

                // Filter out Attributes that are prefixed with _ as they do not get inherited
                if (!filterWhenParentData( d )) {

                    String n = d.getName();

                    boolean found = false;

                    // Only add the field if it's not found in the super class
                    for (MetaData sd : al) {
                        if (sd.getName().equals(n)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        al.add((T) d);
                    }
                }
            }
        }
        
        return al;
    }
    
    /**
     * Returns the first child record
     */
    protected <T extends MetaData> T getFirstChild(Class<T> c) {
        Iterator<T> i = getChildren(c, true).iterator();
        if (!i.hasNext())  return null;
        else return i.next();
    }

    /**
     * Returns the first child record of the specified type
     */
    protected MetaData getFirstChildOfType( String type ) {
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
    public final <T extends MetaData> T getChild(String name, Class<T> c) throws MetaDataNotFoundException {
        return getChild(name, c, true, true);
    }

    /**
     * Returns a child by the specified name of the specified class
     */
    public final <T extends MetaData> T getChild(String name, Class<T> c, boolean includeParentData) throws MetaDataNotFoundException {
        return getChild(name, c, includeParentData, true);
    }

    protected final <T extends MetaData> T getChild(String name, Class<T> c, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {
        return (T) getChildOfTypeOrClass( null, name, c, includeParentData, shouldThrow );
    }

    private final <T extends MetaData> T getChildOfTypeOrClass( String type, String name, Class<T> c, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {

        for (MetaData d : children) {

            // Make sure the name matches if it's not null
            if ( name != null && !d.getName().equals(name)) continue;

            // Make sure the types match if not null
            if ( type != null && !d.isType(type)) continue;

            // Make sure the class matches if not null
            if ( c != null && !c.isInstance(d)) continue;

            // If we made it this far, then return the child
            return (T) d;
        }

        // If it wasn't found above, see if it exists in the parent class
        if (getSuperData() != null && includeParentData) {

            try {
                T md = getSuperData().getChildOfTypeOrClass( type, name, c, true, shouldThrow);

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
    protected void clearChildren() {
        children.clear();
    }

    /**
     * Clears all children of the specified type
     */
    protected void clearChildrenOfType( String type ) {
        children.removeIf(d -> type == null || d.isType( type ));
    }

    /**
     * Clears all children of the specified MetaData class
     */
    protected void clearChildren(Class<? extends MetaData> c) {
        children.removeIf(d -> c == null || c.isInstance(d));
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
     * Wrap the MetaData.  Used with overlays
     * @return The wrapped MetaData
     */
    public MetaData wrap()  {
        MetaData d = (MetaData) clone();
        d.clearChildren();
        d.setSuperData(this);
        return d;
    }

    /**
     * Clones this MetaData object
     */
    @Override
    public Object clone() {

        try {
            MetaData v = (MetaData) super.clone();

            try {
                try {
                    v = (MetaData) this.getClass().getConstructor(String.class, String.class, String.class).newInstance(type, subType, name);
                } catch( NoSuchMethodException e ) {}
                try {
                    if ( v == null ) v = (MetaData) this.getClass().getConstructor(String.class, String.class).newInstance(subType, name);
                } catch( NoSuchMethodException e ) {}
                try {
                    v = (MetaData) this.getClass().getConstructor(String.class).newInstance(name);
                } catch( NoSuchMethodException e ) {}
                try {
                    v = (MetaData) this.getClass().getConstructor().newInstance();
                } catch( NoSuchMethodException e ) {}

            } catch (Exception e) {
                throw new RuntimeException("Could not create new instance of MetaData class [" + getClass() + "]: " + e.getMessage(), e);
            }

            v.parentRef = parentRef;

            for (MetaData md : getChildren()) {
                v.addChild((MetaData) md.clone());
                //v.mChildren = (ArrayList) mChildren.clone();
            }

            return v;
        }
        catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Could not create new instance of MetaData class [" + getClass() + "]: " + e.getMessage(), e);
        }
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

    protected String getToStringPrefix() {

        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            name = name.substring(i + 1);
        }

        return name + "[" + getType() +":" + getSubType() + "]";
    }

    /**
     * Returns a string representation of the MetaData
     */
    private String toInternalString() {

        if (getParent() == null) {
            return getToStringPrefix() + "{" + getName() + "}";
        } else {
            return getToStringPrefix() + "{" + getName() + "}@" + getParent().toInternalString();
        }
    }

    /**
     * Returns a string representation of the MetaData
     */
    public String toString() {

        if (getParent() == null) {
            return getToStringPrefix() + "{" + getName() + "}";
        } else {
            return getToStringPrefix() + "{" + getName() + "}@" + getParent().toString();
        }
    }
}
