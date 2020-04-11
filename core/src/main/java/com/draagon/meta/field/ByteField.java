/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Byte Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class ByteField extends MetaField<Byte> {

    public final static String SUBTYPE_BYTE = "byte";

    public ByteField(String name) {
        super(SUBTYPE_BYTE, name, DataTypes.BYTE);
    }
}
