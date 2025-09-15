package com.draagon.meta.loader.types;

public interface ChildConfigLogic {

    public void setAutoCreatedFromFile( String file );

    public boolean wasAutoCreated();

    public String getCreatedFromFile();

    public void merge( ChildConfig cc );
}
