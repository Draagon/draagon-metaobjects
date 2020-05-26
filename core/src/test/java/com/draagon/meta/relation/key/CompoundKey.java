package com.draagon.meta.relation.key;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;

import java.util.List;

public class CompoundKey extends ObjectKey {

    public final static String SUBTYPE_COMPOUND = "compound";

    public CompoundKey( String name ) {
        super(SUBTYPE_COMPOUND, name );
    }

    @Override
    public String getKeyAsString(Object o) {
        StringBuilder b = new StringBuilder();
        getCompoundKeys().forEach( f -> {
            if ( b.length() > 0 ) b.append('-');
            b.append( f.getString( o ));
        });
        return b.toString();
    }

    /** Get the Compound Keys */
    public List<MetaField> getCompoundKeys() {
        return getFieldKeys();
    }

    @Override
    public void validate() {
        if ( getFieldKeys().size() <= 1 ) {
            throw new MetaDataException( "CompoundKey must have more than one MetaField with the "+ObjectKey.ATTR_ISKEY+" attribute: " + getFieldKeys());
        }
    }
}
