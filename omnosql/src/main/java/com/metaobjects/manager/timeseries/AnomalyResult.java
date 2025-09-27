/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of anomaly detection analysis
 */
public class AnomalyResult {
    private final List<TimeSeriesDataPoint> anomalies;
    private final double confidenceScore;
    private final AnomalyDetectionAlgorithm algorithm;
    private final Map<String, Object> metadata;
    
    public AnomalyResult(List<TimeSeriesDataPoint> anomalies, 
                        double confidenceScore, 
                        AnomalyDetectionAlgorithm algorithm,
                        Map<String, Object> metadata) {
        this.anomalies = List.copyOf(anomalies);
        this.confidenceScore = confidenceScore;
        this.algorithm = algorithm;
        this.metadata = Map.copyOf(metadata);
    }
    
    public List<TimeSeriesDataPoint> getAnomalies() {
        return anomalies;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public AnomalyDetectionAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public boolean hasAnomalies() {
        return !anomalies.isEmpty();
    }
}