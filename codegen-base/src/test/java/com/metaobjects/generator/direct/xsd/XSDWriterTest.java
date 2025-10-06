package com.metaobjects.generator.direct.xsd;

import com.metaobjects.generator.GeneratorTestBase;
import com.metaobjects.loader.simple.SimpleLoader;
import com.metaobjects.loader.uri.URIHelper;
import org.junit.Before;
import java.util.Arrays;

public class XSDWriterTest extends GeneratorTestBase {

    public final static String ROOT_DIR = "./src/test/resources/com/metaobjects/loader/simple";
    protected SimpleLoader loader = null;

    /*@Before
    public void setup() {
        loader = initLoader(Arrays.asList(
                URIHelper.toURI( "model:resource:com/metaobjects/loader/simple/fruitbasket-metadata.json" )
        ));
    }

    @Test
    public void testSimpleTypesXSD() throws IOException {

        File f = new File(ROOT_DIR);
        if (!f.exists()) f.mkdirs();
        File ff = new File( f, "simple-model.xsd");

        MetaDataXSDWriter writer = new MetaDataXSDWriter( loader, new FileOutputStream(ff));
        writer.withNamespace("https://metaobjects.com/schema/metamodel/simple");
        writer.writeXML();
        writer.close();
    }*/
}
