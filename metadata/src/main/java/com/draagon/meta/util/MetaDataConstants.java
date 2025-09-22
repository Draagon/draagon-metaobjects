/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.util;

/**
 * Consolidated constants for the MetaObjects framework.
 * Provides standardized string constants, separators, and common patterns
 * used throughout the metadata system.
 * 
 * @since 5.2.0
 */
public final class MetaDataConstants {

    private MetaDataConstants() {
        // Utility class - no instantiation
    }

    // === SEPARATORS ===
    
    /** Package separator used in metadata hierarchies */
    public static final String PKG_SEPARATOR = "::";
    
    /** Key separator for creating composite keys */
    public static final String KEY_SEPARATOR = "-";
    
    /** Path separator for hierarchical paths */
    public static final String PATH_SEPARATOR = "/";
    
    /** Attribute prefix for inline JSON attributes */
    public static final String JSON_ATTR_PREFIX = "@";

    // === PRIMARY TYPE CONSTANTS (at abstraction level) ===

    /** Standard type name for field metadata */
    public static final String TYPE_FIELD = "field";

    /** Standard type name for object metadata */
    public static final String TYPE_OBJECT = "object";

    /** Standard type name for attribute metadata */
    public static final String TYPE_ATTR = "attr";

    /** Standard type name for validator metadata */
    public static final String TYPE_VALIDATOR = "validator";

    /** Standard type name for view metadata */
    public static final String TYPE_VIEW = "view";

    /** Standard type name for key metadata */
    public static final String TYPE_KEY = "key";

    /** Standard type name for loader metadata */
    public static final String TYPE_LOADER = "loader";

    // === BASE SUBTYPE CONSTANTS ===

    /** Base subtype for field metadata inheritance */
    public static final String SUBTYPE_BASE = "base";

    // === UNIVERSAL ATTRIBUTE NAMES (apply to all MetaData) ===

    /** Universal attribute for abstract metadata marker */
    public static final String ATTR_IS_ABSTRACT = "isAbstract";

    /** Standard attribute name for 'name' */
    public static final String ATTR_NAME = "name";

    /** Standard attribute name for 'type' */
    public static final String ATTR_TYPE = "type";

    /** Standard attribute name for 'subType' */
    public static final String ATTR_SUBTYPE = "subType";

    /** Standard attribute name for 'package' */
    public static final String ATTR_PACKAGE = "package";

    /** Standard attribute name for 'children' */
    public static final String ATTR_CHILDREN = "children";

    /** Standard attribute name for 'metadata' (root element) */
    public static final String ATTR_METADATA = "metadata";

    // === FIELD-LEVEL ATTRIBUTE NAMES (apply to all MetaField types) ===

    /** Field attribute for required field marker */
    public static final String ATTR_REQUIRED = "required";

    /** Field attribute for default value specification */
    public static final String ATTR_DEFAULT_VALUE = "defaultValue";

    /** Field attribute for validation rules */
    public static final String ATTR_VALIDATION = "validation";

    /** Field attribute for default view specification */
    public static final String ATTR_DEFAULT_VIEW = "defaultView";

    // === OBJECT-LEVEL ATTRIBUTE NAMES (apply to all MetaObject types) ===

    /** Object attribute for inheritance specification */
    public static final String ATTR_EXTENDS = "extends";

    /** Object attribute for interface implementation */
    public static final String ATTR_IMPLEMENTS = "implements";

    /** Object attribute for interface marker */
    public static final String ATTR_IS_INTERFACE = "isInterface";

    // === CROSS-MODULE SERVICE ATTRIBUTE NAMES ===
    // These are defined here for compile-time checking across modules

    // Database attributes (used by omdb module)
    /** Database table name attribute */
    public static final String ATTR_DB_TABLE = "dbTable";

    /** Database column name attribute */
    public static final String ATTR_DB_COLUMN = "dbColumn";

    /** Database schema name attribute */
    public static final String ATTR_DB_SCHEMA = "dbSchema";

    /** Database type specification attribute */
    public static final String ATTR_DB_TYPE = "dbType";

    /** Database nullable specification attribute */
    public static final String ATTR_DB_NULLABLE = "dbNullable";

    /** Database length specification attribute */
    public static final String ATTR_DB_LENGTH = "dbLength";

    // Codegen attributes (used by codegen modules)
    /** Codegen JPA skip attribute */
    public static final String ATTR_SKIP_JPA = "skipJpa";

    /** Codegen ID field marker attribute */
    public static final String ATTR_IS_ID = "isId";

    /** Codegen searchable field marker attribute */
    public static final String ATTR_IS_SEARCHABLE = "isSearchable";

    /** Codegen optional field marker attribute */
    public static final String ATTR_IS_OPTIONAL = "isOptional";

    /** Codegen JPA generation marker attribute */
    public static final String ATTR_HAS_JPA = "hasJpa";

    /** Codegen validation marker attribute */
    public static final String ATTR_HAS_VALIDATION = "hasValidation";

    // === LEGACY ATTRIBUTE ALIASES (for backward compatibility) ===

    /** @deprecated Use ATTR_DB_TABLE instead */
    @Deprecated
    public static final String TYPE_ATTRIBUTE = TYPE_ATTR;

    // === ERROR MESSAGE FORMATS ===
    
    /** Format template for not found errors */
    public static final String ERR_NOT_FOUND_FORMAT = "%s '%s' not found in %s";
    
    /** Format template for type mismatch errors */
    public static final String ERR_TYPE_MISMATCH_FORMAT = "Type mismatch at %s: Expected %s, got %s";
    
    /** Format template for validation errors */
    public static final String ERR_VALIDATION_FORMAT = "Validation failed for %s: %s";
    
    /** Format template for configuration errors */
    public static final String ERR_CONFIG_FORMAT = "Configuration error in %s: %s";

    // === DISPLAY VALUES ===
    
    /** Display value for null objects */
    public static final String DISPLAY_NULL = "<null>";
    
    /** Display value for empty strings */
    public static final String DISPLAY_EMPTY = "<empty>";
    
    /** Display value when no items are available */
    public static final String DISPLAY_NONE = "<none>";
    
    /** Ellipsis for truncated values */
    public static final String DISPLAY_ELLIPSIS = "...";

    // === REGISTRY AND SERVICE NAMES ===
    
    /** Default registry name */
    public static final String DEFAULT_REGISTRY = "default";
    
    /** Core metadata type provider name */
    public static final String CORE_TYPE_PROVIDER = "CoreMetaDataTypeProvider";
    
    /** Standard service registry name */
    public static final String STANDARD_REGISTRY = "StandardServiceRegistry";
    
    /** OSGI service registry name */
    public static final String OSGI_REGISTRY = "OSGIServiceRegistry";

    // === LOADING AND INITIALIZATION ===
    
    /** Default loader timeout in milliseconds */
    public static final long DEFAULT_LOADER_TIMEOUT = 30000L;
    
    /** Default cache size for metadata objects */
    public static final int DEFAULT_CACHE_SIZE = 1000;
    
    /** Maximum string length for display before truncation */
    public static final int MAX_DISPLAY_LENGTH = 100;

    // === CONSTRAINT PATTERNS ===
    
    /** Regular expression for valid metadata names */
    public static final String VALID_NAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";
    
    /** Regular expression for valid package names */
    public static final String VALID_PACKAGE_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*(::[a-zA-Z][a-zA-Z0-9_]*)*$";

    // === HELPER METHODS FOR COMMON OPERATIONS ===
    
    /**
     * Creates a composite key from type and name
     * 
     * @param type the type component
     * @param name the name component
     * @return formatted composite key
     */
    public static String createKey(String type, String name) {
        return String.format("%s%s%s", type, KEY_SEPARATOR, name);
    }
    
    /**
     * Creates a hierarchical path from components
     * 
     * @param components the path components
     * @return formatted hierarchical path
     */
    public static String createPath(String... components) {
        return String.join(PATH_SEPARATOR, components);
    }
    
    /**
     * Creates a package-qualified name
     * 
     * @param packageName the package name (may be null)
     * @param name the simple name
     * @return qualified name
     */
    public static String createQualifiedName(String packageName, String name) {
        if (packageName == null || packageName.isEmpty()) {
            return name;
        }
        return String.format("%s%s%s", packageName, PKG_SEPARATOR, name);
    }
    
    /**
     * Truncates a display string if it exceeds the maximum length
     * 
     * @param value the value to format for display
     * @return truncated string with ellipsis if needed
     */
    public static String formatForDisplay(String value) {
        if (value == null) {
            return DISPLAY_NULL;
        }
        if (value.isEmpty()) {
            return DISPLAY_EMPTY;
        }
        if (value.length() > MAX_DISPLAY_LENGTH) {
            return value.substring(0, MAX_DISPLAY_LENGTH - DISPLAY_ELLIPSIS.length()) + DISPLAY_ELLIPSIS;
        }
        return value;
    }
}