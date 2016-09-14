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
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.test.Apple;
import com.draagon.meta.test.Orange;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dmealing
 */
public class XMLMetaDataLoaderTest {
    
    private MetaDataLoader loader = null;
    
    @Before
    public void initLoader() throws Exception {
                
        // Initialize the loader
        XMLFileMetaDataLoader xl = new XMLFileMetaDataLoader();        
        xl.setSource( "meta.fruit.xml" );        
        xl.init();
        
        this.loader = xl;
    }
    
    @Test
    public void testApple() throws Exception {
        
        MetaObject ao = MetaObject.forName( "produce::Apple" );
        Apple apple = (Apple) ao.newInstance();
        
        Apple apple2 = new Apple();
        
        MetaObject mo = loader.getMetaObjectFor( apple );
        assertEquals( mo.getName(), MetaDataLoader.findMetaObject( apple2 ).getName() );
        
        apple.setName( "granny" );
        apple2.setName( "macintosh" );
        
        MetaField nameField = mo.getMetaField( "name" );        
        String name = nameField.getString( apple );
        
        assertEquals( "name field", name, apple.getName() );
        
        apple.setId( 100L );
        
        MetaField idField = mo.getMetaField( "id" );
        Long id = idField.getLong( apple );
        
        assertEquals( "id field", id, apple.getId() );
        
        assertEquals( "id field isKey=true", "true", idField.getAttribute( "isKey" ).toString() );
    }

    @Test
    public void testOrange() throws Exception {
        
        Orange orange = new Orange();
        
        MetaObject mo = MetaObject.forObject( orange );
        assertEquals( "produce::Orange", mo.getName() );

        MetaField idField = mo.getMetaField( "id" );
        
        assertEquals( "id field isKey=false", "false", idField.getAttribute( "isKey" ).toString() );
    }

    @Test
    public void testBasket() throws Exception {
        
        MetaObject mo = MetaObject.forName( "container::Basket" );
        ValueObject basket = (ValueObject) mo.newInstance();
        
        MetaField idField = mo.getMetaField( "id" );
        
        assertEquals( "id field isKey=false", "true", idField.getAttribute( "isKey" ).toString() );
        
        basket.setLong( "id", 1L );
        basket.setInt( "oranges", 3 );
        basket.setInt( "apples", 5 );
        
        assertEquals( "id", basket.getLong("id"), mo.getMetaField("id").getLong( basket ));
        assertEquals( "oranges", basket.getInt("oranges"), mo.getMetaField("oranges").getInt( basket ));
        assertEquals( "apples", basket.getInt("apples"), mo.getMetaField("apples").getInt( basket ));
    }
}
