package com.metaobjects.io.object.json;

import com.metaobjects.MetaDataAware;
import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.json.JsonMetaDataWriter;
import com.metaobjects.io.object.gson.MetaObjectGsonInitializer;
import com.metaobjects.io.object.gson.MetaObjectSerializer;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.object.MetaObjectAware;

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
