package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class TypeConfig {

    private final String typeName;
    private final Class<? extends MetaData> baseClass;
    private final Map<String,Class<? extends MetaData>> subTypes = new TreeMap<>();
    private String defSubTypeName = null;

    public TypeConfig(String typeName, Class<? extends MetaData> baseClass ) {
        this.typeName = typeName;
        this.baseClass = baseClass;
    }

    /////////////////////////////////////////////////////////////////////
    // Type  Methods

    public String getTypeName() {
        return typeName;
    }

    public Class<? extends MetaData> getBaseClass() {
        return this.baseClass;
    }

    /////////////////////////////////////////////////////////////////////
    // SubType  Methods

    public void addSubType( String subtypeName, Class<? extends MetaData> clazz, boolean def ) {

        if ( subtypeName == null ) throw new NullPointerException( "Cannot add subType on type ["+typeName+"] with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot add subType [" +subtypeName + "] on type ["+typeName+"] with a null Class" );

        subTypes.put( subtypeName, clazz );
        if ( def ) defSubTypeName = subtypeName;
    }

    public Class<? extends MetaData> getSubTypeClass( String subTypeName ) {
        if ( defSubTypeName == null ) throw new MetaDataException( "Cannot get subType on type ["+typeName+"] with a null value" );
        return subTypes.get( subTypeName );
    }

    public void setDefaultSubTypeName(String subTypeName ) {

        if ( defSubTypeName == null )
            throw new MetaDataException( "Cannot set default subType on type ["+typeName+"] with a null value" );

        if ( getSubTypeClass( defSubTypeName ) == null )
            throw new MetaDataException( "Cannot set default subType [" +defSubTypeName+ "] on type ["+typeName+"], subType with that name was not found" );

        defSubTypeName = subTypeName;
    }

    public String getDefaultSubTypeName() {
        return defSubTypeName;
    }

    public Class<? extends MetaData> getDefaultTypeClass() {

        if ( defSubTypeName == null )
            return subTypes.values().iterator().next();

        return getSubTypeClass(defSubTypeName);
    }

    /////////////////////////////////////////////////////////////////////
    // Misc Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeConfig that = (TypeConfig) o;
        return Objects.equals(typeName, that.typeName) &&
                Objects.equals(baseClass, that.baseClass) &&
                Objects.equals(subTypes, that.subTypes) &&
                Objects.equals(defSubTypeName, that.defSubTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, baseClass, subTypes, defSubTypeName);
    }

    @Override
    public String toString() {
        return "TypeModel {typeName="+typeName+",baseClass="+baseClass+",subTypes="+subTypes.toString()+",defSubTypeName="+defSubTypeName+"}";
    }
}
