package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.loader.types.TypesConfigParser;
import com.draagon.meta.loader.uri.URIHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class SimpleTypesParser extends TypesConfigParser<InputStream> {

    Log log = LogFactory.getLog(this.getClass());

    protected SimpleTypesParser( TypesConfigLoader loader, String sourceName) {
        super(loader, sourceName);
    }

    public void loadAndMerge( SimpleLoader simpleLoader, URI uri ) {

        InputStream is = null;
        try {
            List<ClassLoader> classLoaders = Arrays.asList(
                    getClass().getClassLoader(),
                    ClassLoader.getSystemClassLoader() );

            is = URIHelper.getInputStream( classLoaders, URIHelper.toURIModel( uri ));
            //intoLoader.getResourceInputStream(resource);
            loadAndMerge( simpleLoader.getTypesConfig(), is );
        }
        catch( IOException e ) {
            throw new MetaDataException( "Unable to load URI ["+uri+"]: " + e.getMessage(), e );
        }
        finally {
            try {
                if (is != null) is.close();
            } catch( IOException e ) {
                throw new MetaDataException( "Unable to close URI ["+uri+"]: " + e.getMessage(), e );
            }
        }
    }

    @Override
    public void loadAndMerge(TypesConfig intoConfig, InputStream is ) {

        TypesConfig loadedConfig = null;
        XMLObjectReader reader = null;
        IOException ioEx = null;

        // Read the TypesConfig
        try {
            reader = new XMLObjectReader( getLoader(), is );
            loadedConfig = (TypesConfig) reader.read( getLoader().getMetaObjectByName(TypesConfig.OBJECT_NAME));
        } catch (IOException e) {
            ioEx = e;
        }

        // Close the Reader
        try {
            reader.close();
        } catch (IOException ex) {
            if ( ioEx != null ) ioEx = ex;
        }

        if ( ioEx != null ) throw new MetaDataException( "Error loading typesConfig from "+
                "["+getSourcename()+"]: "+ ioEx.toString(), ioEx );

        // Merge the Loaded Types Config
        super.mergeTypesConfig( intoConfig, loadedConfig);
    }
}
