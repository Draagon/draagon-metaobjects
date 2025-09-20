/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.registry.MetaDataTypeHandler;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.registry.ServiceRegistryFactory;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.PlacementConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A String Field with self-registration and constraint setup.
 *
 * @version 2.0
 * @author Doug Mealing
 */
@SuppressWarnings("serial")
@MetaDataTypeHandler(type = "field", subType = "string", description = "String field type")
public class StringField extends PrimitiveField<String> {

    private static final Logger log = LoggerFactory.getLogger(StringField.class);

    public final static String SUBTYPE_STRING = "string";
    
    // Attribute name constants for constraints
    public final static String MAX_LENGTH_ATTR_NAME = "maxLength";
    public final static String PATTERN_ATTR_NAME = "pattern";
    public final static String MIN_LENGTH_ATTR_NAME = "minLength";

    public StringField( String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING );
    }

    // Self-registration with constraint setup
    static {
        try {
            MetaDataTypeRegistry registry = new MetaDataTypeRegistry();
            
            // Register this type handler
            registry.registerHandler(
                new MetaDataTypeId(TYPE_FIELD, SUBTYPE_STRING), 
                StringField.class
            );
            
            // Set up constraints for this type
            setupStringFieldConstraints();
            
            log.debug("Self-registered StringField type handler: field.string");
            
        } catch (Exception e) {
            log.error("Failed to register StringField type handler", e);
        }
    }

    /**
     * Setup constraints using existing attribute classes following extensible patterns
     */
    private static void setupStringFieldConstraints() {
        ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
        
        // CONSTRAINT 1: StringField CAN have maxLength attribute (placement constraint)
        // Uses existing IntAttribute class
        PlacementConstraint maxLengthPlacement = new PlacementConstraint(
            "stringfield.maxlength.placement",
            "StringField can optionally have maxLength attribute",
            (metadata) -> metadata instanceof StringField,
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MAX_LENGTH_ATTR_NAME)
        );
        constraintRegistry.addConstraint(maxLengthPlacement);
        
        // CONSTRAINT 2: StringField CAN have pattern attribute (placement constraint)  
        // Uses existing StringAttribute class
        PlacementConstraint patternPlacement = new PlacementConstraint(
            "stringfield.pattern.placement", 
            "StringField can optionally have pattern attribute",
            (metadata) -> metadata instanceof StringField,
            (child) -> child instanceof StringAttribute && 
                      child.getName().equals(PATTERN_ATTR_NAME)
        );
        constraintRegistry.addConstraint(patternPlacement);
        
        // CONSTRAINT 3: StringField CAN have minLength attribute (placement constraint)
        PlacementConstraint minLengthPlacement = new PlacementConstraint(
            "stringfield.minlength.placement",
            "StringField can optionally have minLength attribute", 
            (metadata) -> metadata instanceof StringField,
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MIN_LENGTH_ATTR_NAME)
        );
        constraintRegistry.addConstraint(minLengthPlacement);
    }

    /**
     * Manually Create a StringField
     * @param name Name of the field
     * @param defaultValue Default value for the field
     * @return New StringField
     */
    public static StringField create( String name, String defaultValue ) {
        StringField f = new StringField( name );
        if ( defaultValue != null ) {
            f.addMetaAttr(StringAttribute.create( ATTR_DEFAULT_VALUE, defaultValue ));
        }
        return f;
    }
}
