/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.generator.mustache;

/**
 * Constants for JPA (Java Persistence API) code generation using Mustache templates.
 *
 * <p>These constants define attribute names and values specific to JPA entity generation,
 * including annotations, mapping strategies, and persistence configuration.</p>
 *
 * @since 6.0.0
 */
public final class JpaConstants {

    private JpaConstants() {
        // Utility class - no instantiation
    }

    // === JPA GENERATION ATTRIBUTES ===

    /** Attribute indicating whether to generate JPA annotations */
    public static final String ATTR_HAS_JPA = "hasJpa";

    /** Attribute for JPA entity table name mapping */
    public static final String ATTR_JPA_TABLE = "jpaTable";

    /** Attribute for JPA column name mapping */
    public static final String ATTR_JPA_COLUMN = "jpaColumn";

    /** Attribute indicating a field is a JPA identifier */
    public static final String ATTR_JPA_ID = "jpaId";

    /** Attribute for JPA column length specification */
    public static final String ATTR_JPA_LENGTH = "jpaLength";

    /** Attribute for JPA nullable specification */
    public static final String ATTR_JPA_NULLABLE = "jpaNullable";

    /** Attribute for JPA unique constraint */
    public static final String ATTR_JPA_UNIQUE = "jpaUnique";

    /** Attribute for JPA cascade type */
    public static final String ATTR_JPA_CASCADE = "jpaCascade";

    /** Attribute for JPA fetch type */
    public static final String ATTR_JPA_FETCH = "jpaFetch";

    // === JPA ANNOTATION NAMES ===

    /** JPA Entity annotation */
    public static final String JPA_ENTITY = "Entity";

    /** JPA Table annotation */
    public static final String JPA_TABLE = "Table";

    /** JPA Id annotation */
    public static final String JPA_ID = "Id";

    /** JPA Column annotation */
    public static final String JPA_COLUMN = "Column";

    /** JPA GeneratedValue annotation */
    public static final String JPA_GENERATED_VALUE = "GeneratedValue";

    /** JPA OneToMany annotation */
    public static final String JPA_ONE_TO_MANY = "OneToMany";

    /** JPA ManyToOne annotation */
    public static final String JPA_MANY_TO_ONE = "ManyToOne";

    /** JPA ManyToMany annotation */
    public static final String JPA_MANY_TO_MANY = "ManyToMany";

    /** JPA JoinColumn annotation */
    public static final String JPA_JOIN_COLUMN = "JoinColumn";

    // === JPA PACKAGE IMPORTS ===

    /** JPA persistence package */
    public static final String JPA_PERSISTENCE_PACKAGE = "javax.persistence";

    /** JPA validation package */
    public static final String JPA_VALIDATION_PACKAGE = "javax.validation.constraints";
}