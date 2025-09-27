package com.metaobjects.io.string;

public interface StringSerializationHandler {
    public String getValueAsString(Object o);
    public void setValueAsString(Object o, String val);
}
