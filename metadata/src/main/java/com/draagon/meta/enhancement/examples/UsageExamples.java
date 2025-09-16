package com.draagon.meta.enhancement.examples;

import com.draagon.meta.enhancement.MetaDataEnhancementService;
import com.draagon.meta.enhancement.enhancers.TemplateBasedEnhancer;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.util.Map;

/**
 * v6.0.0: Usage examples for the new MetaDataEnhancementService architecture.
 * These examples show how to replace TypesConfig overlay patterns with service-based enhancements.
 */
public class UsageExamples {
    
    /**
     * Example: ObjectManagerDB initialization with database attributes
     */
    public static class ObjectManagerDBExample {
        
        private MetaDataEnhancementService enhancementService;
        
        public void initialize(MetaDataLoader loader, String dbDialect, String schemaName) {
            enhancementService = new MetaDataEnhancementService();
            
            // Enhance all MetaObjects with database attributes
            for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
                enhancementService.enhanceForService(metaObject, "objectManagerDB", 
                    Map.of(
                        "dialect", dbDialect,
                        "schema", schemaName,
                        "autoCreateTables", true
                    ));
            }
            
            // Now ObjectManagerDB can use attributes like dbTable, dbCol, etc.
        }
    }
    
    /**
     * Example: ORM Code Generator using shared database attributes
     */
    @TemplateBasedEnhancer.RequiresAttributeProviders({
        "DatabaseAttributes",    // Shared with ObjectManagerDB
        "ValidationAttributes", // For validation annotations
        "IOAttributes"          // For serialization annotations
    })
    @TemplateBasedEnhancer.ForServices({"ormCodeGen", "jpaCodeGen"})
    public static class JPAEntityTemplate {
        
        public void generateORMClasses(MetaDataLoader loader) {
            MetaDataEnhancementService enhancementService = new MetaDataEnhancementService();
            
            for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
                // Template-based enhancement will automatically apply required attributes
                enhancementService.enhanceForService(metaObject, "ormCodeGen", 
                    Map.of(
                        "template", JPAEntityTemplate.class,
                        "targetFramework", "JPA",
                        "generateValidation", true
                    ));
                
                // Now template can assume dbTable, dbCol, required, nullable attributes exist
                generateEntityClass(metaObject);
            }
        }
        
        private void generateEntityClass(MetaObject metaObject) {
            // Template implementation can use enhanced attributes
            // e.g., metaObject.getStringAttr("dbTable").getValue()
        }
    }
    
    /**
     * Example: JSON serialization with IO attributes
     */
    public static class JsonSerializationExample {
        
        public void configureJsonSerialization(MetaDataLoader loader) {
            MetaDataEnhancementService enhancementService = new MetaDataEnhancementService();
            
            for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
                enhancementService.enhanceForService(metaObject, "jsonIO", 
                    Map.of(
                        "format", "json",
                        "prettify", true,
                        "includeNulls", false
                    ));
            }
            
            // Now serializer can use jsonIgnore, jsonProperty, etc. attributes
        }
    }
    
    /**
     * Example: Custom template with specific attribute requirements
     */
    @TemplateBasedEnhancer.RequiresAttributeProviders({
        "DatabaseAttributes",
        "ValidationAttributes",
        "CustomBusinessAttributes"  // Custom provider
    })
    public static class CustomBusinessTemplate {
        
        // Template implementation that relies on database, validation, 
        // and custom business attributes being available
    }
    
    /**
     * Example: Migration from TypesConfig overlay to enhancement service
     */
    public static class MigrationExample {
        
        // OLD WAY (TypesConfig overlay):
        // typesConfigLoader.loadOverlay("database-attributes.json");
        // typesConfigLoader.loadOverlay("validation-attributes.json");
        
        // NEW WAY (Service-based enhancement):
        public void enhanceMetaData(MetaDataLoader loader, String serviceName) {
            MetaDataEnhancementService enhancementService = new MetaDataEnhancementService();
            
            // All registered providers are automatically discovered and applied
            for (MetaObject metaObject : loader.getChildren(MetaObject.class)) {
                enhancementService.enhanceForService(metaObject, serviceName, Map.of());
            }
        }
        
        // Or load specific providers:
        public void loadSpecificAttributes(MetaObject metaObject) {
            MetaDataEnhancementService enhancementService = new MetaDataEnhancementService();
            
            enhancementService.loadAttributesFromProvider(metaObject, "DatabaseAttributes");
            enhancementService.loadAttributesFromProvider(metaObject, "ValidationAttributes");
        }
    }
}