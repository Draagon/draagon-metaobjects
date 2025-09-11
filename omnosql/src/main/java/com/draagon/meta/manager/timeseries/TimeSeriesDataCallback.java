/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

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