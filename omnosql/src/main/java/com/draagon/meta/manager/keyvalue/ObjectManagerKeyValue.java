/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.keyvalue;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.ObjectManager;
import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract ObjectManager extension for key-value databases.
 * Provides key-value specific functionality while maintaining ObjectManager compatibility.
 */
public abstract class ObjectManagerKeyValue extends ObjectManager implements KeyValueOperations {
    
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerKeyValue.class);

    // Key-Value specific MetaObject attributes
    public static final String ATTR_KEY_PREFIX = "keyPrefix";
    public static final String ATTR_KEY_FIELD = "keyField";
    public static final String ATTR_PARTITION_STRATEGY = "partitionStrategy";
    public static final String ATTR_DEFAULT_TTL = "defaultTTL";
    public static final String ATTR_HASH_TAG = "hashTag";

    /**
     * Gets the key prefix for a MetaObject
     */
    protected String getKeyPrefix(MetaObject mc) {
        String prefix = getPersistenceAttribute(mc, ATTR_KEY_PREFIX);
        return prefix != null ? prefix : mc.getName() + ":";
    }

    /**
     * Gets the field to use as the key
     */
    protected String getKeyField(MetaObject mc) {
        String keyField = getPersistenceAttribute(mc, ATTR_KEY_FIELD);
        return keyField != null ? keyField : "id";
    }

    /**
     * Gets the default TTL for objects of this type
     */
    protected Duration getDefaultTTL(MetaObject mc) {
        String ttlStr = getPersistenceAttribute(mc, ATTR_DEFAULT_TTL);
        return ttlStr != null ? Duration.parse(ttlStr) : null;
    }

    /**
     * Generates a key for an object
     */
    protected String generateKey(MetaObject mc, Object obj) throws MetaDataException {
        String prefix = getKeyPrefix(mc);
        String keyField = getKeyField(mc);
        
        try {
            Object keyValue = mc.getMetaField(keyField).getObject(obj);
            if (keyValue == null) {
                throw new MetaDataException("Key field '" + keyField + "' cannot be null for object: " + obj);
            }
            return prefix + keyValue.toString();
        } catch (Exception e) {
            throw new MetaDataException("Failed to generate key for object: " + obj, e);
        }
    }

    /**
     * Enhanced bulk create operations for key-value stores
     */
    @Override
    protected void createObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (objects.isEmpty()) {
            return;
        }

        log.debug("Creating {} objects in key-value store with prefix: {}", objects.size(), getKeyPrefix(mc));

        try {
            // Build map of key-value pairs
            Map<String, Object> keyValuePairs = new java.util.HashMap<>();
            for (Object obj : objects) {
                String key = generateKey(mc, obj);
                keyValuePairs.put(key, convertToStorageFormat(mc, obj));
            }
            
            // Use bulk set operation
            Duration defaultTTL = getDefaultTTL(mc);
            setMultiple(c, mc, keyValuePairs, defaultTTL);
            
            // Fire post-creation events
            for (Object obj : objects) {
                postPersistence(c, mc, obj, CREATE);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk key-value creation", e);
            throw new MetaDataException("Failed to bulk create key-value objects with prefix: " + getKeyPrefix(mc), e);
        }
    }

    /**
     * Key-value stores typically don't support complex queries, so we override this
     */
    @Override
    public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
        Expression expression = options.getExpression();
        
        if (expression == null) {
            // No filter - this could be expensive for key-value stores
            log.warn("Querying all objects from key-value store - this may be slow");
            return getAllObjectsByPattern(c, mc, getKeyPrefix(mc) + "*");
        }
        
        // Try to extract key-based queries
        if (isKeyBasedQuery(expression)) {
            return getObjectsByKeyExpression(c, mc, expression);
        }
        
        // For non-key-based queries, we need to scan (expensive!)
        log.warn("Non-key-based query on key-value store - scanning all objects");
        return scanAndFilter(c, mc, expression, options);
    }

    /**
     * Override loadObject to use key-value get
     */
    @Override
    public void loadObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        String key = generateKey(mc, obj);
        
        Object storedValue = getByKey(c, mc, key);
        if (storedValue == null) {
            throw new MetaDataException("Object not found with key: " + key);
        }
        
        populateObjectFromStorageFormat(mc, obj, storedValue);
    }

    /**
     * Override createObject to use key-value set
     */
    @Override
    public void createObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, CREATE);
        
        String key = generateKey(mc, obj);
        Object storageValue = convertToStorageFormat(mc, obj);
        Duration ttl = getDefaultTTL(mc);
        
        if (ttl != null) {
            setWithTTL(c, mc, key, storageValue, ttl);
        } else {
            setByKey(c, mc, key, storageValue);
        }
        
        postPersistence(c, mc, obj, CREATE);
    }

    /**
     * Override updateObject to use key-value set
     */
    @Override
    public void updateObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, UPDATE);
        
        String key = generateKey(mc, obj);
        Object storageValue = convertToStorageFormat(mc, obj);
        
        setByKey(c, mc, key, storageValue);
        
        postPersistence(c, mc, obj, UPDATE);
    }

    /**
     * Override deleteObject to use key-value delete
     */
    @Override
    public void deleteObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, DELETE);
        
        String key = generateKey(mc, obj);
        deleteByKey(c, mc, key);
        
        postPersistence(c, mc, obj, DELETE);
    }

    // Abstract methods that implementations must provide
    protected abstract Object getByKey(ObjectConnection c, MetaObject mc, String key) throws MetaDataException;
    protected abstract void setByKey(ObjectConnection c, MetaObject mc, String key, Object value) throws MetaDataException;
    protected abstract void deleteByKey(ObjectConnection c, MetaObject mc, String key) throws MetaDataException;
    protected abstract Collection<?> getAllObjectsByPattern(ObjectConnection c, MetaObject mc, String pattern) throws MetaDataException;
    
    protected abstract Object convertToStorageFormat(MetaObject mc, Object obj) throws MetaDataException;
    protected abstract void populateObjectFromStorageFormat(MetaObject mc, Object obj, Object storedValue) throws MetaDataException;
    
    protected abstract boolean isKeyBasedQuery(Expression expression);
    protected abstract Collection<?> getObjectsByKeyExpression(ObjectConnection c, MetaObject mc, Expression expression) throws MetaDataException;
    protected abstract Collection<?> scanAndFilter(ObjectConnection c, MetaObject mc, Expression expression, QueryOptions options) throws MetaDataException;

    /**
     * Convenience method for getting an object by its key
     */
    public Object getObjectByKey(ObjectConnection c, MetaObject mc, String keyValue) throws MetaDataException {
        String fullKey = getKeyPrefix(mc) + keyValue;
        Object storedValue = getByKey(c, mc, fullKey);
        
        if (storedValue == null) {
            return null;
        }
        
        Object obj = mc.newInstance();
        populateObjectFromStorageFormat(mc, obj, storedValue);
        return obj;
    }

    /**
     * Convenience method for checking if an object exists
     */
    public boolean objectExists(ObjectConnection c, MetaObject mc, String keyValue) throws MetaDataException {
        String fullKey = getKeyPrefix(mc) + keyValue;
        return exists(c, mc, List.of(fullKey)) > 0;
    }

    /**
     * Convenience method for atomic increment operations
     */
    public Number incrementObjectField(ObjectConnection c, Object obj, String fieldName, Number increment) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        String key = generateKey(mc, obj);
        return incrementField(c, mc, key, fieldName, increment);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Key-Value Store]";
    }
}