package com.draagon.meta.io.object.json;

import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonMetaDataReader;
import static com.draagon.meta.io.xml.XMLIOUtil.*;

import com.draagon.meta.io.json.JsonSerializationHandler;
import com.draagon.meta.io.object.gson.MetaObjectGsonInitializer;
import com.draagon.meta.io.string.StringSerializationHandler;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class RawJsonObjectReader extends JsonMetaDataReader {

    public final static String TYPE_FIELD = "@type";

    public RawJsonObjectReader(MetaDataLoader loader, Reader reader ) {
        super(loader, reader);
    }

    public static <T> T readObject( Class<T> clazz, MetaObject mo, Reader reader ) throws IOException {
        RawJsonObjectReader writer = new RawJsonObjectReader(mo.getLoader(), reader);
        Object o = writer.read( mo);
        writer.close();
        return (T) o;
    }

    public Object read() throws IOException {
        return read( null );
    }

    public Object read(MetaObject mo ) throws IOException {

        MetaObjectGsonInitializer.addSerializersToBuilder( getLoader(), builder());

        try {
            if ( !in().hasNext() ) return null;
        } catch (IOException e) {
            throw new MetaDataIOException( this, "Error reading MetaObject ["+mo+"]: "+e, e );
        }

        return readObject( mo );
    }

    /*protected Object validate( Object o ) throws IOException {
        if ( o != null && o instanceof Validatable ) {
            try {
                ((Validatable) o).validate();
            } catch( ValueException e ) {
                throw new MetaDataIOException( this, "Final Object read was invalid: "+e, e );
            }
        }
        return o;
    }*/

    protected Object readObject(MetaObject mo ) throws IOException {

        try {
            path().inc("{}");
            in().beginObject();

            Object vo = readObjectFields( mo );

            in().endObject();
            path().dec();  // {}

            return vo;
        }
        catch (IOException e) {
            throw new MetaDataIOException( this, "Error reading MetaObject ["+mo+"]: "+e, e );
        }
    }

    protected Object readObjectFields(MetaObject mo) throws IOException {

        Object vo = null;
        String lastName = null;

        try {
            while( in().hasNext()) {
                lastName = in().nextName();
                path().inc( lastName );

                if ( TYPE_FIELD.equals(lastName)) {
                    mo = processMetaObjectType( mo );
                }
                else if ( mo == null ) {
                    throw new MetaDataIOException( this, "First field in an object must have the "+
                            " @type specified when MetaObject is null, found ["+lastName+"]");
                }
                else {
                    if ( vo == null ) {
                        vo = mo.newInstance();
                    }

                    MetaField mf = mo.getMetaField(lastName);
                    path().inc(mf);
                    readFieldValue(mo, mf, vo);
                    path().dec(); // mf
                }
                path().dec(); // lastName
            }

            return vo;
        }
        catch (MetaFieldNotFoundException e) {
            throw new MetaDataIOException( this, "Error reading field name ["+lastName+"]: "+e, e );
        }
        catch (IOException e) {
            throw new MetaDataIOException( this, "Error reading fields for MetaObject ["+mo+"]"+
                    ", last field name ["+lastName+"]: "+e, e );
        }
    }

    protected MetaObject processMetaObjectType( MetaObject mo ) throws IOException, IOException {

        //String typeField = in().nextName();
        //if ( !TYPE_FIELD.equals( typeField )) {
        //    throw new MetaDataIOException( this, "First field in an object must have the @type specified when "+
        //            "the MetaObject is unknown, found ["+typeField+"]");
        //}

        String objectName = in().nextString();
        path().inc( objectName);

        MetaObject mo2 = getLoader().getMetaObjectByName( objectName );

        // TODO: Do a check to handle if mo2 is derivated from mo
        if ( mo != null && !mo2.isSameTypeSubTypeName( mo )) {
            throw new MetaDataIOException( this, "Specified MetaObject ["+mo2+"] is not "+
                    "compatible with ["+mo+"]");
        }
        else if ( mo == null ) {
            mo = mo2;
        }

        path().dec();

        return mo;
    }

    protected void readFieldValue(MetaObject mo, MetaField mf, Object vo) throws IOException {
        try {
            switch( mf.getDataType() ) {
                case BOOLEAN:       mf.setBoolean( vo, in().nextBoolean() ); break;
                case BYTE:
                case SHORT:
                case INT:           mf.setInt( vo, in().nextInt() ); break;
                case DATE:
                case LONG:          mf.setLong( vo, in().nextLong() ); break;
                case FLOAT:
                case DOUBLE:        mf.setDouble( vo, in().nextDouble() ); break;
                case STRING_ARRAY:
                case STRING:        mf.setString( vo, in().nextString() ); break;

                case OBJECT:        readFieldObject( mo, mf, vo ); break;
                case OBJECT_ARRAY:  readFieldObjectArray( mo, mf, vo ); break;

                case CUSTOM:        readFieldCustom( mo, mf, vo ); break;

                default:
                    throw new MetaDataIOException( this, "DataType ["+mf.getDataType()+"] not supported ["+mf+"]");
            }
        }
        catch (IOException e) {
            throw new MetaDataIOException( this, "Error reading MetaField ["+mf+"]: "+e, e );
        }
    }

    protected void readFieldObjectArray(MetaObject mo, MetaField mf, Object vo) throws IOException, IOException {

        path().inc("[]");
        in().beginArray();

        MetaObject refmo = getObjectRef( this, mf );

        List<Object> objects = new ArrayList<>();
        mf.setObject( vo, objects );

        while ( in().hasNext() ) {
            Object voc = readObject( refmo );
            if ( voc != null ) objects.add( voc );
        }

        in().endArray();
        path().dec();
    }

    protected void readFieldObject(MetaObject mo, MetaField mf, Object vo) throws IOException {

        MetaObject refmo = getObjectRef( this, mf );

        Object voc = readObject( refmo );
        mf.setObject( vo, voc );
    }

    protected void readFieldCustom(MetaObject mo, MetaField mf, Object vo) throws IOException {
        if ( mf instanceof JsonSerializationHandler) {
            ((JsonSerializationHandler)mf).readJsonValue(vo,in());
        } else if (mf instanceof StringSerializationHandler){
            ((StringSerializationHandler)mf).setValueAsString(vo,in().nextString());
        } else {
            throw new MetaDataIOException(this, "Custom DataType and does not implement JsonSerializationHandler [" + mf + "]");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if ( !path().isAtRoot() ) {
            throw new IllegalStateException( "On writer ["+toString()+"], path was not empty ["+path().getPathAndClear()+"], logic error" );
        }
    }
}
