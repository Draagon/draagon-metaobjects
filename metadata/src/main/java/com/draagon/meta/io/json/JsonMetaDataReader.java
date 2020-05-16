package com.draagon.meta.io.json;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.MetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public abstract class JsonMetaDataReader extends MetaDataReader {

    private final Gson gson;
    private final Reader reader;
    private final JsonReader in;

    protected JsonMetaDataReader(MetaDataLoader loader, Reader reader ) {
        super(loader);
        this.gson = new Gson();
        this.reader = reader;
        this.in = gson.newJsonReader( reader );
    }

    protected JsonReader in() {
        return in;
    }

    @Override
    public void close() throws MetaDataIOException {

        if ( in!=null ) {
            try {
                in.close();
            }
            catch (IOException e) {
                if ( reader != null ) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {}
                }
                throw new MetaDataIOException(this, e.toString(), e);
            }
        }
    }
}
