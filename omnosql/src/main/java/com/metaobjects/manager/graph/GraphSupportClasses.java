/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.graph;

import java.util.List;
import java.util.Map;

/**
 * Supporting classes for graph operations
 */
public class GraphSupportClasses {

    /**
     * Represents a path through the graph
     */
    public static class GraphPath {
        private final List<Object> nodes;
        private final List<Object> relationships;
        private final double weight;
        private final int length;
        
        public GraphPath(List<Object> nodes, List<Object> relationships, double weight) {
            this.nodes = nodes;
            this.relationships = relationships;
            this.weight = weight;
            this.length = nodes.size() - 1; // Length is number of relationships
        }
        
        public List<Object> getNodes() { return nodes; }
        public List<Object> getRelationships() { return relationships; }
        public double getWeight() { return weight; }
        public int getLength() { return length; }
        public Object getStartNode() { return nodes.isEmpty() ? null : nodes.get(0); }
        public Object getEndNode() { return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1); }
        
        @Override
        public String toString() {
            return String.format("GraphPath{length=%d, weight=%.2f, nodes=%d, relationships=%d}", 
                    length, weight, nodes.size(), relationships.size());
        }
    }

    /**
     * Statistics about a graph database
     */
    public static class GraphStatistics {
        private final long nodeCount;
        private final long relationshipCount;
        private final Map<String, Long> nodeTypeCount;
        private final Map<String, Long> relationshipTypeCount;
        private final long indexCount;
        private final long constraintCount;
        private final Map<String, Object> additionalStats;
        
        public GraphStatistics(long nodeCount, long relationshipCount, 
                Map<String, Long> nodeTypeCount, Map<String, Long> relationshipTypeCount,
                long indexCount, long constraintCount, Map<String, Object> additionalStats) {
            this.nodeCount = nodeCount;
            this.relationshipCount = relationshipCount;
            this.nodeTypeCount = nodeTypeCount;
            this.relationshipTypeCount = relationshipTypeCount;
            this.indexCount = indexCount;
            this.constraintCount = constraintCount;
            this.additionalStats = additionalStats;
        }
        
        public long getNodeCount() { return nodeCount; }
        public long getRelationshipCount() { return relationshipCount; }
        public Map<String, Long> getNodeTypeCount() { return nodeTypeCount; }
        public Map<String, Long> getRelationshipTypeCount() { return relationshipTypeCount; }
        public long getIndexCount() { return indexCount; }
        public long getConstraintCount() { return constraintCount; }
        public Map<String, Object> getAdditionalStats() { return additionalStats; }
        
        @Override
        public String toString() {
            return String.format("GraphStatistics{nodes=%d, relationships=%d, indexes=%d, constraints=%d}", 
                    nodeCount, relationshipCount, indexCount, constraintCount);
        }
    }

    /**
     * Represents a graph operation for batch execution
     */
    public static class GraphOperation {
        public enum OperationType {
            CREATE_NODE, UPDATE_NODE, DELETE_NODE,
            CREATE_RELATIONSHIP, UPDATE_RELATIONSHIP, DELETE_RELATIONSHIP,
            EXECUTE_QUERY, CREATE_INDEX, DROP_INDEX
        }
        
        private final OperationType type;
        private final Object target;
        private final Map<String, Object> parameters;
        
        public GraphOperation(OperationType type, Object target, Map<String, Object> parameters) {
            this.type = type;
            this.target = target;
            this.parameters = parameters;
        }
        
        public OperationType getType() { return type; }
        public Object getTarget() { return target; }
        public Map<String, Object> getParameters() { return parameters; }
        
        // Static factory methods
        public static GraphOperation createNode(Object node, Map<String, Object> properties) {
            return new GraphOperation(OperationType.CREATE_NODE, node, properties);
        }
        
        public static GraphOperation createRelationship(Object relationship, Map<String, Object> properties) {
            return new GraphOperation(OperationType.CREATE_RELATIONSHIP, relationship, properties);
        }
        
        public static GraphOperation executeQuery(String query, Map<String, Object> parameters) {
            return new GraphOperation(OperationType.EXECUTE_QUERY, query, parameters);
        }
        
        @Override
        public String toString() {
            return String.format("GraphOperation{type=%s, target=%s}", type, target);
        }
    }
}