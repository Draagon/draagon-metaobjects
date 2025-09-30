package com.metaobjects.demo.fishstore;

import com.metaobjects.demo.fishstore.domain.*;
import com.metaobjects.field.MetaField;
import com.metaobjects.key.PrimaryKey;
import com.metaobjects.loader.file.FileMetaDataLoader;
import com.metaobjects.loader.file.FileLoaderOptions;
import com.metaobjects.loader.file.LocalFileMetaDataSources;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.db.ObjectManagerDB;
import com.metaobjects.manager.db.driver.DerbyDriver;
import com.metaobjects.manager.db.validator.MetaClassDBValidatorService;
import com.metaobjects.MetaData;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataLoaderRegistry;
import com.metaobjects.registry.ServiceRegistryFactory;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Standalone test for ObjectManagerDB functionality without Spring framework
 */
public class StandaloneObjectManagerDBTest {

    private static final Logger log = LoggerFactory.getLogger(StandaloneObjectManagerDBTest.class);

    public static void main(String[] args) {
        log.info("=== STANDALONE OBJECTMANAGERDB TEST ===");

        try {
            // Step 1: Initialize MetaData Loader
            FileMetaDataLoader loader = createMetaDataLoader();

            // Step 2: Initialize Derby Database
            EmbeddedDataSource dataSource = createDerbyDataSource();

            // Step 3: Initialize ObjectManagerDB
            ObjectManagerDB objectManager = createObjectManagerDB(dataSource);

            // Step 4: Initialize Database Validator (Auto-create tables)
            initializeDatabaseValidator(objectManager, loader);

            // Step 5: Test MetaData Loading
            testMetaDataLoading(loader);

            // Step 6: Test Table Creation and Persistence
            testPersistence(objectManager, loader);

            log.info("=== STANDALONE OBJECTMANAGERDB TEST COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            log.error("Standalone ObjectManagerDB test failed", e);
            e.printStackTrace();
        }
    }

    private static FileMetaDataLoader createMetaDataLoader() throws Exception {
        log.info("--- Creating MetaData Loader ---");

        FileLoaderOptions options = new FileLoaderOptions();
        options.setSources(List.of(
            new LocalFileMetaDataSources(List.of(
                "metadata/fishstore-base-metadata.json",
                "metadata/fishstore-db-overlay.json"
            ))
        ));
        options.setShouldRegister(true);
        options.setStrict(true);
        options.setVerbose(false);

        FileMetaDataLoader loader = new FileMetaDataLoader(options, "standalone-loader");
        loader.init();

        // Register the loader so it can be found by MetaDataUtil
        loader.register();

        log.info("MetaData Loader created successfully");
        return loader;
    }

    private static EmbeddedDataSource createDerbyDataSource() {
        log.info("--- Creating Derby DataSource ---");

        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:testdb");
        dataSource.setCreateDatabase("create");

        log.info("Derby DataSource created successfully");
        return dataSource;
    }

    private static ObjectManagerDB createObjectManagerDB(EmbeddedDataSource dataSource) throws Exception {
        log.info("--- Creating ObjectManagerDB ---");

        ObjectManagerDB objectManager = new ObjectManagerDB();
        objectManager.setDriverClass("com.metaobjects.manager.db.driver.DerbyDriver");
        objectManager.setDataSource(dataSource);

        log.info("ObjectManagerDB created successfully");
        return objectManager;
    }

    private static void initializeDatabaseValidator(ObjectManagerDB objectManager, FileMetaDataLoader loader) throws Exception {
        log.info("--- Initializing Database Validator (Auto-create tables) ---");

        MetaClassDBValidatorService validator = new MetaClassDBValidatorService();
        validator.setObjectManager(objectManager);
        validator.setAutoCreate(true);

        // Create a MetaDataLoaderRegistry with our specific loader
        MetaDataLoaderRegistry registry = new MetaDataLoaderRegistry(ServiceRegistryFactory.getDefault());
        registry.registerLoader(loader);
        validator.setMetaDataLoaderRegistry(registry);

        validator.init();

        log.info("Database Validator initialized successfully");
    }

    private static void testMetaDataLoading(FileMetaDataLoader loader) {
        log.info("--- Testing MetaData Loading ---");

        // First, let's see what metadata objects are actually loaded
        Collection<MetaObject> allMetaObjects = loader.getChildren(MetaObject.class);
        log.info("Found {} MetaObjects in loader:", allMetaObjects.size());
        for (MetaObject mo : allMetaObjects) {
            log.info("  Available MetaObject: {} (package: {})", mo.getName(), mo.getPackage());
        }

        // Try to get MetaObjects by name with package prefix
        try {
            MetaObject storeMeta = loader.getMetaObjectByName("fishstore::Store");
            MetaObject breedMeta = loader.getMetaObjectByName("fishstore::Breed");
            MetaObject baseMeta = loader.getMetaObjectByName("fishstore::Base");

            log.info("Loaded MetaObjects:");
            log.info("  Base: {} (fields: {})", baseMeta.getName(), baseMeta.getMetaFields().size());
            log.info("  Store: {} (fields: {})", storeMeta.getName(), storeMeta.getMetaFields().size());
            log.info("  Breed: {} (fields: {})", breedMeta.getName(), breedMeta.getMetaFields().size());

            // Test inheritance
            MetaObject storeSuperObject = storeMeta.getSuperObject();
            log.info("Store inheritance chain: {}", storeSuperObject != null ? storeSuperObject.getName() : "none");

            // DEBUGGING: Check PrimaryKey metadata
            log.info("--- Debugging PrimaryKey Metadata ---");
            log.info("Base MetaObject children: {}", baseMeta.getChildren().size());
            for (MetaData child : baseMeta.getChildren()) {
                log.info("  Base child: {} (type: {}, subType: {})", child.getName(), child.getType(), child.getSubType());
                if ("key".equals(child.getType()) && "primary".equals(child.getSubType())) {
                    PrimaryKey pk = (PrimaryKey) child;
                    log.info("    PrimaryKey found! Auto-increment strategy: {}", pk.getAutoIncrementStrategy());
                    log.info("    PrimaryKey key fields: {}", pk.getKeyFields().size());
                    for (MetaField field : pk.getKeyFields()) {
                        log.info("      Key field: {}", field.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load metadata objects with package prefix, trying simple names...");

            // Try simple names as fallback
            try {
                MetaObject storeMeta = loader.getMetaObjectByName("Store");
                log.info("Successfully loaded Store with simple name");
            } catch (Exception e2) {
                log.error("Also failed with simple name: {}", e2.getMessage());
            }
        }
    }

    private static void testPersistence(ObjectManagerDB objectManager, FileMetaDataLoader loader) throws Exception {
        log.info("--- Testing ObjectManagerDB Persistence ---");

        ObjectConnection connection = objectManager.getConnection();
        try {
            // Test 1: Create a Store and initialize it properly
            log.info("Creating Store...");
            Store testStore = new Store();

            // Initialize the metadata connection using ObjectManagerDB
            MetaObject storeMeta = loader.getMetaObjectByName("fishstore::Store");
            testStore.setMetaData(storeMeta);

            // Debug: Check auto-increment configuration for all fields
            log.info("=== AUTO-INCREMENT DEBUGGING ===");
            for (MetaField field : storeMeta.getMetaFields()) {
                log.info("Field: {} (type: {})", field.getName(), field.getSubType());

                // Check if this field is part of a PrimaryKey
                for (PrimaryKey primaryKey : storeMeta.getChildren(PrimaryKey.class)) {
                    if (primaryKey.getKeyFields().contains(field)) {
                        log.info("  -> Field '{}' is part of PrimaryKey with auto-increment: {}",
                            field.getName(), primaryKey.getAutoIncrementStrategy());
                    }
                }
            }
            log.info("=== END AUTO-INCREMENT DEBUGGING ===");

            // Now we can set attributes since we have metadata
            testStore.setName("Test Store DB");
            testStore.setMaxTanks(15);

            log.info("Persisting Store: {}", testStore);
            log.info("Store ID before persist: {}", testStore.getId());
            objectManager.createObject(connection, testStore);
            log.info("Successfully persisted Store with ID: {}", testStore.getId());

            // Test 2: Create a Breed and initialize it properly
            log.info("Creating Breed...");
            Breed testBreed = new Breed();

            // Initialize the metadata connection
            MetaObject breedMeta = loader.getMetaObjectByName("fishstore::Breed");
            testBreed.setMetaData(breedMeta);

            // Now we can set attributes
            testBreed.setName("Derby Test Fish");
            testBreed.setAgressionLevel(3);

            log.info("Persisting Breed: {}", testBreed);
            objectManager.createObject(connection, testBreed);
            log.info("Successfully persisted Breed with ID: {}", testBreed.getId());

            // Test 3: Query back the data
            log.info("Querying back persisted data...");

            MetaObject storeMetaQuery = loader.getMetaObjectByName("fishstore::Store");
            Collection<?> allStores = objectManager.getObjects(connection, storeMetaQuery);
            log.info("Retrieved {} stores from database", allStores.size());

            MetaObject breedMetaQuery = loader.getMetaObjectByName("fishstore::Breed");
            Collection<?> allBreeds = objectManager.getObjects(connection, breedMetaQuery);
            log.info("Retrieved {} breeds from database", allBreeds.size());

            // Display results
            for (Object store : allStores) {
                log.info("  Retrieved Store: {}", store);
            }

            for (Object breed : allBreeds) {
                log.info("  Retrieved Breed: {}", breed);
            }

        } finally {
            connection.close();
        }
    }
}