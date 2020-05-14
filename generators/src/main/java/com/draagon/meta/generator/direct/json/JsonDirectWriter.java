package com.draagon.meta.generator.direct.json;

import com.draagon.meta.generator.WriterContext;
import com.draagon.meta.generator.direct.DirectWriter;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.loader.MetaDataLoader;
import com.google.gson.stream.JsonWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class JsonDirectWriter<D> extends DirectWriter<JsonWriter,D> {

    /** Passes at context data for the execution */
    public static class Context extends WriterContext<Context,JsonWriter,String> {

        protected Context(JsonWriter json, String path ) {
            this( null, json, path );
        }
        protected Context( Context parent, JsonWriter json, String path ) {
            super(parent, json, path);
        }

        @Override
        public Context newInstance(Context parent, JsonWriter root, String node) {
            return new Context( parent, root, node );
        }

        @Override
        public String getNodePathName() {
            return node;
        }
    }

    public JsonDirectWriter(MetaDataLoader loader, MetaDataFilters filters) {
        super(loader, filters);
    }

    @Override
    public void write( JsonWriter json, D data ) {

        Context c = new Context( json, "metadata" );
        writeJson( c, data );
    }

    public abstract void writeJson( Context c, D data );

}
