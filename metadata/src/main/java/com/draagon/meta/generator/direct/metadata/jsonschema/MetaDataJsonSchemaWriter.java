package com.draagon.meta.generator.direct.metadata.jsonschema;

import com.draagon.meta.attr.*;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.SubTypeConfig;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.loader.types.TypesConfig;
import com.google.gson.*;

import java.io.OutputStream;
import java.util.*;

/**
 * JSON Schema writer for MetaData configuration.
 * Generates JSON Schema that can validate JSON-based metadata files.
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
        JsonObject schema = new JsonObject();

        // Schema metadata
        schema.addProperty("$schema", schemaVersion);
        if (schemaId != null) {
            schema.addProperty("$id", schemaId);
        }
        schema.addProperty("title", title != null ? title : "MetaData JSON Schema");
        schema.addProperty("description", description != null ? description : "JSON Schema for MetaData validation");
        schema.addProperty("type", "object");

        // Generate definitions for all types
        JsonObject definitions = new JsonObject();
        writeTypesDefinitions(definitions, getLoader().getTypesConfig());
        schema.add("$defs", definitions);

        // Root properties - allow any of the defined types
        JsonObject properties = new JsonObject();
        JsonObject rootMetadata = new JsonObject();
        rootMetadata.addProperty("$ref", "#/$defs/metadata");
        properties.add("metadata", rootMetadata);
        schema.add("properties", properties);

        // Set root object as JSON
        setJsonObject(schema);
    }

    protected void writeTypesDefinitions(JsonObject definitions, TypesConfig tsc) throws GeneratorIOException {
        if (tsc.getTypes() != null && !tsc.getTypes().isEmpty()) {
            for (TypeConfig tc : tsc.getTypes()) {
                writeTypeDefinition(definitions, tc);
            }
        } else {
            log.warn("There are no Type Configurations defined in MetaDataLoader: " + getLoader());
        }
    }

    protected void writeTypeDefinition(JsonObject definitions, TypeConfig tc) throws GeneratorIOException {
        JsonObject typeDef = new JsonObject();
        typeDef.addProperty("type", "object");
        typeDef.addProperty("description", "Definition for " + tc.getName() + " type");

        // Properties for this type
        JsonObject properties = new JsonObject();

        // Standard properties
        writeStandardProperties(properties, tc);

        // Child element properties
        List<ChildConfig> kids = new ArrayList<>();
        if (tc.getTypeChildConfigs() != null) kids.addAll(tc.getTypeChildConfigs());
        if (tc.getSubTypes() != null) {
            for (SubTypeConfig stc : tc.getSubTypes()) {
                if (stc.getChildConfigs() != null) {
                    kids.addAll(stc.getChildConfigs());
                }
            }
        }
        writeChildProperties(properties, tc, kids);

        typeDef.add("properties", properties);

        // Additional properties - allow extending
        typeDef.addProperty("additionalProperties", true);

        definitions.add(tc.getName(), typeDef);

        // Write subtypes as separate definitions
        if (tc.getSubTypes() != null) {
            for (SubTypeConfig stc : tc.getSubTypes()) {
                writeSubTypeDefinition(definitions, tc, stc);
            }
        }
    }

    protected void writeSubTypeDefinition(JsonObject definitions, TypeConfig tc, SubTypeConfig stc) throws GeneratorIOException {
        String defName = tc.getName() + "_" + stc.getName();
        JsonObject subTypeDef = new JsonObject();

        // Extend the base type
        JsonArray allOf = new JsonArray();
        JsonObject baseRef = new JsonObject();
        baseRef.addProperty("$ref", "#/$defs/" + tc.getName());
        allOf.add(baseRef);

        // Add subtype-specific properties
        if (stc.getChildConfigs() != null && !stc.getChildConfigs().isEmpty()) {
            JsonObject subTypeProps = new JsonObject();
            JsonObject properties = new JsonObject();
            writeChildProperties(properties, tc, stc.getChildConfigs());
            subTypeProps.add("properties", properties);
            allOf.add(subTypeProps);
        }

        subTypeDef.add("allOf", allOf);
        definitions.add(defName, subTypeDef);
    }

    protected void writeStandardProperties(JsonObject properties, TypeConfig tc) throws GeneratorIOException {
        // Package property
        JsonObject packageProp = new JsonObject();
        packageProp.addProperty("type", "string");
        packageProp.addProperty("description", "Package name for this " + tc.getName());
        properties.add("package", packageProp);

        if (!tc.getName().equals("metadata")) {
            // Name property
            JsonObject nameProp = new JsonObject();
            nameProp.addProperty("type", "string");
            nameProp.addProperty("description", "Name of this " + tc.getName());
            properties.add("name", nameProp);

            // Type property with enum values
            JsonObject typeProp = new JsonObject();
            typeProp.addProperty("type", "string");
            typeProp.addProperty("description", "Type of this " + tc.getName());
            if (tc.getSubTypeNames() != null && !tc.getSubTypeNames().isEmpty()) {
                JsonArray enumValues = new JsonArray();
                for (String subType : tc.getSubTypeNames()) {
                    enumValues.add(subType);
                }
                typeProp.add("enum", enumValues);
            }
            properties.add("type", typeProp);

            // Super property
            JsonObject superProp = new JsonObject();
            superProp.addProperty("type", "string");
            superProp.addProperty("description", "Super type of this " + tc.getName());
            properties.add("super", superProp);
        }
    }

    protected void writeChildProperties(JsonObject properties, TypeConfig tc, List<ChildConfig> childConfigs) throws GeneratorIOException {
        if (childConfigs == null) return;

        Map<String, Set<String>> childTypeMap = new HashMap<>();

        // Group children by type
        for (ChildConfig cc : childConfigs) {
            if (MetaAttribute.TYPE_ATTR.equals(cc.getType())) {
                // This is an attribute
                writeAttributeProperty(properties, cc.getName(), cc.getSubType());
            } else {
                // This is a child element
                childTypeMap.computeIfAbsent(cc.getType(), k -> new HashSet<>()).add(cc.getSubType());
            }
        }

        // Write child element properties
        for (Map.Entry<String, Set<String>> entry : childTypeMap.entrySet()) {
            writeChildElementProperty(properties, entry.getKey(), entry.getValue());
        }
    }

    protected void writeAttributeProperty(JsonObject properties, String name, String subType) throws GeneratorIOException {
        JsonObject attrProp = new JsonObject();

        if (subType.equals(StringAttribute.SUBTYPE_STRING)) {
            attrProp.addProperty("type", "string");
        } else if (subType.equals(StringArrayAttribute.SUBTYPE_STRING_ARRAY)) {
            attrProp.addProperty("type", "array");
            JsonObject items = new JsonObject();
            items.addProperty("type", "string");
            attrProp.add("items", items);
        } else if (subType.equals(LongAttribute.SUBTYPE_LONG)) {
            attrProp.addProperty("type", "integer");
            attrProp.addProperty("format", "int64");
        } else if (subType.equals(IntAttribute.SUBTYPE_INT)) {
            attrProp.addProperty("type", "integer");
            attrProp.addProperty("format", "int32");
        } else if (subType.equals(BooleanAttribute.SUBTYPE_BOOLEAN)) {
            attrProp.addProperty("type", "boolean");
        } else if (subType.equals(PropertiesAttribute.SUBTYPE_PROPERTIES)) {
            attrProp.addProperty("type", "object");
            attrProp.addProperty("description", "Properties as key-value pairs");
        } else if (subType.equals(ClassAttribute.SUBTYPE_CLASS)) {
            attrProp.addProperty("type", "string");
            attrProp.addProperty("pattern", "^[a-zA-Z_$][a-zA-Z\\d_$]*(?:\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*$");
            attrProp.addProperty("description", "Java class name");
        } else {
            attrProp.addProperty("type", "string");
        }

        attrProp.addProperty("description", "Attribute: " + name + " (" + subType + ")");
        properties.add(name, attrProp);
    }

    protected void writeChildElementProperty(JsonObject properties, String type, Set<String> subTypes) throws GeneratorIOException {
        JsonObject childProp = new JsonObject();
        childProp.addProperty("type", "array");
        childProp.addProperty("description", "Array of " + type + " elements");

        JsonObject items = new JsonObject();

        if (subTypes.size() == 1 && subTypes.contains("*")) {
            // Any subtype allowed
            items.addProperty("$ref", "#/$defs/" + type);
        } else if (subTypes.size() == 1) {
            // Single specific subtype
            String subType = subTypes.iterator().next();
            items.addProperty("$ref", "#/$defs/" + type + "_" + subType);
        } else {
            // Multiple subtypes - use anyOf
            JsonArray anyOf = new JsonArray();
            for (String subType : subTypes) {
                JsonObject subTypeRef = new JsonObject();
                subTypeRef.addProperty("$ref", "#/$defs/" + type + "_" + subType);
                anyOf.add(subTypeRef);
            }
            items.add("anyOf", anyOf);
        }

        childProp.add("items", items);
        properties.add(type, childProp);
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