/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.document.mongodb;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.manager.ObjectConnection;
import com.draagon.meta.manager.QueryOptions;
import com.draagon.meta.manager.document.ObjectManagerDocument;
import com.draagon.meta.manager.exp.Expression;
import com.draagon.meta.manager.exp.ExpressionOperator;
import com.draagon.meta.object.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * MongoDB implementation of ObjectManagerDocument.
 * This is a reference implementation showing how to integrate with MongoDB.
 * Note: This is a conceptual implementation - actual MongoDB integration would require MongoDB Java Driver.
 */
public class ObjectManagerMongoDB extends ObjectManagerDocument {
    
    private static final Logger log = LoggerFactory.getLogger(ObjectManagerMongoDB.class);
    
    private String connectionString;
    private String defaultDatabase;

    public ObjectManagerMongoDB(String connectionString, String defaultDatabase) {
        this.connectionString = connectionString;
        this.defaultDatabase = defaultDatabase;
    }

    @Override
    public ObjectConnection getConnection() throws MetaDataException {
        try {
            // In a real implementation, this would create a MongoDB connection
            return new ObjectConnectionMongoDB(connectionString, defaultDatabase);
        } catch (Exception e) {
            throw new MetaDataException("Failed to create MongoDB connection", e);
        }
    }

    @Override
    public void releaseConnection(ObjectConnection oc) throws MetaDataException {
        // MongoDB connections are typically managed by the driver's connection pool
        try {
            oc.close();
        } catch (Exception e) {
            log.warn("Error releasing MongoDB connection", e);
        }
    }

    @Override
    public boolean isCreateableClass(MetaObject mc) {
        return !isReadOnly(mc);
    }

    @Override
    public boolean isReadableClass(MetaObject mc) {
        return true; // MongoDB collections are always readable
    }

    @Override
    public boolean isUpdateableClass(MetaObject mc) {
        return !isReadOnly(mc);
    }

    @Override
    public boolean isDeleteableClass(MetaObject mc) {
        return !isReadOnly(mc);
    }

    @Override
    public Object getObjectByRef(ObjectConnection c, String refStr) throws MetaDataException {
        // Parse the reference string to extract collection and document ID
        String[] parts = refStr.split(":");
        if (parts.length < 2) {
            throw new MetaDataException("Invalid MongoDB object reference: " + refStr);
        }
        
        String collectionName = parts[0];
        String documentId = parts[1];
        
        // Find the appropriate MetaObject
        // In a real implementation, you'd have a registry of MetaObjects by collection name
        MetaObject mc = findMetaObjectByCollection(collectionName);
        if (mc == null) {
            throw new MetaDataException("No MetaObject found for collection: " + collectionName);
        }
        
        return findDocumentById(c, mc, documentId);
    }

    @Override
    public void loadObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        String idField = getDocumentIdField(mc);
        
        try {
            Object id = mc.getMetaField(idField).getObject(obj);
            if (id == null) {
                throw new MetaDataException("Document ID field '" + idField + "' cannot be null for loading");
            }
            
            Object document = findDocumentById(c, mc, id.toString());
            if (document == null) {
                throw new MetaDataException("Document not found with ID: " + id);
            }
            
            populateObjectFromDocument(mc, obj, document);
            
        } catch (Exception e) {
            throw new MetaDataException("Failed to load MongoDB document", e);
        }
    }

    @Override
    public void createObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, CREATE);
        
        try {
            Object document = convertToDocument(mc, obj);
            insertDocument(c, mc, document);
            
            // Update the object with any generated fields (like _id)
            populateObjectFromDocument(mc, obj, document);
            
        } catch (Exception e) {
            throw new MetaDataException("Failed to create MongoDB document", e);
        }
        
        postPersistence(c, mc, obj, CREATE);
    }

    @Override
    public void updateObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, UPDATE);
        
        try {
            String idField = getDocumentIdField(mc);
            Object id = mc.getMetaField(idField).getObject(obj);
            
            Object document = convertToDocument(mc, obj);
            updateDocumentById(c, mc, id.toString(), document);
            
        } catch (Exception e) {
            throw new MetaDataException("Failed to update MongoDB document", e);
        }
        
        postPersistence(c, mc, obj, UPDATE);
    }

    @Override
    public void deleteObject(ObjectConnection c, Object obj) throws MetaDataException {
        MetaObject mc = getMetaObjectFor(obj);
        
        prePersistence(c, mc, obj, DELETE);
        
        try {
            String idField = getDocumentIdField(mc);
            Object id = mc.getMetaField(idField).getObject(obj);
            
            deleteDocumentById(c, mc, id.toString());
            
        } catch (Exception e) {
            throw new MetaDataException("Failed to delete MongoDB document", e);
        }
        
        postPersistence(c, mc, obj, DELETE);
    }

    @Override
    protected Object convertExpressionToDocumentQuery(Expression expression) throws MetaDataException {
        if (expression == null) {
            return new HashMap<String, Object>(); // Empty query matches all
        }
        
        Map<String, Object> query = new HashMap<>();
        
        if (expression instanceof ExpressionOperator oper) {
            // Handle compound expressions
            Object leftQuery = convertExpressionToDocumentQuery(oper.getExpressionA());
            Object rightQuery = convertExpressionToDocumentQuery(oper.getExpressionB());
            
            if (oper.getOperator() == ExpressionOperator.AND) {
                query.put("$and", Arrays.asList(leftQuery, rightQuery));
            } else if (oper.getOperator() == ExpressionOperator.OR) {
                query.put("$or", Arrays.asList(leftQuery, rightQuery));
            }
        } else {
            // Handle simple expressions
            String field = expression.getField();
            Object value = expression.getValue();
            
            switch (expression.getCondition()) {
                case Expression.EQUAL:
                    query.put(field, value);
                    break;
                case Expression.NOT_EQUAL:
                    query.put(field, Map.of("$ne", value));
                    break;
                case Expression.GREATER:
                    query.put(field, Map.of("$gt", value));
                    break;
                case Expression.EQUAL_GREATER:
                    query.put(field, Map.of("$gte", value));
                    break;
                case Expression.LESSER:
                    query.put(field, Map.of("$lt", value));
                    break;
                case Expression.EQUAL_LESSER:
                    query.put(field, Map.of("$lte", value));
                    break;
                case Expression.CONTAIN:
                    query.put(field, Map.of("$regex", ".*" + value + ".*", "$options", "i"));
                    break;
                // Note: IN operator not available in base Expression class
                // Could be added as an extension if needed
                default:
                    throw new MetaDataException("Unsupported expression condition: " + expression.getCondition());
            }
        }
        
        return query;
    }

    @Override
    protected Collection<?> executeDocumentQuery(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
        try {
            Object mongoQuery = convertExpressionToDocumentQuery(options.getExpression());
            return findDocuments(c, mc, mongoQuery, options);
        } catch (Exception e) {
            throw new MetaDataException("Failed to execute MongoDB query", e);
        }
    }

    @Override
    protected void bulkInsertDocuments(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        if (objects.isEmpty()) return;
        
        try {
            List<Object> documents = new ArrayList<>();
            for (Object obj : objects) {
                documents.add(convertToDocument(mc, obj));
            }
            
            insertDocuments(c, mc, documents);
            
        } catch (Exception e) {
            throw new MetaDataException("Failed to bulk insert MongoDB documents", e);
        }
    }

    @Override
    protected void bulkUpdateDocuments(ObjectConnection c, MetaObject mc, Collection<Object> objects) throws MetaDataException {
        // MongoDB doesn't have a direct bulk update by object, so we'll do individual updates
        // In a real implementation, you could use bulkWrite operations
        for (Object obj : objects) {
            updateObject(c, obj);
        }
    }

    @Override
    protected Object convertToDocument(MetaObject mc, Object obj) throws MetaDataException {
        Map<String, Object> document = new HashMap<>();
        
        for (MetaField field : mc.getMetaFields()) {
            Object value = field.getObject(obj);
            if (value != null) {
                document.put(field.getName(), value);
            }
        }
        
        return document;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object convertFromDocument(MetaObject mc, Object document) throws MetaDataException {
        if (!(document instanceof Map)) {
            throw new MetaDataException("Expected Map document, got: " + document.getClass());
        }
        
        Map<String, Object> docMap = (Map<String, Object>) document;
        Object obj = mc.newInstance();
        
        populateObjectFromDocument(mc, obj, docMap);
        return obj;
    }

    @SuppressWarnings("unchecked")
    private void populateObjectFromDocument(MetaObject mc, Object obj, Object document) throws MetaDataException {
        if (!(document instanceof Map)) {
            throw new MetaDataException("Expected Map document, got: " + document.getClass());
        }
        
        Map<String, Object> docMap = (Map<String, Object>) document;
        
        for (MetaField field : mc.getMetaFields()) {
            Object value = docMap.get(field.getName());
            if (value != null) {
                field.setObject(obj, value);
            }
        }
    }

    // MongoDB-specific implementation methods (these would use actual MongoDB driver)
    private Object findDocumentById(ObjectConnection c, MetaObject mc, String id) throws MetaDataException {
        // In a real implementation: return mongoCollection.find(eq("_id", new ObjectId(id))).first();
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private void insertDocument(ObjectConnection c, MetaObject mc, Object document) throws MetaDataException {
        // In a real implementation: mongoCollection.insertOne((Document) document);
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private void insertDocuments(ObjectConnection c, MetaObject mc, List<Object> documents) throws MetaDataException {
        // In a real implementation: mongoCollection.insertMany((List<Document>) documents);
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private void updateDocumentById(ObjectConnection c, MetaObject mc, String id, Object document) throws MetaDataException {
        // In a real implementation: mongoCollection.replaceOne(eq("_id", new ObjectId(id)), (Document) document);
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private void deleteDocumentById(ObjectConnection c, MetaObject mc, String id) throws MetaDataException {
        // In a real implementation: mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private Collection<?> findDocuments(ObjectConnection c, MetaObject mc, Object query, QueryOptions options) throws MetaDataException {
        // In a real implementation, this would execute the MongoDB query with options
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    private MetaObject findMetaObjectByCollection(String collectionName) {
        // In a real implementation, maintain a registry of MetaObject to collection mappings
        return null;
    }

    @Override
    public boolean collectionExists(ObjectConnection c, MetaObject mc) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public void ensureCollection(ObjectConnection c, MetaObject mc) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public void dropCollection(ObjectConnection c, MetaObject mc) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    // DocumentOperations interface methods
    @Override
    public Collection<?> aggregate(ObjectConnection c, MetaObject mc, List<Map<String, Object>> pipeline) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public void createIndex(ObjectConnection c, MetaObject mc, Map<String, Object> fields, Map<String, Object> options) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public void dropIndex(ObjectConnection c, MetaObject mc, String indexName) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public Collection<?> textSearch(ObjectConnection c, MetaObject mc, String searchText, Map<String, Object> options) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public long updateMany(ObjectConnection c, MetaObject mc, Expression filter, Map<String, Object> update, boolean upsert) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public Object findAndModify(ObjectConnection c, MetaObject mc, Expression filter, Map<String, Object> update, boolean returnNew) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public Collection<?> distinct(ObjectConnection c, MetaObject mc, String field, Expression filter) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public Collection<?> geoNear(ObjectConnection c, MetaObject mc, String locationField, Map<String, Object> geometry, Double maxDistance) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public Map<String, Object> getCollectionStats(ObjectConnection c, MetaObject mc) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public void createCappedCollection(ObjectConnection c, MetaObject mc, long maxSize, Long maxDocuments) throws MetaDataException {
        throw new UnsupportedOperationException("This is a reference implementation - requires actual MongoDB driver");
    }

    @Override
    public int execute(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
        throw new UnsupportedOperationException("MongoDB doesn't support arbitrary query strings - use aggregation pipelines");
    }

    @Override
    public Collection<?> executeQuery(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
        throw new UnsupportedOperationException("MongoDB doesn't support arbitrary query strings - use aggregation pipelines");
    }
}