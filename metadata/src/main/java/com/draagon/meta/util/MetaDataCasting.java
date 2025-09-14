package com.draagon.meta.util;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;

import java.util.*;
import java.util.stream.Stream;

/**
 * Type-safe casting utilities for MetaData objects.
 * Provides centralized safe casting with better error messages.
 */
public final class MetaDataCasting {
    
    private MetaDataCasting() {} // Utility class
    
    /**
     * Safely cast MetaData to a specific type
     * @param source The source MetaData object
     * @param target The target class
     * @return Optional containing the cast object if successful, empty otherwise
     */
    public static <T extends MetaData> Optional<T> safeCast(MetaData source, Class<T> target) {
        if (source == null || target == null) {
            return Optional.empty();
        }
        return target.isInstance(source) 
            ? Optional.of(target.cast(source)) 
            : Optional.empty();
    }
    
    /**
     * Require a cast, throwing an exception with detailed context if it fails
     * @param source The source MetaData object  
     * @param target The target class
     * @return The cast object
     * @throws MetaDataException if the cast fails
     */
    public static <T extends MetaData> T requireCast(MetaData source, Class<T> target) {
        if (source == null) {
            throw new MetaDataException(
                String.format("Cannot cast null to %s", target.getSimpleName()));
        }
        
        return safeCast(source, target)
            .orElseThrow(() -> new MetaDataException(
                String.format("Expected %s but got %s at %s", 
                    target.getSimpleName(), 
                    source.getClass().getSimpleName(),
                    buildMetaDataPath(source))));
    }
    
    /**
     * Filter a collection of MetaData by type, returning a stream of the filtered type
     * @param source Collection of MetaData objects
     * @param target The target type to filter by
     * @return Stream of objects matching the target type
     */
    public static <T extends MetaData> Stream<T> filterByType(Collection<MetaData> source, Class<T> target) {
        if (source == null || target == null) {
            return Stream.empty();
        }
        return source.stream()
            .filter(Objects::nonNull)
            .filter(target::isInstance)
            .map(target::cast);
    }
    
    /**
     * Filter a stream of MetaData by type
     * @param source Stream of MetaData objects
     * @param target The target type to filter by
     * @return Stream of objects matching the target type
     */
    public static <T extends MetaData> Stream<T> filterByType(Stream<MetaData> source, Class<T> target) {
        if (source == null || target == null) {
            return Stream.empty();
        }
        return source
            .filter(Objects::nonNull)
            .filter(target::isInstance)
            .map(target::cast);
    }
    
    /**
     * Check if an object can be cast to the target type
     * @param source The source object
     * @param target The target class
     * @return true if the cast would succeed
     */
    public static <T extends MetaData> boolean canCast(MetaData source, Class<T> target) {
        return source != null && target != null && target.isInstance(source);
    }
    
    /**
     * Build a human-readable path for a MetaData object for error messages
     * @param metaData The MetaData object
     * @return A string representing the path from root to this object
     */
    public static String buildMetaDataPath(MetaData metaData) {
        if (metaData == null) {
            return "<null>";
        }
        
        List<String> pathComponents = new ArrayList<>();
        MetaData current = metaData;
        
        // Avoid infinite loops by tracking visited objects
        Set<MetaData> visited = new HashSet<>();
        
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            
            String typeName = current.getTypeName() != null ? current.getTypeName() : "unknown";
            String name = current.getName() != null ? current.getName() : "unnamed";
            pathComponents.add(typeName + ":" + name);
            
            current = current.getParent();
        }
        
        if (pathComponents.isEmpty()) {
            return "<unknown>";
        }
        
        Collections.reverse(pathComponents);
        return String.join(" -> ", pathComponents);
    }
    
    /**
     * Cast a list of MetaData objects to a specific type, filtering out non-matching items
     * @param source List of MetaData objects
     * @param target The target type
     * @return List containing only objects that match the target type
     */
    public static <T extends MetaData> List<T> castList(List<MetaData> source, Class<T> target) {
        if (source == null || target == null) {
            return new ArrayList<>();
        }
        
        return filterByType(source, target)
            .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }
    
    /**
     * Find the first object in a collection that matches the target type
     * @param source Collection of MetaData objects
     * @param target The target type
     * @return Optional containing the first matching object
     */
    public static <T extends MetaData> Optional<T> findFirst(Collection<MetaData> source, Class<T> target) {
        return filterByType(source, target).findFirst();
    }
}