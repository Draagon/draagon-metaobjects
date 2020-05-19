package com.draagon.meta.loader.types;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataAware;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.json.JsonObjectReader;
import com.draagon.meta.io.object.json.JsonObjectWriter;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.io.object.xml.XMLObjectWriter;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.loader.model.MetaModelLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
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

        TypesConfigLoader loader = TypesConfigLoader.create();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream( SimpleLoader.SIMPLE_TYPES_XML );
        MetaDataAware<MetaObject> tc1 = (MetaDataAware<MetaObject>) XMLObjectReader.readObject( TypesConfig.class,
                loader.getMetaObjectByName(TypesConfig.OBJECT_NAME), inputStream );

        roundTripTest(tc1, "test-config", TypesConfig.class);

        loader.destroy();
    }

    @Test
    public void testMetaDataLoader() throws IOException, MetaDataIOException {

        MetaModelLoader loader = MetaModelLoader.create( "test-model-roundtrip");

        final String TEST_METADATA_XML = "com/draagon/meta/loader/simple/fruitbasket-metadata.xml";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream( TEST_METADATA_XML );
        MetaDataAware<MetaObject> metadata = (MetaDataAware<MetaObject>) XMLObjectReader.readObject( MetaModel.class,
                loader.getMetaObjectByName(MetaModel.OBJECT_NAME), inputStream );

        roundTripTest(metadata, loader.getName(), TypesConfig.class);

        loader.destroy();
    }

    @Test
    public void testSimpleLoader() throws IOException, MetaDataIOException {

        MetaModelLoader loader = MetaModelLoader.create( "test-simple-roundtrip");

        final String TEST_METADATA_XML = "com/draagon/meta/loader/simple/fruitbasket-metadata.xml";

        SimpleLoader simpleLoader = SimpleLoader.createManual( loader.getName(), TEST_METADATA_XML );

        MetaModel metadata = buildModelFromLoader(
                loader.getMetaObjectByName( MetaModel.OBJECT_NAME ), simpleLoader );

        roundTripTest(metadata, loader.getName(), TypesConfig.class);

        loader.destroy();
    }

    public static MetaModel buildModelFromLoader(MetaObject modelMetaObject, SimpleLoader simpleLoader) {

        MetaModel m = (MetaModel) modelMetaObject.newInstance();
        populateModel( m, simpleLoader, true );

        return m;
    }

    private static void populateModel(MetaModel m, MetaData<?> md, boolean isRoot) {

        if (isRoot) {
            m.setString( MetaModel.FIELD_TYPE, MetaModel.OBJECT_NAME);
        }
        else {
            m.setString( MetaModel.FIELD_PACKAGE, _trimToNull( md.getPackage() ));
            m.setString( MetaModel.FIELD_TYPE, md.getTypeName() );
            m.setString( MetaModel.FIELD_SUBTYPE, md.getSubTypeName() );
            m.setString( MetaModel.FIELD_NAME, md.getShortName() );

            if ( md.getSuperData() != null ) {
                String name = md.getSuperData().getName();
                if ( md.getPackage().equals( md.getSuperData().getPackage())) name = md.getSuperData().getShortName();
                m.setString(MetaModel.FIELD_SUPER, name );
            }
        }

        for ( MetaData child : md.getChildren() ) {
            MetaModel cm = (MetaModel) m.getMetaData().newInstance();

            populateModel( cm, child, false );

            List<MetaModel> mkids = m.getObjectArray( MetaModel.class, MetaModel.FIELD_CHILDREN);
            if (mkids == null)  {
                mkids = new ArrayList<>();
                m.setObjectArray( MetaModel.FIELD_CHILDREN, mkids );
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
