package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.io.object.json.JsonObjectReader;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.model.MetaModelParser;
import com.draagon.meta.loader.uri.URIHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * v6.0.0: Updated to use service-based MetaDataTypeRegistry instead of TypesConfig
 */
public class SimpleModelParser extends MetaModelParser<SimpleLoader,URI> {

    protected SimpleModelParser(MetaModelLoader modelLoader, ClassLoader classLoader, String sourceName) {
        super(modelLoader, classLoader, sourceName);
    }

    @Override
    public void loadAndMerge( SimpleLoader intoLoader, URI uri) {

        InputStream is = null;
        try {
            is = URIHelper.getInputStream( uri );
            //intoLoader.getResourceInputStream(resource);
            loadAndMergeFromStream( intoLoader, is );
        }
        catch( IOException e ) {
            throw new MetaDataException( "Unable to load URI ["+uri+"]: " + e.getMessage(), e );
        }
        finally {
            try {
                if ( is != null ) is.close();
            } catch( IOException e ) {
                throw new MetaDataException( "Unable to close URI ["+uri+"]: " + e.getMessage(), e );
            }
        }
    }

    /* Load MetaDataModel Stream */
    public void loadAndMergeFromStream( SimpleLoader intoLoader, InputStream in ) {

        IOException ioEx = null;

        MetaModel metadata = null;
        JsonObjectReader reader = null;

        try {
            reader = new JsonObjectReader( getLoader(), new InputStreamReader( in ));
            metadata = (MetaModel) reader.read( getLoader().getMetaObjectByName(MetaModel.OBJECT_NAME));
        } catch (IOException e) {
            ioEx = e;
        }

        try {
            reader.close();
        } catch (IOException ex) {
            if ( ioEx != null ) ioEx = ex;
        }
        if ( ioEx != null ) throw new MetaDataException( "Error loading MetaData from "+
                "["+getSourcename()+"]: "+ ioEx.toString(), ioEx );

        // Parse MetaData and Construct MetaData types
        mergeMetaDataModel( intoLoader, metadata );
    }
}
