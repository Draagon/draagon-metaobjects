/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 *
 * Moved from draagon-framework-java to avoid the dependency with draagon-utilities.
 */
package com.draagon.meta.util.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Meta Class loader for XML files
 */
public class XMLFileWriter
{
    private static Log log = LogFactory.getLog( XMLFileWriter.class );

    /**
     * Loads all the classes specified in the Filename
     */
    public static Document getBuilder() throws IOException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new IOException( "Unable to get a new XML Document Builder: " + e.toString(), e );
        }
    }

    public static void writeToStream( Document document, OutputStream out, boolean indent ) throws IOException {

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            if (indent) transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);

            transformer.transform( domSource, streamResult );
        }
        catch (TransformerException e) {
            throw new IOException( "Unable to write XML document [" + document.getDocumentURI() +"]: " + e.toString(), e );
        }
    }
}

