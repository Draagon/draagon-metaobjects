package com.draagon.meta.loader.config;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.MetaDataParser;
import com.draagon.meta.loader.file.MetaDataSources;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * FileMetaDataLoader Configuration Settings
 */
public class LoaderConfig<N extends LoaderConfig> {

    private boolean shouldRegister = false;
    private boolean verbose = true;
    private boolean strict = true;

    public LoaderConfig() {}

    public static void createFileLoaderConfig( boolean shouldRegister ) {
        LoaderConfig config = new LoaderConfig()
                .setShouldRegister( shouldRegister );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flags

    public N setShouldRegister(boolean shouldRegister ) {
        this.shouldRegister = shouldRegister;
        return (N) this;
    }

    public boolean shouldRegister() {
        return shouldRegister;
    }

    /**
     * verbose = true
     *   = log output report of loaded metadata types:   # packages, # of objects, etc.
     *
     * @param verbose
     * @return
     */
    public N setVerbose(boolean verbose) {
        this.verbose = verbose;
        return (N) this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    /**
     * strict = false
     *    = log warnings on unknown metadata types
     *    = output report should # of ignored metadata types
     *
     * @param strict
     * @return
     */
    public N setStrict(boolean strict) {
        this.strict  = strict;
        return (N) this;
    }

    public boolean isStrict() {
        return strict;
    }



    ///////////////////////////////////////////////////////////////////////////
    // Misc Functions

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoaderConfig<?> that = (LoaderConfig<?>) o;
        return shouldRegister == that.shouldRegister &&
                verbose == that.verbose &&
                strict == that.strict;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldRegister, verbose, strict);
    }

    @Override
    public String toString() {
        return "LoaderConfig{" +
                "shouldRegister=" + shouldRegister +
                ", verbose=" + verbose +
                ", strict=" + strict +
                '}';
    }
}
