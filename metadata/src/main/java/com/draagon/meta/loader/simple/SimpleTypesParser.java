package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypesConfigLoader;
import com.draagon.meta.loader.config.TypesConfigParser;

import java.io.IOException;
import java.io.InputStream;

public class SimpleTypesParser extends TypesConfigParser<InputStream> {

    protected SimpleTypesParser( TypesConfigLoader loader, String sourceName) {
        super(loader, sourceName);
    }

    public void loadAndMerge( SimpleLoader simpleLoader, String resource ) {

        InputStream is = null;

        try {
            is = simpleLoader.getResourceInputStream( resource);
            loadAndMerge( simpleLoader.getTypesConfig(), is );
        }
        catch( IOException e ) {
            throw new MetaDataException( "Unable to load typesConfig from resource ["+resource+"]: " + e.getMessage(), e );
        }
    }

    @Override
    public void loadAndMerge(TypesConfig intoConfig, InputStream is ) {

        TypesConfig loadedConfig = null;
        XMLObjectReader reader = null;
        MetaDataIOException ioEx = null;

        // Read the TypesConfig
        try {
            reader = new XMLObjectReader( getLoader(), is );
            loadedConfig = (TypesConfig) reader.read( getLoader().getMetaObjectByName(TypesConfig.OBJECT_NAME));
        } catch (MetaDataIOException e) {
            ioEx = e;
        }

        // Close the Reader
        try {
            reader.close();
        } catch (MetaDataIOException ex) {
            if ( ioEx != null ) ioEx = ex;
        }

        if ( ioEx != null ) throw new MetaDataException( "Error loading typesConfig from "+
                "["+getSourcename()+"]: "+ ioEx.toString(), ioEx );

        // Merge the Loaded Types Config
        super.mergeTypesConfig( intoConfig, loadedConfig);
    }
}
