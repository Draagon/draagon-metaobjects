package com.draagon.meta.loader.model;

import com.draagon.meta.*;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.parser.ParserBase;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.util.MetaDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * v6.0.0: Updated to use service-based MetaDataTypeRegistry instead of TypesConfig
 */
public abstract class MetaModelParser<I extends MetaDataLoader, S> extends ParserBase<MetaModelLoader,I,S> {

    private static final Logger log = LoggerFactory.getLogger(MetaModelParser.class);
    
    // Thread-safe counter for auto-naming to handle concurrent parsing
    private final Map<String, Integer> autoNameCounters = new ConcurrentHashMap<>();

    private String defaultPackageName = null;

    protected MetaModelParser(MetaModelLoader loader, ClassLoader classLoader, String sourceName) {
        super(loader, classLoader, sourceName);
    }

    /* Load MetaDataModel Stream */
    public abstract void loadAndMerge( I intoLoader, S source );

    /** Merge the loaded MetaDataModel into the specified MetaDataLoader */
    protected <M extends MetaModel> void mergeMetaDataModel( I intoLoader, M model ) {
        
        // v6.0.0: Use MetaDataTypeRegistry instead of TypesConfig
        MetaDataTypeRegistry typeRegistry = intoLoader.getTypeRegistry();
        if ( typeRegistry == null ) throw new IllegalStateException( "MetaDataLoader did not have a MetaDataTypeRegistry set, loader=["+intoLoader+"]");

        // Set The default Package from the root MetaModel  
        String defPkg = getDefaultPackageName( model );

        // Begin merging the MetaModels into the Loader
        mergeMetaData( intoLoader, defPkg, typeRegistry, model );
    }

    /** Set default package name */
    protected <M extends MetaModel> String getDefaultPackageName( M model ) {

        String defPkg = model.getPackage();
        if ( defPkg == null ) defPkg = "";
        return defPkg;
    }

    /** Merge the MetaModels into the parent MetaData */
    protected <M extends MetaModel> void mergeMetaData(
            MetaData parent, String pkgDef, MetaDataTypeRegistry typeRegistry, M model) {

        List<M> children = (List<M>) model.getChildren();
        if ( children == null ) return;

        for ( M child : children ) {

            // v6.0.0: Validate type exists in registry and create MetaData
            validateModelType( typeRegistry, child );

            // Create or Overload MetaData from the Model
            MetaData merge = createOrOverloadMetaData(parent, pkgDef, typeRegistry, child );

            // Merge additional records from the Model
            mergeAdditionalModelData( parent, merge, child );

            // Merge child records
            mergeMetaData( merge, pkgDef, typeRegistry, child );
        }
    }

    protected <M extends MetaModel> void mergeAdditionalModelData(
            MetaData parent, MetaData merge, M model) {

        // Merge the Value for MetaDataValueHandlers like MetaAttribute
        if ( merge instanceof MetaDataValueHandler && model.getValue() != null ) {
            ((MetaDataValueHandler)merge).setValueAsObject(model.getValue());
        }
    }
    
    /** v6.0.0: Validate that the model type exists in the registry */
    protected <M extends MetaModel> void validateModelType(MetaDataTypeRegistry typeRegistry, M model) {
        String typeName = model.getType();
        String subTypeName = model.getSubType();
        
        if (typeName == null || typeName.isEmpty()) {
            throw new InvalidValueException("Model type cannot be null or empty: " + model);
        }
        
        // For v6.0.0, we rely on the registry to validate types during creation
        // The actual validation happens in createOrOverloadMetaData
    }

    protected <M extends MetaModel> MetaData createOrOverloadMetaData(
            MetaData parent, String pkgDef, MetaDataTypeRegistry typeRegistry, M model) {

        // Get the fullname for the model
        String fullname = getFullname(parent, pkgDef, model);

        // Get the fullname for the supername  
        String superName = getFullSuperName(parent, pkgDef, model);

        // Create or Overload MetaData from Model with specified superName and fullname
        return createOrOverloadMetaDataWithName(parent, typeRegistry, model, superName, fullname);
    }

    protected <M extends MetaModel> String getFullSuperName(
            MetaData parent, String pkgDef, M model) {

        if (model.getSuper() == null) return null;

        String superPkgDef = pkgDef;

        // If the model overrides the package, use that for the relative path
        if (model.getPackage() != null && !model.getPackage().isEmpty()) {
            superPkgDef = getPackage(parent,pkgDef, model);
        }

        // Otherwise, grab the parent package for the relative path
        else {
            // If the parent is not the MetaDataLoader, then see if the parent package is different
            // and if so, use that to reference the superData
            if (!(parent instanceof MetaDataLoader)) {
                String p = MetaDataUtil.findPackageForMetaData(parent);
                if (!p.isEmpty()) { //&& !p.equals( pkgDef ))  {
                    superPkgDef = p;
                }
            }
        }

        // TODO:  Add support for pointing to sub children with . notation

        if (model.getSuper().contains(MetaDataLoader.PKG_SEPARATOR))
            return MetaDataUtil.expandPackageForMetaDataRef(superPkgDef, model.getSuper());
        else if (superPkgDef == null || superPkgDef.isEmpty())
            return model.getSuper();
        else
            return superPkgDef + MetaDataLoader.PKG_SEPARATOR + model.getSuper();
    }

    protected <M extends MetaModel> String getPackage(MetaData parent, String pkgDef, M model) {

        String pkg = model.getPackage();

        if (parent instanceof MetaDataLoader) {
            if (pkg == null) {
                pkg = pkgDef;
            }
            else {
                pkg = MetaDataUtil.expandPackageForMetaDataRef(pkgDef,pkg);
            }
        }
        else if ( pkg == null ) {
            pkg="";
        }

        return pkg;
    }

    protected <M extends MetaModel> String getFullname(
            MetaData parent, String pkgDef, M model ) {

        String pkg = getPackage(parent, pkgDef, model);
        //if (pkg.isEmpty()) pkg=pkgDef;

        String name = model.getName();
        if ( name == null ) {
            name = autoCreateName(parent, model);
        }
        if ( name == null ) {
            throw new InvalidValueException( "Name is null, cannot merge MetaDataModel ["+model+"]" );
        }
        return (pkg.isEmpty()?"":pkg + "::") + name;
    }

    protected <M extends MetaModel> String autoCreateName(
            MetaData parent, M model ) {

        // v6.0.0: Simplified auto-naming - use subtype as base for name generation
        String subTypeName = model.getSubType();
        String typeName = model.getType();
        
        // Use subType if available, otherwise fall back to type
        String namePrefix = (subTypeName != null && !subTypeName.isEmpty()) 
                ? subTypeName.toLowerCase() 
                : (typeName != null ? typeName.toLowerCase() : null);
                
        if (namePrefix == null) {
            return null;
        }
        
        // Create a unique key for this parent-type-subtype combination to track counters
        String counterKey = parent.getName() + "::" + typeName + "::" + (subTypeName != null ? subTypeName : "");
        
        // Get the starting index based on existing children with the same name prefix
        int tempStartIndex = 0;
        for( MetaData md : parent.getChildrenOfType( typeName, true )) {
            if ( md.getName().startsWith( namePrefix )) {
                String suffix = md.getName().substring(namePrefix.length());
                try {
                    int num = Integer.parseInt(suffix);
                    if (num > tempStartIndex) tempStartIndex = num;
                }
                catch( NumberFormatException ex ) {
                    log.debug("Non-numeric suffix [{}] on name [{}] during autoname creation for model: {}", suffix, md.getName(), model);
                }
            }
        }
        final int startIndex = tempStartIndex;
        
        // Use atomic increment to get next unique index for this parent-type-subtype combination
        int nextIndex = autoNameCounters.compute(counterKey, (key, currentValue) -> {
            int baseValue = (currentValue == null) ? startIndex : Math.max(currentValue, startIndex);
            return baseValue + 1;
        });
        
        return namePrefix + nextIndex;
    }

    protected <M extends MetaModel> MetaData createOrOverloadMetaDataWithName(
            MetaData parent, MetaDataTypeRegistry typeRegistry, M model, String superName, String fullname) {

        MetaData merge = findExistingMetaData(parent, model.getType(), fullname);

        if ( merge != null ) {
            merge = overloadedMetaData(model, merge);
        }
        else {
            merge = createNewMetaData(parent, typeRegistry, model, superName, fullname);
        }
        parent.addChild( merge );
        return merge;
    }

    protected <M extends MetaModel> MetaData createNewMetaData(
            MetaData parent, MetaDataTypeRegistry typeRegistry, M model, String superName, String fullname) {

        String subType = getSubTypeName(model);

        // Get the superData && set the subType if null
        MetaData superData = null;
        if ( model.getSuper() != null ) {

            superData = getSuperData( parent, model, superName );

            // If model subType is null, inherit from super data
            if ( subType == null ) {
                subType = superData.getSubTypeName();
            }
            // If model has explicit subType, validate it matches super data
            else if ( !subType.equals(superData.getSubTypeName())) {
                throw new MetaDataException("SubType mismatch [" + subType + "] != "+
                        "["+ superData.getSubTypeName()+"] on superData: " + superData );
            }
        }

        // v6.0.0: Create MetaData using registry (registry handles null subType with defaults)
        MetaData merge = typeRegistry.createInstance(model.getType(), subType, fullname);

        // Set the SuperData if it exists
        if ( superData != null ) {
            validateSuperDataOnNew( merge, superData );
            merge.setSuperData( superData );
        }

        return merge;
    }

    protected MetaData findExistingMetaData(MetaData parent, String typeName, String fullname) {

        MetaData merge = null;
        try {
            merge = parent.getChildOfType( typeName, fullname);
        } catch( MetaDataNotFoundException ignore ) {
            if ( log.isDebugEnabled() ) log.debug( "No child of type ["+typeName+"] "+
                    " with name ["+fullname+"] on parent: "+parent);
        }
        return merge;
    }

    protected <M extends MetaModel> MetaData overloadedMetaData(M model, MetaData merge ) {

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

    /** v6.0.0: Replaced getTypeConfig - validation now handled by registry during creation */
    // This method is no longer needed as type validation happens in the MetaDataTypeRegistry

    protected <M extends MetaModel> String getSubTypeName( M model ) {

        // v6.0.0: Simplified - just return the model's subType, no default from TypesConfig
        String subType = model.getSubType();
        
        // If null, the registry will handle default subType assignment
        return subType;
    }

    /** v6.0.0: Replaced getSubTypeConfig - subtype validation now handled by registry during creation */
    // This method is no longer needed as subtype validation happens in the MetaDataTypeRegistry

    /** v6.0.0: Replaced createNewMetaData overload - creation now handled by registry */
    // This overloaded method is no longer needed as MetaData creation happens in the MetaDataTypeRegistry

    protected void validateSuperDataOnNew(MetaData merge, MetaData superData ) {

        // TODO: Ensure the new superData has the same underlying superData overloads
        if (merge.getSuperData() != null && !merge.getSuperData().isSameTypeSubType(superData)) {
            throw new MetaDataException( "Specified superData subType mismatch "+
                    "["+superData.getSubTypeName()+"] != ["+merge.getSubTypeName()+"] on created metadata: "+merge );
        }
    }

    protected <M extends MetaModel> MetaData getSuperData(
            MetaData parent, M model, String superName ) {

        MetaData superData;
        try {
            MetaDataLoader loader = parent.getLoader();
            superData = loader.getChildOfType(model.getType(), superName);
        } catch( MetaDataNotFoundException ex ) {
            throw new MetaDataException( "Referenced superData ["+superName+"] with type ["+model.getType()+"] "+
                    "was not found by superRef on model: "+model);
        }
        return superData;
    }
}
