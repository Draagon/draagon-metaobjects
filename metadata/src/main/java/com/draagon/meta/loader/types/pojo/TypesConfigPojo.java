package com.draagon.meta.loader.types.pojo;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.ValueException;
import com.draagon.meta.loader.model.MetaModelBuilder;
import com.draagon.meta.loader.types.TypeConfig;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.pojo.PojoObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Stores the MetaData Configuration data
 */
public class TypesConfigPojo extends PojoObject implements TypesConfig {

    //public TypesConfigPojo() {
    //    super( TypesConfigBuilder.createTypesConfig() );
    //}

    private List<TypeConfig> types = null;

    public TypesConfigPojo(MetaObject mo ) {
        super( mo );
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    @Override
    public List<TypeConfig> getTypes() {
        return types;
    }

    @Override
    public void setTypes(List<TypeConfig> types) {
        this.types=types;
    }

    @Override
    public TypeConfig addType(TypeConfig typeConfig) {

        if ( typeConfig == null ) throw new MetaDataException( "Cannot add TypeConfig with a null value" );

        //typeConfig.validate();

        TypeConfig existing = getTypeByName( typeConfig.getName() );
        if ( existing != null ) throw new MetaDataException( "Type [" + typeConfig.getName() + "] "
                + "already exists as class [" + existing.getBaseClass() + "]" );

        List<TypeConfig> types = getTypes();
        if ( types == null ) {
            types = new ArrayList<>();
            setTypes( types );
        }
        types.add( typeConfig );

        return typeConfig;
    }

    public TypeConfig getTypeByName(String name ) {
        if ( getTypes() == null ) return null;
        for ( TypeConfig tc : getTypes() ) {
            if ( tc.getName().equals( name )) return tc;
        }
        return null;
    }

    public Collection<String> getTypeNames() {
        List<String> names = new ArrayList<>();
        for ( TypeConfig tc : getTypes() ) {
            names.add( tc.getName() );
        }
        return names;
    }

    @Override
    public MetaData getGeneratedMetaModel() {
        return MetaModelBuilder.buildDefaultMetaDataModels();
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    @Override
    public void validate() throws ValueException {
        super.validate();
        if (getTypes() == null ) throw new InvalidValueException( "types field is null on TypeConfig "+toString() );
        getTypes().forEach( tc -> { tc.validate(); });
    }


    /////////////////////////////////////////////////////////////////////
    // TODO:  Replace all of these once the new version exists on the core side

    @Override
    public void addOrMergeType(TypeConfig tc) {
        TypeConfig tc2 = getTypeByName( tc.getName() );
        if ( tc2 == null ) {
            addType( tc );
        } else {
            tc2.merge( tc );
        }
    }

    @Override
    public TypeConfig createAndAddType(String typeName, String baseClass ) {

        if (typeName == null)
            throw new InvalidValueException("Cannot create a TypeModel with a null name and class [" + baseClass + "]");
        if (baseClass == null)
            throw new InvalidValueException("Cannot create TypeModel for type [" + typeName + "] with a null Class");

        TypeConfig tc = new TypeConfigPojo(_getLoader().getMetaObjectByName(TypeConfig.OBJECT_NAME));
        tc.setName(typeName);
        tc.setBaseClass(baseClass);

        return addType(tc);
    }

    
    /////////////////////////////////////////////////////////////////////
    // Misc methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypesConfigPojo that = (TypesConfigPojo) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types);
    }

    @Override
    public String toString() {
        return "TypesConfigPojo{" +
                "types=" + types +
                '}';
    }
}
