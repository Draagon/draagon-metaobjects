/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

import com.metaobjects.MetaDataException;
import com.metaobjects.object.MetaObject;
import com.metaobjects.manager.ObjectConnection;

import java.time.Instant;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for time series database operations beyond standard ObjectManager capabilities.
 * Supports InfluxDB, TimescaleDB, Amazon Timestream, and similar time series databases.
 */
public interface TimeSeriesOperations {

    /**
     * Writes time series data points
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param dataPoints Collection of data points to write
     * @param precision Time precision (nanosecond, microsecond, millisecond, second)
     */
    void writeDataPoints(ObjectConnection c, MetaObject mc, Collection<TimeSeriesDataPoint> dataPoints, TimePrecision precision) throws MetaDataException;

    /**
     * Queries time series data with time range
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param startTime Start time (inclusive)
     * @param endTime End time (exclusive)
     * @param tags Optional tag filters
     * @param fields Optional field filters
     * @return Collection of data points
     */
    Collection<TimeSeriesDataPoint> queryByTimeRange(ObjectConnection c, MetaObject mc, Instant startTime, Instant endTime, 
                                                     Map<String, String> tags, List<String> fields) throws MetaDataException;

    /**
     * Performs aggregation over time windows
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param startTime Start time
     * @param endTime End time
     * @param windowSize Time window size for grouping
     * @param aggregations Aggregation functions to apply
     * @param groupByTags Tags to group by
     * @return Aggregation results
     */
    Collection<AggregationResult> aggregate(ObjectConnection c, MetaObject mc, Instant startTime, Instant endTime,
                                          Duration windowSize, List<AggregationFunction> aggregations, List<String> groupByTags) throws MetaDataException;

    /**
     * Creates a continuous query for real-time aggregation
     * @param c Connection to the time series database
     * @param queryName Name of the continuous query
     * @param sourceMetric Source measurement/metric
     * @param targetMetric Target measurement for results
     * @param query Continuous query definition
     * @param schedule How often to run the query
     */
    void createContinuousQuery(ObjectConnection c, String queryName, MetaObject sourceMetric, MetaObject targetMetric,
                              String query, Duration schedule) throws MetaDataException;

    /**
     * Drops a continuous query
     * @param c Connection to the time series database
     * @param queryName Name of the continuous query to drop
     */
    void dropContinuousQuery(ObjectConnection c, String queryName) throws MetaDataException;

    /**
     * Sets up a retention policy for automatic data deletion
     * @param c Connection to the time series database
     * @param policyName Name of the retention policy
     * @param duration How long to keep data
     * @param replication Replication factor
     * @param isDefault Whether this is the default policy
     */
    void createRetentionPolicy(ObjectConnection c, String policyName, Duration duration, int replication, boolean isDefault) throws MetaDataException;

    /**
     * Gets the latest data point for each series
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param tags Optional tag filters
     * @return Map of series identifier to latest data point
     */
    Map<String, TimeSeriesDataPoint> getLastValues(ObjectConnection c, MetaObject mc, Map<String, String> tags) throws MetaDataException;

    /**
     * Downsamples high-resolution data to lower resolution
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param startTime Start time
     * @param endTime End time
     * @param samplingInterval Interval for downsampling
     * @param aggregationFunction Function to use for downsampling
     * @param tags Optional tag filters
     * @return Downsampled data points
     */
    Collection<TimeSeriesDataPoint> downsample(ObjectConnection c, MetaObject mc, Instant startTime, Instant endTime,
                                             Duration samplingInterval, AggregationFunction aggregationFunction, 
                                             Map<String, String> tags) throws MetaDataException;

    /**
     * Fills missing data points using interpolation
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param startTime Start time
     * @param endTime End time
     * @param interval Expected interval between data points
     * @param fillMethod Method to use for filling gaps
     * @param tags Optional tag filters
     * @return Data points with gaps filled
     */
    Collection<TimeSeriesDataPoint> fillGaps(ObjectConnection c, MetaObject mc, Instant startTime, Instant endTime,
                                           Duration interval, FillMethod fillMethod, Map<String, String> tags) throws MetaDataException;

    /**
     * Detects anomalies in time series data
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param startTime Start time
     * @param endTime End time
     * @param algorithm Anomaly detection algorithm
     * @param parameters Algorithm-specific parameters
     * @param tags Optional tag filters
     * @return Collection of detected anomalies
     */
    Collection<AnomalyResult> detectAnomalies(ObjectConnection c, MetaObject mc, Instant startTime, Instant endTime,
                                            AnomalyDetectionAlgorithm algorithm, Map<String, Object> parameters,
                                            Map<String, String> tags) throws MetaDataException;

    /**
     * Creates forecasts based on historical data
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param historicalStartTime Start of historical data for training
     * @param historicalEndTime End of historical data
     * @param forecastDuration How far into the future to forecast
     * @param algorithm Forecasting algorithm
     * @param parameters Algorithm-specific parameters
     * @param tags Optional tag filters
     * @return Forecasted data points
     */
    Collection<ForecastResult> forecast(ObjectConnection c, MetaObject mc, Instant historicalStartTime, Instant historicalEndTime,
                                      Duration forecastDuration, ForecastingAlgorithm algorithm, Map<String, Object> parameters,
                                      Map<String, String> tags) throws MetaDataException;

    /**
     * Gets database statistics and metrics
     * @param c Connection to the time series database
     * @return Database statistics
     */
    TimeSeriesStatistics getDatabaseStatistics(ObjectConnection c) throws MetaDataException;

    /**
     * Executes raw time series query (database-specific)
     * @param c Connection to the time series database
     * @param query Raw query string (e.g., InfluxQL, SQL)
     * @param parameters Query parameters
     * @return Raw query results
     */
    Collection<?> executeRawQuery(ObjectConnection c, String query, Map<String, Object> parameters) throws MetaDataException;

    /**
     * Asynchronously writes time series data
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param dataPoints Collection of data points to write
     * @param precision Time precision
     * @return CompletableFuture that completes when write finishes
     */
    CompletableFuture<Void> writeDataPointsAsync(ObjectConnection c, MetaObject mc, Collection<TimeSeriesDataPoint> dataPoints, 
                                                TimePrecision precision) throws MetaDataException;

    /**
     * Streams real-time data points
     * @param c Connection to the time series database
     * @param mc MetaObject describing the measurement/metric
     * @param tags Optional tag filters
     * @param callback Callback function to process each data point
     */
    void streamRealTimeData(ObjectConnection c, MetaObject mc, Map<String, String> tags, 
                           TimeSeriesDataCallback callback) throws MetaDataException;
}