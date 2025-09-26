package com.draagon.meta.generator.direct.metadata.xml;

import com.draagon.meta.generator.GeneratorIOWriter;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
            return createDocumentBuilder().newDocument();
        } catch( IOException e ) {
            throw new GeneratorIOException( this, "Error creating XML Builder: "+e, e );
        }
    }

    protected void writeDocument(Document doc, OutputStream out) throws GeneratorIOException {
        try {
            writeDocumentToStream(doc, out, true);
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

    /**
     * Private method to create DocumentBuilder (replaces XMLUtil.getBuilder)
     */
    private static DocumentBuilder createDocumentBuilder() throws IOException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            return documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Unable to get a new XML Document Builder: " + e.toString(), e);
        }
    }

    /**
     * Private method to write Document to OutputStream (replaces XMLUtil.writeToStream)
     */
    private static void writeDocumentToStream(Document document, OutputStream out, boolean indent) throws IOException {
        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (indent) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new IOException("Unable to write XML document [" + document.getDocumentURI() + "]: " + e.toString(), e);
        }
    }
}