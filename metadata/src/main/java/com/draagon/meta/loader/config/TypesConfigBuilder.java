package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.ClassAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.io.json.JsonIOConstants;
import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.io.xml.XMLIOUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.relation.ref.ObjectReference;

public class TypesConfigBuilder {

    public final static String LOADER_TYPESCONFIG_NAME = "typesConfig";

    public static MetaDataLoader buildTypesConfigLoader() {
        MetaDataLoader loader = MetaDataLoader
                .createManual( false, LOADER_TYPESCONFIG_NAME)
                .init();
        TypesConfigBuilder.buildTypesConfig( loader );
        return loader;
    }

    public static void buildTypesConfig( MetaDataLoader loader ) {

        loader.addChild( createTypesConfig() )
                .addChild( createTypeConfig() )
                .addChild( createSubTypeConfig() )
                .addChild( createChildConfig() );
    }

    public static MetaObject createTypesConfig() {
        return createMetaObject( TypesConfig.OBJECT_NAME, TypesConfig.OBJECT_IONAME, TypesConfig.class )
                .addChild(createTypeConfigArray());
    }

    public static MetaObject createTypeConfig() {
        return createMetaObject( TypeConfig.OBJECT_NAME, TypeConfig.OBJECT_IONAME, TypeConfig.class )
                .addChild(createStringField(TypeConfig.FIELD_TYPE,true))
                .addChild(createStringField(TypeConfig.FIELD_BASECLASS,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFSUBTYPE,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFNAME,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFPREFIX,true))
                .addChild(createSubTypeConfigArray())
                .addChild(createChildConfigArray());
    }

    public static MetaObject createSubTypeConfig() {
        return createMetaObject( SubTypeConfig.OBJECT_NAME, SubTypeConfig.OBJECT_IONAME, SubTypeConfig.class )
                .addChild(createStringField(SubTypeConfig.FIELD_TYPE,true))
                .addChild(createStringField(SubTypeConfig.FIELD_BASECLASS,true))
                .addChild(createChildConfigArray());
    }

    public static MetaObject createChildConfig() {
        return createMetaObject(ChildConfig.OBJECT_NAME, ChildConfig.OBJECT_IONAME, ChildConfig.class )
                .addChild(createObjectClassAttr(ChildConfig.class))
                .addChild(createStringField(ChildConfig.FIELD_TYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_SUBTYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_NAME,true))
                .addChild(createStringField(ChildConfig.FIELD_NAMEALIASES,true));
    }

    public static MetaField createTypeConfigArray() {
        return createObjectArrayField(TypesConfig.FIELD_TYPES, TypesConfig.OBJREF_TYPE, TypeConfig.OBJECT_NAME, true);
    }

    public static MetaField createSubTypeConfigArray() {
        return createObjectArrayField(TypeConfig.FIELD_SUBTYPES, TypeConfig.OBJREF_SUBTYPE, SubTypeConfig.OBJECT_NAME, false);
    }

    public static MetaField createChildConfigArray() {
        return createObjectArrayField(ConfigObjectAbstract.FIELD_CHILDREN, ConfigObjectAbstract.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true );
    }

    ///////////////////////////////////////////////////////////////
    // Generic Builder Methods

    public static MetaObject createMetaObject( String objectName, String ioName,
                                               Class<? extends ConfigObjectAbstract> clazz ) {
        return ValueMetaObject.create(objectName)
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName))
                .addChild(createObjectClassAttr(TypesConfig.class));
    }

    public static ClassAttribute createObjectClassAttr( Class<? extends ConfigObjectAbstract> clazz ) {
        return ClassAttribute.create(MetaObject.ATTR_CLASS, clazz);
    }

    public static MetaField createObjectArrayField(String tcFieldTypes, String tcRefType, String tcObjType, boolean xmlWrap ) {
        return ObjectArrayField.create(tcFieldTypes)
                .addChild(BooleanAttribute.create( XMLIOConstants.ATTR_XMLWRAP, xmlWrap ))
                .addChild(ObjectReference.create(tcRefType, tcObjType));
    }

    public static MetaData createStringField(String name, boolean asXmlAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }
}
