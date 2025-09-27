/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.columnar;

/**
 * Enumeration of column family operations
 */
public enum ColumnFamilyOperation {
    INSERT,
    UPDATE,
    DELETE,
    SELECT,
    BATCH_INSERT,
    BATCH_UPDATE,
    BATCH_DELETE,
    TRUNCATE,
    CREATE_COLUMN,
    DROP_COLUMN,
    ALTER_COLUMN
}