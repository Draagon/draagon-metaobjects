package com.draagon.meta.loader.typed;

import com.draagon.meta.loader.LoaderOptions;

import java.util.Objects;

public class TypedLoaderOptions<T extends TypedLoaderOptions> extends LoaderOptions<T> {

    private boolean strict = true;

    public TypedLoaderOptions() {}

    public static TypedLoaderOptions create(boolean shouldRegister, boolean verbose, boolean strict ) {
        TypedLoaderOptions config = new TypedLoaderOptions();
        config.setStrict( strict )
                .setShouldRegister( shouldRegister )
                .setVerbose( verbose);
        return config;
    }

    /**
     * strict = false
     *    = log warnings on unknown metadata types
     *    = output report should # of ignored metadata types
     *
     * @param strict
     * @return
     */
    public T setStrict(boolean strict) {
        this.strict  = strict;
        return (T) this;
    }

    public boolean isStrict() {
        return strict;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Misc Functions

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypedLoaderOptions<?> that = (TypedLoaderOptions<?>) o;
        if ( !super.equals( that )) return false;
        return strict == that.strict;
    }

    @Override
    public int hashCode() {
        return Objects.hash( super.hashCode(), strict);
    }

    @Override
    protected String getToStringOptions() {
        return super.getToStringOptions() +
                ", strict=" + strict;
    }
}
