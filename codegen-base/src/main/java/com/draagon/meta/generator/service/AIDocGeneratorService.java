package com.draagon.meta.generator.service;

import com.draagon.meta.registry.MetaDataRegistry;

/**
 * AI Documentation Generator service class that extends MetaData types with AI documentation attributes.
 *
 * <p>This service adds attributes needed for AI-assisted documentation generation to existing
 * MetaData types. All attribute names are defined as constants for type safety
 * and consistency across the codebase.</p>
 *
 * <h3>AI Documentation Attributes:</h3>
 * <ul>
 * <li><strong>AI_DESCRIPTION:</strong> AI-friendly description of the field/object</li>
 * <li><strong>AI_BUSINESS_RULE:</strong> Business rule explanation for AI</li>
 * <li><strong>AI_USAGE_CONTEXT:</strong> Context where this field/object is used</li>
 * <li><strong>AI_EXAMPLES:</strong> Example values for AI understanding</li>
 * <li><strong>AI_RELATIONSHIPS:</strong> Relationships to other entities</li>
 * <li><strong>AI_CONSTRAINTS:</strong> Business constraints explanation</li>
 * <li><strong>AI_CATEGORY:</strong> Functional category (core, auxiliary, computed)</li>
 * <li><strong>AI_IMPORTANCE:</strong> Importance level (critical, important, optional)</li>
 * </ul>
 *
 * @since 6.0.0
 */
public class AIDocGeneratorService {

    // AI Description Attributes
    public static final String AI_DESCRIPTION = "aiDescription";
    public static final String AI_PURPOSE = "aiPurpose";
    public static final String AI_BUSINESS_RULE = "aiBusinessRule";
    public static final String AI_USAGE_CONTEXT = "aiUsageContext";

    // AI Example Attributes
    public static final String AI_EXAMPLES = "aiExamples";
    public static final String AI_SAMPLE_VALUES = "aiSampleValues";
    public static final String AI_TYPICAL_RANGE = "aiTypicalRange";
    public static final String AI_EDGE_CASES = "aiEdgeCases";

    // AI Relationship Attributes
    public static final String AI_RELATIONSHIPS = "aiRelationships";
    public static final String AI_DEPENDENCIES = "aiDependencies";
    public static final String AI_AFFECTS = "aiAffects";
    public static final String AI_DERIVED_FROM = "aiDerivedFrom";

    // AI Classification Attributes
    public static final String AI_CATEGORY = "aiCategory";
    public static final String AI_IMPORTANCE = "aiImportance";
    public static final String AI_COMPLEXITY = "aiComplexity";
    public static final String AI_FREQUENCY = "aiFrequency";

    // AI Constraint Attributes
    public static final String AI_CONSTRAINTS = "aiConstraints";
    public static final String AI_VALIDATION_RULES = "aiValidationRules";
    public static final String AI_BUSINESS_CONSTRAINTS = "aiBusinessConstraints";
    public static final String AI_TECHNICAL_CONSTRAINTS = "aiTechnicalConstraints";

    // AI Metadata Enhancement
    public static final String AI_VERSION = "aiVersion";
    public static final String AI_LAST_UPDATED = "aiLastUpdated";
    public static final String AI_CONFIDENCE_SCORE = "aiConfidenceScore";
    public static final String AI_REVIEW_STATUS = "aiReviewStatus";

    // AI Generation Control
    public static final String AI_SKIP_DOCUMENTATION = "aiSkipDocumentation";
    public static final String AI_CUSTOM_TEMPLATE = "aiCustomTemplate";
    public static final String AI_DOCUMENTATION_LEVEL = "aiDocumentationLevel";

    /**
     * Register AI Documentation-specific type extensions with the MetaData registry.
     *
     * <p>This method extends existing MetaData types with attributes needed
     * for AI documentation generation. It follows the extension pattern of finding
     * existing types and adding optional attributes.</p>
     *
     * @param registry The MetaData registry to extend
     */
    public static void registerTypeExtensions(MetaDataRegistry registry) {
        try {
            // Extend field types for AI documentation generation
            registerFieldExtensions(registry);

            // Extend object types for AI documentation generation
            registerObjectExtensions(registry);

            // Extend attribute types for AI documentation metadata
            registerAttributeExtensions(registry);

        } catch (Exception e) {
            // Log error but don't fail - service provider pattern should be resilient
            System.err.println("Warning: Failed to register AI Documentation type extensions: " + e.getMessage());
        }
    }

    /**
     * Extend field types with AI documentation attributes.
     */
    private static void registerFieldExtensions(MetaDataRegistry registry) {
        // String fields get comprehensive AI documentation
        registry.findType("field", "string")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_CONSTRAINTS, "string")
            .optionalAttribute(AI_RELATIONSHIPS, "stringarray");

        // Numeric fields get AI documentation with focus on ranges and constraints
        registry.findType("field", "int")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_EDGE_CASES, "stringarray")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_CONSTRAINTS, "string");

        registry.findType("field", "long")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_CONSTRAINTS, "string");

        registry.findType("field", "double")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_CONSTRAINTS, "string");

        // Date fields get AI documentation with focus on temporal context
        registry.findType("field", "date")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string");

        // Boolean fields get AI documentation with focus on decision logic
        registry.findType("field", "boolean")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_AFFECTS, "stringarray");
    }

    /**
     * Extend object types with AI documentation attributes.
     */
    private static void registerObjectExtensions(MetaDataRegistry registry) {
        registry.findType("object", "pojo")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_RELATIONSHIPS, "stringarray")
            .optionalAttribute(AI_DEPENDENCIES, "stringarray")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string")
            .optionalAttribute(AI_COMPLEXITY, "string")
            .optionalAttribute(AI_FREQUENCY, "string")
            .optionalAttribute(AI_VERSION, "string")
            .optionalAttribute(AI_REVIEW_STATUS, "string");

        registry.findType("object", "proxy")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string");

        registry.findType("object", "map")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_CATEGORY, "string")
            .optionalAttribute(AI_IMPORTANCE, "string");
    }

    /**
     * Extend attribute types with AI documentation metadata support.
     */
    private static void registerAttributeExtensions(MetaDataRegistry registry) {
        registry.findType("attr", "string")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_EXAMPLES, "stringarray")
            .optionalAttribute(AI_CATEGORY, "string");

        registry.findType("attr", "int")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_TYPICAL_RANGE, "string")
            .optionalAttribute(AI_CATEGORY, "string");

        registry.findType("attr", "boolean")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_PURPOSE, "string")
            .optionalAttribute(AI_CATEGORY, "string");
    }

    /**
     * Check if an attribute name is AI documentation-related.
     *
     * @param attributeName The attribute name to check
     * @return True if the attribute is AI documentation-related
     */
    public static boolean isAIDocumentationAttribute(String attributeName) {
        return attributeName != null && attributeName.startsWith("ai");
    }

    /**
     * Get standard AI importance levels.
     *
     * @return Array of standard importance levels
     */
    public static String[] getStandardImportanceLevels() {
        return new String[]{"critical", "important", "useful", "optional", "deprecated"};
    }

    /**
     * Get standard AI categories for fields and objects.
     *
     * @return Array of standard category names
     */
    public static String[] getStandardCategories() {
        return new String[]{"core", "business", "technical", "audit", "computed", "auxiliary", "legacy"};
    }

    /**
     * Get standard AI complexity levels.
     *
     * @return Array of standard complexity levels
     */
    public static String[] getStandardComplexityLevels() {
        return new String[]{"simple", "moderate", "complex", "very-complex"};
    }
}