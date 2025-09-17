/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.*;
import com.draagon.meta.util.ErrorFormatter;

import java.util.Map;
import java.util.HashMap;

/**
 * Exception thrown when a requested object cannot be found in the object manager.
 * Enhanced with structured error reporting capabilities while preserving object context.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
@SuppressWarnings("serial")
public class ObjectNotFoundException extends MetaDataException {

    private final Object object;

    /**
     * Creates an ObjectNotFoundException with a simple message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public ObjectNotFoundException(String msg) {
        super(msg);
        this.object = null;
    }

    /**
     * Creates an ObjectNotFoundException for a specific object.
     * Backward compatible constructor.
     * 
     * @param o the object that was not found
     */
    public ObjectNotFoundException(Object o) {
        super("Object Not Found [" + o + "]");
        this.object = o;
    }

    /**
     * Creates an ObjectNotFoundException with enhanced context information.
     * 
     * @param message the error message
     * @param targetObject the object that was not found
     * @param source the MetaData object providing context (e.g., MetaObject definition)
     * @param operation the operation being performed when the error occurred
     */
    public ObjectNotFoundException(String message, Object targetObject, MetaData source, String operation) {
        super(message, source, operation, null, buildObjectContext(targetObject));
        this.object = targetObject;
    }

    /**
     * Creates an ObjectNotFoundException with full context information.
     * 
     * @param message the error message
     * @param targetObject the object that was not found
     * @param source the MetaData object providing context
     * @param operation the operation being performed
     * @param additionalContext additional context information
     */
    public ObjectNotFoundException(String message, Object targetObject, MetaData source, String operation, 
                                 Map<String, Object> additionalContext) {
        super(message, source, operation, null, mergeObjectContext(targetObject, additionalContext));
        this.object = targetObject;
    }

    /**
     * Factory method for creating an object not found exception with enhanced error reporting.
     * 
     * @param targetObject the object that was not found
     * @param source the MetaData object providing context
     * @param operation the operation being performed
     * @return a configured ObjectNotFoundException
     */
    public static ObjectNotFoundException create(Object targetObject, MetaData source, String operation) {
        String message = String.format("Object not found during %s: %s", operation, 
                                      targetObject != null ? targetObject.toString() : "<null>");
        return new ObjectNotFoundException(message, targetObject, source, operation);
    }

    /**
     * Factory method for creating an object not found exception with query context.
     * 
     * @param searchCriteria the search criteria that produced no results
     * @param source the MetaData object providing context
     * @param operation the operation being performed
     * @return a configured ObjectNotFoundException
     */
    public static ObjectNotFoundException forQuery(Map<String, Object> searchCriteria, MetaData source, String operation) {
        String message = ErrorFormatter.formatGenericError(source, operation, "No objects found matching criteria", searchCriteria);
        return new ObjectNotFoundException(message, searchCriteria, source, operation, 
                                         Map.of("searchType", "query", "resultCount", 0));
    }

    /**
     * Factory method for creating an object not found exception by ID.
     * 
     * @param objectId the ID of the object that was not found
     * @param source the MetaData object providing context
     * @param operation the operation being performed
     * @return a configured ObjectNotFoundException
     */
    public static ObjectNotFoundException forId(Object objectId, MetaData source, String operation) {
        String message = String.format("Object with ID '%s' not found during %s", objectId, operation);
        return new ObjectNotFoundException(message, objectId, source, operation,
                                         Map.of("searchType", "byId", "objectId", objectId));
    }

    /**
     * Factory method for creating an object not found exception by reference.
     * 
     * @param objectRef the reference of the object that was not found
     * @param source the MetaData object providing context
     * @param operation the operation being performed
     * @return a configured ObjectNotFoundException
     */
    public static ObjectNotFoundException forRef(String objectRef, MetaData source, String operation) {
        String message = String.format("Object with reference '%s' not found during %s", objectRef, operation);
        return new ObjectNotFoundException(message, objectRef, source, operation,
                                         Map.of("searchType", "byRef", "objectRef", objectRef));
    }

    /**
     * Returns the object that was not found.
     * 
     * @return the object that was not found, or null if not applicable
     */
    public Object getObject() {
        return object;
    }

    /**
     * Builds context information for the target object.
     * 
     * @param targetObject the object to build context for
     * @return context map with object information
     */
    private static Map<String, Object> buildObjectContext(Object targetObject) {
        Map<String, Object> context = new HashMap<>();
        if (targetObject != null) {
            context.put("objectClass", targetObject.getClass().getName());
            context.put("objectType", targetObject.getClass().getSimpleName());
            context.put("objectString", targetObject.toString());
            context.put("objectHashCode", targetObject.hashCode());
        } else {
            context.put("objectClass", "<null>");
            context.put("objectType", "<null>");
            context.put("objectString", "<null>");
        }
        return context;
    }

    /**
     * Merges object context with additional context.
     * 
     * @param targetObject the object to build context for
     * @param additionalContext additional context to merge
     * @return merged context map
     */
    private static Map<String, Object> mergeObjectContext(Object targetObject, Map<String, Object> additionalContext) {
        Map<String, Object> merged = new HashMap<>(buildObjectContext(targetObject));
        if (additionalContext != null) {
            merged.putAll(additionalContext);
        }
        return merged;
    }

    /**
     * Enhanced toString that includes object context when available.
     */
    @Override
    public String toString() {
        if (object == null) {
            return super.toString();
        } else {
            return "[" + object.toString() + "] " + super.toString();
        }
    }
}

