package com.draagon.meta.key;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
import static com.draagon.meta.object.MetaObject.ATTR_DESCRIPTION;

/**
 * Base key metadata with common key attributes.
 */
public abstract class MetaKey extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaKey.class);

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Key type constant - MetaKey owns this concept */
    public final static String TYPE_KEY = "key";

    /** Base key subtype for inheritance */
    public final static String SUBTYPE_BASE = "base";

    // === KEY-LEVEL ATTRIBUTE NAME CONSTANTS ===
    /** Keys attribute that defines which fields form the key */
    public final static String ATTR_KEYS = "keys";

    /**
     * Register this type with the MetaDataRegistry (called by provider)
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MetaKey.class, def -> def
            .type(TYPE_KEY).subType(SUBTYPE_BASE)
            .description("Base key metadata with common key attributes")
            .inheritsFrom("metadata", "base")
            .optionalAttribute(ATTR_IS_ABSTRACT, "boolean")
            .optionalAttribute(ATTR_KEYS, "stringArray")
            .optionalAttribute(ATTR_DESCRIPTION, "string")

                // ACCEPTS ANY ATTRIBUTES (all key types inherit these)
                .optionalChild("attr", "*", "*")
        );
    }

    public enum KeyTypes {UNKNOWN, PRIMARY, SECONDARY, LOCAL_FOREIGN, FOREIGN};

    protected MetaKey(String subType, String name) {
        super(TYPE_KEY, subType, name);
    }

    public int getNumKeys() {
        return getKeyFields().size();
    }

    public abstract ObjectKey getObjectKey(Object o);

    protected ObjectKey getObjectKeyForKeyFields(MetaObject mo, KeyTypes keyType, List<MetaField> keyFields, Object o) {

        Object [] keys = new Object[keyFields.size()];

        int i = 0;
        for( MetaField mf : keyFields ) {
            keys[i++] = mf.getObject(o);
        }

        return new ObjectKey( mo, keyType, keys );
    }

    public List<MetaField> getKeyFields() {
        return getSpecifiedKeyFieldsForTarget( getParent(), ATTR_KEYS);
    }

    protected List<MetaField> loadKeyFields() {
        return loadSpecifiedKeyFieldsForTarget( getParent(), ATTR_KEYS);
    }

    protected List<MetaField> getSpecifiedKeyFieldsForTarget( MetaData target, String attrName ) {
        final String CACHE_KEY = "getSpecifiedKeyFieldsForTarget("+target.getName()+","+attrName+")";
        List<MetaField> keys = (List<MetaField>) getCacheValue( CACHE_KEY );
        if ( keys == null ) {
            keys = loadSpecifiedKeyFieldsForTarget( target, attrName );
            setCacheValue( CACHE_KEY, keys);
        }
        return keys;
    }

    protected List<MetaField> loadSpecifiedKeyFieldsForTarget( MetaData target, String attrName ) {

        List<MetaField> keys;
        keys = new ArrayList<>();

        boolean isLoader = ( target instanceof MetaDataLoader );
        if ( !isLoader && !(target instanceof MetaObject ))
            throw new InvalidMetaDataException(this, "Keys can only be attached to MetaObjects " +
                "or MetaDataLoaders as abstracts for attribute='"+attrName+"'");

        if (hasMetaAttr(attrName)) {

            MetaAttribute<?> attr = getMetaAttr(attrName);
            if (attr == null) {
                if (isLoader) return keys;
                throw new InvalidMetaDataException(this, "Attribute with name '" + attrName + "' " +
                        "defining the key fields was NOT found");
            }
            if (attr.getDataType() != DataTypes.STRING_ARRAY) {
                throw new InvalidMetaDataException(this,
                        "Attribute '" + attrName + "' must be a stringArray data type: " + attr);
            }
            List<String> keyNames = (List<String>) attr.getValue();

            if (!isLoader) {
                MetaObject mo = getDeclaringObject();

                for (String fn : keyNames) {
                    MetaField f = mo.getMetaField(fn);
                    if (f == null) {
                        throw new InvalidMetaDataException(this, "Attribute [" + attrName + "] had invalid field name " +
                                "[" + fn + "] that did not exist on MetaObject [" + mo.getName() + "]: keyNames=" + keyNames);
                    }
                    keys.add(f);
                }
            }
        }
        else {
            throw new InvalidMetaDataException(this, "Attribute with name '" + attrName + "' " +
                    "defining the key fields was NOT found");
        }

        return keys;
    }

    public MetaObject getDeclaringObject() {
        if ( getParent() instanceof MetaDataLoader ) return null;
        if ( getParent() instanceof MetaObject ) return (MetaObject) getParent();
        throw new InvalidMetaDataException(this, "MetaKeys can only be attached to MetaObjects " +
                "or MetaDataLoaders as abstracts");
    }

    
}
