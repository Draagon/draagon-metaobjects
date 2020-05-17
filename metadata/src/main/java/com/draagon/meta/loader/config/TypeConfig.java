package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.MetaObject;

import java.util.*;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class TypeConfig extends ConfigObjectAbstract {
    
    public final static String OBJECT_NAME       = "TypeConfig";
    public final static String OBJECT_IONAME    = "type";
    public final static String FIELD_SUBTYPES    = "subTypes";
    public final static String OBJREF_SUBTYPE    = "subTypeRef";
    public final static String FIELD_DEFSUBTYPE  = "defaultSubType";
    public final static String FIELD_DEFNAME     = "defaultName";
    public final static String FIELD_DEFPREFIX   = "defaultNamePrefix";

    public TypeConfig( MetaObject mo ) {
        super(mo);
    }

    /////////////////////////////////////////////////////////////////////
    // Type  Methods

    public String getTypeName() {
        return getString( FIELD_TYPE );
    }

    public Class<? extends MetaData> getBaseClass() {
        return _getClass( MetaData.class, FIELD_BASECLASS );
    }

    public List<ChildConfig> getTypeChildConfigs() {
        return _getObjectArray( ChildConfig.class, FIELD_CHILDREN );
    }

    public void addChildConfig( ChildConfig config ) {
        mergeChildConfigs( FIELD_CHILDREN, config );
    }

    public String getDefaultName() {
        return getString( FIELD_DEFNAME );
    }

    public String getDefaultNamePrefix() {
        return getString( FIELD_DEFPREFIX );
    }

    public String getDefaultSubTypeName() {
        return getString( FIELD_DEFSUBTYPE );
    }

    public void setDefaultSubTypeName(String subTypeName ) {
        _setString( FIELD_DEFSUBTYPE, _trimStringToNull( subTypeName ));
    }

    public void setDefaultName( String defName ) {
        _setString( FIELD_DEFNAME, _trimStringToNull( defName ));
    }

    public void setDefaultNamePrefix( String defNamePrefix ) {
        _setString( FIELD_DEFPREFIX, _trimStringToNull( defNamePrefix ));
    }

    public SubTypeConfig getSubType( String name ) {
        for ( SubTypeConfig tc : getSubTypes() ) {
            if ( tc.getTypeName().equals( name )) return tc;
        }
        return null;
    }

    public Collection<String> getTypeNames() {
        List<String> names = new ArrayList<>();
        for ( SubTypeConfig tc : getSubTypes() ) {
            names.add( tc.getTypeName() );
        }
        return names;
    }

    public Collection<SubTypeConfig> getSubTypes() {
        return _getObjectArray( SubTypeConfig.class, FIELD_SUBTYPES);
    }

    public void addSubTypeConfig( SubTypeConfig subType ) {
        _addToObjectArray( FIELD_SUBTYPES, subType );
    }
    
    /////////////////////////////////////////////////////////////////////
    // Child Config methods

    public ChildConfig getBestMatchChildConfig( List<ChildConfig> children, String type, String subType, String name ) {

        ChildConfig out = null;
        for ( ChildConfig cc: children ) {

            // If the types don't match continue
            if ( !type.equals( cc.getType())) continue;
            // If the subtypes don't match continue
            if ( subType != null && !subType.equals(cc.getSubType()) && !cc.getSubType().equals("*")) continue;
            // If the names don't match continue
            if ( name != null && !hasMatchNameWithAliases( name, cc ) && !cc.getName().equals("*")) continue;

            // TODO:  Cleanup and verify this logic

            // If we already found one previously, see if this one is better
            if ( out != null ) {
                // If the name on the current one is not * and the new one is a *, then continue
                if ( name != null && !out.getName().equals( "*" ) && cc.getName().equals( "*" )) continue;
                // If the subtype on the current one is not * and the new one is a *, then continue
                if ( subType != null && !out.getSubType().equals( "*" ) && cc.getSubType().equals( "*" )) continue;
                // If name and subtype are not null and it matches the new one, use that one
                if ( name != null && subType != null
                        && (!hasMatchNameWithAliases( name, out ) || !subType.equals( cc.getSubType() ))) continue;
            }

            out = cc;
        }

        return out;
    }

    protected boolean hasMatchNameWithAliases( String name, ChildConfig cc ) {

        if ( name.equals( cc.getName() )) return true;
        if ( cc.getNameAliases() != null ) {
            for (String alias : cc.getNameAliases()) {
                if (name.equals(alias)) return true;
            }
        }
        return false;
    }

    protected ChildConfig getExactMatchChildConfig( List<ChildConfig> children, ChildConfig in) {

        for ( ChildConfig cc : children ) {
            if ( cc.getType().equals( in.getType() )
                && cc.getSubType().equals( in.getSubType() )
                && cc.getName().equals( in.getName() )) {
                return cc;
            }
        }
        return null;
    }


    /////////////////////////////////////////////////////////////////////
    // SubType  Methods

    public List<ChildConfig> getSubTypeChildConfigs( String subType ) {
        return getSubType( subType ).getChildConfigs();
    }

    public void addSubTypeChild( String subTypeName, ChildConfig config ) {
        List<ChildConfig> children = getSubTypeChildConfigs(subTypeName);
        ChildConfig cc = getExactMatchChildConfig(children, config);
        if ( cc != null ) {
            cc.merge(config);
        } else {
            children.add(config);
        }
    }

    public void addSubType( String subtypeName, Class<? extends MetaData> clazz ) {

        if ( subtypeName == null ) throw new NullPointerException( "Cannot add subType on type ["+getTypeName()+"] with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot add subType [" +subtypeName + "] on type ["+getTypeName()+"] with a null Class" );

        SubTypeConfig existing = getSubType( subtypeName );
        if ( existing != null ) {
            Class<? extends MetaData> c = existing.getBaseClass();
            if ( !c.equals( clazz )) {
                throw new MetaDataException("Cannot add SubType [" + subtypeName + "] to Type [" + getTypeName() + "], "+
                        "already existed but the classes do not match ["+c.getName()+" != "+clazz.getName() +"]");
            }
        }
        else {
            SubTypeConfig c = SubTypeConfig.create( getMetaData().getLoader(), subtypeName, clazz );
            addSubTypeConfig( c );
        }
    }

    public Class<? extends MetaData> getSubTypeClass( String subTypeName ) {
        if ( subTypeName == null ) throw new MetaDataException( "Cannot get subType on type ["+getTypeName()+"] with a null value" );
        return getSubType( subTypeName ).getBaseClass();
    }


    public Class<? extends MetaData> getDefaultTypeClass() {
        return getSubType(getDefaultSubTypeName()).getBaseClass();
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {

        if ( getDefaultSubTypeName() != null && getSubTypeClass( getDefaultSubTypeName() ) == null )
            throw new MetaDataException( "Default subType [" +getDefaultSubTypeName()+ "] on type ["+getTypeName()+"] was not found" );

        if ( getSubTypes() == null && getSubTypes().isEmpty() )
            throw new MetaDataException( "No SubTypes existed for type ["+getTypeName()+"]" );
    }
}
