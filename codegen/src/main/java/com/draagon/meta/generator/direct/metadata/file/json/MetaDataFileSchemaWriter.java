package com.draagon.meta.generator.direct.metadata.file.json;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;

/**
 * v6.0.0: JSON Schema writer that creates schemas for validating metadata files themselves.
 * This generates JSON Schema that validates the structure of metadata JSON files,
 * not the data instances they describe.
 * 
 * Reads constraint definitions to understand what constitutes valid metadata structure
 * and generates appropriate validation schemas.
 */
public class MetaDataFileSchemaWriter extends JsonDirectWriter<MetaDataFileSchemaWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataFileSchemaWriter.class);

    private String schemaVersion = "https://json-schema.org/draft/2020-12/schema";
    private String schemaId;
    private String title;
    private String description;
    private List<String> constraintFiles = new ArrayList<>();
    
    // Constraint system for reading definitions
    private ConstraintDefinitionParser constraintParser;
    private Map<String, ConstraintDefinitionParser.AbstractConstraintDefinition> abstractConstraints;
    private List<ConstraintDefinitionParser.ConstraintInstance> constraintInstances;

    public MetaDataFileSchemaWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.constraintParser = new ConstraintDefinitionParser();
        this.abstractConstraints = new HashMap<>();
        this.constraintInstances = new ArrayList<>();
        
        // Default constraint files to load
        this.constraintFiles.add("META-INF/constraints/core-constraints.json");
    }

    /////////////////////////////////////////////////////////////////////////
    // Configuration Methods

    public MetaDataFileSchemaWriter withSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    public MetaDataFileSchemaWriter withSchemaId(String schemaId) {
        this.schemaId = schemaId;
        return this;
    }

    public MetaDataFileSchemaWriter withTitle(String title) {
        this.title = title;
        return this;
    }

    public MetaDataFileSchemaWriter withDescription(String description) {
        this.description = description;
        return this;
    }

    public MetaDataFileSchemaWriter addConstraintFile(String constraintFile) {
        this.constraintFiles.add(constraintFile);
        return this;
    }

    /////////////////////////////////////////////////////////////////////////
    // Generator Methods

    @Override
    public String toString() {
        return "MetaDataFileSchemaWriter{" +
                "schemaVersion='" + schemaVersion + '\'' +
                ", schemaId='" + schemaId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", constraintFiles=" + constraintFiles +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // JSON Schema Generation Methods

    @Override
    public void writeJson() throws GeneratorIOException {
        try {
            // Load constraint definitions for schema generation
            loadConstraintDefinitions();
            
            // Generate schema for metadata file structure
            JsonObject schema = generateMetaDataFileSchema();
            setJsonObject(schema);
            
        } catch (Exception e) {
            throw new GeneratorIOException(this, "Failed to generate metadata file JSON schema", e);
        }
    }

    /**
     * Load constraint definitions from configured files
     */
    private void loadConstraintDefinitions() throws ConstraintParseException {
        log.info("Loading constraint definitions for metadata file schema generation from {} files", constraintFiles.size());
        
        for (String constraintFile : constraintFiles) {
            try {
                ConstraintDefinitionParser.ConstraintDefinitions definitions = 
                    constraintParser.parseFromResource(constraintFile);
                    
                // Collect abstract definitions
                for (ConstraintDefinitionParser.AbstractConstraintDefinition abstractDef : definitions.getAbstracts()) {
                    abstractConstraints.put(abstractDef.getId(), abstractDef);
                }
                
                // Collect constraint instances
                constraintInstances.addAll(definitions.getInstances());
                
                log.debug("Loaded {} abstracts and {} instances from {}", 
                    definitions.getAbstracts().size(), definitions.getInstances().size(), constraintFile);
                    
            } catch (ConstraintParseException e) {
                log.warn("Could not load constraint file [{}] for schema generation: {}", constraintFile, e.getMessage());
                // Continue with other files - constraint files are optional for schema generation
            }
        }
        
        log.info("Loaded total {} abstract constraints and {} constraint instances for metadata file schema", 
            abstractConstraints.size(), constraintInstances.size());
    }

    /**
     * Generate JSON schema for validating metadata files
     */
    private JsonObject generateMetaDataFileSchema() {
        JsonObject schema = new JsonObject();
        
        // Add schema metadata
        schema.addProperty("$schema", schemaVersion);
        if (schemaId != null) {
            schema.addProperty("$id", schemaId);
        }
        schema.addProperty("title", title != null ? title : "MetaData File JSON Schema");
        schema.addProperty("description", description != null ? description : 
            "JSON Schema for validating MetaData file structure and constraints");
        
        schema.addProperty("type", "object");
        
        // Define root metadata object structure
        JsonObject properties = new JsonObject();
        properties.add("metadata", createMetaDataObjectSchema());
        schema.add("properties", properties);
        
        // Require metadata root element
        JsonArray required = new JsonArray();
        required.add("metadata");
        schema.add("required", required);
        
        // Add definitions for reusable components
        schema.add("$defs", createMetaDataDefinitions());
        
        return schema;
    }

    /**
     * Create schema for the root metadata object
     */
    private JsonObject createMetaDataObjectSchema() {
        JsonObject metaDataSchema = new JsonObject();
        metaDataSchema.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        // Package property (optional)
        JsonObject packageSchema = new JsonObject();
        packageSchema.addProperty("type", "string");
        packageSchema.addProperty("description", "Package name for the metadata");
        properties.add("package", packageSchema);
        
        // Children array (required)
        JsonObject childrenSchema = new JsonObject();
        childrenSchema.addProperty("type", "array");
        childrenSchema.addProperty("description", "Array of metadata children (objects, fields, etc.)");
        JsonObject childrenItems = new JsonObject();
        childrenItems.add("$ref", new JsonPrimitive("#/$defs/MetaDataChild"));
        childrenSchema.add("items", childrenItems);
        properties.add("children", childrenSchema);
        
        metaDataSchema.add("properties", properties);
        
        // Children is required
        JsonArray required = new JsonArray();
        required.add("children");
        metaDataSchema.add("required", required);
        
        return metaDataSchema;
    }

    /**
     * Create reusable schema definitions
     */
    private JsonObject createMetaDataDefinitions() {
        JsonObject definitions = new JsonObject();
        
        // MetaDataChild - union of object, field, etc.
        definitions.add("MetaDataChild", createMetaDataChildSchema());
        definitions.add("MetaObject", createMetaObjectSchema());
        definitions.add("MetaField", createMetaFieldSchema());
        definitions.add("NameConstraints", createNameConstraintsSchema());
        
        return definitions;
    }

    /**
     * Create schema for any metadata child element
     */
    private JsonObject createMetaDataChildSchema() {
        JsonObject childSchema = new JsonObject();
        childSchema.addProperty("type", "object");
        childSchema.addProperty("description", "A metadata child element (object, field, etc.)");
        
        // One of: object, field, attr, etc.
        JsonArray oneOf = new JsonArray();
        
        JsonObject objectWrapper = new JsonObject();
        JsonObject objectProperty = new JsonObject();
        objectProperty.add("$ref", new JsonPrimitive("#/$defs/MetaObject"));
        objectWrapper.add("object", objectProperty);
        oneOf.add(objectWrapper);
        
        JsonObject fieldWrapper = new JsonObject();
        JsonObject fieldProperty = new JsonObject();
        fieldProperty.add("$ref", new JsonPrimitive("#/$defs/MetaField"));
        fieldWrapper.add("field", fieldProperty);
        oneOf.add(fieldWrapper);
        
        childSchema.add("oneOf", oneOf);
        
        return childSchema;
    }

    /**
     * Create schema for MetaObject definitions
     */
    private JsonObject createMetaObjectSchema() {
        JsonObject objectSchema = new JsonObject();
        objectSchema.addProperty("type", "object");
        objectSchema.addProperty("description", "MetaObject definition");
        
        JsonObject properties = new JsonObject();
        
        // Name (required, with constraints)
        properties.add("name", createNameConstraintsSchema());
        
        // Type (required)
        JsonObject typeSchema = new JsonObject();
        typeSchema.addProperty("type", "string");
        typeSchema.addProperty("description", "Object type (pojo, value, data, etc.)");
        JsonArray typeEnum = new JsonArray();
        typeEnum.add("pojo");
        typeEnum.add("value");
        typeEnum.add("data");
        typeEnum.add("proxy");
        typeEnum.add("mapped");
        typeSchema.add("enum", typeEnum);
        properties.add("type", typeSchema);
        
        // Children (optional)
        JsonObject childrenSchema = new JsonObject();
        childrenSchema.addProperty("type", "array");
        JsonObject childrenItems = new JsonObject();
        childrenItems.add("$ref", new JsonPrimitive("#/$defs/MetaDataChild"));
        childrenSchema.add("items", childrenItems);
        properties.add("children", childrenSchema);
        
        objectSchema.add("properties", properties);
        
        // Required properties
        JsonArray required = new JsonArray();
        required.add("name");
        required.add("type");
        objectSchema.add("required", required);
        
        return objectSchema;
    }

    /**
     * Create schema for MetaField definitions
     */
    private JsonObject createMetaFieldSchema() {
        JsonObject fieldSchema = new JsonObject();
        fieldSchema.addProperty("type", "object");
        fieldSchema.addProperty("description", "MetaField definition");
        
        JsonObject properties = new JsonObject();
        
        // Name (required, with constraints)
        properties.add("name", createNameConstraintsSchema());
        
        // Type (required)
        JsonObject typeSchema = new JsonObject();
        typeSchema.addProperty("type", "string");
        typeSchema.addProperty("description", "Field data type");
        JsonArray typeEnum = new JsonArray();
        typeEnum.add("string");
        typeEnum.add("int");
        typeEnum.add("long");
        typeEnum.add("double");
        typeEnum.add("float");
        typeEnum.add("boolean");
        typeEnum.add("date");
        typeEnum.add("timestamp");
        typeSchema.add("enum", typeEnum);
        properties.add("type", typeSchema);
        
        fieldSchema.add("properties", properties);
        
        // Required properties
        JsonArray required = new JsonArray();
        required.add("name");
        required.add("type");
        fieldSchema.add("required", required);
        
        return fieldSchema;
    }

    /**
     * Create schema for name constraints based on loaded constraint definitions
     */
    private JsonObject createNameConstraintsSchema() {
        JsonObject nameSchema = new JsonObject();
        nameSchema.addProperty("type", "string");
        nameSchema.addProperty("description", "Name following MetaData naming constraints");
        
        // Apply naming pattern constraints from loaded constraints
        for (ConstraintDefinitionParser.ConstraintInstance constraint : constraintInstances) {
            String constraintType = constraint.getAbstractRef() != null ? constraint.getAbstractRef() : constraint.getInlineType();
            String targetName = constraint.getTargetName();
            
            if ("pattern".equals(constraintType) && "name".equals(targetName)) {
                Map<String, Object> params = constraint.getParameters();
                if (params.containsKey("pattern")) {
                    nameSchema.addProperty("pattern", params.get("pattern").toString());
                }
            }
            
            if ("length".equals(constraintType) && "name".equals(targetName)) {
                Map<String, Object> params = constraint.getParameters();
                if (params.containsKey("min")) {
                    nameSchema.addProperty("minLength", ((Number) params.get("min")).intValue());
                }
                if (params.containsKey("max")) {
                    nameSchema.addProperty("maxLength", ((Number) params.get("max")).intValue());
                }
            }
        }
        
        // Default pattern if no constraints found
        if (!nameSchema.has("pattern")) {
            nameSchema.addProperty("pattern", "^[a-zA-Z][a-zA-Z0-9_]*$");
        }
        if (!nameSchema.has("minLength")) {
            nameSchema.addProperty("minLength", 1);
        }
        if (!nameSchema.has("maxLength")) {
            nameSchema.addProperty("maxLength", 64);
        }
        
        return nameSchema;
    }
}