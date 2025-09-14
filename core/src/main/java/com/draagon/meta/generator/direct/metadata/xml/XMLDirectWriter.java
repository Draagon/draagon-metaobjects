package com.draagon.meta.generator.direct.metadata.xml;

import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.XMLUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;

public abstract class XMLDirectWriter<T extends XMLDirectWriter> extends GeneratorIOWriter<T> {

    private OutputStream out;
    private Document doc;

    public XMLDirectWriter(MetaDataLoader loader, OutputStream out ) throws GeneratorIOException {
        super(loader);
        this.out = out;
    }

    public abstract void writeXML() throws GeneratorIOException;

    /////////////////////////////////////////////////////////////////////////
    // XMLWriter Methods

    protected Document createDocument() throws GeneratorIOException {
        try {
            return XMLUtil.getBuilder().newDocument();
        } catch( IOException e ) {
            throw new GeneratorIOException( this, "Error creating XML Builder: "+e, e );
        }
    }

    protected void writeDocument(Document doc, OutputStream out) throws GeneratorIOException {
        try {
            XMLUtil.writeToStream( doc, out, true );
        } catch (IOException e) {
            throw new GeneratorIOException( this, "Error writing XML Document to Outputstream: " + e, e );
        }
    }

    protected Document doc() throws GeneratorIOException {
        if (doc == null) doc = createDocument();
        return doc;
    }

    @Override
    public void close() throws GeneratorIOException {
        writeDocument( doc, out );
        try {
            out.close();
        } catch (IOException e) {
            throw new GeneratorIOException( this, "Error closing outputstream: " + e, e );
        }
    }
}