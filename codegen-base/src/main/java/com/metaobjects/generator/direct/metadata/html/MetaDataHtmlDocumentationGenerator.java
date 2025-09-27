package com.metaobjects.generator.direct.metadata.html;

import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Professional HTML documentation generator for MetaObjects framework.
 *
 * Generates comprehensive, human-readable HTML documentation with:
 * - Modern responsive design with sidebar navigation
 * - Type hierarchy visualization with inheritance relationships
 * - Detailed type definitions with examples and usage patterns
 * - Plugin development guides and extension patterns
 * - Search functionality and cross-references
 * - Professional styling optimized for developer experience
 *
 * This generator operates directly from the TypeDefinition registry without requiring
 * any metadata files, making it suitable for JAR-based generation in Maven builds.
 *
 * Usage in Maven plugin:
 * {@code
 * <generator>
 *   <classname>com.metaobjects.generator.direct.metadata.html.MetaDataHtmlDocumentationGenerator</classname>
 *   <args>
 *     <outputDir>${project.build.directory}/generated-docs</outputDir>
 *     <outputFilename>metaobjects-documentation.html</outputFilename>
 *     <title>MetaObjects Framework Documentation</title>
 *     <version>6.2.0</version>
 *     <includeInheritance>true</includeInheritance>
 *     <includeExamples>true</includeExamples>
 *     <includeExtensionGuide>true</includeExtensionGuide>
 *   </args>
 * </generator>
 * }
 */
public class MetaDataHtmlDocumentationGenerator extends SingleHtmlDirectGeneratorBase {

    private static final Logger log = LoggerFactory.getLogger(MetaDataHtmlDocumentationGenerator.class);

    // Configuration options
    private String version = "6.2.0";
    private String title = "MetaObjects Framework Documentation";
    private boolean includeInheritance = true;
    private boolean includeExamples = true;
    private boolean includeExtensionGuide = true;

    public MetaDataHtmlDocumentationGenerator() {
        super();
        log.debug("Initialized HTML documentation generator with registry-based type discovery");
    }

    @Override
    protected MetaDataHtmlDocumentationWriter getWriter(MetaDataLoader loader, OutputStream os) throws GeneratorIOException {
        try {
            return new MetaDataHtmlDocumentationWriter(loader, os)
                    .withVersion(version)
                    .withTitle(title)
                    .withInheritance(includeInheritance)
                    .withExamples(includeExamples)
                    .withExtensionGuide(includeExtensionGuide);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HTML documentation writer", e);
        }
    }

    /**
     * Configure the version string for the generated documentation
     *
     * @param version Version string (default: "6.2.0")
     */
    public void setVersion(String version) {
        this.version = version;
        log.debug("Set documentation version to: {}", version);
    }

    /**
     * Configure the title for the generated documentation
     *
     * @param title Documentation title (default: "MetaObjects Framework Documentation")
     */
    public void setTitle(String title) {
        this.title = title;
        log.debug("Set documentation title to: {}", title);
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
     * Configure whether to include usage examples for types
     *
     * @param includeExamples true to include usage examples (default: true)
     */
    public void setIncludeExamples(boolean includeExamples) {
        this.includeExamples = includeExamples;
        log.debug("Set includeExamples to: {}", includeExamples);
    }

    /**
     * Configure whether to include extension guide for plugin developers
     *
     * @param includeExtensionGuide true to include extension guide (default: true)
     */
    public void setIncludeExtensionGuide(boolean includeExtensionGuide) {
        this.includeExtensionGuide = includeExtensionGuide;
        log.debug("Set includeExtensionGuide to: {}", includeExtensionGuide);
    }

    @Override
    public String toString() {
        return "MetaDataHtmlDocumentationGenerator{" +
                "version='" + version + '\'' +
                ", title='" + title + '\'' +
                ", includeInheritance=" + includeInheritance +
                ", includeExamples=" + includeExamples +
                ", includeExtensionGuide=" + includeExtensionGuide +
                '}';
    }

    /**
     * Get configuration summary for logging and debugging
     *
     * @return Human-readable configuration summary
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("HTML Documentation Generator Configuration:\n");
        summary.append("  Title: ").append(title).append("\n");
        summary.append("  Version: ").append(version).append("\n");
        summary.append("  Inheritance Analysis: ").append(includeInheritance ? "Enabled" : "Disabled").append("\n");
        summary.append("  Usage Examples: ").append(includeExamples ? "Enabled" : "Disabled").append("\n");
        summary.append("  Extension Guide: ").append(includeExtensionGuide ? "Enabled" : "Disabled");
        return summary.toString();
    }

    ///////////////////////////////////////////////////
    // Specialized HTML Documentation Features

    /**
     * HTML documentation generation attribute constants for enhanced documentation
     */
    public static final String HTML_TITLE = "htmlTitle";
    public static final String HTML_DESCRIPTION = "htmlDescription";
    public static final String HTML_EXAMPLE = "htmlExample";
    public static final String HTML_USAGE_PATTERN = "htmlUsagePattern";
    public static final String HTML_EXTENSION_GUIDE = "htmlExtensionGuide";
    public static final String HTML_SEE_ALSO = "htmlSeeAlso";
    public static final String HTML_SINCE_VERSION = "htmlSinceVersion";
    public static final String HTML_DEPRECATED = "htmlDeprecated";

    /**
     * Registers HTML Documentation generation attributes for enhanced documentation.
     * Can be called by plugins to add rich documentation metadata to their types.
     */
    public static void registerHtmlDocAttributes(com.metaobjects.registry.MetaDataRegistry registry) {
        // Object-level HTML Documentation attributes
        registry.findType("object", "base")
            .optionalAttribute(HTML_TITLE, "string")
            .optionalAttribute(HTML_DESCRIPTION, "string")
            .optionalAttribute(HTML_EXAMPLE, "string")
            .optionalAttribute(HTML_USAGE_PATTERN, "string")
            .optionalAttribute(HTML_EXTENSION_GUIDE, "string")
            .optionalAttribute(HTML_SEE_ALSO, "string")
            .optionalAttribute(HTML_SINCE_VERSION, "string")
            .optionalAttribute(HTML_DEPRECATED, "string");

        // Field-level HTML Documentation attributes
        registry.findType("field", "base")
            .optionalAttribute(HTML_TITLE, "string")
            .optionalAttribute(HTML_DESCRIPTION, "string")
            .optionalAttribute(HTML_EXAMPLE, "string")
            .optionalAttribute(HTML_USAGE_PATTERN, "string")
            .optionalAttribute(HTML_SEE_ALSO, "string")
            .optionalAttribute(HTML_SINCE_VERSION, "string")
            .optionalAttribute(HTML_DEPRECATED, "string");

        // Enhanced examples for specific field types
        registry.findType("field", "string")
            .optionalAttribute(HTML_EXAMPLE, "string")
            .optionalAttribute(HTML_USAGE_PATTERN, "string");

        registry.findType("field", "int")
            .optionalAttribute(HTML_EXAMPLE, "string")
            .optionalAttribute(HTML_USAGE_PATTERN, "string");

        log.info("Registered HTML documentation attributes for enhanced type documentation");
    }
}