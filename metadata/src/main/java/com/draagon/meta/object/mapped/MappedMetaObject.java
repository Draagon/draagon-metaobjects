package com.draagon.meta.object.mapped;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectAware;
import com.draagon.meta.util.DataConverter;

import javax.sound.midi.MetaEventListener;
import java.util.Map;

public class MappedMetaObject extends MetaObject {

    public final static String OBJECT_SUBTYPE = "map";
    private String metaObjectKey = "metaObject";

    public MappedMetaObject(String name) {
        super(OBJECT_SUBTYPE,name);
    }

    protected MappedMetaObject(String subType, String name) {
        super(subType,name);
    }

    public static MappedMetaObject create( String name ) {
        return new MappedMetaObject( name );
    }

    public String getMetaObjectKey() {
        return metaObjectKey;
    }

    public void setMetaObjectKey(String metaObjectKey) {
        this.metaObjectKey = metaObjectKey;
    }

    @Override
    public Object newInstance()  {

        final String KEY = "newInstance-isMap";
        Object o = null;

        // See if we have this cached already
        Boolean isMap = (Boolean) getCacheValue( KEY );
        if ( isMap == null ) {
            try {
                if (getObjectClass() != null) o = super.newInstance();
            } catch(MetaDataException | ClassNotFoundException ignore) {}

            setCacheValue( KEY, isMap );
        }
        else if ( isMap == false ) {
            o = super.newInstance();
        }

        if ( o == null ) {
            o = new MappedObject( this );
            setDefaultValues(o);
        }
        return o;
    }

    @Override
    public void attachMetaObject(Object o) {
        if ( o instanceof MetaObjectAware ) {
            ((MetaObjectAware) o ).setMetaData(this);
        } else if ( o instanceof Map ) {
            Map m = (Map) o;
            m.put( metaObjectKey, this );
        } else {
            super.attachMetaObject(o);
        }
    }

    @Override
    public boolean produces(Object obj) {

        if ( obj instanceof MetaObjectAware ) {
            MetaObject mo = ((MetaObjectAware) obj).getMetaData();
            if ( mo != null )
                return hasChild( mo.getName(), MetaObject.class );
        }
        if (obj instanceof Map) {
            Map m = (Map) obj;
            if ( m.containsKey(getMetaObjectKey())) {
                MetaObject mo = (MetaObject) m.get(getMetaObjectKey());
                return hasChild( mo.getName(), MetaObject.class );
            }
        }
        return false;
    }

    @Override
    public Object getValue(MetaField f, Object obj) {
        if (obj instanceof Map) {
            Map m = (Map) obj;
            return m.get(f.getName());
        } else {
            throw new MetaDataException("Object is not a Map so cannot get value for: " + f);
        }
    }

    @Override
    public void setValue(MetaField f, Object obj, Object val) {
        if (obj instanceof Map) {
            Map m = (Map) obj;
            m.put(f.getName(), val);
        } else {
            throw new MetaDataException("Object is not a Map so cannot set value for: " + f);
        }
    }
}
