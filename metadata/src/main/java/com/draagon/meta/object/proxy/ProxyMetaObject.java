package com.draagon.meta.object.proxy;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;
import com.draagon.meta.object.pojo.PojoMetaObject;

import java.lang.reflect.Proxy;

public class ProxyMetaObject extends PojoMetaObject {

    public final static String OBJECT_SUBTYPE = "proxy";

    public ProxyMetaObject(String name) {
        super(OBJECT_SUBTYPE,name);
    }

    protected ProxyMetaObject(String subType, String name) {
        super(subType,name);
    }

    public static ProxyMetaObject create(String name ) {
        return new ProxyMetaObject( name );
    }

    @Override
    public Object newInstance()  {

        Object o = null;

        try {
            Class clazz = getObjectClass();
            if (clazz != null) {
                if ( clazz.isInterface() ) {
                    o = Proxy.newProxyInstance(
                            ProxyObject.class.getClassLoader(),
                            new Class[] { clazz, ProxyAccessor.class, MetaObjectAware.class, Validatable.class },
                            new ProxyObject(this));
                    if ( o == null ) {
                        throw new MetaDataException("Cannot instantiate proxy object ["+clazz.getName()+"], null returned");
                    }
                }
                else {
                    throw new MetaDataException("Cannot instantiate proxy object as  class ["+clazz.getName()+"] is not an interface");
                }
            }
            else {
                throw new MetaDataException("Cannot instantiate proxy object as no class was found, use 'object' attribute");
            }
        } catch(MetaDataException | ClassNotFoundException e) {
            throw new MetaDataException("Cannot instantiate proxy object:" + e.getMessage(), e );
        }

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
    public Class<?> getObjectClass() throws ClassNotFoundException {

        Class<?> c = null;

        if ( hasObjectClassAttr()) c = getObjectClassFromAttr();

        return c;
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
