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

@MetaDataType(type = "key", subType = "primary", description = "Primary key for unique record identification")
public class PrimaryKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(PrimaryKey.class);

    public final static String SUBTYPE = "primary";
    public final static String NAME = "primary";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(PrimaryKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Primary key for unique record identification")

                // INHERIT FROM BASE KEY
                .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

                // PRIMARY KEY SPECIFIC ATTRIBUTES (base attributes inherited)
                // Note: keys and description are inherited from key.base
            );

            log.debug("Registered PrimaryKey type with unified registry");

            // Register PrimaryKey-specific validation constraints
            setupPrimaryKeyValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register PrimaryKey type with unified registry", e);
        }
    }

    /**
     * Setup PrimaryKey-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupPrimaryKeyValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Primary key uniqueness within object
            ValidationConstraint primaryKeyUniquenessValidation = new ValidationConstraint(
                "primarykey.uniqueness.validation",
                "Only one PrimaryKey allowed per MetaObject",
                (metadata) -> metadata instanceof PrimaryKey,
                (metadata, value) -> {
                    if (metadata instanceof PrimaryKey) {
                        PrimaryKey primaryKey = (PrimaryKey) metadata;
                        if (primaryKey.getParent() instanceof MetaObject) {
                            MetaObject metaObject = (MetaObject) primaryKey.getParent();
                            List<PrimaryKey> primaryKeys = metaObject.getChildren(PrimaryKey.class);
                            // Only one primary key allowed per object
                            return primaryKeys.size() <= 1;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(primaryKeyUniquenessValidation);

            log.debug("Registered PrimaryKey-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register PrimaryKey constraints", e);
        }
    }

    public PrimaryKey() {
        super(SUBTYPE, NAME);
    }
    
    public PrimaryKey(String name) {
        super(SUBTYPE, name);
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.PRIMARY, getKeyFields(), o );
    }
}
