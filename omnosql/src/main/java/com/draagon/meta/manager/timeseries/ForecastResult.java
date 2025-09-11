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
 * Result of time series forecasting
 */
public class ForecastResult {
    private final List<TimeSeriesDataPoint> forecasts;
    private final double confidenceInterval;
    private final ForecastingAlgorithm algorithm;
    private final Map<String, Object> metadata;
    
    public ForecastResult(List<TimeSeriesDataPoint> forecasts,
                         double confidenceInterval,
                         ForecastingAlgorithm algorithm,
                         Map<String, Object> metadata) {
        this.forecasts = List.copyOf(forecasts);
        this.confidenceInterval = confidenceInterval;
        this.algorithm = algorithm;
        this.metadata = Map.copyOf(metadata);
    }
    
    public List<TimeSeriesDataPoint> getForecasts() {
        return forecasts;
    }
    
    public double getConfidenceInterval() {
        return confidenceInterval;
    }
    
    public ForecastingAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}