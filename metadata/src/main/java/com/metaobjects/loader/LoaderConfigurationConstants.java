/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */
package com.metaobjects.loader;

/**
 * Constants for loader configuration arguments.
 * These replace the Maven-specific constants from MojoSupport.
 */
public final class LoaderConfigurationConstants {
    
    public static final String ARG_REGISTER = "register";
    public static final String ARG_VERBOSE = "verbose";
    public static final String ARG_STRICT = "strict";
    
    private LoaderConfigurationConstants() {
        // Utility class - no instances
    }
}