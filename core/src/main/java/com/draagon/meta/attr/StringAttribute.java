/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
@SuppressWarnings("serial")
public class StringAttribute extends MetaAttribute<String> {
    //private static Log log = LogFactory.getLog( StringAttribute.class );

    private String mString = null;

    public final static String SUBTYPE_STRING = "string";

    /**
     * Constructs the MetaClass
     */
    public StringAttribute(String name ) {
        super( SUBTYPE_STRING, name );
    }

    /**
     * Sets the value of the MetaAttribute
     */
    public void setValue(String value) {
        mString = value.toString();
    }

    @Override
    public void setValue(Object value) {
        mString = value.toString();
    }

    /**
     * Returns the value of the MetaAttribute
     */
    @Override
    public String getValue() {
        return mString;
    }

    @Override
    public void setValueAsString(String value) {
        setValue(value);
    }

    @Override
    public String getValueAsString() {
        return getValue();
    }

    public Object clone() {
        StringAttribute sa = (StringAttribute) super.clone();
        sa.mString = mString;
        return sa;
    }
}
