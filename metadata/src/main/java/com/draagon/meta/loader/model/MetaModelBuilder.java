package com.draagon.meta.loader.model;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.ClassAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.relation.ref.ObjectReference;

public class MetaModelBuilder {

    /*public MetaDataLoader createModelLoaderFromTypes( TypesConfig typesConfig ) {
        MetaDataLoader modelLoader = MetaDataLoader
                .createManual( false, "metadata-model")
                .init();
        buildMetaDataModels( modelLoader, typesConfig );
        return modelLoader;
    }

    public MetaDataLoader createStandardModelLoader() {
        MetaDataLoader modelLoader = MetaDataLoader
                .createManual( false, "metadata-model")
                .init();
        buildDefaultMetaDataModels( modelLoader );
        return modelLoader;
    }*/

    public static void buildDefaultMetaDataModels( MetaDataLoader loader ) {
        buildMetaDataModels( loader, null );
    }

    public static void buildMetaDataModels( MetaDataLoader loader, TypesConfig typesConfig ) {

        // METADATA ROOT
        MetaObject metadata = ValueMetaObject.create(MetaModel.OBJECT_NAME)
                .addChild(ClassAttribute.create(ClassAttribute.SUBTYPE_CLASS, MetaModel.class))
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLTYPED, MetaModel.FIELD_TYPE ))
                .addChild(buildStringField(MetaModel.FIELD_PACKAGE,true))
                .addChild(buildStringField(MetaModel.FIELD_SUPER,true))
                .addChild(buildStringField(MetaModel.FIELD_TYPE,true)
                        .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_XMLIGNORE, true)))
                .addChild(buildStringField(MetaModel.FIELD_SUBTYPE,true)
                        .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, MetaModel.FIELD_TYPE)))
                .addChild(buildStringField(MetaModel.FIELD_NAME,true))
                .addChild(ObjectArrayField.create(MetaModel.FIELD_CHILDREN)
                        .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_XMLWRAP, false ))
                        .addChild(ObjectReference.create(MetaModel.OBJREF_CHILDREF, MetaModel.OBJECT_NAME)));
        loader.addChild( metadata );
    }

    public static MetaData buildStringField( String name, boolean asAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asAttr));
    }
}
