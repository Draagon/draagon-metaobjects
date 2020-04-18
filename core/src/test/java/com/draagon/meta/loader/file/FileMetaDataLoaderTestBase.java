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

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.config.FileLoaderConfig;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author dmealing
 */
public class FileMetaDataLoaderTestBase {

    //protected static MetaDataLoader loaderStatic = null;
    private static int counter=1;

    protected MetaDataLoader loader = null;

    @Before
    public void initLoaderStatic() {

        synchronized(this) {

            // Initialize the loader
            MetaDataLoader xl = new FileMetaDataLoader(
                    new FileLoaderConfig()
                                .addParser( "*.xml", XMLMetaDataParser.class )
                                .addParser( "*.json", JsonMetaDataParser.class )
                                .addSources( new LocalMetaDataSources(
                                    Arrays.asList(
                                            "com/draagon/meta/loader/meta.types.xml",
                                            "metadata/test/produce/v1/produce-v1.bundle",
                                            "metadata/test/produce/v1/meta.fruit.overlay.xml")
                                ))
                                .setShouldRegister( true ),
                    getClass().getSimpleName() + "-" + counter++)
                        .init();

            this.loader = xl;
        }
    }

    //@Before
    //public void initLoader() throws Exception {
    //    this.loader = loaderStatic;
    //}

    @After
    public void destroyLoader() throws Exception {
        //this.loader = null;
        this.loader.destroy();
    }

    //@AfterClass
    //public static void destroyLoaderStatic() throws Exception {
    //    loaderStatic.destroy();
    //}
}
