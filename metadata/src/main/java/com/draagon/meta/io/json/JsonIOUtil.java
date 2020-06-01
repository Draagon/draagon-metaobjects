package com.draagon.meta.io.json;

import com.draagon.meta.field.MetaField;

import static com.draagon.meta.io.json.JsonIOConstants.*;

public class JsonIOUtil {

    public static String getJsonName(MetaField mf ) {
        if ( mf.hasMetaAttr(ATTR_JSONNAME)) {
            return mf.getMetaAttr(ATTR_JSONNAME).getValueAsString();
        }
        return mf.getName();
    }
}
