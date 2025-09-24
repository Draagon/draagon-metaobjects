package com.draagon.meta.generator.direct.metadata.ai;

import com.draagon.meta.generator.direct.metadata.json.SingleJsonDirectGeneratorBase;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * v6.1.0: AI-optimized documentation generator for MetaData type system.
 *
 * Generates comprehensive documentation designed for AI consumption, including:
 * - Inheritance hierarchy visualization
 * - Attribute classification (inherited vs type-specific)
 * - Extension point identification for plugin development
 * - Complete type mapping and implementation guidance
 * - Cross-language compatibility information
 *
 * This generator operates directly from the TypeDefinition registry without requiring
 * any metadata files, making it suitable for JAR-based generation in Maven builds.
 *
 * Usage in Maven plugin:
 * {@code
 * <generator>
 *   <classname>com.draagon.meta.generator.direct.metadata.ai.MetaDataAIDocumentationGenerator</classname>
 *   <args>
 *     <outputDir>${project.build.directory}/generated-docs</outputDir>
 *     <outputFilename>metaobjects-ai-documentation.json</outputFilename>
 *     <includeInheritance>true</includeInheritance>
 *     <includeExtensionGuidance>true</includeExtensionGuidance>
 *     <includeCrossLanguageInfo>false</includeCrossLanguageInfo>
 *   </args>
 * </generator>
 * }
 */
public class MetaDataAIDocumentationGenerator extends SingleJsonDirectGeneratorBase {

    private static final Logger log = LoggerFactory.getLogger(MetaDataAIDocumentationGenerator.class);

    // Configuration options
    private String version = "6.1.0";
    private boolean includeInheritance = true;
    private boolean includeImplementationDetails = true;
    private boolean includeExtensionGuidance = true;
    private boolean includeCrossLanguageInfo = false;

    public MetaDataAIDocumentationGenerator() {
        super();
        log.debug("Initialized AI documentation generator with registry-based type discovery");
    }

    @Override
    protected JsonDirectWriter getWriter(MetaDataLoader loader, OutputStream os) {
        try {
            return new MetaDataAIDocumentationWriter(loader, os)
                    .withVersion(version)
                    .withInheritance(includeInheritance)
                    .withImplementationDetails(includeImplementationDetails)
                    .withExtensionGuidance(includeExtensionGuidance)
                    .withCrossLanguageInfo(includeCrossLanguageInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AI documentation writer", e);
        }
    }

    /**
     * Configure the version string for the generated documentation
     *
     * @param version Version string (default: "6.1.0")
     */
    public void setVersion(String version) {
        this.version = version;
        log.debug("Set documentation version to: {}", version);
    }

    /**
     * Configure whether to include inheritance hierarchy analysis
     *
     * @param includeInheritance true to include inheritance visualization (default: true)
     */
    public void setIncludeInheritance(boolean includeInheritance) {
        this.includeInheritance = includeInheritance;
        log.debug("Set includeInheritance to: {}", includeInheritance);
    }

    /**
     * Configure whether to include implementation details and class mappings
     *
     * @param includeImplementationDetails true to include implementation mapping (default: true)
     */
    public void setIncludeImplementationDetails(boolean includeImplementationDetails) {
        this.includeImplementationDetails = includeImplementationDetails;
        log.debug("Set includeImplementationDetails to: {}", includeImplementationDetails);
    }

    /**
     * Configure whether to include extension guidance for plugin developers
     *
     * @param includeExtensionGuidance true to include extension guidance (default: true)
     */
    public void setIncludeExtensionGuidance(boolean includeExtensionGuidance) {
        this.includeExtensionGuidance = includeExtensionGuidance;
        log.debug("Set includeExtensionGuidance to: {}", includeExtensionGuidance);
    }

    /**
     * Configure whether to include cross-language implementation examples
     *
     * @param includeCrossLanguageInfo true to include C#/TypeScript examples (default: false)
     */
    public void setIncludeCrossLanguageInfo(boolean includeCrossLanguageInfo) {
        this.includeCrossLanguageInfo = includeCrossLanguageInfo;
        log.debug("Set includeCrossLanguageInfo to: {}", includeCrossLanguageInfo);
    }

    @Override
    public String toString() {
        return "MetaDataAIDocumentationGenerator{" +
                "version='" + version + '\'' +
                ", includeInheritance=" + includeInheritance +
                ", includeImplementation=" + includeImplementationDetails +
                ", includeExtension=" + includeExtensionGuidance +
                ", includeCrossLang=" + includeCrossLanguageInfo +
                '}';
    }

    /**
     * Get configuration summary for logging and debugging
     *
     * @return Human-readable configuration summary
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("AI Documentation Generator Configuration:\n");
        summary.append("  Version: ").append(version).append("\n");
        summary.append("  Inheritance Analysis: ").append(includeInheritance ? "Enabled" : "Disabled").append("\n");
        summary.append("  Implementation Details: ").append(includeImplementationDetails ? "Enabled" : "Disabled").append("\n");
        summary.append("  Extension Guidance: ").append(includeExtensionGuidance ? "Enabled" : "Disabled").append("\n");
        summary.append("  Cross-Language Info: ").append(includeCrossLanguageInfo ? "Enabled" : "Disabled");
        return summary.toString();
    }

    ///////////////////////////////////////////////////
    // Service Provider Pattern Registration

    // AI Documentation generation attribute constants
    public static final String AI_VERSION = "aiVersion";
    public static final String AI_DESCRIPTION = "aiDescription";
    public static final String AI_BUSINESS_RULE = "aiBusinessRule";
    public static final String AI_USAGE_CONTEXT = "aiUsageContext";
    public static final String AI_VALIDATION_RULES = "aiValidationRules";
    public static final String AI_EXAMPLES = "aiExamples";
    public static final String AI_CONSTRAINTS = "aiConstraints";
    public static final String AI_EXTENSION_GUIDANCE = "aiExtensionGuidance";
    public static final String AI_CROSS_LANGUAGE_INFO = "aiCrossLanguageInfo";

    /**
     * Registers AI Documentation generation attributes for use by the service provider pattern.
     * Called by CodeGenMetaDataProvider to extend existing MetaData types with AI documentation-specific attributes.
     */
    public static void registerAIDocAttributes(com.draagon.meta.registry.MetaDataRegistry registry) {
        // Object-level AI Documentation attributes
        registry.findType("object", "base")
            .optionalAttribute(AI_VERSION, "string")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_EXTENSION_GUIDANCE, "string")
            .optionalAttribute(AI_CROSS_LANGUAGE_INFO, "string");

        registry.findType("object", "pojo")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string");

        // Field-level AI Documentation attributes
        registry.findType("field", "base")
            .optionalAttribute(AI_DESCRIPTION, "string")
            .optionalAttribute(AI_BUSINESS_RULE, "string")
            .optionalAttribute(AI_USAGE_CONTEXT, "string")
            .optionalAttribute(AI_VALIDATION_RULES, "string")
            .optionalAttribute(AI_EXAMPLES, "string")
            .optionalAttribute(AI_CONSTRAINTS, "string");

        registry.findType("field", "string")
            .optionalAttribute(AI_EXAMPLES, "string")
            .optionalAttribute(AI_VALIDATION_RULES, "string");

        registry.findType("field", "int")
            .optionalAttribute(AI_CONSTRAINTS, "string")
            .optionalAttribute(AI_EXAMPLES, "string");
    }
}