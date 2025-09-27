/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.columnar;

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