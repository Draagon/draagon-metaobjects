/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.view;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;

import java.util.Map;

/**
 * Exception thrown when a requested MetaView cannot be found.
 * Enhanced with structured error reporting capabilities.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class MetaViewNotFoundException extends MetaDataNotFoundException {

    /**
     * Creates a MetaViewNotFoundException with a message and view name.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param name the name of the view that was not found
     */
    public MetaViewNotFoundException(String msg, String name) {
        super(msg, name);
    }

    /**
     * Creates a MetaViewNotFoundException with enhanced context information.
     * 
     * @param viewName the name of the view that was not found
     * @param parent the parent MetaData object where the view was expected
     */
    public MetaViewNotFoundException(String viewName, MetaData parent) {
        super("view", viewName, parent);
    }

    /**
     * Creates a MetaViewNotFoundException with full context information.
     * 
     * @param viewName the name of the view that was not found
     * @param parent the parent MetaData object where the view was expected
     * @param additionalContext additional context information
     */
    public MetaViewNotFoundException(String viewName, MetaData parent, Map<String, Object> additionalContext) {
        super("view", viewName, parent, additionalContext);
    }

    /**
     * Factory method for creating a view not found exception with enhanced error reporting.
     * 
     * @param viewName the name of the view that was not found
     * @param parent the parent MetaData object where the view was expected
     * @return a configured MetaViewNotFoundException
     */
    public static MetaViewNotFoundException create(String viewName, MetaData parent) {
        return new MetaViewNotFoundException(viewName, parent);
    }

    /**
     * Factory method for creating a view not found exception with additional context.
     * 
     * @param viewName the name of the view that was not found
     * @param parent the parent MetaData object where the view was expected
     * @param additionalContext additional context information (e.g., view type, device target)
     * @return a configured MetaViewNotFoundException
     */
    public static MetaViewNotFoundException create(String viewName, MetaData parent, Map<String, Object> additionalContext) {
        return new MetaViewNotFoundException(viewName, parent, additionalContext);
    }
}

