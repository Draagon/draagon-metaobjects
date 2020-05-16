package com.draagon.meta.io.value.xml;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import static com.draagon.meta.io.xml.XMLIOUtil.*;
import com.draagon.meta.io.xml.XMLMetaDataWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.util.DataConverter;
import org.w3c.dom.Element;

import java.io.OutputStream;

public class XMLValueObjectWriter extends XMLMetaDataWriter {

    public XMLValueObjectWriter(MetaDataLoader loader, OutputStream out ) {
        super(loader, out);
    }

    public void write( ValueObject vo ) throws MetaDataIOException {

        if ( vo == null ) throw new MetaDataIOException( this, "Cannot write a null ValueObject");

        initDoc();

        writeObject( doc().getDocumentElement(), vo.getMetaData(), vo );
    }

    protected void writeObject( Element el, MetaObject mo, ValueObject vo) throws MetaDataIOException {

        Element objEl = doc().createElement(getXmlName( mo ));
        if ( el == null ) doc().appendChild( objEl );
        else el.appendChild( objEl );

        writeObjectFields( objEl, mo, vo);
    }

    protected void writeObjectFields( Element objEl, MetaObject mo, ValueObject vo ) throws MetaDataIOException {

        for( MetaField mf : mo.getMetaFields()) {
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

    protected void writeFieldAsAttr( Element el, MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        // TODO:  Need support here for unsupported DataTypes

        el.setAttribute( getXmlName( mf ), mf.getString( vo ));
    }

    protected void writeField( Element el, MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?
        if ( xmlWrap( mf )) {
            Element wrap = doc().createElement( getXmlName( mf ));
            el.appendChild( wrap );
            el = wrap;
        }

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

    protected void writeFieldObjectArray( Element el, MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        for (Object o : DataConverter.toObjectArray(mf.getObject(vo))) {

            if ( o != null ) {
                if (!( o instanceof ValueObject )) {
                    throw new MetaDataIOException(this, "Object DataType did not return ValueObject [" + mf + "]");
                }

                ValueObject voc = (ValueObject) o;

                // TODO: Attribute to use Field name vs. Object name?
                writeObject(el, voc.getMetaData(), voc);
            }
        }
    }

    protected void writeFieldObject( Element el, MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?

        Object o = mf.getObject( vo );
        if ( o != null ) {

            if (!(o instanceof ValueObject)) {
                throw new MetaDataIOException(this, "Object DataType did not return ValueObject [" + mf + "]");
            }

            ValueObject voc = (ValueObject) o;

            // TODO: Attribute to use Field name vs. Object name?
            writeObject(el, voc.getMetaData(), voc);

            //Element objEl = doc().createElement( getXmlName( mo ));
            //el.appendChild( objEl );
            //writeObjectFields( objEl, mo, voc);
        }
    }

    protected void writeFieldCustom( Element el, MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {
        throw new MetaDataIOException( this, "Custom DataTypes not yet supported ["+mf+"]");
    }
}
