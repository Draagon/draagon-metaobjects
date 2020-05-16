package com.draagon.meta.io.value.xml;

import com.draagon.meta.field.*;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.value.ValueObjectIOTestBase;
import com.draagon.meta.io.value.json.JsonValueObjectReader;
import com.draagon.meta.io.value.json.JsonValueObjectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.relation.ref.ObjectReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XMLValueObjectIOTest  extends ValueObjectIOTestBase {

    private final static String PRE = "xml-valueobject-io-test-";

    protected void runTest(ValueObject o, String name) throws IOException, MetaDataIOException {

        String filename = PRE+name+".xml";

        writeXML(filename, o);
        ValueObject o2 = readXML(filename);

        //Assert.assertEquals(name+"-xml", o, o2);
    }
}
