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


import com.draagon.meta.validator.RequiredValidator;
import com.draagon.meta.validator.LengthValidator;
import java.net.URI;
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

        // Trigger validator registrations
        try {
            new RequiredValidator("test");
            new LengthValidator("test");
        } catch (Exception e) {
            // Ignore - just triggering static registration
        }
        
        // Trigger view registrations
        try {
            Class.forName("com.draagon.meta.view.BasicMetaView");
        } catch (Exception e) {
            // Ignore - just triggering static registration
        }

            // Initialize the loader
            loader = new SimpleLoader(
                    getClass().getSimpleName() + "-" + i.incrementAndGet())
                    .setSourceURIs(sources)
                    .init();

        return loader;
    }


}
