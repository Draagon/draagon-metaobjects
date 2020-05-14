package com.draagon.meta.generator.direct.json.model;

import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.direct.json.SingleJsonDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;

public class JsonModelGenerator extends SingleJsonDirectGeneratorBase {

    @Override
    protected JsonDirectWriter getWriter(MetaDataLoader loader) {
        return new JsonModelWriter(loader);
    }
}
