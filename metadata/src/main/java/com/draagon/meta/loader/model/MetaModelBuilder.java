package com.draagon.meta.loader.model;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.ClassAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.field.ObjectField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.loader.model.pojo.MetaModelPojo;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;

public class MetaModelBuilder {

    public static MetaData buildDefaultMetaDataModels() {

        // METADATA ROOT
        MetaObject metadata = PojoMetaObject.create(MetaModel.OBJECT_NAME)
                .addChild(StringAttribute.create(MetaObject.ATTR_OBJECT, MetaModelPojo.class.getName()))
                .addChild(buildStringField(MetaModel.FIELD_PACKAGE,true))
                .addChild(buildStringField(MetaModel.FIELD_SUPER,true))
                .addChild(buildStringField(MetaModel.FIELD_TYPE,true)
)
                .addChild(buildStringField(MetaModel.FIELD_SUBTYPE,true)
)
                .addChild(buildStringField(MetaModel.FIELD_NAME,true))
                .addChild(buildValueField(MetaModel.FIELD_VALUE))
                .addChild(ObjectArrayField.create(MetaModel.FIELD_CHILDREN)
                        .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_XMLWRAP, false ))
                        .addChild(StringAttribute.create(MetaObject.ATTR_OBJECT_REF, MetaModel.OBJECT_NAME)));

        return metadata;
    }

    public static MetaData buildStringField( String name, boolean asAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asAttr));
    }

    public static MetaData buildValueField( String name ) {
        return StringField.create(name,null)   // TODO: This should be workable as an Object field
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_XMLWRAP, false ));
    }
}
