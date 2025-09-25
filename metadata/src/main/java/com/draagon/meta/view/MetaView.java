package com.draagon.meta.view;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaDataType(type = "view", subType = "base", description = "Base view metadata with common view attributes")
public abstract class MetaView extends MetaData {
    private static final Logger log = LoggerFactory.getLogger(MetaView.class);

    public final static int READ = 0;
    public final static int EDIT = 1;
    public final static int HIDE = 2;

    public final static String TYPE_VIEW = "view";
    public final static String SUBTYPE_BASE = "base";


    // Base view type registration
    static {
        try {
            MetaDataRegistry.getInstance().registerType(MetaView.class, def -> def
                .type(TYPE_VIEW).subType(SUBTYPE_BASE)
                .description("Base view metadata with common view attributes")
                .inheritsFrom("metadata", "base")

                // UNIVERSAL ATTRIBUTES (all MetaData inherit these)
                .optionalAttribute(ATTR_IS_ABSTRACT, "boolean")


                // VIEWS CAN CONTAIN ATTRIBUTES
                .optionalChild("attr", "*", "*")
            );

            log.debug("Registered base MetaView type with unified registry");

        } catch (Exception e) {
            log.error("Failed to register MetaView type with unified registry", e);
        }
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
