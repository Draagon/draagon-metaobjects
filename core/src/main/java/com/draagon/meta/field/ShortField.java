/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Short Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class ShortField extends MetaField<Short> {

    public final static String SUBTYPE_SHORT    = "short";

    public ShortField( String name ) {
        super( SUBTYPE_SHORT, name, DataTypes.SHORT );
    }
}
