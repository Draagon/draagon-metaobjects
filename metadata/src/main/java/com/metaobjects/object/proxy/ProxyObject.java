package com.metaobjects.object.proxy;

import com.metaobjects.ValueException;
import com.metaobjects.field.MetaField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.Validatable;
import com.metaobjects.util.DataConverter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class ProxyObject implements ProxyAccessor, MetaObjectAware, Validatable {

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
    // Package Protected getter/setter Methods with Array Support

    public Object _getValueByName(String name) {
        Object value = valueMap.get(name);

        // Smart fallback: if field is an array but getter might expect different format
        if (isArrayField(name) && value instanceof List) {
            // For proxy objects, return the List directly - this works best with generated interfaces
            return value;
        }

        return value;
    }

    public void _setValueByName(String name, Object val) {
        if (isArrayField(name)) {
            // Field is defined as array - ensure proper conversion
            if (val instanceof List) {
                // Already a list, store directly
                valueMap.put(name, val);
            } else if (val instanceof String) {
                // Convert string to list using DataConverter
                List<String> list = DataConverter.toStringArray((String) val);
                valueMap.put(name, list);
            } else if (val != null) {
                // Single value - convert to single-element list
                valueMap.put(name, Arrays.asList(val));
            } else {
                // Null value
                valueMap.put(name, null);
            }
        } else {
            // Regular field - store as-is
            valueMap.put(name, val);
        }
    }

    /**
     * Check if a field is defined as an array type.
     * Uses metadata when available.
     */
    protected boolean isArrayField(String fieldName) {
        if (metaObject != null) {
            MetaField field = metaObject.getMetaField(fieldName);
            return field != null && field.isArrayType();
        }
        return false;
    }

    protected String getField( Method method, String name ) {

        int index = 3;
        if ( name.startsWith("set") || name.startsWith("get")) index = 3;
        else if (name.startsWith("is")) index = 2;

        String f = name.substring(index);
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
        // Validation is now handled by the constraint system during metadata construction
        // ProxyObjects rely on the underlying data structure for validation
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
