/*
 * Copyright 2004 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared database attribute registration for cross-module reuse.
 * 
 * <p>This class provides centralized registration of database-related attributes
 * that are used across multiple modules:</p>
 * <ul>
 *   <li><strong>JPA Code Generation</strong> - Uses dbTable, dbColumn, dbNullable</li>
 *   <li><strong>ObjectManagerDB</strong> - Uses same attributes for ORM mapping</li>
 *   <li><strong>Database Tools</strong> - Schema generation and migration utilities</li>
 * </ul>
 * 
 * <p>By centralizing these attribute definitions, we ensure consistency across
 * modules and avoid duplication of database metadata concerns.</p>
 * 
 * @since 6.0.0
 */
public final class DatabaseAttributeRegistration {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseAttributeRegistration.class);
    private static boolean registered = false;
    
    // Private constructor - utility class
    private DatabaseAttributeRegistration() {}
    
    /**
     * Register database attribute awareness with the unified registry.
     * 
     * <p>This method is idempotent and can be called multiple times safely.
     * All database attributes are optional - when not specified, intelligent defaults
     * are provided by the code generation system:</p>
     * 
     * <h3>Object-Level Database Attributes (Optional):</h3>
     * <ul>
     *   <li><strong>dbTable</strong> - Database table name (default: object name in snake_case)</li>
     *   <li><strong>dbSchema</strong> - Database schema name (default: not specified)</li>
     *   <li><strong>hasAuditing</strong> - Whether table has audit columns (default: false)</li>
     * </ul>
     * 
     * <h3>Field-Level Database Attributes (Optional):</h3>
     * <ul>
     *   <li><strong>dbColumn</strong> - Database column name (default: field name in snake_case)</li>
     *   <li><strong>dbNullable</strong> - Whether column accepts NULL (default: inferred from validators)</li>
     *   <li><strong>dbType</strong> - Explicit database type override (default: derived from field type)</li>
     *   <li><strong>dbLength</strong> - Column length (default: inferred from validators)</li>
     *   <li><strong>dbPrecision</strong> - Precision for decimal types (default: database-specific)</li>
     *   <li><strong>dbScale</strong> - Scale for decimal types (default: database-specific)</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> These attributes are no longer globally required.
     * The system uses intelligent naming logic to provide defaults
     * when attributes are not explicitly specified.</p>
     */
    public static synchronized void registerDatabaseAttributes() {
        if (registered) {
            log.debug("Database attributes already registered, skipping");
            return;
        }
        
        try {
            // Database attributes are now fully optional with intelligent defaults
            // No global child requirements needed - DatabaseNamingUtils provides smart defaults
            
            registered = true;
            log.info("Database attribute awareness registered - all attributes are optional with intelligent defaults");
            
        } catch (Exception e) {
            log.error("Failed to register database attributes", e);
            throw new RuntimeException("Database attribute registration failed", e);
        }
    }
    
    /**
     * Check if database attributes have been registered.
     * 
     * @return true if database attributes are registered, false otherwise
     */
    public static boolean isRegistered() {
        return registered;
    }
    
    // Static initializer to auto-register when class is loaded
    static {
        registerDatabaseAttributes();
    }
}