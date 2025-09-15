/*
 * Copyright (c) 2003-2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.draagon.meta;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;

public class InvalidMetaDataException extends MetaDataException {

    public InvalidMetaDataException( MetaData md, String msg) {
        super(prefix(md)+msg);
    }
    public InvalidMetaDataException( MetaData md, String msg, Throwable cause) {
        super(prefix(md)+msg, cause);
    }

    protected static String prefix( MetaData md ) {
        if ( md == null ) return "[null] ";
        String pkg = md.getPackage();
        if (pkg.isEmpty() && !(md instanceof MetaDataLoader)) pkg = MetaDataUtil.findPackageForMetaData(md);
        if (!pkg.isEmpty()) pkg+=MetaData.PKG_SEPARATOR;
        return "["+md.getClass().getSimpleName()+":"+pkg+md.getShortName()+"] ";
    }
}
