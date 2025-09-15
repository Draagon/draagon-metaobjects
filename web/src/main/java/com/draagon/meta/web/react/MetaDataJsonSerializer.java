package com.draagon.meta.web.react;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.view.MetaView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.*;

/**
 * Serializes MetaData objects to JSON format for React consumption
 */
public class MetaDataJsonSerializer {
    
    private final Gson gson;
    
    public MetaDataJsonSerializer() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }
    
    /**
     * Serialize a MetaObject to JSON
     */
    public String serializeMetaObject(MetaObject metaObject) {
        JsonObject json = new JsonObject();
        
        json.addProperty("name", metaObject.getName());
        json.addProperty("type", metaObject.getSubTypeName());
        
        if (metaObject.getSuperData() != null) {
            json.addProperty("super", metaObject.getSuperData().getName());
        }
        
        // Add display name and description
        json.addProperty("displayName", metaObject.getName()); // Could be enhanced with displayName attribute
        
        // Add class information
        if (metaObject.hasMetaAttr("object")) {
            json.addProperty("className", metaObject.getMetaAttr("object").getValueAsString());
        }
        
        if (metaObject.hasMetaAttr("dbTable")) {
            json.addProperty("dbTable", metaObject.getMetaAttr("dbTable").getValueAsString());
        }
        
        // Serialize attributes
        json.add("attributes", serializeAttributes(metaObject));
        
        // Serialize fields
        JsonObject fieldsJson = new JsonObject();
        for (MetaField field : metaObject.getMetaFields()) {
            fieldsJson.add(field.getName(), serializeMetaField(field));
        }
        json.add("fields", fieldsJson);
        
        return gson.toJson(json);
    }
    
    /**
     * Serialize a MetaField to JSON
     */
    public JsonElement serializeMetaField(MetaField field) {
        JsonObject json = new JsonObject();
        
        json.addProperty("name", field.getName());
        json.addProperty("type", field.getDataType().name());
        json.addProperty("displayName", field.getName()); // Could be enhanced
        
        // Add field properties
        if (field.hasMetaAttr("len")) {
            json.addProperty("length", Integer.parseInt(field.getMetaAttr("len").getValueAsString()));
        }
        
        if (field.hasMetaAttr("isKey")) {
            json.addProperty("isKey", Boolean.parseBoolean(field.getMetaAttr("isKey").getValueAsString()));
        }
        
        if (field.hasMetaAttr("auto")) {
            json.addProperty("auto", field.getMetaAttr("auto").getValueAsString());
        }
        
        if (field.hasMetaAttr("objectRef")) {
            json.addProperty("objectRef", field.getMetaAttr("objectRef").getValueAsString());
        }
        
        if (field.hasMetaAttr("dbColumn")) {
            json.addProperty("dbColumn", field.getMetaAttr("dbColumn").getValueAsString());
        }
        
        // Serialize attributes
        json.add("attributes", serializeAttributes(field));
        
        // Serialize validators (simplified)
        JsonArray validators = new JsonArray();
        // TODO: Implement validator serialization when validator API is available
        json.add("validators", validators);
        
        // Serialize views
        JsonObject viewsJson = new JsonObject();
        Collection<MetaView> views = field.getViews();
        if (views != null) {
            for (MetaView view : views) {
                viewsJson.add(view.getName(), serializeMetaView(view));
            }
        }
        json.add("views", viewsJson);
        
        // Set default view
        try {
            MetaView defaultView = field.getDefaultView();
            if (defaultView != null) {
                json.addProperty("defaultView", defaultView.getName());
            }
        } catch (Exception e) {
            // No default view
        }
        
        return json;
    }
    
    /**
     * Serialize a MetaView to JSON
     */
    public JsonElement serializeMetaView(MetaView view) {
        JsonObject json = new JsonObject();
        
        json.addProperty("name", view.getName());
        json.addProperty("type", view.getSubTypeName());
        
        if (view.getDeclaringMetaField() != null) {
            json.addProperty("fieldName", view.getDeclaringMetaField().getName());
        }
        
        // Serialize attributes
        json.add("attributes", serializeAttributes(view));
        
        return json;
    }
    
    /**
     * Serialize MetaAttributes to JSON
     */
    private JsonObject serializeAttributes(MetaData metaData) {
        JsonObject attributesJson = new JsonObject();
        
        Collection<MetaAttribute> attributes = metaData.getMetaAttrs();
        if (attributes != null) {
            for (MetaAttribute attr : attributes) {
                JsonObject attrJson = new JsonObject();
                attrJson.addProperty("name", attr.getName());
                attrJson.addProperty("value", attr.getValueAsString());
                if (attr.getDataType() != null) {
                    attrJson.addProperty("type", attr.getDataType().name());
                }
                attributesJson.add(attr.getName(), attrJson);
            }
        }
        
        return attributesJson;
    }
    
    /**
     * Serialize an entire package of MetaObjects
     */
    public String serializeMetaDataPackage(String packageName, Collection<MetaObject> metaObjects) {
        JsonObject packageJson = new JsonObject();
        
        packageJson.addProperty("name", packageName);
        packageJson.addProperty("version", "1.0.0"); // Could be dynamic
        
        JsonObject objectsJson = new JsonObject();
        for (MetaObject metaObject : metaObjects) {
            JsonElement objectElement = gson.fromJson(serializeMetaObject(metaObject), JsonElement.class);
            objectsJson.add(metaObject.getName(), objectElement);
        }
        packageJson.add("objects", objectsJson);
        
        return gson.toJson(packageJson);
    }
}