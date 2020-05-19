package com.draagon.meta.loader.model;

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.*;
import com.draagon.meta.loader.parser.ParserBase;

import java.util.List;

public abstract class MetaModelParser<I extends MetaDataLoader,S> extends ParserBase<MetaModelLoader,I,S> {

    private String defaultPackageName = null;

    protected MetaModelParser(MetaModelLoader modelLoader, String sourceName) {
        super(modelLoader, sourceName);
    }

    /** Set default package name */
    protected void setDefaultPackageName(String defPkg) {
        this.defaultPackageName = defPkg;
    }

    /** Set default package name */
    protected String getDefaultPackageName() {
        return defaultPackageName;
    }

    /* Load MetaDataModel Stream */
    public abstract void loadAndMerge( I intoLoader, S source );

    /** Merge the loaded MetaDataModel into the specified MetaDataLoader */
    protected void mergeMetaDataModel( I intoLoader, MetaModel model ) {

        TypesConfig typesConfig = intoLoader.getTypesConfig();
        if ( typesConfig == null ) throw new IllegalStateException( "MetaDataLoader did not have a TypesConfig set, loader=["+intoLoader+"]");
        TypeConfig tc = typesConfig.getType( model.getType() );
        if ( tc.getTypeName().equals( "metadata" )) {
            String defPkg = model.getPackage();
            if ( defPkg == null ) defPkg = "";
            mergeMetaData( intoLoader, defPkg, model, typesConfig, true );
        }
    }

    protected void mergeMetaData(MetaData parent, String pkgDef, MetaModel model,
                                 TypesConfig typesConfig, boolean isRoot ) {

        List<MetaModel> children = model.getChildren();
        if ( children == null ) return;

        for ( MetaModel child : children ) {

            String type = child.getType();
            String subType = child.getSubType();
            String name = child.getName();
            String pkg = child.getPackage();

            if ( type == null ) {
                throw new InvalidValueException( "Type is null, cannot merge MetaDataModel ["+child+"]" );
            }

            TypeConfig tc = typesConfig.getType(type);
            if ( tc == null ) {
                throw new InvalidValueException( "Type ["+type+"] did not exist in TypesConfig, "+
                        "cannot merge MetaDataModel ["+child+"]" );
            }

            SubTypeConfig stc = null;
            if ( subType == null && tc.getDefaultSubTypeName() != null ) {
                subType = tc.getDefaultSubTypeName();
            }
            if ( subType != null ) {
                stc = tc.getSubType(subType);
            }

            if ( name == null ) {
                if ( tc.getDefaultName() != null ) {
                    name = tc.getDefaultName();
                } else if ( tc.getDefaultNamePrefix() != null ) {
                    // TODO: Make this increment!
                    name = tc.getDefaultNamePrefix()+"1";
                }
                else {
                    throw new InvalidValueException( "Name is null, cannot merge MetaDataModel ["+child+"]" );
                }
            }

            String fullname = name;
            if (isRoot && pkg == null) pkg = pkgDef;
            if (isRoot && !pkg.isEmpty()) fullname = pkg + "::" + name;

            MetaData merge = null;
            try {
                merge = parent.getChildOfType(type, fullname);

                // TODO:  Merge!
            }
            catch( MetaDataNotFoundException e ) {

                if ( stc == null ) {
                    throw new InvalidValueException( "SubType ["+subType+"] did not exist in TypesConfig, "+
                            "cannot merge MetaDataModel ["+child+"]" );
                }

                Class<? extends MetaData> clazz = stc.getBaseClass();
                merge = (MetaData) parent.newInstanceFromClass( clazz, type, subType, fullname );
                parent.addChild( merge );
            }

            // Merge child records
            mergeMetaData( merge, pkgDef, child, typesConfig, false );
        }
    }
}
