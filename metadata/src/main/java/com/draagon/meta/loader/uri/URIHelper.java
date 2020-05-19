package com.draagon.meta.loader.uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class URIHelper implements URIConstants {

    public final static List<String> VALID_URI_TYPES = Arrays.asList(
            URI_TYPE_TYPES, URI_TYPE_MODEL );

    public final static List<String> VALID_URI_SOURCE_TYPES = Arrays.asList(
            URI_SOURCE_FILE, URI_SOURCE_RESOURCE, URI_SOURCE_URL);

    public static URI toURI( String uriType, File f ) {
        return constructURI( uriType, URI_SOURCE_FILE, f.toString() );
    }

    public static URI toURI( String uriType, URL url ) {
        return constructURI( uriType, URI_SOURCE_URL, url.toString() );
    }

    public static URIModel toURIModel( String in ) {
        URIModel model = parseURIModel( in );
        validateUriType( model.getUriType() );
        validateUriSourceType( model.getUriSourceType() );
        validateUriSource( model.getUriSourceType(), model.getUriSource() );
        return model;
    }

    public static URIModel toURIModel( URI in ) {
        return toURIModel( in.toString() );
    }

    public static URI toURI( String in ) {
        return toURIModel( in.toString() ).toURI();
    }

    public static URI constructURI(String uriType, String uriSourceType, String source) {
        validateUriType( uriType );
        validateUriSourceType( uriSourceType );
        validateUriSource( uriSourceType, source );
        return constructValidatedURI( uriType, uriSourceType, source );
    }

    public static URI constructValidatedURI(String uriType, String uriSourceType, String source) {
        String uriStr = uriType + ":" + uriSourceType + ":" + source;
        try {
            return new URI(uriStr );
        } catch( URISyntaxException e ) {
            throw new IllegalArgumentException( "URI Syntax exception ["+uriStr+"]: "+ e.getMessage(), e );
        }
    }

    public static void validateUriType(String uriType) {
        if ( !VALID_URI_TYPES.contains( uriType )) {
            throw new IllegalArgumentException( "URI Type ["+uriType+"] is not valid, supported types: "+
                    VALID_URI_TYPES );
        }
    }

    public static void validateUriSourceType(String uriSourceType) {
        if ( !VALID_URI_SOURCE_TYPES.contains( uriSourceType )) {
            throw new IllegalArgumentException( "URI Source Type ["+uriSourceType+"] is not valid, supported types: "+
                    VALID_URI_SOURCE_TYPES );
        }
    }

    public static void validateUriSource(String uriSourceType, String source) {
        validateUriSourceType(uriSourceType);
        if ( uriSourceType.equals( URI_SOURCE_FILE )) {
            File f = new File( source );
            String path = null;
            try {
                path = f.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException( "File source is invalid path ["+source+"] "+e.toString(), e );
            }
        }
        else if ( uriSourceType.equals( URI_SOURCE_URL )) {
            try {
                new URL( source ).toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new IllegalArgumentException( "URL source has invalid syntax ["+source+"] "+e.toString(), e );
            }
        }
        else if ( uriSourceType.equals( URI_SOURCE_RESOURCE )) {
            String url = source;
            try {
                String name = source;
                if ( source.indexOf(':')>0) {
                    String prefix = source.substring(0, source.indexOf(':'));
                    if ( prefix.equals( "file" )
                            //|| prefix.equals( "classpath" )
                    ) {
                        new URL(source);
                    } else {
                        throw new IllegalArgumentException( "Classpath resource must start with 'classpath:' not ["+source+"]");
                    }
                }
                else {
                    //new URL( url);
                    // TODO: Not much you can do to check this
                }
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException( "Classpath resource has invalid syntax ["+url+"] "+e.toString(), e );
            }
        }
    }

    public static boolean isValidUriSourceType(String uriSourceType) {
        try {
            validateUriSourceType( uriSourceType );
            return true;
        } catch( IllegalArgumentException e ) {
            return false;
        }
    }

    public static boolean isValidUriType(String uriType) {
        try {
            validateUriType( uriType );
            return true;
        } catch( IllegalArgumentException e ) {
            return false;
        }
    }

    public static boolean isValidUriSource(String uriSourceType, String source) {
        try {
            validateUriSource( uriSourceType, source );
            return true;
        } catch( IllegalArgumentException e ) {
            return false;
        }
    }

    public static boolean isTypesURI( URI uri ) {
        if ( uri.getScheme().startsWith( URI_TYPE_TYPES )) {
            return true;
        }
        return false;
    }

    public static boolean isModelURI( URI uri ) {
        if ( uri.getScheme().startsWith( URI_TYPE_MODEL )) {
            return true;
        }
        return false;
    }

    public static URIModel parseURIModel( String in ) {

        String uriType = null;
        String uriSourceType = null;
        String uriSource = null;

        String token = null;
        String afterType = in;
        String tail = in;

        //////////////////////////////////////////////////////////////////////////
        // Extract the URI Type

        int i = in.indexOf(":");
        if ( i == 0 ) {
            throw new IllegalArgumentException("Invalid syntax, cannot start with : ["+in+"]");
        }
        else if ( i > 0 ) {
            token = in.substring(0, i);
            tail = in.substring(i+1);
            if (VALID_URI_TYPES.contains(token)) {
                uriType = token;
                afterType = tail;

                i = tail.indexOf(":");
                if ( i >= 0 ) {
                    token = tail.substring(0, i);
                    tail = tail.substring(i + 1);

                } else {
                    throw new IllegalArgumentException("Invalid syntax after type ["+token+":] : ["+in+"]");
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////
        // Extract the URI SubType

        if ( uriType == null ) {
            uriType = URI_TYPE_MODEL;
        }

        // NOTE:  Assume it's a model if types isn't specified
        if (token.startsWith("resource")) {

            uriSourceType = URI_SOURCE_RESOURCE;
            uriSource = tail;
        }
        else if (token.startsWith("file")) {

            uriSourceType = URI_SOURCE_FILE;
            i = tail.indexOf("//");
            if ( i == 0 ) {
                uriSource = tail.substring(2);
            } else {
                uriSource = tail;
            }
        }
        else if (token.startsWith("url")) {

            uriSourceType = URI_SOURCE_URL;
            i = tail.indexOf("http");
            if ( i == 0 ) {
                uriSource = tail;
            } else {
                //uriSource = tail;
                throw new IllegalArgumentException("Invalid URL syntax after [url:] : ["+tail+"]");
            }
        }
        else if (token.startsWith("http")) {

            uriSourceType = URI_SOURCE_URL;
            uriSource = afterType;
        }
        else if (token.startsWith("classpath")) {

            uriSourceType = URI_SOURCE_RESOURCE;
            uriSource = afterType;
        }

        return new URIModel( uriType, uriSourceType, uriSource );
    }


    public static InputStream getInputStream(URIModel model) throws IOException {
        return getInputStream( null, model );
    }

    public static InputStream getInputStream( ClassLoader classLoader, URIModel model) throws IOException {

        String st = model.getUriSourceType();
        if ( URI_SOURCE_RESOURCE.equals( st )) {
            URL url = null;

            if (classLoader != null )  url = classLoader.getResource(model.getUriSource());
            if ( url == null ) url = ClassLoader.getSystemClassLoader().getResource(model.getUriSource());
            if ( url == null )  throw new IOException( "Could not open resource: "+ model.toURI());

            return url.openStream();
        }
        else if ( URI_SOURCE_FILE.equals( st )) {
            return new FileInputStream( new File( model.getUriSource() ));
        }
        else if ( URI_SOURCE_URL.equals( st )) {
            return new URL( model.getUriSource() ).openStream();
        }
        else {
            throw new IllegalArgumentException( "Unsupported Source Type: "+ model.toURI().toString() );
        }
    }

    public static InputStream getInputStream(URI uri) throws IOException {
        return getInputStream( toURIModel( uri ));
    }
}
