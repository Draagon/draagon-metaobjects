package com.draagon.meta.io.object.xml;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import static com.draagon.meta.io.xml.XMLIOConstants.*;
import static com.draagon.meta.io.xml.XMLIOUtil.*;
import com.draagon.meta.io.xml.XMLMetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

public class XMLObjectReader extends XMLMetaDataReader {

    public XMLObjectReader(MetaDataLoader loader, InputStream is ) {
        super(loader, is);
    }

    public Object read() throws MetaDataIOException {
        return read(null);
    }

    public Object read (MetaObject mo ) throws MetaDataIOException {

        Object o = null;

        path().inc( mo);
        Document doc = loadXML();
        path().dec();

        Element e = doc.getDocumentElement();

        o = readObject( e, mo );

        return o;
    }

    protected Object readObject(Element e, MetaObject mo) throws MetaDataIOException {

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

    protected void readMetaFields( Element e, MetaObject mo, Object vo) throws MetaDataIOException {

        for ( MetaField mf : mo.getMetaFields() ) {
            path().inc( mf);

            if ( isXmlAttr( mf )) {
                readFieldAsAttribute( e, mo, mf, vo);
            }
            else {
                readFieldAsElement( e, mo, mf, vo);
            }

            path().dec();
        }
    }

    protected void readFieldAsAttribute(Element e, MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {
        String name = getXmlName(mf);
        path().inc( name );
        if ( e.hasAttribute( name)) {
            String val = e.getAttribute(name);
            mf.setString(vo, val);
        }
        path().dec();
    }

    protected void readFieldAsElement(Element e, MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {
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
                // TODO:  Add Helper for this for special cases
                readFieldAsString( e, xmlName, mf, vo );
                break;
            case OBJECT:
                readFieldObject( e, xmlName, mf, vo);
                break;
            case OBJECT_ARRAY:
                readFieldObjectArray( e, xmlName, mf, vo);
                break;
            case CUSTOM:
                readFieldCustom( e, xmlName, mf, vo);
                break;

            default:
                throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
        }

        path().dec();
    }

    protected void readFieldAsString(Element e, String xmlName, MetaField mf, Object vo) throws MetaDataIOException {

        Element el = getFirstElementOfName( e, xmlName );
        if (el == null) return;

        path().inc( el.getNodeName() );

        String val = el.getTextContent();
        mf.setString( vo, val );

        path().dec();
    }

    protected void readFieldObject(Element e, String xmlName, MetaField mf, Object o) throws MetaDataIOException {
        MetaObject refmo = getObjectRef( this, mf );

        if ( xmlWrap( mf )) {
            e = getFirstElementOfName(  e, xmlName );
        }

        String name = getXmlName(refmo);
        Element el = getFirstElementOfName(e, name);
        if ( el != null ) {
            Object value = readObject(el, refmo);
            mf.setObject(o, value);
        }
    }

    protected void readFieldObjectArray(Element e, String xmlName, MetaField mf, Object o) throws MetaDataIOException {

        path().inc( xmlName);

        MetaObject refmo = getObjectRef( this, mf );

        if ( xmlWrap( mf )) {
            e = getFirstElementOfName(  e, xmlName );
        }

        if ( e != null ) {
            path().inc(e.getNodeName());

            String name = getXmlName(refmo);
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

    protected void readFieldCustom(Element e, String xmlName, MetaField mf, Object o) throws MetaDataIOException {
        throw new MetaDataIOException( this, "Custom DataTypes not yet supported ["+mf+"]");
    }
}
