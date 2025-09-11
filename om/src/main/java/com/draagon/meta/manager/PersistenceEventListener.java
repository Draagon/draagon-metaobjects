/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;

import java.util.Collection;

/**
 * Event listener interface for persistence operations.
 * Allows for custom logic to be executed before and after persistence operations.
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