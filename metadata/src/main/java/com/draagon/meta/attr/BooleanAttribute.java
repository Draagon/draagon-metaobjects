/*
 * Copyright 2002 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.attr;

import com.draagon.meta.DataTypes;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Boolean Attribute with unified registry registration.
 *
 * @version 6.0
 */
@MetaDataType(type = "attr", subType = "boolean", description = "Boolean attribute for true/false metadata values")
@SuppressWarnings("serial")
public class BooleanAttribute extends MetaAttribute<Boolean>
{
    private static final Logger log = LoggerFactory.getLogger(BooleanAttribute.class);

    public final static String SUBTYPE_BOOLEAN = "boolean";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaAttribute static initialization first
            try {
                Class.forName(MetaAttribute.class.getName());
                // Add a small delay to ensure MetaAttribute registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaAttribute class loading", e);
            }

            MetaDataRegistry.registerType(BooleanAttribute.class, def -> def
                .type(TYPE_ATTR).subType(SUBTYPE_BOOLEAN)
                .description("Boolean attribute for true/false metadata values")

                // INHERIT FROM BASE ATTRIBUTE
                .inheritsFrom(TYPE_ATTR, SUBTYPE_BASE)

                // === DATABASE BOOLEAN ATTRIBUTE PARENT ACCEPTANCES ===

                // FIELD-LEVEL DATABASE BOOLEAN ATTRIBUTES (for MetaFields)
                .acceptsNamedParents("field", "*", "dbNullable")        // Database nullable specification
                .acceptsNamedParents("field", "*", "dbPrimaryKey")      // Database primary key marker
                .acceptsNamedParents("field", "*", "isIndex")           // Database index marker
                .acceptsNamedParents("field", "*", "isUnique")          // Database unique constraint marker
                .acceptsNamedParents("field", "*", "isViewOnly")        // Read-only field marker

                // OBJECT-LEVEL DATABASE BOOLEAN ATTRIBUTES (for MetaObjects)
                .acceptsNamedParents("object", "*", "allowDirtyWrite")  // Allow dirty write marker
            );

            log.debug("Registered BooleanAttribute type with unified registry");

            // Register BooleanAttribute-specific validation constraints only
            setupBooleanAttributeValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register BooleanAttribute type with unified registry", e);
        }
    }
    
    /**
     * Setup BooleanAttribute-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupBooleanAttributeValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
            
            // VALIDATION CONSTRAINT: Boolean attribute values
            ValidationConstraint booleanAttributeValidation = new ValidationConstraint(
                "booleanattribute.value.validation",
                "BooleanAttribute values must be valid boolean strings",
                (metadata) -> metadata instanceof BooleanAttribute,
                (metadata, value) -> {
                    if (metadata instanceof BooleanAttribute) {
                        BooleanAttribute boolAttr = (BooleanAttribute) metadata;
                        String valueStr = boolAttr.getValueAsString();
                        return valueStr == null || 
                               "true".equalsIgnoreCase(valueStr) || 
                               "false".equalsIgnoreCase(valueStr);
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(booleanAttributeValidation);
            
            log.debug("Registered BooleanAttribute-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register BooleanAttribute constraints", e);
        }
    }

    /**
     * Constructs the Boolean MetaAttribute
     */
    public BooleanAttribute(String name ) {
        super( SUBTYPE_BOOLEAN, name, DataTypes.BOOLEAN);
    }

    /**
     * Manually create a Boolean MetaAttribute with a value
     */
    public static BooleanAttribute create(String name, Boolean value ) {
        BooleanAttribute a = new BooleanAttribute( name );
        a.setValue( value );
        return a;
    }
}
