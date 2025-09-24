package com.draagon.meta.loader;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.IntegerField;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.loader.simple.SimpleLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.mapped.MappedMetaObject;
import com.draagon.meta.registry.SharedTestRegistry;
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
        // Use SharedTestRegistry to ensure proper provider discovery
        SharedTestRegistry.getInstance();
        log.debug("MetaDataLoaderTest setup with shared registry");

        // Create a manual loader and add test objects programmatically
        loader = MetaDataLoader.createManual(false, "test1");

        // Initialize the loader (required before use)
        loader.init();

        // Add a description attribute to the loader (this is supported)
        loader.addMetaAttr(StringAttribute.create("description", "hello"));  // Use "description" instead of "hello"

        // Create the expected "foo" object with proper constraints
        MappedMetaObject foo = MappedMetaObject.create("foo");

        // Create "bar" field with valid attributes
        IntegerField bar = IntegerField.create("bar", 5);
        bar.addMetaAttr(IntAttribute.create("minValue", 10));  // Use "minValue" instead of "length"
        bar.addMetaAttr(StringAttribute.create("defaultValue", "def"));
        foo.addMetaField(bar);

        // Add the object to the loader
        loader.addChild(foo);

        log.debug("MetaDataLoaderTest model built with constraint-compliant objects");
    }

    @After
    public void destroyModel() {
        // NOTE: Don't destroy loader - preserve for other tests using SharedTestRegistry
        // The READ-OPTIMIZED architecture means loaders are permanent for application lifetime
    }

    @Test
    public void testModelMetadata() {

        assertEquals("description attribute from loader", "description", loader.getChildOfType(MetaAttribute.TYPE_ATTR, "description").getName());
        assertEquals("description attribute from registry", "description", loader.getMetaAttr("description").getName());

        assertEquals("1 foo object", 1, loader.getMetaDataOfType(MetaObject.TYPE_OBJECT).size());

        MetaObject mo = loader.getMetaObjectByName(  "foo" );
        MetaObject mo2 = (MetaObject) loader.getChild( "foo", MetaObject.class );

        assertEquals( "find foo", mo, mo2 );
        assertEquals( "foo.bar", "bar", mo.getMetaField("bar").getName() );
        assertEquals( "foo.bar.minValue=\"10\"", "10", mo.getMetaField("bar").getMetaAttr( "minValue").getValueAsString() );
        assertEquals( "foo.bar.minValue=10", 10, (int) mo.getMetaField("bar").getMetaAttr( "minValue").getValue() );

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
