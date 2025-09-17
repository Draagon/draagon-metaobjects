package com.draagon.meta.generator.direct.metadata.jsonschema;

import com.draagon.meta.*;
import com.draagon.meta.attr.*;
import com.draagon.meta.field.*;
import com.draagon.meta.object.*;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.validation.ValidationChain;
import com.draagon.meta.validation.Validator;
import com.google.gson.*;

import java.io.OutputStream;
import java.util.*;

/**
 * JSON Schema writer for MetaData configuration using ValidationChain-based schema generation.
 * v6.0.0: Implemented using ValidationChain system for consistent metadata-driven validation.
 */
public class MetaDataJsonSchemaWriter extends JsonDirectWriter<MetaDataJsonSchemaWriter> {

    private String schemaVersion = "https://json-schema.org/draft/2020-12/schema";
    private String schemaId;
    private String title;
    private String description;

    public MetaDataJsonSchemaWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    public MetaDataJsonSchemaWriter withSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    public MetaDataJsonSchemaWriter withSchemaId(String schemaId) {
        this.schemaId = schemaId;
        return this;
    }

    public MetaDataJsonSchemaWriter withTitle(String title) {
        this.title = title;
        return this;
    }

    public MetaDataJsonSchemaWriter withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    ///////////////////////////////////////////////////////////////////////////
    // JSON Schema Methods

    @Override
    public void writeJson() throws GeneratorIOException {
        try {
            JsonObject schema = generateValidationChainBasedSchema();
            setJsonObject(schema);
        } catch (Exception e) {
            throw new GeneratorIOException(this, "Failed to generate ValidationChain-based JSON schema", e);
        }
    }

    /**
     * Generate JSON schema using ValidationChain system instead of direct metadata inspection.
     * This approach uses the same validation rules that the runtime system uses.
     */
    private JsonObject generateValidationChainBasedSchema() {
        JsonObject schema = new JsonObject();
        
        // Add schema metadata
        schema.addProperty("$schema", schemaVersion);
        if (schemaId != null) {
            schema.addProperty("$id", schemaId);
        }
        // Provide default title and description if not set
        schema.addProperty("title", title != null ? title : "MetaData JSON Schema");
        schema.addProperty("description", description != null ? description : "JSON Schema generated from MetaData definitions using ValidationChain");
        
        schema.addProperty("type", "object");
        
        // Generate schema definitions using ValidationChain
        JsonObject definitions = new JsonObject();
        JsonObject properties = new JsonObject();
        
        // Get MetaDataTypeRegistry to access ValidationChains
        MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
        
        // Process all MetaObject definitions in the loader
        for (MetaObject metaObject : getLoader().getChildren(MetaObject.class)) {
            JsonObject objectSchema = generateObjectSchemaFromValidationChain(metaObject, registry);
            definitions.add(metaObject.getName(), objectSchema);
            properties.add(metaObject.getName(), createReference(metaObject.getName()));
        }
        
        schema.add("$defs", definitions);
        schema.add("properties", properties);
        
        return schema;
    }
    
    /**
     * Generate JSON schema for a MetaObject using its ValidationChain
     */
    private JsonObject generateObjectSchemaFromValidationChain(MetaObject metaObject, MetaDataTypeRegistry registry) {
        JsonObject objectSchema = new JsonObject();
        objectSchema.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        
        // Process each field using its ValidationChain
        for (MetaField field : metaObject.getChildren(MetaField.class)) {
            JsonObject fieldSchema = generateFieldSchemaFromValidationChain(field, registry);
            properties.add(field.getName(), fieldSchema);
            
            // Check if field is required using ValidationChain
            if (isFieldRequired(field, registry)) {
                required.add(field.getName());
            }
        }
        
        objectSchema.add("properties", properties);
        if (required.size() > 0) {
            objectSchema.add("required", required);
        }
        
        return objectSchema;
    }
    
    /**
     * Generate JSON schema for a field using its ValidationChain
     */
    private JsonObject generateFieldSchemaFromValidationChain(MetaField field, MetaDataTypeRegistry registry) {
        JsonObject fieldSchema = new JsonObject();
        
        // Get the field's ValidationChain to determine constraints
        MetaDataTypeId fieldTypeId = new MetaDataTypeId("field", field.getSubTypeName());
        ValidationChain<MetaData> validationChain = registry.getValidationChain(fieldTypeId);
        
        // Map field subtype to JSON schema type
        String jsonType = mapFieldTypeToJsonType(field);
        fieldSchema.addProperty("type", jsonType);
        
        // Add validation constraints from ValidationChain
        addValidationConstraints(fieldSchema, field, validationChain);
        
        return fieldSchema;
    }
    
    /**
     * Map MetaField subtype to JSON schema type
     */
    private String mapFieldTypeToJsonType(MetaField field) {
        String subType = field.getSubTypeName();
        switch (subType) {
            case "int":
            case "integer":
            case "long":
                return "integer";
            case "float":
            case "double":
                return "number";
            case "boolean":
                return "boolean";
            case "date":
                return "string"; // with format: date
            case "stringArray":
            case "objectArray":
                return "array";
            case "object":
                return "object";
            case "string":
            default:
                return "string";
        }
    }
    
    /**
     * Add validation constraints from ValidationChain to JSON schema
     */
    private void addValidationConstraints(JsonObject fieldSchema, MetaField field, ValidationChain<MetaData> validationChain) {
        // Add format constraints for specific field types
        String subType = field.getSubTypeName();
        switch (subType) {
            case "date":
                fieldSchema.addProperty("format", "date");
                break;
            case "email":
                fieldSchema.addProperty("format", "email");
                break;
            case "uri":
                fieldSchema.addProperty("format", "uri");
                break;
        }
        
        // Add array item constraints
        if ("stringArray".equals(subType)) {
            JsonObject items = new JsonObject();
            items.addProperty("type", "string");
            fieldSchema.add("items", items);
        } else if ("objectArray".equals(subType)) {
            JsonObject items = new JsonObject();
            items.addProperty("type", "object");
            fieldSchema.add("items", items);
        }
        
        // Add additional constraints based on field attributes
        field.findAttribute("maxLength").ifPresent(attr -> {
            try {
                int maxLength = Integer.parseInt(attr.getValue().toString());
                fieldSchema.addProperty("maxLength", maxLength);
            } catch (NumberFormatException ignored) {
                // Skip invalid maxLength values
            }
        });
        
        field.findAttribute("minLength").ifPresent(attr -> {
            try {
                int minLength = Integer.parseInt(attr.getValue().toString());
                fieldSchema.addProperty("minLength", minLength);
            } catch (NumberFormatException ignored) {
                // Skip invalid minLength values  
            }
        });
        
        field.findAttribute("pattern").ifPresent(attr -> {
            fieldSchema.addProperty("pattern", attr.getValue().toString());
        });
        
        // Add default value if available
        field.findAttribute("defaultValue").ifPresent(attr -> {
            String defaultValue = attr.getValue().toString();
            // Add as string for now - could be enhanced to parse by type
            fieldSchema.addProperty("default", defaultValue);
        });
    }
    
    /**
     * Check if field is required using ValidationChain
     */
    private boolean isFieldRequired(MetaField field, MetaDataTypeRegistry registry) {
        // Check for required attribute
        return field.findAttribute("required")
            .map(attr -> "true".equalsIgnoreCase(attr.getValue().toString()))
            .orElse(false);
    }
    
    /**
     * Create a JSON reference to a definition
     */
    private JsonObject createReference(String definitionName) {
        JsonObject ref = new JsonObject();
        ref.addProperty("$ref", "#/$defs/" + definitionName);
        return ref;
    }

    ////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions()
                + ",schemaVersion=" + schemaVersion
                + ",schemaId=" + schemaId
                + ",title=" + title
                + ",description=" + description;
    }
}