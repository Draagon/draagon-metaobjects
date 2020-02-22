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
public class ClassAttribute extends MetaAttribute<Class> {
    //private static Log log = LogFactory.getLog( ClassAttribute.class );

    private Class<?> mClass = null;

    /**
     * Constructs the MetaClass
     */
    public ClassAttribute(String name) {
        super(name);
    }

    /**
     * Sets the value of the MetaAttribute
     */
    @Override
    public void setValue(Class value) {
        mClass = (Class<?>) value;
    }

    /**
     * Returns the value of the MetaAttribute
     */
    @Override
    public Class<?> getValue() {
        return mClass;
    }

    @Override
    public void setValueAsString(String value) {
        try {
            mClass = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new InvalidAttributeValueException("Invalid Class Name [" + value + "]");
        }
    }

    @Override
    public String getValueAsString() {
        return mClass.getName();
    }

    @Override
    public Object clone() {
        ClassAttribute ca = (ClassAttribute) super.clone();
        ca.mClass = mClass;
        return ca;
    }
}
