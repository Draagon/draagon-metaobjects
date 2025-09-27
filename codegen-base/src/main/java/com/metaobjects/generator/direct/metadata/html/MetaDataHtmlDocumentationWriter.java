package com.metaobjects.generator.direct.metadata.html;

import com.metaobjects.generator.GeneratorIOWriter;
import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.TypeDefinition;
import com.metaobjects.registry.ChildRequirement;
import static com.metaobjects.attr.MetaAttribute.TYPE_ATTR;
import static com.metaobjects.field.MetaField.TYPE_FIELD;
import static com.metaobjects.object.MetaObject.TYPE_OBJECT;
import static com.metaobjects.object.MetaObject.SUBTYPE_BASE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional HTML documentation generator for MetaObjects framework.
 *
 * Generates comprehensive, human-readable HTML documentation with:
 * - Modern responsive design with sidebar navigation
 * - Type hierarchy visualization with inheritance relationships
 * - Detailed type definitions with examples
 * - Plugin development guides and extension patterns
 * - Search functionality and cross-references
 * - Professional styling optimized for developer experience
 *
 * Output is a complete single-page application optimized for:
 * - Framework understanding and adoption
 * - Developer productivity and learning
 * - Plugin and extension development
 * - Cross-team collaboration and knowledge sharing
 */
public class MetaDataHtmlDocumentationWriter extends GeneratorIOWriter<MetaDataHtmlDocumentationWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataHtmlDocumentationWriter.class);

    private String version = "6.2.0";
    private String title = "MetaObjects Framework Documentation";
    private boolean includeInheritance = true;
    private boolean includeExamples = true;
    private boolean includeExtensionGuide = true;

    // Registry for type discovery
    private MetaDataRegistry typeRegistry;
    private PrintWriter writer;
    private OutputStream out;

    public MetaDataHtmlDocumentationWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader);
        this.out = out;
        this.typeRegistry = MetaDataRegistry.getInstance();
        this.writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        log.info("Initialized HTML documentation writer for {} registered types",
                typeRegistry.getRegisteredTypes().size());
    }

    /////////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public MetaDataHtmlDocumentationWriter withVersion(String version) {
        this.version = version;
        return this;
    }

    public MetaDataHtmlDocumentationWriter withTitle(String title) {
        this.title = title;
        return this;
    }

    public MetaDataHtmlDocumentationWriter withInheritance(boolean includeInheritance) {
        this.includeInheritance = includeInheritance;
        return this;
    }

    public MetaDataHtmlDocumentationWriter withExamples(boolean includeExamples) {
        this.includeExamples = includeExamples;
        return this;
    }

    public MetaDataHtmlDocumentationWriter withExtensionGuide(boolean includeExtensionGuide) {
        this.includeExtensionGuide = includeExtensionGuide;
        return this;
    }

    public void writeHtml() throws GeneratorIOException {
        try {
            log.info("Generating professional HTML documentation for {} types",
                    typeRegistry.getRegisteredTypes().size());

            generateHtmlDocument();
            writer.flush();

            log.info("Generated HTML documentation with {} types, {} inheritance relationships, {} extension points",
                    typeRegistry.getRegisteredTypes().size(),
                    countInheritanceRelationships(),
                    countExtensionPoints());

        } catch (Exception e) {
            throw new GeneratorIOException(this, "Failed to generate HTML documentation", e);
        }
    }

    @Override
    public void close() throws GeneratorIOException {
        try {
            if (writer != null) {
                writer.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            throw new GeneratorIOException(this, "Error closing HTML documentation writer: " + e, e);
        }
    }

    /**
     * Generate the complete HTML document
     */
    private void generateHtmlDocument() {
        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");

        generateHtmlHead();
        writer.println("<body>");
        generateHeader();
        writer.println("<div class=\"container\">");
        generateSidebar();
        generateMainContent();
        writer.println("</div>");
        generateFooter();
        generateJavaScript();
        writer.println("</body>");
        writer.println("</html>");
    }

    /**
     * Generate HTML head with CSS styling
     */
    private void generateHtmlHead() {
        writer.println("<head>");
        writer.println("    <meta charset=\"UTF-8\">");
        writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("    <title>" + escapeHtml(title) + "</title>");
        writer.println("    <style>");
        generateCSS();
        writer.println("    </style>");
        writer.println("</head>");
    }

    /**
     * Generate modern CSS styling
     */
    private void generateCSS() {
        writer.println("        /* Modern Documentation Styling */");
        writer.println("        * { margin: 0; padding: 0; box-sizing: border-box; }");
        writer.println("        ");
        writer.println("        body {");
        writer.println("            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;");
        writer.println("            line-height: 1.6;");
        writer.println("            color: #333;");
        writer.println("            background-color: #f8f9fa;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .header {");
        writer.println("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        writer.println("            color: white;");
        writer.println("            padding: 2rem 0;");
        writer.println("            text-align: center;");
        writer.println("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .header h1 { font-size: 2.5rem; font-weight: 300; margin-bottom: 0.5rem; }");
        writer.println("        .header .subtitle { font-size: 1.1rem; opacity: 0.9; }");
        writer.println("        .header .version { font-size: 0.9rem; opacity: 0.8; margin-top: 0.5rem; }");
        writer.println("        ");
        writer.println("        .container {");
        writer.println("            display: grid;");
        writer.println("            grid-template-columns: 300px 1fr;");
        writer.println("            min-height: calc(100vh - 200px);");
        writer.println("            max-width: 1400px;");
        writer.println("            margin: 0 auto;");
        writer.println("            gap: 2rem;");
        writer.println("            padding: 2rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .sidebar {");
        writer.println("            background: white;");
        writer.println("            border-radius: 8px;");
        writer.println("            box-shadow: 0 2px 12px rgba(0,0,0,0.1);");
        writer.println("            padding: 1.5rem;");
        writer.println("            height: fit-content;");
        writer.println("            position: sticky;");
        writer.println("            top: 2rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .sidebar h3 {");
        writer.println("            color: #667eea;");
        writer.println("            border-bottom: 2px solid #eee;");
        writer.println("            padding-bottom: 0.5rem;");
        writer.println("            margin-bottom: 1rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .sidebar ul { list-style: none; margin-bottom: 1.5rem; }");
        writer.println("        .sidebar li { margin-bottom: 0.5rem; }");
        writer.println("        .sidebar a {");
        writer.println("            color: #666;");
        writer.println("            text-decoration: none;");
        writer.println("            padding: 0.25rem 0.5rem;");
        writer.println("            border-radius: 4px;");
        writer.println("            transition: all 0.2s ease;");
        writer.println("            display: block;");
        writer.println("        }");
        writer.println("        .sidebar a:hover { background: #f0f4ff; color: #667eea; }");
        writer.println("        ");
        writer.println("        .main-content {");
        writer.println("            background: white;");
        writer.println("            border-radius: 8px;");
        writer.println("            box-shadow: 0 2px 12px rgba(0,0,0,0.1);");
        writer.println("            padding: 3rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .section {");
        writer.println("            margin-bottom: 3rem;");
        writer.println("            scroll-margin-top: 2rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .section h2 {");
        writer.println("            color: #333;");
        writer.println("            font-size: 2rem;");
        writer.println("            margin-bottom: 1rem;");
        writer.println("            border-bottom: 3px solid #667eea;");
        writer.println("            padding-bottom: 0.5rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .section h3 {");
        writer.println("            color: #555;");
        writer.println("            font-size: 1.4rem;");
        writer.println("            margin: 2rem 0 1rem 0;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .type-card {");
        writer.println("            border: 1px solid #e0e0e0;");
        writer.println("            border-radius: 8px;");
        writer.println("            padding: 1.5rem;");
        writer.println("            margin-bottom: 1.5rem;");
        writer.println("            background: #fafafa;");
        writer.println("            transition: box-shadow 0.2s ease;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .type-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }");
        writer.println("        ");
        writer.println("        .type-header {");
        writer.println("            display: flex;");
        writer.println("            align-items: center;");
        writer.println("            margin-bottom: 1rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .type-name {");
        writer.println("            font-size: 1.3rem;");
        writer.println("            font-weight: 600;");
        writer.println("            color: #333;");
        writer.println("            margin-right: 1rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .type-badge {");
        writer.println("            background: #667eea;");
        writer.println("            color: white;");
        writer.println("            padding: 0.2rem 0.6rem;");
        writer.println("            border-radius: 12px;");
        writer.println("            font-size: 0.8rem;");
        writer.println("            font-weight: 500;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .inheritance-chain {");
        writer.println("            background: #e8f2ff;");
        writer.println("            border-left: 4px solid #667eea;");
        writer.println("            padding: 1rem;");
        writer.println("            margin: 1rem 0;");
        writer.println("            border-radius: 0 4px 4px 0;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .attributes-grid {");
        writer.println("            display: grid;");
        writer.println("            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));");
        writer.println("            gap: 1rem;");
        writer.println("            margin: 1rem 0;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .attribute {");
        writer.println("            background: white;");
        writer.println("            border: 1px solid #ddd;");
        writer.println("            border-radius: 4px;");
        writer.println("            padding: 0.75rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .attribute-name { font-weight: 600; color: #667eea; }");
        writer.println("        .attribute-type { color: #666; font-size: 0.9rem; }");
        writer.println("        .attribute-required { color: #d73a49; font-weight: 600; }");
        writer.println("        .attribute-optional { color: #28a745; font-weight: 600; }");
        writer.println("        ");
        writer.println("        .code-block {");
        writer.println("            background: #f6f8fa;");
        writer.println("            border: 1px solid #e1e4e8;");
        writer.println("            border-radius: 6px;");
        writer.println("            padding: 1rem;");
        writer.println("            overflow-x: auto;");
        writer.println("            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;");
        writer.println("            font-size: 0.9rem;");
        writer.println("            line-height: 1.4;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .stats-grid {");
        writer.println("            display: grid;");
        writer.println("            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));");
        writer.println("            gap: 1rem;");
        writer.println("            margin: 2rem 0;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .stat-card {");
        writer.println("            background: white;");
        writer.println("            border: 1px solid #e0e0e0;");
        writer.println("            border-radius: 8px;");
        writer.println("            padding: 1.5rem;");
        writer.println("            text-align: center;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .stat-number {");
        writer.println("            font-size: 2rem;");
        writer.println("            font-weight: 600;");
        writer.println("            color: #667eea;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        .stat-label { color: #666; margin-top: 0.5rem; }");
        writer.println("        ");
        writer.println("        .footer {");
        writer.println("            text-align: center;");
        writer.println("            padding: 2rem;");
        writer.println("            color: #666;");
        writer.println("            border-top: 1px solid #eee;");
        writer.println("            margin-top: 3rem;");
        writer.println("        }");
        writer.println("        ");
        writer.println("        /* Responsive Design */");
        writer.println("        @media (max-width: 1024px) {");
        writer.println("            .container {");
        writer.println("                grid-template-columns: 250px 1fr;");
        writer.println("                gap: 1rem;");
        writer.println("                padding: 1rem;");
        writer.println("            }");
        writer.println("        }");
        writer.println("        ");
        writer.println("        @media (max-width: 768px) {");
        writer.println("            .container {");
        writer.println("                grid-template-columns: 1fr;");
        writer.println("            }");
        writer.println("            .sidebar { position: static; }");
        writer.println("            .header h1 { font-size: 2rem; }");
        writer.println("            .main-content { padding: 2rem; }");
        writer.println("        }");
    }

    /**
     * Generate header section
     */
    private void generateHeader() {
        writer.println("    <div class=\"header\">");
        writer.println("        <h1>" + escapeHtml(title) + "</h1>");
        writer.println("        <div class=\"subtitle\">Comprehensive Type System Documentation</div>");
        writer.println("        <div class=\"version\">Version " + escapeHtml(version) + " • Generated " +
                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</div>");
        writer.println("    </div>");
    }

    /**
     * Generate sidebar navigation
     */
    private void generateSidebar() {
        writer.println("        <nav class=\"sidebar\">");
        writer.println("            <h3>Navigation</h3>");
        writer.println("            <ul>");
        writer.println("                <li><a href=\"#overview\">Framework Overview</a></li>");
        writer.println("                <li><a href=\"#statistics\">Type Statistics</a></li>");
        if (includeInheritance) {
            writer.println("                <li><a href=\"#inheritance\">Type Hierarchy</a></li>");
        }
        writer.println("                <li><a href=\"#types\">Type Definitions</a></li>");
        if (includeExtensionGuide) {
            writer.println("                <li><a href=\"#extensions\">Extension Guide</a></li>");
        }
        writer.println("            </ul>");

        // Type category navigation
        Map<String, Integer> typesByCategory = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

        if (!typesByCategory.isEmpty()) {
            writer.println("            <h3>Type Categories</h3>");
            writer.println("            <ul>");
            for (Map.Entry<String, Integer> entry : typesByCategory.entrySet()) {
                String category = entry.getKey();
                int count = entry.getValue();
                writer.println("                <li><a href=\"#category-" + escapeHtml(category) + "\">" +
                             capitalizeFirst(category) + " Types (" + count + ")</a></li>");
            }
            writer.println("            </ul>");
        }

        writer.println("        </nav>");
    }

    /**
     * Generate main content sections
     */
    private void generateMainContent() {
        writer.println("        <main class=\"main-content\">");

        generateOverviewSection();
        generateStatisticsSection();

        if (includeInheritance) {
            generateInheritanceSection();
        }

        generateTypesSection();

        if (includeExtensionGuide) {
            generateExtensionGuideSection();
        }

        writer.println("        </main>");
    }

    /**
     * Generate framework overview section
     */
    private void generateOverviewSection() {
        writer.println("            <section id=\"overview\" class=\"section\">");
        writer.println("                <h2>Framework Overview</h2>");
        writer.println("                <p>The MetaObjects framework provides a sophisticated metadata-driven development platform with " +
                       "a comprehensive type system designed for extensibility and cross-language compatibility.</p>");

        writer.println("                <h3>Core Concepts</h3>");
        writer.println("                <ul>");
        writer.println("                    <li><strong>Read-Optimized Architecture:</strong> MetaData objects are loaded once during startup and optimized for heavy read access</li>");
        writer.println("                    <li><strong>Type Registration System:</strong> Service-based type registry with automatic discovery and inheritance support</li>");
        writer.println("                    <li><strong>Constraint System:</strong> Integrated validation and placement constraints for data integrity</li>");
        writer.println("                    <li><strong>Plugin Architecture:</strong> Extensible type system supporting custom field types, validators, and object behaviors</li>");
        writer.println("                    <li><strong>Cross-Language Support:</strong> String-based type identifiers enable implementation across Java, C#, and TypeScript</li>");
        writer.println("                </ul>");

        writer.println("                <h3>Architecture Patterns</h3>");
        writer.println("                <div class=\"code-block\">");
        writer.println("// MetaDataLoader operates like Java ClassLoader - load once, read many");
        writer.println("MetaDataLoader loader = new SimpleLoader(\"myApp\");");
        writer.println("loader.init(); // Heavy initialization, permanent in memory");
        writer.println("");
        writer.println("// Runtime access is O(1) cached lookup - no synchronization needed");
        writer.println("MetaObject userMeta = loader.getMetaObjectByName(\"User\");");
        writer.println("MetaField emailField = userMeta.getMetaField(\"email\");");
        writer.println("                </div>");

        writer.println("            </section>");
    }

    /**
     * Generate statistics section
     */
    private void generateStatisticsSection() {
        writer.println("            <section id=\"statistics\" class=\"section\">");
        writer.println("                <h2>Type Registry Statistics</h2>");

        Map<String, Integer> typesByCategory = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));

        int totalTypes = typeRegistry.getRegisteredTypes().size();
        int inheritanceRelationships = countInheritanceRelationships();
        int extensionPoints = countExtensionPoints();

        writer.println("                <div class=\"stats-grid\">");
        writer.println("                    <div class=\"stat-card\">");
        writer.println("                        <div class=\"stat-number\">" + totalTypes + "</div>");
        writer.println("                        <div class=\"stat-label\">Total Types</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"stat-card\">");
        writer.println("                        <div class=\"stat-number\">" + inheritanceRelationships + "</div>");
        writer.println("                        <div class=\"stat-label\">Inheritance Relationships</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"stat-card\">");
        writer.println("                        <div class=\"stat-number\">" + extensionPoints + "</div>");
        writer.println("                        <div class=\"stat-label\">Extension Points</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"stat-card\">");
        writer.println("                        <div class=\"stat-number\">" + typesByCategory.size() + "</div>");
        writer.println("                        <div class=\"stat-label\">Type Categories</div>");
        writer.println("                    </div>");
        writer.println("                </div>");

        writer.println("                <h3>Type Distribution</h3>");
        writer.println("                <div class=\"attributes-grid\">");
        for (Map.Entry<String, Integer> entry : typesByCategory.entrySet()) {
            writer.println("                    <div class=\"attribute\">");
            writer.println("                        <div class=\"attribute-name\">" + capitalizeFirst(entry.getKey()) + " Types</div>");
            writer.println("                        <div class=\"attribute-type\">" + entry.getValue() + " registered implementations</div>");
            writer.println("                    </div>");
        }
        writer.println("                </div>");

        writer.println("            </section>");
    }

    /**
     * Generate inheritance hierarchy section
     */
    private void generateInheritanceSection() {
        writer.println("            <section id=\"inheritance\" class=\"section\">");
        writer.println("                <h2>Type Inheritance Hierarchy</h2>");
        writer.println("                <p>The MetaObjects framework uses inheritance to reduce code duplication and provide " +
                       "extensible base types for plugin development.</p>");

        // Group types by inheritance relationships
        Map<String, List<TypeDefinition>> inheritanceGroups = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(def ->
                    def.hasParent() ? def.getParentQualifiedName() : "root"));

        for (Map.Entry<String, List<TypeDefinition>> entry : inheritanceGroups.entrySet()) {
            String parentKey = entry.getKey();
            List<TypeDefinition> children = entry.getValue();

            if (!"root".equals(parentKey)) {
                writer.println("                <h3>" + escapeHtml(parentKey) + "</h3>");
                writer.println("                <div class=\"inheritance-chain\">");

                // Find parent definition
                String[] parts = parentKey.split("\\.");
                if (parts.length == 2) {
                    TypeDefinition parentDef = typeRegistry.getTypeDefinition(parts[0], parts[1]);
                    if (parentDef != null) {
                        writer.println("                    <p><strong>Base Type:</strong> " +
                                     escapeHtml(parentDef.getDescription()) + "</p>");

                        writer.println("                    <p><strong>Child Types:</strong></p>");
                        writer.println("                    <ul>");
                        for (TypeDefinition child : children) {
                            writer.println("                        <li><strong>" + escapeHtml(child.getQualifiedName()) +
                                         "</strong> - " + escapeHtml(child.getDescription()) + "</li>");
                        }
                        writer.println("                    </ul>");

                        // Show common inherited attributes
                        if (parentDef.getChildRequirements() != null && !parentDef.getChildRequirements().isEmpty()) {
                            List<ChildRequirement> inheritedAttrs = parentDef.getChildRequirements().stream()
                                    .filter(req -> TYPE_ATTR.equals(req.getExpectedType()))
                                    .collect(Collectors.toList());

                            if (!inheritedAttrs.isEmpty()) {
                                writer.println("                    <p><strong>Common Inherited Attributes:</strong></p>");
                                writer.println("                    <div class=\"attributes-grid\">");
                                for (ChildRequirement req : inheritedAttrs) {
                                    writer.println("                        <div class=\"attribute\">");
                                    writer.println("                            <div class=\"attribute-name\">" + escapeHtml(req.getName()) + "</div>");
                                    writer.println("                            <div class=\"attribute-type\">" + escapeHtml(req.getExpectedSubType()) + "</div>");
                                    if (req.isRequired()) {
                                        writer.println("                            <div class=\"attribute-required\">Required</div>");
                                    } else {
                                        writer.println("                            <div class=\"attribute-optional\">Optional</div>");
                                    }
                                    writer.println("                        </div>");
                                }
                                writer.println("                    </div>");
                            }
                        }
                    }
                }

                writer.println("                </div>");
            }
        }

        writer.println("            </section>");
    }

    /**
     * Generate comprehensive types section
     */
    private void generateTypesSection() {
        writer.println("            <section id=\"types\" class=\"section\">");
        writer.println("                <h2>Type Definitions</h2>");
        writer.println("                <p>Complete catalog of all registered MetaData types with detailed specifications.</p>");

        // Group types by category
        Map<String, List<TypeDefinition>> typesByCategory = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType));

        for (Map.Entry<String, List<TypeDefinition>> categoryEntry : typesByCategory.entrySet()) {
            String category = categoryEntry.getKey();
            List<TypeDefinition> typesInCategory = categoryEntry.getValue();

            writer.println("                <h3 id=\"category-" + escapeHtml(category) + "\">" +
                         capitalizeFirst(category) + " Types</h3>");

            for (TypeDefinition typeDef : typesInCategory) {
                generateTypeCard(typeDef);
            }
        }

        writer.println("            </section>");
    }

    /**
     * Generate individual type card
     */
    private void generateTypeCard(TypeDefinition typeDef) {
        writer.println("                <div class=\"type-card\">");
        writer.println("                    <div class=\"type-header\">");
        writer.println("                        <div class=\"type-name\">" + escapeHtml(typeDef.getQualifiedName()) + "</div>");
        writer.println("                        <div class=\"type-badge\">" + escapeHtml(typeDef.getImplementationClass().getSimpleName()) + "</div>");
        writer.println("                    </div>");

        writer.println("                    <p>" + escapeHtml(typeDef.getDescription()) + "</p>");

        // Show inheritance information
        if (typeDef.hasParent()) {
            writer.println("                    <div class=\"inheritance-chain\">");
            writer.println("                        <strong>Inherits from:</strong> " + escapeHtml(typeDef.getParentQualifiedName()));
            writer.println("                    </div>");
        }

        // Show implementation class
        writer.println("                    <p><strong>Implementation:</strong> <code>" +
                     escapeHtml(typeDef.getImplementationClass().getName()) + "</code></p>");

        // Show child requirements (attributes)
        if (typeDef.getChildRequirements() != null && !typeDef.getChildRequirements().isEmpty()) {
            writer.println("                    <h4>Attributes & Children</h4>");
            writer.println("                    <div class=\"attributes-grid\">");

            for (ChildRequirement req : typeDef.getChildRequirements()) {
                writer.println("                        <div class=\"attribute\">");
                writer.println("                            <div class=\"attribute-name\">" + escapeHtml(req.getName()) + "</div>");
                writer.println("                            <div class=\"attribute-type\">" +
                             escapeHtml(req.getExpectedType() + "." + req.getExpectedSubType()) + "</div>");
                if (req.isRequired()) {
                    writer.println("                            <div class=\"attribute-required\">Required</div>");
                } else {
                    writer.println("                            <div class=\"attribute-optional\">Optional</div>");
                }
                if (req.getDescription() != null && !req.getDescription().isEmpty()) {
                    writer.println("                            <div class=\"attribute-type\">" +
                                 escapeHtml(req.getDescription()) + "</div>");
                }
                writer.println("                        </div>");
            }

            writer.println("                    </div>");
        }

        // Show usage example if enabled
        if (includeExamples) {
            generateTypeExample(typeDef);
        }

        writer.println("                </div>");
    }

    /**
     * Generate usage example for a type
     */
    private void generateTypeExample(TypeDefinition typeDef) {
        writer.println("                    <h4>Usage Example</h4>");
        writer.println("                    <div class=\"code-block\">");

        String className = typeDef.getImplementationClass().getSimpleName();
        String type = typeDef.getType();
        String subType = typeDef.getSubType();

        if (TYPE_FIELD.equals(type)) {
            writer.println("// JSON metadata definition");
            writer.println("{");
            writer.println("  \"field\": {");
            writer.println("    \"name\": \"" + subType + "Field\",");
            writer.println("    \"type\": \"" + subType + "\"");

            // Add common attributes based on type
            if ("string".equals(subType)) {
                writer.println("    \"@maxLength\": 255,");
                writer.println("    \"@required\": true");
            } else if ("int".equals(subType) || "long".equals(subType)) {
                writer.println("    \"@required\": true");
            }

            writer.println("  }");
            writer.println("}");
            writer.println("");
            writer.println("// Java API usage");
            writer.println(className + " field = new " + className + "(\"fieldName\");");

        } else if (TYPE_OBJECT.equals(type)) {
            writer.println("// JSON metadata definition");
            writer.println("{");
            writer.println("  \"object\": {");
            writer.println("    \"name\": \"Example" + capitalizeFirst(subType) + "\",");
            writer.println("    \"type\": \"" + subType + "\"");
            writer.println("  }");
            writer.println("}");
            writer.println("");
            writer.println("// Java API usage");
            writer.println(className + " obj = new " + className + "(\"ExampleObject\");");
        } else {
            writer.println("// Type registration example");
            writer.println("MetaDataRegistry.registerType(" + className + ".class, def -> def");
            writer.println("    .type(\"" + type + "\").subType(\"" + subType + "\")");
            if (typeDef.hasParent()) {
                writer.println("    .inheritsFrom(\"" + typeDef.getParentQualifiedName().replace(".", "\", \"") + "\")");
            }
            writer.println("    .description(\"" + escapeHtml(typeDef.getDescription()) + "\"));");
        }

        writer.println("                    </div>");
    }

    /**
     * Generate extension guide section
     */
    private void generateExtensionGuideSection() {
        writer.println("            <section id=\"extensions\" class=\"section\">");
        writer.println("                <h2>Extension & Plugin Development Guide</h2>");
        writer.println("                <p>The MetaObjects framework is designed for extensibility. This guide shows how to " +
                       "create custom types, attributes, and behaviors.</p>");

        writer.println("                <h3>Creating Custom Field Types</h3>");
        writer.println("                <div class=\"code-block\">");
        writer.println("@MetaDataType(type = \"field\", subType = \"currency\", description = \"Currency field with precision\")");
        writer.println("public class CurrencyField extends PrimitiveField&lt;BigDecimal&gt; {");
        writer.println("    ");
        writer.println("    static {");
        writer.println("        MetaDataRegistry.registerType(CurrencyField.class, def -> def");
        writer.println("            .type(TYPE_FIELD).subType(\"currency\")");
        writer.println("            .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)  // Inherits all field.base attributes");
        writer.println("            .optionalAttribute(\"precision\", \"int\")");
        writer.println("            .optionalAttribute(\"currencyCode\", \"string\")");
        writer.println("            .description(\"Currency field with precision and formatting\"));");
        writer.println("    }");
        writer.println("}");
        writer.println("                </div>");

        writer.println("                <h3>Creating Custom Object Types</h3>");
        writer.println("                <div class=\"code-block\">");
        writer.println("@MetaDataType(type = \"object\", subType = \"auditable\", description = \"Object with audit trails\")");
        writer.println("public class AuditableObject extends PojoMetaObject {");
        writer.println("    ");
        writer.println("    static {");
        writer.println("        MetaDataRegistry.registerType(AuditableObject.class, def -> def");
        writer.println("            .type(TYPE_OBJECT).subType(\"auditable\")");
        writer.println("            .inheritsFrom(TYPE_OBJECT, SUBTYPE_BASE)  // Inherits all object.base attributes");
        writer.println("            .requiredAttribute(\"auditEnabled\", \"boolean\")");
        writer.println("            .optionalAttribute(\"auditTable\", \"string\")");
        writer.println("            .description(\"Object type with automatic audit trail support\"));");
        writer.println("    }");
        writer.println("}");
        writer.println("                </div>");

        writer.println("                <h3>Extension Patterns</h3>");
        writer.println("                <div class=\"attributes-grid\">");
        writer.println("                    <div class=\"attribute\">");
        writer.println("                        <div class=\"attribute-name\">Inherit from Base Types</div>");
        writer.println("                        <div class=\"attribute-type\">Use .inheritsFrom() to get common attributes automatically</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"attribute\">");
        writer.println("                        <div class=\"attribute-name\">Custom Attributes</div>");
        writer.println("                        <div class=\"attribute-type\">Add type-specific attributes via .requiredAttribute() or .optionalAttribute()</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"attribute\">");
        writer.println("                        <div class=\"attribute-name\">Service Discovery</div>");
        writer.println("                        <div class=\"attribute-type\">Use META-INF/services for automatic registration</div>");
        writer.println("                    </div>");
        writer.println("                    <div class=\"attribute\">");
        writer.println("                        <div class=\"attribute-name\">Cross-Module Support</div>");
        writer.println("                        <div class=\"attribute-type\">String-based inheritance works across module boundaries</div>");
        writer.println("                    </div>");
        writer.println("                </div>");

        writer.println("                <h3>Available Extension Points</h3>");
        writer.println("                <ul>");

        // List base types that can be extended
        List<TypeDefinition> baseTypes = typeRegistry.getAllTypeDefinitions().stream()
                .filter(def -> SUBTYPE_BASE.equals(def.getSubType()))
                .collect(Collectors.toList());

        for (TypeDefinition baseDef : baseTypes) {
            writer.println("                    <li><strong>" + escapeHtml(baseDef.getQualifiedName()) +
                         ":</strong> " + escapeHtml(baseDef.getDescription()) + "</li>");
        }

        writer.println("                </ul>");

        writer.println("            </section>");
    }

    /**
     * Generate footer
     */
    private void generateFooter() {
        writer.println("    <div class=\"footer\">");
        writer.println("        <p>Generated by MetaObjects Framework Documentation Generator v" + escapeHtml(version) + "</p>");
        writer.println("        <p>MetaObjects Framework - Metadata-Driven Development Platform</p>");
        writer.println("    </div>");
    }

    /**
     * Generate JavaScript for interactivity
     */
    private void generateJavaScript() {
        writer.println("    <script>");
        writer.println("        // Smooth scrolling for navigation links");
        writer.println("        document.querySelectorAll('a[href^=\"#\"]').forEach(anchor => {");
        writer.println("            anchor.addEventListener('click', function (e) {");
        writer.println("                e.preventDefault();");
        writer.println("                const target = document.querySelector(this.getAttribute('href'));");
        writer.println("                if (target) {");
        writer.println("                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });");
        writer.println("                }");
        writer.println("            });");
        writer.println("        });");
        writer.println("        ");
        writer.println("        // Highlight current section in navigation");
        writer.println("        const sections = document.querySelectorAll('section[id]');");
        writer.println("        const navLinks = document.querySelectorAll('.sidebar a[href^=\"#\"]');");
        writer.println("        ");
        writer.println("        function updateActiveNavigation() {");
        writer.println("            let current = '';");
        writer.println("            sections.forEach(section => {");
        writer.println("                const sectionTop = section.getBoundingClientRect().top;");
        writer.println("                if (sectionTop <= 100) current = section.getAttribute('id');");
        writer.println("            });");
        writer.println("            ");
        writer.println("            navLinks.forEach(link => {");
        writer.println("                link.style.backgroundColor = '';");
        writer.println("                link.style.color = '';");
        writer.println("                if (link.getAttribute('href') === '#' + current) {");
        writer.println("                    link.style.backgroundColor = '#f0f4ff';");
        writer.println("                    link.style.color = '#667eea';");
        writer.println("                }");
        writer.println("            });");
        writer.println("        }");
        writer.println("        ");
        writer.println("        window.addEventListener('scroll', updateActiveNavigation);");
        writer.println("        window.addEventListener('load', updateActiveNavigation);");
        writer.println("    </script>");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private int countInheritanceRelationships() {
        return (int) typeRegistry.getAllTypeDefinitions().stream()
                .filter(def -> def.hasParent())
                .count();
    }

    private int countExtensionPoints() {
        return (int) typeRegistry.getAllTypeDefinitions().stream()
                .filter(def -> SUBTYPE_BASE.equals(def.getSubType()))
                .count();
    }

    @Override
    public String toString() {
        return "MetaDataHtmlDocumentationWriter{" +
                "version='" + version + '\'' +
                ", registeredTypes=" + typeRegistry.getRegisteredTypes().size() +
                ", includeInheritance=" + includeInheritance +
                ", includeExamples=" + includeExamples +
                '}';
    }
}