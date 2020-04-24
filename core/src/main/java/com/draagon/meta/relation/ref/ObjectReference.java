package com.draagon.meta.relation.ref;

import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.MetaObjectNotFoundException;
import com.draagon.meta.relation.ObjectRelation;
import com.draagon.meta.util.MetaDataUtil;

public abstract class ObjectReference extends ObjectRelation {

    public final static String TYPE_OBJECTREF = "objectRef";

    public final static String ATTR_FOREIGNKEY = "foreignKey";
    public final static String ATTR_REFERENCE = "reference";

    /**
     * Constructs the MetaData
     */
    public ObjectReference(String subType, String name) {
        super(TYPE_OBJECTREF, subType, name);
    }

    public MetaObject getSuperObject() {
        return (MetaObject) getSuperData();
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
                            MetaDataUtil.findPackageForMetaData( getSuperObject() ), a );

                    try {
                        o = getLoader().getMetaObjectByName(name);
                    } catch (MetaDataNotFoundException e) {
                        throw new MetaObjectNotFoundException(
                                "MetaObject[" + name + "] referenced by MetaObject [" + getSuperObject() + "] does not exist", name);
                    }

                    setCacheValue(KEY, o);
                }
            }

            return o;
        }
    }
}
