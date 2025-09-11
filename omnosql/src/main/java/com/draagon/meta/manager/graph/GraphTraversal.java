/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.graph;

import com.draagon.meta.object.MetaObject;
import static com.draagon.meta.manager.graph.GraphEnums.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Specification for graph traversal operations
 */
public class GraphTraversal {
    
    private TraversalStrategy strategy = TraversalStrategy.BREADTH_FIRST;
    private Integer maxDepth;
    private Integer maxResults;
    private List<String> relationshipTypes;
    private RelationshipDirection direction = RelationshipDirection.OUTGOING;
    private List<MetaObject> nodeTypesToFollow;
    private Predicate<Object> nodeFilter;
    private Predicate<Object> relationshipFilter;
    private Map<String, Object> parameters;
    private boolean includeStartNode = false;
    private boolean returnRelationships = false;
    private String uniquenessStrategy = "NODE_GLOBAL"; // NODE_GLOBAL, NODE_PATH, RELATIONSHIP_GLOBAL, etc.
    
    public GraphTraversal() {}
    
    public GraphTraversal(TraversalStrategy strategy, Integer maxDepth) {
        this.strategy = strategy;
        this.maxDepth = maxDepth;
    }
    
    // Builder pattern methods
    public GraphTraversal strategy(TraversalStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
    
    public GraphTraversal maxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }
    
    public GraphTraversal maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }
    
    public GraphTraversal relationshipTypes(List<String> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
        return this;
    }
    
    public GraphTraversal direction(RelationshipDirection direction) {
        this.direction = direction;
        return this;
    }
    
    public GraphTraversal nodeTypesToFollow(List<MetaObject> nodeTypesToFollow) {
        this.nodeTypesToFollow = nodeTypesToFollow;
        return this;
    }
    
    public GraphTraversal nodeFilter(Predicate<Object> nodeFilter) {
        this.nodeFilter = nodeFilter;
        return this;
    }
    
    public GraphTraversal relationshipFilter(Predicate<Object> relationshipFilter) {
        this.relationshipFilter = relationshipFilter;
        return this;
    }
    
    public GraphTraversal parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    public GraphTraversal includeStartNode(boolean includeStartNode) {
        this.includeStartNode = includeStartNode;
        return this;
    }
    
    public GraphTraversal returnRelationships(boolean returnRelationships) {
        this.returnRelationships = returnRelationships;
        return this;
    }
    
    public GraphTraversal uniquenessStrategy(String uniquenessStrategy) {
        this.uniquenessStrategy = uniquenessStrategy;
        return this;
    }
    
    // Getters
    public TraversalStrategy getStrategy() { return strategy; }
    public Integer getMaxDepth() { return maxDepth; }
    public Integer getMaxResults() { return maxResults; }
    public List<String> getRelationshipTypes() { return relationshipTypes; }
    public RelationshipDirection getDirection() { return direction; }
    public List<MetaObject> getNodeTypesToFollow() { return nodeTypesToFollow; }
    public Predicate<Object> getNodeFilter() { return nodeFilter; }
    public Predicate<Object> getRelationshipFilter() { return relationshipFilter; }
    public Map<String, Object> getParameters() { return parameters; }
    public boolean isIncludeStartNode() { return includeStartNode; }
    public boolean isReturnRelationships() { return returnRelationships; }
    public String getUniquenessStrategy() { return uniquenessStrategy; }
    
    @Override
    public String toString() {
        return String.format("GraphTraversal{strategy=%s, maxDepth=%d, direction=%s, relationshipTypes=%s}", 
                strategy, maxDepth, direction, relationshipTypes);
    }
}