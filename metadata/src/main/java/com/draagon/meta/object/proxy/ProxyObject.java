package com.draagon.meta.object.proxy;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataAware;
import com.draagon.meta.ValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProxyObject implements InvocationHandler, ProxyAccessor, MetaObjectAware, Validatable {

    private MetaObject metaObject;
    private Map<String,Object> valueMap = new HashMap<>();

    public ProxyObject(MetaObject metaObject ) {
        this.metaObject = metaObject;
    }

    @Override
    public MetaObject getMetaData() {
        return metaObject;
    }

    @Override
    public void setMetaData(MetaObject metaObject) {
        throw new IllegalStateException("Cannot set MetaData after newInstance");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Package Protected getter/setter Methods

    public Object _getValueByName(String name) {
        return valueMap.get(name);
    }

    public void _setValueByName(String name, Object val) {
        valueMap.put(name, val);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Proxy Invocation Handler

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        String name = method.getName();

        if ( name.equals("toString") && objects == null) {
            return toString();
        }
        else if ( name.equals("validate") && objects == null) {
            validate();
        }
        else if ( name.equals("_getValueByName") && objects != null && objects.length==1) {
            return _getValueByName((String)objects[0]);
        }
        else if ( name.equals("_setValueByName") && objects != null && objects.length==2) {
            _setValueByName((String)objects[0], objects[1]);
        }
        else if ( name.equals("getMetaData") && objects == null) {
            return metaObject;
        }
        else if ( name.equals("setMetaData") && objects != null && objects.length==1) {
            setMetaData((MetaObject)objects[0]);
        }
        else if ( name.equals("equals") && objects != null && objects.length==1) {
            return equals(objects[0]);
        }
        else if ( name.startsWith("get") && objects == null) {
            return valueMap.get( getField( method, name ));
        }
        else if ( name.startsWith("set") && objects != null && objects.length==1) {
            valueMap.put( getField( method, name ), objects[0] );
        }
        else {
            throw new IllegalArgumentException("Unknown method name ["+method.getName()+"] with ("+objects.length+") args on ProxyObject" );
        }
        return null;
    }

    protected String getField( Method method, String name ) {
        String f = name.substring(3);
        f = f.substring(0,1).toLowerCase() + f.substring(1);
        if ( metaObject.getMetaField(f) == null ) {
            throw new IllegalArgumentException("MetaField["+f+"] did not exist for method name ["+method.getName()+"] on ProxyObject" );
        }
        return f;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Validation Method

    @Override
    public void validate() throws ValueException {
        // TODO: Validate using MetaValidators
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if ( o == null ) return false;
        //if (o == null || getClass() != o.getClass()) return false;
        ProxyAccessor that = (ProxyAccessor) o;
        for(MetaField f : getMetaData().getMetaFields()) {
            String n = f.getName();
            Object val = _getValueByName(n);
            Object val2 = that._getValueByName(n);
            if (!Objects.equals(val,val2)) {
                return false;
            }
            //if ( valueMap.get(n) == null && that.valueMap.get(n) != null ||
            //        (valueMap.get(n) != null && !valueMap.get(n).equals(that.valueMap.get(n)))) return false;
        }
        return Objects.equals(metaObject, ((MetaObjectAware) that).getMetaData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueMap.entrySet().hashCode(), metaObject);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
                "metaObject=" + metaObject.getName() +
                ",valueMap=" + valueMap.toString() +
                '}';
    }
}
