package com.draagon.meta.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;

import java.util.List;
import java.util.Optional;

/**
 * Type-safe access utilities for MetaData objects.
 * Provides compile-time type validation and consistent API patterns.
 */
public final class TypedMetaDataAccess {
    
    private TypedMetaDataAccess() {} // Utility class
    
    // ========== FIELD ACCESS ==========
    
    /**
     * Find a field by name in a MetaObject
     * @param metaObject The MetaObject to search in
     * @param fieldName The name of the field to find
     * @return Optional containing the field if found
     */
    public static Optional<MetaField> findField(MetaObject metaObject, String fieldName) {
        if (metaObject == null || fieldName == null) {
            return Optional.empty();
        }
        return metaObject.findChild(fieldName, MetaField.class);
    }
    
    /**
     * Require a field by name, throwing an exception if not found
     * @param metaObject The MetaObject to search in
     * @param fieldName The name of the field to find
     * @return The MetaField
     * @throws MetaFieldNotFoundException if the field is not found
     */
    public static MetaField requireField(MetaObject metaObject, String fieldName) {
        if (metaObject == null) {
            throw new MetaFieldNotFoundException("Cannot find field in null MetaObject", fieldName);
        }
        return findField(metaObject, fieldName)
            .orElseThrow(() -> new MetaFieldNotFoundException(
                "Field '" + fieldName + "' not found in MetaObject '" + metaObject.getName() + "'", fieldName));
    }
    
    /**
     * Get all fields from a MetaObject
     * @param metaObject The MetaObject to get fields from
     * @return List of MetaField objects
     */
    public static List<MetaField> getFields(MetaObject metaObject) {
        if (metaObject == null) {
            return List.of();
        }
        return metaObject.findChildren(MetaField.class).toList();
    }
    
    // ========== ATTRIBUTE ACCESS ==========
    
    /**
     * Find an attribute by name in any MetaData
     * @param metaData The MetaData to search in
     * @param attributeName The name of the attribute to find
     * @return Optional containing the attribute if found
     */
    public static Optional<MetaAttribute> findAttribute(MetaData metaData, String attributeName) {
        if (metaData == null || attributeName == null) {
            return Optional.empty();
        }
        return metaData.findChild(attributeName, MetaAttribute.class);
    }
    
    /**
     * Require an attribute by name, throwing an exception if not found
     * @param metaData The MetaData to search in
     * @param attributeName The name of the attribute to find
     * @return The MetaAttribute
     * @throws MetaAttributeNotFoundException if the attribute is not found
     */
    public static MetaAttribute requireAttribute(MetaData metaData, String attributeName) {
        if (metaData == null) {
            throw new MetaAttributeNotFoundException("Cannot find attribute in null MetaData", attributeName);
        }
        return findAttribute(metaData, attributeName)
            .orElseThrow(() -> new MetaAttributeNotFoundException(
                "Attribute '" + attributeName + "' not found in MetaData '" + metaData.getName() + "'", attributeName));
    }
    
    /**
     * Get all attributes from a MetaData
     * @param metaData The MetaData to get attributes from
     * @return List of MetaAttribute objects
     */
    public static List<MetaAttribute> getAttributes(MetaData metaData) {
        if (metaData == null) {
            return List.of();
        }
        return metaData.findChildren(MetaAttribute.class).toList();
    }
    
    // ========== VALIDATOR ACCESS ==========
    
    /**
     * Get all validators from a MetaField
     * @param field The MetaField to get validators from
     * @return List of MetaValidator objects
     */
    public static List<MetaValidator> getValidators(MetaField field) {
        if (field == null) {
            return List.of();
        }
        return field.findChildren(MetaValidator.class).toList();
    }
    
    /**
     * Find a validator by name in a MetaField
     * @param field The MetaField to search in
     * @param validatorName The name of the validator to find
     * @return Optional containing the validator if found
     */
    public static Optional<MetaValidator> findValidator(MetaField field, String validatorName) {
        if (field == null || validatorName == null) {
            return Optional.empty();
        }
        return field.findChild(validatorName, MetaValidator.class);
    }
    
    // ========== VIEW ACCESS ==========
    
    /**
     * Get all views from a MetaField
     * @param field The MetaField to get views from
     * @return List of MetaView objects
     */
    public static List<MetaView> getViews(MetaField field) {
        if (field == null) {
            return List.of();
        }
        return field.findChildren(MetaView.class).toList();
    }
    
    /**
     * Find a view by name in a MetaField
     * @param field The MetaField to search in
     * @param viewName The name of the view to find
     * @return Optional containing the view if found
     */
    public static Optional<MetaView> findView(MetaField field, String viewName) {
        if (field == null || viewName == null) {
            return Optional.empty();
        }
        return field.findChild(viewName, MetaView.class);
    }
    
    // ========== GENERIC TYPE-SAFE ACCESS ==========
    
    /**
     * Find child by name and type with null safety
     * @param parent The parent MetaData
     * @param name The name to search for
     * @param type The type to cast to
     * @return Optional containing the child if found and of correct type
     */
    public static <T extends MetaData> Optional<T> findChild(MetaData parent, String name, Class<T> type) {
        if (parent == null || name == null || type == null) {
            return Optional.empty();
        }
        return parent.findChild(name, type);
    }
    
    /**
     * Require a child by name and type, throwing an exception if not found
     * @param parent The parent MetaData
     * @param name The name to search for
     * @param type The type to cast to
     * @return The child MetaData
     * @throws RuntimeException if the child is not found
     */
    public static <T extends MetaData> T requireChild(MetaData parent, String name, Class<T> type) {
        if (parent == null) {
            throw new RuntimeException("Cannot find child in null MetaData");
        }
        return parent.requireChild(name, type);
    }
    
    /**
     * Get all children of a specific type
     * @param parent The parent MetaData
     * @param type The type to filter by
     * @return List of children of the specified type
     */
    public static <T extends MetaData> List<T> getChildren(MetaData parent, Class<T> type) {
        if (parent == null || type == null) {
            return List.of();
        }
        return parent.findChildren(type).toList();
    }
}