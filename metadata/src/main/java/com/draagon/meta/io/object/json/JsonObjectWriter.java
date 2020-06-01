package com.draagon.meta.io.object.json;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonMetaDataWriter;
import com.draagon.meta.io.object.gson.MetaObjectGsonInitializer;
import com.draagon.meta.io.object.gson.MetaObjectSerializer;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObjectAware;

import java.io.IOException;
import java.io.Writer;

public class JsonObjectWriter extends JsonMetaDataWriter {

    private final Writer writer;

    public JsonObjectWriter(MetaDataLoader loader, Writer writer ) throws IOException {
        super(loader, writer);
        this.writer = writer;
    }

    public static void writeObject(MetaDataAware o, Writer out) throws IOException {

        JsonObjectWriter writer = new JsonObjectWriter(o.getMetaData().getLoader(), out);
        writer.setDefaultDateFormat();
        writer.write(o);
        writer.close();
    }

    public void write(Object vo) throws IOException {

        if ( vo == null ) throw new MetaDataIOException( this, "Cannot write a null Object");

        MetaObjectGsonInitializer.addSerializersToBuilder( getLoader(), builder());

        //try {
            gson().toJson( vo, writer);
        //}
        //catch (IOException e) {
        //    throw new MetaDataIOException( this, e.toString(), e );
        //}
    }
}
