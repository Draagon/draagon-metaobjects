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

import org.xml.sax.InputSource;

/**
 * Meta Class loader for XML files
 */
public class XercesXMLPlugin implements XMLPlugin
{
    private static Log log = LogFactory.getLog( XercesXMLPlugin.class );

    public Document loadFromStream( InputStream is )
            throws IOException
    {
        try
        {
            org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
            InputSource input = new InputSource( is );
            parser.parse( input );
            return parser.getDocument();
        }
        catch( IOException e )
        {
            log.error( "Unable to parse XML inputstream: " + e.getMessage() , e );
            throw e; //new MetaException( "IO Error parsing Meta XML: " + e.getMessage(), e );
        }
        catch( Exception e )
        {
            log.error( "Unable to load XML inputstream: " + e.getMessage() , e );
            throw new IOException( e.toString() );
        }
        catch( Throwable e )
        {
            log.error( "Throwable: Unable to load XML inputstream: " + e.getMessage() , e );
            throw new IOException( e.toString() );
        }
    }
}

