package com.metaobjects.field;

import com.metaobjects.DataTypes;
import com.metaobjects.InvalidValueException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmealing on 11/10/2016.
 */
@SuppressWarnings("serial")
public abstract class ArrayField<A,T extends List> extends MetaField<T> {

    /** Attribute name for array item name configuration */
    public final static String ATTR_ITEM_NAME   = "itemName";

    /**
     * Creates an ArrayField with the specified subtype, name and data type
     * @param subType the field subtype
     * @param name the field name
     * @param dataType the data type, must be an array type
     * @throws IllegalStateException if dataType is not an array type
     */
    public ArrayField(String subType, String name, DataTypes dataType ) {
        super( subType, name, dataType );
        if ( !dataType.isArray() )
            throw new IllegalStateException( "ArrayField [" + subType + "] with name [" + name + "] must use an Array DataType: [" + dataType + "]" );
        //addAttributeDef( new AttributeDef( ATTR_ITEM_NAME, String.class, false, "Name for the items in the array, default is \"item\"" ));
    }

    // NOTE: This is critcal that a copy is made, otherwise the same List ends up shared all over
    @Override
    public T getDefaultValue() {
        T def = super.getDefaultValue();
        if ( def != null ) {
            List<A> list = new ArrayList<>();
            list.addAll(def);
            def = (T) list;
        }
        return def;
    }

    @Override
    protected T convertDefaultValue(Object o) {

        if (!getValueClass().isInstance(o)) {
            List<A> list = new ArrayList<>();
            if ( o instanceof String && "[]".equals(o)) {
                return (T) new ArrayList<A>();
            }
            else if ( getArrayItemClass().isInstance(o)) {
                list.add((A)o);
            }
            else if (getArrayItemClass().equals(String.class)) {
                list.add((A)String.valueOf(o));
            }
            else {
                throw new InvalidValueException("Could not set value on array of class: "
                        +getArrayItemClass().getClass());
            }
            return (T) list;
        }
        else {
            return (T) o;
        }
    }


    /**
     * Return the array item type
     * @return the class type of array items
     */
    @SuppressWarnings("unchecked")
    public Class<A> getArrayItemClass() {
        return (Class<A>) getDataType().getArrayItemClass();
    }
}
