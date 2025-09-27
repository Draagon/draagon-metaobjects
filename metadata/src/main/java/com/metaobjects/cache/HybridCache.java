package com.metaobjects.cache;

import com.metaobjects.MetaData;
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
 * 
 * <p>Optimized for permanent object references (like MetaData objects) with:</p>
 * <ul>
 *   <li>Object identity-based keys for MetaData objects</li>
 *   <li>String interning for frequently used string keys</li>
 *   <li>Dual cache strategy for OSGI compatibility</li>
 *   <li>Enhanced performance for read-heavy workloads</li>
 * </ul>
 */
public class HybridCache implements CacheStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(HybridCache.class);
    
    // Legacy cache for backward compatibility (WeakHashMap for OSGI)
    private final Map<Object, Object> legacyCache = Collections.synchronizedMap(new WeakHashMap<>());
    
    // Modern cache for enhanced performance (permanent references)
    private final Map<String, Object> modernCache = new ConcurrentHashMap<>();
    
    // Object identity-based cache for permanent MetaData objects
    private final Map<Object, Object> identityCache = new ConcurrentHashMap<>();
    
    // String interning cache for frequently used keys
    private final Map<String, String> internedKeys = new ConcurrentHashMap<>();
    
    // Cache statistics
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong loadCount = new AtomicLong();
    private final AtomicLong identityHitCount = new AtomicLong();
    private final AtomicLong internHitCount = new AtomicLong();
    
    /**
     * Optimizes string keys by interning frequently used ones.
     * Reduces memory usage for repeated cache keys.
     */
    private String optimizeKey(String key) {
        if (key == null) return null;
        
        // Intern frequently used keys to reduce memory usage
        String interned = internedKeys.get(key);
        if (interned == null) {
            // Only intern if this appears to be a method/field name pattern
            if (isFrequentlyUsedKey(key)) {
                interned = key.intern();
                internedKeys.put(key, interned);
                internHitCount.incrementAndGet();
            } else {
                interned = key;
            }
        } else {
            internHitCount.incrementAndGet();
        }
        
        return interned;
    }
    
    /**
     * Determines if a key should be interned based on patterns.
     */
    private boolean isFrequentlyUsedKey(String key) {
        // Intern keys that look like method calls or field access patterns
        return key.contains("getMetaField(") || 
               key.contains("getMetaObject(") ||
               key.contains("hasAttribute(") ||
               key.contains(".") ||
               key.length() < 32; // Short keys are likely to be repeated
    }
    
    /**
     * Uses object identity for permanent objects like MetaData.
     * This is more efficient than string conversion for permanent references.
     */
    private boolean useIdentityCache(Object key) {
        return key instanceof MetaData || 
               (key != null && key.getClass().getName().contains("MetaData"));
    }
    
    /**
     * Get cached value with type safety
     */
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String optimizedKey = optimizeKey(key);
        
        // Check modern cache first
        Object value = modernCache.get(optimizedKey);
        if (value != null) {
            hitCount.incrementAndGet();
            return value != null && type.isInstance(value) ? 
                Optional.of(type.cast(value)) : 
                Optional.empty();
        }
        
        // Fallback to legacy cache for compatibility
        value = legacyCache.get(optimizedKey);
        if (value != null) {
            hitCount.incrementAndGet();
            // Promote to modern cache using optimized key
            modernCache.put(optimizedKey, value);
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
        // Use identity cache for permanent objects like MetaData
        if (useIdentityCache(key)) {
            Object value = identityCache.get(key);
            if (value != null) {
                identityHitCount.incrementAndGet();
                hitCount.incrementAndGet();
                return value;
            }
        }
        
        String stringKey = optimizeKey(String.valueOf(key));
        
        // Check modern cache
        Object value = modernCache.get(stringKey);
        if (value != null) {
            hitCount.incrementAndGet();
            return value;
        }
        
        // Check legacy cache
        value = legacyCache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            
            // Promote to appropriate cache based on key type
            if (useIdentityCache(key)) {
                identityCache.put(key, value);
            } else {
                modernCache.put(stringKey, value);
            }
            
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
        
        String optimizedKey = optimizeKey(key);
        modernCache.put(optimizedKey, value);
        legacyCache.put(optimizedKey, value);
        
        log.trace("Cached value for key: {}", optimizedKey);
    }
    
    /**
     * Store value in cache (legacy method)
     */
    @Override
    public void put(Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        
        // Use identity cache for permanent objects
        if (useIdentityCache(key)) {
            identityCache.put(key, value);
            // Also store in legacy cache for compatibility
            legacyCache.put(key, value);
            log.trace("Cached value in identity cache for key: {}", key);
        } else {
            String stringKey = optimizeKey(String.valueOf(key));
            modernCache.put(stringKey, value);
            legacyCache.put(key, value);
            log.trace("Cached value for legacy key: {}", key);
        }
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
     * Clear all caches
     */
    @Override
    public void clear() {
        modernCache.clear();
        legacyCache.clear();
        identityCache.clear();
        internedKeys.clear();
        
        // Reset statistics
        hitCount.set(0);
        missCount.set(0);
        loadCount.set(0);
        identityHitCount.set(0);
        internHitCount.set(0);
        
        log.debug("Cleared hybrid cache (all cache types)");
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
        return String.format("HybridCache[modern=%d, legacy=%d, identity=%d, interned=%d, hitRate=%.2f%%, identityHits=%d, internHits=%d]", 
            modernCache.size(), 
            legacyCache.size(),
            identityCache.size(),
            internedKeys.size(),
            getStats().map(s -> s.hitRate() * 100).orElse(0.0),
            identityHitCount.get(),
            internHitCount.get());
    }
    
    /**
     * Get optimization statistics
     */
    public OptimizationStats getOptimizationStats() {
        return new OptimizationStats(
            identityHitCount.get(),
            internHitCount.get(),
            identityCache.size(),
            internedKeys.size()
        );
    }
    
    /**
     * Statistics for cache optimization features
     */
    public record OptimizationStats(
        long identityHits,
        long internHits,
        int identityCacheSize,
        int internedKeysSize
    ) {}
    
    /**
     * Manually optimize cache by promoting frequently accessed entries
     * and cleaning up unnecessary interned keys
     */
    public void optimize() {
        promoteLegacyEntries();
        
        // Clean up interned keys that are no longer being used
        if (internedKeys.size() > 1000) {
            // Keep only the most recent 500 interned keys
            Set<String> toRemove = new HashSet<>();
            internedKeys.keySet().stream()
                .skip(500)
                .forEach(toRemove::add);
            toRemove.forEach(internedKeys::remove);
            
            log.debug("Cleaned up {} unused interned keys", toRemove.size());
        }
        
        log.debug("Cache optimization completed: {}", getCacheInfo());
    }
}