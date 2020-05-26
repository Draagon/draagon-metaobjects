package com.draagon.meta.loader.types.pojo;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.ValueException;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoObject;

import java.util.List;
import java.util.Objects;

public class ChildConfigPojo extends PojoObject implements ChildConfig {

    private String type = null;
    private String subType = null;
    private String name = null;
    private List<String> nameAliases = null;

    private String createdFromFile = null;

    public ChildConfigPojo(MetaObject mo ) {
        super(mo);
    }

    ///////////////////////////////////////////////////////////
    // GETTERS AND SETTERS

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type=type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType=subType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public List<String> getNameAliases() {
        return nameAliases;
    }

    public void setNameAliases( List<String> nameAliases ) {
        this.nameAliases = nameAliases;
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

    /////////////////////////////////////////////////////////////////////
    // Validation method

    @Override
    public void validate() throws ValueException {
        if ( getType() == null || getSubType() == null || getName() == null ) {
            throw new InvalidValueException( "ChildConfig does not have type,subType, and/or name fields set: " + toString() );
        }
    }


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    @Override
    public void merge( ChildConfig cc ) {
        if ( !getType().equals( cc.getType() )
                || !getSubType().equals( cc.getSubType() )
                || !getName().equals( cc.getName() )) {
            throw new MetaDataException("Cannot merge when different type/subtype/name: ["+toString()+"]"+
                    " merged with ["+cc.toString()+"]");
        }
        if ( cc.getNameAliases() != null ) mergeNameAliases( cc.getNameAliases() );
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


    /////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChildConfigPojo that = (ChildConfigPojo) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(subType, that.subType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(nameAliases, that.nameAliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subType, name, nameAliases);
    }

    @Override
    public String toString() {
        return "ChildConfigPojo{" +
                "type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", name='" + name + '\'' +
                ", nameAliases=" + nameAliases +
                '}';
    }
}
