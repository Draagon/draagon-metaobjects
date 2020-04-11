/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;

/**
 * An attribute of a MetaClass, MetaField, or MetaView
 */
@SuppressWarnings("serial")
public class BooleanAttribute extends MetaAttribute<Boolean> {
    //private static Log log = LogFactory.getLog( StringAttribute.class );

    public final static String SUBTYPE_BOOLEAN = "boolean";

    /**
     * Constructs the Boolean MetaAttribute
     */
    public BooleanAttribute(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN);
    }

}