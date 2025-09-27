/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.web.util;

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