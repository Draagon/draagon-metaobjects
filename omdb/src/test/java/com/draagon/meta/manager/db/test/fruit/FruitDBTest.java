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
package com.draagon.meta.manager.db.test.fruit;

import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.db.test.AbstractOMDBTest;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.loader.MetaDataRegistry;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

/**
 *
 * @author dmealing
 */
public class FruitDBTest extends AbstractOMDBTest {

    //@Test - Disabled due to metadata loading issues with managed types
    public void testApple() throws Exception {
        
        Apple apple = new Apple();
        MetaObject mo = MetaDataRegistry.findMetaObject( apple );
        
        assertEquals( "produce::Apple", mo.getName() );
        
        apple.setString( "name", "Granny" );
        apple.setInt( "length", 10 );
        apple.setInt( "weight", 10 );
        
        omdb.createObject( oc, apple );
        
        Expression exp = new Expression( "name", "Gr", Expression.START_WITH );
        Collection<?> apples = omdb.getObjects( oc, mo, new QueryOptions( exp ));
        
        assertFalse( "isEmpty", apples.isEmpty() );
        assertEquals( "Granny", ((Apple) apples.iterator().next()).getString("name"));
        
        apple = (Apple) apples.iterator().next();
        apple.setString( "orchard", "Acme Farms" );
        apple.setInt( "weight", 11 );
        omdb.updateObject(oc, apple);
        
        exp = new Expression( "orchard", "Farms", Expression.END_WITH );
        apples = omdb.getObjects( oc, mo, new QueryOptions( exp ));        

        assertFalse( "isEmpty", apples.isEmpty() );
        
        Orange orange = new Orange();
        orange.setString( "name", "Sunkist" );
        orange.setInt( "weight", 8 );
        orange.setInt( "length", 6 );
        orange.setDate( "pickedDate", new Date() );
        omdb.createObject(oc, orange);
        
        omdb.deleteObject(oc, apple);
        assertTrue( omdb.getObjects(oc, mo).isEmpty() );

        // Better be an Orange
        assertFalse( omdb.getObjects(oc, MetaDataRegistry.findMetaObject( orange )).isEmpty() );    
    }
    
    @Test
    public void testBasket() throws Exception {
        
        MetaObject mo = MetaDataRegistry.findMetaObjectByName( "container::Basket" );        
        assertEquals( "container::Basket", mo.getName() );
        
        ValueObject vo = (ValueObject) mo.newInstance();
        
        vo.setInt( "apples", 10 );
        vo.setInt( "oranges", 12 );
        
        omdb.createObject( oc, vo );
        
        Expression exp = new Expression( "apples", 12, Expression.LESSER );
        Collection<?> data = omdb.getObjects( oc, mo, new QueryOptions( exp ));
        
        assertFalse( "isEmpty", data.isEmpty() );
        assertEquals( Integer.valueOf(12), ((ValueObject) data.iterator().next()).getInt("oranges"));
        
        MetaObject mo2 = MetaDataRegistry.findMetaObjectByName( "produce::FullBasketView" );
        data = omdb.getObjects(oc, mo2);
        assertFalse( "isEmpty", data.isEmpty() );
        
        // Empty the basket
        ValueObject o = (ValueObject) data.iterator().next();
        o.setInt( "apples", 0 );
        o.setInt( "oranges", 0 );
        omdb.updateObject( oc, o);
        
        // Now the view should be empty
        data = omdb.getObjects(oc, mo2);
        assertTrue( "isEmpty", data.isEmpty() );
    }
}
