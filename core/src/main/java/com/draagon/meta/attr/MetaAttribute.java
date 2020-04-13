/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.*;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.util.DataConverter;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
@SuppressWarnings("serial")
public class MetaAttribute<T> extends MetaData<MetaAttribute> implements DataTypeAware<T> {
    
    //private static Log log = LogFactory.getLog( MetaAttribute.class );

    public final static String TYPE_ATTR = "attr";

    private T value = null;

    private DataTypes dataType;

    /**
     * Constructs the MetaClass
     */
    public MetaAttribute(String subtype, String name, DataTypes dataType ) {
        super( TYPE_ATTR, subtype, name );
        this.dataType = dataType;
    }

    /**
     * Gets the primary MetaAttribute class
     */
    @Override
    public Class<? extends MetaData> getMetaDataClass() {
        return MetaAttribute.class;
    }

    /**
     * Sets an attribute of the MetaClass
     */
    @Override
    public MetaAttribute addMetaAttr(MetaAttribute attr) {
        return addChild(attr);
    }

    /** Add Child to the Field */
    @Override
    public MetaAttribute addChild(MetaData data) throws InvalidMetaDataException {
        return super.addChild( data );
    }

    /** Wrap the MetaAttribute */
    @Override
    public MetaAttribute wrap() {
        return super.wrap();
    }

    /**
     * Returns the DataType for the value
     * @return DataTypes enum
     */
    @Override
    public DataTypes getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the MetaAttribute
     */
    public void setValue( T value ) {
        this.value = value;
    }

    /**
     * Returns the value of the MetaAttribute
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value as a String
     *
     * @param value String value of the attribute
     */
    public void setValueAsString(String value) {
        setValueAsObject( value );
    }

    /**
     * Returns the value of the MetaAttribute as a String
     */
    public String getValueAsString() {
        return DataConverter.toString( value );
    }

    /**
     * Sets the Value with an Object
     * @param value Object value to set
     */
    public void setValueAsObject(Object value) {
        this.value = (T) DataConverter.toType( dataType, value );
    }

    /**
     * Clone the MetaAttribute
     * @return MetaAttribute clone
     */
    @Override
    public Object clone() {
        MetaAttribute<T> a = (MetaAttribute<T>) super.clone();
        a.value = value;
        return a;
    }
}
