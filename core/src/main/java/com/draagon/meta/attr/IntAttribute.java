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
public class IntAttribute extends MetaAttribute<String> {
    //private static Log log = LogFactory.getLog( StringAttribute.class );

    public final static String SUBTYPE_INT = "int";

    /**
     * Constructs the Integer MetaAttribute
     */
    public IntAttribute(String name ) {
        super( SUBTYPE_INT, name, DataTypes.INT);
    }

}
