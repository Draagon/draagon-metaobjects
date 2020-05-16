package com.draagon.meta.io.value;

import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.field.*;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.value.json.JsonValueObjectReader;
import com.draagon.meta.io.value.json.JsonValueObjectWriter;
import com.draagon.meta.io.value.xml.XMLValueObjectReader;
import com.draagon.meta.io.value.xml.XMLValueObjectWriter;
import com.draagon.meta.io.xml.XMLIOConstants;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.relation.ref.ObjectReference;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ValueObjectIOTestBase {

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
        loader = MetaDataLoader.createManual( false, "json-valueobject-io-test" )
                .init()
                .addChild(ValueMetaObject.create(MD.OBJ_BASKET)
                        .addChild(IntegerField.create( MD.ID, 1 )
                            .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR, true )))
                        .addChild(StringField.create( MD.NAME, null ))
                        .addChild(ObjectArrayField.create( MD.BASKET_FRUITS )
                            .addChild(ObjectReference.create( MD.BASKET_HOLDS, MD.OBJ_FRUIT)))
                )
                .addChild(ValueMetaObject.create("fruit")
                        .addChild(IntegerField.create( MD.ID, 1 )
                                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR, true )))
                        .addChild(StringField.create( MD.NAME, null ))
                        .addChild(BooleanField.create( MD.FRUIT_IN_BASKET, false ))
                        .addChild(ObjectField.create(MD.FRUIT_BUG)
                            .addChild(ObjectReference.create( MD.FRUIT_HAS, MD.OBJ_BUG)))
                )
                .addChild(ValueMetaObject.create(MD.OBJ_BUG)
                        .addChild(IntegerField.create( MD.ID, 1 )
                                .addChild(BooleanAttribute.create(XMLIOConstants.ATTR_ISXMLATTR, true )))
                        .addChild(StringField.create( MD.NAME, null ))
                );
    }

    protected ValueObject createBasket( int id, String name ) {
        ValueObject o = (ValueObject) loader.getMetaObjectByName( MD.OBJ_BASKET).newInstance();
        o.setInt( MD.ID, id );
        o.setString( MD.NAME, name);
        return o;
    }

    protected ValueObject createFruit( int id, String name ) {
        ValueObject o = (ValueObject) loader.getMetaObjectByName( MD.OBJ_FRUIT).newInstance();
        o.setInt( MD.ID, id );
        o.setString( MD.NAME, name);
        return o;
    }

    protected ValueObject createBug( int id, String name ) {
        ValueObject o = (ValueObject) loader.getMetaObjectByName( MD.OBJ_BUG).newInstance();
        o.setInt( MD.ID, id );
        o.setString( MD.NAME, name);
        return o;
    }

    protected void addToBasket( ValueObject b, ValueObject f ) {
        List<Object> objects = (List<Object>) b.getObject( MD.BASKET_FRUITS);
        if ( objects == null ) {
            objects = new ArrayList<>();
            b.setObject( MD.BASKET_FRUITS, objects );
        }
        objects.add( f );
        f.setBoolean( MD.FRUIT_IN_BASKET, true );
    }

    @Test
    public void testFruit() throws IOException, MetaDataIOException {

        ValueObject o = createFruit( 1, "apple" );
        runTest( o, "fruit");
    }

    @Test
    public void testBasket() throws IOException, MetaDataIOException {

        ValueObject o = createBasket( 10, "longaberger" );
        runTest(o, "basket");
    }

    @Test
    public void testFruitInBasket() throws IOException, MetaDataIOException {

        ValueObject a = createFruit( 1, "apple" );
        ValueObject b = createBasket( 10, "longaberger" );
        addToBasket( b, a );

        runTest(b, "inbasket");
    }

    @Test
    public void testFruitInBasket2() throws IOException, MetaDataIOException {

        ValueObject b = createBasket( 10, "longaberger" );
        addToBasket( b, createFruit( 1, "apple" ) );
        addToBasket( b, createFruit( 2, "orange" ) );
        addToBasket( b, createFruit( 3, "pear" ) );

        runTest(b, "inbasket2");
    }

    @Test
    public void testFruitInBasketWithBugs() throws IOException, MetaDataIOException {

        ValueObject b = createBasket( 10, "longaberger" );
        addToBasket( b, createFruit( 1, "apple" ) );
        addToBasket( b, createFruit( 2, "orange" ) );
        addToBasket( b, createFruit( 3, "pear" ) );

        ValueObject banana = createFruit( 3, "banana" );
        banana.setObject( "bug", createBug( 100, "fly"));
        addToBasket( b, banana );

        runTest(b, "inbasketbugs");
    }

    protected abstract void runTest( ValueObject o, String name ) throws IOException, MetaDataIOException;

    protected void writeXML( String filename, ValueObject vo ) throws IOException, MetaDataIOException {

        XMLValueObjectWriter writer = new XMLValueObjectWriter( loader, getTestFileOutputStream( filename ) );
        //writer.withIndent(true);
        writer.write( vo );
        writer.close();
    }

    protected ValueObject readXML( String filename ) throws IOException, MetaDataIOException {

        XMLValueObjectReader reader = new XMLValueObjectReader( loader, getTestFileInputStream( filename ) );
        ValueObject vo = reader.read();
        reader.close();
        return vo;
    }

    protected void writeJson( String filename, ValueObject vo ) throws IOException, MetaDataIOException {

        JsonValueObjectWriter writer = new JsonValueObjectWriter( loader, getTestFileWriter( filename ) );
        writer.withIndent("  ");
        writer.write( vo );
        writer.close();
    }

    protected ValueObject readJson( String filename ) throws IOException, MetaDataIOException {

        JsonValueObjectReader reader = new JsonValueObjectReader( loader, getTestFileReader( filename ) );
        ValueObject vo = reader.read();
        reader.close();
        return vo;
    }
}
