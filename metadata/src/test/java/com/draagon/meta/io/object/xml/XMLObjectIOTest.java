package com.draagon.meta.io.object.xml;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.ObjectIOTestBase;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.mapped.MappedObject;
import org.junit.Assert;

import java.io.*;

public class XMLObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "xml-valueobject-io-test-";

    @Override
    protected void runTest(MappedObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".xml";

        writeXML(filename, o);
        Object o2 = readXML(filename, o.getMetaData());

        Assert.assertEquals(name+"-xml", o, o2);
    }
}
