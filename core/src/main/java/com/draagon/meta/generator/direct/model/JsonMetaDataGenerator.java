package com.draagon.meta.generator.direct.model;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.direct.json.SingleJsonDirectGeneratorBase;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.Writer;

public class JsonMetaDataGenerator extends SingleJsonDirectGeneratorBase {

    @Override
    protected JsonDirectWriter getWriter(MetaDataLoader loader, Writer writer) throws GeneratorIOException {
        return new JsonMetaDatalWriter(loader, writer);
    }
}
