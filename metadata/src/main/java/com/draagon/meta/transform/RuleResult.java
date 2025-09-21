package com.draagon.meta.transform;

import com.draagon.meta.MetaData;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * RuleResult represents the outcome of applying a single transformation rule.
 * It provides detailed information about what was transformed, any issues
 * encountered, and performance metrics.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Successful transformation
 * RuleResult success = new RuleResult(true, "Added JPA annotations to 5 objects");
 * success.addTransformation("User", "Added @Entity and @Table annotations");
 * success.addTransformation("Product", "Added @Id annotation to id field");
 *
 * // Failed transformation
 * RuleResult failure = new RuleResult(false, "Missing required dependencies");
 * failure.addIssue("JPA libraries not found in classpath");
 * }</pre>
 *
 * @since 6.1.0
 */
public class RuleResult {

    private final boolean success;
    private final String message;
    private final List<TransformationDetail> transformations;
    private final List<String> issues;
    private final Map<String, Object> metrics;
    private final long executionTimeMs;

    /**
     * Create a new RuleResult
     * @param success Whether the rule application was successful
     * @param message A descriptive message about the result
     */
    public RuleResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.transformations = new ArrayList<>();
        this.issues = new ArrayList<>();
        this.metrics = new HashMap<>();
        this.executionTimeMs = 0;
    }

    /**
     * Create a new RuleResult with execution time
     * @param success Whether the rule application was successful
     * @param message A descriptive message about the result
     * @param executionTimeMs How long the rule took to execute in milliseconds
     */
    public RuleResult(boolean success, String message, long executionTimeMs) {
        this.success = success;
        this.message = message;
        this.transformations = new ArrayList<>();
        this.issues = new ArrayList<>();
        this.metrics = new HashMap<>();
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Check if the rule application was successful
     * @return True if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the result message
     * @return The result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the number of transformations that were applied
     * @return The transformation count
     */
    public int getTransformationsApplied() {
        return transformations.size();
    }

    /**
     * Get detailed information about all transformations applied
     * @return List of transformation details
     */
    public List<TransformationDetail> getTransformations() {
        return new ArrayList<>(transformations);
    }

    /**
     * Add a transformation detail to this result
     * @param targetName The name of the metadata that was transformed
     * @param description Description of what was transformed
     */
    public void addTransformation(String targetName, String description) {
        transformations.add(new TransformationDetail(targetName, description));
    }

    /**
     * Add a transformation detail with the target metadata object
     * @param target The metadata object that was transformed
     * @param description Description of what was transformed
     */
    public void addTransformation(MetaData target, String description) {
        transformations.add(new TransformationDetail(target.getName(), description, target));
    }

    /**
     * Get any issues encountered during rule application
     * @return List of issue descriptions
     */
    public List<String> getIssues() {
        return new ArrayList<>(issues);
    }

    /**
     * Add an issue to this result
     * @param issue Description of the issue encountered
     */
    public void addIssue(String issue) {
        issues.add(issue);
    }

    /**
     * Get performance and other metrics from rule execution
     * @return Map of metric names to values
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    /**
     * Add a metric to this result
     * @param name The metric name
     * @param value The metric value
     */
    public void addMetric(String name, Object value) {
        metrics.put(name, value);
    }

    /**
     * Get the execution time for this rule
     * @return Execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Check if this result has any issues
     * @return True if issues were encountered
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    /**
     * Check if any transformations were applied
     * @return True if transformations were applied
     */
    public boolean hasTransformations() {
        return !transformations.isEmpty();
    }

    /**
     * Get a summary of this result for logging
     * @return A formatted summary string
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("RuleResult{");
        summary.append("success=").append(success);
        summary.append(", transformations=").append(transformations.size());
        summary.append(", issues=").append(issues.size());
        summary.append(", executionTime=").append(executionTimeMs).append("ms");
        summary.append("}");
        return summary.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }

    /**
     * Builder for creating RuleResult instances
     */
    public static class Builder {
        private boolean success = true;
        private String message = "";
        private long executionTimeMs = 0;
        private final List<TransformationDetail> transformations = new ArrayList<>();
        private final List<String> issues = new ArrayList<>();
        private final Map<String, Object> metrics = new HashMap<>();

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder executionTime(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder addTransformation(String targetName, String description) {
            transformations.add(new TransformationDetail(targetName, description));
            return this;
        }

        public Builder addTransformation(MetaData target, String description) {
            transformations.add(new TransformationDetail(target.getName(), description, target));
            return this;
        }

        public Builder addIssue(String issue) {
            issues.add(issue);
            return this;
        }

        public Builder addMetric(String name, Object value) {
            metrics.put(name, value);
            return this;
        }

        public RuleResult build() {
            RuleResult result = new RuleResult(success, message, executionTimeMs);
            transformations.forEach(t -> result.transformations.add(t));
            issues.forEach(i -> result.issues.add(i));
            metrics.forEach((k, v) -> result.metrics.put(k, v));
            return result;
        }
    }

    /**
     * Detailed information about a specific transformation that was applied
     */
    public static class TransformationDetail {
        private final String targetName;
        private final String description;
        private final MetaData targetMetaData;
        private final long timestamp;

        public TransformationDetail(String targetName, String description) {
            this(targetName, description, null);
        }

        public TransformationDetail(String targetName, String description, MetaData targetMetaData) {
            this.targetName = targetName;
            this.description = description;
            this.targetMetaData = targetMetaData;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTargetName() {
            return targetName;
        }

        public String getDescription() {
            return description;
        }

        public MetaData getTargetMetaData() {
            return targetMetaData;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "TransformationDetail{" +
                   "target='" + targetName + '\'' +
                   ", description='" + description + '\'' +
                   '}';
        }
    }
}