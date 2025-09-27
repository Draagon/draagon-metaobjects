/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Supporting classes and enums for time series operations
 */
public class TimeSeriesSupportClasses {

    /**
     * Time precision for time series data
     */
    public enum TimePrecision {
        NANOSECOND("ns"),
        MICROSECOND("us"), 
        MILLISECOND("ms"),
        SECOND("s"),
        MINUTE("m"),
        HOUR("h");
        
        private final String symbol;
        
        TimePrecision(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() { return symbol; }
    }

    /**
     * Aggregation functions for time series data
     */
    public enum AggregationFunction {
        COUNT, SUM, MEAN, MEDIAN, MODE,
        MIN, MAX, RANGE,
        FIRST, LAST,
        STDDEV, VARIANCE,
        PERCENTILE_25, PERCENTILE_75, PERCENTILE_95, PERCENTILE_99,
        RATE, INCREASE, DELTA,
        MOVING_AVERAGE, EXPONENTIAL_MOVING_AVERAGE
    }

    /**
     * Methods for filling missing data points
     */
    public enum FillMethod {
        NULL,           // Leave gaps as null
        ZERO,           // Fill with zero
        PREVIOUS,       // Use previous value (forward fill)
        LINEAR,         // Linear interpolation
        NONE            // Don't fill gaps
    }

    /**
     * Anomaly detection algorithms
     */
    public enum AnomalyDetectionAlgorithm {
        Z_SCORE,           // Statistical z-score based
        ISOLATION_FOREST,  // Isolation forest algorithm
        ONE_CLASS_SVM,     // One-class support vector machine
        ARIMA_RESIDUALS,   // ARIMA model residuals
        SEASONAL_HYBRID,   // Seasonal hybrid algorithm
        THRESHOLD          // Simple threshold-based
    }

    /**
     * Forecasting algorithms
     */
    public enum ForecastingAlgorithm {
        LINEAR_REGRESSION,     // Simple linear regression
        ARIMA,                 // Auto-regressive integrated moving average
        EXPONENTIAL_SMOOTHING, // Exponential smoothing
        SEASONAL_ARIMA,        // Seasonal ARIMA
        PROPHET,               // Facebook Prophet algorithm
        NEURAL_NETWORK         // Neural network based
    }

    /**
     * Represents a single time series data point
     */
    public static class TimeSeriesDataPoint {
        private final Instant timestamp;
        private final Map<String, String> tags;
        private final Map<String, Object> fields;
        private final String measurement;
        
        public TimeSeriesDataPoint(String measurement, Instant timestamp, Map<String, String> tags, Map<String, Object> fields) {
            this.measurement = measurement;
            this.timestamp = timestamp;
            this.tags = tags;
            this.fields = fields;
        }
        
        public String getMeasurement() { return measurement; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, String> getTags() { return tags; }
        public Map<String, Object> getFields() { return fields; }
        
        public Object getFieldValue(String fieldName) {
            return fields.get(fieldName);
        }
        
        public String getTagValue(String tagName) {
            return tags.get(tagName);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TimeSeriesDataPoint)) return false;
            TimeSeriesDataPoint that = (TimeSeriesDataPoint) o;
            return Objects.equals(timestamp, that.timestamp) &&
                   Objects.equals(tags, that.tags) &&
                   Objects.equals(fields, that.fields) &&
                   Objects.equals(measurement, that.measurement);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(timestamp, tags, fields, measurement);
        }
        
        @Override
        public String toString() {
            return String.format("DataPoint{measurement='%s', timestamp=%s, tags=%s, fields=%s}", 
                    measurement, timestamp, tags, fields);
        }
    }

    /**
     * Result of an aggregation operation
     */
    public static class AggregationResult {
        private final Instant windowStart;
        private final Instant windowEnd;
        private final Map<String, String> groupTags;
        private final Map<String, Object> aggregatedValues;
        
        public AggregationResult(Instant windowStart, Instant windowEnd, Map<String, String> groupTags, Map<String, Object> aggregatedValues) {
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
            this.groupTags = groupTags;
            this.aggregatedValues = aggregatedValues;
        }
        
        public Instant getWindowStart() { return windowStart; }
        public Instant getWindowEnd() { return windowEnd; }
        public Map<String, String> getGroupTags() { return groupTags; }
        public Map<String, Object> getAggregatedValues() { return aggregatedValues; }
        
        public Object getAggregatedValue(String fieldName) {
            return aggregatedValues.get(fieldName);
        }
        
        @Override
        public String toString() {
            return String.format("AggregationResult{window=[%s, %s), tags=%s, values=%s}", 
                    windowStart, windowEnd, groupTags, aggregatedValues);
        }
    }

    /**
     * Result of anomaly detection
     */
    public static class AnomalyResult {
        private final TimeSeriesDataPoint dataPoint;
        private final double anomalyScore;
        private final String anomalyType;
        private final Map<String, Object> details;
        
        public AnomalyResult(TimeSeriesDataPoint dataPoint, double anomalyScore, String anomalyType, Map<String, Object> details) {
            this.dataPoint = dataPoint;
            this.anomalyScore = anomalyScore;
            this.anomalyType = anomalyType;
            this.details = details;
        }
        
        public TimeSeriesDataPoint getDataPoint() { return dataPoint; }
        public double getAnomalyScore() { return anomalyScore; }
        public String getAnomalyType() { return anomalyType; }
        public Map<String, Object> getDetails() { return details; }
        
        @Override
        public String toString() {
            return String.format("AnomalyResult{timestamp=%s, score=%.3f, type='%s'}", 
                    dataPoint.getTimestamp(), anomalyScore, anomalyType);
        }
    }

    /**
     * Result of forecasting operation
     */
    public static class ForecastResult {
        private final TimeSeriesDataPoint predictedDataPoint;
        private final double confidence;
        private final double upperBound;
        private final double lowerBound;
        private final Map<String, Object> modelInfo;
        
        public ForecastResult(TimeSeriesDataPoint predictedDataPoint, double confidence, double upperBound, double lowerBound, Map<String, Object> modelInfo) {
            this.predictedDataPoint = predictedDataPoint;
            this.confidence = confidence;
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
            this.modelInfo = modelInfo;
        }
        
        public TimeSeriesDataPoint getPredictedDataPoint() { return predictedDataPoint; }
        public double getConfidence() { return confidence; }
        public double getUpperBound() { return upperBound; }
        public double getLowerBound() { return lowerBound; }
        public Map<String, Object> getModelInfo() { return modelInfo; }
        
        @Override
        public String toString() {
            return String.format("ForecastResult{timestamp=%s, value=%s, confidence=%.2f}", 
                    predictedDataPoint.getTimestamp(), predictedDataPoint.getFields(), confidence);
        }
    }

    /**
     * Statistics about a time series database
     */
    public static class TimeSeriesStatistics {
        private final long totalDataPoints;
        private final long totalSeries;
        private final long totalMeasurements;
        private final Map<String, Long> measurementCounts;
        private final Instant oldestTimestamp;
        private final Instant newestTimestamp;
        private final long diskUsageBytes;
        private final Map<String, Object> additionalStats;
        
        public TimeSeriesStatistics(long totalDataPoints, long totalSeries, long totalMeasurements,
                                   Map<String, Long> measurementCounts, Instant oldestTimestamp, Instant newestTimestamp,
                                   long diskUsageBytes, Map<String, Object> additionalStats) {
            this.totalDataPoints = totalDataPoints;
            this.totalSeries = totalSeries;
            this.totalMeasurements = totalMeasurements;
            this.measurementCounts = measurementCounts;
            this.oldestTimestamp = oldestTimestamp;
            this.newestTimestamp = newestTimestamp;
            this.diskUsageBytes = diskUsageBytes;
            this.additionalStats = additionalStats;
        }
        
        // Getters
        public long getTotalDataPoints() { return totalDataPoints; }
        public long getTotalSeries() { return totalSeries; }
        public long getTotalMeasurements() { return totalMeasurements; }
        public Map<String, Long> getMeasurementCounts() { return measurementCounts; }
        public Instant getOldestTimestamp() { return oldestTimestamp; }
        public Instant getNewestTimestamp() { return newestTimestamp; }
        public long getDiskUsageBytes() { return diskUsageBytes; }
        public Map<String, Object> getAdditionalStats() { return additionalStats; }
        
        @Override
        public String toString() {
            return String.format("TimeSeriesStatistics{dataPoints=%d, series=%d, measurements=%d, timeRange=[%s, %s]}", 
                    totalDataPoints, totalSeries, totalMeasurements, oldestTimestamp, newestTimestamp);
        }
    }

    /**
     * Callback interface for streaming time series data
     */
    @FunctionalInterface
    public interface TimeSeriesDataCallback {
        void onDataPoint(TimeSeriesDataPoint dataPoint);
    }
}