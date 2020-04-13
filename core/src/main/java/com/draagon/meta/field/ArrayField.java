package com.draagon.meta.field;

import com.draagon.meta.DataTypes;

/**
 * Created by dmealing on 11/10/2016.
 */
@SuppressWarnings("serial")
public abstract class ArrayField<T> extends MetaField<T> {

    public final static String ATTR_ITEM_NAME   = "itemName";

    public ArrayField(String subType, String name, DataTypes dataType ) {
        super( subType, name, dataType );
        if ( !dataType.isArray() )
            throw new IllegalStateException( "ArrayField [" + subType + "] with name [" + name + "] must use an Array DataType: [" + dataType + "]" );
        //addAttributeDef( new AttributeDef( ATTR_ITEM_NAME, String.class, false, "Name for the items in the array, default is \"item\"" ));
    }

    /**
     * Return the array item type
     *
     * @deprecated Use getArrayItemClass()
     */
    public Class getItemClass() {
        return getArrayItemClass();
    }

    /** Return the array item type */
    public Class getArrayItemClass() {
        return getDataType().getArrayItemClass();
    }
}
