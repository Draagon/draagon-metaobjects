package com.draagon.meta.loader.config;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.lang.reflect.Modifier;
import java.util.List;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class SubTypeConfig extends ConfigObjectBase {

    public final static String OBJECT_NAME        = "SubTypeConfig";
    public final static String OBJECT_IONAME      = "subType";
    public final static String FIELD_NAME         = "name";
    public final static String FIELD_BASECLASS    = "class";
    public final static String FIELD_CHILDREN     = "children";
    public final static String OBJREF_CHILD       = "childRef";

    public SubTypeConfig(MetaObject mo ) {
        super(mo);
    }

    public static SubTypeConfig create( MetaDataLoader loader,
                                        String subtypeName,
                                        Class<? extends MetaData> baseClass ) {
        SubTypeConfig c = _newInstance( SubTypeConfig.class, loader, OBJECT_NAME );
        c.setTypeName( subtypeName );
        c.setBaseClass( baseClass );
        return c;
    }

    /////////////////////////////////////////////////////////////////////
    // Type  Methods

    public String getTypeName() {
        return _getString( FIELD_NAME );
    }

    public void setTypeName(String type) {
        _setString( FIELD_NAME, type );
    }

    public Class<? extends MetaData> getBaseClass() {
        return _getClass( MetaData.class, FIELD_BASECLASS );
    }

    public void setBaseClass( Class<? extends MetaData> clazz ) {
        _setClass( FIELD_BASECLASS, clazz );
    }

    public List<ChildConfig> getChildConfigs() {
        return _getAndCreateObjectArray( ChildConfig.class, FIELD_CHILDREN );
    }

    //public void setChildConfigs( List<ChildConfig> children ) {
    //    _setObjectArray( FIELD_CHILDREN, children );
    //}


    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {
        super.validate();
        if ( getTypeName() == null ) throw new InvalidValueException( "Type name on SubType cannot be null" );
        if ( getBaseClass() == null ) throw new InvalidValueException( "Base class on SubType ["+getTypeName()+"] cannot be null" );

        if ( Modifier.isAbstract( getBaseClass().getModifiers() )) throw new InvalidValueException(
                "Base class ["+getBaseClass()+"] on SubType ["+getTypeName()+"] cannot be Abstract" );

        getChildConfigs().forEach( cc -> cc.validate() );
    }
}
