package com.metaobjects.generator.mustache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * Template definition data structure for Mustache-based code generation.
 * This class represents the YAML/JSON template metadata that defines
 * how MetaObjects should be transformed into code.
 * 
 * Based on the cross-language template system architecture documented in
 * .claude/archive/template-system/TEMPLATE_SYSTEM_ARCHITECTURE.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateDefinition {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version = "1.0.0";
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("targetLanguage")
    private String targetLanguage;
    
    @JsonProperty("outputFileExtension")
    private String outputFileExtension;
    
    @JsonProperty("packagePath")
    private boolean packagePath = true;
    
    @JsonProperty("requirements")
    private TemplateRequirements requirements;
    
    @JsonProperty("template")
    private String template;
    
    @JsonProperty("partials")
    private Map<String, String> partials;
    
    // Getters and setters
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getVersion() { 
        return version; 
    }
    
    public void setVersion(String version) { 
        this.version = version; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getTargetLanguage() { 
        return targetLanguage; 
    }
    
    public void setTargetLanguage(String targetLanguage) { 
        this.targetLanguage = targetLanguage; 
    }
    
    public String getOutputFileExtension() { 
        return outputFileExtension; 
    }
    
    public void setOutputFileExtension(String outputFileExtension) { 
        this.outputFileExtension = outputFileExtension; 
    }
    
    public boolean isPackagePath() { 
        return packagePath; 
    }
    
    public void setPackagePath(boolean packagePath) { 
        this.packagePath = packagePath; 
    }
    
    public TemplateRequirements getRequirements() { 
        return requirements; 
    }
    
    public void setRequirements(TemplateRequirements requirements) { 
        this.requirements = requirements; 
    }
    
    public String getTemplate() { 
        return template; 
    }
    
    public void setTemplate(String template) { 
        this.template = template; 
    }
    
    public Map<String, String> getPartials() { 
        return partials; 
    }
    
    public void setPartials(Map<String, String> partials) { 
        this.partials = partials; 
    }
    
    /**
     * Template requirements specification.
     * Defines what attributes and helper functions are required
     * for this template to function correctly.
     */
    public static class TemplateRequirements {
        
        @JsonProperty("attributes")
        private List<String> attributes;
        
        @JsonProperty("helpers")
        private List<String> helpers;
        
        public List<String> getAttributes() { 
            return attributes; 
        }
        
        public void setAttributes(List<String> attributes) { 
            this.attributes = attributes; 
        }
        
        public List<String> getHelpers() { 
            return helpers; 
        }
        
        public void setHelpers(List<String> helpers) { 
            this.helpers = helpers; 
        }
    }
    
    @Override
    public String toString() {
        return "TemplateDefinition{" +
               "name='" + name + '\'' +
               ", version='" + version + '\'' +
               ", targetLanguage='" + targetLanguage + '\'' +
               ", outputFileExtension='" + outputFileExtension + '\'' +
               '}';
    }
}