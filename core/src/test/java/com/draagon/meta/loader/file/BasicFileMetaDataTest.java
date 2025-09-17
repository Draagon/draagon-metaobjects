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

import com.draagon.meta.io.MetaDataIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author dmealing
 */
public class BasicFileMetaDataTest extends FileMetaDataLoaderTestBase {

    private static final Log log = LogFactory.getLog(BasicFileMetaDataTest.class);

    protected FileMetaDataLoader loader = null;

    @Before
    public void initLoader() { this.loader = super.initLoader("xml");}

    @After
    public void destroyLoader() { this.loader.destroy(); }

    /*@Test
    public void testTypesConfig() throws MetaDataIOException {

        TypesConfig typesConfig = loader.getTypesConfig();

        assertEquals( "object->pojo->[0]->name=object",
                typesConfig.getTypeByName("object").getSubType("pojo")
                .getChildConfigs().iterator().next().getName(), "object");

        // Write XML
        //XMLObjectWriter writer = new XMLObjectWriter( typesConfig.getMetaData().getLoader(), System.out );
        //writer.write( typesConfig );
        //writer.close();

        // Write Json
        JsonObjectWriter writer2 = new JsonObjectWriter( typesConfig.getMetaData().getLoader(), new OutputStreamWriter( System.out ));
        writer2.withIndent(" ");
        writer2.write( typesConfig );
        writer2.close();
    }*/
}

