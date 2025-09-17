package com.draagon.meta.util;

import com.draagon.meta.MetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for building and representing hierarchical paths through MetaData structures.
 * Provides human-readable representations of MetaData object locations within their hierarchy.
 * 
 * <p>Example usage:</p>
 * <pre>
 * MetaDataPath path = MetaDataPath.buildPath(someMetaField);
 * String hierarchical = path.toHierarchicalString(); // "object:User(domain) → field:email(string)"
 * String simple = path.toSimpleString(); // "User.email"
 * </pre>
 * 
 * @since 5.2.0
 */
public final class MetaDataPath {
    private final List<PathSegment> segments;

    /**
     * Represents a single segment in a MetaData path.
     */
    public static final class PathSegment {
        private final String type;
        private final String subType;
        private final String name;

        /**
         * Creates a new path segment.
         * 
         * @param type the MetaData type (e.g., "field", "object", "validator")
         * @param subType the MetaData subtype (e.g., "string", "int", "required")
         * @param name the MetaData name (e.g., "firstName", "User", "emailValidator")
         */
        public PathSegment(String type, String subType, String name) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.subType = subType; // Allow null for subtypes
            this.name = Objects.requireNonNull(name, "name cannot be null");
        }

        /**
         * Returns the type of this path segment.
         * @return the type (never null)
         */
        public String getType() {
            return type;
        }

        /**
         * Returns the subtype of this path segment.
         * @return the subtype (may be null)
         */
        public String getSubType() {
            return subType;
        }

        /**
         * Returns the name of this path segment.
         * @return the name (never null)
         */
        public String getName() {
            return name;
        }

        /**
         * Creates a display string representation of this segment.
         * Format: "type:name(subType)" or "type:name" if subType is null
         * 
         * @return display string representation
         */
        public String toDisplayString() {
            if (subType != null && !subType.trim().isEmpty()) {
                return String.format("%s:%s(%s)", type, name, subType);
            } else {
                return String.format("%s:%s", type, name);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PathSegment that = (PathSegment) obj;
            return Objects.equals(type, that.type) &&
                   Objects.equals(subType, that.subType) &&
                   Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, subType, name);
        }

        @Override
        public String toString() {
            return toDisplayString();
        }
    }

    /**
     * Creates a MetaDataPath with the given segments.
     * 
     * @param segments the path segments (copied defensively)
     */
    private MetaDataPath(List<PathSegment> segments) {
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Builds a hierarchical path from a MetaData object by traversing up to the root.
     * 
     * @param target the MetaData object to build a path for
     * @return a MetaDataPath representing the hierarchy to the target
     * @throws IllegalArgumentException if target is null
     */
    public static MetaDataPath buildPath(MetaData target) {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }

        List<PathSegment> segments = new ArrayList<>();
        MetaData current = target;

        while (current != null) {
            segments.add(new PathSegment(
                current.getTypeName(),
                current.getSubTypeName(),
                current.getName()
            ));
            current = current.getParent();
        }

        // Reverse to get root-to-target order
        Collections.reverse(segments);
        return new MetaDataPath(segments);
    }

    /**
     * Creates an empty path.
     * 
     * @return an empty MetaDataPath
     */
    public static MetaDataPath empty() {
        return new MetaDataPath(Collections.emptyList());
    }

    /**
     * Returns the segments of this path.
     * 
     * @return an unmodifiable list of path segments
     */
    public List<PathSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Returns true if this path is empty (has no segments).
     * 
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Returns the number of segments in this path.
     * 
     * @return the segment count
     */
    public int size() {
        return segments.size();
    }

    /**
     * Returns the last (leaf) segment of this path.
     * 
     * @return the last segment, or null if the path is empty
     */
    public PathSegment getLeaf() {
        return segments.isEmpty() ? null : segments.get(segments.size() - 1);
    }

    /**
     * Returns the first (root) segment of this path.
     * 
     * @return the first segment, or null if the path is empty
     */
    public PathSegment getRoot() {
        return segments.isEmpty() ? null : segments.get(0);
    }

    /**
     * Creates a hierarchical string representation using arrow separators.
     * Example: "object:User(domain) → field:email(string) → validator:required(simple)"
     * 
     * @return hierarchical string representation
     */
    public String toHierarchicalString() {
        return segments.stream()
                .map(PathSegment::toDisplayString)
                .collect(Collectors.joining(" → "));
    }

    /**
     * Creates a simple dot-separated string representation using only names.
     * Example: "User.email.required"
     * 
     * @return simple string representation
     */
    public String toSimpleString() {
        return segments.stream()
                .map(PathSegment::getName)
                .collect(Collectors.joining("."));
    }

    /**
     * Creates a type-only string representation.
     * Example: "object.field.validator"
     * 
     * @return type-only string representation
     */
    public String toTypeString() {
        return segments.stream()
                .map(PathSegment::getType)
                .collect(Collectors.joining("."));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MetaDataPath that = (MetaDataPath) obj;
        return Objects.equals(segments, that.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments);
    }

    @Override
    public String toString() {
        return toHierarchicalString();
    }
}