package com.draagon.meta.generator.direct.json.model;

import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.loader.MetaDataLoader;

import java.io.Writer;

public class UIJsonModelGenerator extends JsonModelGenerator {

    @Override
    protected UIJsonModelWriter getWriter(MetaDataLoader loader, Writer writer) throws GeneratorIOException {
        return new UIJsonModelWriter(loader, writer);
    }
}
