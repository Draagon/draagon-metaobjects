package com.metaobjects.io.json;

import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.MetaDataReader;
import com.metaobjects.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DateFormat;

public abstract class JsonMetaDataReader extends MetaDataReader {

    private final Reader reader;
    private final GsonBuilder builder;

    private Gson gson;
    private JsonReader in;

    protected JsonMetaDataReader(MetaDataLoader loader, Reader reader ) {
        super(loader);
        this.builder = new GsonBuilder();
        this.reader = reader;
    }

    protected GsonBuilder builder() {
        return builder;
    }

    public <T extends JsonMetaDataReader> T withDeserializer( Class<?> type, JsonDeserializer<?> deserializer) {
        builder().registerTypeAdapter(type, deserializer);
        return (T) this;
    }

    public <T extends JsonMetaDataReader> T withDateFormat(String pattern) {
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

    protected JsonReader in() {
        if (in == null) {
            in = gson().newJsonReader( reader );
        }
        return in;
    }

    @Override
    public void close() throws IOException {

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new MetaDataIOException(this, e.toString(), e);
            }
        }
        else if (reader != null ) {
            reader.close();
        }
    }
}
