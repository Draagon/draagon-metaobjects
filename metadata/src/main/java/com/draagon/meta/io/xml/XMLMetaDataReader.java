package com.draagon.meta.io.xml;

import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.MetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.IOException;
import java.io.InputStream;

public abstract class XMLMetaDataReader extends MetaDataReader {

    private final InputStream in;

    protected XMLMetaDataReader(MetaDataLoader loader, InputStream in ) {
        super(loader);
        this.in = in;
    }

    @Override
    public void close() throws MetaDataIOException {
        if (in!=null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new MetaDataIOException(this, e.toString(), e);
            }
        }
    }
}
