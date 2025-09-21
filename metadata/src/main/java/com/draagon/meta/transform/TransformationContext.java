package com.draagon.meta.transform;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.ConstraintViolation;
import com.draagon.meta.constraint.RelationshipConstraintEnforcer;
import com.draagon.meta.constraint.ConstraintRegistry;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * TransformationContext provides all the necessary context and utilities for
 * transformation rules to analyze and modify metadata. It acts as the central
 * hub for transformation operations, providing access to the metadata graph,
 * constraint information, and transformation state.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Metadata Access:</strong> Easy access to the entire metadata graph</li>
 *   <li><strong>Constraint Integration:</strong> Leverage constraint violations to drive transformations</li>
 *   <li><strong>Transformation Tracking:</strong> Track what has been modified during transformation</li>
 *   <li><strong>Preview Mode:</strong> Support for previewing transformations without applying them</li>
 * </ul>
 *
 * <h3>Usage in Transformation Rules:</h3>
 * <pre>{@code
 * public RuleResult apply(TransformationContext context) {
 *     // Find objects that need JPA enhancement
 *     List<MetaObject> objectsNeedingJpa = context.getMetaDataLoader()
 *         .getChildren(MetaObject.class).stream()
 *         .filter(obj -> !obj.hasMetaAttr("skipJpa"))
 *         .collect(Collectors.toList());
 *
 *     // Apply transformations
 *     for (MetaObject obj : objectsNeedingJpa) {
 *         if (context.isPreviewMode()) {
 *             // Just record what would be done
 *         } else {
 *             // Actually apply the transformation
 *             context.recordTransformation(obj, "Added JPA annotations");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @since 6.1.0
 */
public class TransformationContext {

    private final MetaDataLoader loader;
    private final TransformationConfiguration configuration;
    private final Map<String, Object> properties;
    private final Set<MetaData> modifiedMetaData;
    private final List<String> transformationLog;

    // Optional components
    private List<MetaData> targetMetaData;
    private List<ConstraintViolation> constraintViolations;
    private RelationshipConstraintEnforcer constraintEnforcer;
    private boolean previewMode;

    /**
     * Create a new TransformationContext
     * @param loader The MetaDataLoader containing the metadata to transform
     * @param configuration The transformation configuration
     */
    public TransformationContext(MetaDataLoader loader, TransformationConfiguration configuration) {
        this.loader = loader;
        this.configuration = configuration;
        this.properties = new HashMap<>();
        this.modifiedMetaData = new HashSet<>();
        this.transformationLog = new ArrayList<>();
        this.previewMode = false;
    }

    /**
     * Get the MetaDataLoader for this transformation
     * @return The metadata loader
     */
    public MetaDataLoader getMetaDataLoader() {
        return loader;
    }

    /**
     * Get the transformation configuration
     * @return The configuration
     */
    public TransformationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Check if this transformation is running in preview mode
     * @return True if in preview mode (transformations should not be applied)
     */
    public boolean isPreviewMode() {
        return previewMode;
    }

    /**
     * Set whether this transformation is in preview mode
     * @param previewMode True to enable preview mode
     */
    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    /**
     * Get the target metadata for this transformation (if specified)
     * @return List of target metadata, or null if transforming all metadata
     */
    public List<MetaData> getTargetMetaData() {
        return targetMetaData != null ? new ArrayList<>(targetMetaData) : null;
    }

    /**
     * Set specific metadata to target for transformation
     * @param targetMetaData The metadata to target
     */
    public void setTargetMetaData(List<MetaData> targetMetaData) {
        this.targetMetaData = targetMetaData != null ? new ArrayList<>(targetMetaData) : null;
    }

    /**
     * Get all metadata that this transformation should process
     * @return List of metadata to process
     */
    public List<MetaData> getMetaDataToProcess() {
        if (targetMetaData != null) {
            return new ArrayList<>(targetMetaData);
        }

        // Return all metadata from the loader
        List<MetaData> allMetaData = new ArrayList<>();
        allMetaData.addAll(loader.getChildren());

        // Recursively add nested metadata
        for (MetaData child : loader.getChildren()) {
            addNestedMetaData(child, allMetaData);
        }

        return allMetaData;
    }

    /**
     * Get constraint violations for the metadata (computed on-demand)
     * @return List of constraint violations, or empty list if none computed
     */
    public List<ConstraintViolation> getConstraintViolations() {
        if (constraintViolations == null && constraintEnforcer != null) {
            // Compute constraint violations on-demand
            constraintViolations = constraintEnforcer.validateAllRelationships(loader);
        }
        return constraintViolations != null ? new ArrayList<>(constraintViolations) : new ArrayList<>();
    }

    /**
     * Set constraint violations for this transformation
     * @param violations The constraint violations
     */
    public void setConstraintViolations(List<ConstraintViolation> violations) {
        this.constraintViolations = violations != null ? new ArrayList<>(violations) : null;
    }

    /**
     * Get the constraint enforcer for computing violations
     * @return The relationship constraint enforcer, or null if not set
     */
    public RelationshipConstraintEnforcer getConstraintEnforcer() {
        return constraintEnforcer;
    }

    /**
     * Set the constraint enforcer for computing violations
     * @param enforcer The relationship constraint enforcer
     */
    public void setConstraintEnforcer(RelationshipConstraintEnforcer enforcer) {
        this.constraintEnforcer = enforcer;
    }

    /**
     * Record that a transformation was applied to the given metadata
     * @param metaData The metadata that was transformed
     * @param description Description of the transformation
     */
    public void recordTransformation(MetaData metaData, String description) {
        if (!previewMode) {
            modifiedMetaData.add(metaData);
        }
        transformationLog.add(metaData.getName() + ": " + description);
    }

    /**
     * Get all metadata that has been modified during this transformation
     * @return Set of modified metadata
     */
    public Set<MetaData> getModifiedMetaData() {
        return new HashSet<>(modifiedMetaData);
    }

    /**
     * Get the transformation log
     * @return List of transformation descriptions
     */
    public List<String> getTransformationLog() {
        return new ArrayList<>(transformationLog);
    }

    /**
     * Clear the transformation log and modified metadata tracking
     */
    public void clearTransformationHistory() {
        modifiedMetaData.clear();
        transformationLog.clear();
    }

    /**
     * Set a custom property for use by transformation rules
     * @param name The property name
     * @param value The property value
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Get a custom property
     * @param name The property name
     * @return The property value, or null if not set
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Get a custom property with a default value
     * @param name The property name
     * @param defaultValue The default value if property is not set
     * @return The property value or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, T defaultValue) {
        Object value = properties.get(name);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Check if a custom property is set
     * @param name The property name
     * @return True if the property is set
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Get all custom properties
     * @return Map of all properties
     */
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }

    /**
     * Find constraint violations related to a specific metadata object
     * @param metaData The metadata to find violations for
     * @return List of violations affecting this metadata
     */
    public List<ConstraintViolation> getViolationsFor(MetaData metaData) {
        List<ConstraintViolation> violations = getConstraintViolations();
        return violations.stream()
            .filter(violation -> violation.getSourceMetaData().equals(metaData))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Find constraint violations of a specific type
     * @param constraintId The constraint ID to filter by
     * @return List of violations with the specified constraint ID
     */
    public List<ConstraintViolation> getViolationsByConstraintId(String constraintId) {
        List<ConstraintViolation> violations = getConstraintViolations();
        return violations.stream()
            .filter(violation -> violation.getConstraintId().equals(constraintId))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Check if this context has any constraint violations
     * @return True if there are constraint violations
     */
    public boolean hasConstraintViolations() {
        return !getConstraintViolations().isEmpty();
    }

    /**
     * Get the number of transformations recorded
     * @return The transformation count
     */
    public int getTransformationCount() {
        return transformationLog.size();
    }

    /**
     * Check if any transformations have been recorded
     * @return True if transformations have been applied
     */
    public boolean hasTransformations() {
        return !transformationLog.isEmpty();
    }

    /**
     * Create a summary of this transformation context
     * @return A formatted summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TransformationContext Summary:\n");
        summary.append("  Loader: ").append(loader.getName()).append("\n");
        summary.append("  Configuration: ").append(configuration.getName()).append("\n");
        summary.append("  Preview Mode: ").append(previewMode).append("\n");
        summary.append("  Target Metadata: ");
        if (targetMetaData != null) {
            summary.append(targetMetaData.size()).append(" objects");
        } else {
            summary.append("All metadata");
        }
        summary.append("\n");
        summary.append("  Constraint Violations: ").append(getConstraintViolations().size()).append("\n");
        summary.append("  Transformations Applied: ").append(getTransformationCount()).append("\n");
        summary.append("  Modified Objects: ").append(modifiedMetaData.size()).append("\n");
        return summary.toString();
    }

    private void addNestedMetaData(MetaData parent, List<MetaData> collector) {
        for (MetaData child : parent.getChildren()) {
            collector.add(child);
            addNestedMetaData(child, collector);
        }
    }

    @Override
    public String toString() {
        return "TransformationContext{" +
               "loader=" + loader.getName() +
               ", config=" + configuration.getName() +
               ", previewMode=" + previewMode +
               ", transformations=" + getTransformationCount() +
               '}';
    }
}