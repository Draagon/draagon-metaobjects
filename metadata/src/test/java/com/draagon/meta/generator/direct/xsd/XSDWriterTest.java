package com.draagon.meta.generator.direct.xsd;

import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.metadata.xsd.MetaDataXSDWriter;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.loader.uri.URIHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class XSDWriterTest extends GeneratorTestBase {

    public final static String ROOT_DIR = "./src/test/resources/com/draagon/meta/loader/simple";
    protected SimpleLoader loader = null;

    @Before
    public void setup() {
        loader = initLoader(Arrays.asList(
                URIHelper.toURI( "model:resource:com/draagon/meta/loader/simple/fruitbasket-metadata.xml" )
        ));
    }

    @Test
    public void testSimpleTypesXSD() throws IOException {

        File f = new File(ROOT_DIR);
        if (!f.exists()) f.mkdirs();
        File ff = new File( f, "simple-model.xsd");

        MetaDataXSDWriter writer = new MetaDataXSDWriter( loader, new FileOutputStream(ff));
        writer.withNamespace("https://draagon.com/schema/metamodel/simple");
        writer.writeXML();
        writer.close();
    }
}
