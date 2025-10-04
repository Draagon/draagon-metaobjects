/*
 * Copyright 2004 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.field;

import com.metaobjects.*;
import com.metaobjects.attr.IntAttribute;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.constraint.CustomConstraint;
import com.metaobjects.constraint.PlacementConstraint;
// Constraint registration now handled by consolidated MetaDataRegistry
import com.metaobjects.registry.MetaDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.metaobjects.field.MetaField.SUBTYPE_BASE;

/**
 * A String Field with unified registry registration and child requirements.
 *
 * @version 6.0
 * @author Doug Mealing
 */
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

    // Unified registry self-registration
    /**
     * Register StringField type using the standardized registerTypes() pattern.
     * This method registers the string field type that inherits from field.base.
     *
     * @param registry The MetaDataRegistry to register with
     */
    public static void registerTypes(MetaDataRegistry registry) {
        try {
            registry.registerType(StringField.class, def -> {
                def.type(TYPE_FIELD).subType(SUBTYPE_STRING)
                   .description("String field with pattern validation that supports validator children")

                   // INHERIT FROM BASE FIELD
                   .inheritsFrom(TYPE_FIELD, SUBTYPE_BASE);

                // STRING-SPECIFIC ATTRIBUTES WITH FLUENT CONSTRAINTS
                def.optionalAttributeWithConstraints(ATTR_PATTERN)
                   .ofType(StringAttribute.SUBTYPE_STRING)
                   .withCustom(value -> {
                       if (value == null) return true;
                       try {
                           // Validate that the pattern is a valid regex
                           java.util.regex.Pattern.compile(value.toString());
                           return true;
                       } catch (java.util.regex.PatternSyntaxException e) {
                           return false;
                       }
                   });

                def.optionalAttributeWithConstraints(ATTR_MAX_LENGTH)
                   .ofType(IntAttribute.SUBTYPE_INT)
                   .asSingle();

                def.optionalAttributeWithConstraints(ATTR_MIN_LENGTH)
                   .ofType(IntAttribute.SUBTYPE_INT)
                   .asSingle();
            });

            if (log != null) {
                log.debug("Registered StringField type with fluent constraint builder (auto-generated constraints)");
            }

        } catch (Exception e) {
            if (log != null) {
                log.error("Failed to register StringField type with unified registry", e);
            }
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
