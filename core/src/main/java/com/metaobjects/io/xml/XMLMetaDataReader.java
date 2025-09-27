package com.metaobjects.io.xml;

import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.MetaDataReader;
import com.metaobjects.loader.MetaDataLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public abstract class XMLMetaDataReader extends MetaDataReader {

    private final InputStream in;
    private Document doc = null;

    protected XMLMetaDataReader(MetaDataLoader loader, InputStream in ) {
        super(loader);
        this.in = in;
    }

    protected Document loadXML() throws IOException {
        // TODO: Add flag for validating XML
        doc = loadDocumentFromStream(in, false);
        return doc;
    }

    @Override
    public void close() throws MetaDataIOException {
        /*if (in!=null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new MetaDataIOException(this, e.toString(), e);
            }
        }*/
    }

    /**
     * Private method to load Document from InputStream with validation option (replaces XMLUtil.loadFromStream)
     */
    private static Document loadDocumentFromStream(InputStream is, boolean validating) throws IOException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            return documentBuilder.parse(is);
        } catch (ParserConfigurationException e) {
            throw new IOException("Unable to get a new XML Document Builder: " + e.toString(), e);
        } catch (SAXException e) {
            throw new IOException("Error attempting to read XML from inputStream: " + e.getMessage(), e);
        }
    }
}