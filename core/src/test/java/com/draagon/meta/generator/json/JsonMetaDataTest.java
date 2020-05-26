package com.draagon.meta.generator.json;

import com.draagon.meta.MetaData;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.model.JsonMetaDataGenerator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalMetaDataSources;
import com.draagon.meta.loader.file.FileLoaderOptions;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonMetaDataTest extends GeneratorTestBase {


    protected FileMetaDataLoader loader = null;

    @Before
    public void initLoader() { this.loader = super.initLoader("xml");}

    @After
    public void destroyLoader() { this.loader.destroy(); }


    protected String getGeneratedTestSourcesPath() {
        // TODO:  Get this from Maven
        return "./target/generated-test-resources";
    }

    @Test
    public void testJsonGenerator1() {

        final String TESTFILE = "test1-metadata.json";

        Map<String,String> args = new HashMap<>();
        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test1-metadata.json" );

        Generator generator = new JsonMetaDataGenerator()
                .setArgs( args )
                .setFilters(Arrays.asList( new String[] {"*"} ));

        generator.execute( loader );

        // TODO: add tests
        FileMetaDataLoader loader2 = new FileMetaDataLoader(
                new FileLoaderOptions()
                        .addParser("*.xml", XMLMetaDataParser.class)
                        .addParser("*.json", JsonMetaDataParser.class)
                        .addSources(new LocalMetaDataSources(
                                "com/draagon/meta/loader/json/metaobjects.types.json"))
                        .addSources(new LocalMetaDataSources(
                                getGeneratedTestSourcesPath(),
                                TESTFILE)
                        )
                        .setShouldRegister(true)
                        // NOTE: Keep false because test data has fruite overlay to add farmName onto metadata root
                        .setStrict(false)
                        .setVerbose(false),
                TESTFILE)
                .init();

        for( MetaData md: loader.getChildren() ) {
            MetaData md2 = loader.getMetaDataByName( MetaData.class, md.getName() );
            Assert.assertEquals( md.getName(), md, md2);
            Assert.assertTrue( md.getName(), md.equals(md2));
            //System.out.println( "MATCH: " +md+ " == " + md2 );
        }
    }
}
