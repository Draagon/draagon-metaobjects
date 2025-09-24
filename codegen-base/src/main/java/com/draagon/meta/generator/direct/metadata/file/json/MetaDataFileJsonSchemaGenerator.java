package com.draagon.meta.generator.direct.metadata.file.json;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.SingleJsonDirectGeneratorBase;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * Generator for creating JSON Schema that validates metadata files themselves.
 * v6.0.0: Creates schemas for validating the structure of metadata JSON files
 * (like {"metadata": {"children": [...]}}), not the data instances.
 * 
 * This reads constraint definitions to understand valid metadata structure and
 * generates JSON Schema that can validate metadata files during development.
 */
public class MetaDataFileJsonSchemaGenerator extends SingleJsonDirectGeneratorBase {

    public final static String ARG_SCHEMA_VERSION = "schemaVersion";
    public final static String ARG_SCHEMA_ID = "schemaId";
    public final static String ARG_TITLE = "title";
    public final static String ARG_DESCRIPTION = "description";

    private String schemaVersion = "https://json-schema.org/draft/2020-12/schema";
    private String schemaId = null;
    private String title = "MetaData File JSON Schema";
    private String description = "JSON Schema for validating MetaData file structure";

    //////////////////////////////////////////////////////////////////////
    // SingleFileDirectorGenerator Execute Override methods

    @Override
    protected void parseArgs() {
        if (hasArg(ARG_SCHEMA_VERSION)) {
            schemaVersion = getArg(ARG_SCHEMA_VERSION);
        }
        
        if (hasArg(ARG_SCHEMA_ID)) {
            schemaId = getArg(ARG_SCHEMA_ID);
        }
        
        if (hasArg(ARG_TITLE)) {
            title = getArg(ARG_TITLE);
        }
        
        if (hasArg(ARG_DESCRIPTION)) {
            description = getArg(ARG_DESCRIPTION);
        }

        if (log.isDebugEnabled()) log.debug(toString());
    }

    @Override
    protected JsonDirectWriter getWriter(MetaDataLoader loader, OutputStream os) throws GeneratorIOException {
        return new MetaDataFileSchemaWriter(loader, os)
                .withSchemaVersion(schemaVersion)
                .withSchemaId(schemaId)
                .withTitle(title)
                .withDescription(description);
    }

    ///////////////////////////////////////////////////
    // Misc Methods

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName() + "{");
        sb.append("args=").append(getArgs());
        sb.append(", filters=").append(getFilters());
        sb.append(", schemaVersion=").append(schemaVersion);
        sb.append(", schemaId=").append(schemaId);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append('}');
        return sb.toString();
    }

    ///////////////////////////////////////////////////
    // Service Provider Pattern Registration

    // JSON Schema generation attribute constants
    public static final String JSON_SCHEMA_VERSION = "jsonSchemaVersion";
    public static final String JSON_SCHEMA_ID = "jsonSchemaId";
    public static final String JSON_TITLE = "jsonTitle";
    public static final String JSON_DESCRIPTION = "jsonDescription";
    public static final String JSON_FORMAT = "jsonFormat";
    public static final String JSON_PATTERN = "jsonPattern";
    public static final String JSON_ENUM = "jsonEnum";
    public static final String JSON_MINIMUM = "jsonMinimum";
    public static final String JSON_MAXIMUM = "jsonMaximum";

    /**
     * Registers JSON Schema generation attributes for use by the service provider pattern.
     * Called by CodeGenMetaDataProvider to extend existing MetaData types with JSON Schema-specific attributes.
     */
    public static void registerJsonSchemaAttributes(com.draagon.meta.registry.MetaDataRegistry registry) {
        // Object-level JSON Schema attributes
        registry.findType("object", "base")
            .optionalAttribute(JSON_SCHEMA_VERSION, "string")
            .optionalAttribute(JSON_SCHEMA_ID, "string")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string");

        registry.findType("object", "pojo")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string");

        // Field-level JSON Schema attributes
        registry.findType("field", "base")
            .optionalAttribute(JSON_TITLE, "string")
            .optionalAttribute(JSON_DESCRIPTION, "string")
            .optionalAttribute(JSON_FORMAT, "string")
            .optionalAttribute(JSON_PATTERN, "string")
            .optionalAttribute(JSON_ENUM, "string");

        registry.findType("field", "string")
            .optionalAttribute(JSON_PATTERN, "string")
            .optionalAttribute(JSON_FORMAT, "string");

        registry.findType("field", "int")
            .optionalAttribute(JSON_MINIMUM, "int")
            .optionalAttribute(JSON_MAXIMUM, "int");

        registry.findType("field", "long")
            .optionalAttribute(JSON_MINIMUM, "long")
            .optionalAttribute(JSON_MAXIMUM, "long");
    }
}