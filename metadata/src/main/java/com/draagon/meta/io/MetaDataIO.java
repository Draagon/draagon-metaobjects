package com.draagon.meta.io;

import com.draagon.meta.io.util.PathTracker;
import com.draagon.meta.loader.MetaDataLoader;

public interface MetaDataIO {

    public MetaDataLoader getLoader();

    public PathTracker path();

    public void close() throws MetaDataIOException;
}
