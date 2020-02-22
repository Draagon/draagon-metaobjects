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

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author dmealing
 */
public class XMLMetaDataParentTest extends XMLMetaDataLoaderTestBase{
    static final Log log = LogFactory.getLog(XMLMetaDataParentTest.class);


    private void testObjectFieldCount(String objectName, boolean includeParentData, int expectedCount) {
        List<MetaObject> result = loader.getMetaData(MetaObject.class, includeParentData);

        log.info("Parent -> Child");
        int fields = 0;
        for(MetaObject mo : result){
            log.info(String.format("[%s]->[%s]",
                    ((null != mo.getSuperObject()) ? mo.getSuperObject().getName() : "root"), mo.getName()));
            if(mo.getName().equals(objectName)) {
                fields = mo.getMetaFields(includeParentData).size();
                for(MetaField f : mo.getMetaFields(includeParentData)) {
                    log.info(String.format("field [%s]", f.getName()));
                }
            }
        }

        assertEquals(objectName + " fields", expectedCount, fields);
    }

    @Test
    public void testOrangeSansParent() {
        testObjectFieldCount("produce::v1::fruit::Orange", false, 1);
    }

    @Test
    public void testOrangeWithParent() {
        testObjectFieldCount("produce::v1::fruit::Orange", true, 6);
    }

}
