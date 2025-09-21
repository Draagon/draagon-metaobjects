package com.draagon.meta.transform;

import java.util.Map;
import java.util.HashMap;

/**
 * TransformationStats provides statistical information about a MetaDataTransformer's
 * configuration and capabilities. This is useful for monitoring, debugging, and
 * optimizing transformation performance.
 *
 * <h3>Metrics Provided:</h3>
 * <ul>
 *   <li><strong>Rule Counts:</strong> Total rules and distribution by priority</li>
 *   <li><strong>Configuration Info:</strong> Current configuration settings</li>
 *   <li><strong>Capability Analysis:</strong> What types of transformations are available</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Get transformer statistics
 * TransformationStats stats = transformer.getStats();
 *
 * log.info("Transformer has {} rules configured", stats.getTotalRules());
 * log.info("High priority rules: {}", stats.getRulesByPriority().get("High (700-899)"));
 * log.info("Configuration: {}", stats.getConfigurationName());
 * }</pre>
 *
 * @since 6.1.0
 */
public class TransformationStats {

    private final int totalRules;
    private final Map<String, Integer> rulesByPriority;
    private final String configurationName;
    private final long timestamp;

    /**
     * Create new TransformationStats
     * @param totalRules Total number of rules configured
     * @param rulesByPriority Distribution of rules by priority range
     * @param configurationName Name of the current configuration
     */
    public TransformationStats(int totalRules,
                              Map<String, Integer> rulesByPriority,
                              String configurationName) {
        this.totalRules = totalRules;
        this.rulesByPriority = new HashMap<>(rulesByPriority);
        this.configurationName = configurationName;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the total number of transformation rules configured
     * @return The total rule count
     */
    public int getTotalRules() {
        return totalRules;
    }

    /**
     * Get the distribution of rules by priority range
     * @return Map of priority ranges to rule counts
     */
    public Map<String, Integer> getRulesByPriority() {
        return new HashMap<>(rulesByPriority);
    }

    /**
     * Get the name of the current transformation configuration
     * @return The configuration name
     */
    public String getConfigurationName() {
        return configurationName;
    }

    /**
     * Get the timestamp when these statistics were generated
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Check if the transformer has any rules configured
     * @return True if rules are available
     */
    public boolean hasRules() {
        return totalRules > 0;
    }

    /**
     * Get the number of high-priority rules (priority >= 700)
     * @return The high-priority rule count
     */
    public int getHighPriorityRules() {
        return rulesByPriority.getOrDefault("Critical (900+)", 0) +
               rulesByPriority.getOrDefault("High (700-899)", 0);
    }

    /**
     * Get the number of medium-priority rules (priority 500-699)
     * @return The medium-priority rule count
     */
    public int getMediumPriorityRules() {
        return rulesByPriority.getOrDefault("Medium (500-699)", 0);
    }

    /**
     * Get the number of low-priority rules (priority < 500)
     * @return The low-priority rule count
     */
    public int getLowPriorityRules() {
        return rulesByPriority.getOrDefault("Low (300-499)", 0) +
               rulesByPriority.getOrDefault("Minimal (0-299)", 0);
    }

    /**
     * Get a formatted summary of these statistics
     * @return A human-readable summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Transformation Statistics Summary:\n");
        summary.append("  Configuration: ").append(configurationName).append("\n");
        summary.append("  Total Rules: ").append(totalRules).append("\n");
        summary.append("  High Priority Rules: ").append(getHighPriorityRules()).append("\n");
        summary.append("  Medium Priority Rules: ").append(getMediumPriorityRules()).append("\n");
        summary.append("  Low Priority Rules: ").append(getLowPriorityRules()).append("\n");

        if (!rulesByPriority.isEmpty()) {
            summary.append("\n  Detailed Priority Distribution:\n");
            for (Map.Entry<String, Integer> entry : rulesByPriority.entrySet()) {
                summary.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return "TransformationStats{" +
               "totalRules=" + totalRules +
               ", configuration='" + configurationName + '\'' +
               ", highPriority=" + getHighPriorityRules() +
               ", mediumPriority=" + getMediumPriorityRules() +
               ", lowPriority=" + getLowPriorityRules() +
               '}';
    }
}