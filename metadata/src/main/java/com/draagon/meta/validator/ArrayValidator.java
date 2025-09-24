package com.draagon.meta.validator;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.InvalidValueException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.draagon.meta.validator.MetaValidator.SUBTYPE_BASE;
import static com.draagon.meta.validator.MetaValidator.TYPE_VALIDATOR;

/**
 * Array Validator that ensures collection/array sizes meet constraints with unified registry registration.
 *
 * @version 6.2
 */
@MetaDataType(type = "validator", subType = "array", description = "Array validator for size constraints")
public class ArrayValidator extends MetaValidator {

    private static final Logger log = LoggerFactory.getLogger(ArrayValidator.class);

    public final static String SUBTYPE_ARRAY = "array";

    public final static String ATTR_MINSIZE = "minSize";
    public final static String ATTR_MAXSIZE = "maxSize";

    // Unified registry self-registration
    static {
        try {
            // Explicitly trigger MetaValidator static initialization first
            try {
                Class.forName(MetaValidator.class.getName());
                // Add a small delay to ensure MetaValidator registration completes
                Thread.sleep(1);
            } catch (ClassNotFoundException | InterruptedException e) {
                log.warn("Could not force MetaValidator class loading", e);
            }

            MetaDataRegistry.registerType(ArrayValidator.class, def -> def
                .type(TYPE_VALIDATOR).subType(SUBTYPE_ARRAY)
                .description("Array validator for size constraints")

                // INHERIT FROM BASE VALIDATOR
                .inheritsFrom(TYPE_VALIDATOR, SUBTYPE_BASE)

                // ArrayValidator inherits all parent acceptance from validator.base:
                // - Can be placed under any field type (especially array/collection fields)
                // - Can be placed under any object type
                // - Can be placed under other validators
                // - Can be placed under loaders as abstract

                // ARRAY-SPECIFIC CHILD ACCEPTANCE DECLARATIONS
                // ArrayValidator can have minSize and maxSize attributes for size constraints
                .acceptsNamedChildren("attr", "int", ATTR_MINSIZE)    // Min size attribute
                .acceptsNamedChildren("attr", "int", ATTR_MAXSIZE)    // Max size attribute

                // NO ADDITIONAL CHILD REQUIREMENTS (only uses inherited validator capabilities + size constraints)
            );

            log.debug("Registered ArrayValidator type with unified registry");

            // Register ArrayValidator-specific validation constraints only
            setupArrayValidatorValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register ArrayValidator type with unified registry", e);
        }
    }

    /**
     * Setup ArrayValidator-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupArrayValidatorValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Array validator size bounds validation
            ValidationConstraint arraySizeBoundsValidation = new ValidationConstraint(
                "arrayvalidator.size.bounds.validation",
                "ArrayValidator minSize and maxSize attributes must be valid with minSize <= maxSize",
                (metadata) -> metadata instanceof ArrayValidator,
                (metadata, value) -> {
                    if (metadata instanceof ArrayValidator) {
                        ArrayValidator validator = (ArrayValidator) metadata;
                        try {
                            int minSize = validator.getMinSize();
                            Integer maxSize = validator.getMaxSize();

                            // Min size cannot be negative
                            if (minSize < 0) return false;

                            // If maxSize is specified, it cannot be negative and must be >= minSize
                            if (maxSize != null) {
                                if (maxSize < 0) return false;
                                if (maxSize < minSize) return false;
                            }

                            return true;

                        } catch (Exception e) {
                            return false; // Any error in validation
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(arraySizeBoundsValidation);

            log.debug("Registered ArrayValidator-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register ArrayValidator constraints", e);
        }
    }

    // Cache for frequently accessed size values
    private Integer cachedMinSize;
    private Integer cachedMaxSize;
    private boolean minSizeCached = false;
    private boolean maxSizeCached = false;

    public ArrayValidator(String name) {
        super(SUBTYPE_ARRAY, name);
    }

    protected ArrayValidator(String subType, String name) {
        super(subType, name);
    }

    public int getMinSize() {
        if (!minSizeCached) {
            if (hasMetaAttr(ATTR_MINSIZE)) {
                MetaAttribute<?> attr = getMetaAttr(ATTR_MINSIZE);
                try {
                    cachedMinSize = getAttrValueAsInt(attr);
                }
                catch(NumberFormatException e ) {
                    throw new MetaDataException( "Invalid min value of ["+attr.getValueAsString()+"] for attribute: " + attr.getName());
                }
            } else {
                cachedMinSize = 0;
            }
            minSizeCached = true;
        }
        return cachedMinSize;
    }

    protected int getAttrValueAsInt(MetaAttribute<?> attr) {
        // Optimized: direct access for INT type, fallback to string parsing for other types
        if (attr.getDataType() == DataTypes.INT) {
            return (Integer) attr.getValue();
        }

        return Integer.parseInt(attr.getValueAsString());
    }

    public boolean hasMaxSize() {
        return hasMetaAttr(ATTR_MAXSIZE);
    }

    public Integer getMaxSize() {
        if (!maxSizeCached) {
            if (hasMetaAttr(ATTR_MAXSIZE)) {
                MetaAttribute<?> attr = getMetaAttr(ATTR_MAXSIZE);
                try {
                    cachedMaxSize = getAttrValueAsInt(attr);
                }
                catch(NumberFormatException e ) {
                    throw new MetaDataException( "Invalid max value of ["+attr.getValueAsString()+"] for attribute: " + attr.getName());
                }
            } else {
                cachedMaxSize = null;
            }
            maxSizeCached = true;
        }
        return cachedMaxSize;
    }

    /**
     * Validates the value of the field in the specified object
     */
    public void validate(Object object, Object value) {

        if ( value != null ) {

            if (value instanceof Collection) {
                int size = ((Collection) value).size();
                if ( size < getMinSize() ) {
                    throw new InvalidValueException( "Minimum array size is ("+ getMinSize()+"), array was ("+size+")");
                }
                if ( hasMaxSize() && size > getMaxSize() ) {
                    throw new InvalidValueException( "Maximum array size is ("+ getMaxSize()+"), array was ("+size+")");
                }
            }
            else {
                // For non-array values, only validate if minimum size constraint would be violated
                // This allows single values to pass when minSize <= 1
                if ( getMinSize() > 1 ) {
                    throw new InvalidValueException( "The value was not an array and the size must be at least "+ getMinSize());
                }
            }
        }
    }
}
