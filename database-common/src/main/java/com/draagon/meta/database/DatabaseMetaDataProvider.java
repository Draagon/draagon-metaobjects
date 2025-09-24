package com.draagon.meta.database;

import com.draagon.meta.database.service.DatabaseService;
import com.draagon.meta.registry.MetaDataRegistry;
import com.draagon.meta.registry.MetaDataTypeProvider;

/**
 * Database MetaData provider that registers type extensions for database services.
 *
 * <p>This provider delegates to service classes that contain the actual extension logic
 * and constants for database operations. It extends existing MetaData types with
 * database-specific attributes needed for ORM mapping and SQL generation.</p>
 *
 * <h3>Services Provided:</h3>
 * <ul>
 * <li><strong>DatabaseService:</strong> Core database attributes (tables, columns, constraints)</li>
 * </ul>
 *
 * <h3>Priority:</h3>
 * <p>Priority 150 - Runs after core types but before code generation services.
 * This ensures database attributes are available for code generators that need them.</p>
 *
 * @since 6.0.0
 */
public class DatabaseMetaDataProvider implements MetaDataTypeProvider {

    @Override
    public void registerTypes(MetaDataRegistry registry) {
        // Delegate to service class that contains the extension logic and constants
        DatabaseService.registerTypeExtensions(registry);
    }

    @Override
    public int getPriority() {
        // Priority 150: After core types, before code generation (200+)
        return 150;
    }

    @Override
    public String getDescription() {
        return "Database MetaData Provider - ORM mapping and SQL generation extensions";
    }
}