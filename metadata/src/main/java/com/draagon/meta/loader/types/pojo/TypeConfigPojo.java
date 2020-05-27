package com.draagon.meta.loader.types.pojo;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.ValueException;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.loader.types.ChildConfig;
import com.draagon.meta.loader.types.SubTypeConfig;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Used to store the MetaData Type config and respective SubTypes and their classes */
public class TypeConfigPojo extends PojoObject implements TypeConfig {
    
    private String name = null;
    private String baseClass = null;
    private List<SubTypeConfig> subTypes = null;
    private List<ChildConfig> children = null;

    public TypeConfigPojo(MetaObject mo) {
        super(mo);
    }

    /////////////////////////////////////////////////////////////////////
    // Type  Methods

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public String getBaseClass() {
        return baseClass;
    }

    @Override
    public void setBaseClass(String baseClass) {
        this.baseClass=baseClass;
    }
    
    @Override
    public List<SubTypeConfig> getSubTypes() {
        return subTypes;
    }

    @Override
    public void setSubTypes(List<SubTypeConfig> subTypes) {
        this.subTypes=subTypes;
    }

    @Override
    public List<ChildConfig> getTypeChildConfigs() {
        return children;
    }

    @Override
    public void setTypeChildConfigs(List<ChildConfig> children) {
        this.children=children;
    }


    /////////////////////////////////////////////////////////////////////
    // Helper methods

    @Override
    public Class<? extends MetaData> getMetaDataClass() {
        try {
            return getMetaData().loadClass( MetaData.class, getBaseClass() );
        } catch (ClassNotFoundException e) {
            throw new InvalidValueException( "BaseClass ["+getBaseClass()+"] not found on Type ["+getName()+"]");
        }
    }

    public void addTypeChildConfig( ChildConfig config ) {
        if ( getTypeChildConfigs() == null) {
            setTypeChildConfigs(new ArrayList<>());
        }
        mergeChildConfig( getTypeChildConfigs(), config );
    }

    public SubTypeConfig getSubType(String name ) {
        if ( getSubTypes() != null ) {
            for (SubTypeConfig tc : getSubTypes()) {
                if (tc.getName().equals(name)) return tc;
            }
        }
        return null;
    }

    public Collection<String> getSubTypeNames() {
        List<String> names = new ArrayList<>();
        for ( SubTypeConfig tc : getSubTypes() ) {
            names.add( tc.getName() );
        }
        return names;
    }

    public void addSubTypeConfig( SubTypeConfig subType ) {
        if ( getSubType( subType.getName() ) != null ) {
            throw new IllegalStateException( "SubType with the same name ["+subType.getName()+"] already "+
                    "exists on Type ["+ getName()+"]");
        }
        if ( subTypes == null ) {
            subTypes = new ArrayList<>();
            setSubTypes(subTypes);
        }
        subTypes.add(subType);
    }

    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side
    
    private String defaultName=null;
    private String defaultNamePrefix=null;
    private String defaultSubTypeName=null;

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void setDefaultName( String defName ) {
        this.defaultName=defName;
    }

    @Override
    public String getDefaultNamePrefix() {
        return defaultNamePrefix;
    }

    @Override
    public String getDefaultSubType() {
        return defaultSubTypeName;
    }

    @Override
    public void setDefaultNamePrefix( String defNamePrefix ) {
        this.defaultNamePrefix=defNamePrefix;
    }

    @Override
    public void setDefaultSubType(String subTypeName ) {
        this.defaultSubTypeName = subTypeName;
    }


    /////////////////////////////////////////////////////////////////////
    // Merge methods

    public void merge(TypeConfig tc ) {

        // Merge TypeConfig fields
        if (tc.getBaseClass() != null) setBaseClass(tc.getBaseClass());
        if (tc.getDefaultName() != null) setDefaultName(tc.getDefaultName());

        // TODO: Ensure both NAME and PREFIX are not set
        if (tc.getDefaultNamePrefix() != null) setDefaultName(tc.getDefaultNamePrefix());
        if (tc.getDefaultSubType() != null) setDefaultSubType(tc.getDefaultSubType());

        // Merge SubTypeConfigs
        for( SubTypeConfig stc : tc.getSubTypes() ) {
            SubTypeConfig existing = getSubType( stc.getName() );
            if ( existing != null ) {
                // Merge SubType Fields
                if ( stc.getBaseClass() != null ) existing.setBaseClass( stc.getBaseClass());
                // Merge SubType ChildConfigs
                if ( existing.getChildConfigs() == null ) {
                    existing.setChildConfigs( new ArrayList<>());
                }
                mergeChildConfigs( existing.getChildConfigs(), stc.getChildConfigs() );
            }
            else {
                // Add SubTypeConfig
                addSubTypeConfig( stc );
            }
        }

        // Merge Type ChildConfigs
        if ( getTypeChildConfigs() == null ) setTypeChildConfigs( new ArrayList<>() );
        mergeChildConfigs( getTypeChildConfigs(), tc.getTypeChildConfigs() );
    }

    protected void mergeChildConfigs(List<ChildConfig> existing, List<ChildConfig> children) {

        for( ChildConfig cc : children ) {
            // TODO:  Merge same children together
            mergeChildConfig( existing, cc );
        }
    }

    protected void mergeChildConfig(List<ChildConfig> existing, ChildConfig cc ) {

        ChildConfig match = getSameChildConfig( existing, cc );
        if ( match == null ) {
            existing.add( cc );
        }
        else {
            if ( cc.getNameAliases() != null && !cc.getNameAliases().isEmpty())
                match.setNameAliases( cc.getNameAliases());
            // TODO: Merge more fields, and need to merge aliases
        }
    }

    protected ChildConfig getSameChildConfig(List<ChildConfig> existing, ChildConfig cc ) {
        if ( existing != null ) {
            for (ChildConfig ecc : existing) {
                if (ecc.getType().equals(cc.getType())
                        && ecc.getSubType().equals(cc.getSubType())
                        && ecc.getName().equals(cc.getName())) {
                    return ecc;
                }
            }
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////
    // Child Config methods

    public ChildConfig getBestMatchChildConfig(List<ChildConfig> children, String type, String subType, String name ) {

        if (children == null ) return null;

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

    protected ChildConfig getExactMatchChildConfig(List<ChildConfig> children, ChildConfig in) {

        if ( children != null ) {
            for ( ChildConfig cc : children ) {
                if ( cc.getType().equals( in.getType() )
                    && cc.getSubType().equals( in.getSubType() )
                    && cc.getName().equals( in.getName() )) {
                    return cc;
                }
            }
        }
        return null;
    }


    /////////////////////////////////////////////////////////////////////
    // SubType  Methods

    public List<ChildConfig> getSubTypeChildConfigs(String subTypeName ) {
        SubTypeConfig subType = getSubType( subTypeName );
        List<ChildConfig> children = subType.getChildConfigs();
        if ( children == null ) {
            children = new ArrayList<>();
            subType.setChildConfigs( children );
        }
        return children;
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

    public void addSubTypeConfig(String subtypeName, String baseClass ) {

        if ( subtypeName == null ) throw new NullPointerException( "Cannot add subType on type ["+ getName()+"] with a null name and class [" + baseClass + "]" );
        if ( baseClass == null ) throw new NullPointerException( "Cannot add subType [" +subtypeName + "] on type ["+ getName()+"] with a null Class" );

        SubTypeConfig existing = getSubType( subtypeName );
        if ( existing != null ) {
            Class<? extends MetaData> c = getSubTypeClass( subtypeName );
            if ( !c.getName().equals( baseClass )) {
                throw new MetaDataException("Cannot add SubType [" + subtypeName + "] to Type [" + getName() + "], "+
                        "already existed but the classes do not match ["+c.getName()+" != "+baseClass+"]");
            }
        }
        else {
            SubTypeConfig c = SubTypeConfigPojo.create( getMetaData().getLoader(), subtypeName, baseClass );
            addSubTypeConfig( c );
        }
    }


    public Class<? extends MetaData> getSubTypeClass( String subTypeName ) {
        if ( subTypeName == null ) throw new MetaDataException( "Cannot get subType on type ["+ getName()+"] with a null value" );
        if ( getSubType( subTypeName ) == null ) throw new MetaDataException( "SubType ["+subTypeName+"] does not exist on Type ["+ getName()+"]" );
        return getSubType( subTypeName ).getMetaDataClass();
    }


    public Class<? extends MetaData> getDefaultTypeClass() {
        return getSubTypeClass(getDefaultSubType());
    }

    public ChildConfig createChildConfig(String type, String subType, String name) {
        ChildConfig cc = new ChildConfigPojo(getMetaData().getLoader().getMetaObjectByName(ChildConfig.OBJECT_NAME));
        cc.setType(type);
        cc.setSubType(subType);
        cc.setName(name);
        return cc;
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() throws ValueException {

        super.validate();

        if ( getName() == null )
            throw new InvalidValueException( "TypeConfig has null name" );
        if ( getBaseClass() == null )
            throw new InvalidValueException( "TypeConfig ["+ getName()+"] has null BaseClass" );

        if (!getName().equals(MetaModel.OBJECT_NAME)
                && (getSubTypes() == null || getSubTypes().isEmpty() ))
            throw new MetaDataException( "No SubTypes existed for type ["+ getName()+"]" );

        if ( getDefaultSubType() != null && getSubTypeClass( getDefaultSubType() ) == null )
            throw new MetaDataException( "Default subType [" + getDefaultSubType()+ "] on type ["+ getName()+"] was not found" );

        if ( getSubTypes() != null ) getSubTypes().forEach( stc -> stc.validate() );
        if ( getTypeChildConfigs() != null ) getTypeChildConfigs().forEach( cc -> cc.validate() );
    }


    /////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeConfigPojo that = (TypeConfigPojo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(baseClass, that.baseClass) &&
                Objects.equals(subTypes, that.subTypes) &&
                Objects.equals(children, that.children) &&
                Objects.equals(defaultName, that.defaultName) &&
                Objects.equals(defaultNamePrefix, that.defaultNamePrefix) &&
                Objects.equals(defaultSubTypeName, that.defaultSubTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, baseClass, subTypes, children, defaultName, defaultNamePrefix, defaultSubTypeName);
    }

    @Override
    public String toString() {
        return "TypeConfigPojo{" +
                "name='" + name + '\'' +
                ", baseClass=" + baseClass +
                ", subTypes=" + subTypes +
                ", children=" + children +
                ", defaultName='" + defaultName + '\'' +
                ", defaultNamePrefix='" + defaultNamePrefix + '\'' +
                ", defaultSubTypeName='" + defaultSubTypeName + '\'' +
                '}';
    }
}
