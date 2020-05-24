package com.draagon.meta.generator.plantuml;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.plantuml.PlantUMLGenerator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        drawUML( args, null );
    }

    @Test
    public void testPlantUMLGenerator2() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "true" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-2.pu" );

        drawUML( args, Arrays.asList( new String[] {} ) );
    }

    @Test
    public void testPlantUMLGenerator3() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "false" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-3.pu" );

        drawUML( args, Arrays.asList( new String[] {} ) );
    }


    @Test
    public void testPlantUMLGenerator4() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "true" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-4.pu" );

        drawUML( args, Arrays.asList( new String[] {
                "!produce::v1::vegetable::*.[attr:_isAbstract]=true",
                "!produce::v1::container::ext::*"
        } ));
    }

    @Test
    public void testPlantUMLGenerator5() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "false" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-5.pu" );

        drawUML( args, Arrays.asList( new String[] {
                "produce::v1::fruit::*",
                "produce::v1::container::@"
        } ));
    }

    @Test
    public void testPlantUMLGenerator6() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "false" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-6.pu" );

        drawUML( args, Arrays.asList( new String[] {
                "produce::v1::fruit::*",
                "produce::v1::vegetable::*",
                "produce::v1::container::@"
        } ));
    }

    @Test
    public void testPlantUMLGenerator7() {

        Map<String,String> args = new HashMap<>();
        args.put( "showAttrs", "false" );
        args.put( "showAbstracts", "true" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "test-plantuml-7.pu" );

        drawUML( args, Arrays.asList( new String[] {
                "produce::v1::fruit::*",
                "produce::v1::vegetable::*",
                "produce::v1::container::@"
        } ));
    }

    protected void drawUML( Map<String,String> args, List<String> filters ) {

        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );

        Generator generator = new PlantUMLGenerator()
                .setArgs( args )
                .setFilters( filters );

        generator.execute( loader );

        // TODO: add tests
    }

}
