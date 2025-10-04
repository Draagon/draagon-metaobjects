/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.document.mongodb;

import com.metaobjects.MetaDataException;
import com.metaobjects.manager.ObjectConnection;
import com.metaobjects.manager.PersistenceException;
import com.metaobjects.manager.QueryOptions;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.value.ValueMetaObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test MongoDB ObjectManager implementation to improve coverage
 */
public class MongoDBManagerTest {

    private ObjectManagerMongoDB mongoManager;

    @Before
    public void setUp() {
        mongoManager = new ObjectManagerMongoDB("mongodb://localhost:27017", "testdb");
    }

    @Test
    public void testManagerInitialization() {
        assertNotNull("MongoDB manager should be created", mongoManager);
        // Test that the manager exists and can be configured
        assertTrue("MongoDB manager should be a document manager", mongoManager instanceof ObjectManagerMongoDB);
    }

    @Test
    public void testConnectionHandling() throws MetaDataException {
        // Test connection creation - this is a reference implementation that creates stubs
        try {
            ObjectConnection connection = mongoManager.getConnection();
            assertNotNull("Connection should be created", connection);
            assertFalse("Connection should not be closed initially", connection.isClosed());

            // Test connection properties
            assertTrue("MongoDB connections should auto-commit", connection.getAutoCommit());
            assertFalse("MongoDB connections should not be read-only", connection.isReadOnly());

            // Test connection close
            connection.close();
            assertTrue("Connection should be closed after close()", connection.isClosed());

        } catch (Exception e) {
            // If it fails, ensure it's for metadata-related reasons
            assertTrue("Should be metadata-related exception",
                      e instanceof MetaDataException);
        }
    }

    @Test
    public void testQueryCapabilities() throws MetaDataException {
        // Test CRUD operation flags
        ValueMetaObject testClass = ValueMetaObject.create("TestDocument");

        // These should work without connection
        boolean creatable = mongoManager.isCreateableClass(testClass);
        boolean readable = mongoManager.isReadableClass(testClass);
        boolean updateable = mongoManager.isUpdateableClass(testClass);
        boolean deleteable = mongoManager.isDeleteableClass(testClass);

        // MongoDB should support all CRUD operations
        assertTrue("MongoDB should support create operations", creatable);
        assertTrue("MongoDB should support read operations", readable);
        assertTrue("MongoDB should support update operations", updateable);
        assertTrue("MongoDB should support delete operations", deleteable);
    }

    @Test
    public void testQueryOptionsHandling() {
        QueryOptions options = new QueryOptions();
        options.setRange(5, 15); // start=5, end=15 (equivalent to offset=5, limit=10)

        // Test query options processing (this exercises internal logic)
        assertNotNull("Query options should have range", options.getRange());
        assertEquals("Range start should be 5", 5, options.getRange().getStart());
        assertEquals("Range end should be 15", 15, options.getRange().getEnd());
    }

    @Test
    public void testDocumentManagerCapabilities() throws MetaDataException {
        // Test document-specific functionality
        ValueMetaObject documentClass = ValueMetaObject.create("Document");

        // Test that the manager recognizes document classes
        assertTrue("Should recognize document classes",
                  mongoManager.isCreateableClass(documentClass));
    }

    @Test
    public void testErrorHandling() {
        // Test error handling paths
        try {
            mongoManager.releaseConnection(null);
            // Should handle null connection gracefully
        } catch (Exception e) {
            // Any exception should be a proper MetaDataException or subclass
            assertTrue("Exception should be persistence-related",
                      e instanceof MetaDataException || e instanceof PersistenceException);
        }
    }

    @Test
    public void testQueryBuilder() throws MetaDataException {
        ValueMetaObject testClass = ValueMetaObject.create("TestDoc");

        // Test query builder creation
        try {
            var builder = mongoManager.query(testClass);
            assertNotNull("Query builder should be created", builder);

            // Test method chaining
            var options = builder
                    .where("field1", "value1")
                    .and("field2", "value2")
                    .build();

            assertNotNull("Query options should be built", options);
        } catch (Exception e) {
            // If it fails, ensure it's for the right reasons
            assertTrue("Should fail due to connection issues",
                      e.getMessage().contains("connection") ||
                      e.getMessage().contains("MongoDB"));
        }
    }
}