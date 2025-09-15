package com.draagon.meta.loader.types;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.*;
import com.draagon.meta.io.json.JsonIOConstants;
import com.draagon.meta.loader.types.pojo.ChildConfigPojo;
import com.draagon.meta.loader.types.pojo.SubTypeConfigPojo;
import com.draagon.meta.loader.types.pojo.TypeConfigPojo;
import com.draagon.meta.loader.types.pojo.TypesConfigPojo;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;

public class TypesConfigBuilder {

    public final static String LOADER_TYPESCONFIG_NAME = "typesConfig";

    protected static TypesConfigLoader createDefaultTypesConfigLoader() {
        return new TypesConfigLoader( LOADER_TYPESCONFIG_NAME ).init();
    }

    public static void buildDefaultTypesConfig( TypesConfigLoader loader ) {

        loader.addChild( createTypesConfig() );
        loader.addChild( createTypeConfig() );
        loader.addChild( createSubTypeConfig() );
        loader.addChild( createChildConfig() );
    }

    protected static MetaObject createTypesConfig() {
        MetaObject typesConfig = createMetaObject( TypesConfig.OBJECT_NAME, TypesConfig.OBJECT_IONAME, TypesConfigPojo.class );
        typesConfig.addChild(createTypeConfigArray());
        return typesConfig;
    }

    protected static MetaObject createTypeConfig() {
        MetaObject typeConfig = createMetaObject( TypeConfig.OBJECT_NAME, TypeConfig.OBJECT_IONAME, TypeConfigPojo.class );
        typeConfig.addChild(createStringField(TypeConfig.FIELD_NAME,true));
        typeConfig.addChild(createStringFieldIO(TypeConfig.FIELD_BASECLASS, TypeConfig.FIELD_IO_CLASS, true));
        typeConfig.addChild(createStringField(TypeConfig.FIELD_DEFSUBTYPE,true));
        typeConfig.addChild(createStringField(TypeConfig.FIELD_DEFNAME,true));
        typeConfig.addChild(createStringField(TypeConfig.FIELD_DEFPREFIX,true));
        typeConfig.addChild(createTypeChildConfigArray(TypeConfig.FIELD_IO_CHILDREN));
        typeConfig.addChild(createSubTypeConfigArray());
        return typeConfig;
    }

    protected static MetaObject createSubTypeConfig() {
        MetaObject subTypeConfig = createMetaObject( SubTypeConfig.OBJECT_NAME, SubTypeConfig.OBJECT_IONAME, SubTypeConfigPojo.class );
        subTypeConfig.addChild(createStringField(SubTypeConfig.FIELD_NAME,true));
        subTypeConfig.addChild(createStringFieldIO(SubTypeConfig.FIELD_BASECLASS, TypeConfig.FIELD_IO_CLASS, true));
        subTypeConfig.addChild(createChildConfigArray(TypeConfig.FIELD_IO_CHILDREN));
        return subTypeConfig;
    }

    protected static MetaObject createChildConfig() {
        MetaObject childConfig = createMetaObject(ChildConfig.OBJECT_NAME, ChildConfig.OBJECT_IONAME, ChildConfigPojo.class );
        childConfig.addChild(createStringField(ChildConfig.FIELD_TYPE,true));
        childConfig.addChild(createStringField(ChildConfig.FIELD_SUBTYPE,true));
        childConfig.addChild(createStringField(ChildConfig.FIELD_NAME,true));
        childConfig.addChild(createStringArrayField(ChildConfig.FIELD_NAMEALIASES,true));
        return childConfig;
    }

    protected static MetaField createTypeConfigArray() {
        MetaField field = createObjectArrayField(TypesConfig.FIELD_TYPES, TypesConfig.OBJREF_TYPE, TypeConfig.OBJECT_NAME, true);
        field.addChild(StringAttribute.create(MetaField.ATTR_DEFAULT_VALUE, "[]"));
        return field;
    }

    protected static MetaField createSubTypeConfigArray() {
        MetaField field = createObjectArrayField(TypeConfig.FIELD_SUBTYPES, TypeConfig.OBJREF_SUBTYPE, SubTypeConfig.OBJECT_NAME, false);
        field.addChild(StringAttribute.create(MetaField.ATTR_DEFAULT_VALUE, "[]"));
        return field;
    }

    protected static MetaField createTypeChildConfigArray(String ioName) {
        MetaField field = createObjectArrayField(TypeConfig.FIELD_TYPECHILDREN, TypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true );
        field.addChild(StringAttribute.create(MetaField.ATTR_DEFAULT_VALUE, "[]"));
        field.addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
        return field;
    }

    protected static MetaField createChildConfigArray(String ioName) {
        MetaField field = createObjectArrayField(SubTypeConfig.FIELD_SUBTYPECHILDREN, SubTypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true );
        field.addChild(StringAttribute.create(MetaField.ATTR_DEFAULT_VALUE, "[]"));
        field.addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
        return field;
    }

    ///////////////////////////////////////////////////////////////
    // Generic Builder Methods

    protected static MetaObject createMetaObject( String objectName, String ioName, Class clazz ) {
        MetaObject metaObject = PojoMetaObject.create(objectName);
        metaObject.addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
        metaObject.addChild(createObjectClassAttr(clazz));
        return metaObject;
    }

    protected static StringAttribute createObjectClassAttr( Class clazz ) {
        return StringAttribute.create(MetaObject.ATTR_OBJECT, clazz.getName());
    }

    protected static MetaField createObjectArrayField(String tcFieldTypes, String tcRefType, String tcObjType, boolean xmlWrap ) {
        MetaField field = ObjectArrayField.create(tcFieldTypes);
        field.addChild(StringAttribute.create(MetaObject.ATTR_OBJECT_REF, tcObjType));
        return field;
    }

    protected static MetaField createStringField(String name, boolean asXmlAttr ) {
        return StringField.create(name,null)
;
    }

    protected static MetaField createStringFieldIO( String name, String ioName, boolean asXmlAttr ) {
        MetaField field = StringField.create(name,null);
        field.addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
        return field;
    }

    protected static MetaField createClassFieldIO( String name, String ioName, boolean asXmlAttr ) {
        MetaField field = ClassField.create(name);
        field.addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
        return field;
    }

    protected static MetaData createStringArrayField(String name, boolean asXmlAttr ) {
        return StringArrayField.create(name,null)
;
    }
}
