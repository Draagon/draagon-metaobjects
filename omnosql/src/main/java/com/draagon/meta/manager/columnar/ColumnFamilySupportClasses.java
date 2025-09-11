/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.columnar;

import java.util.Map;
import java.util.Objects;

/**
 * Supporting classes for column family operations
 */
public class ColumnFamilySupportClasses {

    /**
     * Consistency levels for column family operations
     */
    public enum ConsistencyLevel {
        ANY,       // Any node (lowest consistency)
        ONE,       // One replica
        TWO,       // Two replicas
        THREE,     // Three replicas
        QUORUM,    // Majority of replicas
        ALL,       // All replicas (highest consistency)
        LOCAL_QUORUM,   // Quorum within the local datacenter
        EACH_QUORUM,    // Quorum in each datacenter
        LOCAL_ONE       // One replica in the local datacenter
    }

    /**
     * Represents a row key (partition key + clustering columns)
     */
    public static class RowKey {
        private final Object partitionKey;
        private final Map<String, Object> clusteringColumns;
        
        public RowKey(Object partitionKey, Map<String, Object> clusteringColumns) {
            this.partitionKey = partitionKey;
            this.clusteringColumns = clusteringColumns;
        }
        
        public Object getPartitionKey() { return partitionKey; }
        public Map<String, Object> getClusteringColumns() { return clusteringColumns; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RowKey)) return false;
            RowKey rowKey = (RowKey) o;
            return Objects.equals(partitionKey, rowKey.partitionKey) &&
                   Objects.equals(clusteringColumns, rowKey.clusteringColumns);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(partitionKey, clusteringColumns);
        }
        
        @Override
        public String toString() {
            return String.format("RowKey{partitionKey=%s, clusteringColumns=%s}", 
                    partitionKey, clusteringColumns);
        }
    }

    /**
     * Represents a column family operation for batch execution
     */
    public static class ColumnFamilyOperation {
        public enum OperationType {
            INSERT, UPDATE, DELETE, UPSERT, CONDITIONAL_UPDATE
        }
        
        private final OperationType type;
        private final String columnFamily;
        private final RowKey rowKey;
        private final Map<String, Object> values;
        private final Map<String, Object> conditions;
        private final Integer ttl;
        
        public ColumnFamilyOperation(OperationType type, String columnFamily, RowKey rowKey, 
                                   Map<String, Object> values, Map<String, Object> conditions, Integer ttl) {
            this.type = type;
            this.columnFamily = columnFamily;
            this.rowKey = rowKey;
            this.values = values;
            this.conditions = conditions;
            this.ttl = ttl;
        }
        
        // Static factory methods
        public static ColumnFamilyOperation insert(String columnFamily, RowKey rowKey, Map<String, Object> values) {
            return new ColumnFamilyOperation(OperationType.INSERT, columnFamily, rowKey, values, null, null);
        }
        
        public static ColumnFamilyOperation update(String columnFamily, RowKey rowKey, Map<String, Object> values) {
            return new ColumnFamilyOperation(OperationType.UPDATE, columnFamily, rowKey, values, null, null);
        }
        
        public static ColumnFamilyOperation delete(String columnFamily, RowKey rowKey) {
            return new ColumnFamilyOperation(OperationType.DELETE, columnFamily, rowKey, null, null, null);
        }
        
        public static ColumnFamilyOperation conditionalUpdate(String columnFamily, RowKey rowKey, 
                                                             Map<String, Object> values, Map<String, Object> conditions) {
            return new ColumnFamilyOperation(OperationType.CONDITIONAL_UPDATE, columnFamily, rowKey, values, conditions, null);
        }
        
        // Getters
        public OperationType getType() { return type; }
        public String getColumnFamily() { return columnFamily; }
        public RowKey getRowKey() { return rowKey; }
        public Map<String, Object> getValues() { return values; }
        public Map<String, Object> getConditions() { return conditions; }
        public Integer getTtl() { return ttl; }
        
        @Override
        public String toString() {
            return String.format("ColumnFamilyOperation{type=%s, columnFamily='%s', rowKey=%s}", 
                    type, columnFamily, rowKey);
        }
    }

    /**
     * Token range information for partition distribution
     */
    public static class TokenRange {
        private final String startToken;
        private final String endToken;
        private final String datacenter;
        private final String rack;
        private final String nodeAddress;
        
        public TokenRange(String startToken, String endToken, String datacenter, String rack, String nodeAddress) {
            this.startToken = startToken;
            this.endToken = endToken;
            this.datacenter = datacenter;
            this.rack = rack;
            this.nodeAddress = nodeAddress;
        }
        
        public String getStartToken() { return startToken; }
        public String getEndToken() { return endToken; }
        public String getDatacenter() { return datacenter; }
        public String getRack() { return rack; }
        public String getNodeAddress() { return nodeAddress; }
        
        @Override
        public String toString() {
            return String.format("TokenRange{start='%s', end='%s', dc='%s', rack='%s', node='%s'}", 
                    startToken, endToken, datacenter, rack, nodeAddress);
        }
    }
}