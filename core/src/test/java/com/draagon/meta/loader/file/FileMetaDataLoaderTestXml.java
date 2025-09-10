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

import com.draagon.meta.InvalidValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.ObjectField;
import com.draagon.meta.loader.MetaDataRegistry;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.data.DataObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.test.produce.v1.Apple;
import com.draagon.meta.test.produce.v1.Orange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author dmealing
 */
public class FileMetaDataLoaderTestXml extends FileMetaDataLoaderTestBase {

    protected FileMetaDataLoader loader = null;

    @Before
    public void initLoader() { this.loader = super.initLoader("xml");}

    @After
    public void destroyLoader() { this.loader.destroy(); }

    @Test
    public void testApple() {
        
        MetaObject ao = MetaDataRegistry.findMetaDataByName( MetaObject.class, "produce::v1::fruit::Apple" );
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

        assertEquals( "id field isKey=true", "id", ((MetaObject)idField.getParent())
                .getPrimaryKey().getKeyFields().iterator().next().getName());
    }

    @Test
    public void testOrange()  {
        
        Orange orange = new Orange();

        MetaObject mo = MetaDataRegistry.findMetaObject( orange );
        assertEquals( "produce::v1::fruit::Orange", mo.getName() );

        // Test DefaultValues
        mo.setDefaultValues(orange);
        assertEquals( Boolean.FALSE, orange.getInBasket());

        MetaField idField = mo.getMetaField( "id" );

        assertEquals( "id field isKey=true", "id", ((MetaObject)idField.getParent())
                .getPrimaryKey().getKeyFields().iterator().next().getName());

        // See if it's valid
        try {
            mo.performValidation(orange);
        } catch( InvalidValueException e ) {
            String msg = e.getMessage();
            assertTrue(msg.endsWith("field id"));
        }

        orange.setId(1L);
        orange.setName("testg sd;flkgjs;dlfkgjs;dlkfgjs;ldkfjgs;lkdfjgs;ldkfjgs;ldkfjgs;lkdfjgs;ldkfgjs;ldkfjgs;ldkfgjs;ldkfgjs;ldkfjgs;ldkfj");

        try {
            mo.performValidation(orange);
        } catch( InvalidValueException e ) {
            assertTrue(e.getMessage().contains("1 and 50"));
        }

        orange.setName("");

        try {
            mo.performValidation(orange);
        } catch( InvalidValueException e ) {
            assertTrue(e.getMessage().contains("required on field name"));
        }

        orange.setName("orange");
    }

    @Test
    public void testBasket()  {
        
        MetaObject mo = MetaDataRegistry.findMetaDataByName( MetaObject.class, "produce::v1::container::Basket" );
        ValueObject basket = (ValueObject) mo.newInstance();
        
        MetaField idField = mo.getMetaField( "id" );

        assertEquals( "id field isKey=true", "id", ((MetaObject)idField.getParent())
                .getPrimaryKey().getKeyFields().iterator().next().getName());

        // Test DefaultValues
        assertEquals( Integer.valueOf(0), basket.getInt("numApples"));
        assertEquals( 0, basket.getObjectArray(Object.class,"apples").size());
        
        basket.setLong( "id", 1L );
        basket.setInt( "numOranges", 3 );
        basket.setInt( "numApples", 5 );

        // Test collections of Apples and Oranges
        ValueObject apple1 = (ValueObject) loader.getMetaObjectByName("test.produce.v1.fruit.Apple").newInstance();
        apple1.setString("name", "Red Delicious");
        apple1.setString("color", "red");
        
        ValueObject orange1 = (ValueObject) loader.getMetaObjectByName("test.produce.v1.fruit.Orange").newInstance();
        orange1.setString("name", "Naval Orange");
        orange1.setString("color", "orange");
        
        // Test that fruit objects are properly instantiated
        assertNotNull(apple1);
        assertNotNull(orange1);
        assertEquals("Red Delicious", apple1.getString("name"));
        assertEquals("Naval Orange", orange1.getString("name"));
        
        assertEquals( "id", basket.getLong("id"), mo.getMetaField("id").getLong( basket ));
        assertEquals( "oranges", basket.getInt("numOranges"), mo.getMetaField("numOranges").getInt( basket ));
        assertEquals( "apples", basket.getInt("numApples"), mo.getMetaField("numApples").getInt( basket ));
    }

    @Test
    public void testExtensions() {

        MetaObject mo = MetaDataRegistry.findMetaDataByName( MetaObject.class,"produce::v1::container::Basket" );
        ValueObject basket = (ValueObject) mo.newInstance();
        assertTrue( basket.getMetaData() != null );

        MetaField overlayField = mo.getMetaField( "specialOverlay" );
        assertNotNull( "specialOverlay exists on Container", overlayField );

        MetaField extField = mo.getMetaField( "specialExt" );
        assertNotNull( "specialExt exists on Container", extField );
        assertTrue( "specialExt is an ObjectField", (extField instanceof ObjectField) );

        MetaObject extMo = ((ObjectField) extField ).getObjectRef();
        assertNotNull( "Extension Object exists", extMo );
        assertEquals( "Extension Object name == ProduceExt", "produce::v1::container::ext::ProduceExt", extMo.getName() );
    }

    /*@Test(expected=com.draagon.meta.field.MetaFieldNotFoundException.class)
    public void testAllowExtensionsPreventArbitraryField() throws Exception {

        MetaObject mo = MetaObject.forName("produce::v1::container::Basket");
        ValueObject basket = (ValueObject) mo.newInstance();
        assertFalse("allowsExtensions==false", basket.allowsExtensions());
        assertEquals( "produce::v1::container::Basket", mo.getName() );

        assertEquals("has seven fields", 7, basket.getObjectFieldNames().size());

        basket.setString("newField", "newValue"); // Should throw exception

    }

    @Test
    public void testAllowExtensionsAllowArbitraryField() throws Exception {

        MetaObject mo = MetaObject.forName("produce::v1::container::Basket");
        ValueObject basket = (ValueObject) mo.newInstance();
        basket.allowExtensions(true);
        assertTrue("allowsExtensions==true", basket.allowsExtensions());
        assertEquals( "produce::v1::container::Basket", mo.getName() );

        assertEquals("has seven fields", 7, basket.getObjectFieldNames().size());

        basket.setString("newField", "newValue");

        assertTrue("must contain 'newField'", basket.getObjectFieldNames().contains("newField"));
        assertEquals("has eight fields", 8, basket.getObjectFieldNames().size());
    }*/

    //@After
    //public void destroyLoader() throws Exception {
    //    loader.destroy();
    //}
}
