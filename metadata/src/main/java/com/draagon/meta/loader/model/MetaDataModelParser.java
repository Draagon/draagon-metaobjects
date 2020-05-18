package com.draagon.meta.loader.model;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.config.*;
import com.draagon.meta.loader.parser.ParserBase;

import java.util.List;

public abstract class MetaDataModelParser<I extends MetaDataLoader,S> extends ParserBase<MetaDataModelLoader,I,S> {

    private String defaultPackageName = null;

    protected MetaDataModelParser(MetaDataModelLoader modelLoader, String sourceName) {
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
    protected void mergeMetaDataModel( I intoLoader, MetaDataModel model ) {

        TypesConfig typesConfig = intoLoader.getTypesConfig();
        if ( typesConfig == null ) throw new IllegalStateException( "MetaDataLoader did not have a TypesConfig set, loader=["+intoLoader+"]");
        TypeConfig tc = typesConfig.getType( model.getType() );
        if ( tc.getTypeName().equals( "metadata" )) {
            String defPkg = model.getPackage();
            if ( defPkg == null ) defPkg = "";
            buildMetaData( intoLoader, defPkg, model, typesConfig, true );
        }
    }

    protected void buildMetaData(MetaData parent, String pkgDef, MetaDataModel model,
                                      TypesConfig typesConfig, boolean isRoot ) {

        List<MetaDataModel> children = model.getChildren();
        for ( MetaDataModel md : children ) {

            String type = md.getType();
            String subType = md.getSubType();
            String name = md.getName();
            String pkg = md.getPackage();
            String fullname = name;

            if (isRoot && pkg.equals(null)) pkg = pkgDef;
            if (isRoot && !pkg.isEmpty()) fullname = pkg + "::" + name;

            TypeConfig tc = typesConfig.getType(type);
            SubTypeConfig stc = tc.getSubType(subType);

            MetaData parentChild = parent.getChildOfType( type, fullname );
            if ( parentChild == null ) {
                Class<? extends MetaData> clazz = stc.getBaseClass();
                MetaData mdNew = (MetaData) parent.newInstanceFromClass( clazz, type, subType, fullname );
            }
        }
    }
}
