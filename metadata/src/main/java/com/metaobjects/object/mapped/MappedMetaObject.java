package com.metaobjects.object.mapped;

import com.metaobjects.MetaDataException;
import com.metaobjects.field.MetaField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.MetaObjectAware;
import com.metaobjects.util.DataConverter;
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.metaobjects.object.MetaObject.SUBTYPE_BASE;
import javax.sound.midi.MetaEventListener;
import java.util.Map;

/**
 * MappedMetaObject with unified registry registration for Map-based objects.
 *
 * @version 6.0
 */
public class MappedMetaObject extends MetaObject
{
    private static final Logger log = LoggerFactory.getLogger(MappedMetaObject.class);

    public final static String OBJECT_SUBTYPE = "map";
    private String metaObjectKey = "metaObject";

    /**
     * Register MappedMetaObject type with registry.
     * Called by ObjectTypesMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MappedMetaObject.class, def -> def
            .type(TYPE_OBJECT).subType(OBJECT_SUBTYPE)
            .description("Map-based MetaObject with key-value field access")

            // INHERIT FROM BASE OBJECT
            .inheritsFrom(TYPE_OBJECT, SUBTYPE_BASE)

            // NO MAP-SPECIFIC ATTRIBUTES (only uses inherited base attributes)

            // TEST-SPECIFIC ATTRIBUTES (for codegen tests)
            .optionalAttribute("implements", "string")

            // CHILD REQUIREMENTS INHERITED FROM BASE OBJECT:
            // - All field types (field.*)
            // - Other objects (object.*)
            // - Keys (key.*)
            // - Attributes (attr.*)
            // - Validators (validator.*)
            // - Views (view.*)
        );

        log.debug("Registered MappedMetaObject type with unified registry");
    }

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

    /*@Override
    public Object newInstance()  {

        Object o = null;

        // See if we have this cached already
        Boolean isMap = (Boolean) getCacheValue( KEY );
        if ( isMap == null ) {
            try {
                if (getObjectClass() != null) o = newInstance();
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
    }*/

    /**
     * Retrieves the object class of an object, or MappedObject if one is not specified
     */
    public Class<?> getObjectClass() throws ClassNotFoundException {

        Class<?> c = null;

        if ( hasObjectAttr())
            c = getObjectClassFromAttr();

        if (c == null)
            return MappedObject.class;

        return c;
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
