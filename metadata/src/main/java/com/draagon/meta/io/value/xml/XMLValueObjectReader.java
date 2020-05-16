package com.draagon.meta.io.value.xml;

import com.draagon.meta.io.MetaDataReader;
import com.draagon.meta.io.xml.XMLMetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.value.ValueObject;

import java.io.InputStream;

public class XMLValueObjectReader extends XMLMetaDataReader {

    public XMLValueObjectReader(MetaDataLoader loader, InputStream is ) {
        super(loader, is);
    }

    public ValueObject read() {
        return read(null);
    }

    public ValueObject read( MetaObject mo ) {
        return null;
    }
}
