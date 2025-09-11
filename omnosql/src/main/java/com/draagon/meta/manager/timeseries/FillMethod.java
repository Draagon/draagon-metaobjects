/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

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