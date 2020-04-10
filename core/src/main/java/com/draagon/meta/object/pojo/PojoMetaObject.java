/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.object.pojo;

import com.draagon.meta.ValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import java.lang.reflect.*;

@SuppressWarnings("serial")
public class PojoMetaObject extends MetaObject {

    public final static String SUBTYPE_POJO = "pojo";

    public final static String CACHE_PARAM_GETTER_METHOD = "getterMethod";
    public final static String CACHE_PARAM_SETTER_METHOD = "setterMethod";

    //private static Log log = LogFactory.getLog( BeanMetaClass.class );
    //private WeakHashMap<MetaField,Method> mGetMethods = null;
    //private WeakHashMap<MetaField,Method> mSetMethods = null;
    
    /**
     * Constructs a bean MetaClass
     */
    public PojoMetaObject( String name ) {
        super( SUBTYPE_POJO, name );
        //mGetMethods = new WeakHashMap<MetaField,Method>();
        //mSetMethods = new WeakHashMap<MetaField,Method>();
    }

    /**
     * Constructs a bean MetaClass
     */
    public PojoMetaObject( String subType, String name ) {
        super( subType, name );
        //mGetMethods = new WeakHashMap<MetaField,Method>();
        //mSetMethods = new WeakHashMap<MetaField,Method>();
    }

    protected String getGetterName(MetaField f) {
        // Create the getter name
        StringBuilder methname = new StringBuilder();
        methname.append("get");
        int c = f.getName().charAt(0);
        if (c >= 'a' && c <= 'z') {
            c = c - ('a' - 'A');
        }
        methname.append((char) c);
        methname.append(f.getName().substring(1));

        return methname.toString();
    }

    /**
     * Retrieve GET Method
     */
    protected Method retrieveGetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        synchronized (f) {
            Method method = (Method) f.getCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName());
            //synchronized( mGetMethods ) {
            //  method = (Method) mGetMethods.get( f );
            //}
            if (method == null) {
                String name = getGetterName(f);

                try {
                    method = objClass.getMethod(name, new Class[0]);
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodError("No getter exists named [" + name + "] on object [" + objClass.getName() + "]");
                }

                //synchronized( mGetMethods ) {
                //  mGetMethods.put( f, method );
                //}
                f.setCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName(), method);
            }

            return method;
        }
    }

    protected String getSetterName(MetaField f) {
        // Create the setter name
        StringBuffer methname = new StringBuffer();
        methname.append("set");
        int c = f.getName().charAt(0);
        if (c >= 'a' && c <= 'z') {
            c = c - ('a' - 'A');
        }
        methname.append((char) c);
        methname.append(f.getName().substring(1));

        return methname.toString();
    }

    /**
     * Retrieve SET Method
     */
    protected Method retrieveSetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        synchronized (f) {
            Method method = (Method) f.getCacheValue(CACHE_PARAM_SETTER_METHOD + "." + objClass.getName());
            //synchronized( mSetMethods ) {
            //  method = (Method) mSetMethods.get( f );
            //}

            if (method == null) {
                String name = getSetterName(f);
                Method[] methods = objClass.getMethods();

                for (int i = 0; i < methods.length; i++) {
                    if (methods[ i].getName().equals(name)
                            && methods[ i].getParameterTypes().length == 1
                            && methods[ i].getParameterTypes()[ 0] == f.getValueClass()) {
                        method = methods[ i];
                        break;
                    }
                }


                if (method == null) {
                    throw new NoSuchMethodError("No setter with a single variable exists named [" + name + "] with argument class [" + f.getValueClass().getSimpleName() + "] on object [" + objClass.getName() + "]");
                }

                f.setCacheValue(CACHE_PARAM_SETTER_METHOD + "." + objClass.getName(), method);

                //synchronized( mSetMethods ) {
                //  mSetMethods.put( f, method );
                //}
            }

            return method;
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    @Override
    public void setValue(MetaField f, Object obj, Object val) //throws MetaException
    {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot set value on a null Object for field [" + f + "]");
        }

        Method method = retrieveSetterMethod(f, obj.getClass());

        Class<?> c = method.getParameterTypes()[ 0];

        if (val != null && val.getClass() != c) {
            throw new ValueException("Setter expected class [" + c.getName() + "] but value was of type [" + val.getClass() + "]");
        }

        Object[] arglist = new Object[1];
        arglist[ 0] = val;
        try {
            method.invoke(obj, arglist);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the object attribute represented by this MetaField
     */
    @Override
    public Object getValue(MetaField f, Object obj) //throws MetaException
    {
        if (obj == null) {
            throw new IllegalArgumentException("Null object found, Object expected for field [" + f + "]");
        }

        Method method = retrieveGetterMethod(f, obj.getClass());

        try {
            return method.invoke(obj, new Object[0]);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access Exception setting field [" + f + "] on object [" + obj.getClass() + "]: " + e.getMessage(), e);
        }
    }
    
    /**
     * Whether the MetaObject produces the object specified
     */
    @Override
    public boolean produces(Object obj) {
        try {
            Class<?> cl = getObjectClass();
            if (cl == null) {
                return false;
            }

            boolean rc = obj.getClass().equals( cl );
            if (rc) {
                return true;
            }
        } catch (ClassNotFoundException e) {
        };

        return false;
    }    
}
