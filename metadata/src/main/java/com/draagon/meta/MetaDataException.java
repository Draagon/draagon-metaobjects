/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta;


@SuppressWarnings("serial")
public class MetaDataException extends RuntimeException {

    public MetaDataException(String msg) {
        super(msg);
    }

    public MetaDataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
