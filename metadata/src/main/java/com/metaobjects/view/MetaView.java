package com.metaobjects.view;

import com.metaobjects.InvalidMetaDataException;
import com.metaobjects.MetaData;
import com.metaobjects.MetaDataException;
import com.metaobjects.MetaDataNotFoundException;
import com.metaobjects.field.MetaField;
import com.metaobjects.loader.MetaDataLoader;
import com.metaobjects.util.MetaDataUtil;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.attr.BooleanAttribute;
import static com.metaobjects.MetaData.ATTR_IS_ABSTRACT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetaView extends MetaData {
    private static final Logger log = LoggerFactory.getLogger(MetaView.class);

    public final static int READ = 0;
    public final static int EDIT = 1;
    public final static int HIDE = 2;

    public final static String TYPE_VIEW = "view";
    public final static String SUBTYPE_BASE = "base";


    /**
     * Register MetaView base type with registry.
     * Called by WebMetaDataProvider during service discovery.
     */
    public static void registerTypes(MetaDataRegistry registry) {
        registry.registerType(MetaView.class, def -> {
            def.type(TYPE_VIEW).subType(SUBTYPE_BASE)
               .description("Base view metadata with common view attributes")
               .inheritsFrom("metadata", "base")
               // VIEWS CAN CONTAIN ATTRIBUTES
               .optionalChild("attr", "*", "*");

            // VIEW-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
            def.optionalAttributeWithConstraints(ATTR_IS_ABSTRACT)
               .ofType(BooleanAttribute.SUBTYPE_BOOLEAN)
               .asSingle();
        });

        // Registered base MetaView type with unified registry
    }

    public MetaView(String subtype, String name) {
        super(TYPE_VIEW, subtype, name);
    }
    
    public MetaView(String type, String subtype, String name) {
        super(type, subtype, name);
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class

    /** Add Child to the MetaView */
    //public MetaView addChild(MetaData data) throws InvalidMetaDataException {
    //  return super.addChild( data );
    //}

    /** Wrap the MetaView */
    //public MetaView overload() {
    //  return super.overload();
    //}

    /**
     * Sets an attribute of the MetaClass
     */
    //public MetaView addMetaAttr(MetaAttribute attr) {
    //  return addChild(attr);
    //}
    public MetaField getDeclaringMetaField() {
        if (getParent() instanceof MetaDataLoader) return null;
        if (getParent() instanceof MetaField) return (MetaField) getParent();
        throw new InvalidMetaDataException(this, "MetaViews can only be attached to MetaFields " +
                "or MetaDataLoaders as abstracts");
    }

    /**
     * Gets the Super Field
     */
    protected MetaView getSuperView() {
        return getSuperData();
    }

    /**
     * Retrieves the MetaField for this view associated
     * with the specified object.
     *
     * @param obj
     * @return
     */
    public MetaField<?> getMetaField(Object obj) {
        MetaObject mc = MetaDataUtil.findMetaObject(obj, this);
        return mc.getMetaField(getParent().getName());
    }

    /**
     * Retrieves the display string of the field for a simple display
     */
    public String getDisplayString(Object obj) {

        MetaObject mc = getLoader().getMetaObjectFor(obj);
        if (mc == null) {
            mc = MetaDataUtil.findMetaObject(obj, this);
        }
        MetaField mf = mc.getMetaField(getParent().getName());
        return "" + mf.getString(obj);
    }

    /**
     * Performs validation before setting the value.
     * Validation is now calculated based on actual MetaValidator children
     * of the associated MetaField, eliminating the need for explicit validation attributes.
     */
    protected void performValidation(Object obj, Object val)
            throws MetaDataException {
        // Use all validators from the associated MetaField
        MetaField<?> metaField = getMetaField(obj);
        metaField.getDefaultValidatorList().forEach(v -> v.validate(obj, val));
    }
}
