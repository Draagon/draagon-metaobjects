package com.metaobjects.demo.fishstore.service;

import com.metaobjects.demo.fishstore.domain.*;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.ObjectManager;
import com.metaobjects.object.MetaObject;
import com.metaobjects.spring.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service to test ObjectManagerDB functionality with ManagedObject classes
 * and Derby database auto-table creation
 */
@Service
public class ObjectManagerDBTestService {

    private static final Logger log = LoggerFactory.getLogger(ObjectManagerDBTestService.class);

    @Autowired
    private ObjectManager om;

    @Autowired
    private MetaDataService metaDataService;

    /**
     * Test ObjectManagerDB functionality with ManagedObject classes
     */
    public void testObjectManagerDB() {
        log.info("=== TESTING OBJECTMANAGERDB FUNCTIONALITY ===");

        try {
            // Test 1: Verify metadata loading
            testMetadataLoading();

            // Test 2: Test Derby table creation
            testTableCreation();

            // Test 3: Test ManagedObject persistence
            testManagedObjectPersistence();

            // Test 4: Test data retrieval
            testDataRetrieval();

            log.info("=== OBJECTMANAGERDB TEST COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            log.error("ObjectManagerDB test failed", e);
            throw new RuntimeException("ObjectManagerDB test failed", e);
        }
    }

    private void testMetadataLoading() {
        log.info("--- Test 1: Metadata Loading ---");

        // Verify that metadata objects are loaded correctly
        MetaObject storeMeta = metaDataService.findMetaObjectByName("Store");
        MetaObject breedMeta = metaDataService.findMetaObjectByName("Breed");

        log.info("Store MetaObject loaded: {}", storeMeta.getName());
        log.info("Breed MetaObject loaded: {}", breedMeta.getName());

        // Verify inheritance is working
        MetaObject superObject = storeMeta.getSuperObject();
        log.info("Store inheritance chain: {}", superObject != null ? superObject.getName() : "none");
        log.info("Store fields: {}", storeMeta.getMetaFields().size());
    }

    private void testTableCreation() {
        log.info("--- Test 2: Derby Table Creation ---");

        ObjectConnection oc = om.getConnection();
        try {
            // Try to query the STORE table to see if it was auto-created
            MetaObject storeMeta = metaDataService.findMetaObjectByName("Store");

            // This will trigger table creation if it doesn't exist
            Collection<?> stores = om.getObjects(oc, storeMeta);
            log.info("Successfully accessed STORE table. Found {} existing records.", stores.size());

            // Try BREED table as well
            MetaObject breedMeta = metaDataService.findMetaObjectByName("Breed");
            Collection<?> breeds = om.getObjects(oc, breedMeta);
            log.info("Successfully accessed BREED table. Found {} existing records.", breeds.size());

        } finally {
            oc.close();
        }
    }

    private void testManagedObjectPersistence() {
        log.info("--- Test 3: ManagedObject Persistence ---");

        ObjectConnection oc = om.getConnection();
        try {
            // Create and persist a test store using Store
            Store testStore = new Store();
            testStore.setName("ObjectManagerDB Test Store");
            testStore.setMaxTanks(10);

            log.info("Created Store: {}", testStore);

            // Persist to database
            om.createObject(oc, testStore);
            log.info("Successfully persisted Store with ID: {}", testStore.getId());

            // Create and persist a test breed using Breed
            Breed testBreed = new Breed();
            testBreed.setName("Test Goldfish");
            testBreed.setAgressionLevel(2);

            log.info("Created Breed: {}", testBreed);

            // Persist to database
            om.createObject(oc, testBreed);
            log.info("Successfully persisted Breed with ID: {}", testBreed.getId());

        } finally {
            oc.close();
        }
    }

    private void testDataRetrieval() {
        log.info("--- Test 4: Data Retrieval ---");

        ObjectConnection oc = om.getConnection();
        try {
            // Retrieve all stores
            MetaObject storeMeta = metaDataService.findMetaObjectByName("Store");
            Collection<?> allStores = om.getObjects(oc, storeMeta);

            log.info("Retrieved {} stores from database:", allStores.size());
            for (Object store : allStores) {
                if (store instanceof Store) {
                    Store managedStore = (Store) store;
                    log.info("  Store: {}", managedStore);
                }
            }

            // Retrieve all breeds
            MetaObject breedMeta = metaDataService.findMetaObjectByName("Breed");
            Collection<?> allBreeds = om.getObjects(oc, breedMeta);

            log.info("Retrieved {} breeds from database:", allBreeds.size());
            for (Object breed : allBreeds) {
                if (breed instanceof Breed) {
                    Breed managedBreed = (Breed) breed;
                    log.info("  Breed: {}", managedBreed);
                }
            }

        } finally {
            oc.close();
        }
    }
}