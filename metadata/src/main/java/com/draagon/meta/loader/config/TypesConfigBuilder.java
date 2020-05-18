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
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.relation.ref.ObjectReference;

public class TypesConfigBuilder {

    public final static String LOADER_TYPESCONFIG_NAME = "typesConfig";

    protected static TypesConfigLoader createDefaultTypesConfigLoader() {
        return new TypesConfigLoader( LOADER_TYPESCONFIG_NAME ).init();
    }

    public static void buildTypesConfig( TypesConfigLoader loader ) {

        loader.addChild( createTypesConfig() )
                .addChild( createTypeConfig() )
                .addChild( createSubTypeConfig() )
                .addChild( createChildConfig() );
    }

    protected static MetaObject createTypesConfig() {
        return createMetaObject( TypesConfig.OBJECT_NAME, TypesConfig.OBJECT_IONAME, TypesConfig.class )
                .addChild(createTypeConfigArray());
    }

    protected static MetaObject createTypeConfig() {
        return createMetaObject( TypeConfig.OBJECT_NAME, TypeConfig.OBJECT_IONAME, TypeConfig.class )
                .addChild(createStringField(TypeConfig.FIELD_NAME,true))
                .addChild(createStringField(TypeConfig.FIELD_BASECLASS,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFSUBTYPE,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFNAME,true))
                .addChild(createStringField(TypeConfig.FIELD_DEFPREFIX,true))
                .addChild(createSubTypeConfigArray())
                .addChild(createTypeChildConfigArray());
    }

    protected static MetaObject createSubTypeConfig() {
        return createMetaObject( SubTypeConfig.OBJECT_NAME, SubTypeConfig.OBJECT_IONAME, SubTypeConfig.class )
                .addChild(createStringField(SubTypeConfig.FIELD_NAME,true))
                .addChild(createStringField(SubTypeConfig.FIELD_BASECLASS,true))
                .addChild(createChildConfigArray());
    }

    protected static MetaObject createChildConfig() {
        return createMetaObject(ChildConfig.OBJECT_NAME, ChildConfig.OBJECT_IONAME, ChildConfig.class )
                .addChild(createObjectClassAttr(ChildConfig.class))
                .addChild(createStringField(ChildConfig.FIELD_TYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_SUBTYPE,true))
                .addChild(createStringField(ChildConfig.FIELD_NAME,true))
                .addChild(createStringField(ChildConfig.FIELD_NAMEALIASES,true));
    }

    protected static MetaField createTypeConfigArray() {
        return createObjectArrayField(TypesConfig.FIELD_TYPES, TypesConfig.OBJREF_TYPE, TypeConfig.OBJECT_NAME, true);
    }

    protected static MetaField createSubTypeConfigArray() {
        return createObjectArrayField(TypeConfig.FIELD_SUBTYPES, TypeConfig.OBJREF_SUBTYPE, SubTypeConfig.OBJECT_NAME, false);
    }

    protected static MetaField createTypeChildConfigArray() {
        return createObjectArrayField(TypeConfig.FIELD_CHILDREN, TypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true );
    }

    protected static MetaField createChildConfigArray() {
        return createObjectArrayField(SubTypeConfig.FIELD_CHILDREN, SubTypeConfig.OBJREF_CHILD, ChildConfig.OBJECT_NAME, true );
    }

    ///////////////////////////////////////////////////////////////
    // Generic Builder Methods

    protected static MetaObject createMetaObject( String objectName, String ioName,
                                               Class<? extends ConfigObjectAbstract> clazz ) {
        return ValueMetaObject.create(objectName)
                .addChild(StringAttribute.create(XMLIOConstants.ATTR_XMLNAME, ioName))
                .addChild(StringAttribute.create(JsonIOConstants.ATTR_JSONNAME, ioName))
                .addChild(createObjectClassAttr(clazz));
    }

    protected static ClassAttribute createObjectClassAttr( Class<? extends ConfigObjectAbstract> clazz ) {
        return ClassAttribute.create(MetaObject.ATTR_CLASS, clazz);
    }

    protected static MetaField createObjectArrayField(String tcFieldTypes, String tcRefType, String tcObjType, boolean xmlWrap ) {
        return ObjectArrayField.create(tcFieldTypes)
                .addChild(BooleanAttribute.create( XMLIOConstants.ATTR_XMLWRAP, xmlWrap ))
                .addChild(ObjectReference.create(tcRefType, tcObjType));
    }

    protected static MetaData createStringField(String name, boolean asXmlAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,asXmlAttr));
    }
}
