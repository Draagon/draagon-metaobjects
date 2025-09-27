package com.metaobjects.loader;

import com.metaobjects.InvalidMetaDataException;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.field.IntegerField;
import com.metaobjects.field.MetaField;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.mapped.MappedMetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class MetaDataLoaderTest {

    static final Log log = LogFactory.getLog(MetaDataLoaderTest.class);

    protected MetaDataLoader loader = null;

    @Before
    public void buildModel() {
        MetaDataLoader tempLoader = MetaDataLoader.createManual( false, "test1" );
        tempLoader.init();
        tempLoader.register();
        tempLoader.addMetaAttr( StringAttribute.create( "hello", "world" ));
        
        MappedMetaObject foo = MappedMetaObject.create("foo");
        IntegerField bar = IntegerField.create( "bar", 5 );
        bar.addMetaAttr( IntAttribute.create("length", 10));
        bar.addMetaAttr( StringAttribute.create("abc", "def"));
        foo.addMetaField(bar);
        
        tempLoader.addChild(foo);
        loader = tempLoader.getLoader();
    }

    @After
    public void destroyModel() {
        loader.destroy();
    }

    @Test
    public void testModelMetadata() {

        assertEquals("hello attribute from loader", "hello", loader.getChildOfType(MetaAttribute.TYPE_ATTR, "hello").getName());
        assertEquals("hello attribute from registry", "hello", loader.getMetaAttr("hello").getName());

        assertEquals("1 foo object", 1, loader.getMetaDataOfType(MetaObject.TYPE_OBJECT).size());

        MetaObject mo = loader.getMetaObjectByName(  "foo" );
        MetaObject mo2 = (MetaObject) loader.getChild( "foo", MetaObject.class );

        assertEquals( "find foo", mo, mo2 );
        assertEquals( "foo.bar", "bar", mo.getMetaField("bar").getName() );
        assertEquals( "foo.bar.length=\"10\"", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString() );
        assertEquals( "foo.bar.length=10", 10, (int) mo.getMetaField("bar").getMetaAttr( "length").getValue() );

        Map o = (Map) mo.newInstance();
        //log.info( "MetaObject: " + o );

        assertEquals( "foo.bar=5", 5, (int) o.get( "bar" ));
    }

    @Test
    public void testMap() {

        MetaObject mo = loader.getMetaObjectByName(  "foo" );

        Map o = (Map) mo.newInstance();
        Map o2 = (Map) mo.newInstance();

        assertEquals( "o == o2", o, o2 );

        o.put( "bar", 6 );
        assertNotSame( "o == o2", o, o2 );

        o.put( "bar", "5" );
        assertNotSame( "o == o2", o, o2 );

        // This setter will get ignored and a warning will be logged
        o.put( "bad", "what!" );
        assertNotSame( "o == o2", o, o2 );
    }

    @Test
    public void testModelOverlay() {

        // foo
        //     .bar
        //        .length = 10
        // fooBaby (parent->foo)
        //     .bar (parent->foo.bar)
        //         .length = 11

        MetaObject mo = loader.getMetaObjectByName( "foo" );
        MetaObject baby = MappedMetaObject.create("fooBaby");
        baby.setSuperObject( mo );

        // Create an overlay for bar and length
        MetaField barField = mo.getMetaField( "bar" ).overload(); // overload or extend
        barField.addMetaAttr( IntAttribute.create( "length", 11 ));
        barField.addMetaAttr( StringAttribute.create( MetaField.ATTR_DEFAULT_VALUE, "6" ) );
        baby.addMetaField(barField);

        Map bo = (Map) baby.newInstance();
        //log.info( "MetaObject: " + bo );

        assertEquals( "fooBaby.bar=6", 6, (int) bo.get( "bar" ));
        assertEquals( "fooBaby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "fooBaby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
    }

    @Test
    public void testModelOverlay2() {

        // foo
        //     .bar
        //        .length = 10
        // fooBaby (parent->foo)
        //     .bar (parent->foo.bar)
        //         .length = 11

        MetaObject mo = loader.getMetaObjectByName( "foo" );
        MetaObject baby = MappedMetaObject.create("fooBaby");
        baby.setSuperObject( mo );

        // Create an overlay for bar and length
        MetaField barField = mo.getMetaField( "bar" ).overload();
        barField.addMetaAttr( IntAttribute.create( "length", 11 ));
        barField.addMetaAttr( StringAttribute.create( MetaField.ATTR_DEFAULT_VALUE, "6" ) );
        baby.addMetaField(barField);

        Map bo = (Map) baby.newInstance();
        //log.info( "MetaObject: " + bo );

        assertEquals( "fooBaby.bar=6", 6, bo.get( "bar" ));
        assertEquals( "fooBaby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "fooBaby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
    }

    @Test
    public void testWrapSameFieldException() {

        MetaObject mo = loader.getMetaObjectByName( "foo" );

        Exception ex = null;
        try { mo.addMetaField(mo.getMetaField("bar").overload()); }
        catch( Exception e ) {ex=e;}

        assertEquals( "Exception on wrap existing field", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }

    @Test
    public void testSameFieldException() {

        MetaObject mo = loader.getMetaObjectByName( "foo" );

        Exception ex = null;
        try { mo.addMetaField( StringField.create("bar", "error")); } catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add existing field", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }

    @Test
    public void testSameTypeException() {

        MetaObject foo = loader.getMetaObjectByName( "foo" );
        MetaField bar = foo.getMetaField( "bar" );
        MetaAttribute defVal = bar.getMetaAttr( MetaField.ATTR_DEFAULT_VALUE );

        Exception ex = null;
        try { foo.addChild( MappedMetaObject.create("foo2")); }  catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaObject to MetaObject", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try { bar.addChild( StringField.create("bar2", "error")); } catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaField to MetaField", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try { defVal.addChild( StringAttribute.create("bad", "attr")); } catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaAttribute to MetaAttribute", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }
}
