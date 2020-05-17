package com.draagon.meta.loader.config;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.util.*;

/**
 * Stores the MetaData Configuration data
 */
public class TypesConfig extends ConfigObjectAbstract {

    public final static String OBJECT_NAME      = "TypesConfig";
    public final static String OBJECT_IONAME   = "typesConfig";
    public final static String FIELD_TYPES      = "types";
    public final static String OBJREF_TYPE      = "typeRef";

    public TypesConfig( MetaObject mo ) {
        super( mo );
    }

    public static TypesConfig create( MetaDataLoader loader) {
        return _newInstance( TypesConfig.class, loader, OBJECT_NAME );
    }

    /////////////////////////////////////////////////////////////////////
    // Type Configuration Methods

    public Collection<TypeConfig> getTypes() {
        return _getObjectArray( TypeConfig.class, FIELD_TYPES);
    }

    public TypeConfig getType( String name ) {
        for ( TypeConfig tc : getTypes() ) {
            if ( tc.getTypeName().equals( name )) return tc;
        }
        return null;
    }

    public Collection<String> getTypeNames() {
        List<String> names = new ArrayList<>();
        for ( TypeConfig tc : getTypes() ) {
            names.add( tc.getTypeName() );
        }
        return names;
    }

    //////////////////////////////////////////////////////////////////////
    // Validation Method

    public void validate() {
        for ( TypeConfig tc : getTypes() ) tc.validate();
    }
}
