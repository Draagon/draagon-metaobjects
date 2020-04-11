/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

import java.util.Date;

/**
 * A Date Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class DateField extends MetaField<Date> {

    public final static String SUBTYPE_DATE     = "date";

    public DateField( String name ) {
        super( SUBTYPE_DATE, name, DataTypes.DATE );
    }
}
