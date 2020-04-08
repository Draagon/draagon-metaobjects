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
package com.draagon.meta.loader.xml;

import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author dmealing
 */
public class XMLMetaDataSuperTest extends XMLMetaDataLoaderTestBase{
    static final Log log = LogFactory.getLog(XMLMetaDataSuperTest.class);


    @Test
    public void testSuperListFruit() {
        List<MetaObject> result = loader.getMetaDataBySuper("produce::v1::fruit::Fruit");

        for(MetaObject mo : result){
            if ( log.isDebugEnabled()) {
                log.debug(String.format("[%s]->[%s]",
                        ((null != mo.getSuperObject()) ? mo.getSuperObject().getName() : "root"), mo.getName()));
            }

            log.info( mo.toString() );
        }

        assertEquals("children", 3, result.size());
    }

    @Test
    public void testSuperListVegetable() {
        List<MetaObject> result = loader.getMetaDataBySuper("produce::v1::fruit::Vegetable");

        for(MetaObject mo : result){
            if ( log.isDebugEnabled()) {
                log.debug(String.format("[%s]->[%s]",
                    ((null != mo.getSuperObject()) ? mo.getSuperObject().getName() : "root"), mo.getName()));
            }
        }

        assertEquals("children", 1, result.size());
    }
}
