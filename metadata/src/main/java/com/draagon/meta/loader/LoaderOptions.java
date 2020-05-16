package com.draagon.meta.loader;

import java.util.Objects;


/**
 * FileMetaDataLoader Configuration Settings
 */
public class LoaderOptions<T extends LoaderOptions> {

    private boolean shouldRegister = false;
    private boolean verbose = true;

    public LoaderOptions() {}

    public static LoaderOptions create(boolean shouldRegister, boolean verbose ) {
        LoaderOptions config = new LoaderOptions()
                .setShouldRegister( shouldRegister )
                .setVerbose( verbose);
        return config;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Flags

    public T setShouldRegister(boolean shouldRegister ) {
        this.shouldRegister = shouldRegister;
        return (T) this;
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
    public T setVerbose(boolean verbose) {
        this.verbose = verbose;
        return (T) this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Misc Functions

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoaderOptions<?> that = (LoaderOptions<?>) o;
        return shouldRegister == that.shouldRegister &&
                verbose == that.verbose;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldRegister, verbose);
    }

    protected String getToStringOptions() {
        return "shouldRegister=" + shouldRegister +
                ", verbose=" + verbose;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{" + getToStringOptions() + '}';
    }
}
