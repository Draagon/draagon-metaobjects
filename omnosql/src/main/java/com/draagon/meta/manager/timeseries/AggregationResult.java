/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of time series aggregation operation
 */
public class AggregationResult {
    private final List<TimeSeriesDataPoint> aggregatedData;
    private final AggregationFunction function;
    private final TimePrecision precision;
    private final Instant startTime;
    private final Instant endTime;
    private final Map<String, Object> metadata;
    
    public AggregationResult(List<TimeSeriesDataPoint> aggregatedData,
                           AggregationFunction function,
                           TimePrecision precision,
                           Instant startTime,
                           Instant endTime,
                           Map<String, Object> metadata) {
        this.aggregatedData = List.copyOf(aggregatedData);
        this.function = function;
        this.precision = precision;
        this.startTime = startTime;
        this.endTime = endTime;
        this.metadata = Map.copyOf(metadata);
    }
    
    public List<TimeSeriesDataPoint> getAggregatedData() {
        return aggregatedData;
    }
    
    public AggregationFunction getFunction() {
        return function;
    }
    
    public TimePrecision getPrecision() {
        return precision;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}