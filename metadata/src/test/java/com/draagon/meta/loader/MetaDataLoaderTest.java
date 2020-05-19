package com.draagon.meta.loader;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.IntegerField;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.StringField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import com.draagon.meta.object.value.ValueObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class MetaDataLoaderTest {

    static final Log log = LogFactory.getLog(MetaDataLoaderTest.class);

    protected MetaDataLoader loader = null;

    @Before
    public void buildModel() {
        loader = MetaDataLoader.createManual( false, "test1" )
                .init()
                .register()
                .addMetaAttr( StringAttribute.create( "hello", "world" ))
                .addChild( ValueMetaObject.create( "foo")
                        .addMetaField( IntegerField.create( "bar", 5 )
                                .addMetaAttr( IntAttribute.create("length", 10))
                                .addMetaAttr( StringAttribute.create("abc", "def"))))
                .getLoader();
    }

    @After
    public void destroyModel() {
        loader.destroy();
    }

    @Test
    public void testModelMetadata() {

        assertEquals("hello attribute from loader", "hello", loader.getChildOfType(MetaAttribute.TYPE_ATTR, "hello").getName());
        assertEquals("hello attribute from registry", "hello", MetaDataRegistry.getDataLoader("test1").getMetaAttr("hello").getName());

        assertEquals("1 foo object", 1, loader.getMetaDataOfType(MetaObject.TYPE_OBJECT).size());

        MetaObject mo = loader.getMetaObjectByName(  "foo" );
        MetaObject mo2 = (MetaObject) loader.getChild( "foo", MetaObject.class );

        assertEquals( "find foo", mo, mo2 );
        assertEquals( "foo.bar", "bar", mo.getMetaField("bar").getName() );
        assertEquals( "foo.bar.length=\"10\"", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString() );
        assertEquals( "foo.bar.length=10", 10, (int) mo.getMetaField("bar").getMetaAttr( "length").getValue() );

        ValueObject o = (ValueObject) mo.newInstance();
        //log.info( "MetaObject: " + o );

        assertEquals( "foo.bar=5", 5, (int) o.getInt( "bar" ));
    }

    @Test
    public void testValueObject() {

        MetaObject mo = loader.getMetaObjectByName(  "foo" );

        ValueObject o = (ValueObject) mo.newInstance();
        ValueObject o2 = (ValueObject) mo.newInstance();

        assertEquals( "o == o2", o, o2 );

        o.setInt( "bar", 6 );
        assertNotSame( "o == o2", o, o2 );

        o.setString( "bar", "5" );
        assertEquals( "o == o2", o, o2 );

        // This setter will get ignored and a warning will be logged
        o.setString( "bad", "what!" );
        assertEquals( "o == o2", o, o2 );
    }

    @Test
    public void testModelOverlay() {

        // foo
        //     .bar
        //        .length = 10
        // foo-baby (parent->foo)
        //     .bar (parent->foo.bar)
        //         .length = 11

        MetaObject mo = loader.getMetaObjectByName( "foo" );
        MetaObject baby = ValueMetaObject.create("foo-baby").setSuperObject( mo );

        // Create an overlay for bar and length
        baby.addMetaField(
                mo.getMetaField( "bar" )
                .overload() // overload or extend
                .addMetaAttr( IntAttribute.create( "length", 11 ))
                .addMetaAttr( IntAttribute.create( MetaField.ATTR_DEFAULT_VALUE, 6 ) ) );

        ValueObject bo = (ValueObject) baby.newInstance();
        //log.info( "MetaObject: " + bo );

        assertEquals( "foo-baby.bar=6", 6, (int) bo.getInt( "bar" ));
        assertEquals( "foo-baby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "foo-baby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
    }

    @Test
    public void testModelOverlay2() {

        // foo
        //     .bar
        //        .length = 10
        // foo-baby (parent->foo)
        //     .bar (parent->foo.bar)
        //         .length = 11

        MetaObject mo = loader.getMetaObjectByName( "foo" );
        MetaObject baby = ValueMetaObject.create("foo-baby")
                .setSuperObject( mo );

        // Create an overlay for bar and length
        baby.addMetaField(
                mo.getMetaField( "bar" )
                .overload()
                .addMetaAttr( IntAttribute.create( "length", 11 ))
                .addMetaAttr( IntAttribute.create( MetaField.ATTR_DEFAULT_VALUE, 6 ) ) );

        ValueObject bo = (ValueObject) baby.newInstance();
        //log.info( "MetaObject: " + bo );

        assertEquals( "foo-baby.bar=6", 6, (int) bo.getInt( "bar" ));
        assertEquals( "foo-baby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "foo-baby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
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
        try { foo.addChild( ValueMetaObject.create("foo2")); }  catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaObject to MetaObject", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try { bar.addChild( StringField.create("bar2", "error")); } catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaField to MetaField", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try { defVal.addChild( StringAttribute.create("bad", "attr")); } catch( Exception e ) {ex=e;}
        assertEquals( "Exception on add MetaAttribute to MetaAttribute", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }
}
