package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Stores the MetaData Configuration data
 */
public class MetaDataConfig {

    private final MetaDataTypes types;

    public MetaDataConfig() {
        types = new MetaDataTypes();
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public MetaDataTypes getMetaDataTypes() {
        return types;
    }

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    public String toString() {
        return "MetaDataConfig: " + types.toString();
    }
}
