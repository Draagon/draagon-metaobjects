package com.draagon.meta.generator.direct.xml;

import com.draagon.meta.generator.WriterContext;
import com.draagon.meta.generator.direct.DirectWriter;
import com.draagon.meta.generator.direct.MetaDataFilters;
import com.draagon.meta.loader.MetaDataLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class XMLDirectWriter<D> extends DirectWriter<Document,D> {

    /** Passes at context data for the execution */
    public static class Context extends WriterContext<Context,Document,Element> {

        protected Context(Document doc) {
            super(doc);
        }
        protected Context(Context parent, Document doc, Element e ) {
            super(parent, doc, e);
        }
        @Override
        public Context newInstance(Context parent, Document root, Element node) {
            return new Context( parent, root, node );
        }
    }

    public XMLDirectWriter(MetaDataLoader loader, MetaDataFilters filters) {
        super(loader, filters);
    }

    @Override
    public void write( Document doc, D data ) {

        Context c = new Context( doc );
        writeXML( c, data );
    }

    public abstract void writeXML( Context c, D data );
}
