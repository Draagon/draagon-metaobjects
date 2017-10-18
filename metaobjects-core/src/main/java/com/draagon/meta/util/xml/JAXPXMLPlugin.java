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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Meta Class loader for XML files
 */
public class JAXPXMLPlugin implements XMLPlugin
{
    private static Log log = LogFactory.getLog( XercesXMLPlugin.class );

    public Document loadFromStream( InputStream is )
            throws IOException
    {
        // See if we can even use this builder
        try
        {
            DocumentBuilderFactory dbf =  DocumentBuilderFactory.newInstance();
            dbf.setValidating( false );
            DocumentBuilder db = dbf.newDocumentBuilder();

            return db.parse( is );
        }
        catch( IOException e )
        {
            throw e; //new MetaException( "IO Error parsing Meta XML: " + e.getMessage(), e );
        }
        catch( Exception e )
        {
            log.error( "Unable to load XML inputstream: " + e.getMessage() , e );
            throw new IOException( e.toString() );
        }
    }
}

