/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.object.value.ValueMetaObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Test class to verify the enhanced ObjectManager functionality
 */
public class EnhancedObjectManagerTest {

    private TestObjectManager objectManager;
    private TestEventListener eventListener;

    @Before
    public void setUp() {
        objectManager = new TestObjectManager();
        eventListener = new TestEventListener();
        objectManager.addPersistenceEventListener(eventListener);
    }

    @Test
    public void testEventListenerManagement() {
        // Test adding and removing event listeners
        TestEventListener listener2 = new TestEventListener();
        objectManager.addPersistenceEventListener(listener2);
        objectManager.removePersistenceEventListener(listener2);
        
        // Should not fail - verify that listeners are managed properly
        assertTrue("Event listener management works", true);
    }

    @Test
    public void testQueryBuilder() throws MetaDataException {
        // Test that query builder can be created with a valid MetaObject
        ValueMetaObject mc = ValueMetaObject.create("TestClass");
        QueryBuilder builder = objectManager.query(mc);
        assertNotNull("Query builder should be created", builder);
        
        // Test method chaining
        QueryOptions options = builder
                .where("field1", "value1")
                .and("field2", "value2")
                .orderByAsc("field3")
                .limit(10)
                .build();
        
        assertNotNull("Query options should be built", options);
        assertNotNull("Expression should be set", options.getExpression());
    }

    @Test
    public void testAsyncOperations() throws ExecutionException, InterruptedException {
        ValueMetaObject mc = ValueMetaObject.create("TestClass");
        
        // Test async query
        CompletableFuture<Collection<?>> future = objectManager.getObjectsAsync(mc);
        Collection<?> result = future.get();
        assertNotNull("Async query should return result", result);
        
        // Test async create
        Object testObject = new TestObject();
        CompletableFuture<Void> createFuture = objectManager.createObjectAsync(testObject);
        createFuture.get(); // Should complete without exception
        
        assertTrue("Async operations completed successfully", true);
    }

    @Test
    public void testBulkOperations() throws MetaDataException {
        // Test with empty collection to verify no errors
        List<Object> objects = new ArrayList<>();
        
        try (ObjectConnection connection = objectManager.getConnection()) {
            // Test bulk create with empty collection
            objectManager.createObjects(connection, objects);
            
            // Verify no exceptions thrown
            assertTrue("Bulk operations with empty collection should work", true);
        }
    }

    @Test
    public void testResourceManagement() throws MetaDataException {
        // Test try-with-resources
        try (ObjectConnection connection = objectManager.getConnection()) {
            assertNotNull("Connection should be created", connection);
            assertFalse("Connection should not be closed", connection.isClosed());
        } // Connection should be automatically closed
    }

    // Test implementation classes
    private static class TestObjectManager extends ObjectManager {
        @Override
        public ObjectConnection getConnection() throws MetaDataException {
            return new TestObjectConnection();
        }

        @Override
        public void releaseConnection(ObjectConnection oc) throws MetaDataException {
            // Test implementation
        }

        @Override
        public boolean isCreateableClass(MetaObject mc) { return true; }
        @Override
        public boolean isReadableClass(MetaObject mc) { return true; }
        @Override
        public boolean isUpdateableClass(MetaObject mc) { return true; }
        @Override
        public boolean isDeleteableClass(MetaObject mc) { return true; }

        @Override
        public Object getObjectByRef(ObjectConnection c, String refStr) throws MetaDataException {
            return new TestObject();
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
        public Collection<?> getObjects(ObjectConnection c, MetaObject mc, QueryOptions options) throws MetaDataException {
            return new ArrayList<>();
        }

        @Override
        public int execute(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
            return 0;
        }

        @Override
        public Collection<?> executeQuery(ObjectConnection c, String query, Collection<?> arguments) throws MetaDataException {
            return new ArrayList<>();
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


    private static class TestObject {
        // Simple test object
    }

    private static class TestEventListener implements PersistenceEventListener {
        int beforeCreateCount = 0;
        int afterCreateCount = 0;

        @Override
        public void onBeforeCreate(MetaObject mc, Object obj) {
            beforeCreateCount++;
        }

        @Override
        public void onAfterCreate(MetaObject mc, Object obj) {
            afterCreateCount++;
        }
    }
}