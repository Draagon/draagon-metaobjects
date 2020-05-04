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
        args.put( "showAttrs", "true" );
        args.put( "showAbstracts", "true" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-1.pu" );

        drawUML( args, "*" );
    }

    @Test
    public void testPlantUMLGenerator2() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "true" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-2.pu" );

        drawUML( args, "*" );
    }

    @Test
    public void testPlantUMLGenerator3() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "false" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-3.pu" );

        drawUML( args, "*" );
    }

    protected void drawUML( Map<String,String> args, String filter ) {

        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );

        Generator generator = new PlantUMLGenerator()
                .setArgs( args )
                .setFilter( filter );

        generator.execute( loader );

        // TODO: add tests
    }

}
