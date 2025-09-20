package com.draagon.meta.util;

import com.draagon.meta.MetaData;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.draagon.meta.util.MetaDataConstants.*;

/**
 * Utility class for creating consistent, human-readable error message formatting.
 * Provides standardized error message patterns for common MetaData error scenarios.
 * 
 * <p>Example usage:</p>
 * <pre>
 * String errorMsg = ErrorFormatter.formatNotFoundError("field", "email", metaObject);
 * throw new MetaDataException(errorMsg, metaObject, "lookup");
 * </pre>
 * 
 * @since 5.2.0
 */
public final class ErrorFormatter {

    private ErrorFormatter() {
        // Utility class - no instantiation
    }

    /**
     * Formats a validation error message with detailed context.
     * 
     * @param source the MetaData object being validated
     * @param validation the validation rule that failed
     * @param value the actual value that failed validation
     * @param expected the expected value or constraint
     * @return formatted validation error message
     */
    public static String formatValidationError(MetaData source, String validation, Object value, String expected) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (validation == null) {
            throw new IllegalArgumentException("validation cannot be null");
        }

        return String.format(
            "Validation failed for %s:\n" +
            "  Value: %s\n" +
            "  Expected: %s\n" +
            "  Validation: %s\n" +
            "  Path: %s",
            source.getName(),
            formatValue(value),
            expected,
            validation,
            MetaDataPath.buildPath(source).toHierarchicalString()
        );
    }

    /**
     * Formats a type mismatch error message.
     * 
     * @param source the MetaData object with the type mismatch
     * @param expected the expected type
     * @param actual the actual type encountered
     * @return formatted type error message
     */
    public static String formatTypeError(MetaData source, Class<?> expected, Class<?> actual) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (expected == null) {
            throw new IllegalArgumentException("expected cannot be null");
        }
        if (actual == null) {
            throw new IllegalArgumentException("actual cannot be null");
        }

        return String.format(
            "Type mismatch at %s:\n" +
            "  Expected: %s\n" +
            "  Actual: %s\n" +
            "  Path: %s",
            source.getName(),
            expected.getSimpleName(),
            actual.getSimpleName(),
            MetaDataPath.buildPath(source).toHierarchicalString()
        );
    }

    /**
     * Formats a type mismatch error message with string type names.
     * 
     * @param source the MetaData object with the type mismatch
     * @param expectedType the expected type name
     * @param actualType the actual type name encountered
     * @return formatted type error message
     */
    public static String formatTypeError(MetaData source, String expectedType, String actualType) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (expectedType == null) {
            throw new IllegalArgumentException("expectedType cannot be null");
        }
        if (actualType == null) {
            throw new IllegalArgumentException("actualType cannot be null");
        }

        return String.format(
            "Type mismatch at %s:\n" +
            "  Expected: %s\n" +
            "  Actual: %s\n" +
            "  Path: %s",
            source.getName(),
            expectedType,
            actualType,
            MetaDataPath.buildPath(source).toHierarchicalString()
        );
    }

    /**
     * Formats a "not found" error message with helpful context about available alternatives.
     * 
     * @param itemType the type of item that was not found (e.g., "field", "validator")
     * @param itemName the name of the item that was not found
     * @param parent the parent MetaData object where the item was expected
     * @return formatted not found error message
     */
    public static String formatNotFoundError(String itemType, String itemName, MetaData parent) {
        if (itemType == null) {
            throw new IllegalArgumentException("itemType cannot be null");
        }
        if (itemName == null) {
            throw new IllegalArgumentException("itemName cannot be null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        String available = getAvailableItems(parent, itemType);
        
        return String.format(
            "%s '%s' not found in %s:\n" +
            "  Available: %s\n" +
            "  Path: %s",
            capitalize(itemType),
            itemName,
            parent.getName(),
            available.isEmpty() ? DISPLAY_NONE : available,
            MetaDataPath.buildPath(parent).toHierarchicalString()
        );
    }

    /**
     * Formats a configuration error message.
     * 
     * @param source the MetaData object with the configuration issue
     * @param property the configuration property that has an issue
     * @param value the problematic value
     * @param issue description of the configuration issue
     * @return formatted configuration error message
     */
    public static String formatConfigurationError(MetaData source, String property, Object value, String issue) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (property == null) {
            throw new IllegalArgumentException("property cannot be null");
        }
        if (issue == null) {
            throw new IllegalArgumentException("issue cannot be null");
        }

        return String.format(
            "Configuration error in %s:\n" +
            "  Property: %s\n" +
            "  Value: %s\n" +
            "  Issue: %s\n" +
            "  Path: %s",
            source.getName(),
            property,
            formatValue(value),
            issue,
            MetaDataPath.buildPath(source).toHierarchicalString()
        );
    }

    /**
     * Formats a loading error message with context about what was being loaded.
     * 
     * @param source the MetaData object that failed to load (may be null)
     * @param operation the loading operation that failed
     * @param details additional details about the failure
     * @return formatted loading error message
     */
    public static String formatLoadingError(MetaData source, String operation, String details) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }

        String sourceInfo = source != null ? " for " + source.getName() : "";
        String detailsInfo = (details != null && !details.trim().isEmpty()) ? 
            String.format("\n  Details: %s", details) : "";
        String pathInfo = source != null ? 
            String.format("\n  Path: %s", MetaDataPath.buildPath(source).toHierarchicalString()) : "";

        return String.format("Loading failed during %s%s:%s%s", operation, sourceInfo, detailsInfo, pathInfo);
    }

    /**
     * Formats a generic error with contextual information.
     * 
     * @param source the MetaData object where the error occurred
     * @param operation the operation that failed
     * @param errorMessage the error message
     * @param context additional context information
     * @return formatted generic error message
     */
    public static String formatGenericError(MetaData source, String operation, String errorMessage, 
                                          Map<String, Object> context) {
        if (errorMessage == null) {
            throw new IllegalArgumentException("errorMessage cannot be null");
        }

        String operationInfo = operation != null ? String.format(" (during %s)", operation) : "";
        String sourceInfo = source != null ? 
            String.format("\n  Target: %s\n  Path: %s", 
                source.getName(), 
                MetaDataPath.buildPath(source).toHierarchicalString()) : "";
        
        String contextInfo = "";
        if (context != null && !context.isEmpty()) {
            StringBuilder contextBuilder = new StringBuilder("\n  Context:");
            context.forEach((key, value) -> 
                contextBuilder.append(String.format("\n    %s: %s", key, formatValue(value))));
            contextInfo = contextBuilder.toString();
        }

        return String.format("%s%s%s%s", errorMessage, operationInfo, sourceInfo, contextInfo);
    }

    /**
     * Gets a comma-separated list of available items of the specified type within the parent.
     * 
     * @param parent the parent MetaData object
     * @param itemType the type of items to list
     * @return comma-separated list of available item names
     */
    private static String getAvailableItems(MetaData parent, String itemType) {
        if (parent.getChildren() == null) {
            return "";
        }
        
        return parent.getChildren().stream()
                .filter(child -> itemType.equalsIgnoreCase(child.getTypeName()))
                .map(MetaData::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Formats a value for display in error messages, handling null values and long strings.
     * 
     * @param value the value to format
     * @return formatted string representation
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return DISPLAY_NULL;
        }
        
        String str = value.toString();
        
        // Handle empty strings
        if (str.isEmpty()) {
            return DISPLAY_EMPTY;
        }
        
        // Truncate very long values using constants
        if (str.length() > MAX_DISPLAY_LENGTH) {
            return str.substring(0, MAX_DISPLAY_LENGTH - DISPLAY_ELLIPSIS.length()) + DISPLAY_ELLIPSIS;
        }
        
        return str;
    }

    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str the string to capitalize
     * @return capitalized string
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}