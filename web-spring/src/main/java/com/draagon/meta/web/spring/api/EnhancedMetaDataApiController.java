package com.draagon.meta.web.spring.api;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.object.json.JsonObjectWriter;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.spring.MetaDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced Spring REST API Controller showing multiple injection approaches.
 * 
 * <p>This controller demonstrates all three Spring injection patterns for MetaObjects:</p>
 * <ol>
 *   <li><strong>@Autowired MetaDataLoader</strong> - Backward compatible, works exactly like before</li>
 *   <li><strong>@Autowired MetaDataService</strong> - Convenient service wrapper with Optional support</li>  
 *   <li><strong>@Autowired MetaDataLoaderRegistry</strong> - Full registry access for advanced cases</li>
 * </ol>
 * 
 * <p><strong>Migration Guide:</strong></p>
 * <ul>
 *   <li>Existing code with {@code @Autowired MetaDataLoader} works unchanged</li>
 *   <li>Replace static {@code MetaDataRegistry.findMetaObject()} with {@code metaDataService.findMetaObject()}</li>
 *   <li>Gain Optional support and better error handling</li>
 * </ul>
 * 
 * @since 6.0.0
 */
@Controller
@RequestMapping("/api/v2/metadata")
public class EnhancedMetaDataApiController {
    
    // APPROACH 1: Backward compatible - works exactly like before
    @Autowired
    private MetaDataLoader metaDataLoader;
    
    // APPROACH 2: Convenient service wrapper (RECOMMENDED)
    @Autowired
    private MetaDataService metaDataService;
    
    // APPROACH 3: Full registry access for advanced use cases
    @Autowired
    private MetaDataLoaderRegistry metaDataLoaderRegistry;

    /**
     * APPROACH 1 DEMO: Using @Autowired MetaDataLoader (backward compatible)
     * 
     * This works exactly like the old static approach - no code changes needed!
     */
    @GetMapping("/legacy/objects/{name}")
    @ResponseBody
    public ResponseEntity<String> getLegacyMetaObject(@PathVariable String name) {
        try {
            // Same API as before - OSGi compatibility is transparent
            MetaObject metaObject = metaDataLoader.getMetaObjectByName(name);
            return ResponseEntity.ok(serializeMetaObject(metaObject));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * APPROACH 2 DEMO: Using MetaDataService (RECOMMENDED)
     * 
     * Cleaner API with Optional support and better error handling
     */
    @GetMapping("/objects/{name}")
    @ResponseBody  
    public ResponseEntity<String> getMetaObject(@PathVariable String name) {
        // Much cleaner with Optional support
        Optional<MetaObject> metaObject = metaDataService.findMetaObjectByNameOptional(name);
        
        return metaObject
            .map(this::serializeMetaObject)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * APPROACH 2 DEMO: Find metadata for any object instance
     */
    @PostMapping("/discover")
    @ResponseBody
    public ResponseEntity<String> discoverMetaData(@RequestBody Object obj) {
        Optional<MetaObject> metaObject = metaDataService.findMetaObjectOptional(obj);
        
        return metaObject
            .map(this::serializeMetaObject) 
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }
    
    /**
     * APPROACH 2 DEMO: List all objects with service convenience methods
     */
    @GetMapping("/objects")
    @ResponseBody
    public Map<String, Object> listAllObjects() {
        List<Map<String, String>> objects = new ArrayList<>();
        
        // Clean, simple API
        for (MetaObject obj : metaDataService.getAllMetaObjects()) {
            Map<String, String> objectInfo = new HashMap<>();
            objectInfo.put("name", obj.getName());
            objectInfo.put("type", obj.getSubType());
            objectInfo.put("package", obj.getPackage());
            objects.add(objectInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("objects", objects);
        result.put("totalCount", objects.size());
        return result;
    }
    
    /**
     * APPROACH 3 DEMO: Advanced registry operations
     */
    @GetMapping("/loaders")
    @ResponseBody
    public Map<String, Object> getLoaderInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Access full registry for advanced operations
        List<Map<String, Object>> loaders = new ArrayList<>();
        for (var loader : metaDataLoaderRegistry.getDataLoaders()) {
            Map<String, Object> loaderInfo = new HashMap<>();
            loaderInfo.put("name", loader.getName());
            loaderInfo.put("objectCount", loader.getChildren(MetaObject.class).size());
            loaderInfo.put("isRegistered", loader.isRegistered());
            loaders.add(loaderInfo);
        }
        
        info.put("loaders", loaders);
        info.put("registryType", metaDataLoaderRegistry.getClass().getSimpleName());
        return info;
    }
    
    /**
     * Check if the system is OSGi-compatible
     */
    @GetMapping("/system/info")
    @ResponseBody
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("osgiCompatible", true);
        info.put("registryClass", metaDataLoaderRegistry.getClass().getName());
        info.put("serviceRegistryType", 
            metaDataLoaderRegistry.getClass().getSimpleName().contains("OSGI") ? "OSGi" : "Standard");
        return info;
    }
    
    /**
     * Helper method to serialize MetaObject
     */
    private String serializeMetaObject(MetaObject metaObject) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonObjectWriter jsonWriter = new JsonObjectWriter(metaDataLoader, stringWriter);
            jsonWriter.withPrettyPrint();
            
            Map<String, Object> wrapper = createMetaObjectWrapper(metaObject);
            jsonWriter.write(wrapper);
            jsonWriter.close();
            
            return stringWriter.toString();
        } catch (Exception e) {
            return "{\"error\": \"Serialization failed: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Helper method to create MetaObject wrapper (same as original)
     */
    private Map<String, Object> createMetaObjectWrapper(MetaObject metaObject) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("name", metaObject.getName());
        wrapper.put("type", metaObject.getSubType());
        wrapper.put("displayName", metaObject.getName());
        
        if (metaObject.getSuperData() != null) {
            wrapper.put("super", metaObject.getSuperData().getName());
        }
        
        // Add fields
        Map<String, Object> fields = new HashMap<>();
        for (MetaField field : metaObject.getMetaFields()) {
            Map<String, Object> fieldWrapper = new HashMap<>();
            fieldWrapper.put("name", field.getName());
            fieldWrapper.put("type", field.getDataType().name().toLowerCase());
            fields.put(field.getName(), fieldWrapper);
        }
        wrapper.put("fields", fields);
        
        return wrapper;
    }
}