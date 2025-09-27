package com.metaobjects.io;

import com.metaobjects.io.util.PathTracker;
import com.metaobjects.loader.MetaDataLoader;

import java.io.IOException;

public interface MetaDataIO {

    public MetaDataLoader getLoader();

    public PathTracker path();

    public void close() throws IOException;
}
