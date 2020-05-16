package com.draagon.meta.loader.typed.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;

import java.util.*;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class TypeConfig {

    private final String typeName;
    private final Class<? extends MetaData> baseClass;
    private final List<ChildConfig> typeChildren = new ArrayList<>();
    private final Map<String,Class<? extends MetaData>> subTypes = new TreeMap<>();
    private final Map<String,List<ChildConfig>> subTypeChildren = new TreeMap<>();

    private String defSubTypeName = null;
    private String defName = null;
    private String defNamePrefix = null;

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

    public List<ChildConfig> getTypeChildConfigs() {
        return typeChildren;
    }

    public void addTypeChild( ChildConfig config ) {
        // TODO:  Merge same children together
        typeChildren.add( config );
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

    protected List<ChildConfig> getOrCreateSubTypeChildren( String subType ) {
        List<ChildConfig> children = null;
        synchronized ( subTypeChildren ) {
             children = subTypeChildren.get(subType);
            if (children == null) {
                children = new ArrayList<>();
                subTypeChildren.put( subType, children );
            }
        }
        return children;
    }

    /////////////////////////////////////////////////////////////////////
    // SubType  Methods

    public List<ChildConfig> getSubTypeChildConfigs( String subType ) {
        return subTypeChildren.get( subType );
    }

    public void addSubTypeChild( String subType, ChildConfig config ) {
        List<ChildConfig> children = getOrCreateSubTypeChildren(subType);
        ChildConfig cc = getExactMatchChildConfig(children, config);
        if ( cc != null ) {
            cc.merge(config);
        } else {
            children.add(config);
        }
    }

    public void addSubType( String subtypeName, Class<? extends MetaData> clazz ) {

        if ( subtypeName == null ) throw new NullPointerException( "Cannot add subType on type ["+typeName+"] with a null name and class [" + clazz + "]" );
        if ( clazz == null ) throw new NullPointerException( "Cannot add subType [" +subtypeName + "] on type ["+typeName+"] with a null Class" );

        if ( subTypes.containsKey( subtypeName )) {
            Class<? extends MetaData> c = subTypes.get( subtypeName );
            if ( !c.equals( clazz )) {
                throw new MetaDataException("On Type [" + typeName + "], subType [" + subtypeName + "] already existed, but the classes do not match ["+c.getName()+" != "+clazz.getName() +"]");
            }
        }
        else {
            subTypes.put(subtypeName, clazz);
            subTypeChildren.put(subtypeName, new ArrayList<ChildConfig>());
        }
    }

    public Class<? extends MetaData> getSubTypeClass( String subTypeName ) {
        if ( subTypeName == null ) throw new MetaDataException( "Cannot get subType on type ["+typeName+"] with a null value" );
        return subTypes.get( subTypeName );
    }

    public void setDefaultSubTypeName(String subTypeName ) {
        if ( subTypeName.equals("")) subTypeName = null;
        defSubTypeName = subTypeName;
    }

    public String getDefaultSubTypeName() {
        return defSubTypeName;
    }

    public void setDefaultName( String defName ) {
        if ( defName.equals("")) defName = null;
        this.defName = defName;
    }

    public String getDefaultName() {
        return defName;
    }

    public void setDefaultNamePrefix( String defNamePrefix ) {
        if ( defNamePrefix.equals("")) defNamePrefix = null;
        this.defNamePrefix = defNamePrefix;
    }

    public String getDefaultNamePrefix() {
        return defNamePrefix;
    }

    public Class<? extends MetaData> getDefaultTypeClass() {

        //if ( defSubTypeName == null )
         //   return subTypes.values().iterator().next();

        return getSubTypeClass(defSubTypeName);
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {

        if ( defSubTypeName != null && getSubTypeClass( defSubTypeName ) == null )
            throw new MetaDataException( "Default subType [" +defSubTypeName+ "] on type ["+typeName+"] was not found" );

        if ( subTypes.size() == 0 )
            throw new MetaDataException( "No SubTypes existed for type ["+typeName+"]" );
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
                Objects.equals(defSubTypeName, that.defSubTypeName) &&
                Objects.equals(defName, that.defName) &&
                Objects.equals(defNamePrefix, that.defNamePrefix) &&
                Objects.equals(typeChildren, that.typeChildren) &&
                Objects.equals(subTypeChildren, that.subTypeChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, baseClass, subTypes, defSubTypeName, defName, defNamePrefix, typeChildren, subTypeChildren);
    }

    @Override
    public String toString() {
        return "TypeModel {typeName="+typeName+",baseClass="+baseClass+",subTypes="+subTypes.toString()+"}";
                //+",defSubTypeName="+defSubTypeName+",defName="+defName+",defNamePrefix="+defNamePrefix
                //+",typeChildren="+typeChildren+",subTypeChildren="+subTypeChildren+"}";
    }
}