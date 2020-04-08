/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.MetaData;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
public abstract class MetaAttribute<T> extends MetaData {
    
    //private static Log log = LogFactory.getLog( MetaAttribute.class );

    /**
     * Constructs the MetaClass
     */
    public MetaAttribute(String type, String subtype, String name ) {
        super( type, subtype, name );
    }

    /**
     * Gets the primary MetaData class
     */
    @Override
    public Class<? extends MetaData> getMetaDataClass() {
        return MetaAttribute.class;
    }

    /**
     * Sets the value of the MetaAttribute
     */
    public abstract void setValue(Object value);

    /**
     * Returns the value of the MetaAttribute
     */
    public abstract T getValue();

    /**
     * Sets the value as a String
     *
     * @param value String value of the attribute
     */
    public abstract void setValueAsString(String value);

    /**
     * Returns the value of the MetaAttribute as a String
     */
    public abstract String getValueAsString();
}
