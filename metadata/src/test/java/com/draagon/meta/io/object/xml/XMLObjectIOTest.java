package com.draagon.meta.io.object.xml;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.ObjectIOTestBase;
import com.draagon.meta.object.value.ValueObject;
import org.junit.Assert;

import java.io.*;

public class XMLObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "xml-valueobject-io-test-";

    protected void runTest(ValueObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".xml";

        writeXML(filename, o);
        ValueObject o2 = readXML(filename, o.getMetaData());

        Assert.assertEquals(name+"-xml", o, o2);
    }
}
