package com.draagon.meta.loader.model;

import com.draagon.meta.*;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.types.*;
import com.draagon.meta.loader.parser.ParserBase;
import com.draagon.meta.util.MetaDataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public abstract class MetaModelParser<TSC extends TypesConfig, I extends MetaDataLoader, S> extends ParserBase<MetaModelLoader,I,S> {

    private Log log = LogFactory.getLog( this.getClass() );

    private String defaultPackageName = null;

    protected MetaModelParser(MetaModelLoader loader, ClassLoader classLoader, String sourceName) {
        super(loader, classLoader, sourceName);
    }

    /* Load MetaDataModel Stream */
    public abstract void loadAndMerge( I intoLoader, S source );

    /** Merge the loaded MetaDataModel into the specified MetaDataLoader */
    protected <M extends MetaModel> void mergeMetaDataModel( I intoLoader, M model ) {

        TSC typesConfig = intoLoader.getTypesConfig();
        if ( typesConfig == null ) throw new IllegalStateException( "MetaDataLoader did not have a TypesConfig set, loader=["+intoLoader+"]");

        // Get the TypeConfig for the root model (throws exception)
        TypeConfig tc = getTypeConfig( typesConfig, model );

        // Set The default Package from the root MetaModel
        String defPkg = getDefaultPackageName( tc, model );

        // Begin merging the MetaModels into the Laoder
        mergeMetaData( intoLoader, defPkg, typesConfig, model );
    }

    /** Set default package name */
    protected <T extends TypeConfig, M extends MetaModel> String getDefaultPackageName( T typeConfig, M model ) {

        String defPkg = model.getPackage();
        if ( defPkg == null ) defPkg = "";
        return defPkg;
    }

    /** Merge the MetaModels into the parent MetaData */
    protected <M extends MetaModel> void mergeMetaData(
            MetaData parent, String pkgDef, TSC typesConfig, M model) {

        List<M> children = (List<M>) model.getChildren();
        if ( children == null ) return;

        for ( M child : children ) {

            // Attempt to get TypeConfig and return null if no type was found
            TypeConfig tc = getTypeConfig( typesConfig, child );

            // Create or Overload MetaData from the Model
            MetaData merge = createOrOverloadMetaData(parent, pkgDef, tc, child );

            // Merge additional records from the Model
            mergeAdditionalModelData( parent, merge, tc, child );

            // Merge child records
            mergeMetaData( merge, pkgDef, typesConfig, child );
        }
    }

    protected <T extends TypeConfig, M extends MetaModel> void mergeAdditionalModelData(
            MetaData parent, MetaData merge, TypeConfig tc, M model) {

        // Merge the Value for MetaDataValueHandlers like MetaAttribute
        if ( merge instanceof MetaDataValueHandler && model.getValue() != null ) {
            ((MetaDataValueHandler)merge).setValueAsObject(model.getValue());
        }
    }

    protected <M extends MetaModel> MetaData createOrOverloadMetaData(
            MetaData parent, String pkgDef, TypeConfig tc, M model) {

        String superName = getFullSuperName(parent, tc, pkgDef, model);

        // If the name is null, then try to set it
        String fullname = getFullname(parent, tc, pkgDef, model);

        // Create or Overload MetaData from Model with specified superName and fullname
        return createOrOverloadMetaDataWithName(parent, tc, model, superName, fullname);
    }

    protected <T extends TypeConfig, M extends MetaModel> String getFullSuperName(
            MetaData parent, T tc, String pkgDef, M model) {

        if (model.getSuper() == null) return null;

        // If the parent is not the MetaDataLoader, then see if the parent package is different
        // and if so, use that to reference the superData
        String superPkgDef = pkgDef;
        if (!(parent instanceof MetaDataLoader)) {
            String p = MetaDataUtil.findPackageForMetaData(parent);
            if (!p.isEmpty()) { //&& !p.equals( pkgDef ))  {
                superPkgDef = p;
            }
        }

        // TODO:  Add support for pointing to sub children with . notation

        if (model.getSuper().contains(MetaDataLoader.PKG_SEPARATOR))
            return MetaDataUtil.expandPackageForMetaDataRef(superPkgDef, model.getSuper());
        else
            return superPkgDef + MetaDataLoader.PKG_SEPARATOR + model.getSuper();
    }

    protected <M extends MetaModel> String getPackage(MetaData parent, String pkgDef, M model) {

        String pkg = model.getPackage();
        // NOTE:  Should it always be that you only fully qualify names when the parent
        // is the MetaDataLoader?
        if (pkg == null && parent instanceof MetaDataLoader)  {
            pkg = pkgDef;
        }
        if ( pkg == null ) pkg="";
        return pkg;
    }

    protected <T extends TypeConfig, M extends MetaModel> String getFullname(
            MetaData parent, T tc, String pkgDef, M model ) {

        String pkg = getPackage(parent, pkgDef, model);

        String name = model.getName();
        if ( name == null ) {
            name = autoCreateNameFromTypeConfig(parent, tc, model);
        }
        if ( name == null ) {
            throw new InvalidValueException( "Name is null, cannot merge MetaDataModel ["+model+"]" );
        }
        return (pkg.isEmpty()?"":pkg + "::") + name;
    }

    protected <T extends TypeConfig, M extends MetaModel> String autoCreateNameFromTypeConfig(
            MetaData parent, T tc, M model ) {

        String name = null;
        if ( tc.getDefaultName() != null ) {
            name = tc.getDefaultName();
        }
        else if ( tc.getDefaultNamePrefix() != null ) {

            int i = 0;

            // TODO: If auto named, then super could match, or we could match by subType
            for( MetaData md : parent.getChildrenOfType( tc.getName(), true )) {
                if ( md.getName().startsWith( tc.getDefaultNamePrefix() )) {
                    String n = md.getName().substring(tc.getDefaultNamePrefix().length());
                    try {
                        i = Integer.parseInt(n);
                    }
                    catch( NumberFormatException ex ) {
                        log.warn("Error parsing ["+n+"] on name ["+md.getName()+"] on autoname creation for model: " +model);
                    }
                }
            }
            name = tc.getDefaultNamePrefix()+(++i);
        }
        return name;
    }

    protected <T extends TypeConfig, M extends MetaModel> MetaData createOrOverloadMetaDataWithName(
            MetaData parent, T tc, M model, String superName, String fullname) {

        MetaData merge = findExistingMetaData(parent, tc, fullname);

        if ( merge != null ) {
            merge = overloadedMetaData(tc, model, merge);
        }
        else {
            merge = createNewMetaData(parent, tc, model, superName, fullname, merge);
        }
        parent.addChild( merge );
        return merge;
    }

    protected <T extends TypeConfig, M extends MetaModel> MetaData createNewMetaData(
            MetaData parent, T tc, M model, String superName, String fullname, MetaData merge) {

        String subType = null;

        // Get the superData && set the subType if null
        MetaData superData = null;
        if ( model.getSuper() != null ) {

            superData = getSuperData( parent, tc, model, superName );

            subType = superData.getSubTypeName();
            if ( model.getSubType() != null && !model.getSubType().equals(subType)) {
                throw new MetaDataException("SubType mismatch [" + model.getSubType() + "] != "+
                        "["+ merge.getSubTypeName()+"] on superData: " + superData );
            }
        }
        else {
            // Get the MetaData subType
            subType = getSubTypeName(tc, model);
        }

        SubTypeConfig stc = getSubTypeConfig(tc, model, subType);

        // Create the new MetaData
        merge = createNewMetaData(parent, tc, stc, fullname);

        // Set the SuperData if it exists
        if ( superData != null ) {
            validateSuperDataOnNew( merge, superData );
            merge.setSuperData( superData );
        }

        return merge;
    }

    protected <T extends TypeConfig> MetaData findExistingMetaData(MetaData parent, T tc, String fullname) {

        MetaData merge = null;
        try {
            merge = parent.getChildOfType( tc.getName(), fullname);
        } catch( MetaDataNotFoundException ignore ) {
            if ( log.isDebugEnabled() ) log.debug( "No child of type ["+tc.getName()+"] "+
                    " with name ["+fullname+"] on parent: "+parent);
        }
        return merge;
    }

    protected <T extends TypeConfig, M extends MetaModel> MetaData overloadedMetaData(
            T tc,  M model, MetaData merge ) {

        // Get the superData && set the subType if null
        if ( model.getSuper() != null ) {
            throw new MetaDataException("Super reference [" + model.getSuper() + "] is not valid when "+
                    "overloading existing MetaData: " + merge );
        }

        // Verify the subType is the same as the existing
        else if ( model.getSubType() != null && !merge.getSubTypeName().equals(model.getSubType())) {
            throw new MetaDataException("SubType mismatch [" + model.getSubType() + "] != "+
                    "["+ merge.getSubTypeName()+"] attempting to overloaded existing " +
                    " metadata: " + merge );
        }

        // Overload the existing MetaData
        merge = merge.overload();
        return merge;
    }

    protected <T extends TypeConfig, M extends MetaModel> T getTypeConfig( TSC tsc, M model ) {

        String typeName = model.getType();
        if ( typeName != null ) {
            for (TypeConfig tc : tsc.getTypes()) {
                if (typeName.equals(tc.getName())) return (T) tc;
            }
        }
        throw new InvalidValueException("Type [" + typeName + "] did not exist in TypesConfig, " +
                "cannot parse model: " + model);
    }

    protected <T extends TypeConfig, M extends MetaModel> String getSubTypeName( T tc, M model ) {

        String subType = model.getSubType();
        if ( subType == null && tc.getDefaultSubType() != null ) {
            subType = tc.getDefaultSubType();
        }
        return subType;
    }

    protected <T extends TypeConfig, M extends MetaModel> SubTypeConfig getSubTypeConfig(
            T tc, M model, String subType) {

        SubTypeConfig stc = null;

        if ( subType != null && tc.getSubTypes() != null ) {
            for ( SubTypeConfig st : tc.getSubTypes() ) {
                if ( st.getName().equals(subType)) {
                    stc = st;
                    break;
                }
            }
        }

        // Error if no subType was specified and there was no default
        if ( stc == null ) {
            throw new InvalidValueException( "Type ["+tc.getName()+"] with SubType ["+subType+"] did not exist "+
                    "in TypesConfig, cannot merge MetaDataModel: "+model );
        }
        return stc;
    }

    protected <TC extends TypeConfig, T extends SubTypeConfig> MetaData createNewMetaData(
            MetaData parent, TC tc, T stc, String fullname) {
        MetaData merge;
        Class<? extends MetaData> clazz = stc.getMetaDataClass();
        merge = (MetaData) parent.newInstanceFromClass( clazz, tc.getName(), stc.getName(), fullname );
        return merge;
    }

    protected void validateSuperDataOnNew(MetaData merge, MetaData superData ) {

        // TODO: Ensure the new superData has the same underlying superData overloads
        if (merge.getSuperData() != null && !merge.getSuperData().isSameTypeSubType(superData)) {
            throw new MetaDataException( "Specified superData subType mismatch "+
                    "["+superData.getSubTypeName()+"] != ["+merge.getSubTypeName()+"] on created metadata: "+merge );
        }
    }

    protected <T extends TypeConfig, M extends MetaModel> MetaData getSuperData(
            MetaData parent, T tc, M model, String superName ) {

        MetaData superData;
        try {
            MetaDataLoader loader = parent.getLoader();
            superData = loader.getChildOfType(tc.getName(),superName);
        } catch( MetaDataNotFoundException ex ) {
            throw new MetaDataException( "Referenced superData ["+superName+"] with type ["+tc.getName()+"] "+
                    "was not found by superRef on model: "+model);
        }
        return superData;
    }
}
