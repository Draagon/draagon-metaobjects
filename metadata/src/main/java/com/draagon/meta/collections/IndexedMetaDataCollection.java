package com.draagon.meta.collections;

import com.draagon.meta.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Thread-safe, indexed collection for MetaData children that provides
 * O(1) lookups by name while maintaining insertion order.
 * 
 * This replaces the simple CopyOnWriteArrayList with a more sophisticated
 * collection that provides both performance and thread safety.
 */
public class IndexedMetaDataCollection {
    
    private static final Logger log = LoggerFactory.getLogger(IndexedMetaDataCollection.class);
    
    // Main storage - preserves insertion order and provides thread safety
    private final CopyOnWriteArrayList<MetaData> children = new CopyOnWriteArrayList<>();
    
    // Name index for O(1) lookups
    private final ConcurrentHashMap<String, MetaData> nameIndex = new ConcurrentHashMap<>();
    
    // Type index for efficient type-based queries
    private final ConcurrentHashMap<String, List<MetaData>> typeIndex = new ConcurrentHashMap<>();
    
    // Class index for efficient class-based queries
    private final ConcurrentHashMap<Class<? extends MetaData>, List<MetaData>> classIndex = new ConcurrentHashMap<>();
    
    /**
     * Add a MetaData child to the collection
     * 
     * @param child The child to add
     * @return true if the child was added, false if already exists
     */
    public boolean add(MetaData child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        
        String name = child.getName();
        
        // Check if child with same name already exists
        if (nameIndex.containsKey(name)) {
            log.debug("Child with name '{}' already exists", name);
            return false;
        }
        
        // Add to main collection
        boolean added = children.add(child);
        
        if (added) {
            // Update indices
            nameIndex.put(name, child);
            updateTypeIndex(child, true);
            updateClassIndex(child, true);
            
            log.trace("Added child: {} (total: {})", name, children.size());
        }
        
        return added;
    }
    
    /**
     * Remove a MetaData child from the collection
     * 
     * @param child The child to remove
     * @return true if the child was removed
     */
    public boolean remove(MetaData child) {
        if (child == null) {
            return false;
        }
        
        boolean removed = children.remove(child);
        
        if (removed) {
            // Update indices
            nameIndex.remove(child.getName());
            updateTypeIndex(child, false);
            updateClassIndex(child, false);
            
            log.trace("Removed child: {} (total: {})", child.getName(), children.size());
        }
        
        return removed;
    }
    
    /**
     * Remove child by name
     * 
     * @param name The name of the child to remove
     * @return The removed child, or null if not found
     */
    public MetaData removeByName(String name) {
        MetaData child = nameIndex.get(name);
        if (child != null && remove(child)) {
            return child;
        }
        return null;
    }
    
    /**
     * Replace an existing child (by name) with a new one
     * 
     * @param newChild The new child to replace with
     * @return The previous child, or null if none existed
     */
    public MetaData replace(MetaData newChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("New child cannot be null");
        }
        
        String name = newChild.getName();
        MetaData oldChild = nameIndex.get(name);
        
        if (oldChild != null) {
            // Remove old child
            remove(oldChild);
        }
        
        // Add new child
        add(newChild);
        
        return oldChild;
    }
    
    /**
     * Find child by name - O(1) operation
     * 
     * @param name The name to search for
     * @return Optional containing the child if found
     */
    public Optional<MetaData> findByName(String name) {
        return Optional.ofNullable(nameIndex.get(name));
    }
    
    /**
     * Find children by type - O(1) operation for common types
     * 
     * @param typeName The type name to search for
     * @return List of children with the specified type
     */
    public List<MetaData> findByType(String typeName) {
        return typeIndex.getOrDefault(typeName, Collections.emptyList());
    }
    
    /**
     * Find children by class - O(1) operation for common classes
     * 
     * @param clazz The class to search for
     * @return List of children of the specified class
     */
    @SuppressWarnings("unchecked")
    public <T extends MetaData> List<T> findByClass(Class<T> clazz) {
        List<MetaData> found = classIndex.getOrDefault(clazz, Collections.emptyList());
        return (List<T>) found;
    }

    /**
     * Find children by class with type safety - filters and casts safely
     * 
     * @param clazz The class to search for
     * @return List of children guaranteed to be of the specified class
     */
    public <T extends MetaData> List<T> findByClassSafe(Class<T> clazz) {
        List<MetaData> found = classIndex.getOrDefault(clazz, Collections.emptyList());
        return found.stream()
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Find children matching a predicate
     * 
     * @param predicate The predicate to match
     * @return Stream of matching children
     */
    public Stream<MetaData> findMatching(Predicate<MetaData> predicate) {
        return children.stream().filter(predicate);
    }
    
    /**
     * Check if a child with the given name exists
     * 
     * @param name The name to check
     * @return true if a child with the name exists
     */
    public boolean containsName(String name) {
        return nameIndex.containsKey(name);
    }
    
    /**
     * Check if the collection contains the specified child
     * 
     * @param child The child to check for
     * @return true if the child exists in the collection
     */
    public boolean contains(MetaData child) {
        return children.contains(child);
    }
    
    /**
     * Get all children as an immutable list
     * 
     * @return Immutable list of all children
     */
    public List<MetaData> getAll() {
        return List.copyOf(children);
    }
    
    /**
     * Get all children as a stream
     * 
     * @return Stream of all children
     */
    public Stream<MetaData> stream() {
        return children.stream();
    }
    
    /**
     * Get the number of children
     * 
     * @return The size of the collection
     */
    public int size() {
        return children.size();
    }
    
    /**
     * Check if the collection is empty
     * 
     * @return true if the collection has no children
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }
    
    /**
     * Clear all children and indices
     */
    public void clear() {
        children.clear();
        nameIndex.clear();
        typeIndex.clear();
        classIndex.clear();
        
        log.debug("Cleared indexed collection");
    }
    
    /**
     * Get collection statistics for monitoring
     * 
     * @return CollectionStats record
     */
    public CollectionStats getStats() {
        return new CollectionStats(
            children.size(),
            nameIndex.size(),
            typeIndex.size(),
            classIndex.size(),
            typeIndex.values().stream().mapToInt(List::size).sum(),
            classIndex.values().stream().mapToInt(List::size).sum()
        );
    }
    
    /**
     * Update the type index when adding/removing children
     */
    private void updateTypeIndex(MetaData child, boolean add) {
        String typeName = child.getType();
        
        if (add) {
            typeIndex.computeIfAbsent(typeName, k -> new CopyOnWriteArrayList<>()).add(child);
        } else {
            List<MetaData> typeList = typeIndex.get(typeName);
            if (typeList != null) {
                typeList.remove(child);
                if (typeList.isEmpty()) {
                    typeIndex.remove(typeName);
                }
            }
        }
    }
    
    /**
     * Update the class index when adding/removing children
     */
    private void updateClassIndex(MetaData child, boolean add) {
        Class<? extends MetaData> clazz = child.getClass();
        
        if (add) {
            classIndex.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>()).add(child);
        } else {
            List<MetaData> classList = classIndex.get(clazz);
            if (classList != null) {
                classList.remove(child);
                if (classList.isEmpty()) {
                    classIndex.remove(clazz);
                }
            }
        }
    }
    
    /**
     * Rebuild indices from the current children list
     * This can be called if indices become inconsistent
     */
    public void rebuildIndices() {
        nameIndex.clear();
        typeIndex.clear();
        classIndex.clear();
        
        for (MetaData child : children) {
            nameIndex.put(child.getName(), child);
            updateTypeIndex(child, true);
            updateClassIndex(child, true);
        }
        
        log.debug("Rebuilt indices for {} children", children.size());
    }
    
    /**
     * Collection statistics record
     */
    public record CollectionStats(
        int totalChildren,
        int nameIndexSize,
        int typeIndexSize,
        int classIndexSize,
        int totalTypeIndexEntries,
        int totalClassIndexEntries
    ) {
        public boolean isConsistent() {
            return totalChildren == nameIndexSize;
        }
    }
}