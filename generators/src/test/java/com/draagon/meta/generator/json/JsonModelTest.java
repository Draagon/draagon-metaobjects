package com.draagon.meta.generator.json;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.json.model.JsonModelGenerator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsonModelTest extends GeneratorTestBase {


    protected FileMetaDataLoader loader = null;

    @Before
    public void initLoader() { this.loader = super.initLoader("xml");}

    @After
    public void destroyLoader() { this.loader.destroy(); }


    protected String getGeneratedTestSourcesPath() {
        // TODO:  Get this from Maven
        return "./target/generated-test-sources";
    }

    @Test
    public void testJsonGenerator1() {

        Map<String,String> args = new HashMap<>();
        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test1-metadata.json" );

        Generator generator = new JsonModelGenerator()
                .setArgs( args )
                .setFilters(Arrays.asList( new String[] {"*"} ));

        generator.execute( loader );

        // TODO: add tests
    }
}
