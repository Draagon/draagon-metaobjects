/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.graph;

/**
 * Enums and supporting classes for graph operations
 */
public class GraphEnums {

    /**
     * Direction of relationships in graph traversal
     */
    public enum RelationshipDirection {
        INCOMING,  // Follow relationships coming into the node
        OUTGOING,  // Follow relationships going out from the node
        BOTH       // Follow relationships in both directions
    }

    /**
     * Types of graph indexes
     */
    public enum IndexType {
        BTREE,     // Standard B-tree index for exact matches and range queries
        FULLTEXT,  // Full-text search index
        SPATIAL,   // Geospatial index
        COMPOSITE  // Composite index on multiple properties
    }

    /**
     * Graph clustering/community detection algorithms
     */
    public enum ClusteringAlgorithm {
        LOUVAIN,           // Louvain modularity optimization
        LABEL_PROPAGATION, // Label propagation algorithm
        WEAKLY_CONNECTED,  // Weakly connected components
        STRONGLY_CONNECTED, // Strongly connected components
        TRIANGLE_COUNT     // Triangle counting for clustering coefficient
    }

    /**
     * Graph centrality algorithms
     */
    public enum CentralityAlgorithm {
        BETWEENNESS,  // Betweenness centrality
        CLOSENESS,    // Closeness centrality
        PAGERANK,     // PageRank algorithm
        DEGREE,       // Degree centrality
        EIGENVECTOR,  // Eigenvector centrality
        HARMONIC      // Harmonic centrality
    }

    /**
     * Graph traversal strategies
     */
    public enum TraversalStrategy {
        BREADTH_FIRST, // Breadth-first search
        DEPTH_FIRST,   // Depth-first search
        DIJKSTRA,      // Dijkstra's shortest path
        A_STAR         // A* pathfinding algorithm
    }
}