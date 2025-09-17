/*
 * Copyright (c) 2003-2012 Blue Gnosis, LLC
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.draagon.meta.object;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;

import java.util.Map;

/**
 * Exception thrown when a requested MetaObject cannot be found.
 * Enhanced with structured error reporting capabilities.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
public class MetaObjectNotFoundException extends MetaDataNotFoundException {

    /**
     * Creates a MetaObjectNotFoundException with a message and object name.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param name the name of the object that was not found
     */
    public MetaObjectNotFoundException(String msg, String name) {
        super(msg, name);
    }

    /**
     * Creates a MetaObjectNotFoundException with a message and object instance.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param o the object instance whose MetaObject was not found
     */
    public MetaObjectNotFoundException(String msg, Object o) {
        super(msg, o.getClass().toString());
    }

    /**
     * Creates a MetaObjectNotFoundException with enhanced context information.
     * 
     * @param objectName the name of the object that was not found
     * @param parent the parent MetaData object where the object was expected
     */
    public MetaObjectNotFoundException(String objectName, MetaData parent) {
        super("object", objectName, parent);
    }

    /**
     * Creates a MetaObjectNotFoundException with full context information.
     * 
     * @param objectName the name of the object that was not found
     * @param parent the parent MetaData object where the object was expected
     * @param additionalContext additional context information
     */
    public MetaObjectNotFoundException(String objectName, MetaData parent, Map<String, Object> additionalContext) {
        super("object", objectName, parent, additionalContext);
    }

    /**
     * Factory method for creating an object not found exception with enhanced error reporting.
     * 
     * @param objectName the name of the object that was not found
     * @param parent the parent MetaData object where the object was expected
     * @return a configured MetaObjectNotFoundException
     */
    public static MetaObjectNotFoundException create(String objectName, MetaData parent) {
        return new MetaObjectNotFoundException(objectName, parent);
    }

    /**
     * Factory method for creating an object not found exception based on a Java object instance.
     * 
     * @param objectInstance the Java object instance whose MetaObject was not found
     * @param parent the parent MetaData object where the object was expected (may be null)
     * @return a configured MetaObjectNotFoundException
     */
    public static MetaObjectNotFoundException forInstance(Object objectInstance, MetaData parent) {
        String objectClassName = objectInstance.getClass().getSimpleName();
        return new MetaObjectNotFoundException(objectClassName, parent, 
            Map.of("javaClass", objectInstance.getClass().getName(), 
                   "objectToString", objectInstance.toString()));
    }

    /**
     * Factory method for creating an object not found exception with additional context.
     * 
     * @param objectName the name of the object that was not found
     * @param parent the parent MetaData object where the object was expected
     * @param additionalContext additional context information (e.g., search criteria, loader info)
     * @return a configured MetaObjectNotFoundException
     */
    public static MetaObjectNotFoundException create(String objectName, MetaData parent, Map<String, Object> additionalContext) {
        return new MetaObjectNotFoundException(objectName, parent, additionalContext);
    }
}

