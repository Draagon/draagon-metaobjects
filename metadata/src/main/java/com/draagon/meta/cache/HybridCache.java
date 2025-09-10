package com.draagon.meta.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Hybrid cache implementation that maintains both legacy WeakHashMap
 * and modern ConcurrentHashMap for backward compatibility while
 * providing enhanced performance and thread safety.
 */
public class HybridCache implements CacheStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(HybridCache.class);
    
    // Legacy cache for backward compatibility
    private final Map<Object, Object> legacyCache = Collections.synchronizedMap(new WeakHashMap<>());
    
    // Modern cache for enhanced performance
    private final Map<String, Object> modernCache = new ConcurrentHashMap<>();
    
    // Cache statistics
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong loadCount = new AtomicLong();
    
    /**
     * Get cached value with type safety
     */
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        // Check modern cache first
        Object value = modernCache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            return value != null && type.isInstance(value) ? 
                Optional.of(type.cast(value)) : 
                Optional.empty();
        }
        
        // Fallback to legacy cache for compatibility
        value = legacyCache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            // Promote to modern cache
            modernCache.put(key, value);
            return value != null && type.isInstance(value) ? 
                Optional.of(type.cast(value)) : 
                Optional.empty();
        }
        
        missCount.incrementAndGet();
        return Optional.empty();
    }
    
    /**
     * Get cached value as Object (legacy compatibility)
     */
    @Override
    public Object get(Object key) {
        String stringKey = String.valueOf(key);
        
        // Check modern cache first
        Object value = modernCache.get(stringKey);
        if (value != null) {
            hitCount.incrementAndGet();
            return value;
        }
        
        // Check legacy cache
        value = legacyCache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            // Promote to modern cache
            modernCache.put(stringKey, value);
            return value;
        }
        
        missCount.incrementAndGet();
        return null;
    }
    
    /**
     * Store value in both caches for compatibility
     */
    @Override
    public void put(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        
        modernCache.put(key, value);
        legacyCache.put(key, value);
        
        log.trace("Cached value for key: {}", key);
    }
    
    /**
     * Store value in cache (legacy method)
     */
    @Override
    public void put(Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        
        String stringKey = String.valueOf(key);
        modernCache.put(stringKey, value);
        legacyCache.put(key, value);
        
        log.trace("Cached value for legacy key: {}", key);
    }
    
    /**
     * Compute and cache value if absent
     */
    @Override
    public <T> T computeIfAbsent(String key, Class<T> type, Supplier<T> supplier) {
        // Check if already exists
        Optional<T> existing = get(key, type);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Compute new value
        T value = supplier.get();
        if (value != null) {
            put(key, value);
            loadCount.incrementAndGet();
        }
        
        return value;
    }
    
    /**
     * Remove from both caches
     */
    @Override
    public Object remove(String key) {
        Object modernValue = modernCache.remove(key);
        Object legacyValue = legacyCache.remove(key);
        
        // Return the modern value if available, otherwise legacy
        return modernValue != null ? modernValue : legacyValue;
    }
    
    /**
     * Remove from both caches (legacy method)
     */
    @Override
    public Object remove(Object key) {
        String stringKey = String.valueOf(key);
        Object modernValue = modernCache.remove(stringKey);
        Object legacyValue = legacyCache.remove(key);
        
        return modernValue != null ? modernValue : legacyValue;
    }
    
    /**
     * Check if key exists in either cache
     */
    @Override
    public boolean containsKey(String key) {
        return modernCache.containsKey(key) || legacyCache.containsKey(key);
    }
    
    /**
     * Check if key exists in either cache (legacy method)
     */
    @Override
    public boolean containsKey(Object key) {
        String stringKey = String.valueOf(key);
        return modernCache.containsKey(stringKey) || legacyCache.containsKey(key);
    }
    
    /**
     * Clear both caches
     */
    @Override
    public void clear() {
        modernCache.clear();
        legacyCache.clear();
        
        // Reset statistics
        hitCount.set(0);
        missCount.set(0);
        loadCount.set(0);
        
        log.debug("Cleared hybrid cache");
    }
    
    /**
     * Get size (primarily from modern cache)
     */
    @Override
    public int size() {
        // Return modern cache size as primary, legacy as fallback
        int modernSize = modernCache.size();
        int legacySize = legacyCache.size();
        
        // Return the larger of the two to account for items that may be in one but not the other
        return Math.max(modernSize, legacySize);
    }
    
    /**
     * Check if both caches are empty
     */
    @Override
    public boolean isEmpty() {
        return modernCache.isEmpty() && legacyCache.isEmpty();
    }
    
    /**
     * Get key set from modern cache
     */
    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>(modernCache.keySet());
        
        // Add legacy keys converted to strings
        legacyCache.keySet().forEach(key -> keys.add(String.valueOf(key)));
        
        return keys;
    }
    
    /**
     * Get cache statistics
     */
    @Override
    public Optional<CacheStats> getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long loads = loadCount.get();
        long total = hits + misses;
        
        double hitRate = total > 0 ? (double) hits / total : 0.0;
        double loadRate = total > 0 ? (double) loads / total : 0.0;
        
        return Optional.of(new CacheStats(
            hits, 
            misses, 
            loads, 
            0, // evictionCount not tracked in this implementation
            hitRate, 
            loadRate
        ));
    }
    
    /**
     * Promote legacy cache entries to modern cache
     * This can be called periodically to optimize performance
     */
    public void promoteLegacyEntries() {
        synchronized (legacyCache) {
            legacyCache.entrySet().forEach(entry -> {
                String key = String.valueOf(entry.getKey());
                if (!modernCache.containsKey(key)) {
                    modernCache.put(key, entry.getValue());
                }
            });
        }
        
        log.debug("Promoted legacy cache entries to modern cache");
    }
    
    /**
     * Get detailed cache information for debugging
     */
    public String getCacheInfo() {
        return String.format("HybridCache[modern=%d, legacy=%d, hitRate=%.2f%%]", 
            modernCache.size(), 
            legacyCache.size(), 
            getStats().map(s -> s.hitRate() * 100).orElse(0.0));
    }
}