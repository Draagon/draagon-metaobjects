/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

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