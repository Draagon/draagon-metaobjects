/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.columnar;

/**
 * Represents a row key in a column family database
 */
public class RowKey {
    private final Object key;
    
    public RowKey(Object key) {
        this.key = key;
    }
    
    public Object getKey() {
        return key;
    }
    
    @Override
    public String toString() {
        return key != null ? key.toString() : "null";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RowKey rowKey = (RowKey) obj;
        return key != null ? key.equals(rowKey.key) : rowKey.key == null;
    }
    
    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}