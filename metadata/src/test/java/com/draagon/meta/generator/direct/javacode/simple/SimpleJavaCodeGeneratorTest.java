package com.draagon.meta.generator.direct.javacode.simple;

import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SimpleJavaCodeGeneratorTest extends GeneratorTestBase {

    public final static String ROOT_DIR = "./target/tests/javacode";
    protected SimpleLoader loader = null;

    @Before
    public void setup() {
        loader = initLoader(Arrays.asList( URIHelper.toURI(
                "model:resource:com/draagon/meta/generator/direct/javacode/simple/test-interface-metadata.xml" )
        ));
    }

    @Test
    public void testSimpleInterface1() throws IOException {

        Map<String,String> argMap = new HashMap<>();
        argMap.put(SimpleJavaCodeGenerator.ARG_OUTPUTDIR, ROOT_DIR);
        argMap.put(SimpleJavaCodeGenerator.ARG_OUTPUTFILENAME, "test1-overlay-model.xml");
        argMap.put(SimpleJavaCodeGenerator.ARG_TYPE, SimpleJavaCodeGenerator.TYPE_INTERFACE);
        argMap.put(SimpleJavaCodeGenerator.ARG_PKGPREFIX, "com.draagon.meta.test1");
        argMap.put(SimpleJavaCodeGenerator.ARG_PKGSUFFIX, "domain");
        argMap.put(SimpleJavaCodeGenerator.ARG_NAMEPREFIX, "I");
        argMap.put(SimpleJavaCodeGenerator.ARG_NAMESUFFIX, "Domain");

        SimpleJavaCodeGenerator gen = new SimpleJavaCodeGenerator();
        gen.setArgs(argMap);

        gen.execute(loader);

        // TODO: Add tests
    }

    @Test
    public void testSimpleInterface2() throws IOException {

        Map<String,String> argMap = new HashMap<>();
        argMap.put(SimpleJavaCodeGenerator.ARG_OUTPUTDIR, ROOT_DIR);
        argMap.put(SimpleJavaCodeGenerator.ARG_OUTPUTFILENAME, "test2-overlay-model.xml");
        argMap.put(SimpleJavaCodeGenerator.ARG_TYPE, SimpleJavaCodeGenerator.TYPE_INTERFACE);
        argMap.put(SimpleJavaCodeGenerator.ARG_PKGPREFIX, "com.draagon.meta.test2");
        //argMap.put(JavaCodeGenerator.ARG_PKGSUFFIX, "domain");
        //argMap.put(JavaCodeGenerator.ARG_NAMEPREFIX, "I");
        //argMap.put(JavaCodeGenerator.ARG_NAMESUFFIX, "Domain");

        SimpleJavaCodeGenerator gen = new SimpleJavaCodeGenerator();
        gen.setArgs(argMap);

        gen.execute(loader);

        // TODO: Add tests
    }
}
