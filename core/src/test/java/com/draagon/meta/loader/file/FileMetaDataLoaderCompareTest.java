package com.draagon.meta.loader.file;

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
        Assert.assertEquals( loaderXml.getMetaDataConfig(), loaderJson.getMetaDataConfig() );
    }
}
