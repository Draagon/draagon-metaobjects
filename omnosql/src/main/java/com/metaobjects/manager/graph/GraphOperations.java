/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.graph;

import com.metaobjects.MetaDataException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.exp.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.metaobjects.manager.graph.GraphEnums.*;
import static com.metaobjects.manager.graph.GraphSupportClasses.*;

/**
 * Interface for graph database operations beyond standard ObjectManager capabilities.
 * Supports Neo4j, Amazon Neptune, and similar graph databases.
 */
public interface GraphOperations {

    /**
     * Creates a relationship between two nodes
     * @param c Connection to the graph database
     * @param fromNode Source node object
     * @param toNode Target node object
     * @param relationshipType Type of relationship
     * @param properties Properties to set on the relationship
     * @return The created relationship
     */
    Object createRelationship(ObjectConnection c, Object fromNode, Object toNode, String relationshipType, Map<String, Object> properties) throws MetaDataException;

    /**
     * Deletes a relationship
     * @param c Connection to the graph database
     * @param relationship Relationship object to delete
     */
    void deleteRelationship(ObjectConnection c, Object relationship) throws MetaDataException;

    /**
     * Finds relationships connected to a node
     * @param c Connection to the graph database
     * @param node Node to find relationships for
     * @param relationshipType Optional relationship type filter
     * @param direction Direction of relationships (INCOMING, OUTGOING, BOTH)
     * @return Collection of relationships
     */
    Collection<?> getRelationships(ObjectConnection c, Object node, String relationshipType, RelationshipDirection direction) throws MetaDataException;

    /**
     * Finds nodes connected to a node through relationships
     * @param c Connection to the graph database
     * @param node Source node
     * @param relationshipType Optional relationship type filter
     * @param direction Direction of relationships
     * @param targetNodeType Optional target node type filter
     * @return Collection of connected nodes
     */
    Collection<?> getConnectedNodes(ObjectConnection c, Object node, String relationshipType, RelationshipDirection direction, MetaObject targetNodeType) throws MetaDataException;

    /**
     * Performs a graph traversal
     * @param c Connection to the graph database
     * @param startNode Starting node for traversal
     * @param traversalSpec Traversal specification (depth, breadth-first, relationships to follow, etc.)
     * @return Collection of nodes found during traversal
     */
    Collection<?> traverse(ObjectConnection c, Object startNode, GraphTraversal traversalSpec) throws MetaDataException;

    /**
     * Finds the shortest path between two nodes
     * @param c Connection to the graph database
     * @param startNode Starting node
     * @param endNode Ending node
     * @param relationshipTypes Optional relationship types to traverse
     * @param maxDepth Maximum depth to search
     * @return Path object containing nodes and relationships, or null if no path found
     */
    GraphPath findShortestPath(ObjectConnection c, Object startNode, Object endNode, List<String> relationshipTypes, Integer maxDepth) throws MetaDataException;

    /**
     * Finds all paths between two nodes
     * @param c Connection to the graph database
     * @param startNode Starting node
     * @param endNode Ending node
     * @param relationshipTypes Optional relationship types to traverse
     * @param maxDepth Maximum depth to search
     * @return Collection of paths
     */
    Collection<GraphPath> findAllPaths(ObjectConnection c, Object startNode, Object endNode, List<String> relationshipTypes, Integer maxDepth) throws MetaDataException;

    /**
     * Executes a graph query language query (e.g., Cypher for Neo4j, Gremlin for Neptune)
     * @param c Connection to the graph database
     * @param query Query string
     * @param parameters Query parameters
     * @return Query results
     */
    Collection<?> executeGraphQuery(ObjectConnection c, String query, Map<String, Object> parameters) throws MetaDataException;

    /**
     * Gets nodes by label and properties
     * @param c Connection to the graph database
     * @param nodeType MetaObject representing the node type/label
     * @param properties Properties to match
     * @return Collection of matching nodes
     */
    Collection<?> getNodesByLabelAndProperties(ObjectConnection c, MetaObject nodeType, Map<String, Object> properties) throws MetaDataException;

    /**
     * Creates an index on node properties
     * @param c Connection to the graph database
     * @param nodeType MetaObject representing the node type/label
     * @param properties Properties to index
     * @param indexType Type of index (BTREE, FULLTEXT, etc.)
     */
    void createNodeIndex(ObjectConnection c, MetaObject nodeType, List<String> properties, IndexType indexType) throws MetaDataException;

    /**
     * Creates an index on relationship properties
     * @param c Connection to the graph database
     * @param relationshipType Relationship type name
     * @param properties Properties to index
     * @param indexType Type of index
     */
    void createRelationshipIndex(ObjectConnection c, String relationshipType, List<String> properties, IndexType indexType) throws MetaDataException;

    /**
     * Gets degree (number of relationships) for a node
     * @param c Connection to the graph database
     * @param node Node to get degree for
     * @param relationshipType Optional relationship type filter
     * @param direction Direction of relationships
     * @return Number of relationships
     */
    long getDegree(ObjectConnection c, Object node, String relationshipType, RelationshipDirection direction) throws MetaDataException;

    /**
     * Performs clustering/community detection
     * @param c Connection to the graph database
     * @param algorithm Clustering algorithm to use
     * @param parameters Algorithm-specific parameters
     * @return Map of node to cluster ID
     */
    Map<Object, String> detectCommunities(ObjectConnection c, ClusteringAlgorithm algorithm, Map<String, Object> parameters) throws MetaDataException;

    /**
     * Calculates centrality measures for nodes
     * @param c Connection to the graph database
     * @param algorithm Centrality algorithm (BETWEENNESS, CLOSENESS, PAGERANK, etc.)
     * @param parameters Algorithm-specific parameters
     * @return Map of node to centrality score
     */
    Map<Object, Double> calculateCentrality(ObjectConnection c, CentralityAlgorithm algorithm, Map<String, Object> parameters) throws MetaDataException;

    /**
     * Gets database statistics
     * @param c Connection to the graph database
     * @return Statistics about the graph (node count, relationship count, etc.)
     */
    GraphStatistics getGraphStatistics(ObjectConnection c) throws MetaDataException;

    /**
     * Executes multiple operations in a transaction
     * @param c Connection to the graph database
     * @param operations Operations to execute atomically
     * @return Results of the operations
     */
    List<Object> executeTransaction(ObjectConnection c, List<GraphOperation> operations) throws MetaDataException;
}