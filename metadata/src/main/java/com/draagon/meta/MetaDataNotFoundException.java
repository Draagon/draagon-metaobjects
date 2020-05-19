/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta;

@SuppressWarnings("serial")
public class MetaDataNotFoundException extends MetaDataException {

    private final String name;

    public MetaDataNotFoundException( String msg, String name ) {
        super(msg);
        this.name = name;
    }

    protected String prefix( String type, String name ) {
        return "["+name+"]";
    }

    public String getName() {
        return name;
    }
}
