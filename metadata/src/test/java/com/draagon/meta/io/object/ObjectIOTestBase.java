package com.draagon.meta.io.object;

import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.*;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.object.json.JsonObjectReader;
import com.draagon.meta.io.object.json.JsonObjectWriter;
//import com.draagon.meta.io.object.xml.XMLObjectReader;
//import com.draagon.meta.io.object.xml.XMLObjectWriter;
//import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.mapped.MappedMetaObject;
import com.draagon.meta.object.mapped.MappedObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ObjectIOTestBase {

    protected MetaDataLoader loader = null;
    
    public final static class MD {
        static String ID="id";
        static String NAME="name";
        static String OBJ_BASKET ="basket";
        static String OBJ_FRUIT ="fruit";
        static String OBJ_BUG ="bug";
        static String BASKET_FRUITS="fruits";
        static String BASKET_HOLDS="holds";
        static String FRUIT_BUG="bug";
        static String FRUIT_HAS="has";
        static String FRUIT_IN_BASKET ="inBasket";
    }

    protected File getTestFilePath() {
        File f = new File( "./target/tests");
        if ( !f.exists()) f.mkdirs();
        return f;
    }

    protected OutputStream getTestFileOutputStream(String name) throws IOException {
        File f = new File( getTestFilePath(), name );
        f.createNewFile();
        return new FileOutputStream( f );
    }

    protected InputStream getTestFileInputStream(String name) throws FileNotFoundException {
        File f = new File( getTestFilePath(), name );
        return new FileInputStream( f );
    }

    protected Reader getTestFileReader(String name) throws FileNotFoundException {
       return new InputStreamReader( getTestFileInputStream( name ));
    }

    protected Writer getTestFileWriter(String name) throws IOException {
        return new OutputStreamWriter( getTestFileOutputStream(name));
    }

    @Before
    public void setup() {
        MetaDataLoader tempLoader = MetaDataLoader.createManual( false, "json-MappedObject-io-test" );
        tempLoader.init();
        tempLoader.register();
        
        // Create basket object
        MappedMetaObject basket = MappedMetaObject.create(MD.OBJ_BASKET);
        basket.addChild(IntegerField.create( MD.ID, 1 ));
        basket.addChild(StringField.create( MD.NAME, null ));
        
        ObjectArrayField basketFruitsField = ObjectArrayField.create( MD.BASKET_FRUITS );
        basketFruitsField.addChild(StringAttribute.create(MetaObject.ATTR_OBJECT_REF, MD.OBJ_FRUIT));
        basket.addChild(basketFruitsField);
        
        tempLoader.addChild(basket);
        
        // Create fruit object  
        MappedMetaObject fruit = MappedMetaObject.create("fruit");
        fruit.addChild(IntegerField.create( MD.ID, 1 ));
        fruit.addChild(StringField.create( MD.NAME, null ));
        fruit.addChild(BooleanField.create( MD.FRUIT_IN_BASKET, false ));
        
        ObjectField fruitBugField = ObjectField.create(MD.FRUIT_BUG);
        fruitBugField.addChild(StringAttribute.create(MetaObject.ATTR_OBJECT_REF, MD.OBJ_BUG));
        fruit.addChild(fruitBugField);
        
        tempLoader.addChild(fruit);
        
        // Create bug object
        MappedMetaObject bug = MappedMetaObject.create(MD.OBJ_BUG);
        bug.addChild(IntegerField.create( MD.ID, 1 ));
        bug.addChild(StringField.create( MD.NAME, null ));
        
        tempLoader.addChild(bug);
        
        loader = tempLoader.getLoader();
    }

    @After
    public void tearDown() {
        if (loader != null) {
            loader.destroy();
            loader = null;
        }
    }

    protected MappedObject createBasket(int id, String name ) {
        MappedObject o = (MappedObject) loader.getMetaObjectByName( MD.OBJ_BASKET).newInstance();
        o.put( MD.ID, id );
        o.put( MD.NAME, name);
        return o;
    }

    protected MappedObject createFruit(int id, String name ) {
        MappedObject o = (MappedObject) loader.getMetaObjectByName( MD.OBJ_FRUIT).newInstance();
        o.put( MD.ID, id );
        o.put( MD.NAME, name);
        return o;
    }

    protected MappedObject createBug(int id, String name ) {
        MappedObject o = (MappedObject) loader.getMetaObjectByName( MD.OBJ_BUG).newInstance();
        o.put( MD.ID, id );
        o.put( MD.NAME, name);
        return o;
    }

    protected void addToBasket(MappedObject b, MappedObject f ) {
        List<Object> objects = (List<Object>) b.get( MD.BASKET_FRUITS);
        if ( objects == null ) {
            objects = new ArrayList<>();
            b.put( MD.BASKET_FRUITS, objects );
        }
        objects.add( f );
        f.put( MD.FRUIT_IN_BASKET, true );
    }

    @Test
    public void testFruit() throws IOException, MetaDataIOException {

        MappedObject o = createFruit( 1, "apple" );
        runTest( o, "fruit");
    }

    @Test
    public void testBasket() throws IOException, MetaDataIOException {

        MappedObject o = createBasket( 10, "longaberger" );
        runTest(o, "basket");
    }

    @Test
    public void testFruitInBasket() throws IOException, MetaDataIOException {

        MappedObject a = createFruit( 1, "apple" );
        MappedObject b = createBasket( 10, "longaberger" );
        addToBasket( b, a );

        runTest(b, "inbasket");
    }

    @Test
    public void testFruitInBasket2() throws IOException, MetaDataIOException {

        MappedObject b = createBasket( 10, "longaberger" );
        addToBasket( b, createFruit( 1, "apple" ) );
        addToBasket( b, createFruit( 2, "orange" ) );
        addToBasket( b, createFruit( 3, "pear" ) );

        runTest(b, "inbasket2");
    }

    @Test
    public void testFruitInBasketWithBugs() throws IOException, MetaDataIOException {

        MappedObject b = createBasket( 10, "longaberger" );
        addToBasket( b, createFruit( 1, "apple" ) );
        addToBasket( b, createFruit( 2, "orange" ) );
        addToBasket( b, createFruit( 3, "pear" ) );

        MappedObject banana = createFruit( 3, "banana" );
        banana.put( "bug", createBug( 100, "fly"));
        addToBasket( b, banana );

        runTest(b, "inbasketbugs");
    }

    protected abstract void runTest(MappedObject o, String name ) throws IOException, MetaDataIOException;

    /*protected void writeXML( String filename, Object vo ) throws IOException, MetaDataIOException {

        XMLObjectWriter writer = new XMLObjectWriter( loader, getTestFileOutputStream( filename ) );
        //writer.withIndent(true);
        writer.write( vo );
        writer.close();
    }

    protected MappedObject readXML(String filename, MetaObject mo) throws IOException, MetaDataIOException {

        XMLObjectReader reader = new XMLObjectReader( loader, getTestFileInputStream( filename ) );
        MappedObject vo = (MappedObject) reader.read( mo );
        reader.close();
        return vo;
    }*/

    protected void writeJson( String filename, Object vo ) throws IOException, MetaDataIOException {

        JsonObjectWriter writer = new JsonObjectWriter( loader, getTestFileWriter( filename ) );
        writer.withPrettyPrint();
        writer.write( vo );
        writer.close();
    }

    protected MappedObject readJson(String filename, MetaObject mo ) throws IOException, MetaDataIOException {

        JsonObjectReader reader = new JsonObjectReader( loader, getTestFileReader( filename ) );
        MappedObject vo = (MappedObject) reader.read(mo);
        reader.close();
        return vo;
    }
}
