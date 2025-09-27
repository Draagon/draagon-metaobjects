/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.db;

import com.metaobjects.object.MetaObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Interface for database drivers that support bulk operations for improved performance.
 * Implementing this interface allows ObjectManagerDB to use database-specific
 * batch operations instead of individual SQL statements.
 */
public interface BulkOperationSupport {
    
    /**
     * Performs bulk creation of objects using database-specific batch operations
     * 
     * @param conn Database connection
     * @param mc MetaObject describing the objects
     * @param mapping Database mapping for the objects
     * @param objects Collection of objects to create
     * @return Number of objects successfully created
     * @throws SQLException if a database error occurs
     */
    int createBulk(Connection conn, MetaObject mc, ObjectMappingDB mapping, Collection<Object> objects) throws SQLException;
    
    /**
     * Performs bulk updates of objects using database-specific batch operations
     * 
     * @param conn Database connection
     * @param mc MetaObject describing the objects
     * @param mapping Database mapping for the objects
     * @param objects Collection of objects to update
     * @return Number of objects successfully updated
     * @throws SQLException if a database error occurs
     */
    int updateBulk(Connection conn, MetaObject mc, ObjectMappingDB mapping, Collection<Object> objects) throws SQLException;
    
    /**
     * Performs bulk deletion of objects using database-specific batch operations
     * 
     * @param conn Database connection
     * @param mc MetaObject describing the objects
     * @param mapping Database mapping for the objects
     * @param objects Collection of objects to delete
     * @return Number of objects successfully deleted
     * @throws SQLException if a database error occurs
     */
    int deleteBulk(Connection conn, MetaObject mc, ObjectMappingDB mapping, Collection<Object> objects) throws SQLException;
    
    /**
     * Returns the optimal batch size for bulk operations
     * 
     * @return Recommended batch size (e.g., 1000)
     */
    default int getOptimalBatchSize() {
        return 1000;
    }
    
    /**
     * Indicates whether bulk operations should be used for the given collection size
     * 
     * @param collectionSize Size of the collection to process
     * @return true if bulk operations should be used, false for individual operations
     */
    default boolean shouldUseBulkOperations(int collectionSize) {
        return collectionSize >= 10; // Use bulk operations for 10+ objects
    }
}