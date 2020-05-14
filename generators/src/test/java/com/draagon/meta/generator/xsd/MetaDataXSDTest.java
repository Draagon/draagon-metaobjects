package com.draagon.meta.generator.xsd;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorTestBase;
import com.draagon.meta.generator.direct.xml.xsd.MetaDataXSDGenerator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaDataXSDTest extends GeneratorTestBase {

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
    public void testXSDGenerator1() {

        Map<String,String> args = new HashMap<>();
        args.put( "nameSpace", "https://www.draagon.com/metadata/test1" );
        args.put( GeneratorBase.ARG_OUTPUTFILENAME, "metadata-test1.xsd" );

        writeXSD( args, null );
    }

    protected void writeXSD( Map<String,String> args, List<String> filters ) {

        args.put( GeneratorBase.ARG_OUTPUTDIR, getGeneratedTestSourcesPath() );

        Generator generator = new MetaDataXSDGenerator()
                .setArgs( args )
                .setFilters( filters );

        generator.execute( loader );

        // TODO: add tests
    }

}
