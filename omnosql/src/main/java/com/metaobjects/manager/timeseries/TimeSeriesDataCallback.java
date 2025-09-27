/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

/**
 * Callback interface for streaming time series data processing
 */
@FunctionalInterface
public interface TimeSeriesDataCallback {
    /**
     * Process a single data point from the time series stream
     * 
     * @param dataPoint the data point to process
     * @return true to continue streaming, false to stop
     */
    boolean processDataPoint(TimeSeriesDataPoint dataPoint);
}