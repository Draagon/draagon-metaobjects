package com.draagon.meta.loader.file;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.uri.URIHelper;
import com.draagon.meta.loader.uri.URIModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Handles loading
 *
 * Created by dmealing on 11/30/16.
 */
public class URIMetaDataSources extends MetaDataSources {

    private URIModel currentModel = null;

    public URIMetaDataSources(URI uri) {
        currentModel = URIHelper.toURIModel( uri );
        read( currentModel.getUriSource() );
    }

    public URIMetaDataSources(List<URI> uriSources ) {
        for ( URI uri : uriSources ) {
            currentModel = URIHelper.toURIModel( uri );
            read( currentModel.getUriSource() );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    protected InputStream getInputStreamForFilename(String filename) throws MetaDataException {

        // Set Source Directory if it's on the Model
        //String sourceDir = currentModel.getUriArg(URIHelper.URI_ARG_SOURCEDIR);
        //if ( sourceDir != null ) {
        //    setSourceDir( sourceDir );
            // Call the super method
            //super.getInputStreamForFilename(filename);
        //}
        //else {
        //    setSourceDir(null);
        //}

        // LOAD THE FILE
        if (filename == null) {
            throw new NullPointerException("The MetaData file was null on URI: "+currentModel.toURI());
        }

        try {
            List<ClassLoader> classLoaders = Arrays.asList( getClassLoader(), ClassLoader.getSystemClassLoader());
            return URIHelper.getInputStream( classLoaders, currentModel );
        } catch (IOException e) {
            throw new MetaDataException( "Could not open InputStream for URI: "+currentModel.toURI());
        }
    }

    /** Returns the class loader */
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
