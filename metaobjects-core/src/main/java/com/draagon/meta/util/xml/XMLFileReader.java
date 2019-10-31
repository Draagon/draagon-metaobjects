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

/**
 * Meta Class loader for XML files
 */
public class XMLFileReader
{
    private static Log log = LogFactory.getLog( XMLFileReader.class );

    /**
     * Loads all the classes specified in the Filename
     */
    public static Document loadFromStream( InputStream is )
            throws IOException
    {
        Document d = null;

        // Try Xerces First
        d = getPluginAndTryLoad( is, "com.draagon.meta.util.xml.XercesXMLPlugin" );
        if ( d != null ) return d;

        // Try the JAXP Parser
        d = getPluginAndTryLoad( is, "com.draagon.meta.util.xml.JAXPXMLPlugin" );
        if ( d != null ) return d;

        // If not Plugin worked, then error out
        throw new IOException( "No valid XML Plugin was found to load XML document" );
    }

    private static Document getPluginAndTryLoad( InputStream is, String className )
            throws IOException
    {
        XMLPlugin plugin = getPlugin( className );
        if ( plugin == null ) return null;

        //ystem.out.println( "TRYING PLUGIN: " + plugin );

        try {
            return plugin.loadFromStream( is );
        }
        catch( IOException e ) {
            log.debug( "Error attempting to use plugin [" + className + "]: " + e.getMessage() );
            throw e;
        }
        catch( Throwable e ) {
            //ystem.out.println( "ERROR: Cannot use [" + className + "]: " + e.getMessage() );
            log.debug( "Error attempting to use plugin [" + className + "]: " + e.getMessage() );
        }

        return null;
    }

    private static XMLPlugin getPlugin( String className )
    {
        try {
            Class c = Class.forName( className );
            return (XMLPlugin) c.newInstance();
        }
        catch( Throwable e ) {
            //ystem.out.println( "ERROR: Cannot load [" + className + "]: " + e.getMessage() );
            log.debug( "Unable to load XML Plugin [" + className + "]: " + e.getMessage() );
            return null;
        }
    }
}

