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
public class URIFileMetaDataSources extends FileMetaDataSources {

    private URIModel currentModel = null;

    public URIFileMetaDataSources(URI uri) {
        this(null,uri);
    }

    public URIFileMetaDataSources(ClassLoader classLoader, URI uri) {
        super(classLoader);
        currentModel = URIHelper.toURIModel( uri );
        read( currentModel.getUriSource() );
    }

    public URIFileMetaDataSources(List<URI> uriSources ) {
        this(null,uriSources);
    }

    public URIFileMetaDataSources(ClassLoader classLoader, List<URI> uriSources ) {
        super(classLoader);
        for ( URI uri : uriSources ) {
            currentModel = URIHelper.toURIModel( uri );
            read( currentModel.getUriSource() );
        }
    }

    /**
     * Loads all the classes specified in the Filename
     */
    @Override
    protected InputStream getInputStreamForFilename(String filename) throws MetaDataException {

        // LOAD THE FILE
        if (filename == null) {
            throw new NullPointerException("The MetaData file was null on URI: "+currentModel.toURI());
        }

        try {
            List<ClassLoader> classLoaders = Arrays.asList(
                    getClass().getClassLoader(),
                    getLoaderClassLoader(),
                    ClassLoader.getSystemClassLoader());

            return URIHelper.getInputStream( classLoaders, currentModel );
        }
        catch (IOException e) {
            throw new MetaDataException( "Could not open InputStream for URI: "+currentModel.toURI());
        }
    }
}
