/*
 * Copyright 2003 Draagon Software LLC. All Rights Reserved.
 *
 * This software is the proprietary information of Draagon Software LLC.
 * Use is subject to license terms.
 */

package com.draagon.meta.manager.timeseries;

import java.time.Duration;

/**
 * Time precision for time series aggregation and downsampling
 */
public enum TimePrecision {
    NANOSECOND(Duration.ofNanos(1)),
    MICROSECOND(Duration.ofNanos(1000)),
    MILLISECOND(Duration.ofMillis(1)),
    SECOND(Duration.ofSeconds(1)),
    MINUTE(Duration.ofMinutes(1)),
    HOUR(Duration.ofHours(1)),
    DAY(Duration.ofDays(1)),
    WEEK(Duration.ofDays(7)),
    MONTH(Duration.ofDays(30)), // Approximate
    YEAR(Duration.ofDays(365)); // Approximate
    
    private final Duration duration;
    
    TimePrecision(Duration duration) {
        this.duration = duration;
    }
    
    public Duration getDuration() {
        return duration;
    }
}