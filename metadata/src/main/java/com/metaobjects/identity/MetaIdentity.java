package com.metaobjects.identity;

import com.metaobjects.MetaData;
import com.metaobjects.field.MetaField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.registry.MetaDataRegistry;
import com.metaobjects.attr.MetaAttribute;
import com.metaobjects.attr.StringArrayAttribute;
import com.metaobjects.attr.StringAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static com.metaobjects.object.MetaObject.ATTR_DESCRIPTION;

/**
 * Abstract base class for MetaIdentity that defines how objects are uniquely identified
 * and provides the foundation for relationships between objects.
 *
 * This replaces the deprecated field-level approach (@isPrimaryKey) with proper
 * object-level identity concepts that can handle both simple and compound keys.
 *
 * Concrete implementations: PrimaryIdentity, SecondaryIdentity
 */
public abstract class MetaIdentity extends MetaData {

    private static final Logger log = LoggerFactory.getLogger(MetaIdentity.class);

    // === TYPE AND SUBTYPE CONSTANTS ===
    /** Identity type constant - MetaIdentity owns this concept */
    public final static String TYPE_IDENTITY = "identity";

    /** Primary key subtype - one per object, main identifier */
    public final static String SUBTYPE_PRIMARY = "primary";

    /** Secondary key subtype - business identifiers, multiple allowed */
    public final static String SUBTYPE_SECONDARY = "secondary";

    // === ESSENTIAL ATTRIBUTES ===
    /** Array of field names that comprise this identity */
    public final static String ATTR_FIELDS = "fields";

    /** Generation strategy for identity values */
    public final static String ATTR_GENERATION = "generation";

    // === GENERATION STRATEGY CONSTANTS ===
    /** Auto-incrementing integer (database chooses implementation) */
    public static final String GENERATION_INCREMENT = "increment";

    /** UUID/GUID generation */
    public static final String GENERATION_UUID = "uuid";

    /** Application assigns the value */
    public static final String GENERATION_ASSIGNED = "assigned";


    // Registration methods moved to concrete classes: PrimaryIdentity, SecondaryIdentity

    public MetaIdentity(String subType, String name) {
        super(TYPE_IDENTITY, subType, name);
    }

    // === ESSENTIAL ATTRIBUTE ACCESSORS ===

    /**
     * Returns the field names that comprise this identity.
     * Handles both StringArrayAttribute (legacy) and StringAttribute with @isArray (current).
     */
    public List<String> getFields() {
        if (!hasMetaAttr(ATTR_FIELDS)) {
            return new ArrayList<>();
        }

        MetaAttribute fieldsAttr = getMetaAttr(ATTR_FIELDS);

        // Handle legacy StringArrayAttribute format
        if (fieldsAttr instanceof StringArrayAttribute) {
            StringArrayAttribute legacyFieldsAttr = (StringArrayAttribute) fieldsAttr;
            List<String> fieldsList = legacyFieldsAttr.getValue();
            return fieldsList != null ? fieldsList : new ArrayList<>();
        }

        // Handle new StringAttribute with @isArray format
        if (fieldsAttr instanceof StringAttribute) {
            StringAttribute stringAttr = (StringAttribute) fieldsAttr;
            String value = stringAttr.getValueAsString();

            if (value == null || value.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // Parse comma-delimited format: "field1,field2,field3" or single "field1"
            if (value.contains(",")) {
                List<String> fieldsList = new ArrayList<>();
                for (String field : value.split(",")) {
                    String trimmed = field.trim();
                    if (!trimmed.isEmpty()) {
                        fieldsList.add(trimmed);
                    }
                }
                return fieldsList;
            } else {
                // Single field
                return Arrays.asList(value.trim());
            }
        }

        // Fallback for unknown attribute types
        return new ArrayList<>();
    }

    /**
     * Returns the MetaField objects that comprise this identity.
     */
    public List<MetaField> getMetaFields() {
        List<MetaField> metaFields = new ArrayList<>();
        MetaObject parent = (MetaObject) getParent();
        if (parent != null) {
            for (String fieldName : getFields()) {
                MetaField field = parent.getMetaField(fieldName);
                if (field != null) {
                    metaFields.add(field);
                }
            }
        }
        return metaFields;
    }

    /**
     * Returns the generation strategy for this identity.
     */
    public String getGeneration() {
        return hasMetaAttr(ATTR_GENERATION) ?
               getMetaAttr(ATTR_GENERATION).getValueAsString() : null;
    }


    // === CONVENIENCE METHODS ===

    public boolean isPrimary() {
        return SUBTYPE_PRIMARY.equals(getSubType());
    }

    public boolean isSecondary() {
        return SUBTYPE_SECONDARY.equals(getSubType());
    }

    public boolean hasGeneration() {
        return getGeneration() != null;
    }

    public boolean isAutoGenerated() {
        String generation = getGeneration();
        return GENERATION_INCREMENT.equals(generation) || GENERATION_UUID.equals(generation);
    }

    public boolean isIncrement() {
        return GENERATION_INCREMENT.equals(getGeneration());
    }

    public boolean isUuid() {
        return GENERATION_UUID.equals(getGeneration());
    }

    public boolean isAssigned() {
        return GENERATION_ASSIGNED.equals(getGeneration());
    }

    /**
     * Returns true if this is a compound identity (multiple fields).
     */
    public boolean isCompound() {
        return getFields().size() > 1;
    }

    /**
     * Returns true if this is a simple identity (single field).
     */
    public boolean isSimple() {
        return getFields().size() == 1;
    }

    /**
     * Returns the single field name for simple identities.
     * Throws exception for compound identities.
     */
    public String getSingleFieldName() {
        List<String> fields = getFields();
        if (fields.size() != 1) {
            throw new IllegalStateException("Identity " + getName() + " has " + fields.size() +
                " fields, not a single field. Use getFields() instead.");
        }
        return fields.get(0);
    }

    /**
     * Returns the single MetaField for simple identities.
     * Throws exception for compound identities.
     */
    public MetaField getSingleMetaField() {
        List<MetaField> metaFields = getMetaFields();
        if (metaFields.size() != 1) {
            throw new IllegalStateException("Identity " + getName() + " has " + metaFields.size() +
                " fields, not a single field. Use getMetaFields() instead.");
        }
        return metaFields.get(0);
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]{%s -> %s}",
            getClass().getSimpleName(),
            getType(),
            getSubType(),
            getName(),
            getFields());
    }
}