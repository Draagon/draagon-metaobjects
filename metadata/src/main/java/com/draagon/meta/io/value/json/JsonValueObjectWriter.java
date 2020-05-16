package com.draagon.meta.io.value.json;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.ObjectArrayField;
import com.draagon.meta.io.MetaDataIO;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonMetaDataWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.util.DataConverter;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public class JsonValueObjectWriter extends JsonMetaDataWriter {

    public JsonValueObjectWriter(MetaDataLoader loader, Writer writer ) throws MetaDataIOException {
        super(loader, writer);
    }

    public void write(ValueObject vo) throws MetaDataIOException {
        if ( vo == null ) throw new MetaDataIOException( this, "Cannot write a null ValueObject");
        writeObject( vo.getMetaData(), vo );
    }

    protected void writeObject(MetaObject mo, ValueObject vo) throws MetaDataIOException {
        try {
            out().beginObject().name("@type").value(mo.getName());
            for( MetaField mf : mo.getMetaFields()) {
                writeField( mo, mf, vo );
            }
            out().endObject();
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing object ["+mo.getName()+"]: "+e, e );
        }
    }

    protected void writeField(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {
        try {
            out().name(mf.getName());
            if ( mf.getObject(vo) == null ) {
                out().nullValue();
            }
            else {
                switch (mf.getDataType()) {
                    case BOOLEAN:
                        out().value(mf.getBoolean(vo));
                        break;
                    case BYTE:
                    case SHORT:
                    case INT:
                        out().value(mf.getInt(vo));
                        break;
                    case DATE:
                    case LONG:
                        out().value(mf.getLong(vo));
                        break;
                    case FLOAT:
                    case DOUBLE:
                        out().value(mf.getDouble(vo));
                        break;
                    case STRING_ARRAY:
                    case STRING:
                        out().value(mf.getString(vo));
                        break;

                    case OBJECT:
                        writeFieldObject(mo, mf, vo);
                        break;
                    case OBJECT_ARRAY:
                        writeFieldObjectArray(mo, mf, vo);
                        break;

                    case CUSTOM:
                        writeFieldCustom(mo, mf, vo);
                        break;

                    default:
                        throw new MetaDataIOException(this, "DataType [" + mf.getDataType() + "] not supported [" + mf + "]");
                }
            }
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing object ["+mo.getName()+"]: "+e, e );
        }
    }

    protected void writeFieldObjectArray(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?
        try {
            out().beginArray();
            for (Object o : DataConverter.toObjectArray(mf.getObject(vo))) {
                if (o instanceof ValueObject) {
                    ValueObject vo2 = (ValueObject) o;
                    writeObject(vo2.getMetaData(), vo2);
                } else {
                    throw new MetaDataIOException(this, "ObjectArray DataType did not return ValueObjects [" + mf + "]");
                }
            }
            out().endArray();
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing objectArray ["+mf+"]: "+e, e );
        }
    }

    protected void writeFieldObject(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?
        Object o = mf.getObject( vo );
        if (o instanceof ValueObject) {
            ValueObject vo2 = (ValueObject) o;
            writeObject(vo2.getMetaData(), vo2);
        } else {
            throw new MetaDataIOException(this, "Object DataType, but did not return a ValueObject [" + mf + "]");
        }
    }

    protected void writeFieldCustom(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {
        throw new MetaDataIOException( this, "Custom DataTypes not yet supported ["+mf+"]");
    }
}
