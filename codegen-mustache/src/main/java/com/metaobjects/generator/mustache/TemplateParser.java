package com.metaobjects.generator.mustache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Parser for template definition files in YAML or JSON format.
 * Supports validation of template requirements and helper function availability.
 * 
 * Based on the cross-language template system architecture documented in
 * .claude/archive/template-system/TEMPLATE_IMPLEMENTATION_GUIDE.md
 */
public class TemplateParser {
    
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    
    public TemplateParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }
    
    /**
     * Parse a template definition from a string content.
     * 
     * @param content the template content as string
     * @param isYaml true if content is YAML, false if JSON
     * @return parsed TemplateDefinition
     * @throws IOException if parsing fails
     */
    public TemplateDefinition parseTemplate(String content, boolean isYaml) throws IOException {
        ObjectMapper mapper = isYaml ? yamlMapper : jsonMapper;
        return mapper.readValue(content, TemplateDefinition.class);
    }
    
    /**
     * Parse a template definition from a classpath resource file.
     * 
     * @param filePath the classpath resource path
     * @return parsed TemplateDefinition
     * @throws IOException if file not found or parsing fails
     */
    public TemplateDefinition parseTemplateFromFile(String filePath) throws IOException {
        boolean isYaml = filePath.endsWith(".yaml") || filePath.endsWith(".yml");
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + filePath);
            }
            
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parseTemplate(content, isYaml);
        }
    }
    
    /**
     * Parse a template definition from a file system path.
     * 
     * @param filePath the file system path
     * @return parsed TemplateDefinition
     * @throws IOException if file not found or parsing fails
     */
    public TemplateDefinition parseTemplateFromFilePath(String filePath) throws IOException {
        boolean isYaml = filePath.endsWith(".yaml") || filePath.endsWith(".yml");
        
        try (InputStream inputStream = new java.io.FileInputStream(filePath)) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parseTemplate(content, isYaml);
        }
    }
    
    /**
     * Validate a template definition for correctness and completeness.
     * 
     * @param template the template to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateTemplate(TemplateDefinition template) {
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }
        
        if (template.getTemplate() == null || template.getTemplate().trim().isEmpty()) {
            throw new IllegalArgumentException("Template content is required");
        }
        
        if (template.getTargetLanguage() == null || template.getTargetLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Target language is required");
        }
        
        if (template.getOutputFileExtension() == null || template.getOutputFileExtension().trim().isEmpty()) {
            throw new IllegalArgumentException("Output file extension is required");
        }
        
        // Validate target language is supported
        List<String> supportedLanguages = List.of("java", "csharp", "typescript");
        if (!supportedLanguages.contains(template.getTargetLanguage().toLowerCase())) {
            throw new IllegalArgumentException("Unsupported target language: " + template.getTargetLanguage() + 
                ". Supported languages: " + supportedLanguages);
        }
        
        // Validate required helpers are available
        if (template.getRequirements() != null && template.getRequirements().getHelpers() != null) {
            HelperRegistry helperRegistry = new HelperRegistry();
            for (String helperName : template.getRequirements().getHelpers()) {
                if (!helperRegistry.contains(helperName)) {
                    throw new IllegalArgumentException("Required helper not available: " + helperName);
                }
            }
        }
        
        // Validate Mustache template syntax (basic check)
        try {
            String templateContent = template.getTemplate();
            long openBraces = templateContent.chars().filter(ch -> ch == '{').count();
            long closeBraces = templateContent.chars().filter(ch -> ch == '}').count();
            
            if (openBraces != closeBraces) {
                throw new IllegalArgumentException("Mustache template has unmatched braces");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Mustache template syntax: " + e.getMessage());
        }
    }
    
    /**
     * Check if a template supports the specified target language.
     * 
     * @param template the template to check
     * @param targetLanguage the target language to check
     * @return true if the template supports the target language
     */
    public boolean supportsLanguage(TemplateDefinition template, String targetLanguage) {
        return template.getTargetLanguage() != null && 
               template.getTargetLanguage().equalsIgnoreCase(targetLanguage);
    }
    
    /**
     * Check if a template requires specific attributes on MetaObjects.
     * 
     * @param template the template to check
     * @param attributeName the attribute name to check
     * @return true if the template requires the specified attribute
     */
    public boolean requiresAttribute(TemplateDefinition template, String attributeName) {
        return template.getRequirements() != null &&
               template.getRequirements().getAttributes() != null &&
               template.getRequirements().getAttributes().contains(attributeName);
    }
}