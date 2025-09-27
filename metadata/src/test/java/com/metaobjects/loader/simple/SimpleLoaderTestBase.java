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
package com.metaobjects.loader.simple;

import com.metaobjects.registry.SharedRegistryTestBase;
import java.net.URI;
import java.util.List;

/**
 * Base class for SimpleLoader tests that uses the shared registry approach
 * to prevent registry conflicts between tests.
 *
 * @author dmealing
 */
public class SimpleLoaderTestBase extends SharedRegistryTestBase {

    /**
     * Initialize a loader with specific sources while using the shared registry.
     * This prevents registry conflicts that cause missing type registrations.
     */
    protected SimpleLoader initLoader(List<URI> sources) {
        // Use the shared registry approach to create a loader with specific sources
        return createTestLoader(getClass().getSimpleName(), sources);
    }
}
