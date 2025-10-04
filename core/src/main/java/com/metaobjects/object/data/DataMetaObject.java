/*
 * Copyright 2002 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.object.data;

import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.field.MetaField;
import com.metaobjects.identity.MetaIdentity;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.pojo.PojoMetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.relationship.MetaRelationship;
import com.metaobjects.util.DataConverter;
import com.metaobjects.validator.MetaValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class DataMetaObject extends PojoMetaObject
{
    private static final Logger log = LoggerFactory.getLogger(DataMetaObject.class);

    public final static String SUBTYPE_DATA = "data";

    public static final String ATTR_ALLOWEXTENSIONS = "allowExtensions";
    public static final String ATTR_ISSTRICT = "isStrict";

    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(DataMetaObject.class, def -> def
                .type(MetaObject.TYPE_OBJECT).subType(SUBTYPE_DATA)
                .description("Data-based metadata object with dynamic attribute access")
                .optionalChild(MetaField.TYPE_FIELD, "*")
                .optionalChild(MetaAttribute.TYPE_ATTR, "*")
                .optionalChild(MetaValidator.TYPE_VALIDATOR, "*")
                .optionalChild(MetaIdentity.TYPE_IDENTITY, "*", "*")  // Enable new MetaIdentity system for value objects
                .optionalChild(MetaRelationship.TYPE_RELATIONSHIP, "*", "*")  // Enable new MetaIdentity system for value objects
            );
            if (log != null) {
                log.debug("Registered DataMetaObject type with unified registry");
            }
        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register DataMetaObject type with unified registry", e);
            }
        }
    }

    // NOTE:  The PojoMetaObject will keep attempting to use reflection, so this bypasses that
    public final static String CACHE_PARAM_HAS_GETTER_METHOD = "hasGetterMethod";
    public final static String CACHE_PARAM_HAS_SETTER_METHOD = "hasSetterMethod";

    public final static List<String> ignoreGetterFieldNames = Arrays.asList( "class", "metaData" );

    @Override
    protected Method retrieveGetterMethod(MetaField mf, Class<?> objClass) {
        if ( ignoreGetterFieldNames.contains( mf.getName() )) return null;
        return super.retrieveGetterMethod( mf, objClass );
    }

    /**
     * Constructs the MetaClassObject for MetaObjects
     */
    public DataMetaObject(String name ) {
        super( SUBTYPE_DATA, name);
    }
    
    protected DataMetaObject(String subTypeName, String name ) {
        super( subTypeName, name );
    }

    /**
     * Manually create a ValueMetaObject with the specified name
     * @param name Name for the ValueMetaObject
     * @return Created DataMetaObject
     */
    public static DataMetaObject create(String name ) {
        return new DataMetaObject( name );
    }

    protected Class<?> getDefaultObjectClass() {
        return DataObject.class;
    }

    /**
     * Retrieves the object class of an object
     */
    @Override
    public Class<?> getObjectClass() throws ClassNotFoundException {

        Class<?> c = null;

        if (hasObjectAttr())
            c = getObjectClassFromAttr();

        if (c == null)
            c = createClassFromMetaDataName( false );

        if ( c == null )
            c = getDefaultObjectClass();

        return c;
    }

    public boolean allowExtensions() {
        if ( hasMetaAttr(ATTR_ALLOWEXTENSIONS)) {
            return DataConverter.toBoolean(getMetaAttr(ATTR_ALLOWEXTENSIONS));
        }
        return false;
    }

    public boolean isStrict() {
        if ( hasMetaAttr(ATTR_ISSTRICT)) {
            return DataConverter.toBoolean(getMetaAttr(ATTR_ISSTRICT));
        }
        return true;
    }

    /**
     * Whether the MetaClass handles the object specified
     */
    @Override
    public boolean produces(Object obj) {
        
        if (obj == null) {
            return false;
        }

        if (obj instanceof DataObjectBase) {

            DataObjectBase o = (DataObjectBase) obj;

            if (o._getObjectName() == null) {
                // See if we can match by the object produced
                return super.produces(obj);
            }

            // TODO: WARNING:  This doesn't match up class loaders!
            if (o._getObjectName().equals(getName())) {
                return true;
            }
        }

        return false;
    }


    ////////////////////////////////////////////////////
    // PERSISTENCE METHODS


    protected boolean hasGetterMethod(MetaField f, Class<?> objClass) {

        // Try the cache value first
        Boolean b = (Boolean) f.getCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName());
        if (b != null) {
            return b.booleanValue();
        }

        // Now try to actually get the method
        Method m = null;
        try {
            m = retrieveGetterMethod(f, objClass);
        } catch (NoSuchMethodError e) {
        }

        // Return whether the setter existed
        if (m != null) {
            f.setCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName(), Boolean.TRUE);
            return true;
        } else {
            f.setCacheValue(CACHE_PARAM_HAS_GETTER_METHOD + "." + objClass.getName(), Boolean.FALSE);
            return false;
        }
    }

    protected boolean hasSetterMethod(MetaField f, Class<?> objClass) {

        // Try the cache value first
        Boolean b = (Boolean) f.getCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName());
        if (b != null) {
            return b.booleanValue();
        }

        // Now try to actually get the method
        Method m = null;
        try {
            m = retrieveSetterMethod(f, objClass);
        } catch (NoSuchMethodError e) {
        }

        // Return whether the setter existed
        if (m != null) {
            f.setCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName(), Boolean.TRUE);
            return true;
        } else {
            f.setCacheValue(CACHE_PARAM_HAS_SETTER_METHOD + "." + objClass.getName(), Boolean.FALSE);
            return false;
        }
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    @Override
    public Object getValue(MetaField f, Object obj) //throws MetaException
    {
        if (!(obj instanceof DataObjectBase)) {
            throw new IllegalArgumentException("DataObjectBase expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        if (hasGetterMethod(f, obj.getClass())) {
            return super.getValue(f, obj);
        } else {
            return ((DataObjectBase) obj)._getObjectAttribute(f.getName());
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    @Override
    public void setValue(MetaField f, Object obj, Object value) //throws MetaException
    {
        if (!(obj instanceof DataObjectBase)) {
            throw new IllegalArgumentException("DataObjectBase expected, Invalid object of class [" + obj.getClass().getName() + "]");
        }

        // Convert the value to the appropriate data type for this field
        // TODO:  Handle strict mode here
        value = DataConverter.toType(f.getDataType(), value);

        // Call the setter method if it exists
        if (hasSetterMethod(f, obj.getClass())) {
            super.setValue(f, obj, value);
        }
        // Otherwise set the value directly
        else {
            ((DataObjectBase) obj)._setObjectAttribute( f.getName(), value );
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Package Protected DataObjectBase Methods

    public String getObjectName( DataObjectBase o ) {
        return o._getObjectName();
    }

    public boolean objectAllowsExtension( DataObjectBase o ) {
        return o._allowsExtensions();
    }

    public boolean objectEnforcesStrictness( DataObjectBase o ) {
        return o._enforcesStrictness();
    }

    public boolean isObjectPropertyTrue(DataObjectBase o, String name) {
        return o._isObjectPropertyTrue(name);
    }

    public String getObjectProperty(DataObjectBase o, String name) {
        return o._getObjectProperty(name);
    }

    public void setObjectProperty(DataObjectBase o, String name, String key ) {
        o._setObjectProperty( name, key );
    }

    public Collection<String> getObjectFieldNames( DataObjectBase o ) {
        return o._getObjectFieldNames();
    }

    public boolean hasObjectAttribute(DataObjectBase o, String name ) {
        return o._hasObjectAttribute( name );
    }

    public Collection<String> getObjectAttributes( DataObjectBase o ) {
        return o._getObjectAttributes();
    }

    public Object getObjectAttribute(DataObjectBase o, String name) {
        return o._getObjectAttribute(name);
    }
}
