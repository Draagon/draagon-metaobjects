package com.draagon.meta.object.proxy;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyObjectHandler implements InvocationHandler {

    private MetaObject metaObject;
    private Object proxyObject;

    public ProxyObjectHandler( MetaObjectAware proxy ) {
        this.metaObject = proxy.getMetaData();
        this.proxyObject = proxy;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Proxy Invocation Handler

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

        String name = method.getName();

        try {
            Method proxyMethod = proxyObject.getClass().getMethod(method.getName(), method.getParameterTypes());
            return proxyMethod.invoke( proxyObject, objects );
        }
        catch (NoSuchMethodException e) {

            if ( proxyObject instanceof ProxyAccessor ) {
                ProxyAccessor access = (ProxyAccessor) proxyObject;

                if (( name.startsWith("get") || name.startsWith("is"))
                        && objects == null) {
                    return access._getValueByName(getField(name));
                }
                else if (name.startsWith("set")
                        && objects != null && objects.length==1) {
                    access._setValueByName(getField(name), objects[0]);
                    return null;
                }
            }

            throw e;
        }
    }

    protected String getField( String name ) {

        int index = 3;
        if ( name.startsWith("set") || name.startsWith("get")) index = 3;
        else if (name.startsWith("is")) index = 2;

        String f = name.substring(index);
        f = f.substring(0,1).toLowerCase() + f.substring(1);
        if ( metaObject.getMetaField(f) == null ) {
            throw new IllegalArgumentException("MetaField["+f+"] did not exist for method name ["+name+"] "+
                    "on proxied object: "+proxyObject);
        }
        return f;
    }
}
