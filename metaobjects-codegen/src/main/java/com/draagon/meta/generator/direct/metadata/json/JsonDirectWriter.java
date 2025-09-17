package com.draagon.meta.generator.direct.metadata.json;

import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Base class for JSON direct writers.
 * This is the JSON equivalent of XMLDirectWriter.
 */
public abstract class JsonDirectWriter<T extends JsonDirectWriter> extends GeneratorIOWriter<T> {

    private OutputStream out;
    private JsonObject jsonObject;
    private Gson gson;

    public JsonDirectWriter(MetaDataLoader loader, OutputStream out) throws GeneratorIOException {
        super(loader);
        this.out = out;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    public abstract void writeJson() throws GeneratorIOException;

    /////////////////////////////////////////////////////////////////////////
    // JsonWriter Methods

    protected void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    protected JsonObject getJsonObject() throws GeneratorIOException {
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        return jsonObject;
    }

    protected void writeJsonToStream(JsonObject json, OutputStream out) throws GeneratorIOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
            writer.flush();
        } catch (IOException e) {
            throw new GeneratorIOException(this, "Error writing JSON to OutputStream: " + e, e);
        }
    }

    @Override
    public void close() throws GeneratorIOException {
        writeJsonToStream(getJsonObject(), out);
        try {
            out.close();
        } catch (IOException e) {
            throw new GeneratorIOException(this, "Error closing outputstream: " + e, e);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // Utility Methods

    protected Gson getGson() {
        return gson;
    }

    protected void setGson(Gson gson) {
        this.gson = gson;
    }
}