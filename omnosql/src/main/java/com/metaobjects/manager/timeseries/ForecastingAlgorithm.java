/*
 * Copyright 2003 Doug Mealing LLC dba Meta Objects. All Rights Reserved.
 *
 * This software is the proprietary information of Doug Mealing LLC dba Meta Objects.
 * Use is subject to license terms.
 */

package com.metaobjects.manager.timeseries;

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