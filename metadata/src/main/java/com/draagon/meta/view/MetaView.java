package com.draagon.meta.view;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.field.MetaField.TYPE_FIELD;
import static com.draagon.meta.loader.MetaDataLoader.TYPE_METADATA;
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
            // Explicitly trigger MetaDataLoader static initialization first
            try {
                Class.forName(MetaDataLoader.class.getName());
                // Add a small delay to ensure MetaDataLoader registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaDataLoader class loading", e);
            }

            MetaDataRegistry.registerType(MetaView.class, def -> def
                .type(TYPE_VIEW).subType(SUBTYPE_BASE)
                .description("Base view metadata with common view attributes")

                // INHERIT FROM BASE METADATA
                .inheritsFrom(TYPE_METADATA, "base")

                // VIEW PARENT ACCEPTANCE DECLARATIONS
                // Views can be placed under fields and loaders
                .acceptsParents(TYPE_FIELD, "*")                     // Any field type
                .acceptsParents(TYPE_METADATA, "*")                  // Any metadata (loaders)

                // VIEW CHILD ACCEPTANCE DECLARATIONS
                // Views can contain attributes
                .acceptsChildren(TYPE_ATTR, "*")                    // Any attribute type

                // Universal fallback for any view
                .acceptsParents("*", "*")  // MetaView can be used under any parent that needs views

            );

            log.debug("Registered MetaView type with unified registry");

            // Register MetaView-specific validation constraints only
            setupMetaViewValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register MetaView type with unified registry", e);
        }
    }

    /**
     * Setup MetaView-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupMetaViewValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: View placement validation
            ValidationConstraint viewPlacementValidation = new ValidationConstraint(
                "metaview.placement.validation",
                "MetaView can only be placed under MetaFields or MetaDataLoaders",
                (metadata) -> metadata instanceof MetaView,
                (metadata, value) -> {
                    if (metadata instanceof MetaView) {
                        MetaView metaView = (MetaView) metadata;
                        MetaData parent = metaView.getParent();
                        return parent instanceof MetaField || parent instanceof MetaDataLoader;
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(viewPlacementValidation);

            log.debug("Registered MetaView-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register MetaView constraints", e);
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
        throw new MetaDataException("MetaViews can only be attached to MetaFields " +
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
