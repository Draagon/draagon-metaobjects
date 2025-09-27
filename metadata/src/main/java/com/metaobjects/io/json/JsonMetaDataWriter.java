package com.metaobjects.io.json;

import com.metaobjects.io.MetaDataWriter;
import com.metaobjects.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;

public abstract class JsonMetaDataWriter extends MetaDataWriter {

    private final Writer writer;
    private final GsonBuilder builder;

    private Gson gson;
    private JsonWriter out;

    protected JsonMetaDataWriter(MetaDataLoader loader, Writer writer ) throws IOException {
        super(loader);
        this.builder = new GsonBuilder();
        this.writer = writer;
    }

    protected GsonBuilder builder() {
        return builder;
    }

    public <T extends JsonMetaDataWriter> T withPrettyPrint() {
        builder().setPrettyPrinting();
        return (T) this;
    }

    public <T extends JsonMetaDataWriter> T withSerializer( Class<?> type, JsonSerializer<?> serializer) {
        builder().registerTypeAdapter(type, serializer);
        return (T) this;
    }

    public <T extends JsonMetaDataWriter> T withDateFormat(String pattern) {
        builder().setDateFormat(pattern);
        return (T) this;
    }

    protected void setDefaultDateFormat() {
        builder.setDateFormat(DateFormat.FULL, DateFormat.FULL);
    }

    protected Gson gson() {
        if ( gson == null ) {
            gson = builder.create();
        }
        return gson;
    }

    protected JsonWriter out() throws IOException {
        if ( out == null ) out = gson().newJsonWriter(writer);
        return out;
    }

    protected void writeJson(JsonElement jsonElement) throws IOException {
        writer.write(gson().toJson(jsonElement));
    }

    protected void writeJson(String json) throws IOException {
        writer.write(json);
    }

    @Override
    public void close() throws IOException {
        if ( out != null ) {
            out.close();
        }
        else if ( writer != null ) {
            writer.close();
        }
    }
}
