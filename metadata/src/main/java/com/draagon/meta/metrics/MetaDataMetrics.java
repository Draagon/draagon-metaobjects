package com.draagon.meta.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Metrics collection for MetaData operations.
 * Provides thread-safe counters and timing information.
 */
public class MetaDataMetrics {
    
    private final String name;
    private final Instant createdAt;
    
    // Creation metrics
    private final LongAdder creationCount = new LongAdder();
    private final LongAdder validationCount = new LongAdder();
    private final LongAdder validationFailures = new LongAdder();
    
    // Child operation metrics
    private final LongAdder childAdditions = new LongAdder();
    private final LongAdder childRemovals = new LongAdder();
    private final LongAdder childReplacements = new LongAdder();
    
    // Cache metrics
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();
    private final LongAdder cacheEvictions = new LongAdder();
    
    // Performance metrics
    private final AtomicLong totalValidationTime = new AtomicLong();
    private final AtomicLong maxValidationTime = new AtomicLong();
    private final AtomicLong totalChildSearchTime = new AtomicLong();
    
    // Error metrics
    private final LongAdder errorCount = new LongAdder();
    private final AtomicLong lastErrorTime = new AtomicLong();
    
    public MetaDataMetrics(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }
    
    // ========== RECORDING METHODS ==========
    
    public void recordCreation() {
        creationCount.increment();
    }
    
    public void recordValidation(Duration duration, boolean success) {
        validationCount.increment();
        if (!success) {
            validationFailures.increment();
        }
        
        long durationMillis = duration.toMillis();
        totalValidationTime.addAndGet(durationMillis);
        
        // Update max validation time
        long currentMax = maxValidationTime.get();
        while (durationMillis > currentMax) {
            if (maxValidationTime.compareAndSet(currentMax, durationMillis)) {
                break;
            }
            currentMax = maxValidationTime.get();
        }
    }
    
    public void recordChildAddition() {
        childAdditions.increment();
    }
    
    public void recordChildRemoval() {
        childRemovals.increment();
    }
    
    public void recordChildReplacement() {
        childReplacements.increment();
    }
    
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    public void recordCacheEviction() {
        cacheEvictions.increment();
    }
    
    public void recordChildSearchTime(Duration duration) {
        totalChildSearchTime.addAndGet(duration.toNanos());
    }
    
    public void recordError() {
        errorCount.increment();
        lastErrorTime.set(System.currentTimeMillis());
    }
    
    public void recordPropertyChange() {
        // General property change - could be extended with specific metrics
        // For now, just count as general events
    }
    
    public void recordInstanceCreation(Duration duration, boolean success) {
        // Record instance creation with timing
        if (success) {
            // Could add specific instance creation metrics here
        } else {
            recordError();
        }
    }
    
    // ========== QUERY METHODS ==========
    
    public String getName() {
        return name;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Duration getUptime() {
        return Duration.between(createdAt, Instant.now());
    }
    
    public long getCreationCount() {
        return creationCount.sum();
    }
    
    public long getValidationCount() {
        return validationCount.sum();
    }
    
    public long getValidationFailures() {
        return validationFailures.sum();
    }
    
    public double getValidationSuccessRate() {
        long total = getValidationCount();
        return total > 0 ? (double) (total - getValidationFailures()) / total : 0.0;
    }
    
    public long getChildOperations() {
        return childAdditions.sum() + childRemovals.sum() + childReplacements.sum();
    }
    
    public long getCacheOperations() {
        return cacheHits.sum() + cacheMisses.sum();
    }
    
    public double getCacheHitRate() {
        long total = getCacheOperations();
        return total > 0 ? (double) cacheHits.sum() / total : 0.0;
    }
    
    public double getAverageValidationTime() {
        long count = getValidationCount();
        return count > 0 ? (double) totalValidationTime.get() / count : 0.0;
    }
    
    public long getMaxValidationTime() {
        return maxValidationTime.get();
    }
    
    public double getAverageChildSearchTime() {
        // This would need more sophisticated tracking in a real implementation
        return totalChildSearchTime.get() / 1_000_000.0; // Convert to milliseconds
    }
    
    public long getErrorCount() {
        return errorCount.sum();
    }
    
    public Instant getLastErrorTime() {
        long timestamp = lastErrorTime.get();
        return timestamp > 0 ? Instant.ofEpochMilli(timestamp) : null;
    }
    
    // ========== SNAPSHOT METHODS ==========
    
    /**
     * Get a snapshot of current metrics
     */
    public MetricsSnapshot getSnapshot() {
        return new MetricsSnapshot(
            name,
            createdAt,
            getUptime(),
            getCreationCount(),
            getValidationCount(),
            getValidationFailures(),
            getValidationSuccessRate(),
            childAdditions.sum(),
            childRemovals.sum(),
            childReplacements.sum(),
            cacheHits.sum(),
            cacheMisses.sum(),
            cacheEvictions.sum(),
            getCacheHitRate(),
            getAverageValidationTime(),
            getMaxValidationTime(),
            getErrorCount(),
            getLastErrorTime()
        );
    }
    
    /**
     * Reset all metrics
     */
    public void reset() {
        creationCount.reset();
        validationCount.reset();
        validationFailures.reset();
        childAdditions.reset();
        childRemovals.reset();
        childReplacements.reset();
        cacheHits.reset();
        cacheMisses.reset();
        cacheEvictions.reset();
        totalValidationTime.set(0);
        maxValidationTime.set(0);
        totalChildSearchTime.set(0);
        errorCount.reset();
        lastErrorTime.set(0);
    }
    
    /**
     * Get a summary string of key metrics
     */
    public String getSummary() {
        return String.format(
            "MetaDataMetrics[%s]: validations=%d (%.1f%% success), children=%d ops, cache=%.1f%% hit rate, errors=%d",
            name,
            getValidationCount(),
            getValidationSuccessRate() * 100,
            getChildOperations(),
            getCacheHitRate() * 100,
            getErrorCount()
        );
    }
    
    /**
     * Immutable snapshot of metrics at a point in time
     */
    public record MetricsSnapshot(
        String name,
        Instant createdAt,
        Duration uptime,
        long creationCount,
        long validationCount,
        long validationFailures,
        double validationSuccessRate,
        long childAdditions,
        long childRemovals,
        long childReplacements,
        long cacheHits,
        long cacheMisses,
        long cacheEvictions,
        double cacheHitRate,
        double averageValidationTime,
        long maxValidationTime,
        long errorCount,
        Instant lastErrorTime
    ) {
        public long totalChildOperations() {
            return childAdditions + childRemovals + childReplacements;
        }
        
        public long totalCacheOperations() {
            return cacheHits + cacheMisses;
        }
        
        public String toDetailedString() {
            return String.format("""
                MetaData Metrics: %s
                Created: %s (uptime: %s)
                Operations:
                  - Creations: %d
                  - Validations: %d (%d failures, %.1f%% success rate)
                  - Child operations: %d (add: %d, remove: %d, replace: %d)
                  - Cache operations: %d (hits: %d, misses: %d, evictions: %d, %.1f%% hit rate)
                Performance:
                  - Avg validation time: %.2f ms
                  - Max validation time: %d ms
                Errors: %d (last: %s)
                """,
                name,
                createdAt,
                uptime,
                creationCount,
                validationCount, validationFailures, validationSuccessRate * 100,
                totalChildOperations(), childAdditions, childRemovals, childReplacements,
                totalCacheOperations(), cacheHits, cacheMisses, cacheEvictions, cacheHitRate * 100,
                averageValidationTime,
                maxValidationTime,
                errorCount,
                lastErrorTime != null ? lastErrorTime.toString() : "none"
            );
        }
    }
}