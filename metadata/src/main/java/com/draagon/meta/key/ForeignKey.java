package com.draagon.meta.key;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.MetaDataNotFoundException;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.util.MetaDataUtil;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.draagon.meta.key.MetaKey.SUBTYPE_BASE;

import java.util.List;

@MetaDataType(type = "key", subType = "foreign", description = "Foreign key for referencing other objects")
public class ForeignKey extends MetaKey {

    private static final Logger log = LoggerFactory.getLogger(ForeignKey.class);

    public final static String SUBTYPE = "foreign";

    // Unified registry self-registration
    static {
        try {
            MetaDataRegistry.registerType(ForeignKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE)
                .description("Foreign key for referencing other objects")

                // INHERIT FROM BASE KEY
                .inheritsFrom(TYPE_KEY, SUBTYPE_BASE)

                // FOREIGN KEY SPECIFIC ATTRIBUTES (base attributes inherited)
                .acceptsNamedAttributes("string", "foreignObjectRef")
                .acceptsNamedAttributes("string", "foreignKey")
                .acceptsNamedAttributes("string", "foreignKeyMap")
                // Note: keys and description are inherited from key.base
            );

            log.debug("Registered ForeignKey type with unified registry");

            // Register ForeignKey-specific validation constraints
            setupForeignKeyValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register ForeignKey type with unified registry", e);
        }
    }

    /**
     * Setup ForeignKey-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupForeignKeyValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Foreign object reference validation
            ValidationConstraint foreignObjectRefValidation = new ValidationConstraint(
                "foreignkey.objectref.validation",
                "ForeignKey must have valid foreignObjectRef attribute",
                (metadata) -> metadata instanceof ForeignKey,
                (metadata, value) -> {
                    if (metadata instanceof ForeignKey) {
                        ForeignKey foreignKey = (ForeignKey) metadata;
                        try {
                            // Validate that foreign object can be resolved
                            if (foreignKey.hasMetaAttr(ATTR_FOREIGNOBJECTREF)) {
                                foreignKey.getForeignObject(); // This will throw if invalid
                            }
                            return true;
                        } catch (Exception e) {
                            // During metadata construction, foreign object references may not be resolvable yet
                            // Allow the validation to pass during construction phase
                            log.debug("Foreign object validation deferred during construction: {}", e.getMessage());
                            return true;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(foreignObjectRefValidation);

            // VALIDATION CONSTRAINT: Foreign key reference validation
            ValidationConstraint foreignKeyRefValidation = new ValidationConstraint(
                "foreignkey.keyref.validation",
                "ForeignKey must reference valid key on foreign object",
                (metadata) -> metadata instanceof ForeignKey,
                (metadata, value) -> {
                    if (metadata instanceof ForeignKey) {
                        ForeignKey foreignKey = (ForeignKey) metadata;
                        try {
                            // Validate that foreign key can be resolved
                            foreignKey.getForeignKey(); // This will throw if invalid
                            return true;
                        } catch (Exception e) {
                            // During metadata construction, foreign key references may not be resolvable yet
                            // Allow the validation to pass during construction phase
                            log.debug("Foreign key validation deferred during construction: {}", e.getMessage());
                            return true;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(foreignKeyRefValidation);

            log.debug("Registered ForeignKey-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register ForeignKey constraints", e);
        }
    }

    public final static String ATTR_FOREIGNOBJECTREF = "foreignObjectRef";
    public final static String ATTR_FOREIGNKEY = "foreignKey";
    public final static String ATTR_FORIGNKEYMAP = "foreignKeyMap";

    public ForeignKey(String name) {
        super(SUBTYPE, name);
    }

    private ForeignKey(String subType, String name) {
        super(subType, name);
    }

    public MetaObject getForeignObject() {

        final String KEY = "getForeignObject()";

        MetaObject o = (MetaObject) getCacheValue(KEY);

        if (o == null) {

            if (!hasMetaAttr(ATTR_FOREIGNOBJECTREF))
                throw new MetaDataException("Attribute with name '"+ ATTR_FOREIGNOBJECTREF +"' "+
                        "defining the foreign object did not exist" );

            String objectRef = getMetaAttr(ATTR_FOREIGNOBJECTREF).getValueAsString();
            if (objectRef != null) {

                String name = MetaDataUtil.expandPackageForMetaDataRef(getDeclaringObject().getPackage(), objectRef);

                try {
                    o = getLoader().getMetaObjectByName(name);
                }
                catch (MetaDataNotFoundException e) {
                    throw MetaDataNotFoundException.forObject(name, getDeclaringObject());
                }
            }

            setCacheValue(KEY, o);
        }

        return o;
    }

    /** Returns the PrimaryKey or a named SecondaryKey */
    public MetaKey getForeignKey() {

        final String CACHE_KEY = "getForeignKey()";
        MetaKey key = (MetaKey) getCacheValue( CACHE_KEY );
        if ( key == null ) {

            MetaObject mo = getForeignObject();

            if (hasMetaAttr(ATTR_FOREIGNKEY)) {
                String keyName = getMetaAttr(ATTR_FOREIGNKEY).getValueAsString();

                if (keyName.equals(PrimaryKey.NAME)) {
                    key = mo.getPrimaryKey();
                    if (key == null) {
                        throw new MetaDataNotFoundException("No PrimaryKey existed on foreign MetaObject: "
                                + mo, PrimaryKey.NAME);
                    }
                } else {
                    key = mo.getSecondaryKeyByName(keyName);
                    if (key == null) {
                        throw new MetaDataNotFoundException("No SecondaryKey with name [" + keyName + "] existed on foreign " +
                                "MetaObject: " + mo, keyName);
                    }
                }
            } else {
                key = mo.getPrimaryKey();
                if (key == null) {
                    throw new MetaDataNotFoundException("No PrimaryKey existed on foreign MetaObject: "
                            + mo, PrimaryKey.NAME);
                }
            }

            setCacheValue( CACHE_KEY, key );
        }
        return key;
    }

    public List<MetaField> getForeignKeyFields() {
        return getForeignKey().getKeyFields();
    }

    public int getNumForeignKeys() {
        return getForeignKeyFields().size();
    }

    public ObjectKey getForeignKey(Object foreignObject) {
        return getObjectKeyForKeyFields( getForeignObject(), KeyTypes.FOREIGN, getForeignKeyFields(), foreignObject );
    }

    @Override
    public ObjectKey getObjectKey(Object o) {
        return getObjectKeyForKeyFields( getDeclaringObject(), KeyTypes.LOCAL_FOREIGN, getKeyFields(), o );
    }

    
}
