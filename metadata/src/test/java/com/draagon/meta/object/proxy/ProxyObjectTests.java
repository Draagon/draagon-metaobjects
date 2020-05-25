package com.draagon.meta.object.proxy;

import com.draagon.meta.key.ObjectKey;
import com.draagon.meta.key.PrimaryKey;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.test.proxy.fruitbasket.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.*;

public class ProxyObjectTests {

    protected final String BASKET_TO_FRUIT = "simple::fruitbasket::BasketToFruit";
    protected MetaDataLoader loader = null;

    protected int numBaskets=0;
    protected int numFruit=0;
    protected int numInBaskets=0;
    protected long missingOrangeId=0;
    protected long basket2Id=0;

    protected Map<String,Object> objectStore = new HashMap<>();

    @Before
    public void initLoader() throws ClassNotFoundException {
        loader = SimpleLoader.createManual("proxytest", Arrays.asList(
                //"com/draagon/meta/loader/simple/fruitbasket-metadata.xml",
                "com/draagon/meta/loader/simple/fruitbasket-proxy-metadata.xml"
        ));

        setupData();
    }

    protected void setupData() throws ClassNotFoundException {

        numBaskets=0;
        numFruit=0;
        numInBaskets=0;

        Basket b1 = newBasket( Basket.class, 1l, "basket1" ); numBaskets++;
        Basket b2 = newBasket( Basket.class, 2l, "basket2" ); numBaskets++;
        basket2Id = b2.getId();

        addToBasket(b1,newFruit( Apple.class, 100l, "apple1" ));   numFruit++; numInBaskets++;
        addToBasket(b1,newFruit( Macintosh.class, 101l, "mac1" )); numFruit++; numInBaskets++;
        addToBasket(b1,newFruit( Orange.class, 102l, "orange1" )); numFruit++; numInBaskets++;

        addToBasket(b2,newFruit( Apple.class, 200l, "apple2" ));   numFruit++; numInBaskets++;
        addToBasket(b2,newFruit( Macintosh.class, 201l, "mac2" )); numFruit++; numInBaskets++;

        Orange o = newFruit( Orange.class, 202l, "orange2" ); numFruit++;
        missingOrangeId=o.getId();
    }

    @Test
    public void getAllTest() throws ClassNotFoundException {
        Assert.assertEquals( numBaskets, getAllFromStore(Basket.class).size());
        Assert.assertEquals( numFruit, getAllFromStore(Fruit.class).size());
        Assert.assertEquals( numInBaskets, getAllFromStore(BasketToFruit.class).size());
    }

    @Test
    public void appleTest() throws ClassNotFoundException {
        Long knownId = 100l;

        Apple apple = getFromStore(Apple.class, knownId);
        //apple.setId(5l);
        Long id = apple.getId();
        Assert.assertEquals(knownId,id);

        MetaObject mo = apple.getMetaData();
        PrimaryKey key = mo.getPrimaryKey();
        Object [] val = key.getObjectKey( apple ).get();
        Assert.assertEquals( 1, val.length );

        Assert.assertEquals(id, val[0]);
    }

    @Test
    public void basketTest() throws ClassNotFoundException {
        Long knownId = 1L;

        Basket b = getFromStore( Basket.class, knownId);
        Long id = b.getId();
        Assert.assertEquals(knownId,id);

        MetaObject mo = b.getMetaData();
        PrimaryKey key = mo.getPrimaryKey();
        Object [] val = key.getObjectKey( b ).get();
        Assert.assertEquals( 1, val.length );

        Assert.assertEquals(id, val[0]);
    }

    @Test
    public void basketSize() throws ClassNotFoundException {
        Assert.assertEquals( numInBaskets, getAllFromStore(BasketToFruit.class).size());

        Orange o = getFromStore( Orange.class, missingOrangeId );
        Basket b2 = getFromStore( Basket.class, basket2Id );
        addToBasket(b2, o);

        Assert.assertEquals( numInBaskets+1, getAllFromStore(BasketToFruit.class).size());

        IllegalStateException ex = null;
        try {
            addToBasket(b2, o);
        } catch( IllegalStateException e ) {
            ex = e;
        }

        Assert.assertTrue( ex != null );

        Assert.assertEquals( numInBaskets+1, getAllFromStore(BasketToFruit.class).size());
    }

    ////////////////////////////////////////////////////////////////////////
    // Helper and Object Store Methods

    protected String getType( Class clazz ) {
        if ( Fruit.class.isAssignableFrom( clazz )) return "fruit";
        else if ( Basket.class.isAssignableFrom( clazz )) return "basket";
        else if ( BasketToFruit.class.isAssignableFrom( clazz ) ) return "basketToFruit";
        // return clazz.getSimpleName().toLowerCase();
        throw new IllegalStateException( "Unsupported object type: "+clazz.getName());
    }

    protected String getType( Object o ) {
        if ( o instanceof Fruit ) return "fruit";
        else if ( o instanceof Basket ) return "basket";
        else if ( o instanceof BasketToFruit ) return "basketToFruit";
        throw new IllegalStateException( "Unsupported object type: "+o.getClass().getName());
    }

    protected String toKeyStr( ObjectKey key ) {
        return key.getAsString();
    }

    protected void addToStore( MetaObjectAware o ) {
        ObjectKey key = o.getMetaData().getPrimaryKey().getObjectKey( o );
        objectStore.put( getType(o)+":"+toKeyStr(key), o );
    }

    protected <T> T getFromStore( Class<T> clazz, Object singleKey ) {
        Object [] key = null;
        if ( singleKey instanceof Array) {
            key = (Object[]) singleKey;
        } else {
            key = new Object[1];
            key[0] = singleKey;
        }
        ObjectKey objectKey = new ObjectKey(key);
        return getFromStore( clazz, objectKey );
    }

    protected <T> List<T> getAllFromStore( Class<T> clazz ) {
        List<T> all = new ArrayList<>();
        String type = getType(clazz);
        for (String key : objectStore.keySet() ) {
            if ( key.startsWith( type+":")) all.add( (T) objectStore.get(key));
        }
        return all;
    }

    protected <T> T getFromStore( Class<T> clazz, ObjectKey key ) {
        String type = getType(clazz);
        String keyStr = toKeyStr(key);
        String storeKey = type+":"+keyStr;
        return (T) objectStore.get(storeKey);
    }

    protected <T extends Fruit> T newFruit(Class<T> clazz, long id, String name ) throws ClassNotFoundException {
        Fruit f = loader.newObjectInstance( clazz );
        f.setId(id);
        f.setName(name);

        if ( getFromStore( clazz, f.getId()) != null )
            throw new IllegalStateException( "Fruit with id ("+id+") already exists");

        addToStore(f);
        return (T) f;
    }

    protected <T extends Basket> T newBasket(Class<T> clazz, long id, String name ) throws ClassNotFoundException {
        Basket b = loader.newObjectInstance( clazz );
        b.setId(id);
        b.setName(name);

        if ( getFromStore( clazz, b.getId()) != null )
            throw new IllegalStateException( "Basket with id ("+id+") already exists");

        addToStore(b);
        return (T) b;
    }

    protected Basket addToBasket( Basket b, Fruit f ) throws ClassNotFoundException {

        if ( Boolean.TRUE.equals(f.getInBasket() ))
            throw new IllegalStateException("Fruit already in a basket: "+f);

        BasketToFruit bf = loader.newObjectInstance( BasketToFruit.class );
        bf.setBasketId( b.getId() );
        bf.setFruitId( f.getId() );
        addToStore(bf);

        f.setBasketId(b.getId());
        f.setInBasket(true);

        return b;
    }
}
