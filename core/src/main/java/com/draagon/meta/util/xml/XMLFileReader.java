/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 *
 * Moved from draagon-framework-java to avoid the dependency with draagon-utilities.
 */
package com.draagon.meta.util.xml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Meta Class loader for XML files
 */
public class XMLFileReader
{
    private static Log log = LogFactory.getLog( XMLFileReader.class );

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
}

