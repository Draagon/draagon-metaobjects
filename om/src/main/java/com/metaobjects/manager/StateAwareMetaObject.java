/*
 * Copyright (c) 2003-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.metaobjects.manager;

import com.metaobjects.MetaDataException;
import com.metaobjects.field.MetaField;

/**
 * Defines MetaObject implementations where the objects are aware of their state.
 * 
 * @author dmealing
 */
public interface StateAwareMetaObject {
    ///////////////////////////////////////////////////////////////
    // Object State Methods

    public boolean isNew(Object obj) throws MetaDataException;

    public boolean isModified(Object obj) throws MetaDataException;

    public boolean isDeleted(Object obj) throws MetaDataException;

    public void setNew(Object obj, boolean state) throws MetaDataException;

    public void setModified(Object obj, boolean state) throws MetaDataException;

    public void setDeleted(Object obj, boolean state) throws MetaDataException;

    public long getCreationTime(Object obj) throws MetaDataException;

    public long getModifiedTime(Object obj) throws MetaDataException;

    public long getDeletedTime(Object obj) throws MetaDataException;

    /**
     * Returns whether the field on the object was modified
     */
    public boolean isFieldModified(MetaField f, Object obj) throws MetaDataException;

    /**
     * Sets whether the field is modified
     */
    public void setFieldModified(MetaField f, Object obj, boolean state) throws MetaDataException;

    /**
     * Gets the time the field was modified
     */
    public long getFieldModifiedTime(MetaField f, Object obj) throws MetaDataException;
}
