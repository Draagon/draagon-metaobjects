package com.draagon.meta.enhancement.enhancers;

import com.draagon.meta.MetaData;
import com.draagon.meta.enhancement.EnhancementContext;
import com.draagon.meta.enhancement.MetaDataEnhancer;
import com.draagon.meta.enhancement.MetaDataEnhancementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;


/**
 * v6.0.0: Enhancer that applies attributes based on template requirements.
 * This replaces the TypesConfig overlay system for template-driven attribute loading.
 * 
 * Templates can declare their required attribute providers using the @RequiresAttributeProviders annotation.
 */
public class TemplateBasedEnhancer implements MetaDataEnhancer {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateBasedEnhancer.class);
    
    public static final String ENHANCER_ID = "TemplateBasedEnhancer";
    
    /**
     * Annotation for templates to declare required attribute providers
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RequiresAttributeProviders {
        /**
         * @return Array of attribute provider IDs required by this template
         */
        String[] value();
    }
    
    /**
     * Annotation for templates to declare required services
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ForServices {
        /**
         * @return Array of service names this template supports
         */
        String[] value() default {"codeGen", "templateGen"};
    }
    
    @Override
    public boolean enhance(MetaData metaData, EnhancementContext context) {
        String templateName = context.getTemplateName();
        if (templateName == null || templateName.trim().isEmpty()) {
            // Try to get template from properties
            Object templateObj = context.getProperty("template");
            if (templateObj == null) {
                return false;
            }
            
            if (templateObj instanceof String) {
                templateName = (String) templateObj;
            } else if (templateObj instanceof Class) {
                return enhanceForTemplateClass(metaData, (Class<?>) templateObj, context);
            } else {
                return enhanceForTemplateClass(metaData, templateObj.getClass(), context);
            }
        }
        
        // Try to load template class by name
        try {
            Class<?> templateClass = Class.forName(templateName);
            return enhanceForTemplateClass(metaData, templateClass, context);
        } catch (ClassNotFoundException e) {
            log.warn("Template class [{}] not found for enhancement", templateName);
            return false;
        }
    }
    
    @Override
    public Collection<String> getSupportedServices() {
        return Arrays.asList("codeGen", "templateGen", "generator");
    }
    
    @Override
    public String getEnhancerId() {
        return ENHANCER_ID;
    }
    
    @Override
    public int getPriority() {
        return 200; // Apply template-based enhancements after basic attribute providers
    }
    
    @Override
    public String getDescription() {
        return "Applies attributes based on template requirements declared via annotations";
    }
    
    private boolean enhanceForTemplateClass(MetaData metaData, Class<?> templateClass, EnhancementContext context) {
        boolean enhanced = false;
        
        // Check for required attribute providers annotation
        RequiresAttributeProviders requiresProviders = templateClass.getAnnotation(RequiresAttributeProviders.class);
        if (requiresProviders != null) {
            enhanced = applyRequiredProviders(metaData, requiresProviders.value(), context) || enhanced;
        }
        
        // Check for service-specific enhancements
        ForServices forServices = templateClass.getAnnotation(ForServices.class);
        if (forServices != null) {
            String contextService = context.getServiceName();
            if (contextService != null && Arrays.asList(forServices.value()).contains(contextService)) {
                log.debug("Template [{}] supports service [{}], applying enhancements", 
                        templateClass.getSimpleName(), contextService);
                enhanced = true;
            }
        }
        
        if (enhanced) {
            log.info("Applied template-based enhancements from [{}] to MetaData [{}]", 
                    templateClass.getSimpleName(), metaData.getName());
        }
        
        return enhanced;
    }
    
    private boolean applyRequiredProviders(MetaData metaData, String[] providerIds, EnhancementContext context) {
        boolean applied = false;
        
        // Get enhancement service from context or create new one
        MetaDataEnhancementService enhancementService = getEnhancementService(context);
        
        for (String providerId : providerIds) {
            try {
                enhancementService.loadAttributesFromProvider(metaData, providerId);
                log.debug("Applied attributes from provider [{}] to MetaData [{}]", 
                        providerId, metaData.getName());
                applied = true;
            } catch (Exception e) {
                log.error("Error applying attributes from provider [{}] to MetaData [{}]: {}", 
                        providerId, metaData.getName(), e.getMessage(), e);
            }
        }
        
        return applied;
    }
    
    private MetaDataEnhancementService getEnhancementService(EnhancementContext context) {
        // Try to get from context first
        MetaDataEnhancementService service = context.getProperty("enhancementService", 
                MetaDataEnhancementService.class);
        
        if (service == null) {
            // Create new service instance
            service = new MetaDataEnhancementService();
        }
        
        return service;
    }
}