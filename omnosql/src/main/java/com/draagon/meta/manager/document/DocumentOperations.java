/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.document;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.exp.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface for document database operations beyond standard ObjectManager capabilities.
 * Supports MongoDB, CouchDB, and similar document-oriented databases.
 */
public interface DocumentOperations {

    /**
     * Performs aggregation pipeline operations
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param pipeline List of aggregation stages
     * @return Aggregation results
     */
    Collection<?> aggregate(ObjectConnection c, MetaObject mc, List<Map<String, Object>> pipeline) throws MetaDataException;

    /**
     * Creates an index on specified fields
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param fields Fields to index (field name -> index type, e.g., "name" -> 1 for ascending)
     * @param options Index options (unique, sparse, etc.)
     */
    void createIndex(ObjectConnection c, MetaObject mc, Map<String, Object> fields, Map<String, Object> options) throws MetaDataException;

    /**
     * Drops an index by name
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param indexName Name of the index to drop
     */
    void dropIndex(ObjectConnection c, MetaObject mc, String indexName) throws MetaDataException;

    /**
     * Performs a text search across indexed fields
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param searchText Text to search for
     * @param options Search options (language, case sensitivity, etc.)
     * @return Objects matching the text search
     */
    Collection<?> textSearch(ObjectConnection c, MetaObject mc, String searchText, Map<String, Object> options) throws MetaDataException;

    /**
     * Updates multiple documents matching the expression
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param filter Expression to match documents
     * @param update Update operations to apply
     * @param upsert Whether to create document if not found
     * @return Number of documents updated
     */
    long updateMany(ObjectConnection c, MetaObject mc, Expression filter, Map<String, Object> update, boolean upsert) throws MetaDataException;

    /**
     * Performs atomic find-and-modify operation
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param filter Expression to find the document
     * @param update Update operations to apply
     * @param returnNew Whether to return the updated document
     * @return The original or updated document
     */
    Object findAndModify(ObjectConnection c, MetaObject mc, Expression filter, Map<String, Object> update, boolean returnNew) throws MetaDataException;

    /**
     * Gets distinct values for a field
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param field Field name to get distinct values for
     * @param filter Optional filter expression
     * @return Collection of distinct values
     */
    Collection<?> distinct(ObjectConnection c, MetaObject mc, String field, Expression filter) throws MetaDataException;

    /**
     * Performs geospatial query
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param locationField Field containing geospatial data
     * @param geometry Geometry specification (point, polygon, etc.)
     * @param maxDistance Maximum distance in meters
     * @return Objects within the specified geometry
     */
    Collection<?> geoNear(ObjectConnection c, MetaObject mc, String locationField, Map<String, Object> geometry, Double maxDistance) throws MetaDataException;

    /**
     * Gets collection statistics
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @return Statistics about the collection (size, count, indexes, etc.)
     */
    Map<String, Object> getCollectionStats(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Creates a capped collection with size limits
     * @param c Connection to the document database
     * @param mc MetaObject describing the collection
     * @param maxSize Maximum size in bytes
     * @param maxDocuments Maximum number of documents
     */
    void createCappedCollection(ObjectConnection c, MetaObject mc, long maxSize, Long maxDocuments) throws MetaDataException;
}