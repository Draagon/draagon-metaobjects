package com.draagon.meta.constraint.simple;

import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;

import java.util.Set;

/**
 * Example migration from complex constraints to simple, schema-friendly constraints.
 * This demonstrates how to replace complex functional interface-based constraints
 * with simple, declarative constraint types.
 */
public class SimpleConstraintMigration {

    /**
     * Example of migrating complex constraints to simple constraints
     * @param registry MetaData registry to add constraints to
     */
    public static void registerSimpleConstraints(MetaDataRegistry registry) {

        // BEFORE: Complex PlacementConstraint with predicates
        /*
        registry.addConstraint(new PlacementConstraint(
            "stringfield.maxlength.placement",
            "String fields can optionally have maxLength attribute",
            (parent) -> parent.getClass().getSimpleName().equals("StringField"),
            (child) -> child instanceof com.draagon.meta.attr.IntAttribute &&
                      "maxLength".equals(child.getName())
        ));
        */

        // AFTER: Simple PlacementConstraint with patterns
        registry.registerConstraint(new SimplePlacementConstraint(
            "stringfield.maxlength.placement",
            "String fields can have maxLength attribute",
            "field.string",          // Parent pattern
            "attr.int[maxLength]",   // Child pattern
            true                     // Allowed
        ));

        // BEFORE: Complex ValidationConstraint with BiPredicate
        /*
        registry.addConstraint(new ValidationConstraint(
            "field.naming.pattern",
            "Field names must follow identifier pattern",
            (metadata) -> metadata instanceof com.draagon.meta.field.MetaField,
            (metadata, value) -> {
                if (value == null) return true;
                String name = value.toString();
                return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
            }
        ));
        */

        // AFTER: Simple RegexConstraint
        registry.registerConstraint(new SimpleRegexConstraint(
            "field.naming.pattern",
            "Field names must follow identifier pattern: ^[a-zA-Z][a-zA-Z0-9_]*$",
            "field",                          // Target type
            "*",                              // Any subtype
            "*",                              // Any name (this validates the name itself)
            "^[a-zA-Z][a-zA-Z0-9_]*$",      // Regex pattern
            true                              // Allow null
        ));

        // BEFORE: Complex length validation with BiPredicate
        /*
        registry.addConstraint(new ValidationConstraint(
            "field.name.length",
            "Field names must be 1-64 characters",
            (metadata) -> metadata instanceof com.draagon.meta.field.MetaField,
            (metadata, value) -> {
                if (value == null) return false;
                String name = value.toString();
                return name.length() >= 1 && name.length() <= 64;
            }
        ));
        */

        // AFTER: Simple LengthConstraint
        registry.registerConstraint(new SimpleLengthConstraint(
            "field.name.length",
            "Field names must be 1-64 characters",
            "field",        // Target type
            "*",            // Any subtype
            "*",            // Any name
            1,              // Min length
            64,             // Max length
            false           // Don't allow null
        ));

        // BEFORE: Complex enum validation with BiPredicate
        /*
        registry.addConstraint(new ValidationConstraint(
            "field.type.enum",
            "Field types must be valid primitive types",
            (metadata) -> metadata instanceof com.draagon.meta.field.MetaField &&
                         "type".equals(metadata.getName()),
            (metadata, value) -> {
                if (value == null) return false;
                String type = value.toString();
                return Set.of("string", "int", "long", "double", "boolean", "date").contains(type);
            }
        ));
        */

        // AFTER: Simple EnumConstraint
        registry.registerConstraint(new SimpleEnumConstraint(
            "field.type.enum",
            "Field types must be valid primitive types",
            "field",                    // Target type
            "*",                        // Any subtype
            "type",                     // Target name
            Set.of("string", "int", "long", "double", "boolean", "date"), // Allowed values
            true,                       // Case sensitive
            false                       // Don't allow null
        ));

        // Required field validation
        registry.registerConstraint(new SimpleRequiredConstraint(
            "field.name.required",
            "Field names are required",
            "field",        // Target type
            "*",            // Any subtype
            "name"          // Target name attribute
        ));

        // Complex validation that can't be simplified - use CustomLogicConstraint
        registry.registerConstraint(new CustomLogicConstraint(
            "object.foreign.key.reference",
            "Foreign key fields must reference existing objects",
            (metadata) -> metadata instanceof com.draagon.meta.field.MetaField &&
                         metadata.getMetaAttr("foreignKeyRef") != null,
            (metadata, value) -> {
                // Complex logic to validate foreign key references exist
                // This cannot be expressed in simple schema constraints
                if (value == null) return true;
                String refValue = value.toString();
                // Check if referenced object exists in registry...
                return validateForeignKeyExists(metadata, refValue);
            },
            "Validates that foreign key references point to existing metadata objects"
        ));
    }

    // Mock method for example
    private static boolean validateForeignKeyExists(com.draagon.meta.MetaData metadata, String refValue) {
        // Complex business logic that requires runtime evaluation
        // This is the type of logic that belongs in CustomLogicConstraint
        return true;
    }

    /**
     * Benefits of the simple constraint approach:
     *
     * 1. SCHEMA GENERATION: XSD and JSON Schema generators can easily extract:
     *    - SimpleRegexConstraint.getRegexPattern() → <xs:pattern value="..."/>
     *    - SimpleLengthConstraint.getMinLength()/getMaxLength() → <xs:minLength/>/<xs:maxLength/>
     *    - SimpleEnumConstraint.getAllowedValues() → <xs:enumeration value="..."/>
     *    - SimplePlacementConstraint.getParentPattern()/getChildPattern() → XSD element restrictions
     *
     * 2. AI DOCUMENTATION: AI doc generators can analyze:
     *    - SimpleConstraint.getTargetDescription() → "Applies to field.string[maxLength]"
     *    - SimpleConstraint.getDescription() → Human-readable constraint purpose
     *    - Constraint data without executing functional interfaces
     *
     * 3. MAINTAINABILITY:
     *    - No complex predicate logic to debug
     *    - Clear, declarative constraint definitions
     *    - Easy to understand and modify
     *
     * 4. TESTABILITY:
     *    - Simple constraints can be unit tested with mock MetaData objects
     *    - No complex setup required for functional interface testing
     *
     * 5. PERFORMANCE:
     *    - Pattern matching is faster than predicate evaluation
     *    - Constraints can be pre-filtered by target patterns
     */
}