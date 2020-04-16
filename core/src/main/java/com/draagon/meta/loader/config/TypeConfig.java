package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;

import java.util.Map;
import java.util.TreeMap;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class TypeConfig {

    private final String typeName;
    private final Class<? extends MetaData> baseClass;
    private final Map<String,Class<? extends MetaData>> subTypes = new TreeMap<>();
    private String defSubTypeName = null;

    public TypeConfig( String typeName, Class<? extends MetaData> baseClass ) {
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

        if ( subtypeName == null ) throw new NullPointerException( "Cannot add subType with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot add subType [" +subtypeName + "] with a null Class" );

        subTypes.put( subtypeName, clazz );
        if ( def ) defSubTypeName = subtypeName;
    }

    public Class<? extends MetaData> getSubTypeClass( String subTypeName ) {
        if ( defSubTypeName == null ) throw new NullPointerException( "Cannot get subType with a null value" );
        return subTypes.get( subTypeName );
    }

    public void setDefaultSubTypeName(String subTypeName ) {

        if ( defSubTypeName == null )
            throw new NullPointerException( "Cannot set default subType with a null" );

        if ( getSubTypeClass( defSubTypeName ) == null )
            throw new IllegalArgumentException( "Cannot set default subType [" +defSubTypeName+ "], subType with that name was not found" );

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

    public String toString() {
        return "TypeConfig {typeName="+typeName+",baseClass="+baseClass+",subTypes="+subTypes.toString()+",defSubTypeName="+defSubTypeName+"}";
    }
}
