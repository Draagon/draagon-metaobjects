package com.metaobjects;

public interface MetaDataValueHandler<T> {

    public void setValueAsString( String value );
    public void setValueAsObject( Object value );
    public void setValue( T value );

    public String getValueAsString();
    public T getValue();
}
