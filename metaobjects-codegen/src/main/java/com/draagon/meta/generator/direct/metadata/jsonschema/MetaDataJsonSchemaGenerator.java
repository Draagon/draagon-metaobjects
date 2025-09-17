package com.draagon.meta.generator.direct.metadata.jsonschema;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.SingleJsonDirectGeneratorBase;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.OutputStream;

/**
 * Generator for creating JSON Schema from MetaData configuration.
 * This is the JSON equivalent of the XSD generators, providing schema validation
 * for JSON-based metadata files.
 */
public class MetaDataJsonSchemaGenerator extends SingleJsonDirectGeneratorBase {

    public final static String ARG_SCHEMA_VERSION = "schemaVersion";
    public final static String ARG_SCHEMA_ID = "schemaId";
    public final static String ARG_TITLE = "title";
    public final static String ARG_DESCRIPTION = "description";

    private String schemaVersion = "https://json-schema.org/draft/2020-12/schema";
    private String schemaId = null;
    private String title = "MetaData JSON Schema";
    private String description = "JSON Schema for MetaData configuration files";

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
        return new MetaDataJsonSchemaWriter(loader, os)
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
}