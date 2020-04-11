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

    /**
     * Constructs a bean MetaClass
     */
    public PojoMetaObject( String name ) {
        super( SUBTYPE_POJO, name );
    }

    /**
     * Constructs a bean MetaClass
     */
    public PojoMetaObject( String subType, String name ) {
        super( subType, name );
    }

    protected void uppercase( StringBuilder b, String name ) {
        int c = name.charAt(0);
        if (c >= 'a' && c <= 'z') {
            c = c - ('a' - 'A');
        }
        b.append((char) c);
        b.append(name.substring(1));
    }

    protected String getGetterName(MetaField f) {
        // Create the getter name
        StringBuilder m = new StringBuilder();
        m.append("get");
        uppercase( m, f.getName() );
        return m.toString();
    }

    /**
     * Retrieve GET Method
     */
    protected Method retrieveGetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        synchronized (f) {
            Method method = (Method) f.getCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName());
            if (method == null) {

                String name = getGetterName(f);

                try {
                    method = objClass.getMethod(name, new Class[0]);
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodError("No getter exists named [" + name + "] on object [" + objClass.getName() + "]");
                }

                f.setCacheValue(CACHE_PARAM_GETTER_METHOD + "." + objClass.getName(), method);
            }

            return method;
        }
    }

    protected String getSetterName(MetaField f) {
        // Create the getter name
        StringBuilder b = new StringBuilder();
        b.append("set");
        uppercase( b, f.getName() );
        return b.toString();
    }

    /**
     * Retrieve SET Method
     */
    protected Method retrieveSetterMethod(MetaField f, Class<?> objClass) //throws MetaException
    {
        synchronized (f) {
            Method method = (Method) f.getCacheValue(CACHE_PARAM_SETTER_METHOD + "." + objClass.getName());
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
            }

            return method;
        }
    }

    /**
     * Sets the object attribute represented by this MetaField
     */
    @Override
    public void setValue(MetaField f, Object obj, Object val)  {

        if (obj == null)
            throw new IllegalArgumentException("Cannot set value on a null Object for field [" + f + "]");

        Method method = retrieveSetterMethod(f, obj.getClass());

        Class<?> c = method.getParameterTypes()[ 0];

        if (val != null && val.getClass() != c) {
            throw new ValueException("Setter expected class [" + c.getName() + "] but value was of type [" + val.getClass() + "]");
        }

        Object[] arglist = new Object[1];
        arglist[0] = val;
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
    public Object getValue(MetaField f, Object obj)  {

        if (obj == null)
            throw new IllegalArgumentException("Null object found, Object expected for field [" + f + "]");

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
