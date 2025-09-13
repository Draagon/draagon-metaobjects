package com.draagon.meta.loader.simple.xml;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.loader.types.TypesConfigParser;
import com.draagon.meta.loader.uri.URIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class SimpleTypesParserXML extends TypesConfigParser<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(SimpleTypesParserXML.class);

    protected SimpleTypesParserXML( TypesConfigLoader loader, ClassLoader classLoader, String sourceName) {
        super(loader, classLoader, sourceName);
    }

    public void loadAndMerge( SimpleLoaderXML simpleLoader, URI uri ) {

        InputStream is = null;
        try {
            List<ClassLoader> classLoaders = Arrays.asList(
                    getClassLoader(),
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
