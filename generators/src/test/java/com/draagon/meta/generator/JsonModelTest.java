package com.draagon.meta.generator;

import com.draagon.meta.generator.direct.JsonModelGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonModelTest extends GeneratorTestBase {

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
                .setFilter( "*" );

        generator.execute( loader );

        // TODO: add tests
    }
}
