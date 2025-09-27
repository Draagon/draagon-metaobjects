/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.document;

import com.metaobjects.MetaDataException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.ObjectManager;
import com.metaobjects.manager.QueryOptions;
import com.metaobjects.manager.exp.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract ObjectManager extension for document databases.
 * Provides document-specific functionality while maintaining ObjectManager compatibility.
 */
public abstract class ObjectManagerDocument extends ObjectManager implements DocumentOperations {
    
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerDocument.class);

    // Document-specific MetaObject attributes
    public static final String ATTR_COLLECTION_NAME = "collectionName";
    public static final String ATTR_DATABASE_NAME = "databaseName"; 
    public static final String ATTR_DOCUMENT_ID_FIELD = "documentIdField";
    public static final String ATTR_INDEX_DEFINITION = "indexDefinition";
    public static final String ATTR_SHARD_KEY = "shardKey";
    public static final String ATTR_TEXT_SEARCH_FIELDS = "textSearchFields";

    /**
     * Gets the collection name for a MetaObject
     */
    protected String getCollectionName(MetaObject mc) {
        String collectionName = getPersistenceAttribute(mc, ATTR_COLLECTION_NAME);
        return collectionName != null ? collectionName : mc.getName();
    }

    /**
     * Gets the database name for a MetaObject
     */
    protected String getDatabaseName(MetaObject mc) {
        return getPersistenceAttribute(mc, ATTR_DATABASE_NAME);
    }

    /**
     * Gets the document ID field name
     */
    protected String getDocumentIdField(MetaObject mc) {
        String idField = getPersistenceAttribute(mc, ATTR_DOCUMENT_ID_FIELD);
        return idField != null ? idField : "_id";
    }

    /**
     * Enhanced bulk create operations leveraging document database capabilities
     */
    @Override
    protected void createObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (objects.isEmpty()) {
            return;
        }

        log.debug("Creating {} objects in document collection: {}", objects.size(), getCollectionName(mc));

        try {
            // Document databases typically support efficient bulk inserts
            bulkInsertDocuments(c, mc, objects);
            
            // Fire post-creation events for each object
            for (Object obj : objects) {
                postPersistence(c, mc, obj, CREATE);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk document creation", e);
            throw new MetaDataException("Failed to bulk create documents in collection: " + getCollectionName(mc), e);
        }
    }

    /**
     * Enhanced bulk update operations
     */
    @Override
    protected void updateObjectsBulk(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (objects.isEmpty()) {
            return;
        }

        log.debug("Updating {} objects in document collection: {}", objects.size(), getCollectionName(mc));

        try {
            // Use document database bulk update capabilities
            bulkUpdateDocuments(c, mc, objects);
            
            // Fire post-update events for each object
            for (Object obj : objects) {
                postPersistence(c, mc, obj, UPDATE);
            }
            
        } catch (Exception e) {
            log.error("Error during bulk document update", e);
            throw new MetaDataException("Failed to bulk update documents in collection: " + getCollectionName(mc), e);
        }
    }

    /**
     * Override to provide document-specific query implementation
     */
    @Override
    public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
        String collectionName = getCollectionName(mc);
        log.debug("Querying documents from collection: {} with options: {}", collectionName, options);
        
        try {
            return executeDocumentQuery(c, mc, options);
        } catch (Exception e) {
            log.error("Error querying documents from collection: {}", collectionName, e);
            throw new MetaDataException("Failed to query documents from collection: " + collectionName, e);
        }
    }

    /**
     * Helper method to convert ObjectManager expressions to document database queries
     */
    protected abstract Object convertExpressionToDocumentQuery(Expression expression) throws MetaDataException;

    /**
     * Execute a document query with the given options
     */
    protected abstract Collection<?> executeDocumentQuery(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException;

    /**
     * Bulk insert documents into the database
     */
    protected abstract void bulkInsertDocuments(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException;

    /**
     * Bulk update documents in the database
     */
    protected abstract void bulkUpdateDocuments(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException;

    /**
     * Convert an object to a document format suitable for storage
     */
    protected abstract Object convertToDocument(MetaObject mc, Object obj) throws MetaDataException;

    /**
     * Convert a document from storage format to an object
     */
    protected abstract Object convertFromDocument(MetaObject mc, Object document) throws MetaDataException;

    /**
     * Convenience method for simple text search
     */
    public Collection<?> searchText(ObjectConnection c, MetaObject mc, String searchText) throws MetaDataException {
        return textSearch(c, mc, searchText, Map.of());
    }

    /**
     * Convenience method for simple aggregation
     */
    public Collection<?> aggregateSimple(ObjectConnection c, MetaObject mc, String operation, String field) throws MetaDataException {
        List<Map<String, Object>> pipeline = List.of(
            Map.of("$group", Map.of(
                "_id", null,
                "result", Map.of("$" + operation, "$" + field)
            ))
        );
        return aggregate(c, mc, pipeline);
    }

    /**
     * Gets statistics about the collection
     */
    public Map<String, Object> getCollectionStatistics(ObjectConnection c, MetaObject mc) throws MetaDataException {
        return getCollectionStats(c, mc);
    }

    /**
     * Checks if the collection exists
     */
    public abstract boolean collectionExists(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Creates the collection if it doesn't exist
     */
    public abstract void ensureCollection(ObjectConnection c, MetaObject mc) throws MetaDataException;

    /**
     * Drops the collection
     */
    public abstract void dropCollection(ObjectConnection c, MetaObject mc) throws MetaDataException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Document Database]";
    }
}