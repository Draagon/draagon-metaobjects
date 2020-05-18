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

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author dmealing
 */
public class FileMetaDataLoaderTestBase {

    //protected static MetaDataLoader loaderStatic = null;
    private static AtomicInteger i = new AtomicInteger();

    protected FileMetaDataLoader initLoader( String type ) {

        FileMetaDataLoader loader = null;

        if ("json".equals(type)) {
            // Initialize the loader
            loader = new FileMetaDataLoader(
                    new FileLoaderOptions()
                            .addParser("*.xml", XMLMetaDataParser.class)
                            .addParser("*.json", JsonMetaDataParser.class)
                            .addSources(new LocalMetaDataSources(
                                    "com/draagon/meta/loader/json/metaobjects.types.json"))
                            .addSources(new LocalMetaDataSources(
                                    //"src/test/resources",
                                    Arrays.asList(
                                            "metadata/test/produce/v1/produce-v1-json.bundle",
                                            "metadata/test/produce/v1/overlay/meta.fruit.overlay.json")
                            ))
                            .setShouldRegister(true)
                            .setStrict(true)
                            .setVerbose(false),
                    getClass().getSimpleName() + "-" + i.incrementAndGet())
                    .init();
        }
        else if ( "xml".equals(type)) {
            // Initialize the loader
            loader = new FileMetaDataLoader(
                    new FileLoaderOptions()
                            .addParser("*.xml", XMLMetaDataParser.class)
                            .addParser("*.json", JsonMetaDataParser.class)
                            .addSources(new LocalMetaDataSources(
                                    Arrays.asList(
                                            "com/draagon/meta/loader/xml/metaobjects.types.xml",
                                            "metadata/test/produce/v1/produce-v1.bundle",
                                            "metadata/test/produce/v1/overlay/meta.fruit.overlay.xml")
                            ))
                            .setShouldRegister(true)
                            .setStrict(false)
                            .setVerbose(false),
                    getClass().getSimpleName() + "-" + i.incrementAndGet())
                    .init();
        }
        else {
            throw new MetaDataException( "Unknown initLoader type [" + type + "], must be xml or json" );
        }

        return loader;
    }
}
