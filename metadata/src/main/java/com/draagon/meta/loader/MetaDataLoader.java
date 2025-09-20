/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.MetaDataTypeId;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.registry.MetaDataLoaderRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MetaDataLoader serves as the foundation for loading and managing metadata definitions.
 * 
 * <p>MetaDataLoader operates exactly like Java's ClassLoader - it loads metadata definitions 
 * once at startup and keeps them permanently in memory for the application lifetime. This is 
 * <strong>NOT</strong> a typical data access pattern but rather a metadata definition system 
 * analogous to the Java reflection system.</p>
 * 
 * <h3>ClassLoader Pattern Analogy</h3>
 * <table border="1">
 * <tr><th>Java Reflection</th><th>MetaObjects Framework</th><th>Purpose</th></tr>
 * <tr><td>Class.forName()</td><td>MetaDataLoader.load()</td><td>Load definitions</td></tr>
 * <tr><td>Class.getFields()</td><td>MetaObject.getMetaFields()</td><td>Access structure</td></tr>
 * <tr><td>Field.get(object)</td><td>MetaField.getValue(object)</td><td>Read object data</td></tr>
 * <tr><td>Permanent in memory</td><td>Permanent MetaData objects</td><td>Cached access</td></tr>
 * <tr><td>Thread-safe reads</td><td>Thread-safe metadata access</td><td>Concurrent operations</td></tr>
 * </table>
 * 
 * <h3>Loading vs Runtime Phases</h3>
 * <pre>{@code
 * // LOADING PHASE - Happens once at startup
 * MetaDataLoader loader = new SimpleLoader("myLoader");
 * loader.setSourceURIs(Arrays.asList(URI.create("metadata.json")));
 * loader.init(); // Loads ALL metadata into permanent memory structures
 * 
 * // RUNTIME PHASE - All operations are READ-ONLY
 * MetaObject userMeta = loader.getMetaObjectByName("User");  // O(1) lookup
 * MetaField field = userMeta.getMetaField("email");          // Cached access
 * Object value = field.getValue(userObject);                // Thread-safe read
 * }</pre>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>Startup Cost, Runtime Speed</strong>: Heavy initialization, ultra-fast runtime access</li>
 * <li><strong>Permanent References</strong>: Like Class objects, MetaData stays in memory until app shutdown</li>
 * <li><strong>Thread-Safe Reads</strong>: No synchronization needed for read operations (primary use case)</li>
 * <li><strong>OSGI Ready</strong>: WeakHashMap and service patterns handle dynamic class loading</li>
 * </ul>
 * 
 * @author Doug Mealing
 * @version 6.0.0
 * @since 1.0
 * @see com.draagon.meta.loader.simple.SimpleLoader
 * @see com.draagon.meta.loader.file.FileMetaDataLoader
 * @see MetaData
 */
public class MetaDataLoader extends MetaData implements LoaderConfigurable {

    private static final Logger log = LoggerFactory.getLogger(MetaDataLoader.class);
    
    // Concurrent loading protection
    private static final ConcurrentHashMap<String, CompletableFuture<MetaDataLoader>> activeLoaders = new ConcurrentHashMap<>();
    private static final long DEFAULT_LOADING_TIMEOUT_MS = 30000; // 30 seconds

    public final static String TYPE_LOADER = "loader";
    public final static String SUBTYPE_MANUAL = "manual";

    // TODO:  Allow for custom configurations for overloaded MetaDataLoaders
    private final LoaderOptions loaderOptions;
    
    // v6.0.0: Replace TypesConfig with service-based registries
    private MetaDataTypeRegistry typeRegistry = null;
    private MetaDataLoaderRegistry loaderRegistry = null;

    // Enhanced thread-safe loading state management
    private final LoadingState loadingState = new LoadingState();
    
    // Legacy state flags for backward compatibility
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

    // v6.0.0: Replace TypesConfig methods with registry-based API
    
    public MetaDataTypeRegistry getTypeRegistry() {
        if (typeRegistry == null) {
            typeRegistry = new MetaDataTypeRegistry(ServiceRegistryFactory.getDefault());
        }
        return typeRegistry;
    }

    public MetaDataLoader setTypeRegistry(MetaDataTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
        return this;
    }

    public MetaDataLoaderRegistry getLoaderRegistry() {
        if (loaderRegistry == null) {
            loaderRegistry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        }
        return loaderRegistry;
    }

    public MetaDataLoader setLoaderRegistry(MetaDataLoaderRegistry loaderRegistry) {
        this.loaderRegistry = loaderRegistry;
        return this;
    }

    /**
     * Check the state of the MetaDataLoader to ensure it is initialized and not destroyed();
     */
    protected void checkState() {
        // Enhanced state checking with detailed error messages
        if (!loadingState.isUsable()) {
            throw new IllegalStateException(
                String.format("MetaDataLoader [%s] is not usable. %s", 
                    getName(), loadingState.getStatusDescription()));
        }
        
        // Legacy compatibility checks
        if ( !isInitialized ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] was not initialized" );
        if ( isDestroyed ) throw new IllegalStateException( "MetaDataLoader [" + getName() + "] is destroyed" );
    }
    
    /**
     * Get the current loading state
     * @return The LoadingState instance
     */
    public LoadingState getLoadingState() {
        return loadingState;
    }
    
    /**
     * Check if the loader is currently loading
     * @return true if loading is in progress
     */
    public boolean isLoading() {
        return loadingState.isLoadingInProgress();
    }
    
    /**
     * Get detailed status information
     * @return String describing the current loader status
     */
    public String getDetailedStatus() {
        return String.format("MetaDataLoader[%s] %s", getName(), loadingState.getStatusDescription());
    }
    
    /**
     * Build a unique key for this loader instance for concurrent loading protection
     */
    private String buildLoaderKey() {
        return String.format("%s:%s:%s", getClass().getSimpleName(), getSubTypeName(), getName());
    }
    
    /**
     * Check if this loader is currently being initialized by another thread
     * @return true if initialization is in progress
     */
    public boolean isInitializationInProgress() {
        String loaderKey = buildLoaderKey();
        CompletableFuture<MetaDataLoader> future = activeLoaders.get(loaderKey);
        return future != null && !future.isDone();
    }
    
    /**
     * Get the number of loaders currently being initialized
     * @return Number of active initializations
     */
    public static int getActiveInitializationCount() {
        return (int) activeLoaders.values().stream().filter(f -> !f.isDone()).count();
    }
    
    /**
     * Force cleanup of failed or stale loader initialization attempts
     * @param loaderKey The key of the loader to cleanup, or null to cleanup all failed attempts
     */
    public static void cleanupFailedInitializations(String loaderKey) {
        if (loaderKey != null) {
            CompletableFuture<MetaDataLoader> future = activeLoaders.get(loaderKey);
            if (future != null && (future.isDone() || future.isCompletedExceptionally())) {
                activeLoaders.remove(loaderKey);
                log.info("Cleaned up failed initialization for loader: {}", loaderKey);
            }
        } else {
            // Cleanup all completed/failed futures
            activeLoaders.entrySet().removeIf(entry -> {
                CompletableFuture<MetaDataLoader> future = entry.getValue();
                if (future.isDone() || future.isCompletedExceptionally()) {
                    log.debug("Cleaned up initialization for loader: {}", entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Retry initialization with error recovery
     * @param maxRetries Maximum number of retry attempts
     * @param retryDelayMs Delay between retries in milliseconds
     * @return This MetaDataLoader
     * @throws MetaDataLoadingException if all retries fail
     */
    public MetaDataLoader initWithRetry(int maxRetries, long retryDelayMs) {
        MetaDataLoadingException lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("Retrying initialization for loader [{}], attempt {} of {}", 
                           getName(), attempt + 1, maxRetries + 1);
                    
                    // Reset state for retry
                    resetForRetry();
                    
                    // Wait before retry
                    if (retryDelayMs > 0) {
                        Thread.sleep(retryDelayMs);
                    }
                }
                
                return init();
                
            } catch (MetaDataLoadingException e) {
                lastException = e;
                log.warn("Initialization attempt {} failed for loader [{}]: {}", 
                        attempt + 1, getName(), e.getMessage());
                
                // Cleanup failed attempt
                cleanupFailedInitializations(buildLoaderKey());
                
                if (attempt == maxRetries) {
                    break; // Don't sleep on the last attempt
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MetaDataLoadingException(
                    "Initialization retry interrupted for loader: " + getName(), e);
            }
        }
        
        throw new MetaDataLoadingException(
            "Failed to initialize loader [" + getName() + "] after " + (maxRetries + 1) + " attempts", 
            getName(), LoadingState.Phase.INITIALIZING, 0, lastException);
    }
    
    /**
     * Reset loader state for retry attempts
     */
    private void resetForRetry() {
        // Reset loading state
        loadingState.forceTransition(LoadingState.Phase.UNINITIALIZED);
        loadingState.clearError();
        
        // Reset legacy flags
        isInitialized = false;
        isRegistered = false;
        
        // Clear any partial state
        if (typeRegistry != null || loaderRegistry != null) {
            log.debug("Clearing partial registry state for retry");
            typeRegistry = null;
            loaderRegistry = null;
        }
        
        log.debug("Reset loader state for retry: {}", getName());
    }
    
    /**
     * Graceful shutdown with cleanup
     */
    public void shutdown() {
        try {
            log.info("Shutting down MetaDataLoader [{}]", getName());
            
            // Cancel any active initialization
            String loaderKey = buildLoaderKey();
            CompletableFuture<MetaDataLoader> future = activeLoaders.get(loaderKey);
            if (future != null && !future.isDone()) {
                future.cancel(true);
                log.debug("Cancelled active initialization for loader: {}", loaderKey);
            }
            
            // Destroy if not already destroyed
            if (!isDestroyed()) {
                destroy();
            }
            
            // Cleanup from active loaders
            cleanupFailedInitializations(loaderKey);
            
            log.info("Successfully shut down MetaDataLoader [{}]", getName());
            
        } catch (Exception e) {
            log.error("Error during shutdown of MetaDataLoader [{}]", getName(), e);
        }
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

    // v6.0.0: Replace TypesConfig initialization with registry initialization
    protected void initDefaultRegistries() {
        // Initialize registries with service discovery
        if (typeRegistry == null) {
            typeRegistry = new MetaDataTypeRegistry(ServiceRegistryFactory.getDefault());
            log.debug("Initialized default MetaDataTypeRegistry for loader: {}", getName());
        }
        
        if (loaderRegistry == null) {
            loaderRegistry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
            log.debug("Initialized default MetaDataLoaderRegistry for loader: {}", getName());
        }
    }

    /**
     * Initialize the MetaDataLoader with enhanced thread-safe state management and concurrent protection.
     * This method prevents concurrent initialization attempts for the same loader.
     * @return This MetaDataLoader
     * @throws MetaDataLoadingException if initialization fails
     */
    public MetaDataLoader init() {
        return initWithConcurrencyProtection(DEFAULT_LOADING_TIMEOUT_MS);
    }
    
    /**
     * Initialize the MetaDataLoader with concurrent protection and custom timeout.
     * @param timeoutMs Maximum time to wait for initialization in milliseconds
     * @return This MetaDataLoader
     * @throws MetaDataLoadingException if initialization fails or times out
     */
    public MetaDataLoader initWithTimeout(long timeoutMs) {
        return initWithConcurrencyProtection(timeoutMs);
    }
    
    /**
     * Internal initialization method with concurrent protection
     */
    private MetaDataLoader initWithConcurrencyProtection(long timeoutMs) {
        String loaderKey = buildLoaderKey();
        
        // Check if there's already an initialization in progress for this loader
        CompletableFuture<MetaDataLoader> loadingFuture = activeLoaders.computeIfAbsent(loaderKey, 
            key -> CompletableFuture.supplyAsync(() -> performInitialization(key), 
                                               ForkJoinPool.commonPool()));
        
        try {
            MetaDataLoader result = loadingFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
            return result;
        } catch (TimeoutException e) {
            activeLoaders.remove(loaderKey); // Allow retry
            throw new MetaDataLoadingException(
                "Loader initialization timeout after " + timeoutMs + "ms: " + loaderKey, 
                getName(), LoadingState.Phase.INITIALIZING, timeoutMs, e);
        } catch (InterruptedException | ExecutionException e) {
            activeLoaders.remove(loaderKey); // Allow retry
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            throw new MetaDataLoadingException(
                "Loader initialization failed: " + loaderKey, 
                getName(), LoadingState.Phase.INITIALIZING, 0, cause);
        }
    }
    
    /**
     * Internal method that performs the actual initialization work
     */
    private MetaDataLoader performInitialization(String loaderKey) {
        long startTime = System.currentTimeMillis();
        
        try {
            return performInitializationInternal(startTime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize loader: " + loaderKey, e);
        } finally {
            activeLoaders.remove(loaderKey); // Clean up
        }
    }
    
    /**
     * Legacy initialization method for backward compatibility
     * @deprecated Use init() which now includes concurrent protection
     */
    @Deprecated
    public MetaDataLoader initLegacy() {
        return performInitializationInternal(System.currentTimeMillis());
    }
    
    /**
     * Core initialization logic - refactored for better maintainability
     */
    private MetaDataLoader performInitializationInternal(long startTime) {
        validateAndTransitionToInitializing();
        
        try {
            logInitializationStart();
            initializeRegistriesIfNeeded();
            transitionToInitialized(startTime);
            updateCompatibilityFlags();
            registerIfRequested();
            logInitializationSuccess(startTime);
            
            return this;
            
        } catch (Exception e) {
            handleInitializationFailure(e, startTime);
            throw e; // Re-throw after handling
        }
    }
    
    /**
     * Validates current state and transitions to initializing phase
     */
    private void validateAndTransitionToInitializing() {
        if (!loadingState.tryTransition(LoadingState.Phase.UNINITIALIZED, LoadingState.Phase.INITIALIZING)) {
            LoadingState.Phase currentPhase = loadingState.getCurrentPhase();
            if (currentPhase == LoadingState.Phase.INITIALIZED || currentPhase == LoadingState.Phase.REGISTERED) {
                throw new IllegalStateException("MetaDataLoader [" + getName() + "] was already initialized");
            } else {
                throw new IllegalStateException("MetaDataLoader [" + getName() + "] cannot be initialized from phase: " + currentPhase);
            }
        }
    }
    
    /**
     * Logs initialization start if verbose mode is enabled
     */
    private void logInitializationStart() {
        if (loaderOptions.isVerbose()) {
            log.info("Loading the [" + getClass().getSimpleName() + "] MetaDataLoader with name [" + getName() + "]");
        }
    }
    
    /**
     * Initializes default registries if they haven't been set
     */
    private void initializeRegistriesIfNeeded() {
        if (typeRegistry == null || loaderRegistry == null) {
            initDefaultRegistries();
        }
    }
    
    /**
     * Transitions to initialized state with error handling
     */
    private void transitionToInitialized(long startTime) {
        if (!loadingState.tryTransition(LoadingState.Phase.INITIALIZING, LoadingState.Phase.INITIALIZED)) {
            throw new MetaDataLoadingException(
                "Failed to transition to INITIALIZED phase", 
                getName(), loadingState.getCurrentPhase(), 
                System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Updates legacy compatibility flags
     */
    private void updateCompatibilityFlags() {
        isInitialized = true;
    }
    
    /**
     * Registers the loader if configured to do so
     */
    private void registerIfRequested() {
        if (loaderOptions.shouldRegister()) {
            register();
        }
    }
    
    /**
     * Logs successful initialization if verbose mode is enabled
     */
    private void logInitializationSuccess(long startTime) {
        if (loaderOptions.isVerbose()) {
            log.info("Successfully loaded MetaDataLoader [" + getName() + "] in " + 
                    (System.currentTimeMillis() - startTime) + "ms");
        }
    }
    
    /**
     * Handles initialization failures by recording error state and preparing exception
     */
    private void handleInitializationFailure(Exception e, long startTime) {
        loadingState.setError(e, LoadingState.Phase.UNINITIALIZED);
        
        if (!(e instanceof MetaDataLoadingException)) {
            throw new MetaDataLoadingException(
                "Failed to initialize MetaDataLoader [" + getName() + "]", 
                getName(), LoadingState.Phase.INITIALIZING, 
                System.currentTimeMillis() - startTime, e);
        }
    }


    /**
     * Returns if the MetaDataLoader is initialized (enhanced with new state management)
     * @return True if initialized
     */
    public boolean isInitialized() {
        // Use enhanced state checking in addition to legacy flag
        return isInitialized && loadingState.isInPhase(LoadingState.Phase.INITIALIZED, LoadingState.Phase.REGISTERED);
    }

    /**
     * Register this MetaDataLoader with the MetaDataRegistry using enhanced state management
     */
    public MetaDataLoader register() {
        // Check if we can transition to registering state
        if (!loadingState.tryTransition(LoadingState.Phase.INITIALIZED, LoadingState.Phase.REGISTERING)) {
            LoadingState.Phase currentPhase = loadingState.getCurrentPhase();
            if (currentPhase == LoadingState.Phase.REGISTERED) {
                // Already registered, this is okay
                return this;
            } else {
                throw new IllegalStateException(
                    "Cannot register MetaDataLoader [" + getName() + "] from phase: " + currentPhase);
            }
        }
        
        try {
            if (!isRegistered) {
                MetaDataRegistry.registerLoader(this);
            }
            
            // Transition to registered state
            if (!loadingState.tryTransition(LoadingState.Phase.REGISTERING, LoadingState.Phase.REGISTERED)) {
                throw new IllegalStateException(
                    "Failed to transition to REGISTERED phase for MetaDataLoader [" + getName() + "]");
            }
            
            // Update legacy flag for compatibility
            isRegistered = true;
            
            if (loaderOptions.isVerbose()) {
                log.info("Successfully registered MetaDataLoader [" + getName() + "]");
            }
            
            return this;
            
        } catch (Exception e) {
            loadingState.setError(e, LoadingState.Phase.INITIALIZED);
            throw new MetaDataLoadingException(
                "Failed to register MetaDataLoader [" + getName() + "]", 
                getName(), LoadingState.Phase.REGISTERING, 0, e);
        }
    }

    /**
     * Returns whether the MetaDataLoader in the MetaDataRegistry
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class

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
     * Unloads the MetaDataLoader with enhanced state management
     */
    public void destroy() {
        // Check if already destroyed using new state management
        if (loadingState.isDestroyed()) {
            throw new IllegalStateException("MetaDataLoader [" + getName() + "] was already destroyed!");
        }
        
        // Legacy compatibility check
        if (isDestroyed) {
            throw new IllegalStateException("MetaDataLoader [" + getName() + "] was already destroyed!");
        }

        if (loaderOptions.isVerbose()) {
            log.info("Destroying the [" + getName() + "] MetaDataLoader");
        }
        
        try {
            // Remove all classes
            clearChildren();

            // Unregister the class loader
            if (isRegistered) {
                MetaDataRegistry.unregisterLoader(this);
            }
            
            // Transition to destroyed state
            loadingState.forceTransition(LoadingState.Phase.DESTROYED);
            
            // Update legacy flags for compatibility
            isDestroyed = true;
            
            if (loaderOptions.isVerbose()) {
                log.info("Successfully destroyed MetaDataLoader [" + getName() + "]");
            }
            
        } catch (Exception e) {
            loadingState.setError(e);
            log.error("Error during destruction of MetaDataLoader [" + getName() + "]", e);
            throw new RuntimeException("Failed to destroy MetaDataLoader [" + getName() + "]", e);
        }
    }

    public boolean isDestroyed() {
        // Use enhanced state checking in addition to legacy flag
        return isDestroyed || loadingState.isDestroyed();
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