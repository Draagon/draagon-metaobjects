package com.draagon.meta.loader.typed.config;

import java.util.Objects;

/**
 * Stores the MetaData Configuration data
 */
public class MetaDataConfig {

    private final TypesConfig types;

    public MetaDataConfig() {
        types = new TypesConfig();
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public TypesConfig getTypesConfig() {
        return types;
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {
        types.validate();
    }

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataConfig that = (MetaDataConfig) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public String toString() {
        return "MetaDataConfig{" +
                "types=" + types +
                '}';
    }
}
