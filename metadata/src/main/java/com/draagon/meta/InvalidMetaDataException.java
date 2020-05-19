/*
 * Copyright (c) 2003-2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.draagon.meta;

@SuppressWarnings("serial")
public class InvalidMetaDataException extends MetaDataException {

    public InvalidMetaDataException( MetaData md, String msg) {
        super(prefix(md)+msg);
    }
    public InvalidMetaDataException( MetaData md, String msg, Throwable cause) {
        super(prefix(md)+msg, cause);
    }

    protected static String prefix( MetaData md ) {
        if ( md == null ) return "[null] ";
        return "["+md.toString()+"] ";
    }
}
