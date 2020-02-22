/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.draagon.meta;

/**
 * Interface to return the associated MetaData for an object
 * 
 * @author dmealing
 */
public interface MetaDataAware<T extends MetaData> {
    
    /** Retrieve the MetaData for the object */
    public T getMetaData();
    
    /** Sets the MetaData onto the object */
    public void setMetaData( T metaObject );
}
