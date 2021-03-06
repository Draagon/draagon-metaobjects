package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.util.DataConverter;

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
