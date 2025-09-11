/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

/**
 * Aggregation functions for time series data
 */
public enum AggregationFunction {
    SUM,
    AVERAGE,
    MIN,
    MAX,
    COUNT,
    MEDIAN,
    STANDARD_DEVIATION,
    VARIANCE,
    FIRST,
    LAST,
    RATE,
    DERIVATIVE,
    INTEGRAL,
    PERCENTILE
}