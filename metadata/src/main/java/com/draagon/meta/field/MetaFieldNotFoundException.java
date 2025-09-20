/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.field;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;

import java.util.Map;

/**
 * Exception thrown when a requested MetaField cannot be found.
 * Enhanced with structured error reporting capabilities.
 * 
 * @deprecated As of 5.2.0, use {@link com.draagon.meta.MetaDataNotFoundException#forField(String, MetaData)} instead.
 *             This class is redundant and will be removed in a future version.
 *             The consolidated exception hierarchy provides the same functionality with better consistency.
 * 
 * @since 1.0 (enhanced in 5.2.0, deprecated in 5.2.0)
 */
@Deprecated(since = "5.2.0", forRemoval = true)
@SuppressWarnings("serial")
public class MetaFieldNotFoundException extends MetaDataNotFoundException {

    /**
     * Creates a MetaFieldNotFoundException with a message and field name.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param name the name of the field that was not found
     */
    public MetaFieldNotFoundException(String msg, String name) {
        super(msg, name);
    }

    /**
     * Creates a MetaFieldNotFoundException with enhanced context information.
     * 
     * @param fieldName the name of the field that was not found
     * @param parent the parent MetaData object where the field was expected
     */
    public MetaFieldNotFoundException(String fieldName, MetaData parent) {
        super("field", fieldName, parent);
    }

    /**
     * Creates a MetaFieldNotFoundException with full context information.
     * 
     * @param fieldName the name of the field that was not found
     * @param parent the parent MetaData object where the field was expected
     * @param additionalContext additional context information
     */
    public MetaFieldNotFoundException(String fieldName, MetaData parent, Map<String, Object> additionalContext) {
        super("field", fieldName, parent, additionalContext);
    }

    /**
     * Factory method for creating a field not found exception with enhanced error reporting.
     * 
     * @param fieldName the name of the field that was not found
     * @param parent the parent MetaData object where the field was expected
     * @return a configured MetaFieldNotFoundException
     */
    public static MetaFieldNotFoundException create(String fieldName, MetaData parent) {
        return new MetaFieldNotFoundException(fieldName, parent);
    }

    /**
     * Factory method for creating a field not found exception with additional context.
     * 
     * @param fieldName the name of the field that was not found
     * @param parent the parent MetaData object where the field was expected
     * @param additionalContext additional context information (e.g., expected type, search criteria)
     * @return a configured MetaFieldNotFoundException
     */
    public static MetaFieldNotFoundException create(String fieldName, MetaData parent, Map<String, Object> additionalContext) {
        return new MetaFieldNotFoundException(fieldName, parent, additionalContext);
    }
}

