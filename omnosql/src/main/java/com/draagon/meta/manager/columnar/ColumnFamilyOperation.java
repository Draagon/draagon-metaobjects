/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.columnar;

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