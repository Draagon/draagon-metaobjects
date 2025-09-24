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

import com.draagon.meta.registry.SharedTestRegistry;
import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.validator.LengthValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for SimpleLoader tests using shared registry approach.
 *
 * <p>Uses SharedTestRegistry to eliminate test interference and follow
 * the READ-OPTIMIZED architecture where registry is loaded once per
 * application lifetime, not repeatedly created/destroyed.</p>
 *
 * @author dmealing
 */
public class SimpleLoaderTestBase {

    private static final Logger log = LoggerFactory.getLogger(SimpleLoaderTestBase.class);
    private static AtomicInteger i = new AtomicInteger();

    static {
        // Ensure shared registry is initialized when this base class is loaded
        SharedTestRegistry.getInstance();
        log.info("SimpleLoaderTestBase initialized with shared registry: {}",
                SharedTestRegistry.getStatus());
    }

    protected SimpleLoader initLoader(List<URI> sources) {
        // Use shared registry approach - no repeated service discovery
        // The registry is already initialized with all providers loaded

        log.debug("Creating SimpleLoader with shared registry (instance #{})", i.get() + 1);

        // Initialize the loader - registry already contains all discovered types
        SimpleLoader loader = new SimpleLoader(
                getClass().getSimpleName() + "-" + i.incrementAndGet())
                .setSourceURIs(sources)
                .init();

        log.debug("SimpleLoader initialized successfully with {} source URIs", sources.size());
        return loader;
    }
}
