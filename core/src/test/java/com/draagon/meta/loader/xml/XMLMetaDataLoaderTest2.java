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

import static org.junit.Assert.*;

/**
 *
 * @author dmealing
 */
public class XMLMetaDataLoaderTest2 {
    
    private MetaDataLoader loader = null;

    @Before
    public void initLoader() throws Exception {
                
        // Initialize the loader
        XMLFileMetaDataLoader xl = new XMLFileMetaDataLoader( "test" );
        List<String> list = new ArrayList<String>();
        list.add( "metadata/test/produce/v1/produce-v1.bundle" );
        list.add( "metadata/test/produce/v1/meta.fruit.overlay.xml" );
        xl.init(new LocalMetaDataSources( "src/test/resources", list));
        xl.register();

        this.loader = xl;
    }
    
    @Test
    public void testApple() throws Exception {
        
        MetaObject ao = MetaDataRegistry.findMetaDataByName( MetaObject.class,"produce::v1::fruit::Apple" );
        Apple apple = (Apple) ao.newInstance();
        
        Apple apple2 = new Apple();
        
        MetaObject mo = loader.getMetaObjectFor( apple );
        assertEquals( mo.getName(), MetaDataRegistry.findMetaObject( apple2 ).getName() );
        
        apple.setName( "granny" );
        apple2.setName( "macintosh" );
        
        MetaField nameField = mo.getMetaField( "name" );        
        String name = nameField.getString( apple );
        
        assertEquals( "name field", name, apple.getName() );
        
        apple.setId( 100L );
        
        MetaField idField = mo.getMetaField( "id" );
        Long id = idField.getLong( apple );
        
        assertEquals( "id field", id, apple.getId() );
        
        assertEquals( "id field isKey=true", "true", idField.getMetaAttr( "isKey" ).toString() );
    }

    @Test
    public void testOrange() throws Exception {
        
        Orange orange = new Orange();
        
        MetaObject mo = MetaDataRegistry.findMetaObject( orange );
        assertEquals( "produce::v1::fruit::Orange", mo.getName() );

        MetaField idField = mo.getMetaField( "id" );
        
        assertEquals( "id field isKey=true", "true", idField.getMetaAttr( "isKey" ).getValueAsString() );
    }

    @Test
    public void testBasket() throws Exception {
        
        MetaObject mo = MetaDataRegistry.findMetaDataByName( MetaObject.class, "produce::v1::container::Basket" );
        ValueObject basket = (ValueObject) mo.newInstance();
        
        MetaField idField = mo.getMetaField( "id" );
        
        assertEquals( "id field isKey=false", "true", idField.getMetaAttr( "isKey" ).getValueAsString() );
        
        basket.setLong( "id", 1L );
        basket.setInt( "numOranges", 3 );
        basket.setInt( "numApples", 5 );

        // TODO: Add tests for collections of Apples and Oranges
        
        assertEquals( "id", basket.getLong("id"), mo.getMetaField("id").getLong( basket ));
        assertEquals( "oranges", basket.getInt("numOranges"), mo.getMetaField("numOranges").getInt( basket ));
        assertEquals( "apples", basket.getInt("numApples"), mo.getMetaField("numApples").getInt( basket ));
    }

    @Test
    public void testExtensions() throws Exception {

        MetaObject mo = MetaDataRegistry.findMetaDataByName( MetaObject.class, "produce::v1::container::Basket" );
        ValueObject basket = (ValueObject) mo.newInstance();

        MetaField overlayField = mo.getMetaField( "specialOverlay" );
        assertNotNull( "specialOverlay exists on Container", overlayField );

        MetaField extField = mo.getMetaField( "specialExt" );
        assertNotNull( "specialExt exists on Container", extField );
        assertTrue( "specialExt is an ObjectField", (extField instanceof ObjectField) );

        MetaObject extMo = ((ObjectField) extField ).getObjectRef();
        assertNotNull( "Extension Object exists", extMo );
        assertEquals( "Extension Object name == ProduceExt", "produce::v1::ext::ProduceExt", extMo.getName() );
    }


    //@After
    //public void destroyLoader() throws Exception {
    //    loader.destroy();
    //}
}
