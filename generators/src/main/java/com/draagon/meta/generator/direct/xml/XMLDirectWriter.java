package com.draagon.meta.generator.direct.xml;

import com.draagon.meta.generator.MetaDataWriter;
import com.draagon.meta.generator.MetaDataWriterException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.xml.XMLFileWriter;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;

public abstract class XMLDirectWriter<T extends XMLDirectWriter> extends MetaDataWriter<T> {

    private OutputStream out;
    private Document doc;

    public XMLDirectWriter(MetaDataLoader loader, OutputStream out ) throws MetaDataWriterException {
        super(loader);
        this.out = out;
    }

    public abstract void writeXML() throws MetaDataWriterException;

    /////////////////////////////////////////////////////////////////////////
    // XMLWriter Methods

    protected Document createDocument() throws MetaDataWriterException {
        try {
            return XMLFileWriter.getBuilder();
        } catch( IOException e ) {
            throw new MetaDataWriterException( this, "Error creating XML Builder: "+e, e );
        }
    }

    protected void writeDocument(Document doc, OutputStream out) throws MetaDataWriterException {
        try {
            XMLFileWriter.writeToStream( doc, out, true );
        } catch (IOException e) {
            throw new MetaDataWriterException( this, "Error writing XML Document to Outputstream: " + e, e );
        }
    }

    protected Document doc() throws MetaDataWriterException {
        if (doc == null) doc = createDocument();
        return doc;
    }

    @Override
    public void close() throws MetaDataWriterException {
        writeDocument( doc, out );
        try {
            out.close();
        } catch (IOException e) {
            throw new MetaDataWriterException( this, "Error closing outputstream: " + e, e );
        }
    }
}
