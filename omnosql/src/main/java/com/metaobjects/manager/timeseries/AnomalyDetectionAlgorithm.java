/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

/**
 * Enumeration of available anomaly detection algorithms
 */
public enum AnomalyDetectionAlgorithm {
    STATISTICAL_OUTLIER,
    Z_SCORE,
    ISOLATION_FOREST,
    ARIMA,
    SEASONAL_DECOMPOSITION,
    CUSTOM
}