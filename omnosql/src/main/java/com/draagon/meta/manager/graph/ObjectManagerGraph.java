/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.graph;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.ObjectManager;
import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.draagon.meta.manager.graph.GraphEnums.*;
import static com.draagon.meta.manager.graph.GraphSupportClasses.*;

/**
 * Abstract ObjectManager extension for graph databases.
 * Provides graph-specific functionality while maintaining ObjectManager compatibility.
 */
public abstract class ObjectManagerGraph extends ObjectManager implements GraphOperations {
    
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerGraph.class);

    // Graph-specific MetaObject attributes
    public static final String ATTR_NODE_LABEL = "nodeLabel";
    public static final String ATTR_RELATIONSHIP_TYPE = "relationshipType";
    public static final String ATTR_NODE_ID_PROPERTY = "nodeIdProperty";
    public static final String ATTR_INDEX_PROPERTIES = "indexProperties";
    public static final String ATTR_UNIQUE_PROPERTIES = "uniqueProperties";

    /**
     * Gets the node label for a MetaObject
     */
    protected String getNodeLabel(MetaObject mc) {
        String label = getPersistenceAttribute(mc, ATTR_NODE_LABEL);
        return label != null ? label : mc.getName();
    }

    /**
     * Gets the node ID property name
     */
    protected String getNodeIdProperty(MetaObject mc) {
        String idProperty = getPersistenceAttribute(mc, ATTR_NODE_ID_PROPERTY);
        return idProperty != null ? idProperty : "id";
    }

    /**
     * Enhanced bulk create operations for graph databases
     */
    @Override
    protected void createObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (objects.isEmpty()) {
            return;
        }

        String nodeLabel = getNodeLabel(mc);
        log.debug("Creating {} nodes with label: {}", objects.size(), nodeLabel);

        try {
            // Graph databases can efficiently create nodes in batch
            List<GraphOperation> operations = new java.util.ArrayList<>();
            
            for (Object obj : objects) {
                Map<String, Object> properties = convertObjectToNodeProperties(mc, obj);
                operations.add(GraphOperation.createNode(obj, properties));
            }
            
            // Execute batch operations
            executeTransaction(c, operations);
            
            // Fire post-creation events
            for (Object obj : objects) {
                postPersistence(c, mc, obj, CREATE);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk node creation", e);
            throw new MetaDataException("Failed to bulk create nodes with label: " + nodeLabel, e);
        }
    }

    /**
     * Graph-specific query implementation
     */
    @Override
    public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
        String nodeLabel = getNodeLabel(mc);
        log.debug("Querying nodes with label: {} and options: {}", nodeLabel, options);
        
        try {
            return executeNodeQuery(c, mc, options);
        } catch (Exception e) {
            log.error("Error querying nodes with label: {}", nodeLabel, e);
            throw new MetaDataException("Failed to query nodes with label: " + nodeLabel, e);
        }
    }

    /**
     * Override createObject to create a graph node
     */
    @Override
    public void createObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, CREATE);
        
        String nodeLabel = getNodeLabel(mc);
        Map<String, Object> properties = convertObjectToNodeProperties(mc, obj);
        
        Object createdNode = createGraphNode(c, mc, nodeLabel, properties);
        populateObjectFromNode(mc, obj, createdNode);
        
        postPersistence(c, mc, obj, CREATE);
    }

    /**
     * Override loadObject to load from graph node
     */
    @Override
    public void loadObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        String nodeLabel = getNodeLabel(mc);
        String idProperty = getNodeIdProperty(mc);
        
        Object nodeId = mc.getMetaField(idProperty).getObject(obj);
        if (nodeId == null) {
            throw new MetaDataException("Node ID property '" + idProperty + "' cannot be null for loading");
        }
        
        Object node = findNodeByIdAndLabel(c, nodeId, nodeLabel);
        if (node == null) {
            throw new MetaDataException("Node not found with ID: " + nodeId + " and label: " + nodeLabel);
        }
        
        populateObjectFromNode(mc, obj, node);
    }

    /**
     * Override updateObject to update graph node
     */
    @Override
    public void updateObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, UPDATE);
        
        String nodeLabel = getNodeLabel(mc);
        String idProperty = getNodeIdProperty(mc);
        Object nodeId = mc.getMetaField(idProperty).getObject(obj);
        
        Map<String, Object> properties = convertObjectToNodeProperties(mc, obj);
        updateGraphNode(c, mc, nodeId, nodeLabel, properties);
        
        postPersistence(c, mc, obj, UPDATE);
    }

    /**
     * Override deleteObject to delete graph node
     */
    @Override
    public void deleteObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, DELETE);
        
        String nodeLabel = getNodeLabel(mc);
        String idProperty = getNodeIdProperty(mc);
        Object nodeId = mc.getMetaField(idProperty).getObject(obj);
        
        deleteGraphNode(c, mc, nodeId, nodeLabel);
        
        postPersistence(c, mc, obj, DELETE);
    }

    // Abstract methods for graph operations
    protected abstract Object createGraphNode(ObjectConnection c, MetaObject mc, String label, Map<String, Object> properties) throws MetaDataException;
    protected abstract Object findNodeByIdAndLabel(ObjectConnection c, Object nodeId, String label) throws MetaDataException;
    protected abstract void updateGraphNode(ObjectConnection c, MetaObject mc, Object nodeId, String label, Map<String, Object> properties) throws MetaDataException;
    protected abstract void deleteGraphNode(ObjectConnection c, MetaObject mc, Object nodeId, String label) throws MetaDataException;
    
    protected abstract Collection<?> executeNodeQuery(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException;
    protected abstract Map<String, Object> convertObjectToNodeProperties(MetaObject mc, Object obj) throws MetaDataException;
    protected abstract void populateObjectFromNode(MetaObject mc, Object obj, Object node) throws MetaDataException;

    /**
     * Convenience method to create a relationship between two objects
     */
    public Object createRelationshipBetweenObjects(ObjectConnection c, Object fromObj, Object toObj, 
                                                  String relationshipType, Map<String, Object> properties) throws MetaDataException {
        Object fromNode = findNodeForObject(c, fromObj);
        Object toNode = findNodeForObject(c, toObj);
        
        if (fromNode == null || toNode == null) {
            throw new MetaDataException("One or both nodes not found for relationship creation");
        }
        
        return createRelationship(c, fromNode, toNode, relationshipType, properties);
    }

    /**
     * Convenience method to find connected objects
     */
    public Collection<?> getConnectedObjects(ObjectConnection c, Object obj, String relationshipType, 
                                           RelationshipDirection direction, MetaObject targetType) throws MetaDataException {
        Object node = findNodeForObject(c, obj);
        if (node == null) {
            throw new MetaDataException("Node not found for object: " + obj);
        }
        
        Collection<?> connectedNodes = getConnectedNodes(c, node, relationshipType, direction, targetType);
        return convertNodesToObjects(connectedNodes, targetType);
    }

    /**
     * Convenience method for simple graph traversal
     */
    public Collection<?> traverseFromObject(ObjectConnection c, Object startObj, String relationshipType, 
                                          int maxDepth, RelationshipDirection direction) throws MetaDataException {
        Object startNode = findNodeForObject(c, startObj);
        if (startNode == null) {
            throw new MetaDataException("Start node not found for object: " + startObj);
        }
        
        GraphTraversal traversal = new GraphTraversal()
                .strategy(TraversalStrategy.BREADTH_FIRST)
                .maxDepth(maxDepth)
                .relationshipTypes(List.of(relationshipType))
                .direction(direction);
                
        Collection<?> nodes = traverse(c, startNode, traversal);
        return convertNodesToObjects(nodes, getMetaObjectFor(startObj));
    }

    /**
     * Convenience method for shortest path between objects
     */
    public GraphPath findShortestPathBetweenObjects(ObjectConnection c, Object startObj, Object endObj, 
                                                   List<String> relationshipTypes, Integer maxDepth) throws MetaDataException {
        Object startNode = findNodeForObject(c, startObj);
        Object endNode = findNodeForObject(c, endObj);
        
        if (startNode == null || endNode == null) {
            throw new MetaDataException("One or both nodes not found for path finding");
        }
        
        return findShortestPath(c, startNode, endNode, relationshipTypes, maxDepth);
    }

    // Helper methods
    protected abstract Object findNodeForObject(ObjectConnection c, Object obj) throws MetaDataException;
    protected abstract Collection<?> convertNodesToObjects(Collection<?> nodes, MetaObject mc) throws MetaDataException;

    /**
     * Creates necessary indexes for efficient querying
     */
    public void ensureIndexes(ObjectConnection c, MetaObject mc) throws MetaDataException {
        String indexPropsStr = getPersistenceAttribute(mc, ATTR_INDEX_PROPERTIES);
        if (indexPropsStr != null) {
            List<String> properties = List.of(indexPropsStr.split(","));
            createNodeIndex(c, mc, properties, IndexType.BTREE);
        }
        
        String uniquePropsStr = getPersistenceAttribute(mc, ATTR_UNIQUE_PROPERTIES);
        if (uniquePropsStr != null) {
            List<String> properties = List.of(uniquePropsStr.split(","));
            createNodeIndex(c, mc, properties, IndexType.BTREE);
            // Also create uniqueness constraint if supported
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Graph Database]";
    }
}