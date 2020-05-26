package com.draagon.meta.io.xml;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.MetaDataWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.XMLUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;

public abstract class XMLMetaDataWriter extends MetaDataWriter {

    private final OutputStream out;
    private Document doc;
    private boolean flushed = false;

    protected XMLMetaDataWriter(MetaDataLoader loader, OutputStream out ) {
        super(loader);
        this.out = out;
    }

    protected void initDoc() throws IOException {
        if (doc == null) doc = createDocument();
    }

    protected Document doc() {
        return doc;
    }

    protected Document createDocument() throws IOException {
        try {
            // TODO: Add flag for validating
            return XMLUtil.getBuilder(false).newDocument();
        } catch( IOException e ) {
            throw new MetaDataIOException( this, "Error creating XML Builder: "+e, e );
        }
    }

    protected void writeDocument(Document doc, OutputStream out) throws IOException {
        try {
            XMLUtil.writeToStream( doc, out, true );
        } catch (IOException e) {
            throw new MetaDataIOException( this, "Error writing XML Document to Outputstream: " + e, e );
        }
    }

    public void flush() throws IOException {
        if ( !flushed ) {
            flushed = true;
            writeDocument(doc, out);
        }
    }

    @Override
    public void close() throws IOException {

        flush();

        /*if (out!=null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new MetaDataIOException( this, e.toString(), e );
            }
        }*/
    }
}
