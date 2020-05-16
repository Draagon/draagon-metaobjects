package com.draagon.meta.io;

import com.draagon.meta.loader.MetaDataLoader;

public interface MetaDataIO {

    public MetaDataLoader getLoader();

    public void close() throws MetaDataIOException;
}
