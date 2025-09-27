package com.metaobjects.cache;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Unified caching strategy interface that provides type-safe caching
 * operations for MetaData objects. This replaces the dual cache system
 * with a consistent, performant approach.
 */
public interface CacheStrategy {
    
    /**
     * Get a cached value with type safety
     * 
     * @param key The cache key
     * @param type The expected type of the cached value
     * @return Optional containing the cached value if present and of correct type
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Get a cached value as Object (for legacy compatibility)
     * 
     * @param key The cache key
     * @return The cached value or null if not present
     */
    Object get(Object key);
    
    /**
     * Store a value in the cache
     * 
     * @param key The cache key
     * @param value The value to cache
     */
    void put(String key, Object value);
    
    /**
     * Store a value in the cache (legacy method)
     * 
     * @param key The cache key (converted to string)
     * @param value The value to cache
     */
    void put(Object key, Object value);
    
    /**
     * Compute and cache a value if absent
     * 
     * @param key The cache key
     * @param type The expected type
     * @param supplier Supplier to compute the value if not present
     * @return The cached or computed value
     */
    <T> T computeIfAbsent(String key, Class<T> type, Supplier<T> supplier);
    
    /**
     * Remove a value from the cache
     * 
     * @param key The cache key
     * @return The removed value, or null if not present
     */
    Object remove(String key);
    
    /**
     * Remove a value from the cache (legacy method)
     * 
     * @param key The cache key
     * @return The removed value, or null if not present
     */
    Object remove(Object key);
    
    /**
     * Check if the cache contains the specified key
     * 
     * @param key The cache key
     * @return true if the key exists in the cache
     */
    boolean containsKey(String key);
    
    /**
     * Check if the cache contains the specified key (legacy method)
     * 
     * @param key The cache key
     * @return true if the key exists in the cache
     */
    boolean containsKey(Object key);
    
    /**
     * Clear all cache entries
     */
    void clear();
    
    /**
     * Get the current size of the cache
     * 
     * @return The number of entries in the cache
     */
    int size();
    
    /**
     * Check if the cache is empty
     * 
     * @return true if the cache has no entries
     */
    boolean isEmpty();
    
    /**
     * Get all cache keys
     * 
     * @return Set of all keys in the cache
     */
    Set<String> keySet();
    
    /**
     * Get cache statistics if available
     * 
     * @return Optional containing cache statistics
     */
    Optional<CacheStats> getStats();
    
    /**
     * Cache statistics record
     */
    record CacheStats(
        long hitCount,
        long missCount,
        long loadCount,
        long evictionCount,
        double hitRate,
        double loadRate
    ) {
        public static CacheStats empty() {
            return new CacheStats(0, 0, 0, 0, 0.0, 0.0);
        }
    }
}