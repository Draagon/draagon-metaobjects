/*
 * Copyright (c) 2004-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Created on Jun 14, 2004
 */
package com.draagon.meta.object;

import com.draagon.meta.MetaDataAware;

/**
 * Whether an object is aware of it's MetaObject
 * 
 * @author dmealing
 */
public interface MetaObjectAware extends MetaDataAware<MetaObject> {

    /** 
     * Returns a MetaObject for the getMetaData call
     */
    @Override
    public MetaObject getMetaData();

    /**
     * Sets the MetaObject onto the object 
     */
    @Override
    public void setMetaData( MetaObject metaObject );
}
