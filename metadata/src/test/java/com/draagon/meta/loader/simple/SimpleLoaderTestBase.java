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
package com.draagon.meta.loader.simple;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.loader.LoaderOptions;
import com.draagon.meta.loader.types.TypesConfig;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author dmealing
 */
public class SimpleLoaderTestBase {

    //protected static MetaDataLoader loaderStatic = null;
    private static AtomicInteger i = new AtomicInteger();

    protected SimpleLoader initLoader(List<URI> sources) {

        SimpleLoader loader = null;

            // Initialize the loader
            loader = new SimpleLoader(
                    getClass().getSimpleName() + "-" + i.incrementAndGet())
                    .setSourceURIs(sources)
                    .init();

        return loader;
    }
}
