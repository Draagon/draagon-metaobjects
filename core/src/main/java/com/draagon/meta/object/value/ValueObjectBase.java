
package com.draagon.meta.object.value;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObjectBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class ValueObjectBase extends DataObjectBase implements Map<String, Object> {

    static final Logger log = LoggerFactory.getLogger(ValueObjectBase.class);

    /**
     * Create a generic value object that supports extensions by default
     */
    public ValueObjectBase() {
        _allowExtensions( false );
    }

    /**
     * Create a generic value object associated to the MetaObject
     * @param mo MetaObject to associated
     */
    public ValueObjectBase(MetaObject mo) {
        super( mo );
    }

    /**
     * A generic value object with the specified name
     * @param name Name of the object
     */
    public ValueObjectBase(String name ) {
        super(name);
        _allowExtensions( true );
    }

    /**
     * Return the name of the object
     * @return the object name
     */
    public String getObjectName() {
        return super._getObjectName();
    }

    /**
     * Set whether to allow extensions (only works when no metadata is attached
     */
    public void allowExtensions( boolean allowExtensions ) {
        super._allowExtensions( allowExtensions );
    }

    /**
     * Whether this value object supports extensions
     * @return true if extensions are supported
     */
    public boolean allowsExtensions() {
        return super._allowsExtensions();
    }

    protected boolean enforcesStrictness() {
        return _enforcesStrictness();
    }

    protected void enforceStrictness( boolean strict ) {
        super._enforceStrictness(strict);
    }

    /**
     * Sets an attribute of the MetaObject
     */
    public void setObjectAttribute(String name, Object value ) {
        super._setObjectAttribute(name,value);
    }

    /**
     * Checks is a property of this object is true based on a value of "true".  Null is false.
     * @param name Name of the property
     * @return Whether the property has a value of "true"
     */
    public boolean isObjectPropertyTrue(String name ) {
        return super._isObjectPropertyTrue(name);
    }

    /**
     * Get a property associated with this object.  Properties are used for special operations, not to define fields
     */
    public String getObjectProperty(String name ) {
        return super._getObjectProperty( name );
    }

    /**
     * Sets a property to be associated with this object.  Properties are used for special operations, not to define fields
     */
    public void setObjectProperty(String name, String key ) {
        super._setObjectProperty( name, key );
    }

    /**
     * Return all field names associated with this object.  Handles all MetaFields on the associated MetaObject
     * if one exists.  Also handles adding any extended fields
     *
     * @return All field names
     */
    public Collection<String> getObjectFieldNames() {
        return super._getObjectFieldNames();
    }

    /**
     * Returns whether the attribute is associated with this ValueObject (ignoring the MetaObject)
     * @param name Name of the attribute/value
     * @return True if it exists on this ValueObject
     */
    public boolean hasObjectAttribute( String name ) {
        return super._hasObjectAttribute( name );
    }

    /**
     * Returns the names of all attribute values associated with this ValueObject (ignoring the MetaObject)
     * @return Collection of attribute value names
     */
    public Collection<String> getObjectAttributes() {
        return super._getObjectAttributes();
    }

    /**
     * Retrieves an attribute of the MetaObject
     */
    public Object getObjectAttribute(String name) {
        return super._getObjectAttribute(name);
    }


    //////////////////////////////////////////////////////////
    // MAP METHODS

    @Override
    public void clear() {
        super._clearObjectAttributeValues();
    }

    @Override
    public boolean containsKey(Object key) {
        return getObjectFieldNames().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {

        for( String name : getObjectFieldNames() ) {
            if (value == null && getObjectAttribute(name) == null) {
                return true;
            }
            if (value != null && value.equals(getObjectAttribute(name))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates a Map.Entry for a specific MetaField and Object
     */
    public class AttributeEntry implements Entry<String, Object> {

        private String attr = null;

        public AttributeEntry(String attr) {
            this.attr = attr;
        }

        @Override
        public String getKey() {
            return attr;
        }

        @Override
        public Object getValue() {
            return getObjectAttribute(attr);
        }

        @Override
        public Object setValue(Object value) {
            Object old = getObjectAttribute(attr);
            setObjectAttribute(attr,value);
            return old;
        }
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {

        Set<Entry<String, Object>> s = new HashSet<Entry<String, Object>>();
        for (String name : getObjectFieldNames()) {
            s.add(new AttributeEntry(name));
        }
        return s;
    }

    @Override
    public Object get(Object key) {
        return getObjectAttribute(String.valueOf(key));
    }

    @Override
    public boolean isEmpty() {
        if ( getObjectFieldNames().size() == 0 ) return true;
        for ( String key : getObjectFieldNames() ) {
            if ( getObjectAttributeValue( key ) != null ) return false;
        }
        return true;
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>( getObjectFieldNames() );
    }

    @Override
    public Object put(String key, Object value) {
        Object old = getObjectAttribute( key );
        setObjectAttribute( key, value );
        return old;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (String key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public Object remove(Object key) {
        return super._removeObjectAttributeValue(key);
    }

    @Override
    public int size() {
        return getObjectFieldNames().size();
    }

    @Override
    public Collection<Object> values() {
        Collection<Object> s = new ArrayList<Object>();
        for (String name : getObjectFieldNames()) {
            s.add(getObjectAttribute(name));
        }
        return s;
    }
}
