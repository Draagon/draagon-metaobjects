package com.draagon.meta.loader.config;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.MetaObject;

import java.util.List;

public final class ChildConfig extends ConfigObjectBase {

    public final static String OBJECT_NAME       = "ChildConfig";
    public final static String OBJECT_IONAME     = "child";
    public final static String FIELD_TYPE        = "type";
    public final static String FIELD_SUBTYPE     = "subType";
    public final static String FIELD_NAME        = "name";
    public final static String FIELD_NAMEALIASES = "nameAliases";

    private String createdFromFile = null;

    public ChildConfig( MetaObject mo ) {
        super(mo);
    }

    ///////////////////////////////////////////////////////////
    // GETTERS AND SETTERS

    public String getType() {
        return _getString(FIELD_TYPE);
    }

    public void setType(String type) {
        _setString(FIELD_TYPE,type);
    }

    public String getSubType() {
        return _getString(FIELD_SUBTYPE);
    }

    public void setSubType(String subType) {
        _setString(FIELD_SUBTYPE,subType);
    }

    public String getName() {
        return _getString(FIELD_NAME);
    }

    public void setName(String name) {
        _setString(FIELD_NAME,name);
    }

    public List<String> getNameAliases() {
        return _getStringArray(FIELD_NAMEALIASES);
    }

    public void setNameAliases( List<String> nameAliases ) {
        _setStringArray( FIELD_NAMEALIASES, nameAliases );
    }


    ///////////////////////////////////////////////////////////////////////
    // Auto Creation Methods

    public void setAutoCreatedFromFile( String file ) {
        this.createdFromFile = file;
    }

    public boolean wasAutoCreated() {
        return createdFromFile != null;
    }

    public String getCreatedFromFile() {
        return createdFromFile;
    }


    ///////////////////////////////////////////////////////////////////////
    // Validation

    @Override
    public void validate() {
        super.validate();
        if ( getType() == null || getSubType() == null || getName() == null ) {
            throw new InvalidValueException( "ChildConfig does not have type,subType, and/or name fields set: " + toString() );
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // MISC METHODS

    public void merge( ChildConfig cc ) {
        if ( !getType().equals( cc.getType() )
                || !getSubType().equals( cc.getSubType() )
                || !getName().equals( cc.getName() )) {
            throw new MetaDataException("Cannot merge when different type/subtype/name: ["+toString()+"]"+
                    " merged with ["+cc.toString()+"]");
        }
        if ( cc.getNameAliases() != null ) mergeNameAliases( cc.getNameAliases() );
        //if ( cc.required != null ) required = cc.required;
        //if ( cc.autoCreate != null ) autoCreate = cc.autoCreate;
        //if ( cc.defaultValue != null ) defaultValue = cc.defaultValue;
        //if ( cc.minValue != null ) minValue = cc.minValue;
        //if ( cc.maxValue != null ) maxValue = cc.maxValue;
        //if ( cc.inlineAttr != null ) inlineAttr = cc.inlineAttr;
        //if ( cc.inlineAttrName != null ) inlineAttrName = cc.inlineAttrName;
        //if ( cc.inlineAttrValueMap != null ) inlineAttrValueMap = cc.inlineAttrValueMap;
    }

    private void mergeNameAliases(List<String> nameAliases) {

        List<String> current = getNameAliases();
        if ( current == null ) {
            setNameAliases( nameAliases );
        }
        else if ( nameAliases != null ) {
            for ( String a : nameAliases ) {
                if ( !current.contains( a )) current.add( a );
            }
            setNameAliases( current );
        }
    }
}
