/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.keyvalue;

/**
 * Represents a single operation in a key-value transaction
 */
public class KeyValueOperation {
    
    public enum OperationType {
        GET, SET, DELETE, INCREMENT, DECREMENT, 
        LIST_APPEND, LIST_REMOVE, SET_ADD, SET_REMOVE,
        EXPIRE, EXISTS
    }
    
    private final OperationType type;
    private final String key;
    private final String field;
    private final Object value;
    private final Object additionalParam;
    
    public KeyValueOperation(OperationType type, String key) {
        this(type, key, null, null, null);
    }
    
    public KeyValueOperation(OperationType type, String key, Object value) {
        this(type, key, null, value, null);
    }
    
    public KeyValueOperation(OperationType type, String key, String field, Object value) {
        this(type, key, field, value, null);
    }
    
    public KeyValueOperation(OperationType type, String key, String field, Object value, Object additionalParam) {
        this.type = type;
        this.key = key;
        this.field = field;
        this.value = value;
        this.additionalParam = additionalParam;
    }
    
    // Static factory methods for common operations
    public static KeyValueOperation get(String key) {
        return new KeyValueOperation(OperationType.GET, key);
    }
    
    public static KeyValueOperation set(String key, Object value) {
        return new KeyValueOperation(OperationType.SET, key, value);
    }
    
    public static KeyValueOperation delete(String key) {
        return new KeyValueOperation(OperationType.DELETE, key);
    }
    
    public static KeyValueOperation increment(String key, String field, Number amount) {
        return new KeyValueOperation(OperationType.INCREMENT, key, field, amount);
    }
    
    public static KeyValueOperation listAppend(String key, String field, Object value) {
        return new KeyValueOperation(OperationType.LIST_APPEND, key, field, value);
    }
    
    public static KeyValueOperation setAdd(String key, String field, Object value) {
        return new KeyValueOperation(OperationType.SET_ADD, key, field, value);
    }
    
    // Getters
    public OperationType getType() { return type; }
    public String getKey() { return key; }
    public String getField() { return field; }
    public Object getValue() { return value; }
    public Object getAdditionalParam() { return additionalParam; }
    
    @Override
    public String toString() {
        return String.format("KeyValueOperation{type=%s, key='%s', field='%s', value=%s}", 
                type, key, field, value);
    }
}