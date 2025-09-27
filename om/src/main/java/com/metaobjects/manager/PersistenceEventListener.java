/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager;

import com.metaobjects.field.MetaField;
import com.metaobjects.object.MetaObject;

import java.util.Collection;

/**
 * Event listener interface for persistence operations providing hooks for custom logic
 * to be executed before and after persistence operations on MetaObjects.
 * 
 * <p>The event system enables powerful cross-cutting concerns such as auditing, validation,
 * caching, security, and business rule enforcement. Events are fired synchronously within
 * the same transaction context as the persistence operation.</p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Audit logging implementation
 * public class AuditEventListener implements PersistenceEventListener {
 *     private final AuditLog auditLog;
 *     
 *     @Override
 *     public void onAfterCreate(MetaObject mc, Object obj) {
 *         auditLog.logCreate(mc.getName(), getObjectId(obj), getCurrentUser());
 *     }
 *     
 *     @Override
 *     public void onAfterUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
 *         String changes = modifiedFields.stream()
 *             .map(field -> field.getName() + "=" + field.getObject(obj))
 *             .collect(Collectors.joining(", "));
 *         auditLog.logUpdate(mc.getName(), getObjectId(obj), changes, getCurrentUser());
 *     }
 *     
 *     @Override
 *     public void onAfterDelete(MetaObject mc, Object obj) {
 *         auditLog.logDelete(mc.getName(), getObjectId(obj), getCurrentUser());
 *     }
 * }
 * 
 * // Cache invalidation implementation
 * public class CacheInvalidationListener implements PersistenceEventListener {
 *     private final CacheManager cacheManager;
 *     
 *     @Override
 *     public void onAfterCreate(MetaObject mc, Object obj) {
 *         invalidateRelatedCaches(mc, obj);
 *     }
 *     
 *     @Override
 *     public void onAfterUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
 *         // Only invalidate if specific fields changed
 *         if (modifiedFields.stream().anyMatch(field -> field.hasMetaAttr("cacheable"))) {
 *             invalidateRelatedCaches(mc, obj);
 *         }
 *     }
 *     
 *     private void invalidateRelatedCaches(MetaObject mc, Object obj) {
 *         cacheManager.evict(mc.getName() + ":" + getObjectId(obj));
 *         cacheManager.evictPattern(mc.getName() + ":*");
 *     }
 * }
 * 
 * // Business rule validation
 * public class BusinessRuleListener implements PersistenceEventListener {
 *     @Override
 *     public void onBeforeCreate(MetaObject mc, Object obj) {
 *         if ("Order".equals(mc.getName())) {
 *             validateOrderBusinessRules(obj);
 *         }
 *     }
 *     
 *     @Override
 *     public void onBeforeUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
 *         if ("User".equals(mc.getName()) && 
 *             modifiedFields.stream().anyMatch(f -> "email".equals(f.getName()))) {
 *             validateEmailUniqueness(obj);
 *         }
 *     }
 * }
 * 
 * // Registration with ObjectManager
 * objectManager.addPersistenceEventListener(new AuditEventListener(auditLog));
 * objectManager.addPersistenceEventListener(new CacheInvalidationListener(cacheManager));
 * objectManager.addPersistenceEventListener(new BusinessRuleListener());
 * }</pre>
 * 
 * <h3>Event Timing and Transaction Context:</h3>
 * <ul>
 *   <li><strong>Before Events</strong>: Called within the same transaction, allowing validation 
 *       and business rule enforcement that can prevent the operation</li>
 *   <li><strong>After Events</strong>: Called after successful persistence, ideal for auditing,
 *       cache invalidation, and notifications</li>
 *   <li><strong>Error Events</strong>: Called when persistence operations fail, useful for
 *       error reporting and cleanup</li>
 * </ul>
 * 
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Keep event handlers fast to avoid performance impact</li>
 *   <li>Use before events for validation and business rules</li>
 *   <li>Use after events for auditing and side effects</li>
 *   <li>Handle exceptions gracefully in event handlers</li>
 *   <li>Consider async processing for non-critical operations</li>
 *   <li>Use the modifiedFields parameter to optimize update handlers</li>
 * </ul>
 * 
 * <h3>Thread Safety:</h3>
 * <p>Event listeners must be thread-safe as they may be called concurrently
 * from multiple threads. The ObjectManager synchronizes access to the listener
 * list but not individual listener method calls.</p>
 * 
 * @see ObjectManager#addPersistenceEventListener(PersistenceEventListener)
 * @see ObjectManager#removePersistenceEventListener(PersistenceEventListener)
 */
public interface PersistenceEventListener {
    
    /**
     * Called before an object is created
     * @param mc MetaObject describing the object
     * @param obj The object being created
     */
    default void onBeforeCreate(MetaObject mc, Object obj) {
        // Default empty implementation
    }
    
    /**
     * Called after an object is created successfully
     * @param mc MetaObject describing the object
     * @param obj The object that was created
     */
    default void onAfterCreate(MetaObject mc, Object obj) {
        // Default empty implementation
    }
    
    /**
     * Called before an object is updated
     * @param mc MetaObject describing the object
     * @param obj The object being updated
     * @param modifiedFields Collection of fields that were modified
     */
    default void onBeforeUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
        // Default empty implementation
    }
    
    /**
     * Called after an object is updated successfully
     * @param mc MetaObject describing the object
     * @param obj The object that was updated
     * @param modifiedFields Collection of fields that were modified
     */
    default void onAfterUpdate(MetaObject mc, Object obj, Collection<MetaField> modifiedFields) {
        // Default empty implementation
    }
    
    /**
     * Called before an object is deleted
     * @param mc MetaObject describing the object
     * @param obj The object being deleted
     */
    default void onBeforeDelete(MetaObject mc, Object obj) {
        // Default empty implementation
    }
    
    /**
     * Called after an object is deleted successfully
     * @param mc MetaObject describing the object
     * @param obj The object that was deleted
     */
    default void onAfterDelete(MetaObject mc, Object obj) {
        // Default empty implementation
    }
    
    /**
     * Called when a persistence error occurs
     * @param mc MetaObject describing the object
     * @param obj The object involved in the error
     * @param operation The operation that failed ("create", "update", "delete", "load")
     * @param error The exception that occurred
     */
    default void onError(MetaObject mc, Object obj, String operation, Exception error) {
        // Default empty implementation
    }
}