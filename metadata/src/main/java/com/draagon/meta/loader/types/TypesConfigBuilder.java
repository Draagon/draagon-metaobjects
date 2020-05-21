package com.draagon.meta.loader.types;

import com.draagon.meta.MetaData;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.ClassAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.*;
import com.draagon.meta.io.json.JsonIOConstants;
import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.loader.types.pojo.ChildConfigPojo;
import com.draagon.meta.loader.types.pojo.SubTypeConfigPojo;
import com.draagon.meta.loader.types.pojo.TypeConfigPojo;
import com.draagon.meta.loader.types.pojo.TypesConfigPojo;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoMetaObject;
import com.draagon.meta.relation.ref.ObjectReference;

public class TypesConfigBuilder {

    public final static String LOADER_TYPESCONFIG_NAME = "typesConfig";

    protected static TypesConfigLoader createDefaultTypesConfigLoader() {
        return new TypesConfigLoader( LOADER_TYPESCONFIG_NAME ).init();
    }

    public static void buildDefaultTypesConfig( TypesConfigLoader loader ) {

        loader.addChild( createTypesConfig() )
                .addChild( createTypeConfig() )
                .addChild( createSubTypeConfig() )
                .addChild( createChildConfig() );
    }

    protected static MetaObject createTypesConfig() {
        return createMetaObject( TypesConfig.OBJECT_NAME, TypesConfig.OBJECT_IONAME, TypesConfigPojo.class )
                .addChild(createTypeConfigArray());
    }

    protected static MetaObject createTypeConfig() {
        return createMetaObject( TypeConfig.OBJECT_NAME, TypeConfig.OBJECT_IONAME, TypeConfigPojo.class )
                .addChild(createStringField(TypeConfig.FIELD_NAME,true))
                .addChild(createClassFieldIO(TypeConfig.FIELD_BASECLASS, TypeConfig.FIELD_IO_CLASS, true))
                .addChild(createStringField(TypeConfig.FIELD_DEFSUBTYPE,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFNAME,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFPREFIX,true))
                .addChild(createTypeChildConfigArray(TypeConfig.FIELD_IO_CHILDREN))
                .addChild(createSubTypeConfigArray());
    }

    protected static MetaObject createSubTypeConfig() {
        return createMetaObject( SubTypeConfig.OBJECT_NAME, SubTypeConfig.OBJECT_IONAME, SubTypeConfigPojo.class )
                .addChild(createStringField(SubTypeConfig.FIELD_NAME,true))
                .addChild(createClassFieldIO(SubTypeConfig.FIELD_BASECLASS, TypeConfig.FIELD_IO_CLASS, true))
                .addChild(createChildConfigArray(TypeConfig.FIELD_IO_CHILDREN));
    }

    protected static MetaObject createChildConfig() {
        return createMetaObject(ChildConfig.OBJECT_NAME, ChildConfig.OBJECT_IONAME, ChildConfigPojo.class )
                //.addChild(createObjectClassAttr(ChildConfig.class))
                .addChild(createStringField(ChildConfig.FIELD_TYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_SUBTYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_NAME,true))
                .addChild(createStringArrayField(ChildConfig.FIELD_NAMEALIASES,true));
    }

    protected static MetaField createTypeConfigArray() {
        return createObjectArrayField(TypesConfig.FIELD_TYPES, TypesConfig.OBJREF_TYPE, TypeConfig.OBJECT_NAME, true);
    }

    protected static MetaField createSubTypeConfigArray() {
        return createObjectArrayField(TypeConfig.FIELD_SUBTYPES, TypeConfig.OBJREF_SUBTYPE, SubTypeConfig.OBJECT_NAME, false);
    }

    protected static MetaField createTypeChildConfigArray(String ioName) {
        return createObjectArrayField(TypeConfig.FIELD_TYPECHILDREN, TypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true )
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
    }

    protected static MetaField createChildConfigArray(String ioName) {
        return createObjectArrayField(SubTypeConfig.FIELD_SUBTYPECHILDREN, SubTypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true )
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName));
    }

    ///////////////////////////////////////////////////////////////
    // Generic Builder Methods

    protected static MetaObject createMetaObject( String objectName, String ioName, Class clazz ) {
        return PojoMetaObject.create(objectName)
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName))
                .addChild(createObjectClassAttr(clazz));
    }

    protected static ClassAttribute createObjectClassAttr( Class clazz ) {
        return ClassAttribute.create(MetaObject.ATTR_CLASS, clazz);
    }

    protected static MetaField createObjectArrayField(String tcFieldTypes, String tcRefType, String tcObjType, boolean xmlWrap ) {
        return ObjectArrayField.create(tcFieldTypes)
                .addChild(BooleanAttribute.create( XMLIOConstants.ATTR_XMLWRAP, xmlWrap ))
                .addChild(ObjectReference.create(tcRefType, tcObjType));
    }

    protected static MetaField createStringField(String name, boolean asXmlAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }

    protected static MetaField createStringFieldIO( String name, String ioName, boolean asXmlAttr ) {
        return StringField.create(name,null)
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName))
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }

    protected static MetaField createClassFieldIO( String name, String ioName, boolean asXmlAttr ) {
        return ClassField.create(name)
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName))
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }

    protected static MetaData createStringArrayField(String name, boolean asXmlAttr ) {
        return StringArrayField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }
}
