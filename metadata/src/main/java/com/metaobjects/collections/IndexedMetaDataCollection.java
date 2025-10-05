package com.metaobjects.collections;

import com.metaobjects.MetaData;
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
    
    // DYNAMIC type-specific name indexes for O(1) lookups (supports future types)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, MetaData>> typeNamespaces = new ConcurrentHashMap<>();
    
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

        // Get or create the namespace index for this child type
        ConcurrentHashMap<String, MetaData> typeSpecificIndex =
            typeNamespaces.computeIfAbsent(child.getType(), k -> new ConcurrentHashMap<>());

        // Check if child with same name already exists IN THE APPROPRIATE NAMESPACE
        if (typeSpecificIndex.containsKey(name)) {
            log.debug("Child of type '{}' with name '{}' already exists in namespace", child.getType(), name);
            return false;
        }

        // Add to main collection
        boolean added = children.add(child);

        if (added) {
            // Update type-specific namespace index
            typeSpecificIndex.put(name, child);

            // Update other indices
            updateTypeIndex(child, true);
            updateClassIndex(child, true);

            log.trace("Added child: {} of type {} (total: {})", name, child.getType(), children.size());
        }

        return added;
    }

    /**
     * Get the namespace index for a given type - creates if doesn't exist
     */
    private ConcurrentHashMap<String, MetaData> getNamespaceIndexForType(String type) {
        return typeNamespaces.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
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
            // Remove from type-specific namespace index
            ConcurrentHashMap<String, MetaData> typeSpecificIndex = typeNamespaces.get(child.getType());
            if (typeSpecificIndex != null) {
                typeSpecificIndex.remove(child.getName());
                // Clean up empty namespace
                if (typeSpecificIndex.isEmpty()) {
                    typeNamespaces.remove(child.getType());
                }
            }

            // Update other indices
            updateTypeIndex(child, false);
            updateClassIndex(child, false);

            log.trace("Removed child: {} of type {} (total: {})", child.getName(), child.getType(), children.size());
        }

        return removed;
    }
    
    /**
     * Remove child by name and type
     *
     * @param name The name of the child to remove
     * @param type The type namespace to search in
     * @return The removed child, or null if not found
     */
    public MetaData removeByNameAndType(String name, String type) {
        ConcurrentHashMap<String, MetaData> typeSpecificIndex = typeNamespaces.get(type);
        if (typeSpecificIndex != null) {
            MetaData child = typeSpecificIndex.get(name);
            if (child != null && remove(child)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Replace an existing child (by name and type) with a new one
     *
     * @param newChild The new child to replace with
     * @return The previous child, or null if none existed
     */
    public MetaData replace(MetaData newChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("New child cannot be null");
        }

        String name = newChild.getName();
        String type = newChild.getType();

        // Look for existing child in the same type namespace
        ConcurrentHashMap<String, MetaData> typeSpecificIndex = typeNamespaces.get(type);
        MetaData oldChild = null;
        if (typeSpecificIndex != null) {
            oldChild = typeSpecificIndex.get(name);
        }

        if (oldChild != null) {
            // Remove old child
            remove(oldChild);
        }

        // Add new child
        add(newChild);

        return oldChild;
    }
    

    /**
     * Find child by name and type - O(1) operation using type-specific namespace
     *
     * @param name The name to search for
     * @param type The type namespace to search in
     * @return Optional containing the child if found
     */
    public Optional<MetaData> findByNameAndType(String name, String type) {
        ConcurrentHashMap<String, MetaData> typeSpecificIndex = getNamespaceIndexForType(type);
        return Optional.ofNullable(typeSpecificIndex.get(name));
    }

    /**
     * Find child by name across ALL type namespaces - O(n) operation where n is number of types
     * This method searches all type namespaces and returns the first match found.
     *
     * @param name The name to search for
     * @return Optional containing the child if found in any namespace
     */
    public Optional<MetaData> findByName(String name) {
        // Search across all type namespaces
        for (ConcurrentHashMap<String, MetaData> typeNamespace : typeNamespaces.values()) {
            MetaData found = typeNamespace.get(name);
            if (found != null) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    /**
     * Get all supported types (namespaces) in this collection
     *
     * @return Set of all type names that have been registered
     */
    public Set<String> getSupportedTypes() {
        return typeNamespaces.keySet();
    }

    /**
     * Get all children of a specific type
     *
     * @param type The type to get children for
     * @return Collection of all children of the specified type
     */
    public Collection<MetaData> getAllByType(String type) {
        ConcurrentHashMap<String, MetaData> typeSpecificIndex = typeNamespaces.get(type);
        return typeSpecificIndex != null ? typeSpecificIndex.values() : Collections.emptyList();
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
     * Check if a child with the given name and type exists
     *
     * @param name The name to check
     * @param type The type namespace to check in
     * @return true if a child with the name exists in the type namespace
     */
    public boolean containsNameAndType(String name, String type) {
        ConcurrentHashMap<String, MetaData> typeSpecificIndex = typeNamespaces.get(type);
        return typeSpecificIndex != null && typeSpecificIndex.containsKey(name);
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

        // Clear dynamic type-specific namespace indexes
        typeNamespaces.clear();

        // Clear other indexes
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
        int totalNamespaceEntries = typeNamespaces.values().stream()
            .mapToInt(Map::size).sum();

        return new CollectionStats(
            children.size(),
            totalNamespaceEntries,
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
        // Clear all indexes
        typeNamespaces.clear();
        typeIndex.clear();
        classIndex.clear();

        // Rebuild all indexes
        for (MetaData child : children) {
            // Update type-specific namespace index
            ConcurrentHashMap<String, MetaData> typeSpecificIndex = getNamespaceIndexForType(child.getType());
            typeSpecificIndex.put(child.getName(), child);

            // Update other indexes
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
        int totalNamespaceEntries,
        int typeIndexSize,
        int classIndexSize,
        int totalTypeIndexEntries,
        int totalClassIndexEntries
    ) {
        public boolean isConsistent() {
            return totalChildren == totalNamespaceEntries;
        }
    }
}