/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.*;

/**
 * A Double Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
public class DoubleField extends MetaField<Double>
{
    public DoubleField( String name ) {
        super( SUBTYPE_DOUBLE, name );
    }
    
    //private static Log log = LogFactory.getLog( DoubleField.class );

    /**
     * Returns the type of value
     */
    public int getType()
    {
    return DOUBLE;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass()
    {
    return Double.class;
    }
}
