package com.draagon.meta.transform;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * TransformationResult aggregates the results of applying multiple transformation rules
 * to a metadata graph. It provides comprehensive reporting on what was transformed,
 * performance metrics, and any issues encountered.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // After transformation
 * TransformationResult result = transformer.transform(loader);
 *
 * if (result.isSuccess()) {
 *     log.info("Applied {} transformations in {}ms",
 *              result.getTotalTransformations(),
 *              result.getTotalExecutionTime());
 * } else {
 *     log.error("Transformation failed: {}", result.getFailureReasons());
 * }
 * }</pre>
 *
 * @since 6.1.0
 */
public class TransformationResult {

    private final Map<String, RuleResult> ruleResults;
    private final long startTime;
    private long endTime;
    private boolean complete;

    /**
     * Create a new TransformationResult
     */
    public TransformationResult() {
        this.ruleResults = new HashMap<>();
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime;
        this.complete = false;
    }

    /**
     * Add a rule result to this transformation result
     * @param ruleName The name of the rule
     * @param ruleResult The result of applying the rule
     */
    public void addRuleResult(String ruleName, RuleResult ruleResult) {
        ruleResults.put(ruleName, ruleResult);
    }

    /**
     * Mark this transformation as complete
     * @param complete Whether the transformation is complete
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
        if (complete) {
            this.endTime = System.currentTimeMillis();
        }
    }

    /**
     * Check if the overall transformation was successful
     * @return True if all rules succeeded or if partial success is acceptable
     */
    public boolean isSuccess() {
        if (!complete) {
            return false;
        }

        // Success if at least one rule succeeded and no critical failures
        boolean hasSuccess = ruleResults.values().stream().anyMatch(RuleResult::isSuccess);
        boolean hasCriticalFailure = ruleResults.values().stream()
            .anyMatch(result -> !result.isSuccess() && result.getIssues().stream()
                .anyMatch(issue -> issue.toLowerCase().contains("critical")));

        return hasSuccess && !hasCriticalFailure;
    }

    /**
     * Check if the transformation completed (successfully or not)
     * @return True if the transformation finished
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Get the total number of transformations applied across all rules
     * @return The total transformation count
     */
    public int getTotalTransformations() {
        return ruleResults.values().stream()
            .mapToInt(RuleResult::getTransformationsApplied)
            .sum();
    }

    /**
     * Get the total execution time for all rules
     * @return Total execution time in milliseconds
     */
    public long getTotalExecutionTime() {
        return endTime - startTime;
    }

    /**
     * Get the number of rules that were applied
     * @return The number of rules
     */
    public int getRulesApplied() {
        return ruleResults.size();
    }

    /**
     * Get the number of rules that succeeded
     * @return The number of successful rules
     */
    public int getSuccessfulRules() {
        return (int) ruleResults.values().stream()
            .filter(RuleResult::isSuccess)
            .count();
    }

    /**
     * Get the number of rules that failed
     * @return The number of failed rules
     */
    public int getFailedRules() {
        return (int) ruleResults.values().stream()
            .filter(result -> !result.isSuccess())
            .count();
    }

    /**
     * Get results for all rules that were applied
     * @return Map of rule names to their results
     */
    public Map<String, RuleResult> getAllRuleResults() {
        return new HashMap<>(ruleResults);
    }

    /**
     * Get the result for a specific rule
     * @param ruleName The name of the rule
     * @return The rule result, or null if the rule wasn't applied
     */
    public RuleResult getRuleResult(String ruleName) {
        return ruleResults.get(ruleName);
    }

    /**
     * Get results only for rules that succeeded
     * @return Map of successful rule names to their results
     */
    public Map<String, RuleResult> getSuccessfulRuleResults() {
        return ruleResults.entrySet().stream()
            .filter(entry -> entry.getValue().isSuccess())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get results only for rules that failed
     * @return Map of failed rule names to their results
     */
    public Map<String, RuleResult> getFailedRuleResults() {
        return ruleResults.entrySet().stream()
            .filter(entry -> !entry.getValue().isSuccess())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get a list of all issues encountered during transformation
     * @return List of issue descriptions
     */
    public List<String> getAllIssues() {
        return ruleResults.values().stream()
            .flatMap(result -> result.getIssues().stream())
            .collect(Collectors.toList());
    }

    /**
     * Get failure reasons for rules that didn't succeed
     * @return List of failure reason descriptions
     */
    public List<String> getFailureReasons() {
        return ruleResults.entrySet().stream()
            .filter(entry -> !entry.getValue().isSuccess())
            .map(entry -> entry.getKey() + ": " + entry.getValue().getMessage())
            .collect(Collectors.toList());
    }

    /**
     * Get detailed transformation information across all rules
     * @return List of all transformation details
     */
    public List<RuleResult.TransformationDetail> getAllTransformationDetails() {
        return ruleResults.values().stream()
            .flatMap(result -> result.getTransformations().stream())
            .collect(Collectors.toList());
    }

    /**
     * Get performance metrics aggregated across all rules
     * @return Map of aggregated metrics
     */
    public Map<String, Object> getAggregatedMetrics() {
        Map<String, Object> aggregated = new HashMap<>();

        aggregated.put("totalRules", getRulesApplied());
        aggregated.put("successfulRules", getSuccessfulRules());
        aggregated.put("failedRules", getFailedRules());
        aggregated.put("totalTransformations", getTotalTransformations());
        aggregated.put("totalExecutionTimeMs", getTotalExecutionTime());
        aggregated.put("avgTransformationsPerRule",
            getRulesApplied() > 0 ? (double) getTotalTransformations() / getRulesApplied() : 0);
        aggregated.put("avgExecutionTimePerRule",
            getRulesApplied() > 0 ? (double) getTotalExecutionTime() / getRulesApplied() : 0);

        return aggregated;
    }

    /**
     * Get a formatted summary of this transformation result
     * @return A human-readable summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("TransformationResult Summary:\n");
        summary.append("  Status: ").append(isSuccess() ? "SUCCESS" : "FAILURE").append("\n");
        summary.append("  Rules Applied: ").append(getRulesApplied()).append("\n");
        summary.append("  Successful Rules: ").append(getSuccessfulRules()).append("\n");
        summary.append("  Failed Rules: ").append(getFailedRules()).append("\n");
        summary.append("  Total Transformations: ").append(getTotalTransformations()).append("\n");
        summary.append("  Execution Time: ").append(getTotalExecutionTime()).append("ms\n");

        if (getFailedRules() > 0) {
            summary.append("  Failure Reasons:\n");
            for (String reason : getFailureReasons()) {
                summary.append("    - ").append(reason).append("\n");
            }
        }

        return summary.toString();
    }

    /**
     * Get a detailed report including all rule results and transformations
     * @return A comprehensive report
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append(getSummary());
        report.append("\nDetailed Rule Results:\n");

        for (Map.Entry<String, RuleResult> entry : ruleResults.entrySet()) {
            String ruleName = entry.getKey();
            RuleResult result = entry.getValue();

            report.append("  ").append(ruleName).append(":\n");
            report.append("    Status: ").append(result.isSuccess() ? "SUCCESS" : "FAILURE").append("\n");
            report.append("    Message: ").append(result.getMessage()).append("\n");
            report.append("    Transformations: ").append(result.getTransformationsApplied()).append("\n");
            report.append("    Execution Time: ").append(result.getExecutionTimeMs()).append("ms\n");

            if (result.hasIssues()) {
                report.append("    Issues:\n");
                for (String issue : result.getIssues()) {
                    report.append("      - ").append(issue).append("\n");
                }
            }

            if (result.hasTransformations()) {
                report.append("    Transformations Applied:\n");
                for (RuleResult.TransformationDetail detail : result.getTransformations()) {
                    report.append("      - ").append(detail.getTargetName())
                          .append(": ").append(detail.getDescription()).append("\n");
                }
            }

            report.append("\n");
        }

        return report.toString();
    }

    @Override
    public String toString() {
        return "TransformationResult{" +
               "rules=" + getRulesApplied() +
               ", success=" + isSuccess() +
               ", transformations=" + getTotalTransformations() +
               ", executionTime=" + getTotalExecutionTime() + "ms" +
               '}';
    }
}