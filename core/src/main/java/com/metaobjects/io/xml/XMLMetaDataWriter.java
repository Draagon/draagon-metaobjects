package com.metaobjects.io.xml;

import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.MetaDataWriter;
import com.metaobjects.loader.MetaDataLoader;
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
            return createDocumentBuilder(false).newDocument();
        } catch( IOException e ) {
            throw new MetaDataIOException( this, "Error creating XML Builder: "+e, e );
        }
    }

    protected void writeDocument(Document doc, OutputStream out) throws IOException {
        try {
            writeDocumentToStream(doc, out, true);
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

    /**
     * Private method to create DocumentBuilder (replaces XMLUtil.getBuilder)
     */
    private static DocumentBuilder createDocumentBuilder(boolean validating) throws IOException {
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

    ///////////////////////////////////////////////////
    // Service Provider Pattern Registration

    // XML metadata serialization attribute constants
    public static final String XML_META_ELEMENT_NAME = "xmlMetaElementName";
    public static final String XML_META_NAMESPACE = "xmlMetaNamespace";
    public static final String XML_META_SCHEMA_LOCATION = "xmlMetaSchemaLocation";
    public static final String XML_META_VERSION = "xmlMetaVersion";
    public static final String XML_META_ENCODING = "xmlMetaEncoding";
    public static final String XML_META_STANDALONE = "xmlMetaStandalone";

    /**
     * Registers XML metadata serialization attributes for use by the service provider pattern.
     * Called by CoreMetaDataProvider to extend existing MetaData types with XML metadata-specific attributes.
     */
    public static void registerXMLMetaDataAttributes(com.metaobjects.registry.MetaDataRegistry registry) {
        // Object-level XML metadata attributes
        registry.findType("object", "base")
            .optionalAttribute(XML_META_ELEMENT_NAME, "string")
            .optionalAttribute(XML_META_NAMESPACE, "string")
            .optionalAttribute(XML_META_SCHEMA_LOCATION, "string")
            .optionalAttribute(XML_META_VERSION, "string");

        registry.findType("object", "pojo")
            .optionalAttribute(XML_META_ELEMENT_NAME, "string")
            .optionalAttribute(XML_META_NAMESPACE, "string");

        // Field-level XML metadata attributes
        registry.findType("field", "base")
            .optionalAttribute(XML_META_ELEMENT_NAME, "string")
            .optionalAttribute(XML_META_NAMESPACE, "string");

        // Global metadata attributes
        registry.findType("metadata", "base")
            .optionalAttribute(XML_META_ENCODING, "string")
            .optionalAttribute(XML_META_STANDALONE, "boolean")
            .optionalAttribute(XML_META_VERSION, "string");
    }
}