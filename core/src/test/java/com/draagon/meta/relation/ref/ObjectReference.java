package com.draagon.meta.relation.ref;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.relation.ObjectRelation;
import com.draagon.meta.util.MetaDataUtil;

public class ObjectReference extends ObjectRelation {

    public final static String TYPE_OBJECTREF = "objectRef";

    public final static String ATTR_FOREIGNKEY = "foreignKey";
    public final static String ATTR_REFERENCE = "reference";

    public final static String SUBTYPE_DIRECT = "direct";

    /**
     * Constructs the MetaData
     */
    public ObjectReference(String name) {
        super(TYPE_OBJECTREF, SUBTYPE_DIRECT, name);
    }

    /**
     * Constructs the MetaData
     */
    protected ObjectReference(String subType, String name) {
        super(TYPE_OBJECTREF, subType, name);
    }

    public static ObjectReference create( String name, String objectRef ) {
        ObjectReference ref = new ObjectReference( name );
        ref.addChild(StringAttribute.create( ATTR_REFERENCE, objectRef));
        return ref;
    }

    public MetaField getParentField() {
        MetaData parent = getParent();
        if ( !( parent instanceof MetaField )) throw new MetaDataException(
                "getParentField() called, but parent is NOT a MetaField: objectRef="
                        + getName() +", parent=" + parent.toString() );
        return (MetaField) parent;
    }

    /** Gets the MetaObject referenced by this ObjectReference using the reference attribute */
    public MetaObject getReferencedObject() {

        final String KEY = "getReferencedObject()";

        synchronized ( this ) {

            MetaObject o = (MetaObject) getCacheValue(KEY);

            if (o == null) {

                String a = getMetaAttr(ATTR_REFERENCE).getValueAsString();
                if (a != null) {

                    String name = MetaDataUtil.expandPackageForMetaDataRef(
                            MetaDataUtil.findPackageForMetaData( this.getParent() ), a );

                    try {
                        o = getLoader().getMetaObjectByName(name);
                    } catch (MetaDataNotFoundException e) {
                        throw new MetaObjectNotFoundException(
                                "MetaObject[" + name + "] referenced by ObjectReference [" + this + "] does not exist", name);
                    }

                    setCacheValue(KEY, o);
                }
            }

            return o;
        }
    }
}
