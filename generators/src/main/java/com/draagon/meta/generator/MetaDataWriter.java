package com.draagon.meta.generator;

import com.draagon.meta.loader.MetaDataLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class MetaDataWriter<T extends MetaDataWriter> {

    protected Log log = LogFactory.getLog( this.getClass() );

    private final MetaDataLoader loader;
    private MetaDataFilters filters;
    private String name;
    private String filename;

    protected MetaDataWriter(MetaDataLoader loader) {
        this.loader = loader;
    }

    public MetaDataLoader getLoader() {
        return loader;
    }

    /////////////////////////////////////////////////////////////////////////
    // Options Pattern

    public T withName( String name ) {
        this.name = name;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public T withFilters( MetaDataFilters filters ) {
        this.filters = filters;
        return (T) this;
    }

    public MetaDataFilters getFilters() {
        return filters;
    }

    public T withFilename( String filename ) {
        this.filename = filename;
        return (T) this;
    }

    public String getFilename() {
        return filename;
    }

    /////////////////////////////////////////////////////////////////////////
    // MetaDataWriter methods

    public abstract void close() throws MetaDataWriterException;


    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    protected String getToStringOptions() {
        return "loader="+loader.getShortName()
                + (name != null ? ",name="+name : "")
                + (filename != null ? ",filename="+filename : "")
                + (filters != null ? ",filters="+filters : ",filters=no");
    }

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName() + "{"+getToStringOptions()+"}";
    }
}
