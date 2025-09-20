package com.draagon.meta.loader.file;

import com.draagon.meta.loader.LoaderOptions;

import java.util.*;

/**
 * FileMetaDataLoader Configuration Settings
 * v6.0.0: Simplified - removed parser registry pattern matching, parsers are now selected directly
 */
public class FileLoaderOptions<T extends FileLoaderOptions> extends LoaderOptions {

    private boolean allowAutoAttrs = false;
    private final List<FileMetaDataSources> sources = new ArrayList<>();

    public FileLoaderOptions() {}

    public static void createFileLoaderConfig(List<FileMetaDataSources> sources, boolean shouldRegister ) {
        FileLoaderOptions config = new FileLoaderOptions()
                .setSources( sources )
                .setAllowAutoAttrs( true )
                .setShouldRegister( shouldRegister );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Builder Overloads

    public T setShouldRegister(boolean shouldRegister ) { return super.setShouldRegister( shouldRegister ); }
    public T setVerbose(boolean verbose) { return super.setVerbose( verbose ); }
    public T setStrict(boolean strict) { return super.setStrict( strict ); }

    ///////////////////////////////////////////////////////////////////////////
    // MetaData Sources

    public T setSources( List<FileMetaDataSources> sourcesList ) {
        this.sources.clear();
        this.sources.addAll( sourcesList );
        return (T) this;
    }

    public T addSources( FileMetaDataSources sources ) {
        this.sources.add( sources );
        return (T) this;
    }

    public List<FileMetaDataSources> getSources() {
        return sources;
    }

    public boolean hasSources() {
        return !sources.isEmpty();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auto Attributes Support

    public boolean allowsAutoAttrs() {
        return allowAutoAttrs;
    }

    public FileLoaderOptions setAllowAutoAttrs(boolean allowAutoAttrs) {
        this.allowAutoAttrs = allowAutoAttrs;
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileLoaderOptions<?> config = (FileLoaderOptions<?>) o;
        return allowAutoAttrs == config.allowAutoAttrs &&
                Objects.equals(sources, config.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allowAutoAttrs, sources);
    }

    @Override
    public String toString() {
        return "FileLoaderConfig{" +
                "shouldRegister=" + shouldRegister() +
                ", verbose=" + isVerbose() +
                ", strict=" + isStrict() +
                ", allowAutoAttrs=" + allowAutoAttrs +
                ", sources=" + sources +
                '}';
    }
}