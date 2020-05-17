package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.xml.XMLObjectReader;
import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.TypesConfig;
import com.draagon.meta.loader.config.TypesConfigBuilder;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataMetaObject;
import com.draagon.meta.relation.ref.ObjectReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleBuilder implements MetaDataConstants {

    public static void build(SimpleLoader simpleLoader, String resource ) {

        TypesConfig typesConfig = loadTypesConfig(simpleLoader.getClass().getClassLoader(), resource);

        buildMetaData( simpleLoader, typesConfig );
    }

    public static TypesConfig loadTypesConfig(ClassLoader classLoader, String resource) {

        TypesConfig typesConfig;

        MetaDataLoader loader = TypesConfigBuilder.buildTypesConfigLoader();

        try {
            InputStream is = null;
            File f = new File(resource);
            if (!f.exists()) {
                is = classLoader.getResourceAsStream(resource);
            } else {
                is = new FileInputStream(f);
            }

            XMLObjectReader reader = new XMLObjectReader( loader, is );
            typesConfig = (TypesConfig) reader.read( loader.getMetaObjectByName(TypesConfig.OBJECT_NAME));
            reader.close();
        }
        catch( MetaDataIOException | IOException e ) {
            throw new MetaDataException( "Unable to load typesConfig from resource ["+resource+"]: " + e.getMessage(), e );
        }

        return typesConfig;
    }

    public static void buildMetaData( MetaDataLoader loader, TypesConfig typesConfig ) {

        // METADATA ROOT
        MetaObject metadata = DataMetaObject.create(OBJ_METADATA)
                .addChild(buildStringField(MD_FIELD_PACKAGE,true))
                .addChild(buildStringField(MD_FIELD_SUPER,true))
                .addChild(buildStringField(MD_FIELD_TYPE,true))
                .addChild(buildStringField(MD_FIELD_SUBTYPE,true))
                .addChild(buildStringField(MD_FIELD_NAME,true))
                .addChild(ObjectArrayField.create(MD_FIELD_CHILDREN)
                    .addChild(ObjectReference.create(MD_REF_CHILDREF, OBJ_METADATA)));
        loader.addChild( metadata );
    }

    public static MetaData buildStringField( String name, boolean asAttr ) {
        return StringField.create(name,null)
                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR,null));
    }
}
