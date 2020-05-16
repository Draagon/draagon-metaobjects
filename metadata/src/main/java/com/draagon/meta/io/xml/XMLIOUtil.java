package com.draagon.meta.io.xml;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.DataConverter;
import com.draagon.meta.util.MetaDataUtil;

import static com.draagon.meta.io.xml.XMLIOConstants.*;

public class XMLIOUtil {

    public static String getXmlName( MetaData md ) {
        if ( md.hasMetaAttr( ATTR_XMLNAME )) {
            return md.getMetaAttr( ATTR_XMLNAME ).getValueAsString();
        }
        return md.getShortName();
    }

    public static boolean isXmlAttr( MetaField mf ) {
        if ( mf.hasMetaAttr( ATTR_ISXMLATTR )) {
            MetaAttribute isXml = mf.getMetaAttr( ATTR_ISXMLATTR );
            return DataConverter.toBoolean( isXml.getValue() );
        }
        return false;
    }

    public static boolean xmlWrap( MetaField mf ) {

        boolean wrap = true;

        if ( mf.hasMetaAttr( ATTR_XMLWRAP )) {
            MetaAttribute isXml = mf.getMetaAttr( ATTR_XMLWRAP );
            wrap = DataConverter.toBoolean( isXml.getValue() );
        }
        else if ( mf.getDataType() == DataTypes.OBJECT
                || mf.getDataType() == DataTypes.OBJECT_ARRAY ) {
            try {
                MetaObject objRef = MetaDataUtil.getObjectRef(mf);
                if ( getXmlName( objRef ).equals( getXmlName( mf ))) {
                    wrap = false;
                }
            }
            catch( MetaObjectNotFoundException ignore ) {
                wrap = true;
            }
        }

        return wrap;
    }
}
