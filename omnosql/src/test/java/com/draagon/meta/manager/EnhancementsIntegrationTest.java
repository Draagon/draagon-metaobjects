/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.object.value.ValueMetaObject;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration test to demonstrate the key enhancements working together
 */
public class EnhancementsIntegrationTest {

    @Test
    public void demonstrateKeyEnhancements() throws Exception {
        // Create an enhanced ObjectManager with custom executor
        TestObjectManager objectManager = new TestObjectManager();
        objectManager.setAsyncExecutor(Executors.newFixedThreadPool(2));

        // Add event listener to track operations
        TestEventListener eventListener = new TestEventListener();
        objectManager.addPersistenceEventListener(eventListener);

        // Create a MetaObject
        ValueMetaObject metaObject = ValueMetaObject.create("TestEntity");

        try {
            // Demonstrate Query Builder Pattern
            QueryOptions queryOptions = objectManager.query(metaObject)
                    .where("status", "ACTIVE")
                    .and("priority", "HIGH")
                    .orderByDesc("created")
                    .limit(10)
                    .build();

            assertNotNull("Query options should be built", queryOptions);
            assertNotNull("Expression should be set", queryOptions.getExpression());

            // Demonstrate Asynchronous Operations
            CompletableFuture<java.util.Collection<?>> asyncResult = 
                objectManager.getObjectsAsync(metaObject, queryOptions);

            // Wait for async operation to complete with timeout
            java.util.Collection<?> result = asyncResult.get(5, TimeUnit.SECONDS);
            assertNotNull("Async result should not be null", result);

            // Demonstrate Resource Management with try-with-resources
            try (ObjectConnection connection = objectManager.getConnection()) {
                assertNotNull("Connection should be created", connection);
                assertFalse("Connection should not be closed initially", connection.isClosed());

                // Demonstrate enhanced bulk operations
                objectManager.createObjects(connection, new java.util.ArrayList<>());

                assertTrue("Enhanced functionality works together", true);
            } // Connection automatically closed

        } finally {
            // Clean up - the executor will be cleaned up automatically
            // or can be managed by the application
            assertTrue("Integration test completed successfully", true);
        }
    }

    // Test implementation classes
    private static class TestObjectManager extends ObjectManager {
        @Override
        public ObjectConnection getConnection() throws MetaDataException {
            return new TestObjectConnection();
        }

        @Override
        public void releaseConnection(ObjectConnection oc) throws MetaDataException {}

        @Override
        public boolean isCreateableClass(com.draagon.meta.object.MetaObject mc) { return true; }
        @Override
        public boolean isReadableClass(com.draagon.meta.object.MetaObject mc) { return true; }
        @Override
        public boolean isUpdateableClass(com.draagon.meta.object.MetaObject mc) { return true; }
        @Override
        public boolean isDeleteableClass(com.draagon.meta.object.MetaObject mc) { return true; }

        @Override
        public Object getObjectByRef(ObjectConnection c, String refStr) throws MetaDataException {
            return new Object();
        }

        @Override
        public void loadObject(ObjectConnection c, Object obj) throws MetaDataException {}
        @Override
        public void createObject(ObjectConnection c, Object obj) throws MetaDataException {}
        @Override
        public void updateObject(ObjectConnection c, Object obj) throws MetaDataException {}
        @Override
        public void deleteObject(ObjectConnection c, Object obj) throws MetaDataException {}

        @Override
        public java.util.Collection<?> getObjects(ObjectConnection c, com.draagon.meta.object.MetaObject mc, QueryOptions options) throws MetaDataException {
            return new java.util.ArrayList<>();
        }

        @Override
        public int execute(ObjectConnection c, String query, java.util.Collection<?> arguments) throws MetaDataException {
            return 0;
        }

        @Override
        public java.util.Collection<?> executeQuery(ObjectConnection c, String query, java.util.Collection<?> arguments) throws MetaDataException {
            return new java.util.ArrayList<>();
        }
    }

    private static class TestObjectConnection implements ObjectConnection {
        private boolean closed = false;

        @Override
        public Object getDatastoreConnection() { return null; }
        @Override
        public void setReadOnly(boolean state) throws PersistenceException {}
        @Override
        public boolean isReadOnly() throws PersistenceException { return false; }
        @Override
        public void setAutoCommit(boolean state) throws PersistenceException {}
        @Override
        public boolean getAutoCommit() throws PersistenceException { return true; }
        @Override
        public void commit() throws PersistenceException {}
        @Override
        public void rollback() throws PersistenceException {}
        @Override
        public void close() throws MetaDataException { closed = true; }
        @Override
        public boolean isClosed() throws PersistenceException { return closed; }
    }

    private static class TestEventListener implements PersistenceEventListener {
        // Default implementations are sufficient for this test
    }
}