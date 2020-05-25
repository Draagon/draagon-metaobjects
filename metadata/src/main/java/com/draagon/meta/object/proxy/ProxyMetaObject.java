package com.draagon.meta.object.proxy;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.pojo.PojoMetaObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class ProxyMetaObject extends PojoMetaObject {

    public final static String OBJECT_SUBTYPE = "proxy";
    public final static String ATTR_PROXYOBJECT = "proxyObject";

    public ProxyMetaObject(String name) {
        super(OBJECT_SUBTYPE,name);
    }

    protected ProxyMetaObject(String subType, String name) {
        super(subType,name);
    }

    public static MetaObject create(String name, Class<?> objectClass ) {
        return create( name, objectClass, null );
    }
    public static MetaObject create(String name, Class<?> objectClass, Class<?> proxyObjectClass ) {
        MetaObject mo = new ProxyMetaObject( name )
                .addChild(StringAttribute.create(ATTR_OBJECT, objectClass.getName() ));
        if ( proxyObjectClass != null )
            mo.addChild(StringAttribute.create(ATTR_PROXYOBJECT, proxyObjectClass.getName() ));
        return mo;
    }

    @Override
    public Object newInstance()  {

        Class<?> clazz = getObjectClass();

        Object o = Proxy.newProxyInstance(
                ProxyObject.class.getClassLoader(),
                new Class[] { clazz },    // To make this extensible, add the interface here
                new ProxyObjectHandler( newProxyInstance() ));

        //if ( o == null ) {
        //    throw new MetaDataException("Cannot instantiate proxy object ["+clazz.getName()+"], null returned");
        //}

        setDefaultValues(o);

        return o;
    }

    @Override
    public void attachMetaObject(Object o) {
        if ( o instanceof MetaObjectAware ) {
            ((MetaObjectAware) o ).setMetaData(this);
        }
    }

    /**
     * Retrieves the object class of an object, or null if one is not specified
     */
    @Override
    public Class<?> getObjectClass() {

        if ( hasObjectAttr()) {
            try {
                Class<?> c = getObjectClassFromAttr();
                if ( !c.isInterface() ) {
                    throw new InvalidMetaDataException( this,
                            "Object class ["+getMetaAttr(ATTR_OBJECT)+"] must be an interface");
                }
                return c;
            }
            catch (ClassNotFoundException e) {
                throw new InvalidMetaDataException( this, "Object class ["+getMetaAttr(ATTR_OBJECT)+"] was not found");
            }
        }
        else {
            throw new InvalidMetaDataException( this,
                    "An '"+ATTR_OBJECT+"' attribute must be specified and it must be an interface");
        }
    }

    /**
     * Retrieves the object class of an object, or null if one is not specified
     */
    public Class<?> getProxyObjectClass() {

        final String KEY = "ProxyObjectClass";

        // See if we have this cached already
        Class<?> oc = (Class<?>) getCacheValue( KEY );
        if ( oc == null ) {

            if ( hasMetaAttr(ATTR_PROXYOBJECT)) {

                MetaAttribute proxyObjectAttr = getMetaAttr(ATTR_PROXYOBJECT);
                String proxyObject = proxyObjectAttr.getValueAsString();

                try {
                    oc = Class.forName( proxyObject );

                    if ( oc.isInterface() ) {
                        throw new InvalidMetaDataException( proxyObjectAttr, "ProxyObject Class ["+proxyObject+"] "+
                                "for MetaObject [" + getName() + "] cannot be an interface");
                    }
                    else if ( !MetaObjectAware.class.isAssignableFrom( oc )) {
                        throw new InvalidMetaDataException( proxyObjectAttr, "ProxyObject Class ["+proxyObject+"] "+
                                "for MetaObject [" + getName() + "] must implement MetaDataAware");
                    }
                }
                catch (ClassNotFoundException e) {
                    throw new InvalidMetaDataException( proxyObjectAttr,
                            "Could not find ProxyObject Class ["+proxyObject+"] for MetaObject [" + getName() + "]: "
                                    + e.getMessage() );
                }
            }

            if ( oc==null ) oc = ProxyObject.class;

            // Store the resulting Class in the cache
            setCacheValue( KEY, oc );
        }

        return oc;
    }

    /**
     * Return a new MetaObject instance from the MetaObject
     */
    public MetaObjectAware newProxyInstance()  {

        Class<?> oc = getProxyObjectClass();

        try {
            // Construct the object and pass the MetaObject into the constructor
            Constructor c = oc.getConstructor( MetaObject.class );
            c.setAccessible(true);
            return (MetaObjectAware) c.newInstance((MetaObject) this);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new MetaDataException(
                    "Could not construct ProxyObject with single MetaObject parameter " +
                            "[" + oc + "] " + "for MetaObject [" + getName() + "]: " + e.getMessage(), e);
        }
    }


    @Override
    public boolean produces(Object obj) {
        if ( obj instanceof MetaObjectAware ) {
            MetaObject mo = ((MetaObjectAware) obj).getMetaData();
            if ( mo != null )
                return hasChild( mo.getName(), MetaObject.class );
        }
        return false;
    }

    @Override
    public Object getValue(MetaField f, Object obj) {
        return super.getValue(f,obj);
    }

    @Override
    public void setValue(MetaField f, Object obj, Object val) {
        super.setValue( f, obj, val );
    }
}
