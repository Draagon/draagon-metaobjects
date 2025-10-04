/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects;

import com.metaobjects.util.ErrorFormatter;

import java.util.Map;

/**
 * Exception thrown when a requested MetaData item cannot be found.
 * Enhanced with structured error reporting capabilities.
 * 
 * @since 1.0 (enhanced in 5.2.0)
 */
@SuppressWarnings("serial")
public class MetaDataNotFoundException extends MetaDataException {

    private final String name;

    /**
     * Creates a MetaDataNotFoundException with a message and name.
     * Backward compatible constructor.
     * 
     * @param msg the error message
     * @param name the name of the item that was not found
     */
    public MetaDataNotFoundException(String msg, String name) {
        super(msg);
        this.name = name;
    }

    /**
     * Creates a MetaDataNotFoundException with enhanced context information.
     * 
     * @param itemType the type of item that was not found (e.g., "field", "validator")
     * @param itemName the name of the item that was not found
     * @param parent the parent MetaData object where the item was expected
     */
    public MetaDataNotFoundException(String itemType, String itemName, MetaData parent) {
        super(ErrorFormatter.formatNotFoundError(itemType, itemName, parent), 
              parent, "lookup", null, 
              Map.of("itemType", itemType, "itemName", itemName));
        this.name = itemName;
    }

    /**
     * Creates a MetaDataNotFoundException with full context information.
     * 
     * @param itemType the type of item that was not found
     * @param itemName the name of the item that was not found
     * @param parent the parent MetaData object where the item was expected
     * @param additionalContext additional context information
     */
    public MetaDataNotFoundException(String itemType, String itemName, MetaData parent,
                                   Map<String, Object> additionalContext) {
        super(ErrorFormatter.formatNotFoundError(itemType, itemName, parent), 
              parent, "lookup", null, 
              mergeContext(Map.of("itemType", itemType, "itemName", itemName), additionalContext));
        this.name = itemName;
    }

    /**
     * Factory method for creating field not found exceptions.
     * 
     * @param fieldName the name of the field that was not found
     * @param parent the parent MetaData object where the field was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forField(String fieldName, MetaData parent) {
        return new MetaDataNotFoundException("field", fieldName, parent);
    }

    /**
     * Factory method for creating validator not found exceptions.
     * 
     * @param validatorName the name of the validator that was not found
     * @param parent the parent MetaData object where the validator was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forValidator(String validatorName, MetaData parent) {
        return new MetaDataNotFoundException("validator", validatorName, parent);
    }

    /**
     * Factory method for creating view not found exceptions.
     * 
     * @param viewName the name of the view that was not found
     * @param parent the parent MetaData object where the view was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forView(String viewName, MetaData parent) {
        return new MetaDataNotFoundException("view", viewName, parent);
    }

    /**
     * Factory method for creating attribute not found exceptions.
     * 
     * @param attributeName the name of the attribute that was not found
     * @param parent the parent MetaData object where the attribute was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forAttribute(String attributeName, MetaData parent) {
        return new MetaDataNotFoundException("attribute", attributeName, parent);
    }

    /**
     * Factory method for creating object not found exceptions.
     *
     * @param objectName the name of the object that was not found
     * @param parent the parent MetaData object where the object was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forObject(String objectName, MetaData parent) {
        return new MetaDataNotFoundException("object", objectName, parent);
    }

    /**
     * Factory method for creating relationship not found exceptions.
     *
     * @param relationshipName the name of the relationship that was not found
     * @param parent the parent MetaData object where the relationship was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forRelationship(String relationshipName, MetaData parent) {
        return new MetaDataNotFoundException("relationship", relationshipName, parent);
    }

    /**
     * Factory method for creating identity not found exceptions.
     *
     * @param identityName the name of the identity that was not found
     * @param parent the parent MetaData object where the identity was expected
     * @return a configured MetaDataNotFoundException
     */
    public static MetaDataNotFoundException forIdentity(String identityName, MetaData parent) {
        return new MetaDataNotFoundException("identity", identityName, parent);
    }

    /**
     * Legacy method for creating prefix strings.
     * Kept for backward compatibility.
     * 
     * @param type the type of item
     * @param name the name of item
     * @return formatted prefix string
     */
    protected String prefix(String type, String name) {
        return "[" + name + "]";
    }

    /**
     * Returns the name of the item that was not found.
     * 
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Merges two context maps, with the second taking precedence.
     * 
     * @param base the base context map
     * @param additional the additional context map
     * @return merged context map
     */
    private static Map<String, Object> mergeContext(Map<String, Object> base, Map<String, Object> additional) {
        if (additional == null || additional.isEmpty()) {
            return base;
        }
        
        java.util.Map<String, Object> merged = new java.util.HashMap<>(base);
        merged.putAll(additional);
        return merged;
    }
}
