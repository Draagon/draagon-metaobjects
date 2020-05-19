package com.draagon.meta.loader.model;

import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;

import java.util.List;

public class MetaModel extends ValueObject {

    public final static String OBJECT_NAME      = "metadata";
    public final static String FIELD_PACKAGE    = "package";
    public final static String FIELD_TYPE       = "type";
    public final static String FIELD_SUBTYPE    = "subType";
    public final static String FIELD_NAME       = "name";
    public final static String FIELD_SUPER      = "super";
    public final static String FIELD_CHILDREN   = "children";
    public final static String OBJREF_CHILDREF  = "childRef";

    public MetaModel(MetaObject mo ) {
        super(mo);
    }

    public String getPackage() {
        return getString( FIELD_PACKAGE );
    }

    public String getType() {
        return getString( FIELD_TYPE );
    }

    public String getSubType() {
        return getString(FIELD_SUBTYPE);
    }

    public String getName() {
        return getString(FIELD_NAME);
    }

    public String getSuper() {
        return getString( FIELD_SUPER );
    }

    public List<MetaModel> getChildren() {
        return super.getObjectArray( MetaModel.class, FIELD_CHILDREN );
    }
}
