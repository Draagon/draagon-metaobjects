package com.draagon.meta.field;

/**
 * Created by dmealing on 11/10/2016.
 */
public abstract class ArrayField<T> extends MetaField<T> {

    public final static String ATTR_ITEM_NAME   = "itemName";

    public ArrayField(String subtype, String name ) {
        super( subtype, name );
        //addAttributeDef( new AttributeDef( ATTR_ITEM_NAME, String.class, false, "Name for the items in the array, default is \"item\"" ));
    }

    /** Return the array item type */
    public abstract Class getItemClass();
}
