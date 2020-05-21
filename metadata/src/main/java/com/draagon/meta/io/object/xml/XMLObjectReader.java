package com.draagon.meta.io.object.xml;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.ValueException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import static com.draagon.meta.io.xml.XMLIOConstants.*;
import static com.draagon.meta.io.xml.XMLIOUtil.*;
import com.draagon.meta.io.xml.XMLMetaDataReader;
import com.draagon.meta.io.xml.XMLSerializationHandler;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.Validatable;
import com.draagon.meta.util.DataConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XMLObjectReader extends XMLMetaDataReader {

    public XMLObjectReader(MetaDataLoader loader, InputStream is ) {
        super(loader, is);
    }

    public static <T> T readObject( Class<T> clazz, MetaObject mo, InputStream is ) throws IOException {
        XMLObjectReader writer = new XMLObjectReader(mo.getLoader(), is);
        Object o = writer.read( mo);
        writer.close();
        return (T) o;
    }

    public Object read() throws IOException {
        return read(null);
    }

    public Object read (MetaObject mo ) throws IOException {

        Object o = null;

        path().inc( mo);
        Document doc = loadXML();
        path().dec();

        Element e = doc.getDocumentElement();

        return validate( readObject( e, mo ));
    }

    protected Object validate( Object o ) throws IOException {
        if ( o != null && o instanceof Validatable ) {
            try {
                ((Validatable) o).validate();
            } catch( ValueException e ) {
                throw new MetaDataIOException( this, "Final Object read was invalid: "+e, e );
            }
        }
        return o;
    }

    protected Object readObject(Element e, MetaObject mo) throws IOException {

        String name = e.getNodeName();
        path().inc( name );

        if ( e.hasAttribute(XML_ATTYPE)) {
            String metaDataName = e.getAttribute(XML_ATTYPE);
            if (metaDataName != null) {
                mo = getLoader().getMetaObjectByName(metaDataName);
            }
        }
        if ( mo == null ) {
            throw new MetaDataIOException( this, "MetaObject was null, and no attribute "+XML_ATTYPE+" was found" );
        }

        Object o = mo.newInstance();

        readMetaFields( e, mo, o );

        path().dec();

        return o;
    }

    protected void readMetaFields( Element e, MetaObject mo, Object vo) throws IOException {

        if ( isXmlTyped(mo)) {
            String fieldName = getXmlTypedField( mo );
            String name = e.getNodeName();
            // TODO: Reverse Lookup off the XML Name
            mo.getMetaField( fieldName ).setString( vo, name );
        }

        for ( MetaField mf : mo.getMetaFields() ) {
            path().inc( mf);

            if ( !ifXmlIgnore( mf )) {
                if (isXmlAttr(mf)) {
                    readFieldAsAttribute(e, mo, mf, vo);
                } else {
                    readFieldAsElement(e, mo, mf, vo);
                }
            }

            path().dec();
        }
    }

    protected void readFieldAsAttribute(Element e, MetaObject mo, MetaField mf, Object vo) throws IOException {
        String name = getXmlName(mf);
        path().inc( name );
        if ( e.hasAttribute( name)) {
            String val = e.getAttribute(name);
            switch (mf.getDataType()) {
                case BOOLEAN:
                    mf.setBoolean(vo, DataConverter.toBoolean(val)); break;
                case BYTE:
                    mf.setByte(vo, DataConverter.toByte(val)); break;
                case SHORT:
                    mf.setShort(vo, DataConverter.toShort(val)); break;
                case INT:
                    mf.setInt(vo, DataConverter.toInt(val)); break;
                case DATE:
                    mf.setDate(vo, DataConverter.toDate(val)); break;
                case LONG:
                    mf.setLong(vo, DataConverter.toLong(val)); break;
                case FLOAT:
                    mf.setFloat(vo, DataConverter.toFloat(val)); break;
                case DOUBLE:
                    mf.setDouble(vo, DataConverter.toDouble(val)); break;
                case STRING_ARRAY:
                    mf.setStringArray(vo, DataConverter.toStringArray(val)); break;
                case STRING:
                    mf.setString(vo, val); break;
                case OBJECT:
                case OBJECT_ARRAY:
                    throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] as attribute is not supported [" + mf + "]");
                case CUSTOM:
                    if ( mf instanceof XMLSerializationHandler ) {
                        ((XMLSerializationHandler)mf).setXmlAttr( vo, val);
                    } else {
                        throw new MetaDataIOException(this, "Custom DataType and does not implement XMLSerializationHandler [" + mf + "]");
                    }
                    break;
                default:
                    throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] as attribute is not supported [" + mf + "]");
            }
        }
        path().dec();
    }

    protected void readFieldAsElement(Element e, MetaObject mo, MetaField mf, Object vo) throws IOException {
        String xmlName = getXmlName(mf);
        path().inc( xmlName );

        switch (mf.getDataType()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case DATE:      // TODO: Special Handling?
            case LONG:
            case FLOAT:
            case DOUBLE:
            case STRING_ARRAY:  // TODO: Special Handling
            case STRING:
                readFieldAsPrimitive( e, xmlName, mf, vo ); break;
            case OBJECT:
                readFieldObject( e, xmlName, mf, vo); break;
            case OBJECT_ARRAY:
                readFieldObjectArray( e, xmlName, mf, vo); break;
            case CUSTOM:
                readFieldCustom( e, xmlName, mf, vo); break;

            default:
                throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
        }

        path().dec();
    }

    protected void readFieldAsPrimitive(Element e, String xmlName, MetaField mf, Object vo) throws IOException {

        Element el = getFirstElementOfName( e, xmlName );
        if (el == null) return;

        path().inc( el.getNodeName() );
        String val = el.getTextContent();

        switch (mf.getDataType()) {
            case BOOLEAN:
                mf.setBoolean(vo, DataConverter.toBoolean(val)); break;
            case BYTE:
                mf.setByte(vo, DataConverter.toByte(val)); break;
            case SHORT:
                mf.setShort(vo, DataConverter.toShort(val)); break;
            case INT:
                mf.setInt(vo, DataConverter.toInt(val)); break;
            case DATE:
                mf.setDate(vo, DataConverter.toDate(val)); break;
            case LONG:
                mf.setLong(vo, DataConverter.toLong(val)); break;
            case FLOAT:
                mf.setFloat(vo, DataConverter.toFloat(val)); break;
            case DOUBLE:
                mf.setDouble(vo, DataConverter.toDouble(val)); break;
            case STRING_ARRAY:
                mf.setStringArray(vo, DataConverter.toStringArray(val)); break;
            case STRING:
                mf.setString(vo, val); break;
            case OBJECT:
            case OBJECT_ARRAY:
            case CUSTOM:
            default:
                throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] as primitive is not supported [" + mf + "]");
        }

        path().dec();
    }

    protected void readFieldObject(Element e, String xmlName, MetaField mf, Object o) throws IOException {
        MetaObject refmo = getObjectRef( this, mf );

        if ( xmlWrap( mf )) {
            e = getFirstElementOfName(  e, xmlName );
        }

        String name = null;
        if ( !isXmlTyped(refmo)) name = getXmlName(refmo);
        Element el = getFirstElementOfName(e, name);
        if ( el != null ) {
            Object value = readObject(el, refmo);
            mf.setObject(o, value);
        }
    }

    protected void readFieldObjectArray(Element e, String xmlName, MetaField mf, Object o) throws IOException {

        path().inc( xmlName);

        MetaObject refmo = getObjectRef( this, mf );

        if ( xmlWrap( mf )) {
            e = getFirstElementOfName(  e, xmlName );
        }

        if ( e != null ) {
            path().inc(e.getNodeName());

            // Use Null to get all elements since it's typed object
            String name = null;
            if ( !isXmlTyped(refmo)) name = getXmlName(refmo);

            for (Element el : getElementsOfName(e, name)) {

                path().inc(refmo);

                Object value = refmo.newInstance();
                if ( value != null ) {
                    mf.addToObjectArray( o, value );
                }

                readMetaFields(el, refmo, value);
                path().dec();
            }
            path().dec();
        }
        path().dec();
    }

    protected void readFieldCustom(Element e, String xmlName, MetaField mf, Object o) throws IOException {
        if ( mf instanceof XMLSerializationHandler) {
            ((XMLSerializationHandler)mf).readXmlValue(o,xmlName,e);
        }
        throw new MetaDataIOException(this, "Custom DataType and does not implement XMLSerializationHandler [" + mf + "]");
    }
}
