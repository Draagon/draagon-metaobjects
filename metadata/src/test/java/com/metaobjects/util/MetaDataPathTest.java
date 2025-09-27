package com.metaobjects.util;

import com.metaobjects.MetaData;
import com.metaobjects.field.MetaField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.util.MetaDataPath;
import com.metaobjects.util.MetaDataPath.PathSegment;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for MetaDataPath utility class.
 */
public class MetaDataPathTest {

    @Test
    public void testPathSegmentCreation() {
        PathSegment segment = new PathSegment("field", "string", "email");
        
        assertEquals("field", segment.getType());
        assertEquals("string", segment.getSubType());
        assertEquals("email", segment.getName());
        assertEquals("field:email(string)", segment.toDisplayString());
    }

    @Test
    public void testPathSegmentWithNullSubType() {
        PathSegment segment = new PathSegment("object", null, "User");
        
        assertEquals("object", segment.getType());
        assertNull(segment.getSubType());
        assertEquals("User", segment.getName());
        assertEquals("object:User", segment.toDisplayString());
    }

    @Test
    public void testPathSegmentEquality() {
        PathSegment segment1 = new PathSegment("field", "string", "email");
        PathSegment segment2 = new PathSegment("field", "string", "email");
        PathSegment segment3 = new PathSegment("field", "int", "email");
        
        assertEquals(segment1, segment2);
        assertNotEquals(segment1, segment3);
        assertEquals(segment1.hashCode(), segment2.hashCode());
    }

    @Test
    public void testBuildPathFromSingleMetaData() {
        MetaData root = createTestMetaData("object", "domain", "User");
        
        MetaDataPath path = MetaDataPath.buildPath(root);
        
        assertFalse(path.isEmpty());
        assertEquals(1, path.size());
        assertEquals("object:User(domain)", path.toHierarchicalString());
        assertEquals("User", path.toSimpleString());
        assertEquals("object", path.toTypeString());
    }

    @Test
    public void testBuildPathFromNestedMetaData() {
        MetaData root = createTestMetaData("object", "domain", "User");
        MetaData field = createTestMetaData("field", "string", "email");
        MetaData validator = createTestMetaData("validator", "required", "emailRequired");
        
        // Simulate hierarchy: User -> email -> emailRequired
        addChild(root, field);
        addChild(field, validator);
        
        MetaDataPath path = MetaDataPath.buildPath(validator);
        
        assertEquals(3, path.size());
        assertEquals("object:User(domain) → field:email(string) → validator:emailRequired(required)", 
                     path.toHierarchicalString());
        assertEquals("User.email.emailRequired", path.toSimpleString());
        assertEquals("object.field.validator", path.toTypeString());
    }

    @Test
    public void testPathSegmentAccess() {
        MetaData root = createTestMetaData("object", "domain", "User");
        MetaData field = createTestMetaData("field", "string", "email");
        
        addChild(root, field);
        
        MetaDataPath path = MetaDataPath.buildPath(field);
        
        List<PathSegment> segments = path.getSegments();
        assertEquals(2, segments.size());
        
        PathSegment rootSegment = path.getRoot();
        assertNotNull(rootSegment);
        assertEquals("User", rootSegment.getName());
        assertEquals("object", rootSegment.getType());
        
        PathSegment leafSegment = path.getLeaf();
        assertNotNull(leafSegment);
        assertEquals("email", leafSegment.getName());
        assertEquals("field", leafSegment.getType());
    }

    @Test
    public void testEmptyPath() {
        MetaDataPath emptyPath = MetaDataPath.empty();
        
        assertTrue(emptyPath.isEmpty());
        assertEquals(0, emptyPath.size());
        assertNull(emptyPath.getRoot());
        assertNull(emptyPath.getLeaf());
        assertEquals("", emptyPath.toHierarchicalString());
        assertEquals("", emptyPath.toSimpleString());
        assertEquals("", emptyPath.toTypeString());
    }

    @Test
    public void testPathEquality() {
        MetaData root1 = createTestMetaData("object", "domain", "User");
        MetaData field1 = createTestMetaData("field", "string", "email");
        addChild(root1, field1);
        
        MetaData root2 = createTestMetaData("object", "domain", "User");
        MetaData field2 = createTestMetaData("field", "string", "email");
        addChild(root2, field2);
        
        MetaDataPath path1 = MetaDataPath.buildPath(field1);
        MetaDataPath path2 = MetaDataPath.buildPath(field2);
        
        assertEquals(path1, path2);
        assertEquals(path1.hashCode(), path2.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildPathWithNullTarget() {
        MetaDataPath.buildPath(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPathSegmentWithNullType() {
        new PathSegment(null, "string", "email");
    }

    @Test(expected = NullPointerException.class)
    public void testPathSegmentWithNullName() {
        new PathSegment("field", "string", null);
    }

    @Test
    public void testToStringMethods() {
        MetaData root = createTestMetaData("object", "domain", "User");
        MetaData field = createTestMetaData("field", "string", "email");
        addChild(root, field);
        
        MetaDataPath path = MetaDataPath.buildPath(field);
        
        // Test that toString() uses hierarchical format
        assertEquals(path.toHierarchicalString(), path.toString());
        
        // Test specific formats
        assertTrue(path.toHierarchicalString().contains("→"));
        assertTrue(path.toSimpleString().contains("."));
        assertTrue(path.toTypeString().contains("."));
    }

    // Helper methods for creating test MetaData objects
    private MetaData createTestMetaData(String type, String subType, String name) {
        return new TestMetaData(type, subType, name);
    }

    private void addChild(MetaData parent, MetaData child) {
        ((TestMetaData) parent).addTestChild(child);
        ((TestMetaData) child).setTestParent(parent);
    }

    // Simple test implementation of MetaData for testing purposes
    private static class TestMetaData extends MetaData {
        private MetaData parent;

        public TestMetaData(String type, String subType, String name) {
            super(type, subType, name);
        }

        @Override
        public MetaData getParent() {
            return parent;
        }

        public void setTestParent(MetaData parent) {
            this.parent = parent;
        }

        public void addTestChild(MetaData child) {
            // For testing purposes, we don't need to maintain the full children collection
        }
    }
}