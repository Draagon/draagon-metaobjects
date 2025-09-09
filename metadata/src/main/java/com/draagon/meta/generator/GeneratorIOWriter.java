package com.draagon.meta.generator;

import com.draagon.meta.io.MetaDataWriter;
import com.draagon.meta.loader.MetaDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GeneratorIOWriter<T extends GeneratorIOWriter> extends MetaDataWriter {

    protected static final Logger log = LoggerFactory.getLogger(GeneratorIOWriter.class);

    private MetaDataFilters filters;
    private String name;
    private String filename;

    protected GeneratorIOWriter(MetaDataLoader loader) {
        super(loader);
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

    public abstract void close() throws GeneratorIOException;


    /////////////////////////////////////////////////////////////////////////
    // Misc Methods

    protected String getToStringOptions() {
        return "loader="+getLoader().getShortName()
                + (name != null ? ",name="+name : "")
                + (filename != null ? ",filename="+filename : "")
                + (filters != null ? ",filters="+filters : ",filters=no");
    }

    @Override
    public String toString() {
        return this.getClass().getClass().getSimpleName() + "{"+getToStringOptions()+"}";
    }
}
