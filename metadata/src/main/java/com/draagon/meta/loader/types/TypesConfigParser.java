package com.draagon.meta.loader.types;

import com.draagon.meta.loader.parser.ParserBase;

import java.util.List;

public abstract class TypesConfigParser<S> extends ParserBase<TypesConfigLoader,TypesConfig,S> {

    protected TypesConfigParser(TypesConfigLoader loader, String sourceName) {
        super(loader, sourceName);
    }

    @Override
    public abstract void loadAndMerge( TypesConfig intoConfig, S source );

    protected void mergeTypesConfig(TypesConfig intoConfig, TypesConfig loadedConfig) {

        List<TypeConfig> loaded = loadedConfig.getTypes();
        for( TypeConfig tc : loaded ) {
            intoConfig.addOrMergeType(tc);
        }
    }
}
