/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */
package com.draagon.meta.loader;

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