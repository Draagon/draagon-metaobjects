package com.metaobjects.io;

import com.metaobjects.io.util.PathTracker;
import com.metaobjects.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetaDataReader implements MetaDataIO {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MetaDataReader.class);

    private final MetaDataLoader loader;
    private PathTracker path = new PathTracker();

    protected MetaDataReader(MetaDataLoader loader) {
        this.loader = loader;
    }

    public MetaDataLoader getLoader() {
        return loader;
    }

    //public abstract D read() throws MetaDataIOException;

    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    protected String getToStringOptions() {
        return "loader="+loader.getShortName();
    }

    public PathTracker path() {
        return path;
    }

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName() + "{"+getToStringOptions()+"}";
    }
}
