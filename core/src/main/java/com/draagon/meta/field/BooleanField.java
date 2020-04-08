/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
/**
 * A Boolean Field.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
public class BooleanField extends MetaField<Boolean> {
    //private static Log log = LogFactory.getLog( BooleanField.class );

    public BooleanField(String type, String subtype, String name ) {
        super( type, subtype, name );
    }

    /**
     * Returns the type of value
     */
    public int getType() {
        return BOOLEAN;
    }

    /**
     * Gets the type of value object class returned
     */
    public Class<?> getValueClass() {
        return Boolean.class;
    }
}
