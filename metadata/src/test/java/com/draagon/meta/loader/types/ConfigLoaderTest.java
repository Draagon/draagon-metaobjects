package com.draagon.meta.loader.types;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataAware;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.json.JsonObjectReader;
import com.draagon.meta.io.object.json.JsonObjectWriter;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.io.object.xml.XMLObjectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.simple.xml.SimpleLoaderXML;
import com.draagon.meta.object.MetaObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoaderTest {

    public final static String PREFIX = "./target/tests/config/";

    @Test
    public void testConfigLoader() throws IOException, MetaDataIOException {

        TypesConfigLoader loader = TypesConfigLoader.create(getClass().getClassLoader());

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream( SimpleLoader.SIMPLE_TYPES_JSON );
        MetaDataAware<MetaObject> tc1 = (MetaDataAware<MetaObject>) JsonObjectReader.readObject( TypesConfig.class,
                loader.getMetaObjectByName(TypesConfig.OBJECT_NAME), new InputStreamReader( inputStream ));

        roundTripTest(tc1, "test-config", TypesConfig.class);

        loader.destroy();
    }

    @Test
    public void testConfigLoaderXML() throws IOException, MetaDataIOException {

        TypesConfigLoader loader = TypesConfigLoader.create(getClass().getClassLoader());

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream( SimpleLoaderXML.SIMPLE_TYPES_XML );
        MetaDataAware<MetaObject> tc1 = (MetaDataAware<MetaObject>) XMLObjectReader.readObject( TypesConfig.class,
                loader.getMetaObjectByName(TypesConfig.OBJECT_NAME), inputStream );

        roundTripTest(tc1, "test-config-xml", TypesConfig.class);

        loader.destroy();
    }

    @Test
    public void testMetaDataLoader() throws IOException, MetaDataIOException {

        MetaModelLoader loader = MetaModelLoader.create( getClass().getClassLoader(), "test-model-roundtrip");

        final String TEST_METADATA_XML = "com/draagon/meta/loader/simple/fruitbasket-metadata.xml";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream( TEST_METADATA_XML );
        MetaDataAware<MetaObject> metadata = (MetaDataAware<MetaObject>) XMLObjectReader.readObject( MetaModel.class,
                loader.getMetaObjectByName(MetaModel.OBJECT_NAME), inputStream );

        roundTripTest(metadata, loader.getName(), TypesConfig.class);

        loader.destroy();
    }

    @Test
    public void testSimpleLoader() throws IOException, MetaDataIOException {

        MetaModelLoader loader = MetaModelLoader.create( getClass().getClassLoader(), "test-simple-roundtrip");

        final String TEST_METADATA_JSON = "com/draagon/meta/loader/simple/fruitbasket-metadata.json";

        SimpleLoader simpleLoader = SimpleLoader.createManual( loader.getName(), TEST_METADATA_JSON );

        MetaModel metadata = buildModelFromLoader(
                loader.getMetaObjectByName( MetaModel.OBJECT_NAME), simpleLoader );

        roundTripTest(metadata, loader.getName(), TypesConfig.class);

        loader.destroy();
    }

    
    @Test
    public void testSimpleLoaderXML() throws IOException, MetaDataIOException {

        MetaModelLoader loader = MetaModelLoader.create( getClass().getClassLoader(), "test-simple-xml-roundtrip");

        final String TEST_METADATA_XML = "com/draagon/meta/loader/simple/fruitbasket-metadata.xml";

        SimpleLoaderXML simpleLoader = SimpleLoaderXML.createManual( loader.getName(), TEST_METADATA_XML );

        MetaModel metadata = buildModelFromLoader(
                loader.getMetaObjectByName( MetaModel.OBJECT_NAME), simpleLoader );

        roundTripTest(metadata, loader.getName(), TypesConfig.class);

        loader.destroy();
    }

    public static MetaModel buildModelFromLoader(MetaObject modelMetaObject, MetaDataLoader loader) {

        MetaModel m = (MetaModel) modelMetaObject.newInstance();
        populateModel( m, loader, true );

        return m;
    }

    private static void populateModel(MetaModel m, MetaData md, boolean isRoot) {

        if (isRoot) {
            m.setType(MetaModel.OBJECT_NAME);
        }
        else {
            m.setPackage( _trimToNull( md.getPackage() ));
            m.setType( md.getTypeName() );
            m.setSubType( md.getSubTypeName() );
            m.setName( md.getShortName() );

            if ( md.getSuperData() != null ) {
                String name = md.getSuperData().getName();
                if ( md.getPackage().equals( md.getSuperData().getPackage())) name = md.getSuperData().getShortName();
                m.setSuper( name );
            }
        }

        for ( MetaData child : md.getChildren() ) {
            MetaModel cm = (MetaModel) m.getMetaData().newInstance();

            populateModel( cm, child, false );

            List<MetaModel> mkids = m.getChildren();
            if (mkids == null)  {
                mkids = new ArrayList<>();
                m.setChildren( mkids );
            }
            mkids.add(cm);
        }
    }

    private static String _trimToNull(String s) {
        if ( s != null && s.trim().isEmpty() ) return null;
        return s;
    }

    public void roundTripTest(MetaDataAware<MetaObject> tc1, String filename,
                              Class<? extends MetaDataAware<MetaObject>> clazz) throws IOException, MetaDataIOException {

        MetaObject mo = tc1.getMetaData();

        File d = new File( PREFIX );
        if ( !d.exists() ) d.mkdirs();

        File f = new File( d, filename+".xml" );
        if ( !f.exists()) f.createNewFile();

        XMLObjectWriter.writeObject( tc1, new FileOutputStream( f ));
        MetaDataAware<MetaObject> tc2 = XMLObjectReader.readObject( clazz, mo, new FileInputStream( f ));

        Assert.assertEquals( tc1, tc2 );

        f = new File( d, filename+".json" );
        if ( !f.exists()) f.createNewFile();

        JsonObjectWriter.writeObject( tc2, new FileWriter( f ));
        MetaDataAware<MetaObject> tc3 = JsonObjectReader.readObject( clazz, mo, new FileReader( f ));

        Assert.assertEquals( tc2, tc3 );
    }
}
