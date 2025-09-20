/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.attr;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;

import java.util.Map;

/**
 * Exception thrown when a requested MetaAttribute cannot be found.
 * Enhanced with structured error reporting capabilities.
 * 
 * @deprecated As of 5.2.0, use {@link com.draagon.meta.MetaDataNotFoundException#forAttribute(String, MetaData)} instead.
 *             This class is redundant and will be removed in a future version.
 *             The consolidated exception hierarchy provides the same functionality with better consistency.
 * 
 * @since 1.0 (enhanced in 5.2.0, deprecated in 5.2.0)
 */
@Deprecated(since = "5.2.0", forRemoval = true)
@SuppressWarnings("serial")
public class MetaAttributeNotFoundException extends MetaDataNotFoundException {

    /**
     * Creates a MetaAttributeNotFoundException with a message and attribute name.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param name the name of the attribute that was not found
     */
    public MetaAttributeNotFoundException(String msg, String name) {
        super(msg, name);
    }

    /**
     * Creates a MetaAttributeNotFoundException with enhanced context information.
     * 
     * @param attributeName the name of the attribute that was not found
     * @param parent the parent MetaData object where the attribute was expected
     */
    public MetaAttributeNotFoundException(String attributeName, MetaData parent) {
        super("attribute", attributeName, parent);
    }

    /**
     * Creates a MetaAttributeNotFoundException with full context information.
     * 
     * @param attributeName the name of the attribute that was not found
     * @param parent the parent MetaData object where the attribute was expected
     * @param additionalContext additional context information
     */
    public MetaAttributeNotFoundException(String attributeName, MetaData parent, Map<String, Object> additionalContext) {
        super("attribute", attributeName, parent, additionalContext);
    }

    /**
     * Factory method for creating an attribute not found exception with enhanced error reporting.
     * 
     * @param attributeName the name of the attribute that was not found
     * @param parent the parent MetaData object where the attribute was expected
     * @return a configured MetaAttributeNotFoundException
     */
    public static MetaAttributeNotFoundException create(String attributeName, MetaData parent) {
        return new MetaAttributeNotFoundException(attributeName, parent);
    }

    /**
     * Factory method for creating an attribute not found exception with additional context.
     * 
     * @param attributeName the name of the attribute that was not found
     * @param parent the parent MetaData object where the attribute was expected
     * @param additionalContext additional context information (e.g., expected type, search criteria)
     * @return a configured MetaAttributeNotFoundException
     */
    public static MetaAttributeNotFoundException create(String attributeName, MetaData parent, Map<String, Object> additionalContext) {
        return new MetaAttributeNotFoundException(attributeName, parent, additionalContext);
    }
}

