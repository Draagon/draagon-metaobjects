package com.metaobjects.generator.plantuml;

import com.metaobjects.generator.Generator;
import com.metaobjects.generator.GeneratorBase;
import com.metaobjects.generator.GeneratorTestBase;
import com.metaobjects.generator.direct.plantuml.PlantUMLGenerator;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.loader.uri.URIHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantUMLTest extends GeneratorTestBase {


    protected SimpleLoader loader = null;

    @Before
    public void initLoader() { 
        this.loader = super.initLoader(Arrays.asList(
            URIHelper.toURI("model:resource:com/metaobjects/loader/simple/fruitbasket-metadata.json")
        ));
    }

    @After
    public void destroyLoader() { this.loader.destroy(); }

    
    protected String getGeneratedTestSourcesPath() {
        // Maven target directory
        return "./target/generated-test-resources";
    }

    @Test
    public void testPlantUMLGenerator1() {
        Generator generator = PlantUMLGenerator.builder()
                .showAttrs(true)
                .showAbstracts(true)
                .outputFilename("test-plantuml-1.pu")
                .outputDir(getGeneratedTestSourcesPath())
                .build();

        generator.execute(loader);

        // Verify the output file was generated
        verifyOutputFile("test-plantuml-1.pu");
    }

    @Test
    public void testPlantUMLGenerator2() {
        Generator generator = PlantUMLGenerator.builder()
                .showAttrs(false)
                .showAbstracts(true)
                .outputFilename("test-plantuml-2.pu")
                .outputDir(getGeneratedTestSourcesPath())
                .withFilters(Arrays.asList(new String[] {}))
                .build();

        generator.execute(loader);
        verifyOutputFile("test-plantuml-2.pu");
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
        Generator generator = PlantUMLGenerator.builder()
                .showAttrs(false)
                .showAbstracts(false)
                .outputFilename("test-plantuml-5.pu")
                .outputDir(getGeneratedTestSourcesPath())
                .withFilters(Arrays.asList(new String[] {
                        "produce::v1::fruit::*",
                        "produce::v1::container::@"
                }))
                .build();

        generator.execute(loader);
        verifyOutputFile("test-plantuml-5.pu");
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
        args.put( "showFields", "true" );
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

        // Verify the output file was generated
        String outputPath = getGeneratedTestSourcesPath();
        String filename = args.get(GeneratorBase.ARG_OUTPUTFILENAME);
        if (filename != null) {
            java.io.File outputFile = new java.io.File(outputPath, filename);
            assertTrue("Generated PlantUML file should exist: " + outputFile.getAbsolutePath(), 
                      outputFile.exists());
            assertTrue("Generated PlantUML file should not be empty", 
                      outputFile.length() > 0);
        }
    }

    /**
     * Helper method to verify that a PlantUML output file was generated correctly
     * @param filename the output filename to verify
     */
    protected void verifyOutputFile(String filename) {
        String outputPath = getGeneratedTestSourcesPath();
        java.io.File outputFile = new java.io.File(outputPath, filename);
        assertTrue("Generated PlantUML file should exist: " + outputFile.getAbsolutePath(), 
                  outputFile.exists());
        assertTrue("Generated PlantUML file should not be empty", 
                  outputFile.length() > 0);
    }

}
