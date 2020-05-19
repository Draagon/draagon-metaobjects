package com.draagon.meta.io.xml;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.MetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.XMLUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;

public abstract class XMLMetaDataReader extends MetaDataReader {

    private final InputStream in;
    private Document doc = null;

    protected XMLMetaDataReader(MetaDataLoader loader, InputStream in ) {
        super(loader);
        this.in = in;
    }

    protected Document loadXML() throws IOException {
        // TODO: Add flag for validating XML
        doc = XMLUtil.loadFromStream(in, false);
        return doc;
    }

    @Override
    public void close() throws MetaDataIOException {
        /*if (in!=null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new MetaDataIOException(this, e.toString(), e);
            }
        }*/
    }
}
