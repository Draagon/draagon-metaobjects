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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for key-value store operations beyond standard ObjectManager capabilities.
 * Supports Redis, DynamoDB, and similar key-value databases.
 */
public interface KeyValueOperations {

    /**
     * Gets multiple objects by their keys
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the objects
     * @param keys Collection of keys to retrieve
     * @return Map of key to object
     */
    Map<String, Object> getMultiple(ObjectConnection c, MetaObject mc, Collection<String> keys) throws MetaDataException;

    /**
     * Sets multiple key-value pairs atomically
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the objects
     * @param objects Map of key to object
     * @param ttl Optional time-to-live for all objects
     */
    void setMultiple(ObjectConnection c, MetaObject mc, Map<String, Object> objects, Duration ttl) throws MetaDataException;

    /**
     * Sets an object with time-to-live
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param obj Object to store
     * @param ttl Time-to-live duration
     */
    void setWithTTL(ObjectConnection c, MetaObject mc, String key, Object obj, Duration ttl) throws MetaDataException;

    /**
     * Sets an object only if the key doesn't exist
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param obj Object to store
     * @return true if the object was set, false if key already existed
     */
    boolean setIfNotExists(ObjectConnection c, MetaObject mc, String key, Object obj) throws MetaDataException;

    /**
     * Atomically increments a numeric field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field Field name to increment
     * @param increment Amount to increment by
     * @return New value after increment
     */
    Number incrementField(ObjectConnection c, MetaObject mc, String key, String field, Number increment) throws MetaDataException;

    /**
     * Atomically decrements a numeric field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field Field name to decrement
     * @param decrement Amount to decrement by
     * @return New value after decrement
     */
    Number decrementField(ObjectConnection c, MetaObject mc, String key, String field, Number decrement) throws MetaDataException;

    /**
     * Appends to a list field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field List field name
     * @param values Values to append
     * @return New length of the list
     */
    long appendToList(ObjectConnection c, MetaObject mc, String key, String field, List<?> values) throws MetaDataException;

    /**
     * Removes from a list field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field List field name
     * @param count Number of elements to remove (negative removes from end)
     * @param value Value to remove
     * @return Number of elements removed
     */
    long removeFromList(ObjectConnection c, MetaObject mc, String key, String field, long count, Object value) throws MetaDataException;

    /**
     * Adds to a set field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field Set field name
     * @param values Values to add
     * @return Number of new elements added
     */
    long addToSet(ObjectConnection c, MetaObject mc, String key, String field, Set<?> values) throws MetaDataException;

    /**
     * Removes from a set field
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param field Set field name
     * @param values Values to remove
     * @return Number of elements removed
     */
    long removeFromSet(ObjectConnection c, MetaObject mc, String key, String field, Set<?> values) throws MetaDataException;

    /**
     * Gets keys matching a pattern
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the objects
     * @param pattern Key pattern (supports wildcards)
     * @return Collection of matching keys
     */
    Collection<String> getKeysByPattern(ObjectConnection c, MetaObject mc, String pattern) throws MetaDataException;

    /**
     * Checks if keys exist
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the objects
     * @param keys Keys to check
     * @return Number of keys that exist
     */
    long exists(ObjectConnection c, MetaObject mc, Collection<String> keys) throws MetaDataException;

    /**
     * Sets expiration for an existing key
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param ttl Time-to-live duration
     * @return true if expiration was set, false if key doesn't exist
     */
    boolean expire(ObjectConnection c, MetaObject mc, String key, Duration ttl) throws MetaDataException;

    /**
     * Gets time-to-live for a key
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @return Time-to-live duration, null if no expiration set
     */
    Duration getTimeToLive(ObjectConnection c, MetaObject mc, String key) throws MetaDataException;

    /**
     * Executes a transaction (pipeline) atomically
     * @param c Connection to the key-value store
     * @param operations List of operations to execute atomically
     * @return Results of the operations
     */
    List<Object> executeTransaction(ObjectConnection c, List<KeyValueOperation> operations) throws MetaDataException;

    /**
     * Asynchronously gets an object
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @return CompletableFuture of the object
     */
    CompletableFuture<Object> getAsync(ObjectConnection c, MetaObject mc, String key) throws MetaDataException;

    /**
     * Asynchronously sets an object
     * @param c Connection to the key-value store
     * @param mc MetaObject describing the object
     * @param key Object key
     * @param obj Object to store
     * @return CompletableFuture that completes when operation finishes
     */
    CompletableFuture<Void> setAsync(ObjectConnection c, MetaObject mc, String key, Object obj) throws MetaDataException;
}