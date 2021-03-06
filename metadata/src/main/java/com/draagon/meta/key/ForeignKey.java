package com.draagon.meta.key;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.util.MetaDataUtil;

import java.util.List;

public class ForeignKey extends MetaKey {

    public final static String SUBTYPE = "foreign";

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
                    throw new MetaObjectNotFoundException("Foreign MetaObject [" + name + "] referenced by key "+
                            "["+getName()+"] on MetaObject ["+getDeclaringObject().getName()+"] does not exist", name);
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

    @Override
    public void validate() {
        super.validate();
        if ( getParent() instanceof MetaDataLoader) {
            // TODO: When adding native abstract support, ensure it's true if attached to MetaDataLoader
        }
        else {
            getForeignObject();

            getForeignKey();

            if (getForeignKeyFields().size() == 0)
                throw new InvalidMetaDataException(this, "Attribute '" + ATTR_FOREIGNKEY + "' " +
                        "had no valid key fields listed");

            if (getNumKeys() != getNumForeignKeys()) {
                throw new InvalidMetaDataException(this, "Number of keys ("+getNumKeys()+") is not the same size as "+
                        "number of foreign keys ("+getNumForeignKeys()+")" );
            }

            // TODO:  Compare data types on keys vs. foreign keys
        }
    }
}
