package com.draagon.meta.generator.direct.json;

import com.draagon.meta.generator.MetaDataFilters;
import com.draagon.meta.generator.MetaDataWriter;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.generator.direct.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public abstract class JsonDirectWriter<T extends JsonDirectWriter> extends MetaDataWriter<T> {

    private final Gson gson;
    private final Writer writer;
    protected final JsonWriter out;

    public JsonDirectWriter(MetaDataLoader loader, OutputStream os ) throws MetaDataWriterException {
        this(loader, new OutputStreamWriter( os ));
    }

    public JsonDirectWriter(MetaDataLoader loader, Writer writer ) throws MetaDataWriterException {
        super(loader);
        this.gson = new Gson();
        this.writer = writer;
        try {
            this.out = gson.newJsonWriter( writer );
        } catch (IOException e) {
            throw new MetaDataWriterException( this, "Error opening JsonWriter: " + e, e );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Options

    public T withIndent( String indent ) {
        out.setIndent( indent );
        return (T) this;
    }

    public abstract void writeJson() throws MetaDataWriterException;

    protected JsonWriter out() {
        return out;
    }

    @Override
    public void close() throws MetaDataWriterException {
        try {
            if (out != null) out.close();
            //else if (writer != null) writer.close();
        } catch (IOException e) {
            throw new MetaDataWriterException( this, "Error closing "+(out!=null?"JsonWriter":"Writer")+": " + e, e );
        }
    }
}
