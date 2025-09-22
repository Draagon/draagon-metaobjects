package com.draagon.meta.key;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.ValidationResult;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@MetaDataType(type = "key", subType = "foreign", description = "Foreign key for referencing other objects")
public class ForeignKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(ForeignKey.class);

    public final static String SUBTYPE = "foreign";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(ForeignKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Foreign key for referencing other objects")
                
                // FOREIGN KEY ATTRIBUTES
                .optionalAttribute("keys", "stringArray")
                .optionalAttribute("foreignObjectRef", "string")
                .optionalAttribute("foreignKey", "string")
                .optionalAttribute("foreignKeyMap", "string")
                .optionalAttribute("description", "string")
            );
            
            log.debug("Registered ForeignKey type with unified registry");
        } catch (Exception e) {
            log.error("Failed to register ForeignKey type with unified registry", e);
        }
    }

    public final static String ATTR_FOREIGNOBJECTREF = "foreignObjectRef";
    public final static String ATTR_FOREIGNKEY = "foreignKey";
    public final static String ATTR_FORIGNKEYMAP = "foreignKeyMap";

    public ForeignKey(String name) {
        super(SUBTYPE, name);
    }

    private ForeignKey(String subType, String name) {
        super(subType, name);
    }

    public MetaObject getForeignObject() {

        final String KEY = "getForeignObject()";

        MetaObject o = (MetaObject) getCacheValue(KEY);

        if (o == null) {

            if (!hasMetaAttr(ATTR_FOREIGNOBJECTREF))
                throw new InvalidMetaDataException( this, "Attribute with name '"+ ATTR_FOREIGNOBJECTREF +"' "+
                        "defining the foreign object did not exist" );

            String objectRef = getMetaAttr(ATTR_FOREIGNOBJECTREF).getValueAsString();
            if (objectRef != null) {

                String name = MetaDataUtil.expandPackageForMetaDataRef(getDeclaringObject().getPackage(), objectRef);

                try {
                    o = getLoader().getMetaObjectByName(name);
                }
                catch (MetaDataNotFoundException e) {
                    throw MetaDataNotFoundException.forObject(name, getDeclaringObject());
                }
            }

            setCacheValue(KEY, o);
        }

        return o;
    }

    /** Returns the PrimaryKey or a named SecondaryKey */
    public MetaKey getForeignKey() {

        final String CACHE_KEY = "getForeignKey()";
        MetaKey key = (MetaKey) getCacheValue( CACHE_KEY );
        if ( key == null ) {

            MetaObject mo = getForeignObject();

            if (hasMetaAttr(ATTR_FOREIGNKEY)) {
                String keyName = getMetaAttr(ATTR_FOREIGNKEY).getValueAsString();

                if (keyName.equals(PrimaryKey.NAME)) {
                    key = mo.getPrimaryKey();
                    if (key == null) {
                        throw new MetaDataNotFoundException("No PrimaryKey existed on foreign MetaObject: "
                                + mo, PrimaryKey.NAME);
                    }
                } else {
                    key = mo.getSecondaryKeyByName(keyName);
                    if (key == null) {
                        throw new MetaDataNotFoundException("No SecondaryKey with name [" + keyName + "] existed on foreign " +
                                "MetaObject: " + mo, keyName);
                    }
                }
            } else {
                key = mo.getPrimaryKey();
                if (key == null) {
                    throw new MetaDataNotFoundException("No PrimaryKey existed on foreign MetaObject: "
                            + mo, PrimaryKey.NAME);
                }
            }

            setCacheValue( CACHE_KEY, key );
        }
        return key;
    }

    public List<MetaField> getForeignKeyFields() {
        return getForeignKey().getKeyFields();
    }

    public int getNumForeignKeys() {
        return getForeignKeyFields().size();
    }

    public ObjectKey getForeignKey(Object foreignObject) {
        return getObjectKeyForKeyFields( getForeignObject(), KeyTypes.FOREIGN, getForeignKeyFields(), foreignObject );
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.LOCAL_FOREIGN, getKeyFields(), o );
    }

    
}
