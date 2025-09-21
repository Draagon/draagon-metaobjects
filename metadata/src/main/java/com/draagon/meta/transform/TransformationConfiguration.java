package com.draagon.meta.transform;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * TransformationConfiguration defines the behavior and settings for metadata
 * transformation operations. It allows fine-tuning of transformation behavior,
 * performance characteristics, and rule application policies.
 *
 * <h3>Configuration Options:</h3>
 * <ul>
 *   <li><strong>Execution Policy:</strong> How to handle rule failures and execution order</li>
 *   <li><strong>Performance Settings:</strong> Optimization and caching behavior</li>
 *   <li><strong>Rule Filtering:</strong> Which rules to apply and in what circumstances</li>
 *   <li><strong>Output Control:</strong> Logging and reporting preferences</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Default configuration for most use cases
 * TransformationConfiguration config = TransformationConfiguration.defaultConfiguration();
 *
 * // High-performance configuration for large metadata graphs
 * TransformationConfiguration fastConfig = TransformationConfiguration.builder()
 *     .name("high-performance")
 *     .enableCaching(true)
 *     .maxConcurrentRules(4)
 *     .stopOnFirstFailure(false)
 *     .build();
 *
 * // Conservative configuration that stops on any failure
 * TransformationConfiguration safeConfig = TransformationConfiguration.builder()
 *     .name("conservative")
 *     .stopOnFirstFailure(true)
 *     .enableDetailedLogging(true)
 *     .dryRunMode(true)
 *     .build();
 * }</pre>
 *
 * @since 6.1.0
 */
public class TransformationConfiguration {

    private final String name;
    private final boolean stopOnFirstFailure;
    private final boolean enableCaching;
    private final boolean enableDetailedLogging;
    private final boolean dryRunMode;
    private final int maxConcurrentRules;
    private final Set<String> enabledRuleCategories;
    private final Set<String> disabledRules;
    private final Map<String, Object> customProperties;

    /**
     * Create a new TransformationConfiguration
     */
    public TransformationConfiguration(String name,
                                     boolean stopOnFirstFailure,
                                     boolean enableCaching,
                                     boolean enableDetailedLogging,
                                     boolean dryRunMode,
                                     int maxConcurrentRules,
                                     Set<String> enabledRuleCategories,
                                     Set<String> disabledRules,
                                     Map<String, Object> customProperties) {
        this.name = name;
        this.stopOnFirstFailure = stopOnFirstFailure;
        this.enableCaching = enableCaching;
        this.enableDetailedLogging = enableDetailedLogging;
        this.dryRunMode = dryRunMode;
        this.maxConcurrentRules = maxConcurrentRules;
        this.enabledRuleCategories = new HashSet<>(enabledRuleCategories);
        this.disabledRules = new HashSet<>(disabledRules);
        this.customProperties = new HashMap<>(customProperties);
    }

    /**
     * Get the configuration name
     * @return The configuration name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if transformation should stop on the first rule failure
     * @return True if should stop on first failure
     */
    public boolean isStopOnFirstFailure() {
        return stopOnFirstFailure;
    }

    /**
     * Check if caching is enabled for transformation operations
     * @return True if caching is enabled
     */
    public boolean isCachingEnabled() {
        return enableCaching;
    }

    /**
     * Check if detailed logging is enabled
     * @return True if detailed logging is enabled
     */
    public boolean isDetailedLoggingEnabled() {
        return enableDetailedLogging;
    }

    /**
     * Check if this is a dry run (preview only, no actual changes)
     * @return True if in dry run mode
     */
    public boolean isDryRunMode() {
        return dryRunMode;
    }

    /**
     * Get the maximum number of rules that can run concurrently
     * @return The maximum concurrent rules (1 means sequential execution)
     */
    public int getMaxConcurrentRules() {
        return maxConcurrentRules;
    }

    /**
     * Get the set of enabled rule categories
     * @return Set of enabled category names (empty means all categories enabled)
     */
    public Set<String> getEnabledRuleCategories() {
        return new HashSet<>(enabledRuleCategories);
    }

    /**
     * Get the set of disabled rules
     * @return Set of disabled rule names
     */
    public Set<String> getDisabledRules() {
        return new HashSet<>(disabledRules);
    }

    /**
     * Check if a rule category is enabled
     * @param category The rule category name
     * @return True if the category is enabled
     */
    public boolean isCategoryEnabled(String category) {
        return enabledRuleCategories.isEmpty() || enabledRuleCategories.contains(category);
    }

    /**
     * Check if a specific rule is enabled
     * @param ruleName The rule name
     * @return True if the rule is not explicitly disabled
     */
    public boolean isRuleEnabled(String ruleName) {
        return !disabledRules.contains(ruleName);
    }

    /**
     * Get a custom property value
     * @param name The property name
     * @return The property value, or null if not set
     */
    public Object getCustomProperty(String name) {
        return customProperties.get(name);
    }

    /**
     * Get a custom property with a default value
     * @param name The property name
     * @param defaultValue The default value
     * @return The property value or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomProperty(String name, T defaultValue) {
        Object value = customProperties.get(name);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Get all custom properties
     * @return Map of all custom properties
     */
    public Map<String, Object> getAllCustomProperties() {
        return new HashMap<>(customProperties);
    }

    /**
     * Create a default configuration suitable for most transformation scenarios
     * @return A default TransformationConfiguration
     */
    public static TransformationConfiguration defaultConfiguration() {
        return new Builder()
            .name("default")
            .stopOnFirstFailure(false)
            .enableCaching(true)
            .enableDetailedLogging(false)
            .dryRunMode(false)
            .maxConcurrentRules(1)
            .build();
    }

    /**
     * Create a high-performance configuration optimized for large metadata graphs
     * @return A high-performance TransformationConfiguration
     */
    public static TransformationConfiguration highPerformanceConfiguration() {
        return new Builder()
            .name("high-performance")
            .stopOnFirstFailure(false)
            .enableCaching(true)
            .enableDetailedLogging(false)
            .dryRunMode(false)
            .maxConcurrentRules(Runtime.getRuntime().availableProcessors())
            .build();
    }

    /**
     * Create a conservative configuration that enables detailed validation and logging
     * @return A conservative TransformationConfiguration
     */
    public static TransformationConfiguration conservativeConfiguration() {
        return new Builder()
            .name("conservative")
            .stopOnFirstFailure(true)
            .enableCaching(false)
            .enableDetailedLogging(true)
            .dryRunMode(false)
            .maxConcurrentRules(1)
            .build();
    }

    /**
     * Create a preview-only configuration for analyzing potential transformations
     * @return A preview-only TransformationConfiguration
     */
    public static TransformationConfiguration previewConfiguration() {
        return new Builder()
            .name("preview")
            .stopOnFirstFailure(false)
            .enableCaching(false)
            .enableDetailedLogging(true)
            .dryRunMode(true)
            .maxConcurrentRules(1)
            .build();
    }

    /**
     * Create a new builder for creating custom configurations
     * @return A new TransformationConfiguration.Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "TransformationConfiguration{" +
               "name='" + name + '\'' +
               ", stopOnFirstFailure=" + stopOnFirstFailure +
               ", enableCaching=" + enableCaching +
               ", maxConcurrentRules=" + maxConcurrentRules +
               '}';
    }

    /**
     * Builder for creating TransformationConfiguration instances
     */
    public static class Builder {
        private String name = "unnamed";
        private boolean stopOnFirstFailure = false;
        private boolean enableCaching = true;
        private boolean enableDetailedLogging = false;
        private boolean dryRunMode = false;
        private int maxConcurrentRules = 1;
        private final Set<String> enabledRuleCategories = new HashSet<>();
        private final Set<String> disabledRules = new HashSet<>();
        private final Map<String, Object> customProperties = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder stopOnFirstFailure(boolean stopOnFirstFailure) {
            this.stopOnFirstFailure = stopOnFirstFailure;
            return this;
        }

        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }

        public Builder enableDetailedLogging(boolean enableDetailedLogging) {
            this.enableDetailedLogging = enableDetailedLogging;
            return this;
        }

        public Builder dryRunMode(boolean dryRunMode) {
            this.dryRunMode = dryRunMode;
            return this;
        }

        public Builder maxConcurrentRules(int maxConcurrentRules) {
            this.maxConcurrentRules = Math.max(1, maxConcurrentRules);
            return this;
        }

        public Builder enableRuleCategory(String category) {
            this.enabledRuleCategories.add(category);
            return this;
        }

        public Builder disableRule(String ruleName) {
            this.disabledRules.add(ruleName);
            return this;
        }

        public Builder customProperty(String name, Object value) {
            this.customProperties.put(name, value);
            return this;
        }

        public TransformationConfiguration build() {
            return new TransformationConfiguration(
                name,
                stopOnFirstFailure,
                enableCaching,
                enableDetailedLogging,
                dryRunMode,
                maxConcurrentRules,
                enabledRuleCategories,
                disabledRules,
                customProperties
            );
        }
    }
}