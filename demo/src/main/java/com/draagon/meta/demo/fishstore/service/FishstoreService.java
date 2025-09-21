/*
 * Copyright (c) 2012 Doug Mealing LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Mealing LLC - initial API and implementation and/or initial documentation
 */
package com.draagon.meta.demo.fishstore.service;

import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.ObjectManager;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.spring.MetaDataService;
import com.draagon.meta.demo.fishstore.*;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dmealing
 */
@Service
public class FishstoreService {
    
    private static final Logger log = LoggerFactory.getLogger(FishstoreService.class);
    
    @Autowired
    private ObjectManager om;
    
    @Autowired
    private MetaDataService metaDataService;
    
    /** Initialize the database if needed */     
    public void init() {
        
        ObjectConnection oc = om.getConnection();
        try {
            MetaObject storeMO = metaDataService.findMetaObjectByName("Store");
            Collection<?> stores = om.getObjects(oc, storeMO);
            
            if (stores.isEmpty()) {
                log.info("Initializing fishstore with sample data...");
                
                // Create sample stores
                createSampleStore(oc, storeMO, "Downtown Aquarium", 25);
                createSampleStore(oc, storeMO, "Seaside Fish Market", 15);
                createSampleStore(oc, storeMO, "Pet Paradise", 40);
                
                // Create sample breeds
                MetaObject breedMO = metaDataService.findMetaObjectByName("Breed");
                createSampleBreed(oc, breedMO, "Goldfish", 2);
                createSampleBreed(oc, breedMO, "Angelfish", 3);
                createSampleBreed(oc, breedMO, "Betta", 5);
                createSampleBreed(oc, breedMO, "Shark", 9);
                
                log.info("Sample data initialization completed.");
            } else {
                log.info("Database already contains {} stores", stores.size());
            }
        } catch (Exception e) {
            log.error("Error initializing fishstore data", e);
        }
        finally {
            oc.close();
        }
    }
    
    private void createSampleStore(ObjectConnection oc, MetaObject storeMO, String name, int maxTanks) {
        try {
            Store store = new Store();
            store.setName(name);
            store.setMaxTanks(maxTanks);
            
            om.createObject(oc, store);
            log.info("Created store: {}", name);
        } catch (Exception e) {
            log.error("Error creating store: " + name, e);
        }
    }
    
    private void createSampleBreed(ObjectConnection oc, MetaObject breedMO, String name, int aggressionLevel) {
        try {
            Breed breed = new Breed();
            breed.setName(name);
            breed.setAgressionLevel(aggressionLevel);
            
            om.createObject(oc, breed);
            log.info("Created breed: {} (aggression: {})", name, aggressionLevel);
        } catch (Exception e) {
            log.error("Error creating breed: " + name, e);
        }
    }
    
    /**
     * Get all stores for the React API
     */
    public Collection<Store> getAllStores() {
        ObjectConnection oc = om.getConnection();
        try {
            MetaObject storeMO = metaDataService.findMetaObjectByName("Store");
            return (Collection<Store>) om.getObjects(oc, storeMO);
        } catch (Exception e) {
            log.error("Error getting stores", e);
            return java.util.Collections.emptyList();
        }
        finally {
            oc.close();
        }
    }
    
    /**
     * Get all breeds for the React API
     */
    public Collection<Breed> getAllBreeds() {
        ObjectConnection oc = om.getConnection();
        try {
            MetaObject breedMO = metaDataService.findMetaObjectByName("Breed");
            return (Collection<Breed>) om.getObjects(oc, breedMO);
        } catch (Exception e) {
            log.error("Error getting breeds", e);
            return java.util.Collections.emptyList();
        }
        finally {
            oc.close();
        }
    }
}
