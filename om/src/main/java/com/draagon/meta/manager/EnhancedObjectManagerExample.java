/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating the enhanced ObjectManager features including:
 * - Modern Java patterns and resource management
 * - Event-driven architecture
 * - Asynchronous operations
 * - Query builder pattern
 * - Bulk operations
 */
public class EnhancedObjectManagerExample {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedObjectManagerExample.class);
    
    private final ObjectManager objectManager;
    
    public EnhancedObjectManagerExample(ObjectManager objectManager) {
        this.objectManager = objectManager;
        setupEventListeners();
    }
    
    /**
     * Demonstrates event-driven architecture
     */
    private void setupEventListeners() {
        objectManager.addPersistenceEventListener(new PersistenceEventListener() {
            @Override
            public void onBeforeCreate(MetaObject mc, Object obj) {
                log.info("About to create object of type: {}", mc.getName());
            }
            
            @Override
            public void onAfterCreate(MetaObject mc, Object obj) {
                log.info("Successfully created object of type: {}", mc.getName());
            }
            
            @Override
            public void onBeforeUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
                log.info("About to update {} fields in object of type: {}", 
                        modifiedFields.size(), mc.getName());
            }
            
            @Override
            public void onError(MetaObject mc, Object obj, String operation, Exception error) {
                log.error("Error during {} operation on {}: {}", operation, mc.getName(), error.getMessage());
            }
        });
    }
    
    /**
     * Demonstrates modern resource management with try-with-resources
     */
    public void demonstrateResourceManagement(Object obj) throws MetaDataException {
        try (ObjectConnection connection = objectManager.getConnection()) {
            // Connection is automatically closed
            objectManager.createObject(connection, obj);
            connection.commit();
        } // Connection closed here automatically
    }
    
    /**
     * Demonstrates the enhanced query builder pattern
     */
    public Collection<?> demonstrateQueryBuilder(String className) throws MetaDataException {
        return objectManager.query(className)
                .where("status", "ACTIVE")
                .and("createdDate", System.currentTimeMillis())
                .orderByDesc("createdDate")
                .limit(100)
                .execute();
    }
    
    /**
     * Demonstrates asynchronous operations
     */
    public void demonstrateAsyncOperations(String className, Object newObj) {
        MetaObject mc;
        try {
            mc = MetaDataUtil.findMetaObjectByName(className, this);
        } catch (Exception e) {
            log.error("Could not find MetaObject for class: {}", className, e);
            return;
        }
        
        // Asynchronous query
        CompletableFuture<Collection<?>> queryFuture = objectManager.getObjectsAsync(mc);
        
        // Asynchronous create
        CompletableFuture<Void> createFuture = objectManager.createObjectAsync(newObj);
        
        // Chain operations
        CompletableFuture<Void> chainedOperation = queryFuture
                .thenAccept(results -> log.info("Found {} objects", results.size()))
                .thenCompose(v -> createFuture)
                .thenRun(() -> log.info("Object created successfully"));
        
        // Handle completion
        chainedOperation
                .orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Async operation failed", error);
                    } else {
                        log.info("Async operation completed successfully");
                    }
                });
    }
    
    /**
     * Demonstrates enhanced bulk operations
     */
    public void demonstrateBulkOperations(List<Object> objects) throws MetaDataException {
        log.info("Processing {} objects in bulk", objects.size());
        
        try (ObjectConnection connection = objectManager.getConnection()) {
            connection.beginTransaction();
            
            try {
                // Bulk create - automatically groups by type and uses optimized operations
                objectManager.createObjects(connection, objects);
                
                // Commit transaction
                connection.endTransaction(true);
                log.info("Bulk operation completed successfully");
                
            } catch (Exception e) {
                connection.endTransaction(false); // Rollback
                throw e;
            }
        }
    }
    
    /**
     * Demonstrates advanced query building with complex expressions
     */
    public Collection<?> demonstrateAdvancedQueries(String className) throws MetaDataException {
        return objectManager.query(className)
                .where("status", "ACTIVE")
                .and("priority", "HIGH")
                .or(new Expression("urgent", true))
                .orderByAsc("createdDate")
                .orderByDesc("priority")
                .limit(50, 100) // Results 50-100
                .distinct()
                .execute();
    }
    
    /**
     * Demonstrates first() method for single object retrieval
     */
    public Object findActiveUser(String username) throws MetaDataException {
        return objectManager.query("User")
                .where("username", username)
                .and("status", "ACTIVE")
                .first(); // Returns first matching object or null
    }
    
    /**
     * Demonstrates async query with first result
     */
    public CompletableFuture<Object> findActiveUserAsync(String username) {
        return objectManager.query("User")
                .where("username", username)
                .and("status", "ACTIVE")
                .firstAsync();
    }
    
    /**
     * Demonstrates counting with complex expressions
     */
    public long countActiveUsers() throws MetaDataException {
        return objectManager.query("User")
                .where("status", "ACTIVE")
                .count();
    }
    
    /**
     * Demonstrates transaction management
     */
    public void demonstrateTransactionManagement(List<Object> objects) throws MetaDataException {
        try (ObjectConnection connection = objectManager.getConnection()) {
            connection.beginTransaction();
            
            boolean success = false;
            try {
                // Multiple operations in a transaction
                for (Object obj : objects) {
                    objectManager.createObject(connection, obj);
                }
                
                // Business logic validation
                if (validateBusinessRules(objects)) {
                    success = true;
                }
            } finally {
                connection.endTransaction(success);
            }
        }
    }
    
    private boolean validateBusinessRules(List<Object> objects) {
        // Business validation logic
        return true;
    }
    
    /**
     * Demonstrates error handling with events
     */
    public void demonstrateErrorHandling(Object invalidObject) {
        try {
            objectManager.createObjectAsync(invalidObject)
                    .exceptionally(error -> {
                        log.error("Async create failed", error);
                        // Error events are automatically fired
                        return null;
                    });
        } catch (Exception e) {
            log.error("Unexpected error", e);
        }
    }
}