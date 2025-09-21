/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.manager.exp.Range;
import com.draagon.meta.manager.exp.SortOrder;
import com.draagon.meta.object.MetaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent query builder for ObjectManager operations providing a clean,
 * readable API for constructing complex database queries.
 * 
 * <h3>Basic Usage Examples:</h3>
 * 
 * <pre>{@code
 * // Simple query by field value
 * Collection<?> users = objectManager.query("User")
 *     .where("status", "active")
 *     .execute();
 *
 * // Complex query with multiple conditions, ordering, and pagination
 * Collection<?> results = objectManager.query("Product")
 *     .where("category", "electronics")
 *     .and("price", 100)
 *     .orderByDesc("createdDate")
 *     .limit(10)
 *     .execute();
 *
 * // Using expressions for advanced conditions
 * Optional<Object> user = objectManager.query("User")
 *     .where(new Expression("age", Expression.GREATER, 18))
 *     .and("isActive", true)
 *     .firstOptional();
 *
 * // Asynchronous query execution
 * CompletableFuture<Collection<?>> futureResults = objectManager.query("Order")
 *     .where("customerId", customerId)
 *     .orderByAsc("orderDate")
 *     .executeAsync();
 *
 * // Count operations
 * long activeUserCount = objectManager.query("User")
 *     .where("status", "active")
 *     .count();
 *
 * // Field selection for performance
 * Collection<?> userNames = objectManager.query("User")
 *     .fields("firstName", "lastName")
 *     .where("department", "engineering")
 *     .execute();
 *
 * // Range/pagination with ordering
 * Collection<?> pagedResults = objectManager.query("Article")
 *     .where("published", true)
 *     .orderByDesc("publishDate")
 *     .limit(20, 40)  // Get records 20-40
 *     .execute();
 * }</pre>
 * 
 * <h3>Event System Integration:</h3>
 * <p>The QueryBuilder automatically integrates with the ObjectManager's event system.
 * All persistence operations (create, update, delete) will fire appropriate events
 * to registered {@link PersistenceEventListener}s, allowing for:</p>
 * <ul>
 *   <li>Audit logging of data changes</li>
 *   <li>Cache invalidation</li>
 *   <li>Data validation</li>
 *   <li>Business rule enforcement</li>
 *   <li>Real-time notifications</li>
 * </ul>
 * 
 * <h3>Performance Considerations:</h3>
 * <ul>
 *   <li>Use field selection to limit data transfer</li>
 *   <li>Implement proper indexing on queried fields</li>
 *   <li>Use pagination for large result sets</li>
 *   <li>Consider async operations for non-blocking execution</li>
 * </ul>
 * 
 * @see ObjectManager#query(MetaObject)
 * @see ObjectManager#query(String)
 * @see PersistenceEventListener
 */
public class QueryBuilder {
    
    private final ObjectManager objectManager;
    private final MetaObject metaObject;
    private Expression expression;
    private SortOrder sortOrder;
    private Range range;
    private boolean distinct = false;
    private List<MetaField> fields = new ArrayList<>();
    
    public QueryBuilder(ObjectManager objectManager, MetaObject metaObject) {
        this.objectManager = objectManager;
        this.metaObject = metaObject;
    }
    
    /**
     * Adds a WHERE condition
     */
    public QueryBuilder where(Expression expression) {
        if (this.expression == null) {
            this.expression = expression;
        } else {
            this.expression = this.expression.and(expression);
        }
        return this;
    }
    
    /**
     * Adds a WHERE condition with field name and value
     */
    public QueryBuilder where(String fieldName, Object value) {
        return where(new Expression(fieldName, value));
    }
    
    /**
     * Adds an AND condition
     */
    public QueryBuilder and(Expression expression) {
        return where(expression);
    }
    
    /**
     * Adds an AND condition with field name and value
     */
    public QueryBuilder and(String fieldName, Object value) {
        return where(fieldName, value);
    }
    
    /**
     * Adds an OR condition
     */
    public QueryBuilder or(Expression expression) {
        if (this.expression == null) {
            this.expression = expression;
        } else {
            this.expression = this.expression.or(expression);
        }
        return this;
    }
    
    /**
     * Adds an OR condition with field name and value
     */
    public QueryBuilder or(String fieldName, Object value) {
        return or(new Expression(fieldName, value));
    }
    
    /**
     * Sets the sort order
     */
    public QueryBuilder orderBy(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }
    
    /**
     * Sets ascending sort by field name
     */
    public QueryBuilder orderByAsc(String fieldName) {
        this.sortOrder = new SortOrder(fieldName, SortOrder.ASC);
        return this;
    }
    
    /**
     * Sets descending sort by field name
     */
    public QueryBuilder orderByDesc(String fieldName) {
        this.sortOrder = new SortOrder(fieldName, SortOrder.DESC);
        return this;
    }
    
    /**
     * Sets the result range (pagination)
     */
    public QueryBuilder limit(int start, int end) {
        this.range = new Range(start, end);
        return this;
    }
    
    /**
     * Sets the result limit
     */
    public QueryBuilder limit(int count) {
        this.range = new Range(1, count);
        return this;
    }
    
    /**
     * Sets distinct results
     */
    public QueryBuilder distinct() {
        this.distinct = true;
        return this;
    }
    
    /**
     * Sets specific fields to retrieve
     */
    public QueryBuilder fields(MetaField... fields) {
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }
    
    /**
     * Sets specific fields to retrieve by name
     */
    public QueryBuilder fields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                MetaField field = metaObject.getMetaField(fieldName);
                this.fields.add(field);
            } catch (Exception e) {
                throw new IllegalArgumentException("Field not found: " + fieldName, e);
            }
        }
        return this;
    }
    
    /**
     * Builds the QueryOptions object
     */
    public QueryOptions build() {
        QueryOptions options = new QueryOptions(expression);
        
        if (sortOrder != null) {
            options.setSortOrder(sortOrder);
        }
        
        if (range != null) {
            options.setRange(range);
        }
        
        options.setDistinct(distinct);
        
        if (!fields.isEmpty()) {
            options.setFields(fields);
        }
        
        return options;
    }
    
    /**
     * Executes the query synchronously
     */
    public Collection<?> execute() throws MetaDataException {
        try (ObjectConnection connection = objectManager.getConnection()) {
            return objectManager.getObjects(connection, metaObject, build());
        }
    }
    
    /**
     * Executes the query asynchronously
     */
    public CompletableFuture<Collection<?>> executeAsync() {
        return objectManager.getObjectsAsync(metaObject, build());
    }
    
    
    /**
     * Executes the query and returns the first result as Optional for null-safe access
     * @return Optional containing the first result, or empty if no results found
     * @throws MetaDataException if query execution fails
     */
    public Optional<Object> firstOptional() throws MetaDataException {
        QueryOptions options = build();
        options.setRange(new Range(1, 1));
        
        try (ObjectConnection connection = objectManager.getConnection()) {
            Collection<?> results = objectManager.getObjects(connection, metaObject, options);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.iterator().next());
        }
    }
    
    /**
     * Executes the query asynchronously and returns the first result as Optional
     * @return CompletableFuture containing Optional of the first result
     */
    public CompletableFuture<Optional<Object>> firstOptionalAsync() {
        QueryOptions options = build();
        options.setRange(new Range(1, 1));
        
        return objectManager.getObjectsAsync(metaObject, options)
            .thenApply(results -> results.isEmpty() ? Optional.empty() : Optional.of(results.iterator().next()));
    }
    
    
    /**
     * Returns the count of matching objects
     */
    public long count() throws MetaDataException {
        try (ObjectConnection connection = objectManager.getConnection()) {
            return objectManager.getObjectsCount(connection, metaObject, expression);
        }
    }
    
    /**
     * Returns the count of matching objects asynchronously
     */
    public CompletableFuture<Long> countAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try (ObjectConnection connection = objectManager.getConnection()) {
                return objectManager.getObjectsCount(connection, metaObject, expression);
            } catch (Exception e) {
                throw new RuntimeException("Error counting objects asynchronously", e);
            }
        });
    }
}