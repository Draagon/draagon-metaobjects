package com.draagon.meta.loader.parser;

import com.draagon.meta.loader.MetaDataLoader;

public abstract class ParserBase<T extends MetaDataLoader,M,S> {

    private final T loader;
    private final String sourceName;

    /** Create the MetaDataParser */
    protected ParserBase(T loader, String sourceName ) {
        this.loader = loader;
        this.sourceName = sourceName;
    }

    /** Return the FileMetaDataLoader */
    public T getLoader() {
        return this.loader;
    }

    /** Return the filename being loaded */
    public String getSourcename() {
        return sourceName;
    }

    /** Load and Parse from Source and then Merge into */
    public abstract void loadAndMerge( M into, S source );
}
