/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

/**
 * Methods for filling missing values in time series data
 */
public enum FillMethod {
    NONE,           // Leave gaps as null
    NULL_VALUE,     // Fill with null
    ZERO,           // Fill with zero
    PREVIOUS,       // Forward fill (last known value)
    NEXT,           // Backward fill (next known value)
    LINEAR,         // Linear interpolation
    MEAN,           // Fill with mean of surrounding values
    MEDIAN,         // Fill with median of surrounding values
    CUSTOM_VALUE    // Fill with a custom specified value
}