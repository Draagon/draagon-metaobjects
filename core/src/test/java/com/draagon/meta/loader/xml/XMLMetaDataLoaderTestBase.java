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
import com.draagon.meta.field.ObjectField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.test.Apple;
import com.draagon.meta.test.Orange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    protected MetaDataLoader loader = null;

    @Before
    public void initLoader() throws Exception {


        // Initialize the loader
        XMLFileMetaDataLoader xl = new XMLFileMetaDataLoader( "test" );
        List<String> list = new ArrayList<String>();
        list.add( "metadata/test/produce/v1/produce-v1.bundle" );
        list.add( "metadata/test/produce/v1/meta.fruit.overlay.xml" );
        xl.init(new LocalMetaDataSources(list));
        xl.register();

        this.loader = xl;
    }

    @After
    public void destroyLoader() throws Exception {
        loader.destroy();
    }
}
