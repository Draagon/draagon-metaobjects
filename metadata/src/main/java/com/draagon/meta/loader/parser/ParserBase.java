package com.draagon.meta.loader.parser;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfigLoader;

public abstract class ParserBase<T extends MetaDataLoader,M,S> {

    private final T loader;
    private final String sourceName;
    private final ClassLoader classLoader;

    /** Create the MetaDataParser */
    protected ParserBase(T loader, ClassLoader classLoader, String sourceName ) {
        this.loader = loader;
        this.sourceName = sourceName;
        this.classLoader = classLoader;
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
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
