package com.draagon.meta.manager.db;

import com.draagon.meta.MetaData;
import com.draagon.meta.manager.PersistenceException;
import com.draagon.meta.util.ErrorFormatter;

import java.util.Map;
import java.util.HashMap;

/**
 * Exception thrown when a dirty write conflict is detected during database operations.
 * Enhanced with structured error reporting capabilities while preserving object context.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class DirtyWriteException extends PersistenceException {
	
	private static final long serialVersionUID = 103419229085271187L;
	
	private final Object object;
	
    /**
     * Creates a DirtyWriteException with default message.
     * Backward compatible constructor.
     */
    public DirtyWriteException() {
        super("DirtyWriteException");
        this.object = null;
    }

    /**
     * Creates a DirtyWriteException with a custom message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public DirtyWriteException(String msg) {
        super(msg);
        this.object = null;
    }

    /**
     * Creates a DirtyWriteException for a specific object.
     * Backward compatible constructor.
     * 
     * @param o the object that caused the dirty write conflict
     */
    public DirtyWriteException(Object o) {
        super("DirtyWriteException");
    	this.object = o;
    }

    /**
     * Creates a DirtyWriteException with enhanced context information.
     * 
     * @param message the error message
     * @param conflictObject the object that caused the dirty write conflict
     * @param source the MetaData object providing context (e.g., MetaObject definition)
     * @param operation the database operation being performed when the conflict occurred
     */
    public DirtyWriteException(String message, Object conflictObject, MetaData source, String operation) {
        super(message, source, operation, null, buildConflictContext(conflictObject));
        this.object = conflictObject;
    }

    /**
     * Creates a DirtyWriteException with full context information.
     * 
     * @param message the error message
     * @param conflictObject the object that caused the dirty write conflict
     * @param source the MetaData object providing context
     * @param operation the database operation being performed
     * @param additionalContext additional context information
     */
    public DirtyWriteException(String message, Object conflictObject, MetaData source, String operation, 
                             Map<String, Object> additionalContext) {
        super(message, source, operation, null, mergeConflictContext(conflictObject, additionalContext));
        this.object = conflictObject;
    }

    /**
     * Factory method for creating a dirty write exception with enhanced error reporting.
     * 
     * @param conflictObject the object that caused the dirty write conflict
     * @param source the MetaData object providing context
     * @param operation the database operation being performed
     * @return a configured DirtyWriteException
     */
    public static DirtyWriteException create(Object conflictObject, MetaData source, String operation) {
        String message = ErrorFormatter.formatGenericError(source, operation, 
                                                          "Dirty write conflict detected",
                                                          Map.of("objectType", conflictObject.getClass().getSimpleName()));
        return new DirtyWriteException(message, conflictObject, source, operation);
    }

    /**
     * Factory method for creating a version conflict exception.
     * 
     * @param conflictObject the object with version conflict
     * @param expectedVersion the expected version number
     * @param actualVersion the actual version number found
     * @param source the MetaData object providing context
     * @return a configured DirtyWriteException
     */
    public static DirtyWriteException forVersionConflict(Object conflictObject, int expectedVersion, int actualVersion, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "versionCheck", 
                                                          "Version conflict during update",
                                                          Map.of("expectedVersion", expectedVersion, "actualVersion", actualVersion));
        
        Map<String, Object> context = Map.of(
            "conflictType", "version",
            "expectedVersion", expectedVersion,
            "actualVersion", actualVersion,
            "objectId", getObjectId(conflictObject)
        );
        
        return new DirtyWriteException(message, conflictObject, source, "versionCheck", context);
    }

    /**
     * Factory method for creating a timestamp conflict exception.
     * 
     * @param conflictObject the object with timestamp conflict
     * @param expectedTimestamp the expected last modified timestamp
     * @param actualTimestamp the actual last modified timestamp found
     * @param source the MetaData object providing context
     * @return a configured DirtyWriteException
     */
    public static DirtyWriteException forTimestampConflict(Object conflictObject, long expectedTimestamp, long actualTimestamp, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "timestampCheck", 
                                                          "Timestamp conflict during update",
                                                          Map.of("expectedTimestamp", expectedTimestamp, "actualTimestamp", actualTimestamp));
        
        Map<String, Object> context = Map.of(
            "conflictType", "timestamp",
            "expectedTimestamp", expectedTimestamp,
            "actualTimestamp", actualTimestamp,
            "timeDifference", actualTimestamp - expectedTimestamp,
            "objectId", getObjectId(conflictObject)
        );
        
        return new DirtyWriteException(message, conflictObject, source, "timestampCheck", context);
    }

    /**
     * Factory method for creating a concurrent modification exception.
     * 
     * @param conflictObject the object being modified concurrently
     * @param source the MetaData object providing context
     * @param conflictDetails details about the concurrent modification
     * @return a configured DirtyWriteException
     */
    public static DirtyWriteException forConcurrentModification(Object conflictObject, MetaData source, Map<String, Object> conflictDetails) {
        String message = ErrorFormatter.formatGenericError(source, "concurrentUpdate", 
                                                          "Concurrent modification detected",
                                                          conflictDetails);
        
        Map<String, Object> context = new HashMap<>(conflictDetails);
        context.put("conflictType", "concurrent");
        context.put("objectId", getObjectId(conflictObject));
        
        return new DirtyWriteException(message, conflictObject, source, "concurrentUpdate", context);
    }
        
    /**
     * Returns the object that caused the dirty write conflict.
     * 
     * @return the object that caused the conflict, or null if not applicable
     */
    public Object getObject() {
  	    return object;
    }

    /**
     * Builds context information for the conflict object.
     * 
     * @param conflictObject the object to build context for
     * @return context map with object information
     */
    private static Map<String, Object> buildConflictContext(Object conflictObject) {
        Map<String, Object> context = new HashMap<>();
        if (conflictObject != null) {
            context.put("conflictObjectClass", conflictObject.getClass().getName());
            context.put("conflictObjectType", conflictObject.getClass().getSimpleName());
            context.put("conflictObjectString", conflictObject.toString());
            context.put("conflictObjectId", getObjectId(conflictObject));
            context.put("conflictType", "dirtyWrite");
        } else {
            context.put("conflictObjectClass", "<null>");
            context.put("conflictObjectType", "<null>");
            context.put("conflictObjectString", "<null>");
            context.put("conflictType", "dirtyWrite");
        }
        return context;
    }

    /**
     * Merges conflict context with additional context.
     * 
     * @param conflictObject the object to build context for
     * @param additionalContext additional context to merge
     * @return merged context map
     */
    private static Map<String, Object> mergeConflictContext(Object conflictObject, Map<String, Object> additionalContext) {
        Map<String, Object> merged = new HashMap<>(buildConflictContext(conflictObject));
        if (additionalContext != null) {
            merged.putAll(additionalContext);
        }
        return merged;
    }

    /**
     * Attempts to extract an ID from the object for context.
     * 
     * @param obj the object to extract ID from
     * @return the object ID as a string, or "unknown" if not extractable
     */
    private static String getObjectId(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // Try common ID field names via reflection (simplified)
        try {
            // This is a simplified approach - in real implementation you might use
            // more sophisticated reflection or metadata-driven ID extraction
            return obj.hashCode() + "@" + obj.getClass().getSimpleName();
        } catch (Exception e) {
            return "unknown";
        }
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
