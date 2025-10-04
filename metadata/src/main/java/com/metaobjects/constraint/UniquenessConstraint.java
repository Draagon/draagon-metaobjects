package com.metaobjects.constraint;

import com.metaobjects.MetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Uniqueness validation constraint that ensures collections of values are unique.
 *
 * This constraint is designed to handle cross-field validation where uniqueness
 * must be maintained across multiple child elements or extracted values.
 *
 * Example usage:
 * - Field name uniqueness: Ensure all field names within an object are unique
 * - Key name uniqueness: Ensure all key names within an object are unique
 * - Attribute name uniqueness: Ensure all attribute names are unique
 */
public class UniquenessConstraint extends BaseConstraint {

    private final Function<MetaData, Collection<String>> valueExtractor;
    private final String valueDescription;

    /**
     * Create a uniqueness constraint
     * @param constraintId Unique identifier
     * @param description Human-readable description
     * @param targetType Target type pattern
     * @param targetSubType Target subtype pattern
     * @param targetName Target name pattern
     * @param valueExtractor Function to extract collection of values to check for uniqueness
     * @param valueDescription Description of what values are being checked (e.g., "field names", "key names")
     */
    public UniquenessConstraint(String constraintId, String description,
                               String targetType, String targetSubType, String targetName,
                               Function<MetaData, Collection<String>> valueExtractor,
                               String valueDescription) {
        super(constraintId, description, targetType, targetSubType, targetName);
        this.valueExtractor = valueExtractor;
        this.valueDescription = valueDescription != null ? valueDescription : "values";
    }

    @Override
    public void validate(MetaData metaData, Object value) throws ConstraintViolationException {
        // Extract the collection of values to check
        Collection<String> values = valueExtractor.apply(metaData);

        if (values == null || values.isEmpty()) {
            return; // Empty collections are always unique
        }

        // Create a set to identify duplicates
        Set<String> uniqueValues = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (String val : values) {
            if (val != null && !uniqueValues.add(val)) {
                duplicates.add(val);
            }
        }

        if (!duplicates.isEmpty()) {
            String duplicateList = duplicates.stream()
                .sorted()
                .collect(Collectors.joining(", "));

            throw new ConstraintViolationException(
                String.format("Duplicate %s found in %s '%s': %s",
                    valueDescription, metaData.getType(), metaData.getName(), duplicateList),
                constraintId,
                metaData
            );
        }
    }

    @Override
    public String getType() {
        return "uniqueness";
    }

    /**
     * Get the value extractor function
     * @return Function that extracts values to check for uniqueness
     */
    public Function<MetaData, Collection<String>> getValueExtractor() {
        return valueExtractor;
    }

    /**
     * Get the description of values being checked
     * @return Description of what values are being checked
     */
    public String getValueDescription() {
        return valueDescription;
    }

    @Override
    public String toString() {
        return "UniquenessConstraint{" +
               "id='" + constraintId + '\'' +
               ", valueDescription='" + valueDescription + '\'' +
               ", target=" + getTargetDescription() +
               '}';
    }

    /**
     * Static factory method for field name uniqueness (common case)
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param targetType Target type (e.g., "object")
     * @param targetSubType Target subtype (e.g., "*")
     * @return UniquenessConstraint for field name uniqueness
     */
    public static UniquenessConstraint forFieldNames(String constraintId, String description,
                                                    String targetType, String targetSubType) {
        return new UniquenessConstraint(
            constraintId,
            description,
            targetType, targetSubType, "*",
            (metaData) -> {
                if (metaData instanceof com.metaobjects.object.MetaObject) {
                    com.metaobjects.object.MetaObject obj = (com.metaobjects.object.MetaObject) metaData;
                    return obj.getChildren(com.metaobjects.field.MetaField.class).stream()
                        .map(field -> field.getName())
                        .collect(Collectors.toList());
                }
                return List.of();
            },
            "field names"
        );
    }

    /**
     * Static factory method for key name uniqueness
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param targetType Target type (e.g., "object")
     * @param targetSubType Target subtype (e.g., "*")
     * @return UniquenessConstraint for key name uniqueness
     */
    public static UniquenessConstraint forKeyNames(String constraintId, String description,
                                                  String targetType, String targetSubType) {
        return new UniquenessConstraint(
            constraintId,
            description,
            targetType, targetSubType, "*",
            (metaData) -> {
                if (metaData instanceof com.metaobjects.object.MetaObject) {
                    com.metaobjects.object.MetaObject obj = (com.metaobjects.object.MetaObject) metaData;
                    // ✅ MIGRATED: Get identity names from the new MetaIdentity approach
                    List<String> identityNames = new ArrayList<>();

                    // Add identity names from MetaIdentity children
                    obj.getIdentities().stream()
                        .map(identity -> identity.getName())
                        .forEach(identityNames::add);

                    // Add relationship names (for foreign key relationships)
                    obj.getRelationships().stream()
                        .map(rel -> rel.getName())
                        .forEach(identityNames::add);

                    return identityNames;
                }
                return List.of();
            },
            "identity and relationship names"
        );
    }

    /**
     * Static factory method for attribute name uniqueness
     * @param constraintId Unique constraint identifier
     * @param description Human-readable description
     * @param targetType Target type (e.g., "object")
     * @param targetSubType Target subtype (e.g., "*")
     * @return UniquenessConstraint for attribute name uniqueness
     */
    public static UniquenessConstraint forAttributeNames(String constraintId, String description,
                                                        String targetType, String targetSubType) {
        return new UniquenessConstraint(
            constraintId,
            description,
            targetType, targetSubType, "*",
            (metaData) -> {
                return metaData.getChildren(com.metaobjects.attr.MetaAttribute.class).stream()
                    .map(attr -> attr.getName())
                    .collect(Collectors.toList());
            },
            "attribute names"
        );
    }
}