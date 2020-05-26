package com.draagon.meta.io.json;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.MetaDataWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;

public abstract class JsonMetaDataWriter extends MetaDataWriter {

    private final Gson gson;
    private final Writer writer;
    private final JsonWriter out;

    protected JsonMetaDataWriter(MetaDataLoader loader, Writer writer ) throws IOException {
        super(loader);
        this.gson = new Gson();
        this.writer = writer;
        this.out = gson.newJsonWriter( writer );
    }

    public JsonMetaDataWriter withIndent( String indent ) {
        out.setIndent( indent );
        return this;
    }

    protected JsonWriter out() {
        return out;
    }

    @Override
    public void close() throws IOException {
        if ( out != null ) {
            out.close();
        }
        /*else if ( writer != null ) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new MetaDataIOException( this, e.toString(), e );
            }
        }*/
    }
}
