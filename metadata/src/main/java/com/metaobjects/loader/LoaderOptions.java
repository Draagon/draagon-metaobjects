package com.metaobjects.loader;

import java.util.Objects;


/**
 * FileMetaDataLoader Configuration Settings
 */
public class LoaderOptions {

    private boolean shouldRegister = false;
    private boolean verbose = true;
    private boolean strict = true;

    public LoaderOptions() {}

    public static LoaderOptions create(boolean shouldRegister, boolean verbose, boolean strict ) {
        LoaderOptions config = new LoaderOptions()
                .setShouldRegister( shouldRegister )
                .setVerbose( verbose)
                .setStrict( strict);
        return config;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flags

    public <T extends LoaderOptions> T setShouldRegister(boolean shouldRegister ) {
        this.shouldRegister = shouldRegister;
        return (T) this;
    }

    public boolean shouldRegister() {
        return shouldRegister;
    }

    public <T extends LoaderOptions>T setVerbose(boolean verbose) {
        this.verbose = verbose;
        return (T) this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public <T extends LoaderOptions>T setStrict(boolean strict) {
        this.strict = strict;
        return (T) this;
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
        LoaderOptions that = (LoaderOptions) o;
        return shouldRegister == that.shouldRegister &&
                verbose == that.verbose &&
                strict == that.strict;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldRegister, verbose);
    }

    protected String getToStringOptions() {
        return "shouldRegister=" + shouldRegister +
                ", verbose=" + verbose +
                ", strict=" + strict;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{" + getToStringOptions() + '}';
    }
}
