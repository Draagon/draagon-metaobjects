/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.columnar;

import com.metaobjects.MetaDataException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.exp.Expression;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for column family (wide-column) database operations beyond standard ObjectManager capabilities.
 * Supports Cassandra, HBase, and similar column family databases.
 */
public interface ColumnFamilyOperations {

    /**
     * Inserts or updates a row with time-to-live
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key for the row
     * @param clusteringColumns Clustering columns (can be null/empty)
     * @param obj Object to store
     * @param ttlSeconds Time-to-live in seconds
     */
    void upsertWithTTL(ObjectConnection c, MetaObject mc, Object partitionKey, Map<String, Object> clusteringColumns, Object obj, Integer ttlSeconds) throws MetaDataException;

    /**
     * Performs a conditional update using Compare-and-Set (CAS)
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key for the row
     * @param clusteringColumns Clustering columns
     * @param expectedValues Expected current values for conditional update
     * @param newValues New values to set
     * @return true if the update was applied, false if condition failed
     */
    boolean conditionalUpdate(ObjectConnection c, MetaObject mc, Object partitionKey, Map<String, Object> clusteringColumns, 
                            Map<String, Object> expectedValues, Map<String, Object> newValues) throws MetaDataException;

    /**
     * Gets a range of rows by clustering columns
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key
     * @param startClusteringKey Start of clustering key range (inclusive)
     * @param endClusteringKey End of clustering key range (exclusive)
     * @param limit Maximum number of rows to return
     * @return Collection of objects in the range
     */
    Collection<?> getRange(ObjectConnection c, MetaObject mc, Object partitionKey, 
                          Map<String, Object> startClusteringKey, Map<String, Object> endClusteringKey, Integer limit) throws MetaDataException;

    /**
     * Gets rows by partition key with filtering
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key
     * @param filter Additional filtering expression (ALLOW FILTERING may be required)
     * @param limit Maximum number of rows to return
     * @return Collection of matching objects
     */
    Collection<?> getByPartitionWithFilter(ObjectConnection c, MetaObject mc, Object partitionKey, Expression filter, Integer limit) throws MetaDataException;

    /**
     * Performs batch operations across multiple partitions
     * @param c Connection to the column family database
     * @param operations List of batch operations to execute
     * @param consistency Consistency level for the batch
     * @return Results of the batch operations
     */
    List<Object> executeBatch(ObjectConnection c, List<ColumnFamilyOperation> operations, ConsistencyLevel consistency) throws MetaDataException;

    /**
     * Increments or decrements counter columns
     * @param c Connection to the column family database
     * @param mc MetaObject describing the counter column family
     * @param partitionKey Partition key
     * @param clusteringColumns Clustering columns
     * @param counterUpdates Map of counter column names to increment/decrement values
     */
    void updateCounters(ObjectConnection c, MetaObject mc, Object partitionKey, Map<String, Object> clusteringColumns, 
                       Map<String, Long> counterUpdates) throws MetaDataException;

    /**
     * Gets multiple rows by their keys asynchronously
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param keys List of row keys (partition key + clustering columns)
     * @return CompletableFuture containing map of key to object
     */
    CompletableFuture<Map<RowKey, Object>> getMultipleAsync(ObjectConnection c, MetaObject mc, List<RowKey> keys) throws MetaDataException;

    /**
     * Executes a prepared statement with bound parameters
     * @param c Connection to the column family database
     * @param preparedStatementId ID of the prepared statement
     * @param parameters Parameter values to bind
     * @return Query results
     */
    Collection<?> executePrepared(ObjectConnection c, String preparedStatementId, Map<String, Object> parameters) throws MetaDataException;

    /**
     * Creates a materialized view
     * @param c Connection to the column family database
     * @param baseColumnFamily Base column family MetaObject
     * @param viewName Name of the materialized view
     * @param selectColumns Columns to include in the view
     * @param whereClause WHERE clause for the view
     * @param primaryKey Primary key definition for the view
     */
    void createMaterializedView(ObjectConnection c, MetaObject baseColumnFamily, String viewName, 
                               List<String> selectColumns, String whereClause, List<String> primaryKey) throws MetaDataException;

    /**
     * Drops a materialized view
     * @param c Connection to the column family database
     * @param viewName Name of the materialized view to drop
     */
    void dropMaterializedView(ObjectConnection c, String viewName) throws MetaDataException;

    /**
     * Creates a secondary index
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param columnName Column to index
     * @param indexName Name for the index
     */
    void createSecondaryIndex(ObjectConnection c, MetaObject mc, String columnName, String indexName) throws MetaDataException;

    /**
     * Drops a secondary index
     * @param c Connection to the column family database
     * @param indexName Name of the index to drop
     */
    void dropSecondaryIndex(ObjectConnection c, String indexName) throws MetaDataException;

    /**
     * Gets token range information for a partition key
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key
     * @return Token range information
     */
    TokenRange getTokenRange(ObjectConnection c, MetaObject mc, Object partitionKey) throws MetaDataException;

    /**
     * Repairs inconsistencies between replicas
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key to repair
     * @param consistency Consistency level for the repair
     */
    void repairPartition(ObjectConnection c, MetaObject mc, Object partitionKey, ConsistencyLevel consistency) throws MetaDataException;

    /**
     * Gets the write timestamp of the last update
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param partitionKey Partition key
     * @param clusteringColumns Clustering columns
     * @return Timestamp of the last write
     */
    Instant getLastWriteTime(ObjectConnection c, MetaObject mc, Object partitionKey, Map<String, Object> clusteringColumns) throws MetaDataException;

    /**
     * Sets up change data capture (CDC) for a column family
     * @param c Connection to the column family database
     * @param mc MetaObject describing the column family
     * @param cdcOptions CDC configuration options
     */
    void enableChangeDataCapture(ObjectConnection c, MetaObject mc, Map<String, Object> cdcOptions) throws MetaDataException;
}