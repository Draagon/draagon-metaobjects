package com.draagon.meta.generator.direct.json;

import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public abstract class JsonDirectWriter<T extends JsonDirectWriter> extends GeneratorIOWriter<T> {

    private final Gson gson;
    private final Writer writer;
    protected final JsonWriter out;

    public JsonDirectWriter(MetaDataLoader loader, OutputStream os ) throws GeneratorIOException {
        this(loader, new OutputStreamWriter( os ));
    }

    public JsonDirectWriter(MetaDataLoader loader, Writer writer ) throws GeneratorIOException {
        super(loader);
        this.gson = new Gson();
        this.writer = writer;
        try {
            this.out = gson.newJsonWriter( writer );
        } catch (IOException e) {
            throw new GeneratorIOException( this, "Error opening JsonWriter: " + e, e );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Options

    public T withIndent( String indent ) {
        out.setIndent( indent );
        return (T) this;
    }

    public abstract void writeJson() throws GeneratorIOException;

    protected JsonWriter out() {
        return out;
    }

    @Override
    public void close() throws GeneratorIOException {
        try {
            if (out != null) out.close();
            //else if (writer != null) writer.close();
        } catch (IOException e) {
            throw new GeneratorIOException( this, "Error closing "+(out!=null?"JsonWriter":"Writer")+": " + e, e );
        }
    }
}
