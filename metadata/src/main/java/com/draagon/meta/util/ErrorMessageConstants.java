/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.util;

/**
 * Error message format templates for consistent error reporting across the MetaObjects framework.
 *
 * <p>These format templates provide standardized patterns for common error scenarios,
 * ensuring consistent messaging and easier localization support in the future.</p>
 *
 * @since 6.0.0
 */
public final class ErrorMessageConstants {

    private ErrorMessageConstants() {
        // Utility class - no instantiation
    }

    // === ERROR MESSAGE FORMATS ===

    /** Format template for not found errors */
    public static final String ERR_NOT_FOUND_FORMAT = "%s '%s' not found in %s";

    /** Format template for type mismatch errors */
    public static final String ERR_TYPE_MISMATCH_FORMAT = "Type mismatch at %s: Expected %s, got %s";

    /** Format template for validation errors */
    public static final String ERR_VALIDATION_FORMAT = "Validation failed for %s: %s";

    /** Format template for configuration errors */
    public static final String ERR_CONFIG_FORMAT = "Configuration error in %s: %s";
}