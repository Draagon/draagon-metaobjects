package com.metaobjects.generator.direct.metadata.file.json;

import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.direct.metadata.json.JsonDirectWriter;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.constraint.PlacementConstraint;
import com.metaobjects.constraint.CustomConstraint;
import com.metaobjects.constraint.Constraint;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.registry.TypeDefinition;
import com.metaobjects.registry.ChildRequirement;
import static com.metaobjects.MetaData.*;
import static com.metaobjects.loader.parser.json.JsonMetaDataParser.JSON_ATTR_PREFIX;
import com.metaobjects.MetaDataTypeId;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * v6.1.0: Registry-driven JSON Schema writer that creates schemas for validating metadata files.
 * This generates JSON Schema that validates the structure of metadata JSON files using dynamic
 * type discovery from the TypeDefinition registry.
 *
 * Features:
 * - Dynamic type enumeration from MetaDataRegistry
 * - Inheritance-aware attribute schemas
 * - Type-specific child requirement validation
 * - Automatic plugin type support
 * - No hardcoded type lists
 */
public class MetaDataFileSchemaWriter extends JsonDirectWriter<MetaDataFileSchemaWriter> {

    private static final Logger log = LoggerFactory.getLogger(MetaDataFileSchemaWriter.class);

    private String schemaVersion = "https://json-schema.org/draft/2020-12/schema";
    private String schemaId;
    private String title;
    private String description;

    // Registry-based type discovery
    private MetaDataRegistry typeRegistry;
    private List<PlacementConstraint> placementConstraints;
    private List<CustomConstraint> validationConstraints;

    public MetaDataFileSchemaWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader, out);
        this.typeRegistry = MetaDataRegistry.getInstance();
        this.placementConstraints = new ArrayList<>();
        this.validationConstraints = new ArrayList<>();

        log.info("Initialized registry-driven schema writer with {} registered types",
                typeRegistry.getRegisteredTypes().size());
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

    // Registry-based generation - no constraint files needed
    @Deprecated
    public MetaDataFileSchemaWriter addConstraintFile(String constraintFile) {
        log.warn("Constraint files are deprecated - using registry-based type discovery instead");
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
                ", registeredTypes=" + typeRegistry.getRegisteredTypes().size() +
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
     * Load constraint definitions and type registry data
     */
    private void loadConstraintDefinitions() {
        log.info("Loading registry data for schema generation: {} types, {} constraints",
                typeRegistry.getRegisteredTypes().size(),
                typeRegistry.getAllValidationConstraints().size());

        // Get constraints from the unified registry
        this.placementConstraints = typeRegistry.getPlacementValidationConstraints();
        this.validationConstraints = typeRegistry.getFieldValidationConstraints();

        log.debug("Registry types: {}", typeRegistry.getRegisteredTypeNames());
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
        
        // Define root metadata object structure using constants
        JsonObject properties = new JsonObject();
        properties.add(ATTR_METADATA, createMetaDataObjectSchema());
        schema.add("properties", properties);

        // Require metadata root element
        JsonArray required = new JsonArray();
        required.add(ATTR_METADATA);
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

        // Package property (optional) - using constants
        JsonObject packageSchema = new JsonObject();
        packageSchema.addProperty("type", "string");
        packageSchema.addProperty("description", "Package name for the metadata");
        properties.add(ATTR_PACKAGE, packageSchema);

        // Children array (required) - using constants
        JsonObject childrenSchema = new JsonObject();
        childrenSchema.addProperty("type", "array");
        childrenSchema.addProperty("description", "Array of metadata children (objects, fields, etc.)");
        JsonObject childrenItems = new JsonObject();
        childrenItems.add("$ref", new JsonPrimitive("#/$defs/MetaDataChild"));
        childrenSchema.add("items", childrenItems);
        properties.add(ATTR_CHILDREN, childrenSchema);
        
        metaDataSchema.add("properties", properties);
        
        // Children is required - using constants
        JsonArray required = new JsonArray();
        required.add(ATTR_CHILDREN);
        metaDataSchema.add("required", required);
        
        return metaDataSchema;
    }

    /**
     * Create reusable schema definitions using registry-based type discovery
     */
    private JsonObject createMetaDataDefinitions() {
        JsonObject definitions = new JsonObject();

        // Core structural definitions
        definitions.add("MetaDataChild", createMetaDataChildSchema());
        definitions.add("NameConstraints", createNameConstraintsSchema());

        // Dynamic type-specific definitions from registry
        generateTypeSpecificDefinitions(definitions);

        return definitions;
    }

    /**
     * Create schema for any metadata child element using dynamic type discovery
     */
    private JsonObject createMetaDataChildSchema() {
        JsonObject childSchema = new JsonObject();
        childSchema.addProperty("type", "object");
        childSchema.addProperty("description", "A metadata child element (dynamic types from registry)");

        // Generate oneOf array dynamically from all registered primary types
        JsonArray oneOf = new JsonArray();
        Set<String> primaryTypes = typeRegistry.getAllTypeDefinitions().stream()
                .map(TypeDefinition::getType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String primaryType : primaryTypes) {
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("type", "object");

            // Create properties object
            JsonObject properties = new JsonObject();
            JsonObject typeRef = new JsonObject();
            typeRef.add("$ref", new JsonPrimitive("#/$defs/" + capitalizeFirstLetter(primaryType)));
            properties.add(primaryType, typeRef);
            wrapper.add("properties", properties);

            // Add required array
            JsonArray required = new JsonArray();
            required.add(primaryType);
            wrapper.add("required", required);

            oneOf.add(wrapper);
        }

        childSchema.add("oneOf", oneOf);
        log.debug("Generated dynamic child schema for types: {}", primaryTypes);

        return childSchema;
    }

    /**
     * Generate type-specific schema definitions dynamically from registry
     */
    private void generateTypeSpecificDefinitions(JsonObject definitions) {
        // Group type definitions by primary type
        Map<String, List<TypeDefinition>> typeGroups = typeRegistry.getAllTypeDefinitions().stream()
                .collect(Collectors.groupingBy(TypeDefinition::getType));

        for (Map.Entry<String, List<TypeDefinition>> entry : typeGroups.entrySet()) {
            String primaryType = entry.getKey();
            List<TypeDefinition> typeDefs = entry.getValue();

            // Create schema for this primary type
            JsonObject typeSchema = createPrimaryTypeSchema(primaryType, typeDefs);
            definitions.add(capitalizeFirstLetter(primaryType), typeSchema);

            log.debug("Generated schema for type '{}' with {} subtypes",
                    primaryType, typeDefs.size());
        }
    }

    /**
     * Create schema for a primary type (object, field, etc.) with dynamic subtype enumeration
     */
    private JsonObject createPrimaryTypeSchema(String primaryType, List<TypeDefinition> typeDefs) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.addProperty("description", String.format("%s definition with %d registered subtypes",
                capitalizeFirstLetter(primaryType), typeDefs.size()));

        JsonObject properties = new JsonObject();

        // Name (required, with constraints) - using constants
        properties.add(ATTR_NAME, createNameConstraintsSchema());

        // Type/SubType (required) - dynamic enumeration from registry
        JsonObject typeSchema = new JsonObject();
        typeSchema.addProperty("type", "string");
        typeSchema.addProperty("description", String.format("%s subtype", capitalizeFirstLetter(primaryType)));

        // Generate enum from actual registered subtypes
        JsonArray typeEnum = new JsonArray();
        Set<String> subTypes = typeDefs.stream()
                .map(TypeDefinition::getSubType)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        subTypes.forEach(typeEnum::add);
        typeSchema.add("enum", typeEnum);
        properties.add(ATTR_SUBTYPE, typeSchema);

        // Children (optional if this type accepts children)
        if (hasChildRequirements(typeDefs)) {
            JsonObject childrenSchema = new JsonObject();
            childrenSchema.addProperty("type", "array");
            JsonObject childrenItems = new JsonObject();
            childrenItems.add("$ref", new JsonPrimitive("#/$defs/MetaDataChild"));
            childrenSchema.add("items", childrenItems);
            properties.add(ATTR_CHILDREN, childrenSchema);
        }

        schema.add("properties", properties);

        // Add pattern properties for inline attributes (@ prefixed)
        JsonObject patternProperties = new JsonObject();
        patternProperties.add("^" + JSON_ATTR_PREFIX + "[a-zA-Z][a-zA-Z0-9_]*$",
                createInlineAttributeValueSchema());
        schema.add("patternProperties", patternProperties);

        // Required properties - using constants
        JsonArray required = new JsonArray();
        required.add(ATTR_NAME);
        required.add(ATTR_SUBTYPE);
        schema.add("required", required);

        return schema;
    }

    /**
     * Create schema for name constraints using MetaDataConstants
     */
    private JsonObject createNameConstraintsSchema() {
        JsonObject nameSchema = new JsonObject();
        nameSchema.addProperty("type", "string");
        nameSchema.addProperty("description", "Name following MetaData naming constraints");

        // Use pattern from MetaDataConstants, converted for JSON Schema (remove anchors)
        String jsonSchemaPattern = VALID_NAME_PATTERN.replaceAll("^\\^|\\$$", ""); // Remove ^ and $
        nameSchema.addProperty("pattern", jsonSchemaPattern);
        nameSchema.addProperty("minLength", 1);
        nameSchema.addProperty("maxLength", 64);

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

    /**
     * Helper method to capitalize first letter of a string
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Check if any type definition in the list has child requirements
     */
    private boolean hasChildRequirements(List<TypeDefinition> typeDefs) {
        return typeDefs.stream()
                .anyMatch(def -> !def.getChildRequirements().isEmpty());
    }
}