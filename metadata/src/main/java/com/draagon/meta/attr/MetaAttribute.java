/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.*;
import com.draagon.meta.util.DataConverter;

import java.util.Objects;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
@SuppressWarnings("serial")
public class MetaAttribute<T> extends MetaData<MetaAttribute> implements DataTypeAware<T>, MetaDataValueHandler<T> {
    
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
    public MetaAttribute overload() {
        return super.overload();
    }

    /**
     * Returns the DataType for the value
     * @return DataTypes enum
     */
    @Override
    public DataTypes getDataType() {
        return dataType;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // MetaData Value Handler Methods

    /**
     * Sets the value of the MetaAttribute
     */
    @Override
    public void setValue( T value ) {
        this.value = value;
    }

    /**
     * Returns the value of the MetaAttribute
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Sets the value as a String
     *
     * @param value String value of the attribute
     */
    @Override
    public void setValueAsString(String value) {
        setValueAsObject( value );
    }

    /**
     * Returns the value of the MetaAttribute as a String
     */
    @Override
    public String getValueAsString() {
        return DataConverter.toString( value );
    }

    /**
     * Sets the Value with an Object
     * @param value Object value to set
     */
    @Override
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MetaAttribute<?> that = (MetaAttribute<?>) o;
        return Objects.equals(value, that.value) &&
                dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, dataType);
    }

    /** Get the toString Prefix */
    @Override
    protected String getToStringPrefix() {
        return  super.getToStringPrefix() + "{dataType=" + dataType + ", value=" + value + "}";
    }
}
