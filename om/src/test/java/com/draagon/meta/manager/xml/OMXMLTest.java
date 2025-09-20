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
package com.draagon.meta.manager.xml;

import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalFileMetaDataSources;
import com.draagon.meta.loader.file.FileLoaderOptions;
import com.draagon.meta.manager.ObjectConnection;

import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.test.produce.v1.Apple;
import org.junit.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author dmealing
 */
public class OMXMLTest {
    
    protected ObjectManagerXML omxml = null;
    protected MetaDataLoader loader = null;
    protected ObjectConnection oc = null;

    protected AtomicInteger i = new AtomicInteger();
    
    @Before
    public void setupDB() throws Exception {
                
        if ( loader == null ) {

            // Initialize the loader
            loader = new FileMetaDataLoader(
                    new FileLoaderOptions()
                            .addSources(new LocalFileMetaDataSources(
                                    Arrays.asList(
                                            "metadata/test/produce/v1/produce-v1.bundle")
                            ))
                            .setShouldRegister(true)
                            .setVerbose(false),
                    getClass().getSimpleName() + "-" + i.incrementAndGet())
                    .init();

            omxml = new ObjectManagerXML();
            omxml.setLocation( "testdata/produce/v1/" );
            omxml.init();

            oc = omxml.getConnection();
        }        
    }

    @Test
    @org.junit.Ignore("Temporarily disabled during types config cleanup")
    public void testFruit() {

        Apple apple = (Apple) loader.getMetaObjectByName( "produce::v1::fruit::Apple").newInstance();
        apple.setId((long)i.get());
        apple.setName( "Apple-"+i.get());
        omxml.createObject( oc, apple );

        apple = (Apple) loader.getMetaObjectByName( "produce::v1::fruit::Apple").newInstance();
        apple.setId(88L);
        apple.setName( "Apple-88" );
        omxml.createObject( oc, apple );

        System.out.println( "Apples: "+ omxml.getObjects( oc, apple.getMetaData() ));

        Apple a88 = (Apple) omxml.getObjects( oc, apple.getMetaData(), new QueryOptions( new Expression("id",88L))).iterator().next();

        Assert.assertEquals( "Apple 88", apple, a88 );
    }
    
    @After
    public synchronized void destroyEntityManager() throws Exception {

        omxml.releaseConnection(oc);
        loader.destroy();
    }
}
