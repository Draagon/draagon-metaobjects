package com.draagon.meta.generator.direct.json.model;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import static com.draagon.meta.generator.util.GeneratorUtil.*;

import com.draagon.meta.generator.direct.json.JsonDirectWriter;
import com.draagon.meta.generator.direct.json.model.JsonModelGenerator;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.key.ObjectKey;
import com.draagon.meta.relation.ref.ObjectReference;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.validator.MetaValidator;
import com.draagon.meta.view.MetaView;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class UIJsonModelGenerator extends JsonModelGenerator {

    @Override
    protected UIJsonModelWriter getWriter(MetaDataLoader loader) {
        return new UIJsonModelWriter(loader);
    }
}
