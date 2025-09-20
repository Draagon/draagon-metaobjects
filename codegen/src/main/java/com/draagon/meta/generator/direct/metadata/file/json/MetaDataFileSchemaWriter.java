package com.draagon.meta.generator.direct.metadata.file.json;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.constraint.Constraint;
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
    
    // Constraint system for accessing programmatic definitions
    private ConstraintRegistry constraintRegistry;
    private List<PlacementConstraint> placementConstraints;
    private List<ValidationConstraint> validationConstraints;

    public MetaDataFileSchemaWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.constraintRegistry = ConstraintRegistry.getInstance();
        this.placementConstraints = new ArrayList<>();
        this.validationConstraints = new ArrayList<>();
        
        // Note: Constraint files no longer used - constraints are programmatic
        // this.constraintFiles.add("META-INF/constraints/core-constraints.json");
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
     * Load constraint definitions from programmatic constraint registry
     */
    private void loadConstraintDefinitions() {
        log.info("Loading constraint definitions for metadata file schema generation from programmatic registry");
        
        // Get constraints from the unified registry
        this.placementConstraints = constraintRegistry.getPlacementConstraints();
        this.validationConstraints = constraintRegistry.getValidationConstraints();
        
        log.info("Loaded {} placement constraints and {} validation constraints for metadata file schema", 
            placementConstraints.size(), validationConstraints.size());
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
        
        // Add pattern properties for inline attributes (@ prefixed)
        JsonObject patternProperties = new JsonObject();
        patternProperties.add("^@[a-zA-Z][a-zA-Z0-9_]*$", createInlineAttributeValueSchema());
        objectSchema.add("patternProperties", patternProperties);
        
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
        
        // Add pattern properties for inline attributes (@ prefixed)
        JsonObject patternProperties = new JsonObject();
        patternProperties.add("^@[a-zA-Z][a-zA-Z0-9_]*$", createInlineAttributeValueSchema());
        fieldSchema.add("patternProperties", patternProperties);
        
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
        
        // Apply standard naming pattern used by programmatic constraints
        // This is the same pattern enforced by ValidationConstraint in MetaField.java
        nameSchema.addProperty("pattern", "^[a-zA-Z][a-zA-Z0-9_]*$");
        nameSchema.addProperty("minLength", 1);
        nameSchema.addProperty("maxLength", 64);
        
        log.debug("Generated name constraints schema with pattern validation");
        
        return nameSchema;
    }

    /**
     * Create schema for inline attribute values (@ prefixed attributes)
     * Supports boolean, number, and string values
     */
    private JsonObject createInlineAttributeValueSchema() {
        JsonObject valueSchema = new JsonObject();
        valueSchema.addProperty("description", "Inline attribute value (@ prefixed) - supports boolean, number, or string");
        
        // Allow boolean, number, or string values
        JsonArray anyOf = new JsonArray();
        
        // Boolean value
        JsonObject boolSchema = new JsonObject();
        boolSchema.addProperty("type", "boolean");
        anyOf.add(boolSchema);
        
        // Number value
        JsonObject numberSchema = new JsonObject();
        numberSchema.addProperty("type", "number");
        anyOf.add(numberSchema);
        
        // String value
        JsonObject stringSchema = new JsonObject();
        stringSchema.addProperty("type", "string");
        anyOf.add(stringSchema);
        
        valueSchema.add("anyOf", anyOf);
        
        return valueSchema;
    }
}