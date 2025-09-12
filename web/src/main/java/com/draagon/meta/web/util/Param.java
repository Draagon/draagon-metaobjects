/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.web.util;

/**
 * A simple name-value parameter pair for HTML form elements
 */
public class Param {
    private final String name;
    private final String value;
    
    public Param(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "Param{name='" + name + "', value='" + value + "'}";
    }
}