/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta;

import com.draagon.meta.attr.AttributeDef;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MetaData implements Cloneable, Serializable {

    private final Map<Object, Object> cacheValues = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
    private final CopyOnWriteArrayList<MetaData> children = new CopyOnWriteArrayList<MetaData>();
    private final CopyOnWriteArrayList<AttributeDef> attributeDefs = new CopyOnWriteArrayList<AttributeDef>();
    private final String name;

    private MetaData superData = null;
    private WeakReference<MetaData> parentRef = null;

    /**
     * Constructs the MetaData
     */
    public MetaData( String name ) {
        this.name = name;
    }
    
    public abstract Class<? extends MetaData> getMetaDataClass();

    ////////////////////////////////////////////////////
    // SETTER / GETTER METHODS
    
    /**
     * Returns the Name of this piece of MetaData
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the parent of the attribute
     */
    protected void attachParent(MetaData parent) {
        parentRef = new WeakReference<MetaData>(parent);
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
     */
    public void addAttribute(MetaAttribute attr) {
        addChild(attr);
    }

    /**
     * Sets an attribute of the MetaClass
     */
    public void deleteAttribute(String name) throws MetaAttributeNotFoundException {
        try {
            deleteChild(name, MetaAttribute.class);
        } catch (MetaException e) {
            throw new MetaAttributeNotFoundException("MetaAtribute [" + name + "] not found in [" + toInternalString() + "]", name);
        }
    }

    /**
     * Sets an attribute value of the MetaData
     */
    public void setAttribute(String name, Object value) {
        MetaAttribute ma = null;
        try {
            ma = (MetaAttribute) getChild(name, MetaAttribute.class);
        } catch (MetaDataNotFoundException e) {
            throw new MetaAttributeNotFoundException("MetaAttribute [" + name + "] was not found in [" + toInternalString() + "]", name);
        }
        
        ma.setValue(value);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public Object getAttribute(String name)
            throws MetaAttributeNotFoundException {
        return getAttribute(name,true);
    }

    /**
     * Retrieves an attribute value of the MetaData
     */
    public Object getAttribute(String name, boolean includeParentData)
            throws MetaAttributeNotFoundException {
        try {
            MetaAttribute ma = (MetaAttribute) getChild(name, MetaAttribute.class, includeParentData);
            return ma.getValue();
        } catch (MetaDataNotFoundException e) {
            throw new MetaAttributeNotFoundException( "MetaAtribute [" + name + "] not found in [" + toInternalString() + "]", name );
        }
    }

    /**
     * Retrieves all attribute names
     */
    public boolean hasAttribute(String name) {
        return hasAttribute(name,true);
    }

    /**
     * Retrieves all attribute names
     */
    public boolean hasAttribute(String name, boolean includeParentData) {
        try {
            if (getChild(name, MetaAttribute.class, includeParentData, false) != null) {
                return true;
            }
        } catch (MetaDataNotFoundException e) {
        }
        
        return false;
    }


    /**
     * Retrieves all attribute names
     */
    public Collection<MetaAttribute> getAttributes() {
        return getAttributes(true);
    }

    /**
     * Retrieves all attribute names
     */
    public Collection<MetaAttribute> getAttributes( boolean includeParentData ) {
        Collection<MetaAttribute> attrs = new ArrayList<MetaAttribute>();
        for (MetaData md : getChildren(MetaAttribute.class,includeParentData)) {
            attrs.add((MetaAttribute) md);
        }
        return attrs;
    }

    ////////////////////////////////////////////////////
    // CHILDREN METHODS
    
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
    public void addChild(MetaData data)
            throws InvalidMetaDataException {
        addChild(data, true);
    }

    /**
     * Adds a child MetaData object of the specified class type. If no class
     * type is set, then a child of the same type is not checked against.
     */
    public void addChild(MetaData data, boolean checkExists) 
            throws InvalidMetaDataException {
        
        if (data == null) {
            throw new IllegalArgumentException("Cannot add null MetaData");
        }
        
        if (checkExists) {
            try {
                MetaData d = getChild(data.getName(), data.getMetaDataClass());
                if (d.getParent() == this) {
                    if (d instanceof MetaAttribute
                            || d instanceof MetaValidator
                            || d instanceof MetaView /*|| d instanceof MetaField*/) {
                        deleteChild(d);
                    } else {
                        throw new InvalidMetaDataException("MetaData [" + data.toInternalString() + "] with name [" + data.getName() + "] already exists in [" + toInternalString() + "] as [" + d + "]");
                    }
                }
            } catch (MetaDataNotFoundException e) {
            }
        }
        
        data.attachParent(this);
        children.add(data);
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
    protected <T extends MetaData> Collection<T> getChildren(Class<T> c, boolean includeParentData ) {
        
        // Get all the local children
        ArrayList<T> al = new ArrayList<T>();
        for (MetaData d : children) {
            if (c == null || c.isInstance(d)) {
                al.add( (T) d);
            }
        }

        // Add the super class's children
        if (getSuperData() != null && includeParentData) {
            for (MetaData d : getSuperData().getChildren(c, true)) {

                // Filter out Attributes that are prefixed with _ as they do not get inherited
                String n = d.getName();
                if (!( d instanceof MetaAttribute && n != null && n.startsWith("_") )) {

                    // We know it exists in the super class, so get it correctly, which adds
                    // it to this class except for MetaAttributs
                    // if ( tmp instanceof MetaAttribute ) d = tmp;
                    // else d = getChild( tmp.getName(), tmp.getMetaDataClass() );

                    boolean found = false;

                    // Only add the field if it's not found in the super class
                    for (MetaData sd : al) {
                        if (sd.getName().equals(n)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        //if (!( d instanceof MetaAttribute ))
                        //  d = getChild( d.getName(), d.getMetaDataClass() );

                        al.add((T) d); //wrapMetaData( d ));
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
        if (!i.hasNext()) {
            return null;
        }
        return i.next();
    }
    
    /**
     * Returns a child by the specified name of the specified class
     *
     * @param name The name of the child to retrieve. A null will return the
     * first child.
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
        return (T) getChildOfType( name, c, includeParentData, shouldThrow );
    }

    protected final MetaData getChildOfType(String name, Class<? extends MetaData> c, boolean includeParentData, boolean shouldThrow) throws MetaDataNotFoundException {
        
        for (MetaData d : children) {
            if ((name == null || d.getName().equals(name))
                    && (c == null || c.isInstance(d))) {
                return d;
            }
        }

        // See if it exists in the parent class
        if (getSuperData() != null && includeParentData) {
            try {
                MetaData md = getSuperData().getChild(name, c, true, shouldThrow);
                if ( md == null ) return null;

                // Filter out Attributes that are prefixed with _ as they do not get inherited
                String n = md.getName();
                if (!( md instanceof MetaAttribute && n != null && n.startsWith("_") )) {

                    // If the MetaData is not an attribute, then clone, clear,
                    //  and set the super data
                    /*if ( !( md instanceof MetaAttribute ))
                     {
                        MetaData add = (MetaData) md.clone();
                        add.clearChildren();
                        add.setSuperData( md );
                        addChild( add, false );
                        return add;
                     }*/

                    return md; //wrapMetaData( md );
                }// else {
                //    System.out.println( "ATTR: " + md.toString() );
                //}
            } 
            catch (MetaDataNotFoundException ex) {
            }
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
    protected void clearChildren(Class<? extends MetaData> c) {
        for (MetaData d : children) {
            if (c == null || c.isInstance(d)) {
                children.remove(d);
            }
        }
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    /**
     * Returns a list of expected attributes
     */
    public List<AttributeDef> getAttributeDefs() {
        return attributeDefs;
    }

    /**
     * Add the specified attribute definitions
     */
    public void addAttributeDef( AttributeDef def ) {
        attributeDefs.addIfAbsent( def );
    }
    
    /**
     * Validates the state of the data in the MetaData object
     */
    public void validate() {
        
        if (getName() == null) {
            throw new InvalidMetaDataException("MetaData has no name");
        }

        // Validate the Attribute options
        for (AttributeDef ao : getAttributeDefs()) {
            
            boolean has = hasAttribute(ao.getName());

            // Check for required fields
            if (ao.isRequired() && !has) {
                throw new InvalidMetaDataException("Required attribute [" + ao.getName() + "] was not found");
            }

            // Check for proper class types
            if (has && ao.getType() != null) {
                try {
                    Object obj = getAttribute(ao.getName());
                    if (!ao.getType().isInstance(obj)) {
                        throw new InvalidMetaDataException("Attribute [" + ao.getName() + "] was not an instance of class [" + ao.getType() + "]");
                    }
                } catch (MetaAttributeNotFoundException e) {
                }
            }
        }

        // Validate the children
        for (MetaData d : getChildren()) {
            d.validate();
        }
    }
    
    public MetaData wrap() {
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
        
        MetaData v;
        try {
            Constructor<? extends MetaData> c = (Constructor<? extends MetaData>) this.getClass().getConstructor(String.class);
            v = c.newInstance(name);
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

    /**
     * Returns a string representation of the MetaData
     */
    private String toInternalString() {
        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            name = name.substring(i + 1);
        }
        
        if (getParent() == null) {
            return name + "{" + getName() + "}";
        } else {
            return name + "{" + getName() + "}@" + getParent().toInternalString();
        }
    }

    /**
     * Returns a string representation of the MetaData
     */
    public String toString() {
        String name = getClass().getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            name = name.substring(i + 1);
        }
        
        if (getParent() == null) {
            return name + "{" + getName() + "}";
        } else {
            return name + "{" + getName() + "}@" + getParent().toString();
        }
    }
}
