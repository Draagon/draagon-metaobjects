/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

/**
 * Enumeration of available forecasting algorithms
 */
public enum ForecastingAlgorithm {
    LINEAR_REGRESSION,
    ARIMA,
    EXPONENTIAL_SMOOTHING,
    SEASONAL_ARIMA,
    LSTM,
    PROPHET,
    CUSTOM
}