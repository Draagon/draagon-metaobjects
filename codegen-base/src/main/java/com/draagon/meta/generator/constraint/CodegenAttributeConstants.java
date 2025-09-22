/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.generator.constraint;

/**
 * Code generation attribute constants for template-based code generation.
 *
 * <p>This class defines attribute names specific to code generation, template engines,
 * and build-time code generation tools. These attributes are used for:</p>
 * <ul>
 *   <li><strong>JPA Generation Control</strong> - Controlling JPA entity generation</li>
 *   <li><strong>Template Processing</strong> - Template-specific behavior markers</li>
 *   <li><strong>Field Behavior</strong> - Code generation behavioral attributes</li>
 *   <li><strong>Build-Time Generation</strong> - Maven plugin and build tool attributes</li>
 * </ul>
 *
 * <p><strong>Architectural Note:</strong> Database-related attributes (dbTable, dbColumn, etc.)
 * are defined in {@link com.draagon.meta.database.common.DatabaseAttributeConstants} to avoid
 * duplication between OMDB and code generation modules.</p>
 *
 * @since 5.2.0
 */
public final class CodegenAttributeConstants {

    private CodegenAttributeConstants() {
        // Utility class - no instantiation
    }

    // === JPA GENERATION CONTROL ===

    /** Skip JPA generation marker attribute for MetaObjects and MetaFields */
    public static final String ATTR_SKIP_JPA = "skipJpa";

    // === FIELD BEHAVIOR ATTRIBUTES ===

    /** Collection type marker attribute for MetaFields */
    public static final String ATTR_COLLECTION = "collection";

    /** Searchable field marker attribute for MetaFields */
    public static final String ATTR_IS_SEARCHABLE = "isSearchable";

    /** Optional field marker attribute for MetaFields */
    public static final String ATTR_IS_OPTIONAL = "isOptional";

    /** ID field marker attribute for MetaFields */
    public static final String ATTR_IS_ID = "isId";

    // === TEMPLATE GENERATION MARKERS ===

    /** JPA generation marker attribute for MetaObjects and MetaFields */
    public static final String ATTR_HAS_JPA = "hasJpa";

    /** Validation generation marker attribute for MetaObjects and MetaFields */
    public static final String ATTR_HAS_VALIDATION = "hasValidation";

    /** API generation marker attribute for MetaObjects */
    public static final String ATTR_HAS_API = "hasApi";

    /** Test generation marker attribute for MetaObjects */
    public static final String ATTR_HAS_TESTS = "hasTests";

    // === BUILD-TIME GENERATION ATTRIBUTES ===

    /** Code generation package override attribute */
    public static final String ATTR_CODEGEN_PACKAGE = "codegenPackage";

    /** Code generation class name override attribute */
    public static final String ATTR_CODEGEN_CLASS_NAME = "codegenClassName";

    /** Template selection attribute for custom template usage */
    public static final String ATTR_TEMPLATE = "template";

    /** Output directory override for generated files */
    public static final String ATTR_OUTPUT_DIR = "outputDir";

    // === HELPER METHODS ===

    /**
     * Check if an attribute name is a code generation related attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute is code generation related
     */
    public static boolean isCodegenAttribute(String attributeName) {
        if (attributeName == null) {
            return false;
        }

        return ATTR_SKIP_JPA.equals(attributeName) ||
               ATTR_COLLECTION.equals(attributeName) ||
               ATTR_IS_SEARCHABLE.equals(attributeName) ||
               ATTR_IS_OPTIONAL.equals(attributeName) ||
               ATTR_IS_ID.equals(attributeName) ||
               ATTR_HAS_JPA.equals(attributeName) ||
               ATTR_HAS_VALIDATION.equals(attributeName) ||
               ATTR_HAS_API.equals(attributeName) ||
               ATTR_HAS_TESTS.equals(attributeName) ||
               ATTR_CODEGEN_PACKAGE.equals(attributeName) ||
               ATTR_CODEGEN_CLASS_NAME.equals(attributeName) ||
               ATTR_TEMPLATE.equals(attributeName) ||
               ATTR_OUTPUT_DIR.equals(attributeName);
    }

    /**
     * Check if an attribute name is a JPA generation control attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute controls JPA generation
     */
    public static boolean isJpaControlAttribute(String attributeName) {
        return ATTR_SKIP_JPA.equals(attributeName) ||
               ATTR_HAS_JPA.equals(attributeName);
    }

    /**
     * Check if an attribute name is a field behavior attribute.
     *
     * @param attributeName the attribute name to check
     * @return true if the attribute defines field behavior
     */
    public static boolean isFieldBehaviorAttribute(String attributeName) {
        return ATTR_COLLECTION.equals(attributeName) ||
               ATTR_IS_SEARCHABLE.equals(attributeName) ||
               ATTR_IS_OPTIONAL.equals(attributeName) ||
               ATTR_IS_ID.equals(attributeName);
    }
}