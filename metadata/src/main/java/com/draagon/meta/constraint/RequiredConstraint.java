package com.draagon.meta.constraint;

import com.draagon.meta.MetaData;

/**
 * Required field validation constraint that's easy to serialize to schemas.
 *
 * Example usage:
 * - Field names required: new RequiredConstraint("field.name.required", "Field names are required",
 *                                               "field", "*", "name")
 * - Type required: new RequiredConstraint("metadata.type.required", "Type is required",
 *                                         "*", "*", "type")
 */
public class RequiredConstraint extends BaseConstraint {

    /**
     * Create a required constraint
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     */
    public RequiredConstraint(String constraintId, String description,
                             String targetType, String targetSubType, String targetName) {
        super(constraintId, description, targetType, targetSubType, targetName);
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        if (value == null) {
            throw new ConstraintViolationException(
                String.format("Value for %s '%s' is required and cannot be null",
                    metaData.getType(), metaData.getName()),
                constraintId,
                metaData
            );
        }

        // Check for empty strings
        if (value instanceof String) {
            String stringValue = (String) value;
            if (stringValue.trim().isEmpty()) {
                throw new ConstraintViolationException(
                    String.format("Value for %s '%s' is required and cannot be empty",
                        metaData.getType(), metaData.getName()),
                    constraintId,
                    metaData
                );
            }
        }
    }

    @Override
    public String getType() {
        return "required";
    }

    /**
     * Check if empty strings are considered invalid
     * @return True (empty strings are not allowed for required fields)
     */
    public boolean isEmptyStringInvalid() {
        return true;
    }

    @Override
    public String toString() {
        return "RequiredConstraint{" +
               "id='" + constraintId + '\'' +
               ", target=" + getTargetDescription() +
               ", description='" + description + '\'' +
               '}';
    }
}