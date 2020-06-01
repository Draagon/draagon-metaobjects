package com.draagon.meta.io.object.json;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.ObjectIOTestBase;
import com.draagon.meta.object.mapped.MappedObject;
import org.junit.Assert;

import java.io.*;

public class JsonObjectIOTest extends ObjectIOTestBase {

    private final static String PRE = "json-valueobject-io-test-";

    @Override
    protected void runTest(MappedObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".json";

        writeJson(filename, o);
        Object o2 = readJson(filename,o.getMetaData());

        Assert.assertEquals(name+"-json", o, o2);
    }
}
