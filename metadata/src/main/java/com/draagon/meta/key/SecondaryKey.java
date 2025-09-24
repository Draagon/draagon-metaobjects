package com.draagon.meta.key;

import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.draagon.meta.key.MetaKey.SUBTYPE_BASE;

@MetaDataType(type = "key", subType = "secondary", description = "Secondary key for alternative record identification")
public class SecondaryKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(SecondaryKey.class);

    public final static String SUBTYPE = "secondary";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(SecondaryKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Secondary key for alternative record identification")

                // INHERIT FROM BASE KEY
                .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

                // SECONDARY KEY SPECIFIC ATTRIBUTES (base attributes inherited)
                // Note: keys and description are inherited from key.base
            );

            log.debug("Registered SecondaryKey type with unified registry");

            // Register SecondaryKey-specific validation constraints
            setupSecondaryKeyValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register SecondaryKey type with unified registry", e);
        }
    }

    /**
     * Setup SecondaryKey-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupSecondaryKeyValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Secondary key name uniqueness within object
            ValidationConstraint secondaryKeyUniquenessValidation = new ValidationConstraint(
                "secondarykey.uniqueness.validation",
                "SecondaryKey names must be unique within a MetaObject",
                (metadata) -> metadata instanceof SecondaryKey,
                (metadata, value) -> {
                    if (metadata instanceof SecondaryKey) {
                        SecondaryKey secondaryKey = (SecondaryKey) metadata;
                        if (secondaryKey.getParent() instanceof MetaObject) {
                            MetaObject metaObject = (MetaObject) secondaryKey.getParent();
                            List<SecondaryKey> secondaryKeys = metaObject.getChildren(SecondaryKey.class);
                            // Check that no other secondary key has the same name
                            String currentName = secondaryKey.getName();
                            long nameCount = secondaryKeys.stream()
                                .filter(sk -> currentName != null && currentName.equals(sk.getName()))
                                .count();
                            return nameCount <= 1; // Only itself should have this name
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(secondaryKeyUniquenessValidation);

            log.debug("Registered SecondaryKey-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register SecondaryKey constraints", e);
        }
    }

    public SecondaryKey(String name) {
        super(SUBTYPE, name);
    }

    private SecondaryKey(String subType, String name) {
        super(subType, name);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.SECONDARY, getKeyFields(), o );
    }
}
