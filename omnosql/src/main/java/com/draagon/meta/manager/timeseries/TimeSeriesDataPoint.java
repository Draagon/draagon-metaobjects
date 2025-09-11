/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single data point in a time series
 */
public class TimeSeriesDataPoint {
    private final Instant timestamp;
    private final Object value;
    private final Map<String, Object> tags;
    
    public TimeSeriesDataPoint(Instant timestamp, Object value) {
        this(timestamp, value, Map.of());
    }
    
    public TimeSeriesDataPoint(Instant timestamp, Object value, Map<String, Object> tags) {
        this.timestamp = timestamp;
        this.value = value;
        this.tags = Map.copyOf(tags);
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Map<String, Object> getTags() {
        return tags;
    }
    
    @Override
    public String toString() {
        return String.format("TimeSeriesDataPoint{timestamp=%s, value=%s, tags=%s}", 
                timestamp, value, tags);
    }
}