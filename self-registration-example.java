// CORRECTED SELF-REGISTRATION EXAMPLE
// Using actual existing attribute classes and proper conventions

package com.draagon.meta.field;

import com.draagon.meta.DataTypes;
import com.draagon.meta.attr.StringAttribute;
import com.draagon.meta.attr.IntAttribute;
import com.draagon.meta.registry.MetaDataTypeHandler;
import com.draagon.meta.registry.MetaDataTypeRegistry;
import com.draagon.meta.constraint.Constraint;
import com.draagon.meta.constraint.ChildRequiredConstraint;
import com.draagon.meta.constraint.PlacementConstraint;

/**
 * Example showing corrected self-registration pattern for StringField
 * with proper constraint setup and existing attribute usage
 */
@MetaDataTypeHandler(type = "field", subType = "string", description = "String field with length constraints")
public class StringField extends PrimitiveField<String> {

    public final static String SUBTYPE_STRING = "string";
    
    // CORRECTED: Proper naming convention for attribute constants
    public final static String MAX_LENGTH_ATTR_NAME = "maxLength";
    public final static String PATTERN_ATTR_NAME = "pattern";
    public final static String MIN_LENGTH_ATTR_NAME = "minLength";

    public StringField(String name) {
        super(SUBTYPE_STRING, name, DataTypes.STRING);
    }

    // Self-registration with constraint setup
    static {
        try {
            MetaDataTypeRegistry registry = ServiceRegistryFactory.getDefault()
                .getService(MetaDataTypeRegistry.class);
            
            // Register this type handler
            registry.registerHandler(
                new MetaDataTypeId(TYPE_FIELD, SUBTYPE_STRING), 
                StringField.class
            );
            
            // Set up constraints for this type based on what we know
            setupStringFieldConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register StringField type handler", e);
        }
    }

    /**
     * CORRECTED: Setup constraints using existing attribute classes
     * Demonstrates proper separation of placement vs validation constraints
     */
    private static void setupStringFieldConstraints() {
        
        // CONSTRAINT 1: StringField CAN have maxLength attribute (placement constraint)
        // Uses existing IntAttribute class instead of made-up LengthConstraintAttribute
        PlacementConstraint maxLengthPlacement = new PlacementConstraint(
            "stringfield.maxlength.placement",
            "StringField can optionally have maxLength attribute",
            (metadata) -> metadata.getType().equals(TYPE_FIELD) && 
                         metadata.getSubTypeName().equals(SUBTYPE_STRING),
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MAX_LENGTH_ATTR_NAME)
        );
        ConstraintRegistry.addConstraint(maxLengthPlacement);
        
        // CONSTRAINT 2: StringField CAN have pattern attribute (placement constraint)  
        // Uses existing StringAttribute class instead of made-up PatternAttribute
        PlacementConstraint patternPlacement = new PlacementConstraint(
            "stringfield.pattern.placement", 
            "StringField can optionally have pattern attribute",
            (metadata) -> metadata.getType().equals(TYPE_FIELD) && 
                         metadata.getSubTypeName().equals(SUBTYPE_STRING),
            (child) -> child instanceof StringAttribute && 
                      child.getName().equals(PATTERN_ATTR_NAME)
        );
        ConstraintRegistry.addConstraint(patternPlacement);
        
        // CONSTRAINT 3: StringField CAN have minLength attribute (placement constraint)
        PlacementConstraint minLengthPlacement = new PlacementConstraint(
            "stringfield.minlength.placement",
            "StringField can optionally have minLength attribute", 
            (metadata) -> metadata.getType().equals(TYPE_FIELD) && 
                         metadata.getSubTypeName().equals(SUBTYPE_STRING),
            (child) -> child instanceof IntAttribute && 
                      child.getName().equals(MIN_LENGTH_ATTR_NAME)
        );
        ConstraintRegistry.addConstraint(minLengthPlacement);
    }

    /**
     * CORRECTED: Example of constraint-based validation that uses existing attributes
     */
    @Override
    protected void validateValue(Object value) {
        if (value == null) return;
        
        String stringValue = value.toString();
        
        // Use actual StringAttribute and IntAttribute classes for constraints
        try {
            IntAttribute maxLengthAttr = (IntAttribute) getMetaAttr(MAX_LENGTH_ATTR_NAME);
            if (maxLengthAttr != null && maxLengthAttr.getValue() != null) {
                int maxLength = maxLengthAttr.getValue();
                if (stringValue.length() > maxLength) {
                    throw new MetaDataValidationException(this, 
                        "String length " + stringValue.length() + " exceeds maximum " + maxLength);
                }
            }
        } catch (MetaDataNotFoundException ignored) {
            // maxLength attribute is optional
        }
        
        try {
            StringAttribute patternAttr = (StringAttribute) getMetaAttr(PATTERN_ATTR_NAME);
            if (patternAttr != null && patternAttr.getValue() != null) {
                String pattern = patternAttr.getValue();
                if (!stringValue.matches(pattern)) {
                    throw new MetaDataValidationException(this,
                        "String value '" + stringValue + "' does not match pattern: " + pattern);
                }
            }
        } catch (MetaDataNotFoundException ignored) {
            // pattern attribute is optional
        }
    }
}

// =============================================================================
// CORRECTED EXAMPLE: MetaAttribute self-registration
// =============================================================================

@MetaDataTypeHandler(type = "attr", subType = "string", description = "String attribute type")
public class StringAttribute extends MetaAttribute<String> {

    public final static String SUBTYPE_STRING = "string";

    public StringAttribute(String name) {
        super(SUBTYPE_STRING, name, DataTypes.STRING);
    }

    // Self-registration for attributes
    static {
        try {
            MetaDataTypeRegistry registry = ServiceRegistryFactory.getDefault()
                .getService(MetaDataTypeRegistry.class);
            
            // CORRECTED: Use proper TYPE constant from MetaAttribute parent class
            registry.registerHandler(
                new MetaDataTypeId(TYPE_ATTR, SUBTYPE_STRING), 
                StringAttribute.class
            );
            
            // Setup constraints for string attributes
            setupStringAttributeConstraints();
            
        } catch (Exception e) {
            log.error("Failed to register StringAttribute type handler", e);
        }
    }

    private static void setupStringAttributeConstraints() {
        // CORRECTED: Placement constraint - StringAttribute can be placed under any MetaData
        // This shows proper separation of concerns - not trying to "allow" or "disallow"
        PlacementConstraint attributePlacement = new PlacementConstraint(
            "stringattr.placement",
            "StringAttribute can be placed under any MetaData type",
            (parent) -> parent instanceof MetaData, // Any MetaData can have string attributes
            (child) -> child instanceof StringAttribute
        );
        ConstraintRegistry.addConstraint(attributePlacement);
    }

    public static StringAttribute create(String name, String value) {
        StringAttribute a = new StringAttribute(name);
        a.setValue(value);
        return a;
    }
}

// =============================================================================
// CORRECTED EXAMPLE: IntAttribute self-registration  
// =============================================================================

@MetaDataTypeHandler(type = "attr", subType = "int", description = "Integer attribute type")
public class IntAttribute extends MetaAttribute<Integer> {

    public final static String SUBTYPE_INT = "int";

    public IntAttribute(String name) {
        super(SUBTYPE_INT, name, DataTypes.INT);
    }

    // Self-registration for int attributes
    static {
        try {
            MetaDataTypeRegistry registry = ServiceRegistryFactory.getDefault()
                .getService(MetaDataTypeRegistry.class);
            
            // CORRECTED: Use proper TYPE constant from MetaAttribute parent class  
            registry.registerHandler(
                new MetaDataTypeId(TYPE_ATTR, SUBTYPE_INT),
                IntAttribute.class
            );
            
        } catch (Exception e) {
            log.error("Failed to register IntAttribute type handler", e);
        }
    }

    public static IntAttribute create(String name, Integer value) {
        IntAttribute a = new IntAttribute(name);
        a.setValue(value);
        return a;
    }
}

// =============================================================================
// KEY CORRECTIONS MADE:
// =============================================================================

/*
1. ✅ USE EXISTING ATTRIBUTE CLASSES:
   - StringAttribute instead of PatternAttribute
   - IntAttribute instead of LengthConstraintAttribute
   
2. ✅ PROPER NAMING CONVENTIONS:
   - MAX_LENGTH_ATTR_NAME instead of LENGTH_CONSTRAINT_ATTR_NAME
   - PATTERN_ATTR_NAME instead of PATTERN_CONSTRAINT_ATTR_NAME
   
3. ✅ USE PROPER TYPE CONSTANTS:
   - TYPE_ATTR from MetaAttribute parent class
   - TYPE_FIELD from MetaField parent class
   - Not hardcoded strings or arrays
   
4. ✅ CONSTRAINT SEPARATION:
   - Placement constraints: WHERE something can be placed
   - Validation constraints: HOW values are validated
   - Not trying to "allow" or "disallow" - just describing what we know
   
5. ✅ EXTENSIBILITY MAINTAINED:
   - Plugins can add new field types that also use StringAttribute/IntAttribute
   - No rigid restrictions that prevent future subtypes
   - Constraint-based approach allows downstream customization
   
6. ✅ PROPER ARCHITECTURE:
   - Self-registration happens in static initializers
   - Uses existing ServiceRegistry/MetaDataTypeRegistry infrastructure
   - Follows established patterns from @MetaDataTypeHandler examples
*/