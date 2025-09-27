package com.metaobjects.field;

import com.metaobjects.DataTypes;
import com.metaobjects.io.string.StringSerializationHandler;
import com.metaobjects.util.DataConverter;

public abstract class PrimitiveField<T> extends MetaField<T> implements StringSerializationHandler {

    public PrimitiveField(String subtype, String name, DataTypes dataType) {
        super(subtype, name, dataType);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // String SerializationHandler

    public String getValueAsString(Object o) {
        return DataConverter.toString( getObjectAttribute( o ));
    }

    public void setValueAsString(Object o, String val) {
        setObjectAttribute( o,  DataConverter.toType(getDataType(), val ));
    }

}
