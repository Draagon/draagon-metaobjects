package com.metaobjects.registry;

import com.metaobjects.MetaData;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.constraint.CustomConstraint;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Fluent builder for creating attribute constraints with comprehensive type safety and validation.
 *
 * <p>This builder provides a self-documenting API for defining attribute constraints that specify:</p>
 * <ul>
 *   <li><strong>Type specification:</strong> The underlying attribute type (string, int, boolean, etc.)</li>
 *   <li><strong>Cardinality:</strong> Single value, array, set, or map semantics</li>
 *   <li><strong>Value constraints:</strong> Ranges, enums, patterns, and custom validation</li>
 * </ul>
 *
 * <strong>Usage Examples:</strong>
 * <pre>{@code
 * // Array of strings (like identity fields)
 * .optionalAttribute(ATTR_FIELDS).ofType(SUBTYPE_STRING).asArray()
 *
 * // Enumerated generation strategies
 * .optionalAttribute(ATTR_GENERATION).ofType(SUBTYPE_STRING).withEnum("increment", "uuid", "assigned")
 *
 * // Integer with range constraint
 * .optionalAttribute(ATTR_MAX_LENGTH).ofType(SUBTYPE_INT).withRange(1, 10000)
 *
 * // String with regex pattern validation
 * .optionalAttribute(ATTR_PATTERN).ofType(SUBTYPE_STRING).withRegex()
 * }</pre>
 *
 * @since 6.2.8
 */
public class AttributeConstraintBuilder {

    private final TypeDefinitionBuilder parent;
    private final String attributeName;
    private final boolean required;

    /**
     * Create an attribute constraint builder
     *
     * @param parent Parent TypeDefinitionBuilder for method chaining
     * @param attributeName Name of the attribute being constrained
     * @param required Whether this attribute is required (true) or optional (false)
     */
    public AttributeConstraintBuilder(TypeDefinitionBuilder parent, String attributeName, boolean required) {
        this.parent = parent;
        this.attributeName = attributeName;
        this.required = required;
    }

    /**
     * Specify the underlying attribute type (string, int, boolean, etc.)
     *
     * @param attributeSubType The attribute subType using constants from attribute classes
     * @return AttributeTypeBuilder for cardinality and constraint specification
     */
    public AttributeTypeBuilder ofType(String attributeSubType) {
        return new AttributeTypeBuilder(parent, attributeName, attributeSubType, required);
    }

    /**
     * Fluent builder for attribute type specification and constraints
     */
    public static class AttributeTypeBuilder {

        private final TypeDefinitionBuilder parent;
        private final String attributeName;
        private final String attributeSubType;
        private final boolean required;

        /**
         * Create an attribute type builder
         *
         * @param parent Parent TypeDefinitionBuilder for method chaining
         * @param attributeName Name of the attribute being constrained
         * @param attributeSubType The attribute subType (string, int, boolean, etc.)
         * @param required Whether this attribute is required (true) or optional (false)
         */
        public AttributeTypeBuilder(TypeDefinitionBuilder parent, String attributeName,
                                  String attributeSubType, boolean required) {
            this.parent = parent;
            this.attributeName = attributeName;
            this.attributeSubType = attributeSubType;
            this.required = required;
        }

        // ==================== CARDINALITY SPECIFICATION ====================

        /**
         * Specify this attribute holds a single value (default behavior)
         *
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder asSingle() {
            return registerAttributeWithConstraints(false, false, false);
        }

        /**
         * Specify this attribute holds an array of values
         *
         * <p>This is the key method for fields like identity.fields that contain multiple values.</p>
         *
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder asArray() {
            return registerAttributeWithConstraints(true, false, false);
        }

        /**
         * Specify this attribute holds a set of unique values
         *
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder asSet() {
            return registerAttributeWithConstraints(false, true, false);
        }

        /**
         * Specify this attribute holds a map/properties structure
         *
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder asMap() {
            return registerAttributeWithConstraints(false, false, true);
        }

        // ==================== CONSTRAINT SPECIFICATION ====================

        /**
         * Add an enumeration constraint (valid values)
         *
         * @param validValues Array of valid string values
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder withEnum(String... validValues) {
            AttributeConstraintSpecBuilder builder = new AttributeConstraintSpecBuilder(this);
            return builder.withEnum(validValues);
        }

        /**
         * Add a range constraint for numeric values
         *
         * @param min Minimum value (inclusive)
         * @param max Maximum value (inclusive)
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder withRange(int min, int max) {
            AttributeConstraintSpecBuilder builder = new AttributeConstraintSpecBuilder(this);
            return builder.withRange(min, max);
        }

        /**
         * Add regex pattern validation for string values
         *
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder withRegex() {
            AttributeConstraintSpecBuilder builder = new AttributeConstraintSpecBuilder(this);
            return builder.withRegex();
        }

        /**
         * Add custom validation logic
         *
         * @param validator Custom predicate for value validation
         * @return Parent TypeDefinitionBuilder for method chaining
         */
        public TypeDefinitionBuilder withCustom(Predicate<Object> validator) {
            AttributeConstraintSpecBuilder builder = new AttributeConstraintSpecBuilder(this);
            return builder.withCustom(validator);
        }

        /**
         * Internal method to register the attribute with specified cardinality constraints
         */
        private TypeDefinitionBuilder registerAttributeWithConstraints(boolean isArray, boolean isSet, boolean isMap) {
            // 1. Register basic child requirement
            if (required) {
                parent.requiredChild(MetaAttribute.TYPE_ATTR, attributeSubType, attributeName);
            } else {
                parent.optionalChild(MetaAttribute.TYPE_ATTR, attributeSubType, attributeName);
            }

            // 2. Generate cardinality constraints
            if (isArray) {
                generateArrayConstraint();
            } else if (isSet) {
                generateSetConstraint();
            } else if (isMap) {
                generateMapConstraint();
            }

            return parent;
        }

        /**
         * Generate array constraint for this attribute
         */
        private void generateArrayConstraint() {
            String constraintId = parent.getType() + "." + parent.getSubType() + "." + attributeName + ".array";
            String description = attributeName + " attribute must be an array on " +
                               parent.getType() + "." + parent.getSubType();

            CustomConstraint arrayConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> parent.getType().equals(metadata.getType()) &&
                             parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> isArrayValue(value),
                "Array constraint validation"
            );

            parent.addAutoGeneratedConstraint(arrayConstraint);
        }

        /**
         * Generate set constraint for this attribute
         */
        private void generateSetConstraint() {
            String constraintId = parent.getType() + "." + parent.getSubType() + "." + attributeName + ".set";
            String description = attributeName + " attribute must be a set on " +
                               parent.getType() + "." + parent.getSubType();

            CustomConstraint setConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> parent.getType().equals(metadata.getType()) &&
                             parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> isSetValue(value),
                "Set constraint validation"
            );

            parent.addAutoGeneratedConstraint(setConstraint);
        }

        /**
         * Generate map constraint for this attribute
         */
        private void generateMapConstraint() {
            String constraintId = parent.getType() + "." + parent.getSubType() + "." + attributeName + ".map";
            String description = attributeName + " attribute must be a map on " +
                               parent.getType() + "." + parent.getSubType();

            CustomConstraint mapConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> parent.getType().equals(metadata.getType()) &&
                             parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> isMapValue(value),
                "Map constraint validation"
            );

            parent.addAutoGeneratedConstraint(mapConstraint);
        }

        /**
         * Check if value represents an array (JSON array or comma-delimited string)
         */
        private boolean isArrayValue(Object value) {
            if (value == null) return true; // null is valid for optional arrays
            if (value instanceof String) {
                String str = (String) value;
                // Check for JSON array format or comma-delimited values
                return str.startsWith("[") && str.endsWith("]") || str.contains(",");
            }
            return false;
        }

        /**
         * Check if value represents a set (unique values)
         */
        private boolean isSetValue(Object value) {
            // For now, same validation as array - uniqueness checked elsewhere
            return isArrayValue(value);
        }

        /**
         * Check if value represents a map/properties
         */
        private boolean isMapValue(Object value) {
            if (value == null) return true; // null is valid for optional maps
            if (value instanceof String) {
                String str = (String) value;
                // Check for JSON object format or properties format
                return str.startsWith("{") && str.endsWith("}") || str.contains("=");
            }
            return false;
        }
    }

    /**
     * Builder for additional constraint specifications
     */
    public static class AttributeConstraintSpecBuilder {

        private final AttributeTypeBuilder typeBuilder;

        public AttributeConstraintSpecBuilder(AttributeTypeBuilder typeBuilder) {
            this.typeBuilder = typeBuilder;
        }

        /**
         * Add enumeration constraint and finalize as single value
         */
        public TypeDefinitionBuilder withEnum(String... validValues) {
            generateEnumConstraint(validValues);
            return typeBuilder.asSingle();
        }

        /**
         * Add range constraint and finalize as single value
         */
        public TypeDefinitionBuilder withRange(int min, int max) {
            generateRangeConstraint(min, max);
            return typeBuilder.asSingle();
        }

        /**
         * Add regex constraint and finalize as single value
         */
        public TypeDefinitionBuilder withRegex() {
            generateRegexConstraint();
            return typeBuilder.asSingle();
        }

        /**
         * Add custom constraint and finalize as single value
         */
        public TypeDefinitionBuilder withCustom(Predicate<Object> validator) {
            generateCustomConstraint(validator);
            return typeBuilder.asSingle();
        }

        // Chain to cardinality methods
        public TypeDefinitionBuilder andAsArray() { return typeBuilder.asArray(); }
        public TypeDefinitionBuilder andAsSingle() { return typeBuilder.asSingle(); }
        public TypeDefinitionBuilder andAsSet() { return typeBuilder.asSet(); }
        public TypeDefinitionBuilder andAsMap() { return typeBuilder.asMap(); }

        private void generateEnumConstraint(String... validValues) {
            String constraintId = typeBuilder.parent.getType() + "." + typeBuilder.parent.getSubType() +
                                "." + typeBuilder.attributeName + ".enum";
            String description = typeBuilder.attributeName + " must be one of: " + Arrays.toString(validValues);

            CustomConstraint enumConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> typeBuilder.parent.getType().equals(metadata.getType()) &&
                             typeBuilder.parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> value == null || Arrays.asList(validValues).contains(value.toString()),
                "Enum constraint validation"
            );

            typeBuilder.parent.addAutoGeneratedConstraint(enumConstraint);
        }

        private void generateRangeConstraint(int min, int max) {
            String constraintId = typeBuilder.parent.getType() + "." + typeBuilder.parent.getSubType() +
                                "." + typeBuilder.attributeName + ".range";
            String description = typeBuilder.attributeName + " must be between " + min + " and " + max;

            CustomConstraint rangeConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> typeBuilder.parent.getType().equals(metadata.getType()) &&
                             typeBuilder.parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> {
                    if (value == null) return true;
                    try {
                        int intValue = Integer.parseInt(value.toString());
                        return intValue >= min && intValue <= max;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                "Range constraint validation"
            );

            typeBuilder.parent.addAutoGeneratedConstraint(rangeConstraint);
        }

        private void generateRegexConstraint() {
            String constraintId = typeBuilder.parent.getType() + "." + typeBuilder.parent.getSubType() +
                                "." + typeBuilder.attributeName + ".regex";
            String description = typeBuilder.attributeName + " must match regex pattern";

            CustomConstraint regexConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> typeBuilder.parent.getType().equals(metadata.getType()) &&
                             typeBuilder.parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> value == null || value.toString().matches("^[a-zA-Z][a-zA-Z0-9_]*$"),
                "Regex constraint validation"
            );

            typeBuilder.parent.addAutoGeneratedConstraint(regexConstraint);
        }

        private void generateCustomConstraint(Predicate<Object> validator) {
            String constraintId = typeBuilder.parent.getType() + "." + typeBuilder.parent.getSubType() +
                                "." + typeBuilder.attributeName + ".custom";
            String description = typeBuilder.attributeName + " must pass custom validation";

            CustomConstraint customConstraint = new CustomConstraint(
                constraintId, description,
                (metadata) -> typeBuilder.parent.getType().equals(metadata.getType()) &&
                             typeBuilder.parent.getSubType().equals(metadata.getSubType()),
                (metadata, value) -> validator.test(value),
                "Custom validation logic"
            );

            typeBuilder.parent.addAutoGeneratedConstraint(customConstraint);
        }
    }
}