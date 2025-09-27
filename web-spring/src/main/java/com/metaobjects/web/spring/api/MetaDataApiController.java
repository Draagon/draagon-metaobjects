package com.metaobjects.web.spring.api;

import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObject;
import com.metaobjects.field.MetaField;
import com.metaobjects.io.object.json.JsonObjectWriter;
import com.metaobjects.MetaDataNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring REST API Controller for serving MetaData as JSON to React components
 * 
 * Endpoints:
 * - GET /api/metadata/objects - List all MetaObjects
 * - GET /api/metadata/objects/{name} - Get specific MetaObject
 * - GET /api/metadata/packages/{packageName} - Get entire package
 */
@Controller
@RequestMapping("/api/metadata")
public class MetaDataApiController {
    
    @Autowired
    private MetaDataLoader metaDataLoader;

    /**
     * Enable CORS for development
     */
    @CrossOrigin(origins = "*")
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    /**
     * List all available MetaObjects
     */
    @GetMapping("/objects")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public Map<String, Object> listObjects(HttpServletResponse response) {
        setCorsHeaders(response);
        
        List<Map<String, String>> objects = new ArrayList<>();
        for (MetaObject obj : metaDataLoader.getMetaObjects()) {
            Map<String, String> objectInfo = new HashMap<>();
            objectInfo.put("name", obj.getName());
            objectInfo.put("type", obj.getSubType());
            objects.add(objectInfo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("objects", objects);
        return result;
    }

    /**
     * Get specific MetaObject by name  
     */
    @GetMapping("/objects/{name}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String getMetaObject(@PathVariable String name, HttpServletResponse response) throws IOException {
        setCorsHeaders(response);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        try {
            MetaObject metaObject = metaDataLoader.getMetaObjectByName(name);
            if (metaObject == null) {
                throw new MetaDataNotFoundException("MetaObject not found", name);
            }
            
            // Use the existing JsonObjectWriter to serialize the MetaObject
            StringWriter stringWriter = new StringWriter();
            JsonObjectWriter jsonWriter = new JsonObjectWriter(metaDataLoader, stringWriter);
            jsonWriter.withPrettyPrint();
            
            // Create a simple wrapper object to serialize
            Map<String, Object> wrapper = createMetaObjectWrapper(metaObject);
            jsonWriter.write(wrapper);
            jsonWriter.close();
            
            return stringWriter.toString();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Get entire metadata package
     */
    @GetMapping("/packages/{packageName}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public Map<String, Object> getMetaDataPackage(@PathVariable String packageName, HttpServletResponse response) {
        setCorsHeaders(response);
        
        Map<String, Object> packageData = new HashMap<>();
        packageData.put("name", packageName);
        packageData.put("version", "1.0.0");
        
        Map<String, Object> objects = new HashMap<>();
        for (MetaObject obj : metaDataLoader.getMetaObjects()) {
            objects.put(obj.getName(), createMetaObjectWrapper(obj));
        }
        packageData.put("objects", objects);
        
        return packageData;
    }

    /**
     * Create a simplified wrapper for MetaObject that can be easily serialized
     */
    private Map<String, Object> createMetaObjectWrapper(MetaObject metaObject) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("name", metaObject.getName());
        wrapper.put("type", metaObject.getSubType());
        wrapper.put("displayName", metaObject.getName());
        
        if (metaObject.getSuperData() != null) {
            wrapper.put("super", metaObject.getSuperData().getName());
        }
        
        // Add attributes
        Map<String, Object> attributes = new HashMap<>();
        if (metaObject.hasMetaAttr("object")) {
            attributes.put("className", metaObject.getMetaAttr("object").getValueAsString());
        }
        wrapper.put("attributes", attributes);
        
        // Add fields
        Map<String, Object> fields = new HashMap<>();
        for (MetaField field : metaObject.getMetaFields()) {
            fields.put(field.getName(), createMetaFieldWrapper(field));
        }
        wrapper.put("fields", fields);
        
        return wrapper;
    }

    /**
     * Create a simplified wrapper for MetaField
     */
    private Map<String, Object> createMetaFieldWrapper(MetaField field) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("name", field.getName());
        wrapper.put("type", field.getDataType().name().toLowerCase());
        wrapper.put("displayName", field.getName());
        wrapper.put("isRequired", false); // Default, could be enhanced
        
        // Add attributes
        Map<String, Object> attributes = new HashMap<>();
        if (field.hasMetaAttr("len")) {
            try {
                wrapper.put("length", Integer.parseInt(field.getMetaAttr("len").getValueAsString()));
            } catch (NumberFormatException ignored) {}
        }
        if (field.hasMetaAttr("isKey")) {
            wrapper.put("isKey", Boolean.parseBoolean(field.getMetaAttr("isKey").getValueAsString()));
        }
        if (field.hasMetaAttr("dbColumn")) {
            attributes.put("dbColumn", field.getMetaAttr("dbColumn").getValueAsString());
        }
        wrapper.put("attributes", attributes);
        
        // Simplified validators and views
        wrapper.put("validators", new ArrayList<>());
        wrapper.put("views", new HashMap<>());
        
        return wrapper;
    }

    /**
     * Handle OPTIONS requests for CORS
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    @CrossOrigin(origins = "*")
    public void handleOptions(HttpServletResponse response) {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}