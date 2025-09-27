/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager;

import com.metaobjects.MetaDataException;
import com.metaobjects.field.StringField;
import com.metaobjects.field.DoubleField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.value.ValueMetaObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simplified persistence layer examples demonstrating the architecture.
 * This serves as basic integration tests for the ObjectManager enhancements.
 */
public class PersistenceLayerExamples {

    private MetaObject customerMetaObject;
    private MetaObject productMetaObject;

    @Before
    public void setUp() throws MetaDataException {
        // Create sample MetaObjects for testing
        customerMetaObject = ValueMetaObject.create("Customer");
        customerMetaObject.addChild(new StringField("id"));
        customerMetaObject.addChild(new StringField("name"));
        customerMetaObject.addChild(new StringField("email"));
        
        productMetaObject = ValueMetaObject.create("Product");
        productMetaObject.addChild(new StringField("id"));
        productMetaObject.addChild(new StringField("name"));
        productMetaObject.addChild(new DoubleField("price"));
    }

    /**
     * Tests that the basic MetaObject setup works
     */
    @Test
    public void testBasicMetaObjectSetup() throws MetaDataException {
        assertNotNull("Customer MetaObject should not be null", customerMetaObject);
        assertNotNull("Product MetaObject should not be null", productMetaObject);
        
        assertEquals("Customer", customerMetaObject.getName());
        assertEquals("Product", productMetaObject.getName());
        
        // Verify fields were added
        assertNotNull("Should have id field", customerMetaObject.getMetaField("id"));
        assertNotNull("Should have name field", customerMetaObject.getMetaField("name"));
        assertNotNull("Should have email field", customerMetaObject.getMetaField("email"));
    }

    /**
     * Tests that the ObjectManager enhancement classes exist and can be referenced
     */
    @Test
    public void testObjectManagerClassesExist() {
        // Test that the main persistence layer classes exist
        try {
            Class.forName("com.metaobjects.manager.document.ObjectManagerDocument");
            Class.forName("com.metaobjects.manager.keyvalue.ObjectManagerKeyValue");
            Class.forName("com.metaobjects.manager.graph.ObjectManagerGraph");
            
            // Test that the operation interfaces exist  
            Class.forName("com.metaobjects.manager.document.DocumentOperations");
            Class.forName("com.metaobjects.manager.keyvalue.KeyValueOperations");
            Class.forName("com.metaobjects.manager.graph.GraphOperations");
            Class.forName("com.metaobjects.manager.timeseries.TimeSeriesOperations");
            Class.forName("com.metaobjects.manager.search.SearchOperations");
            Class.forName("com.metaobjects.manager.columnar.ColumnFamilyOperations");
            
        } catch (ClassNotFoundException e) {
            fail("Expected ObjectManager enhancement classes should exist: " + e.getMessage());
        }
    }

    /**
     * Tests that the support classes for specialized operations exist
     */
    @Test
    public void testSupportClassesExist() {
        try {
            // Time series support classes
            Class.forName("com.metaobjects.manager.timeseries.TimeSeriesDataPoint");
            Class.forName("com.metaobjects.manager.timeseries.AggregationFunction");
            Class.forName("com.metaobjects.manager.timeseries.AggregationResult");
            Class.forName("com.metaobjects.manager.timeseries.TimePrecision");
            
            // Columnar support classes
            Class.forName("com.metaobjects.manager.columnar.ConsistencyLevel");
            Class.forName("com.metaobjects.manager.columnar.RowKey");
            Class.forName("com.metaobjects.manager.columnar.TokenRange");
            
        } catch (ClassNotFoundException e) {
            fail("Expected support classes should exist: " + e.getMessage());
        }
    }

    /**
     * Tests that reference implementations exist
     */
    @Test
    public void testReferenceImplementationsExist() {
        try {
            // Document database reference implementation
            Class.forName("com.metaobjects.manager.document.mongodb.ObjectManagerMongoDB");
            Class.forName("com.metaobjects.manager.document.mongodb.ObjectConnectionMongoDB");
            
            // Key-value reference implementation
            Class.forName("com.metaobjects.manager.keyvalue.redis.ObjectManagerRedis");
            Class.forName("com.metaobjects.manager.keyvalue.redis.ObjectConnectionRedis");
            
        } catch (ClassNotFoundException e) {
            fail("Expected reference implementation classes should exist: " + e.getMessage());
        }
    }
}