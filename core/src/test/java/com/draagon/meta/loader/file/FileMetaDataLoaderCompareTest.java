package com.draagon.meta.loader.file;

import com.draagon.meta.loader.typed.config.TypesConfig;
import com.draagon.meta.loader.typed.config.TypeConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileMetaDataLoaderCompareTest extends FileMetaDataLoaderTestBase {

    protected FileMetaDataLoader loaderXml = null;
    protected FileMetaDataLoader loaderJson = null;

    @Before
    public void initLoader() {
        this.loaderXml = super.initLoader("xml");
        this.loaderJson = super.initLoader("json");
    }

    @After
    public void destroyLoader() {
        this.loaderXml.destroy();
        this.loaderJson.destroy();
    }

    @Test
    public void testMetaObjectsTypes() {

        // Compare the meta data configurations of metaobjects.types.xml and metaobjects.types.json
        TypesConfig c1 = loaderXml.getMetaDataConfig().getTypesConfig();
        TypesConfig c2 = loaderJson.getMetaDataConfig().getTypesConfig();

        Assert.assertEquals("Type Names", c1.getTypeNames(), c2.getTypeNames());

        for ( String n : c1.getTypeNames() ) {
            TypeConfig t1 = c1.getType( n );
            TypeConfig t2 = c2.getType( n );

            Assert.assertEquals( "Type["+n+"]" , t1.toString(), t2.toString());
            Assert.assertEquals( "Type["+n+"].typeChildConfigs" , t1.getTypeChildConfigs(), t2.getTypeChildConfigs());
            Assert.assertEquals( "TypeConfig", t1, t2);
        }

        Assert.assertEquals( "TypesConfig" , c1, c2);
    }
}
