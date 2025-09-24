/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software
 * LLC. Use is subject to license terms.
 */
package com.draagon.meta.validator;

import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.attr.BooleanAttribute;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.loader.MetaDataLoader.TYPE_METADATA;

/**
 * MetaValidator that performs validations on a MetaField with unified registry registration.
 *
 * @version 6.2
 */
@MetaDataType(type = "validator", subType = "base", description = "Base validator metadata with common validator attributes")
public abstract class MetaValidator extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaValidator.class);

    public final static String TYPE_VALIDATOR = "validator";
    public final static String SUBTYPE_BASE = "base";
    public final static String ATTR_MSG = "msg";

    // Unified registry self-registration
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

            MetaDataRegistry.registerType(MetaValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_BASE)
                .description("Base validator metadata with common validator attributes")

                // INHERIT FROM BASE METADATA
                .inheritsFrom(TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

                // VALIDATOR PARENT ACCEPTANCE DECLARATIONS
                // Validators can be placed under fields for field-level validation
                .acceptsParents(MetaField.TYPE_FIELD, "*")  // Any field type can have validators

                // BIDIRECTIONAL CONSTRAINT: Validators accept metadata.base as parent
                .acceptsParents(MetaDataLoader.TYPE_METADATA, MetaDataLoader.SUBTYPE_BASE)

                // Validators can be placed under loaders as abstract/template validators
                .acceptsParents(MetaData.ATTR_METADATA, "*")  // Abstract validators in loaders

                // VALIDATOR CHILD ACCEPTANCE DECLARATIONS
                // All validators can have attributes
                .acceptsChildren(TYPE_ATTR, "*")  // Any attribute type

                // Validators can contain nested validators for complex validation logic
                // NOTE:  NO THEY CANNOT!
                //.acceptsChildren(TYPE_VALIDATOR, "*")  // Any validator type
            );

            log.debug("Registered base MetaValidator type with unified registry");

            // Register MetaValidator-specific validation constraints only
            setupMetaValidatorValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register MetaValidator type with unified registry", e);
        }
    }

    /**
     * Setup MetaValidator-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupMetaValidatorValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Validator message attribute
            ValidationConstraint validatorMessageValidation = new ValidationConstraint(
                "metavalidator.message.validation",
                "MetaValidator message attribute must be valid string",
                (metadata) -> metadata instanceof MetaValidator,
                (metadata, value) -> {
                    if (metadata instanceof MetaValidator) {
                        MetaValidator validator = (MetaValidator) metadata;
                        // Check if message attribute exists and is valid
                        try {
                            String msg = validator.getMessage("default");
                            return msg != null && !msg.trim().isEmpty();
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(validatorMessageValidation);

            log.debug("Registered MetaValidator-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register MetaValidator constraints", e);
        }
    }

    public MetaValidator(String subtype, String name) {
        super(TYPE_VALIDATOR, subtype, name);
    }

    // Note: getMetaDataClass() is now inherited from MetaData base class

    /** Add Child to the MetaValidator */
    //public MetaValidator addChild(MetaData data) throws InvalidMetaDataException {
    //    return super.addChild( data );
    //}

    /** Wrap the MetaValidator */
    //public MetaValidator overload() {
    //    return super.overload();
    //}

    /**
     * Sets an attribute of the MetaClass
     */
    //public MetaValidator addMetaAttr(MetaAttribute attr) {
    //    return addChild(attr);
    //}

    /**
     * Gets the declaring meta field.<br>
     * NOTE: This may not be the MetaField from which the view
     * was retrieved, so be careful!
     */
    public MetaField getDeclaringMetaField() {
        if ( getParent() instanceof MetaDataLoader) return null;
        if ( getParent() instanceof MetaField ) return (MetaField) getParent();
        throw new InvalidMetaDataException(this, "MetaValidators can only be attached to MetaFields " +
                "or MetaDataLoaders as abstracts");
    }

    /**
     * Retrieves the MetaField for this view associated
     * with the specified object.
     */
    public MetaField getMetaField(Object obj) {
        MetaObject mo = MetaDataUtil.findMetaObject(obj, this);
        MetaField mf = getDeclaringMetaField();
        if ( mo != null ) {
            return mo.getMetaField(mf.getName());
        }
        else if ( mf != null ) {
            return mf;
        }
        return null;
    }

    /**
     * Sets the Super Validator
     */
    public void setSuperValidator(MetaValidator superValidator) {
        setSuperData(superValidator);
    }

    /**
     * Gets the Super Validator
     */
    protected MetaValidator getSuperValidator() {
        return getSuperData();
    }

    /////////////////////////////////////////////////////////////
    // VALIDATION METHODS

    /**
     * Validates the value of the field in the specified object
     */
    public abstract void validate(Object object, Object value);

    /////////////////////////////////////////////////////////////
    // HELPER METHODS

    /**
     * Retrieves the message to use for displaying errors
     */
    public String getMessage(String defMsg) {
        String msg = defMsg;
        try {
            msg = getMetaAttr(ATTR_MSG).getValueAsString();
        } catch (MetaDataNotFoundException ignoreException) {
        }
        return msg;
    }
}
