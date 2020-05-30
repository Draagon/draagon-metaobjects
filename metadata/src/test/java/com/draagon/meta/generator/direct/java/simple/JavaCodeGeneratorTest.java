package com.draagon.meta.generator.direct.java.simple;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.xsd.MetaDataXSDWriter;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JavaCodeGeneratorTest extends GeneratorTestBase {

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
        argMap.put(GeneratorBase.ARG_OUTPUTDIR, ROOT_DIR);
        argMap.put(JavaCodeGenerator.ARG_TYPE, JavaCodeGenerator.TYPE_INTERFACE);
        argMap.put(JavaCodeGenerator.ARG_PKGPREFIX, "com.draagon.meta.test1");
        argMap.put(JavaCodeGenerator.ARG_PKGSUFFIX, "domain");
        argMap.put(JavaCodeGenerator.ARG_NAMEPREFIX, "I");
        argMap.put(JavaCodeGenerator.ARG_NAMESUFFIX, "Domain");

        JavaCodeGenerator gen = new JavaCodeGenerator();
        gen.setArgs(argMap);

        gen.execute(loader);

        // TODO: Add tests
    }

    @Test
    public void testSimpleInterface2() throws IOException {

        Map<String,String> argMap = new HashMap<>();
        argMap.put(GeneratorBase.ARG_OUTPUTDIR, ROOT_DIR);
        argMap.put(JavaCodeGenerator.ARG_TYPE, JavaCodeGenerator.TYPE_INTERFACE);
        argMap.put(JavaCodeGenerator.ARG_PKGPREFIX, "com.draagon.meta.test2");
        //argMap.put(JavaCodeGenerator.ARG_PKGSUFFIX, "domain");
        //argMap.put(JavaCodeGenerator.ARG_NAMEPREFIX, "I");
        //argMap.put(JavaCodeGenerator.ARG_NAMESUFFIX, "Domain");

        JavaCodeGenerator gen = new JavaCodeGenerator();
        gen.setArgs(argMap);

        gen.execute(loader);

        // TODO: Add tests
    }
}
