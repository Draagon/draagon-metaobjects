/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 *
 * Moved from draagon-framework-java to avoid the dependency with draagon-utilities.
 */
package com.draagon.meta.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log log = LogFactory.getLog( XMLUtil.class );

    /**
     * Loads all the classes specified in the Filename
     */
    public static Document loadFromStream( InputStream is ) throws IOException {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            return db.parse(is);
        }
        catch( ParserConfigurationException | SAXException e ) {
            throw new IOException(  "Error attempting to open XML inputStream: " + e.getMessage(), e );
        }
    }

    /**
     * Create an instance of an XML DocumentBuilder
     */
    public static DocumentBuilder getBuilder() throws IOException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
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
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (indent) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);

            transformer.transform( domSource, streamResult );
        }
        catch (TransformerException e) {
            throw new IOException( "Unable to write XML document [" + document.getDocumentURI() +"]: " + e.toString(), e );
        }
    }
}

