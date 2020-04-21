package com.draagon.meta.loader.config;

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

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    public String toString() {
        return "MetaDataConfig: " + types.toString();
    }
}
