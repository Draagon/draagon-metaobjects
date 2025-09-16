package com.draagon.meta.generator.direct.metadata.jsonschema;

import com.draagon.meta.attr.*;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.json.JsonDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.*;

import java.io.OutputStream;
import java.util.*;

/**
 * JSON Schema writer for MetaData configuration.
 * v6.0.0: Temporarily disabled pending ValidationChain-based schema generation implementation.
 * 
 * TODO: Implement ValidationChain-based JSON schema generation in Phase B
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
        // v6.0.0: Temporarily throw exception pending ValidationChain implementation
        throw new UnsupportedOperationException(
            "MetaDataJsonSchemaWriter is temporarily disabled in v6.0.0. " +
            "JSON schema generation will be reimplemented using ValidationChain in Phase B. " +
            "Please use ValidationChain-based validation instead of JSON schema validation for now."
        );
    }

    /** v6.0.0: Disabled pending ValidationChain-based implementation in Phase B */
    // All schema generation methods temporarily removed - will be reimplemented using ValidationChain

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