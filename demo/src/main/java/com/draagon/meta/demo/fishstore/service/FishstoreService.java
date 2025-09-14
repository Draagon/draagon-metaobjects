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
import com.draagon.meta.loader.MetaDataRegistry;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author dmealing
 */
@Service
public class FishstoreService {
    
    @Autowired
    private ObjectManager om;
    
    /** Initialize the database if needed */     
    public void init() {
        
        ObjectConnection oc = om.getConnection();
        try {
            MetaObject storeMO = MetaDataRegistry.findMetaObjectByName( "fishstore::Store" );
            Collection<?> stores = om.getObjects( oc, storeMO );
            if ( stores.isEmpty() ) {
                
                // TODO:  Insert a store
            }
        }
        finally {
            oc.close();
        }
    }
}
