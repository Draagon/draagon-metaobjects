package com.draagon.meta.io.object.xml;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import static com.draagon.meta.io.xml.XMLIOUtil.*;

import com.draagon.meta.io.json.JsonSerializationHandler;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.io.util.IOUtil;
import com.draagon.meta.io.xml.XMLMetaDataWriter;
import com.draagon.meta.io.xml.XMLSerializationHandler;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStream;

public class XMLObjectWriter extends XMLMetaDataWriter {

    public XMLObjectWriter(MetaDataLoader loader, OutputStream out ) {
        super(loader, out);
    }

    public static void writeObject(MetaDataAware o, OutputStream out) throws IOException {
        XMLObjectWriter writer = new XMLObjectWriter(o.getMetaData().getLoader(), out);
        writer.write(o);
        writer.close();
    }

    public void write( Object vo ) throws IOException {

        if ( vo == null ) throw new MetaDataIOException( this, "Cannot write a null Object");

        initDoc();

        MetaObject mo = null;
        writeObject( doc().getDocumentElement(), IOUtil.getMetaObjectFor(getLoader(),vo), vo );

        // Flush the document to the OutputStream
        flush();
    }


    protected void writeObject( Element el, MetaObject mo, Object vo) throws IOException {

        String xmlName = null;
        if ( isXmlTyped( mo )) {
            xmlName = mo.getMetaField( getXmlTypedField( mo )).getString( vo );
        } else {
            xmlName = getXmlName(mo);
        }

        Element objEl = doc().createElement( xmlName );
        if ( el == null ) doc().appendChild( objEl );
        else el.appendChild( objEl );

        writeObjectFields( objEl, mo, vo);
    }

    protected void writeObjectFields( Element objEl, MetaObject mo, Object vo ) throws IOException {

        String typedField = null;
        if (isXmlTyped( mo )) {
            typedField = getXmlTypedField(mo);
        }

        for( MetaField mf : mo.getMetaFields()) {

            // Skipped the typed field
            if ( typedField != null && mf.getName().equals( typedField )) continue;

            // TODO: Add more efficient way to check on null values
            Object val = mf.getObject( vo );
            if ( val != null ) {
                if (isXmlAttr(mf)) {
                    writeFieldAsAttr(objEl, mo, mf, vo);
                } else {
                    writeField(objEl, mo, mf, vo);
                }
            }
        }
    }

    protected void writeFieldAsAttr( Element el, MetaObject mo, MetaField mf, Object vo) throws IOException {

        String value = null;

        switch (mf.getDataType()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case DATE:      // TODO: Special Handling?
            case LONG:
            case FLOAT:
            case DOUBLE:
            case STRING_ARRAY:
            case STRING:
            case OBJECT:
            case OBJECT_ARRAY:
                value = mf.getString( vo );
                break;
            case CUSTOM:
                value = getCustomFieldAsAttr(mo, mf, vo);
                break;
            default:
                throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
        }
        if ( value != null ) {
            el.setAttribute(getXmlName(mf), value);
        }
    }

    protected String getCustomFieldAsAttr( MetaObject mo, MetaField mf, Object vo ) throws MetaDataIOException {
        if ( mf instanceof XMLSerializationHandler) {
            return ((XMLSerializationHandler)mf).getXmlAttr(vo);
        } else if ( mf instanceof StringSerializationHandler ) {
            return ((StringSerializationHandler)mf).getValueAsString(vo);
        } else {
            throw new MetaDataIOException(this, "Cannot get value, as Custom  DataTypes are not supported [" + mf + "]");
        }
    }

    protected void writeField( Element el, MetaObject mo, MetaField mf, Object vo) throws IOException {

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
                el = drawFieldWrapper( el, mf );
                el.appendChild( doc().createTextNode( mf.getString( vo )));
                break;
            case OBJECT:
                writeFieldObject( el, mo, mf, vo);
                break;
            case OBJECT_ARRAY:
                writeFieldObjectArray( el, mo, mf, vo);
                break;
            case CUSTOM:
                writeFieldCustom( el, mo, mf, vo);
                break;

            default:
                throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
        }
    }

    protected void writeFieldObjectArray( Element el, MetaObject mo, MetaField mf, Object vo) throws IOException {

        boolean first = true;
        for (Object o : DataConverter.toObjectArray(mf.getObject(vo))) {
            if ( o != null ) {

                if ( first ) {
                    el = drawFieldWrapper( el, mf );
                    first = false;
                }

                // TODO: Attribute to use Field name vs. Object name?
                writeObject(el, IOUtil.getMetaObjectFor(getLoader(),o), o);
            }
        }
    }

    protected Element drawFieldWrapper( Element el, MetaField mf ) {
        if ( xmlWrap( mf )) {
            Element wrap = doc().createElement( getXmlName( mf ));
            el.appendChild( wrap );
            el = wrap;
        }
        return el;
    }

    protected void writeFieldObject( Element el, MetaObject mo, MetaField mf, Object vo) throws IOException {

        if ( hasObjectRef(this,mf )) {

            Object o = mf.getObject(vo);
            if (o != null) {

                el = drawFieldWrapper(el, mf);

                // TODO: Attribute to use Field name vs. Object name?
                writeObject(el, IOUtil.getMetaObjectFor(getLoader(), o), o);

                //Element objEl = doc().createElement( getXmlName( mo ));
                //el.appendChild( objEl );
                //writeObjectFields( objEl, mo, voc);
            }
        }
        else {
            String val = null;
            if ( mf instanceof StringSerializationHandler ) {
                val = ((StringSerializationHandler) mf ).getValueAsString( vo );
            } else {
                val = mf.getString( vo );
            }
            if ( val != null ) {
                el = drawFieldWrapper(el, mf);
                el.appendChild( doc().createTextNode( val ));
            }
        }
    }

    protected void writeFieldCustom( Element el, MetaObject mo, MetaField mf, Object vo) throws IOException {
        if ( mf instanceof XMLSerializationHandler) {
            el = drawFieldWrapper( el, mf );
            ((XMLSerializationHandler)mf).writeXmlValue(vo,getXmlName(mf),doc(),el);
        }
        else if ( mf instanceof StringSerializationHandler) {
            el = drawFieldWrapper( el, mf );
            el.appendChild( doc().createTextNode(((StringSerializationHandler)mf).getValueAsString(mo)));
        }
        else {
            throw new MetaDataIOException(this, "Custom DataType and does not implement XMLSerializationHandler [" + mf + "]");
        }
    }
}