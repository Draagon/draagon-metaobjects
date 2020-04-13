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

public class MetaDatatLoaderTest {

    static final Log log = LogFactory.getLog(MetaDatatLoaderTest.class);

    protected MetaDataLoader loader = null;

    @Before
    public void buildModel() {
        loader = MetaDataLoader.createManual( "test1" )
                .init()
                .register()
                .addChild( StringAttribute.create( "hello", "world" ))
                .addChild( ValueMetaObject.create( "foo")
                        .addMetaField( IntegerField.create( "bar", 5 )
                                .addChild( IntAttribute.create("length", 10))
                                .addChild( StringAttribute.create("abc", "def"))))
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

        MetaObject mo = (MetaObject) loader.getMetaObject(  "foo" );
        MetaObject mo2 = loader.getChild( "foo", MetaObject.class );

        assertEquals( "find foo", mo, mo2 );
        assertEquals( "foo.bar", "bar", mo.getMetaField("bar").getName() );
        assertEquals( "foo.bar.length=\"10\"", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString() );
        assertEquals( "foo.bar.length=10", 10, (int) mo.getMetaField("bar").getMetaAttr( "length").getValue() );

        ValueObject o = (ValueObject) mo.newInstance();
        log.info( "MetaObject: " + o );

        assertEquals( "foo.bar=5", 5, (int) o.getInt( "bar" ));
    }

    @Test
    public void testModelOverlay() {

        MetaObject mo = loader.getMetaObject( "foo" );
        MetaObject baby = ValueMetaObject.create("foo-baby")
                .setSuperObject( mo );

        // Create an overlay for bar and length
        baby.addMetaField(  mo.getMetaField( "bar" )
                .wrap()
                .addChild( IntAttribute.create( "length", 11 ))
                .addMetaAttr( IntAttribute.create( MetaField.ATTR_DEFAULT_VALUE, 6 ) ) );

        ValueObject bo = (ValueObject) baby.newInstance();
        log.info( "MetaObject: " + bo );

        assertEquals( "foo-baby.bar=6", 6, (int) bo.getInt( "bar" ));
        assertEquals( "foo-baby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "foo-baby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
    }

    @Test
    public void testModelOverlay2() {

        MetaObject mo = loader.getMetaObject( "foo" );
        MetaObject baby = ValueMetaObject.create("foo-baby")
                .setSuperObject( mo );

        // Create an overlay for bar and length
        baby.addMetaField( baby.getMetaField( "bar" )
                .wrap()
                .addChild( IntAttribute.create( "length", 11 ))
                .addMetaAttr( IntAttribute.create( MetaField.ATTR_DEFAULT_VALUE, 6 ) ) );

        ValueObject bo = (ValueObject) baby.newInstance();
        log.info( "MetaObject: " + bo );

        assertEquals( "foo-baby.bar=6", 6, (int) bo.getInt( "bar" ));
        assertEquals( "foo-baby.bar.length=11", "11", baby.getMetaField("bar").getMetaAttr( "length").getValueAsString());
        assertEquals( "foo.bar.length=10", "10", mo.getMetaField("bar").getMetaAttr( "length").getValueAsString());

        assertEquals( "foo-baby.bar.abc=def", "def", baby.getMetaField("bar").getMetaAttr( "abc").getValueAsString() );
    }

    @Test
    public void testWrapSameFieldException() {

        MetaObject mo = loader.getMetaObject( "foo" );

        Exception ex = null;
        try {
            mo.addMetaField(mo.getMetaField("bar").wrap());
        } catch( Exception e ) {ex=e;}

        assertEquals( "Exception on wrap existing field", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }

    @Test
    public void testSameFieldException() {

        MetaObject mo = loader.getMetaObject( "foo" );

        Exception ex = null;
        try {
            mo.addMetaField( StringField.create("bar", "error"));
        } catch( Exception e ) {ex=e;}

        assertEquals( "Exception on add existing field", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }

    @Test
    public void testSameTypeException() {

        MetaObject foo = loader.getMetaObject( "foo" );
        MetaField bar = foo.getMetaField( "bar" );
        MetaAttribute defVal = bar.getMetaAttr( MetaField.ATTR_DEFAULT_VALUE );

        Exception ex = null;
        try {
            foo.addChild( ValueMetaObject.create("foo2"));
        } catch( Exception e ) {ex=e;}

        assertEquals( "Exception on add MetaObject to MetaObject", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try {
            bar.addChild( StringField.create("bar2", "error"));
        } catch( Exception e ) {ex=e;}

        assertEquals( "Exception on add MetaField to MetaField", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));

        try {
            defVal.addChild( StringAttribute.create("bad", "attr"));
        } catch( Exception e ) {ex=e;}

        assertEquals( "Exception on add MetaAttribute to MetaAttribute", true, ex.getClass().isAssignableFrom(InvalidMetaDataException.class ));
    }
}
