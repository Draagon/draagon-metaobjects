package com.metaobjects.io;

import com.metaobjects.io.util.PathTracker;
import com.metaobjects.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetaDataWriter implements MetaDataIO {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MetaDataWriter.class);

    private final MetaDataLoader loader;
    private final PathTracker path = new PathTracker();

    protected MetaDataWriter(MetaDataLoader loader) {
        this.loader = loader;
    }

    public MetaDataLoader getLoader() {
        return loader;
    }

    public PathTracker path() {
        return path;
    }

    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    protected String getToStringOptions() {
        return "loader="+loader.getShortName();
    }

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName() + "{"+getToStringOptions()+"}";
    }
}
