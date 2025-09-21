package com.draagon.meta.transform;

import com.draagon.meta.MetaData;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.constraint.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * MetaDataTransformer provides intelligent transformation and enhancement of metadata
 * based on constraint violations, business rules, and transformation patterns.
 *
 * <p>This transformer can automatically:</p>
 * <ul>
 *   <li><strong>Fix Constraint Violations:</strong> Automatically resolve common constraint failures</li>
 *   <li><strong>Enhance Metadata:</strong> Add missing attributes, relationships, and annotations</li>
 *   <li><strong>Normalize Structure:</strong> Apply consistent patterns across metadata graphs</li>
 *   <li><strong>Infer Relationships:</strong> Automatically detect and create missing relationships</li>
 * </ul>
 *
 * <h3>Transformation Patterns:</h3>
 * <pre>{@code
 * // Auto-enhance JPA metadata
 * transformer.addRule(new JpaEnhancementRule());
 *
 * // Fix inheritance violations
 * transformer.addRule(new InheritanceCompletionRule());
 *
 * // Apply transformations
 * TransformationResult result = transformer.transform(loader);
 * }</pre>
 *
 * @since 6.1.0
 */
public class MetaDataTransformer {

    private static final Logger log = LoggerFactory.getLogger(MetaDataTransformer.class);

    private final List<TransformationRule> transformationRules;
    private final TransformationConfiguration configuration;

    /**
     * Create a new MetaDataTransformer with default configuration
     */
    public MetaDataTransformer() {
        this(TransformationConfiguration.defaultConfiguration());
    }

    /**
     * Create a new MetaDataTransformer with custom configuration
     * @param configuration The transformation configuration
     */
    public MetaDataTransformer(TransformationConfiguration configuration) {
        this.transformationRules = new ArrayList<>();
        this.configuration = configuration;
        log.info("MetaDataTransformer initialized with configuration: {}", configuration.getName());
    }

    /**
     * Add a transformation rule to this transformer
     * @param rule The transformation rule to add
     */
    public void addRule(TransformationRule rule) {
        transformationRules.add(rule);
        log.debug("Added transformation rule: {} (Priority: {})", rule.getName(), rule.getPriority());
    }

    /**
     * Remove a transformation rule from this transformer
     * @param rule The transformation rule to remove
     * @return True if the rule was removed
     */
    public boolean removeRule(TransformationRule rule) {
        boolean removed = transformationRules.remove(rule);
        if (removed) {
            log.debug("Removed transformation rule: {}", rule.getName());
        }
        return removed;
    }

    /**
     * Transform the entire metadata graph in the given loader
     * @param loader The MetaDataLoader containing metadata to transform
     * @return TransformationResult with details of all transformations applied
     */
    public TransformationResult transform(MetaDataLoader loader) {
        log.info("Starting metadata transformation for loader: {}", loader.getName());

        TransformationContext context = new TransformationContext(loader, configuration);
        TransformationResult result = new TransformationResult();

        // Sort rules by priority (higher priority first)
        List<TransformationRule> sortedRules = new ArrayList<>(transformationRules);
        sortedRules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));

        log.debug("Applying {} transformation rules in priority order", sortedRules.size());

        // Apply each transformation rule
        for (TransformationRule rule : sortedRules) {
            try {
                log.debug("Applying transformation rule: {}", rule.getName());

                RuleResult ruleResult = rule.apply(context);
                result.addRuleResult(rule.getName(), ruleResult);

                if (ruleResult.getTransformationsApplied() > 0) {
                    log.info("Rule '{}' applied {} transformations",
                            rule.getName(), ruleResult.getTransformationsApplied());
                }

                // Check if we should stop on first failure
                if (!ruleResult.isSuccess() && configuration.isStopOnFirstFailure()) {
                    log.warn("Stopping transformation due to rule failure: {}", rule.getName());
                    break;
                }

            } catch (Exception e) {
                log.error("Error applying transformation rule '{}': {}", rule.getName(), e.getMessage(), e);

                RuleResult errorResult = new RuleResult(false, "Rule execution failed: " + e.getMessage());
                result.addRuleResult(rule.getName(), errorResult);

                if (configuration.isStopOnFirstFailure()) {
                    break;
                }
            }
        }

        result.setComplete(true);
        log.info("Metadata transformation complete. Applied {} total transformations",
                result.getTotalTransformations());

        return result;
    }

    /**
     * Transform specific metadata objects
     * @param metaDataList The list of MetaData objects to transform
     * @param loader The MetaDataLoader for context
     * @return TransformationResult with details of transformations applied
     */
    public TransformationResult transform(List<MetaData> metaDataList, MetaDataLoader loader) {
        log.info("Starting targeted metadata transformation for {} objects", metaDataList.size());

        TransformationContext context = new TransformationContext(loader, configuration);
        context.setTargetMetaData(metaDataList);

        TransformationResult result = new TransformationResult();

        // Apply transformation rules to the specific metadata
        for (TransformationRule rule : transformationRules) {
            try {
                if (rule.isApplicableTo(metaDataList)) {
                    log.debug("Applying rule '{}' to {} target objects", rule.getName(), metaDataList.size());

                    RuleResult ruleResult = rule.apply(context);
                    result.addRuleResult(rule.getName(), ruleResult);
                }
            } catch (Exception e) {
                log.error("Error applying rule '{}' to targets: {}", rule.getName(), e.getMessage(), e);
                result.addRuleResult(rule.getName(),
                    new RuleResult(false, "Rule execution failed: " + e.getMessage()));
            }
        }

        result.setComplete(true);
        return result;
    }

    /**
     * Preview transformations without applying them
     * @param loader The MetaDataLoader to analyze
     * @return TransformationPreview showing what would be transformed
     */
    public TransformationPreview preview(MetaDataLoader loader) {
        log.info("Generating transformation preview for loader: {}", loader.getName());

        TransformationContext context = new TransformationContext(loader, configuration);
        context.setPreviewMode(true);

        TransformationPreview preview = new TransformationPreview();

        for (TransformationRule rule : transformationRules) {
            try {
                RuleResult previewResult = rule.apply(context);
                preview.addRulePreview(rule.getName(), previewResult);
            } catch (Exception e) {
                log.warn("Error generating preview for rule '{}': {}", rule.getName(), e.getMessage());
            }
        }

        log.info("Transformation preview complete. {} rules would apply {} total transformations",
                preview.getApplicableRules(), preview.getPotentialTransformations());

        return preview;
    }

    /**
     * Get statistics about the current transformation configuration
     * @return TransformationStats with current transformer state
     */
    public TransformationStats getStats() {
        Map<String, Integer> rulesByPriority = new HashMap<>();
        for (TransformationRule rule : transformationRules) {
            String priorityRange = getPriorityRange(rule.getPriority());
            rulesByPriority.merge(priorityRange, 1, Integer::sum);
        }

        return new TransformationStats(
            transformationRules.size(),
            rulesByPriority,
            configuration.getName()
        );
    }

    /**
     * Get the list of transformation rules currently configured
     * @return List of transformation rules
     */
    public List<TransformationRule> getTransformationRules() {
        return new ArrayList<>(transformationRules);
    }

    /**
     * Get the current transformation configuration
     * @return The transformation configuration
     */
    public TransformationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Clear all transformation rules
     */
    public void clearRules() {
        int ruleCount = transformationRules.size();
        transformationRules.clear();
        log.info("Cleared {} transformation rules", ruleCount);
    }

    /**
     * Check if this transformer has any rules configured
     * @return True if rules are configured
     */
    public boolean hasRules() {
        return !transformationRules.isEmpty();
    }

    private String getPriorityRange(int priority) {
        if (priority >= 900) return "Critical (900+)";
        if (priority >= 700) return "High (700-899)";
        if (priority >= 500) return "Medium (500-699)";
        if (priority >= 300) return "Low (300-499)";
        return "Minimal (0-299)";
    }

    @Override
    public String toString() {
        return "MetaDataTransformer{" +
               "rules=" + transformationRules.size() +
               ", config='" + configuration.getName() + '\'' +
               '}';
    }

    /**
     * Builder for creating configured MetaDataTransformer instances
     */
    public static class Builder {
        private final TransformationConfiguration configuration;
        private final List<TransformationRule> rules = new ArrayList<>();

        public Builder() {
            this.configuration = TransformationConfiguration.defaultConfiguration();
        }

        public Builder(TransformationConfiguration configuration) {
            this.configuration = configuration;
        }

        public Builder addRule(TransformationRule rule) {
            rules.add(rule);
            return this;
        }

        public Builder addJpaEnhancements() {
            rules.add(new JpaEnhancementRule());
            return this;
        }

        public Builder addInheritanceCompletion() {
            rules.add(new InheritanceCompletionRule());
            return this;
        }

        public Builder addConstraintResolution() {
            rules.add(new ConstraintResolutionRule());
            return this;
        }

        public MetaDataTransformer build() {
            MetaDataTransformer transformer = new MetaDataTransformer(configuration);
            for (TransformationRule rule : rules) {
                transformer.addRule(rule);
            }
            return transformer;
        }
    }
}