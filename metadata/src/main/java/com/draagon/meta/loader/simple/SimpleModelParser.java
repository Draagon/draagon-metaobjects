package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.model.MetaModelParser;
import com.draagon.meta.loader.uri.URIHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;

/**
 * v6.0.0: Updated to use service-based MetaDataTypeRegistry instead of TypesConfig
 */
public class SimpleModelParser extends MetaModelParser<SimpleLoader,URI> {

    /**
     * Simple MetaModel implementation for JSON parsing
     */
    private static class SimpleMetaModel implements com.draagon.meta.loader.model.MetaModel {
        private String pkg;
        private String type;
        private String subType;
        private String name;
        private String superRef;
        private String value;
        private java.util.List<com.draagon.meta.loader.model.MetaModel> children;
        
        @Override public String getPackage() { return pkg; }
        @Override public void setPackage(String pkg) { this.pkg = pkg; }
        @Override public String getType() { return type; }
        @Override public void setType(String type) { this.type = type; }
        @Override public String getSubType() { return subType; }
        @Override public void setSubType(String subType) { this.subType = subType; }
        @Override public String getName() { return name; }
        @Override public void setName(String name) { this.name = name; }
        @Override public String getSuper() { return superRef; }
        @Override public void setSuper(String superRef) { this.superRef = superRef; }
        @Override public String getValue() { return value; }
        @Override public void setValue(String value) { this.value = value; }
        @Override public java.util.List<com.draagon.meta.loader.model.MetaModel> getChildren() { return children; }
        @Override public void setChildren(java.util.List<com.draagon.meta.loader.model.MetaModel> children) { this.children = children; }
        
        // MetaObjectAware methods
        @Override public com.draagon.meta.object.MetaObject getMetaData() { return null; }
        @Override public void setMetaData(com.draagon.meta.object.MetaObject metaObject) { }
        
        // Validatable methods
        @Override public void validate() throws com.draagon.meta.ValueException { }
        
        // Inline attribute support
        @Override public com.draagon.meta.loader.model.MetaModel createInlineAttributeChild(String name, Object value) {
            SimpleMetaModel attrChild = new SimpleMetaModel();
            attrChild.setType(SYNTHETIC_ATTR_TYPE);
            attrChild.setName(name);
            attrChild.setValue(value != null ? value.toString() : null);
            return attrChild;
        }
    }

    protected SimpleModelParser(MetaModelLoader modelLoader, ClassLoader classLoader, String sourceName) {
        super(modelLoader, classLoader, sourceName);
    }
    
    /**
     * Override to properly handle subType inheritance from super data
     */
    @Override
    protected <M extends com.draagon.meta.loader.model.MetaModel> com.draagon.meta.MetaData createNewMetaData(
            com.draagon.meta.MetaData parent, com.draagon.meta.registry.MetaDataTypeRegistry typeRegistry, M model, String superName, String fullname) {

        String subType = model.getSubType();

        // Get the superData && set the subType if null (implementing the missing logic)
        com.draagon.meta.MetaData superData = null;
        if ( model.getSuper() != null ) {

            superData = getSuperData( parent, model, superName );

            // If model subType is null, inherit from super data
            if ( subType == null ) {
                subType = superData.getSubTypeName();
            }
            // If model subType is not null, validate it matches super data
            else if ( !subType.equals(superData.getSubTypeName())) {
                throw new com.draagon.meta.MetaDataException("SubType mismatch [" + subType + "] != "+
                        "["+ superData.getSubTypeName()+"] on superData: " + superData );
            }
        }
        
        // Create MetaData using registry (registry handles null subType with defaults)
        com.draagon.meta.MetaData merge = typeRegistry.createInstance(model.getType(), subType, fullname);

        // Set the SuperData if it exists
        if ( superData != null ) {
            validateSuperDataOnNew( merge, superData );
            merge.setSuperData( superData );
        }

        return merge;
    }

    @Override
    public void loadAndMerge( SimpleLoader intoLoader, URI uri) {

        InputStream is = null;
        try {
            is = URIHelper.getInputStream( 
                getClassLoader() != null ? Arrays.asList(getClassLoader()) : null, 
                URIHelper.toURIModel(uri) 
            );
            //intoLoader.getResourceInputStream(resource);
            loadAndMergeFromStream( intoLoader, is );
        }
        catch( IOException e ) {
            throw new MetaDataException( "Unable to load URI ["+uri+"]: " + e.getMessage(), e );
        }
        finally {
            try {
                if ( is != null ) is.close();
            } catch( IOException e ) {
                throw new MetaDataException( "Unable to close URI ["+uri+"]: " + e.getMessage(), e );
            }
        }
    }

    /* Load MetaDataModel Stream */
    public void loadAndMergeFromStream( SimpleLoader intoLoader, InputStream in ) {

        try {
            // Parse JSON directly using Gson
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            
            // Validate it's a metadata object
            if (!"metadata".equals(getJsonStringValue(root, "type"))) {
                throw new MetaDataException("Root object must have type='metadata' in file: " + getSourcename());
            }
            
            // Parse JSON into MetaModel object
            SimpleMetaModel rootModel = parseJsonToMetaModel(root);
            
            // Use the standard MetaModelParser approach to merge into loader
            mergeMetaDataModel(intoLoader, rootModel);
            
        } catch (Exception e) {
            throw new MetaDataException("Error loading MetaData from [" + getSourcename() + "]: " + e.getMessage(), e);
        } finally {
            try { 
                in.close(); 
            } catch (IOException ignored) {}
        }
    }
    
    /**
     * Parse JSON into MetaModel object hierarchy
     */
    private SimpleMetaModel parseJsonToMetaModel(JsonObject jsonObj) {
        SimpleMetaModel metaModel = new SimpleMetaModel();
        
        // Set basic properties
        String type = getJsonStringValue(jsonObj, "type");
        String subType = getJsonStringValue(jsonObj, "subType");
        String name = getJsonStringValue(jsonObj, "name");
        String packageName = getJsonStringValue(jsonObj, "package");
        String superRef = getJsonStringValue(jsonObj, "super");
        String value = getJsonStringValue(jsonObj, "value");
        
        metaModel.setType(type);
        metaModel.setSubType(subType); // Keep null as null - this allows inheritance from super data
        metaModel.setName(name);
        metaModel.setPackage(packageName);
        metaModel.setSuper(superRef);
        metaModel.setValue(value);
        
        // Parse inline attributes (@ prefixed) if type supports them
        parseInlineAttributes(metaModel, jsonObj, type);
        
        // Parse children
        if (jsonObj.has("children")) {
            JsonArray children = jsonObj.getAsJsonArray("children");
            java.util.List<com.draagon.meta.loader.model.MetaModel> childModels = new java.util.ArrayList<>();
            
            for (JsonElement childElement : children) {
                if (childElement.isJsonObject()) {
                    SimpleMetaModel childModel = parseJsonToMetaModel(childElement.getAsJsonObject());
                    childModels.add(childModel);
                }
            }
            metaModel.setChildren(childModels);
        }
        
        return metaModel;
    }
    
    /**
     * Parse inline attributes (@ prefixed) from JSON object and add as synthetic children
     */
    private void parseInlineAttributes(SimpleMetaModel metaModel, JsonObject jsonObj, String type) {
        // Check if this type supports inline attributes (has default subType)
        if (type == null || !supportsInlineAttributes(type)) {
            return;
        }
        
        // Parse @ prefixed attributes
        for (java.util.Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(com.draagon.meta.loader.model.MetaModel.INLINE_ATTR_PREFIX)) {
                // Remove @ prefix for attribute name
                String attrName = key.substring(1);
                JsonElement value = entry.getValue();
                
                // Cast value based on JSON type and add as synthetic child
                Object castedValue = castJsonValue(value);
                metaModel.addInlineAttribute(attrName, castedValue);
            }
        }
    }
    
    /**
     * Check if a type supports inline attributes (attr type has default subType)
     */
    private boolean supportsInlineAttributes(String type) {
        try {
            // Get the loader's type registry to check if attr type has default subType
            com.draagon.meta.registry.MetaDataTypeRegistry registry = 
                getLoader().getTypeRegistry();
            return registry != null && registry.getDefaultSubType("attr") != null;
        } catch (Exception e) {
            // If we can't determine, be conservative and don't allow inline attributes
            return false;
        }
    }
    
    /**
     * Cast JSON value to appropriate Java type based on JSON type
     */
    private Object castJsonValue(JsonElement jsonElement) {
        if (jsonElement.isJsonNull()) {
            return null;
        } else if (jsonElement.isJsonPrimitive()) {
            com.google.gson.JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                // Try int first, then double for non-integer numbers
                try {
                    return primitive.getAsInt();
                } catch (NumberFormatException e) {
                    return primitive.getAsDouble();
                }
            } else if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        // For arrays and objects, convert to string representation
        return jsonElement.toString();
    }
    
    /**
     * Helper method to get string value from JSON object
     */
    private String getJsonStringValue(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }
}
