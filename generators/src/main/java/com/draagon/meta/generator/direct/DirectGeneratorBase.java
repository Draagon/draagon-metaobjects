package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorMetaException;

import java.util.List;
import java.util.Map;

public abstract class DirectGeneratorBase<T extends DirectGeneratorBase> extends GeneratorBase<T> {

    @Override
    public T setScripts(List<String> scripts) {
        throw new GeneratorMetaException( "A Direct Generator does not support specifying scripts");
    }
}