package com.draagon.meta.io;

import com.draagon.meta.loader.MetaDataLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MetaDataReader implements MetaDataIO {

    protected Log log = LogFactory.getLog( this.getClass() );

    private final MetaDataLoader loader;

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

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName() + "{"+getToStringOptions()+"}";
    }
}
