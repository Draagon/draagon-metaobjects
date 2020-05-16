/*
 * Copyright (c) 2003-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;

/**
 * Defines MetaObject implementations where the objects are aware of their state.
 * 
 * @author dmealing
 */
public interface StateAwareMetaObject {
    ///////////////////////////////////////////////////////////////
    // Object State Methods

    public abstract boolean isNew(Object obj) throws MetaDataException;

    public abstract boolean isModified(Object obj) throws MetaDataException;

    public abstract boolean isDeleted(Object obj) throws MetaDataException;

    public abstract void setNew(Object obj, boolean state) throws MetaDataException;

    public abstract void setModified(Object obj, boolean state) throws MetaDataException;

    public abstract void setDeleted(Object obj, boolean state) throws MetaDataException;

    public abstract long getCreationTime(Object obj) throws MetaDataException;

    public abstract long getModifiedTime(Object obj) throws MetaDataException;

    public abstract long getDeletedTime(Object obj) throws MetaDataException;

    /**
     * Returns whether the field on the object was modified
     */
    public abstract boolean isFieldModified(MetaField f, Object obj) throws MetaDataException;

    /**
     * Sets whether the field is modified
     */
    public abstract void setFieldModified(MetaField f, Object obj, boolean state) throws MetaDataException;

    /**
     * Gets the time the field was modified
     */
    public abstract long getFieldModifiedTime(MetaField f, Object obj) throws MetaDataException;
}
