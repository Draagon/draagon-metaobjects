package com.draagon.meta.loader.types;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.object.Validatable;

import java.util.List;

public interface ChildConfigLogic {

    public void setAutoCreatedFromFile( String file );

    public boolean wasAutoCreated();

    public String getCreatedFromFile();

    public void merge( ChildConfig cc );
}
