package com.draagon.meta.constraint;

import com.draagon.meta.MetaDataException;

/**
 * v6.0.0: Exception thrown when parsing constraint definition files fails.
 * Provides detailed error information for debugging constraint configuration issues.
 */
public class ConstraintParseException extends MetaDataException {
    
    private final String sourceName;
    
    public ConstraintParseException(String message) {
        super(message);
        this.sourceName = null;
    }
    
    public ConstraintParseException(String message, String sourceName) {
        super(message);
        this.sourceName = sourceName;
    }
    
    public ConstraintParseException(String message, Throwable cause) {
        super(message, cause);
        this.sourceName = null;
    }
    
    public ConstraintParseException(String message, String sourceName, Throwable cause) {
        super(message, cause);
        this.sourceName = sourceName;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();
        if (sourceName != null) {
            return baseMessage + " (source: " + sourceName + ")";
        }
        return baseMessage;
    }
}