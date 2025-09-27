/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

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