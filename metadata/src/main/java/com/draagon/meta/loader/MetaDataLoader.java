/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.loader.types.TypesConfig;
import com.draagon.meta.loader.types.TypesConfigLoader;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  MetaDataLoader with common functions for all MetaDataLoaders
 */
public class MetaDataLoader extends MetaData implements LoaderConfigurable {

    private static final Logger log = LoggerFactory.getLogger(MetaDataLoader.class);

    public final static String TYPE_LOADER = "loader";
    public final static String SUBTYPE_MANUAL = "manual";

    // TODO:  Allow for custom configurations for overloaded MetaDataLoaders
    private final LoaderOptions loaderOptions;
    private TypesConfigLoader<?> typesLoader = null;
    private TypesConfig typesConfig = null;

    private boolean isRegistered = false;
    private boolean isInitialized = false;
    private boolean isDestroyed = false;
    
    /**
     * Constructs a new MetaDataLoader
     * @param subtype The subType for the metadata loader
     */
    public MetaDataLoader(LoaderOptions loaderOptions, String subtype ) {
        this( loaderOptions, subtype, TYPE_LOADER + "-" + System.currentTimeMillis());
    }

    /**
     * Constructs a new MetaDataLoader
     * @param subtype The subtype of the metadata loader
     * @param name The name of the metadata loader
     */
    public MetaDataLoader(LoaderOptions loaderOptions, String subtype, String name ) {
        super( TYPE_LOADER, subtype, name );
        this.loaderOptions = loaderOptions;
    }

    /**
     * Manually construct a MetaDataLoader.  Usually used for unit testing.
     * @param name The name of the Manually create MetaDataLoader
     * @return The created MetaDataLoader
     */
    public static MetaDataLoader createManual( boolean shouldRegister, String name ) {
        return new MetaDataLoader(
                LoaderOptions.create( false, false, false),
                        SUBTYPE_MANUAL, name );
    }

    ///////////////////////////////////////////////////////////////////////
    // Configs

    public LoaderOptions getLoaderOptions() {
        return loaderOptions;
    }

    public <T extends TypesConfig> T getTypesConfig() {
        return (T) typesConfig;
    }

    public <T extends TypesConfig> MetaDataLoader setTypesLoader(TypesConfigLoader<T> typesLoader ) {
        this.typesLoader = typesLoader;
        typesConfig = typesLoader.newTypesConfig();
        if (typesConfig == null ) {
            throw new MetaDataNotFoundException( "No TypesConfig was found (was it not initialized?) in "+
                    "TypesConfigLoader: "+typesLoader, TypesConfig.OBJECT_NAME);
        }
        return this;
    }

    public <T extends TypesConfig> TypesConfigLoader<T> getTypesLoader() {
        return (TypesConfigLoader<T>) typesLoader;
    }

    /**
     * Check the state of the MetaDataLoader to ensure it is initialized and not destroyed();
     */
    protected void checkState() {
        if ( !isInitialized ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was not initialized" );
        if ( isDestroyed ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] is destroyed" );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // LoaderConfigurable Support Methods
    private String configSourceDir = null;

    @Override
    public void configure(LoaderConfiguration config) {
        if (config.getClassLoader() != null) {
            if (log.isDebugEnabled()) log.debug("Setting ClassLoader: " + config.getClassLoader());
            setMetaDataClassLoader(config.getClassLoader());
        }
        
        if (config.getSourceDir() != null) {
            File sd = new File(config.getSourceDir());
            if (!sd.exists()) throw new IllegalStateException("SourceDir [" + config.getSourceDir() + "] does not exist");
            if (log.isDebugEnabled()) log.debug("Setting SourceDir: " + config.getSourceDir());
            configSourceDir = config.getSourceDir();
        }
        
        if (config.getSources() != null && !config.getSources().isEmpty()) {
            if (log.isDebugEnabled()) log.debug("Processing sources: " + config.getSources());
            processSources(configSourceDir, config.getSources());
        }
        
        processArguments(config.getArguments());
        init();
    }

    protected void processSources(String sourceDir, List<String> sourceList) {
        throw new UnsupportedOperationException(getClass().getName() + " does not support source processing " +
                "(you must implement processSources method)");
    }

    protected void processArguments(Map<String, String> args) {
        if (args == null) return;

        if (args.get(LoaderConfigurationConstants.ARG_REGISTER) != null) {
            getLoaderOptions().setShouldRegister(Boolean.parseBoolean(args.get(LoaderConfigurationConstants.ARG_REGISTER)));
        }
        if (args.get(LoaderConfigurationConstants.ARG_VERBOSE) != null) {
            getLoaderOptions().setVerbose(Boolean.parseBoolean(args.get(LoaderConfigurationConstants.ARG_VERBOSE)));
        }
        if (args.get(LoaderConfigurationConstants.ARG_STRICT) != null) {
            getLoaderOptions().setStrict(Boolean.parseBoolean(args.get(LoaderConfigurationConstants.ARG_STRICT)));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization Methods

    protected void initDefaultTypesConfig() {
        // Create the TypesConfigLoader and set a new TypesConfig
        TypesConfigLoader typesLoader = TypesConfigLoader.create( getMetaDataClassLoader() );
        setTypesLoader(typesLoader);
    }

    /**
     * Initialize the MetaDataLoader.  It will prevent a second init call.
     * @return This MetaDataLoader
     */
    public MetaDataLoader init() {

        if ( isInitialized ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was already initialized" );

        if ( loaderOptions.isVerbose() ) {
            log.info("Loading the [" + getClass().getSimpleName() + "] MetaDataLoader with name [" + getName() + "]" );
        }

        // Initialize the Default TypesConfig if one did not exist
        if ( getTypesLoader() == null ) {
            initDefaultTypesConfig();
        }

        isInitialized = true;

        if ( loaderOptions.shouldRegister() ) {
            register();
        }

        return this;
    }


    /**
     * Returns if the MetaDataLoader is initialized
     * @return True if initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Register this MetaDataLoader with the MetaDataRegistry
     */
    public MetaDataLoader register() {
        if ( !isRegistered ) {
            MetaDataRegistry.registerLoader(this);
        }
        isRegistered = true;
        return this;
    }

    /**
     * Returns whether the MetaDataLoader in the MetaDataRegistry
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Gets the primary MetaData class
     */
    @SuppressWarnings("unchecked")
    public final <T extends MetaData> Class<T> getMetaDataClass() {
        return (Class<T>) MetaDataLoader.class;
    }

    /** Wrap the MetaDataLoader */
    public MetaDataLoader overload() {
        throw new IllegalStateException( "You cannot wrap a MetaDataLoader!" );
    }

    /**
     * Sets an attribute on the MetaClass
     */
    public void addMetaAttr(MetaAttribute attr) {
        addChild(attr);
    }

    /**
     * Whether the MetaDataLoader handles the object specified
     */
    protected boolean handles(Object obj) {
        checkState();
        if (getMetaObjectFor(obj) != null) {
            return true;
        }
        return false;
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public List<MetaData> getMetaDataOfType( String type ) {
        return getMetaDataOfType(type, true);
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public List<MetaData> getMetaDataOfType( String type, boolean includeParentData ) {
        checkState();
        return getChildrenOfType(type,includeParentData);
    }


    /**
     * Retrieves a collection of all Meta Classes
     */
    public List<MetaObject> getMetaObjects() {
        checkState();
        return getChildren( MetaObject.class, true );
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public MetaObject getMetaObjectByName(String name ) {
        checkState();
        return (MetaObject) getChildOfType( MetaObject.TYPE_OBJECT, name );
    }

    /**
     * Return the matching object instance
     */
    public <T> T newObjectInstance(Class<T> clazz) throws ClassNotFoundException {
        for(MetaObject mo : getMetaObjects()) {
            if (mo.getObjectClass().equals(clazz)) {
                return (T) mo.newInstance();
            }
        }
        throw new ClassNotFoundException("Could not find MetaObject for class ["+clazz.getName()+"]");
    }

    /**
     * Gets the MetaObject of the specified Object
     */
    public MetaObject getMetaObjectFor(Object obj) {
        checkState();
        for (MetaObject mc : getChildren( MetaObject.class, true )) {
            if (mc.produces(obj)) {
                return mc;
            }
        }

        return null;
    }


    /**
     * Retrieves a collection of all Meta Classes
     */
    public <N extends MetaData> List<N> getMetaData(Class<N> c ) {
        return getMetaData(c, true);
    }

    /**
     * Retrieves a collection of all Meta Classes
     */
    public <N extends MetaData> List<N> getMetaData( Class<N> c, boolean includeParentData ) {
        checkState();
        return getChildren(c,includeParentData);
    }


    /**
     * Gets the MetaData with the specified Class type and name
     */
    public <N extends MetaData> N getMetaDataByName( Class<N> c, String metaDataName) throws MetaDataNotFoundException {

        checkState();

        String KEY = "QuickCache-"+c.getName()+"-"+metaDataName;

        MetaData mc = (MetaData) getCacheValue(KEY);
        if (mc == null) {
            synchronized( this ) {

                mc = (MetaData) getCacheValue(KEY);
                if (mc == null) {
                    for (MetaData mc2 : getMetaData( c )) {
                        if (mc2.getName().equals(metaDataName)) {
                            mc = mc2;
                            break;
                        }
                    }

                    if (mc != null) {
                        setCacheValue(KEY, mc);
                    }
                }
            }

            if (mc == null) {
                throw new MetaDataNotFoundException( "MetaData with name [" + metaDataName + "] not found in MetaDataLoader [" + toString() + "]", metaDataName );
            }
        }


        return (N) mc;
    }

    /**
     * Gets the MetaData with the specified name in parent hierarchy.
     * <p>
     * Only uses direct 'super' relationship, not 'inherits'
     */
    protected List<MetaObject> getMetaDataBySuper(String metaDataName, List<MetaObject> objects) throws MetaDataNotFoundException {

        checkState();

        String KEY = "QuickCacheDerived-" + metaDataName;
        List<MetaObject> result = (List<MetaObject>) getCacheValue(KEY);
        if (result == null) {
            synchronized (this) {
                result = (List<MetaObject>) getCacheValue(KEY);
                if (result == null) {
                    result = new ArrayList<>();

                    for (MetaObject mo : objects) {
                        if (null != mo.getSuperObject()) {
                            if (mo.getSuperObject().getName().equals(metaDataName)) {
                                result.add( mo);
                                result.addAll( getMetaDataBySuper(mo.getName(), objects));
                            }
                        }
                    }
                    setCacheValue(KEY, result);  // Build the sub-trees as we go
                }
            }
        }
        return result;
    }

    /**
     * Gets the MetaData with the specified name in parent hierarchy.
     * <p>
     * Only uses direct 'super' relationship, not 'inherits'
     */
    public List<MetaObject> getMetaDataBySuper(String metaDataName) throws MetaDataNotFoundException {

        checkState();

        String KEY = "QuickCacheDerived-" + metaDataName;
        List<MetaObject> result;
        result = (List<MetaObject>) getCacheValue(KEY);
        if (result == null) {
            synchronized (this) {
                result = (List<MetaObject>) getCacheValue(KEY);
                if (result == null) {
                    List<MetaObject> objects = getMetaObjects();
                    // Delegate to a second level, so we don't have to keep retrieving the list of all MetaObjects
                    result = getMetaDataBySuper(metaDataName, objects);
                    // rely on delegate function to set the cache
                }
            }

        }

        return result;
    }



    /**
     * Lookup the specified class by name
     * @param className
     * @return
     */
    public Class<?> loadClass(String className ) throws ClassNotFoundException {

        checkState();
        try {
            return getClass().getClassLoader().loadClass( className );
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Specified Java Class [" + className + "] was not found: " + e.getMessage(), e);
        }
    }


    /**
     * Adds the child MetaData
     */
    @Override
    public void addChild(MetaData mc) {
        checkState();
        super.addChild(mc);
    }

    /**
     * Unloads the MetaDataLoader
     */
    public void destroy() {

        if ( isDestroyed ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was already destroyed!" );

        if ( loaderOptions.isVerbose() ) {
            log.info("Destroying the [" + getName() + "] MetaDataLoader");
        }

        // Remove all classes
        clearChildren();

        isDestroyed = true;

        // Unregister the class loader
        if ( isRegistered ) {
            MetaDataRegistry.unregisterLoader(this);
        }
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    ////////////////////////////////////////////////////
    // MISC METHODS

    public String toString() {
        if (getParent() == null) {
            return getClass().getSimpleName() + "[" + getSubTypeName() + ":" + getName() + "]";
        } else {
            return getClass().getSimpleName() + "[" + getSubTypeName() + ":" + getName() + "@" + getParent().toString() + "]";
        }
    }

}