package com.metaobjects.io.json;

import com.metaobjects.field.MetaField;

import static com.metaobjects.io.json.JsonIOConstants.*;

public class JsonIOUtil {

    public static String getJsonName(MetaField mf ) {
        if ( mf.hasMetaAttr(ATTR_JSONNAME)) {
            return mf.getMetaAttr(ATTR_JSONNAME).getValueAsString();
        }
        return mf.getName();
    }
}
