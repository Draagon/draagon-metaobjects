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
    
    //private static Log log = LogFactory.getLog( DateField.class );
    public DateField( String type, String subtype, String name ) {
        super( type, subtype, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return DATE;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Date.class;
    }
}
