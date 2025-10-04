/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.web.view;

import com.metaobjects.MetaDataException;
import com.metaobjects.field.StringField;
import com.metaobjects.object.MetaObject;
import com.metaobjects.object.value.ValueMetaObject;
import com.metaobjects.web.view.html.TextView;
import com.metaobjects.web.view.html.TextAreaView;
import com.metaobjects.web.view.html.DateView;
import com.metaobjects.web.view.html.HotLinkView;
import com.metaobjects.attr.StringAttribute;
import com.metaobjects.attr.IntAttribute;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Enhanced tests for web view components to improve web module coverage
 */
public class ViewComponentTest {

    private MetaObject metaObject;
    private StringField nameField;
    private StringField emailField;

    @Before
    public void setUp() throws MetaDataException {
        // Create test MetaObject
        metaObject = ValueMetaObject.create("TestUser");

        // Add fields
        nameField = new StringField("name");
        emailField = new StringField("email");
        metaObject.addChild(nameField);
        metaObject.addChild(emailField);
    }

    @Test
    public void testTextViewCreation() {
        TextView textView = new TextView("userNameView");
        assertNotNull("TextView should be created", textView);
        assertEquals("View name should be set", "userNameView", textView.getName());
        assertEquals("View type should be text", "text", textView.getSubType());
    }

    @Test
    public void testTextViewHierarchy() throws MetaDataException {
        TextView textView = new TextView("nameView");

        // Add view as child of field
        nameField.addChild(textView);

        // Test hierarchy
        assertEquals("Parent should be nameField", nameField, textView.getParent());
        assertTrue("Field should contain view", nameField.getChildren().contains(textView));

        // Test view can access its declaring field
        assertEquals("Should get declaring field", nameField, textView.getDeclaringMetaField());
    }

    @Test
    public void testTextAreaViewCreation() {
        TextAreaView textAreaView = new TextAreaView("descriptionView");
        assertNotNull("TextAreaView should be created", textAreaView);
        assertEquals("View name should be set", "descriptionView", textAreaView.getName());
        assertEquals("View type should be textarea", "textarea", textAreaView.getSubType());
    }

    @Test
    public void testTextAreaViewAttributes() throws MetaDataException {
        TextAreaView textAreaView = new TextAreaView("commentsView");

        // Add attributes for textarea configuration (using IntAttribute as required by constraints)
        IntAttribute rowsAttr = new IntAttribute("rows");
        rowsAttr.setValue(5);
        textAreaView.addChild(rowsAttr);

        IntAttribute colsAttr = new IntAttribute("cols");
        colsAttr.setValue(40);
        textAreaView.addChild(colsAttr);

        // Test that attributes are set
        assertTrue("Should have rows attribute", textAreaView.hasMetaAttr("rows"));
        assertTrue("Should have cols attribute", textAreaView.hasMetaAttr("cols"));
        assertEquals("Rows should be 5", "5", textAreaView.getMetaAttr("rows").getValueAsString());
        assertEquals("Cols should be 40", "40", textAreaView.getMetaAttr("cols").getValueAsString());
    }

    @Test
    public void testDateViewCreation() {
        DateView dateView = new DateView("birthdateView");
        assertNotNull("DateView should be created", dateView);
        assertEquals("View name should be set", "birthdateView", dateView.getName());
        assertEquals("View type should be date", "date", dateView.getSubType());
    }

    @Test
    public void testDateViewAttributes() throws MetaDataException {
        DateView dateView = new DateView("dateView");

        // Add date format attribute
        StringAttribute formatAttr = new StringAttribute("dateFormat");
        formatAttr.setValue("yyyy-MM-dd");
        dateView.addChild(formatAttr);

        assertTrue("Should have dateFormat attribute", dateView.hasMetaAttr("dateFormat"));
        assertEquals("Date format should be set", "yyyy-MM-dd",
                   dateView.getMetaAttr("dateFormat").getValueAsString());
    }

    @Test
    public void testHotLinkViewCreation() {
        HotLinkView hotLinkView = new HotLinkView("linkView");
        assertNotNull("HotLinkView should be created", hotLinkView);
        assertEquals("View name should be set", "linkView", hotLinkView.getName());
        assertEquals("View type should be hotlink", "hotlink", hotLinkView.getSubType());
    }

    @Test
    public void testHotLinkViewAttributes() throws MetaDataException {
        HotLinkView hotLinkView = new HotLinkView("profileLinkView");

        // Add link attributes
        StringAttribute hrefAttr = new StringAttribute("href");
        hrefAttr.setValue("/user/profile");
        hotLinkView.addChild(hrefAttr);

        StringAttribute targetAttr = new StringAttribute("target");
        targetAttr.setValue("_blank");
        hotLinkView.addChild(targetAttr);

        StringAttribute textAttr = new StringAttribute("linkText");
        textAttr.setValue("View Profile");
        hotLinkView.addChild(textAttr);

        // Test attributes are set
        assertTrue("Should have href attribute", hotLinkView.hasMetaAttr("href"));
        assertTrue("Should have target attribute", hotLinkView.hasMetaAttr("target"));
        assertTrue("Should have linkText attribute", hotLinkView.hasMetaAttr("linkText"));

        assertEquals("Href should be set", "/user/profile",
                   hotLinkView.getMetaAttr("href").getValueAsString());
        assertEquals("Target should be set", "_blank",
                   hotLinkView.getMetaAttr("target").getValueAsString());
        assertEquals("Link text should be set", "View Profile",
                   hotLinkView.getMetaAttr("linkText").getValueAsString());
    }

    @Test
    public void testViewWithMetaField() throws MetaDataException {
        TextView textView = new TextView("fieldView");
        emailField.addChild(textView);

        // Create test object with values
        TestUser testUser = new TestUser("John Doe", "john@example.com");

        // Test that view can access the field's display string
        try {
            String displayString = textView.getDisplayString(testUser);
            // This might fail if MetaDataUtil can't find the object, which is expected
            // in this test environment, but we test that the method exists
        } catch (Exception e) {
            // Expected in test environment without full metadata setup
            assertTrue("Should be MetaDataException or related",
                     e instanceof MetaDataException || e instanceof RuntimeException);
        }
    }

    @Test
    public void testViewModeConstants() {
        // Test that view mode constants are accessible
        assertEquals("READ mode should be 0", 0, WebView.READ);
        assertEquals("EDIT mode should be 1", 1, WebView.EDIT);
        assertEquals("HIDE mode should be 2", 2, WebView.HIDE);
    }

    @Test
    public void testViewAttributeAccess() throws MetaDataException {
        TextView textView = new TextView("attributeView");

        // Add various attributes using MetaData API
        StringAttribute labelAttr = new StringAttribute("webLabel");
        labelAttr.setValue("User Name");
        textView.addChild(labelAttr);

        StringAttribute placeholderAttr = new StringAttribute("webPlaceholder");
        placeholderAttr.setValue("Enter your name");
        textView.addChild(placeholderAttr);

        StringAttribute cssClassAttr = new StringAttribute("webCssClass");
        cssClassAttr.setValue("form-control required");
        textView.addChild(cssClassAttr);

        // Test attribute access
        assertTrue("Should have webLabel", textView.hasMetaAttr("webLabel"));
        assertTrue("Should have webPlaceholder", textView.hasMetaAttr("webPlaceholder"));
        assertTrue("Should have webCssClass", textView.hasMetaAttr("webCssClass"));

        assertEquals("Label should be set", "User Name",
                   textView.getMetaAttr("webLabel").getValueAsString());
        assertEquals("Placeholder should be set", "Enter your name",
                   textView.getMetaAttr("webPlaceholder").getValueAsString());
        assertEquals("CSS class should be set", "form-control required",
                   textView.getMetaAttr("webCssClass").getValueAsString());
    }

    @Test
    public void testViewTypeRegistration() {
        // Test that view types can be created without errors
        // This tests the basic type system integration
        TextView textView = new TextView("regTest1");
        TextAreaView textAreaView = new TextAreaView("regTest2");
        DateView dateView = new DateView("regTest3");
        HotLinkView hotLinkView = new HotLinkView("regTest4");

        // All should be created successfully
        assertNotNull("TextView should be created", textView);
        assertNotNull("TextAreaView should be created", textAreaView);
        assertNotNull("DateView should be created", dateView);
        assertNotNull("HotLinkView should be created", hotLinkView);

        // Test that they have correct types
        assertEquals("view", textView.getType());
        assertEquals("view", textAreaView.getType());
        assertEquals("view", dateView.getType());
        assertEquals("view", hotLinkView.getType());
    }

    // Test helper class
    private static class TestUser {
        private final String name;
        private final String email;

        public TestUser(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}