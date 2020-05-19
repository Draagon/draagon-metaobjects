package com.draagon.meta.io.object.json;

import com.draagon.meta.MetaDataAware;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonMetaDataWriter;
import com.draagon.meta.io.object.xml.XMLObjectWriter;
import com.draagon.meta.io.util.IOUtil;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.DataConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class JsonObjectWriter extends JsonMetaDataWriter {

    public JsonObjectWriter(MetaDataLoader loader, Writer writer ) throws MetaDataIOException {
        super(loader, writer);
    }

    public static void writeObject(MetaDataAware o, Writer out) throws MetaDataIOException {
        JsonObjectWriter writer = new JsonObjectWriter(o.getMetaData().getLoader(), out);
        writer.withIndent( "  " );
        writer.write(o);
        writer.close();
    }

    public void write(Object vo) throws MetaDataIOException {
        if ( vo == null ) throw new MetaDataIOException( this, "Cannot write a null Object");
        try {
            writeObject(IOUtil.getMetaObjectFor(getLoader(), vo ), vo);
        } catch (RuntimeException e) {
            throw new MetaDataIOException( this, e.toString(), e );
        }
    }

    protected void writeObject(MetaObject mo, Object vo) throws MetaDataIOException {
        try {
            path().inc("{}");
            out().beginObject();
            path().inc( mo );
            out().name("@type").value(mo.getName());
            for( MetaField mf : mo.getMetaFields()) {
                writeField( mo, mf, vo );
            }
            path().dec();
            out().endObject();
            path().dec();
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing json ["+mo.getName()+"]: "+e, e );
        }
    }

    protected void writeField(MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {
        try {
            path().inc(mf);
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
            path().dec();
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing json ["+mo.getName()+"]: "+e, e );
        }
    }

    protected void writeFieldObjectArray(MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?
        try {
            out().beginArray();
            for (Object o : DataConverter.toObjectArray(mf.getObject(vo))) {
                writeObject(IOUtil.getMetaObjectFor(getLoader(), o), o);
            }
            out().endArray();
        }
        catch( IOException e ) {
            throw new MetaDataIOException( this, "Error writing objectArray ["+mf+"]: "+e, e );
        }
    }

    protected void writeFieldObject(MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {

        // TODO:  Should we worry about the objectRef?
        Object o = mf.getObject( vo );
        writeObject(IOUtil.getMetaObjectFor(getLoader(), o ), o);
    }

    protected void writeFieldCustom(MetaObject mo, MetaField mf, Object vo) throws MetaDataIOException {
        throw new MetaDataIOException( this, "Custom DataTypes not yet supported ["+mf+"]");
    }
}
