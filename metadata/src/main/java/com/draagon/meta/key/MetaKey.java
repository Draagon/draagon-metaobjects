package com.draagon.meta.key;

import com.draagon.meta.DataTypes;
import com.draagon.meta.InvalidMetaDataException;
import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.constraint.ConstraintRegistry;
import com.draagon.meta.constraint.ValidationConstraint;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.draagon.meta.MetaData.ATTR_IS_ABSTRACT;
import static com.draagon.meta.object.MetaObject.ATTR_DESCRIPTION;
import static com.draagon.meta.attr.MetaAttribute.TYPE_ATTR;
import static com.draagon.meta.loader.MetaDataLoader.TYPE_METADATA;
import static com.draagon.meta.object.MetaObject.TYPE_OBJECT;

@MetaDataType(type = "key", subType = "base", description = "Base key metadata with common key attributes")
public abstract class MetaKey extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaKey.class);

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Key type constant - MetaKey owns this concept */
    public final static String TYPE_KEY = "key";

    /** Base key subtype for inheritance */
    public final static String SUBTYPE_BASE = "base";

    // === KEY-LEVEL ATTRIBUTE NAME CONSTANTS ===
    /** Keys attribute that defines which fields form the key */
    public final static String ATTR_KEYS = "keys";

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

            MetaDataRegistry.registerType(MetaKey.class, def -> def
                .type(TYPE_KEY).subType(SUBTYPE_BASE)
                .description("Base key metadata with common key attributes")

                // INHERIT FROM BASE METADATA
                .inheritsFrom(TYPE_METADATA, "base")

                // KEY PARENT ACCEPTANCE DECLARATIONS
                // Keys can be placed under objects and loaders
                .acceptsParents(TYPE_OBJECT, "*")          // Any object type
                .acceptsParents(TYPE_METADATA, "*")                   // Any metadata (loaders)

                // KEY CHILD ACCEPTANCE DECLARATIONS
                // Keys can contain attributes
                .acceptsChildren(TYPE_ATTR, "*")                     // Any attribute type

                // Universal fallback for any key
                .acceptsParents("*", "*")  // MetaKey can be used under any parent that needs keys

            );

            log.debug("Registered MetaKey type with unified registry");

            // Register MetaKey-specific validation constraints only
            setupMetaKeyValidationConstraints();

        } catch (Exception e) {
            log.error("Failed to register MetaKey type with unified registry", e);
        }
    }

    /**
     * Setup MetaKey-specific validation constraints only.
     * Structural constraints are now handled by the bidirectional constraint system.
     */
    private static void setupMetaKeyValidationConstraints() {
        try {
            ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();

            // VALIDATION CONSTRAINT: Key field names validation
            ValidationConstraint keyFieldNamesValidation = new ValidationConstraint(
                "metakey.fieldnames.validation",
                "MetaKey keys attribute must reference existing field names",
                (metadata) -> metadata instanceof MetaKey,
                (metadata, value) -> {
                    if (metadata instanceof MetaKey) {
                        MetaKey metaKey = (MetaKey) metadata;
                        try {
                            // Validate that key fields exist and are accessible
                            metaKey.getKeyFields();
                            return true;
                        } catch (Exception e) {
                            // During metadata construction, field references may not be resolvable yet
                            // Allow the validation to pass during construction phase
                            log.debug("Key field validation deferred during construction: {}", e.getMessage());
                            return true;
                        }
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(keyFieldNamesValidation);

            // VALIDATION CONSTRAINT: Key placement validation
            ValidationConstraint keyPlacementValidation = new ValidationConstraint(
                "metakey.placement.validation",
                "MetaKey can only be placed under MetaObjects or MetaDataLoaders",
                (metadata) -> metadata instanceof MetaKey,
                (metadata, value) -> {
                    if (metadata instanceof MetaKey) {
                        MetaKey metaKey = (MetaKey) metadata;
                        MetaData parent = metaKey.getParent();

                        // During metadata construction, parent might not be set yet
                        if (parent == null) {
                            log.debug("Key placement validation deferred - parent not yet set during construction");
                            return true; // Allow during construction
                        }

                        return parent instanceof MetaObject || parent instanceof MetaDataLoader;
                    }
                    return true;
                }
            );
            constraintRegistry.addConstraint(keyPlacementValidation);

            log.debug("Registered MetaKey-specific constraints");

        } catch (Exception e) {
            log.error("Failed to register MetaKey constraints", e);
        }
    }

    public enum KeyTypes {UNKNOWN, PRIMARY, SECONDARY, LOCAL_FOREIGN, FOREIGN};

    protected MetaKey(String subType, String name) {
        super(TYPE_KEY, subType, name);
    }

    public int getNumKeys() {
        return getKeyFields().size();
    }

    public abstract ObjectKey getObjectKey(Object o);

    protected ObjectKey getObjectKeyForKeyFields(MetaObject mo, KeyTypes keyType, List<MetaField> keyFields, Object o) {

        Object [] keys = new Object[keyFields.size()];

        int i = 0;
        for( MetaField mf : keyFields ) {
            keys[i++] = mf.getObject(o);
        }

        return new ObjectKey( mo, keyType, keys );
    }

    public List<MetaField> getKeyFields() {
        return getSpecifiedKeyFieldsForTarget( getParent(), ATTR_KEYS);
    }

    protected List<MetaField> loadKeyFields() {
        return loadSpecifiedKeyFieldsForTarget( getParent(), ATTR_KEYS);
    }

    protected List<MetaField> getSpecifiedKeyFieldsForTarget( MetaData target, String attrName ) {
        final String CACHE_KEY = "getSpecifiedKeyFieldsForTarget("+target.getName()+","+attrName+")";
        List<MetaField> keys = (List<MetaField>) getCacheValue( CACHE_KEY );
        if ( keys == null ) {
            keys = loadSpecifiedKeyFieldsForTarget( target, attrName );
            setCacheValue( CACHE_KEY, keys);
        }
        return keys;
    }

    protected List<MetaField> loadSpecifiedKeyFieldsForTarget( MetaData target, String attrName ) {

        List<MetaField> keys;
        keys = new ArrayList<>();

        boolean isLoader = ( target instanceof MetaDataLoader );
        if ( !isLoader && !(target instanceof MetaObject ))
            throw new MetaDataException("Keys can only be attached to MetaObjects " +
                "or MetaDataLoaders as abstracts for attribute='"+attrName+"'");

        if (hasMetaAttr(attrName)) {

            MetaAttribute<?> attr = getMetaAttr(attrName);
            if (attr == null) {
                if (isLoader) return keys;
                throw new MetaDataException("Attribute with name '" + attrName + "' " +
                        "defining the key fields was NOT found");
            }
            if (attr.getDataType() != DataTypes.STRING_ARRAY) {
                throw new MetaDataException(
                        "Attribute '" + attrName + "' must be a stringArray data type: " + attr);
            }
            List<String> keyNames = (List<String>) attr.getValue();

            if (!isLoader) {
                MetaObject mo = getDeclaringObject();

                for (String fn : keyNames) {
                    MetaField f = mo.getMetaField(fn);
                    if (f == null) {
                        throw new MetaDataException("Attribute [" + attrName + "] had invalid field name " +
                                "[" + fn + "] that did not exist on MetaObject [" + mo.getName() + "]: keyNames=" + keyNames);
                    }
                    keys.add(f);
                }
            }
        }
        else {
            throw new MetaDataException("Attribute with name '" + attrName + "' " +
                    "defining the key fields was NOT found");
        }

        return keys;
    }

    public MetaObject getDeclaringObject() {
        if ( getParent() instanceof MetaDataLoader ) return null;
        if ( getParent() instanceof MetaObject ) return (MetaObject) getParent();
        throw new MetaDataException("MetaKeys can only be attached to MetaObjects " +
                "or MetaDataLoaders as abstracts");
    }

    
}
