package com.metaobjects.generator.mustache;

import com.metaobjects.generator.Generator;
import com.metaobjects.generator.GeneratorException;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.generator.util.GeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Mustache-based template generator for MetaObjects.
 * Integrates with the existing MetaObjects Maven plugin system to provide
 * cross-language code generation using Mustache templates.
 * 
 * Based on the cross-language template system architecture documented in
 * .claude/archive/template-system/TEMPLATE_IMPLEMENTATION_GUIDE.md and .claude/archive/template-system/TEMPLATE_SYSTEM_ARCHITECTURE.md
 */
public class MustacheTemplateGenerator implements Generator {
    
    private static final Logger log = LoggerFactory.getLogger(MustacheTemplateGenerator.class);
    
    // Configuration parameters
    public static final String PARAM_TEMPLATE_PATH = "templatePath";
    public static final String PARAM_OUTPUT_DIR = "outputDir";
    public static final String PARAM_PACKAGE_PREFIX = "packagePrefix";
    public static final String PARAM_TARGET_LANGUAGE = "targetLanguage";
    public static final String PARAM_TEMPLATE_NAME = "templateName";
    
    private MustacheTemplateEngine templateEngine;
    private TemplateParser templateParser;
    private Map<String, String> args = new HashMap<>();
    private List<String> filters = new ArrayList<>();
    private List<String> scripts = new ArrayList<>();
    
    // Configuration
    private String templatePath = "templates/";
    private String outputDir = "target/generated-sources/metaobjects";
    private String packagePrefix = "";
    private String targetLanguage = "java";
    private String templateName = null; // If null, auto-discover templates
    
    @Override
    public Generator setArgs(Map<String, String> args) {
        this.args = args != null ? new HashMap<>(args) : new HashMap<>();
        
        // Parse configuration parameters from args
        this.templatePath = this.args.getOrDefault(PARAM_TEMPLATE_PATH, this.templatePath);
        this.outputDir = this.args.getOrDefault(PARAM_OUTPUT_DIR, this.outputDir);
        this.packagePrefix = this.args.getOrDefault(PARAM_PACKAGE_PREFIX, this.packagePrefix);
        this.targetLanguage = this.args.getOrDefault(PARAM_TARGET_LANGUAGE, this.targetLanguage);
        this.templateName = this.args.get(PARAM_TEMPLATE_NAME);
        
        if (!templatePath.endsWith("/")) {
            templatePath += "/";
        }
        
        log.info("Mustache Template Generator configured:");
        log.info("  Template Path: {}", templatePath);
        log.info("  Output Directory: {}", outputDir);
        log.info("  Target Language: {}", targetLanguage);
        log.info("  Package Prefix: {}", packagePrefix);
        if (templateName != null) {
            log.info("  Template Name: {}", templateName);
        }
        
        // Initialize engines
        if (templateEngine == null) {
            templateEngine = new MustacheTemplateEngine();
            templateParser = new TemplateParser();
        }
        
        return this;
    }
    
    @Override
    public Generator setFilters(List<String> filters) {
        this.filters = filters != null ? new ArrayList<>(filters) : new ArrayList<>();
        return this;
    }
    
    @Override
    public Generator setScripts(List<String> scripts) {
        this.scripts = scripts != null ? new ArrayList<>(scripts) : new ArrayList<>();
        return this;
    }
    
    @Override
    public void execute(MetaDataLoader loader) throws GeneratorException {
        try {
            
            log.info("Starting Mustache template generation...");
            
            // Get filtered MetaObjects
            Collection<MetaObject> metaObjects = GeneratorUtil.getFilteredMetaData(
                loader, MetaObject.class, null); // TODO: Use filters properly
            
            log.info("Found {} MetaObjects to process", metaObjects.size());
            
            // Load templates
            List<TemplateDefinition> templates = loadTemplates();
            
            if (templates.isEmpty()) {
                log.warn("No templates found for target language: {}", targetLanguage);
                return;
            }
            
            log.info("Loaded {} templates for language: {}", templates.size(), targetLanguage);
            
            // Generate code for each MetaObject using each applicable template
            int filesGenerated = 0;
            for (MetaObject metaObject : metaObjects) {
                for (TemplateDefinition template : templates) {
                    if (isTemplateApplicable(template, metaObject)) {
                        generateFileFromTemplate(metaObject, template);
                        filesGenerated++;
                    }
                }
            }
            
            log.info("Mustache template generation completed. Generated {} files.", filesGenerated);
            
        } catch (Exception e) {
            throw new GeneratorException("Failed to execute Mustache template generation", e);
        }
    }
    
    /**
     * Load templates from the template path.
     */
    private List<TemplateDefinition> loadTemplates() throws IOException {
        List<TemplateDefinition> templates = new ArrayList<>();
        
        if (templateName != null) {
            // Load specific template
            TemplateDefinition template = loadSingleTemplate(templateName);
            if (template != null) {
                templates.add(template);
            }
        } else {
            // Auto-discover templates
            templates = discoverTemplates();
        }
        
        return templates;
    }
    
    /**
     * Load a single template by name.
     */
    private TemplateDefinition loadSingleTemplate(String name) throws IOException {
        // Try different file extensions
        String[] extensions = {".mustache.yaml", ".mustache.yml", ".yaml", ".yml"};
        
        for (String ext : extensions) {
            String templateFile = templatePath + name + ext;
            try {
                TemplateDefinition template;
                // Try filesystem path first (for absolute paths), then classpath
                if (templatePath.startsWith("/") || templatePath.contains(":")) {
                    // Absolute path - use filesystem
                    template = templateParser.parseTemplateFromFilePath(templateFile);
                } else {
                    // Relative path - use classpath
                    template = templateParser.parseTemplateFromFile(templateFile);
                }
                templateParser.validateTemplate(template);
                return template;
            } catch (IOException e) {
                log.debug("Template not found: {}", templateFile);
            }
        }
        
        throw new IOException("Template not found: " + name);
    }
    
    /**
     * Discover templates in the template directory.
     */
    private List<TemplateDefinition> discoverTemplates() throws IOException {
        List<TemplateDefinition> templates = new ArrayList<>();
        
        // For now, implement a simple discovery mechanism
        // In a real implementation, this would scan the classpath or file system
        String[] knownTemplates = {
            "jpa-entity",
            "valueobject-extension", 
            "basic-entity",
            "spring-repository"
        };
        
        for (String templateName : knownTemplates) {
            try {
                TemplateDefinition template = loadSingleTemplate(templateName);
                if (templateParser.supportsLanguage(template, targetLanguage)) {
                    templates.add(template);
                }
            } catch (IOException e) {
                log.debug("Optional template not found: {}", templateName);
            }
        }
        
        return templates;
    }
    
    /**
     * Check if a template is applicable to a MetaObject.
     */
    private boolean isTemplateApplicable(TemplateDefinition template, MetaObject metaObject) {
        // Check target language
        if (!templateParser.supportsLanguage(template, targetLanguage)) {
            return false;
        }
        
        // Check required attributes
        if (template.getRequirements() != null && template.getRequirements().getAttributes() != null) {
            for (String requiredAttr : template.getRequirements().getAttributes()) {
                if (!metaObject.hasMetaAttr(requiredAttr)) {
                    log.debug("MetaObject {} missing required attribute {} for template {}", 
                        metaObject.getName(), requiredAttr, template.getName());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Generate a file from a template and MetaObject.
     */
    private void generateFileFromTemplate(MetaObject metaObject, TemplateDefinition template) 
            throws IOException {
        try {
            log.debug("Generating {} from template {} for MetaObject {}", 
                template.getOutputFileExtension(), template.getName(), metaObject.getName());
            
            // Validate template
            templateParser.validateTemplate(template);
            
            // Generate code using Mustache engine
            String generatedCode = templateEngine.generateCode(template, metaObject, packagePrefix);
            
            // Determine output file path and name
            String fullName = metaObject.getName();
            String className = fullName.contains("::") ? 
                fullName.substring(fullName.lastIndexOf("::") + 2) : fullName;
            String fileName = className + "." + template.getOutputFileExtension();
            String packagePath = template.isPackagePath() ? getPackagePath(metaObject) : "";
            
            // Create output directory
            Path outputPath = Paths.get(outputDir, packagePath);
            Files.createDirectories(outputPath);
            
            // Write generated file
            File outputFile = outputPath.resolve(fileName).toFile();
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(generatedCode);
            }
            
            log.debug("Generated file: {}", outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            throw new IOException("Failed to generate file from template: " + template.getName() + 
                " for MetaObject: " + metaObject.getName(), e);
        }
    }
    
    /**
     * Get the package path for a MetaObject.
     */
    private String getPackagePath(MetaObject metaObject) {
        // First try to get from package property
        String packageName = metaObject.getPackage();
        if (packageName == null || packageName.isEmpty()) {
            // Fallback to package attribute
            if (metaObject.hasMetaAttr("package")) {
                packageName = metaObject.getMetaAttr("package").getValueAsString();
            } else {
                packageName = "com.example.generated";
            }
        }
            
        // Apply package prefix if configured
        if (!packagePrefix.isEmpty()) {
            packageName = packagePrefix + "." + packageName;
        }
        
        return packageName.replace(".", File.separator);
    }
    
    /**
     * Get the template engine for testing purposes.
     */
    public MustacheTemplateEngine getTemplateEngine() {
        return templateEngine;
    }
    
    /**
     * Get the template parser for testing purposes.
     */
    public TemplateParser getTemplateParser() {
        return templateParser;
    }
}