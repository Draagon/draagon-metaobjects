package com.metaobjects.io.object.xml;

import com.metaobjects.io.MetaDataIOException;
import com.metaobjects.io.object.ObjectIOTestBase;
import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.object.mapped.MappedObject;
import org.junit.Assert;

import java.io.*;

public class XMLObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "xml-valueobject-io-test-";

    @Override
    protected void runTest(MappedObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".xml";

        writeJson(filename, o);
        Object o2 = readJson(filename, o.getMetaData());

        Assert.assertEquals(name+"-xml", o, o2);
    }
}
