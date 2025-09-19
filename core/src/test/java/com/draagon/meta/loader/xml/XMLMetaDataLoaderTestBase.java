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

import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.FileLoaderOptions;
import com.draagon.meta.loader.file.LocalFileMetaDataSources;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author dmealing
 */
public class XMLMetaDataLoaderTestBase {

    protected static FileMetaDataLoader loaderStatic = null;

    protected FileMetaDataLoader loader = null;

    @BeforeClass
    public synchronized static void initLoaderStatic() {

        if ( loaderStatic == null ) {
            // Initialize the loader using FileMetaDataLoader with XML parser
            FileMetaDataLoader xl = new FileMetaDataLoader(
                new FileLoaderOptions()
                    .addParser( "*.xml", XMLMetaDataParser.class )
                    .setShouldRegister( false )
                    .setAllowAutoAttrs( true )
                    .setStrict( false )
                    .setVerbose( false ),
                "test" );
            
            List<String> list = new ArrayList<String>();
            list.add("metadata/test/produce/v1/produce-v1.bundle");
            list.add("metadata/test/produce/v1/meta.fruit.overlay.xml");
            xl.init(new LocalFileMetaDataSources(null,list));
            xl.register();

            loaderStatic = xl;
        }
    }

    @Before
    public void initLoader() throws Exception {
        this.loader = loaderStatic;
    }

    @After
    public void destroyLoader() throws Exception {
        this.loader = null;
    }

    @AfterClass
    public static void destroyLoaderStatic() throws Exception {
    }
}
