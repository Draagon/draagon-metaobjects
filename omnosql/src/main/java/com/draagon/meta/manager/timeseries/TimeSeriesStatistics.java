/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

import java.util.Map;

/**
 * Statistical metrics for time series data
 */
public class TimeSeriesStatistics {
    private final long count;
    private final double min;
    private final double max;
    private final double mean;
    private final double median;
    private final double standardDeviation;
    private final double variance;
    private final Map<String, Object> additionalStats;
    
    public TimeSeriesStatistics(long count, double min, double max, double mean,
                               double median, double standardDeviation, double variance,
                               Map<String, Object> additionalStats) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
        this.additionalStats = Map.copyOf(additionalStats);
    }
    
    public long getCount() {
        return count;
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    public double getMean() {
        return mean;
    }
    
    public double getMedian() {
        return median;
    }
    
    public double getStandardDeviation() {
        return standardDeviation;
    }
    
    public double getVariance() {
        return variance;
    }
    
    public Map<String, Object> getAdditionalStats() {
        return additionalStats;
    }
}