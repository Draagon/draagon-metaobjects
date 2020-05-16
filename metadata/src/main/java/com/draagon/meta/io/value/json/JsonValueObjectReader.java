package com.draagon.meta.io.value.json;

import com.draagon.meta.MetaData;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.field.MetaFieldNotFoundException;
import com.draagon.meta.io.MetaDataIOException;
import com.draagon.meta.io.json.JsonMetaDataReader;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueObject;
import com.draagon.meta.relation.ref.ObjectReference;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonValueObjectReader extends JsonMetaDataReader {

    public final static String TYPE_FIELD = "@type";

    private List<String> pathList = new ArrayList<>();

    public JsonValueObjectReader(MetaDataLoader loader, Reader reader ) {
        super(loader, reader);
    }

    public ValueObject read() throws MetaDataIOException {
        return read( null );
    }

    public ValueObject read( MetaObject mo ) throws MetaDataIOException {

        try {
            if ( !in().hasNext() ) return null;
            return readObject( mo );
        }
        catch (MetaDataIOException e) {
            throw new MetaDataIOException( this, "["+getPathAndClear()+"]: "+e.getMessage(), e );
        }
        catch (IOException e) {
            throw new MetaDataIOException( this, "["+getPathAndClear()+"] Error reading MetaObject ["+mo+"]: "+e, e );
        }
        catch (RuntimeException e) {
            throw new MetaDataIOException( this, "["+getPathAndClear()+"]: "+e, e );
        }
    }

    protected void inc( MetaData md ) {
        inc( "["+md.getTypeName()+":"+md.getName()+"]" );
    }

    protected void inc( String path ) {
        pathList.add( "/"+path );
    }

    protected String dec() {
        return pathList.remove( pathList.size()-1 );
    }

    protected String getPathAndClear() {
        StringBuilder b = new StringBuilder();
        for ( String p : pathList ) b.append( p );
        pathList.clear();
        return b.toString();
    }

    protected ValueObject readObject( MetaObject mo ) throws MetaDataIOException {

        try {
            inc("{}");
            in().beginObject();

            ValueObject vo = readObjectFields( mo );

            in().endObject();
            dec();  // {}

            return vo;
        }
        catch (IOException e) {
            throw new MetaDataIOException( this, "Error reading MetaObject ["+mo+"]: "+e, e );
        }
    }

    protected ValueObject readObjectFields(MetaObject mo) throws MetaDataIOException {

        ValueObject vo = null;
        String lastName = null;

        try {
            while( in().hasNext()) {
                lastName = in().nextName();
                inc( lastName );

                if ( TYPE_FIELD.equals(lastName)) {
                    mo = processMetaObjectType( mo );
                }
                else if ( mo == null ) {
                    throw new MetaDataIOException( this, "First field in an object must have the "+
                            " @type specified when MetaObject is null, found ["+lastName+"]");
                }
                else {
                    if ( vo == null ) {
                        vo = (ValueObject) mo.newInstance();
                    }

                    MetaField mf = mo.getMetaField(lastName);
                    inc(mf);
                    readFieldValue(mo, mf, vo);
                    dec(); // mf
                }
                dec(); // lastName
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

    protected MetaObject processMetaObjectType( MetaObject mo ) throws MetaDataIOException, IOException {

        //String typeField = in().nextName();
        //if ( !TYPE_FIELD.equals( typeField )) {
        //    throw new MetaDataIOException( this, "First field in an object must have the @type specified when "+
        //            "the MetaObject is unknown, found ["+typeField+"]");
        //}

        String objectName = in().nextString();
        inc( objectName);

        MetaObject mo2 = getLoader().getMetaObjectByName( objectName );

        // TODO: Do a check to handle if mo2 is derivated from mo
        if ( mo != null && !mo2.isSameTypeSubTypeName( mo )) {
            throw new MetaDataIOException( this, "Specified MetaObject ["+mo2+"] is not "+
                    "compatible with ["+mo+"]");
        }
        else if ( mo == null ) {
            mo = mo2;
        }

        dec();

        return mo;
    }

    protected void readFieldValue(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {
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

    protected void readFieldObjectArray(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException, IOException {

        inc("[]");
        in().beginArray();

        ObjectReference oref = mf.getFirstObjectReference();
        if ( oref == null ) throw new MetaDataIOException( this, "No ObjectReference existed ["+mf+"]" );
        MetaObject refmo = oref.getReferencedObject();
        if ( refmo == null ) throw new MetaDataIOException( this, "No MetaObject reference not found ["+refmo+"]" );

        List<Object> objects = new ArrayList<>();
        mf.setObject( vo, objects );

        while ( in().hasNext() ) {
            ValueObject voc = readObject( refmo );
            if ( voc != null ) objects.add( voc );
        }

        in().endArray();
        dec();
    }

    protected void readFieldObject(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {

        ObjectReference oref = mf.getFirstObjectReference();
        if ( oref == null ) throw new MetaDataIOException( this, "No ObjectReference existed ["+mf+"]" );
        MetaObject refmo = oref.getReferencedObject();
        if ( refmo == null ) throw new MetaDataIOException( this, "No MetaObject reference not found ["+refmo+"]" );

        ValueObject voc = readObject( refmo );
        mf.setObject( vo, voc );
    }

    protected void readFieldCustom(MetaObject mo, MetaField mf, ValueObject vo) throws MetaDataIOException {
        throw new MetaDataIOException( this, "Custom DataTypes not yet supported ["+mf+"]");
    }

    @Override
    public void close() throws MetaDataIOException {
        super.close();
        if ( !pathList.isEmpty() ) {
            throw new IllegalStateException( "On writer ["+toString()+"], path was not empty ["+getPathAndClear()+"], logic error" );
        }
    }
}
