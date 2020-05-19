package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.loader.model.MetaDataModel;
import com.draagon.meta.loader.model.MetaDataModelLoader;
import com.draagon.meta.loader.model.MetaDataModelParser;

import java.io.IOException;
import java.io.InputStream;

public class SimpleModelParser extends MetaDataModelParser<SimpleLoader,String> {

    protected SimpleModelParser(MetaDataModelLoader modelLoader, String sourceName) {
        super(modelLoader, sourceName);
    }

    @Override
    public void loadAndMerge( SimpleLoader intoLoader, String resource) {

        try {
            InputStream is = intoLoader.getResourceInputStream(resource);
            loadAndMergeFromStream( intoLoader, is );
        }
        catch( IOException e ) {
            throw new MetaDataException( "Unable to load typesConfig from resource ["+resource+"]: " + e.getMessage(), e );
        }
    }

    /* Load MetaDataModel Stream */
    public void loadAndMergeFromStream( SimpleLoader intoLoader, InputStream in ) {

        IOException ioEx = null;

        MetaDataModel metadata = null;
        XMLObjectReader reader = null;

        try {
            reader = new XMLObjectReader( getLoader(), in );
            metadata = (MetaDataModel) reader.read( getLoader().getMetaObjectByName(MetaDataModel.OBJECT_NAME));
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
