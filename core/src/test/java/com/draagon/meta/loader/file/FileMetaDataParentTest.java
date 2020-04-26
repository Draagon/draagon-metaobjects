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
package com.draagon.meta.loader.file;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author dmealing
 */
public class FileMetaDataParentTest extends FileMetaDataLoaderTestBase {

    private static final Log log = LogFactory.getLog(FileMetaDataParentTest.class);

    protected FileMetaDataLoader loader = null;

    @Before
    public void initLoader() { this.loader = super.initLoader("xml");}

    @After
    public void destroyLoader() { this.loader.destroy(); }

    private void testObjectFieldCount(String objectName, boolean includeParentData, int expectedCount) {
        List<MetaObject> result = loader.getChildren(MetaObject.class, includeParentData);

        int fields = 0;
        for(MetaObject mo : result){
            if ( log.isDebugEnabled() ) {
                log.info(String.format("[%s]->[%s]",
                        ((null != mo.getSuperObject()) ? mo.getSuperObject().getName() : "root"), mo.getName()));
            }
            if(mo.getName().equals(objectName)) {
                fields = mo.getMetaFields(includeParentData).size();
                for(MetaField f : mo.getMetaFields(includeParentData)) {
                    if ( log.isDebugEnabled()) log.debug(String.format("field [%s]", f.getName()));
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
