/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 *
 * Moved from draagon-framework-java to avoid the dependency with draagon-utilities.
 */
package com.draagon.meta.util;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Meta Class loader for XML files
 */
public class XMLUtil
{
    private static final Logger log = LoggerFactory.getLogger(XMLUtil.class);

    /**
     * Loads all the classes specified in the Filename
     */
    /*public static Document load(Reader in, String encoding, boolean validating ) throws IOException {

        try {
            return getBuilder( validating ).parse(new ReaderInputStream( in, encoding));
        } catch( SAXException e ) {
            throw new IOException(  "Error attempting to read XML from inputStream: " + e.getMessage(), e );
        }
    }*/

    /**
     * Loads all the classes specified in the Filename
     */
    public static Document loadFromStream( InputStream is ) throws IOException {
        return loadFromStream( is, false );
    }

    /**
     * Loads all the classes specified in the Filename
     */
    public static Document loadFromStream( InputStream is, boolean validating ) throws IOException {

        try {
            return getBuilder( validating ).parse(is);
        } catch( SAXException e ) {
            throw new IOException(  "Error attempting to read XML from inputStream: " + e.getMessage(), e );
        }
    }

    /**
     * Create an instance of an XML DocumentBuilder
     */
    public static DocumentBuilder getBuilder() throws IOException {
        return getBuilder( false );
    }

    /**
     * Create an instance of an XML DocumentBuilder
     */
    public static DocumentBuilder getBuilder( boolean validating ) throws IOException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            //documentFactory.setValidating(validating);
            return documentFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IOException( "Unable to get a new XML Document Builder: " + e.toString(), e );
        }
    }

    /**
     * Write an XML Document to an OutputStream
     */
    public static void writeToStream(Document document, OutputStream out, boolean indent ) throws IOException {
        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            getTransformer(document, indent).transform( domSource, streamResult );
        }
        catch (TransformerException e) {
            throw new IOException( "Unable to write XML document [" + document.getDocumentURI() +"]: " + e.toString(), e );
        }
    }
    /**
     * Write an XML Document to a Writer
     */
    public static void write(Document document, Writer out, boolean indent ) throws IOException {
        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            getTransformer(document, indent).transform( domSource, streamResult );
        }
        catch (TransformerException e) {
            throw new IOException( "Unable to write XML document [" + document.getDocumentURI() +"]: " + e.toString(), e );
        }
    }

    /** Get an XML Transformer for the specified Document and specify indent */
    public static Transformer getTransformer(Document document, boolean indent) throws IOException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (indent) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            return transformer;
        }
        catch (TransformerException e) {
            throw new IOException( "Unable to write XML document [" + document.getDocumentURI() +"]: " + e.toString(), e );
        }
    }
}

