package com.draagon.meta.generator;

import com.draagon.meta.generator.direct.JsonModelGenerator;
import com.draagon.meta.generator.direct.PlantUMLGenerator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PlantUMLTest extends GeneratorTestBase {


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
    public void testPlantUMLGenerator1() {

        Map<String,String> args = new HashMap<>();
        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test1-metadata.pu" );

        Generator generator = new PlantUMLGenerator()
                .setArgs( args )
                .setFilter( "*" );

        generator.execute( loader );

        // TODO: add tests
    }
}
