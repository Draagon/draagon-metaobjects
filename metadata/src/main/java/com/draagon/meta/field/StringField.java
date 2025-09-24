/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.field;

import com.draagon.meta.*;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import com.draagon.meta.registry.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.draagon.meta.field.MetaField.SUBTYPE_BASE;

/**
 * A String Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
@MetaDataType(type = "field", subType = "string", description = "String field type with length and pattern validation")
public class StringField extends PrimitiveField<String> {

    private static final Logger log = LoggerFactory.getLogger(StringField.class);

    // === SUBTYPE CONSTANT ===
    /** String field subtype constant */
    public static final String SUBTYPE_STRING = "string";

    // === STRING-SPECIFIC ATTRIBUTE NAME CONSTANTS ===
    /** Pattern validation attribute for string fields */
    public static final String ATTR_PATTERN = "pattern";

    /** Maximum length attribute for string fields */
    public static final String ATTR_MAX_LENGTH = "maxLength";

    /** Minimum length attribute for string fields */
    public static final String ATTR_MIN_LENGTH = "minLength";

    public StringField( String name ) {
        super( SUBTYPE_STRING, name, DataTypes.STRING );
    }

    /**
     * Register field.string type and string-specific constraints using Phase 2 standardized pattern.
     *
     * <p>This method replaces the previous static block approach with a controlled, testable
     * registration process that ensures proper dependency order during initialization.</p>
     *
     * @param registry MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            // Register StringField type with inheritance from field.base
            registry.registerType(StringField.class, def -> def
                .type(TYPE_FIELD).subType(SUBTYPE_STRING)
                .description("String field with length and pattern validation")

                // INHERIT FROM BASE FIELD (which inherits from metadata.base)
                .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE)

                // STRING-SPECIFIC ATTRIBUTES (using new API) - CORE ONLY
                .acceptsNamedAttributes(StringAttribute.SUBTYPE_STRING, ATTR_PATTERN)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MAX_LENGTH)
                .acceptsNamedAttributes(IntAttribute.SUBTYPE_INT, ATTR_MIN_LENGTH)

                // NOTE: Database attributes are declared by DatabaseConstraintProvider
                // This maintains separation of concerns and extensibility
            );

            log.debug("Registered StringField type using Phase 2 pattern");

            // Register StringField-specific validation constraints only
            setupStringFieldValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register StringField type using Phase 2 pattern", e);
            throw new RuntimeException("StringField type registration failed", e);
        }
    }
    
    /**
     * Setup StringField-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupStringFieldValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALUE VALIDATION CONSTRAINT: Pattern validation for string fields
            ValidationConstraint patternValidation = new ValidationConstraint(
                "stringfield.pattern.validation",
                "StringField pattern attribute must be valid regex",
                (metadata) -> metadata instanceof StringField && metadata.hasMetaAttr(ATTR_PATTERN),
                (metadata, value) -> {
                    if (value == null) return true; // Optional
                    try {
                        Pattern.compile(value.toString());
                        return true;
                    } catch (PatternSyntaxException e) {
                        return false;
                    }
                }
            );
            constraintRegistry.addConstraint(patternValidation);

            // Additional validation constraints can be added here
            // (length validation, etc. - only VALUE validation, not structural)
            
            log.debug("Registered StringField-specific constraints");
            
        } catch (Exception e) {
            log.error("Failed to register StringField constraints", e);
        }
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
