package com.draagon.meta.manager;

import com.draagon.meta.MetaData;
import com.draagon.meta.util.ErrorFormatter;
import com.draagon.meta.util.MetaDataPath;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Exception thrown when persistence operations fail in the object manager.
 * Enhanced with structured error reporting capabilities while remaining a RuntimeException.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class PersistenceException extends RuntimeException {

    private final MetaDataPath metaDataPath;
    private final String operation;
    private final Map<String, Object> context;
    private final long timestamp;
    private final String threadName;

    /**
     * Creates a PersistenceException with a simple message.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     */
    public PersistenceException(String msg) {
        this(msg, null, null, null, Collections.emptyMap());
    }

    /**
     * Creates a PersistenceException with a message and cause.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param cause the underlying cause
     */
    public PersistenceException(String msg, Throwable cause) {
        this(msg, null, null, cause, Collections.emptyMap());
    }

    /**
     * Creates a PersistenceException with enhanced context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context (e.g., MetaObject definition)
     * @param operation the persistence operation being performed when the error occurred
     */
    public PersistenceException(String message, MetaData source, String operation) {
        this(message, source, operation, null, Collections.emptyMap());
    }

    /**
     * Creates a PersistenceException with full context information.
     * 
     * @param message the error message
     * @param source the MetaData object providing context
     * @param operation the persistence operation being performed
     * @param cause the underlying cause (may be null)
     * @param additionalContext additional context information (may be empty)
     */
    public PersistenceException(String message, MetaData source, String operation,
                              Throwable cause, Map<String, Object> additionalContext) {
        super(buildEnhancedMessage(message, source, operation, additionalContext), cause);

        this.metaDataPath = source != null ? MetaDataPath.buildPath(source) : null;
        this.operation = operation;
        this.context = new LinkedHashMap<>(additionalContext != null ? additionalContext : Collections.emptyMap());
        this.timestamp = System.currentTimeMillis();
        this.threadName = Thread.currentThread().getName();

        // Add automatic context if source is available
        if (source != null) {
            this.context.put("sourceClass", source.getClass().getSimpleName());
            this.context.put("sourceType", source.getTypeName());
            this.context.put("sourceSubType", source.getSubTypeName());
            this.context.put("sourceName", source.getName());
        }
    }

    /**
     * Factory method for creating a persistence save error.
     * 
     * @param targetObject the object that failed to save
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured PersistenceException
     */
    public static PersistenceException forSave(Object targetObject, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "save", "Failed to save object", 
                                                          Map.of("objectType", targetObject.getClass().getSimpleName()));
        return new PersistenceException(message, source, "save", cause, 
                                      Map.of("targetObject", targetObject.toString(), 
                                            "targetClass", targetObject.getClass().getName()));
    }

    /**
     * Factory method for creating a persistence update error.
     * 
     * @param targetObject the object that failed to update
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured PersistenceException
     */
    public static PersistenceException forUpdate(Object targetObject, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "update", "Failed to update object", 
                                                          Map.of("objectType", targetObject.getClass().getSimpleName()));
        return new PersistenceException(message, source, "update", cause,
                                      Map.of("targetObject", targetObject.toString(),
                                            "targetClass", targetObject.getClass().getName()));
    }

    /**
     * Factory method for creating a persistence delete error.
     * 
     * @param targetObject the object that failed to delete
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured PersistenceException
     */
    public static PersistenceException forDelete(Object targetObject, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "delete", "Failed to delete object", 
                                                          Map.of("objectType", targetObject.getClass().getSimpleName()));
        return new PersistenceException(message, source, "delete", cause,
                                      Map.of("targetObject", targetObject.toString(),
                                            "targetClass", targetObject.getClass().getName()));
    }

    /**
     * Factory method for creating a persistence transaction error.
     * 
     * @param transactionInfo information about the failed transaction
     * @param source the MetaData object providing context
     * @param cause the underlying cause
     * @return a configured PersistenceException
     */
    public static PersistenceException forTransaction(String transactionInfo, MetaData source, Throwable cause) {
        String message = ErrorFormatter.formatGenericError(source, "transaction", "Transaction failed", 
                                                          Map.of("transactionInfo", transactionInfo));
        return new PersistenceException(message, source, "transaction", cause,
                                      Map.of("transactionDetails", transactionInfo));
    }

    /**
     * Factory method for creating a persistence validation error.
     * 
     * @param validationErrors the validation errors that occurred
     * @param source the MetaData object providing context
     * @return a configured PersistenceException
     */
    public static PersistenceException forValidation(Map<String, String> validationErrors, MetaData source) {
        String message = ErrorFormatter.formatGenericError(source, "validation", "Object validation failed", 
                                                          new java.util.HashMap<String, Object>(validationErrors));
        return new PersistenceException(message, source, "validation", null,
                                      Map.of("validationErrors", validationErrors, 
                                            "errorCount", validationErrors.size()));
    }

    /**
     * Builds an enhanced error message with context information.
     */
    private static String buildEnhancedMessage(String message, MetaData source,
                                             String operation, Map<String, Object> context) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append(message);

        // Only add details if we have enhanced context
        if (source != null || operation != null || (context != null && !context.isEmpty())) {
            enhanced.append("\n\n--- Persistence Error Details ---");

            if (source != null) {
                enhanced.append("\nPath: ").append(MetaDataPath.buildPath(source).toHierarchicalString());
            }

            if (operation != null) {
                enhanced.append("\nOperation: ").append(operation);
            }

            enhanced.append("\nThread: ").append(Thread.currentThread().getName());
            enhanced.append("\nTimestamp: ").append(Instant.ofEpochMilli(System.currentTimeMillis()));

            if (context != null && !context.isEmpty()) {
                enhanced.append("\nContext:");
                context.forEach((key, value) ->
                    enhanced.append("\n  ").append(key).append(": ").append(value));
            }
        }

        return enhanced.toString();
    }

    /**
     * Returns the hierarchical path to the MetaData object where this error occurred.
     * 
     * @return Optional containing the MetaDataPath, or empty if no source was provided
     */
    public Optional<MetaDataPath> getMetaDataPath() {
        return Optional.ofNullable(metaDataPath);
    }

    /**
     * Returns the persistence operation being performed when this error occurred.
     * 
     * @return Optional containing the operation name, or empty if no operation was provided
     */
    public Optional<String> getOperation() {
        return Optional.ofNullable(operation);
    }

    /**
     * Returns the context information associated with this error.
     * 
     * @return unmodifiable map of context information
     */
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    /**
     * Returns the timestamp when this error occurred.
     * 
     * @return timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the name of the thread where this error occurred.
     * 
     * @return thread name
     */
    public String getThreadName() {
        return threadName;
    }

    /**
     * Returns a specific context value by key.
     * 
     * @param key the context key
     * @return Optional containing the value, or empty if not found
     */
    public Optional<Object> getContextValue(String key) {
        return Optional.ofNullable(context.get(key));
    }

    /**
     * Checks if this exception has enhanced context information.
     * 
     * @return true if this exception has enhanced context (path, operation, or additional context)
     */
    public boolean hasEnhancedContext() {
        return metaDataPath != null || operation != null || !context.isEmpty();
    }
}
