package com.draagon.meta.transform;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * TransformationPreview provides a preview of what transformations would be applied
 * without actually modifying the metadata. This is useful for analysis, reporting,
 * and user confirmation before applying potentially destructive changes.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Generate a preview of transformations
 * TransformationPreview preview = transformer.preview(loader);
 *
 * // Check what would be transformed
 * if (preview.getPotentialTransformations() > 0) {
 *     log.info("Would apply {} transformations across {} rules",
 *              preview.getPotentialTransformations(),
 *              preview.getApplicableRules());
 *
 *     // Show details to user for confirmation
 *     for (String ruleName : preview.getRuleNames()) {
 *         RuleResult result = preview.getRulePreview(ruleName);
 *         log.info("Rule '{}' would apply {} transformations",
 *                  ruleName, result.getTransformationsApplied());
 *     }
 * }
 * }</pre>
 *
 * @since 6.1.0
 */
public class TransformationPreview {

    private final Map<String, RuleResult> rulePreviewResults;
    private final long generationTime;

    /**
     * Create a new TransformationPreview
     */
    public TransformationPreview() {
        this.rulePreviewResults = new HashMap<>();
        this.generationTime = System.currentTimeMillis();
    }

    /**
     * Add a rule preview result
     * @param ruleName The name of the rule
     * @param previewResult The preview result for the rule
     */
    public void addRulePreview(String ruleName, RuleResult previewResult) {
        rulePreviewResults.put(ruleName, previewResult);
    }

    /**
     * Get the preview result for a specific rule
     * @param ruleName The rule name
     * @return The preview result, or null if rule wasn't analyzed
     */
    public RuleResult getRulePreview(String ruleName) {
        return rulePreviewResults.get(ruleName);
    }

    /**
     * Get all rule names that have preview results
     * @return List of rule names
     */
    public List<String> getRuleNames() {
        return new ArrayList<>(rulePreviewResults.keySet());
    }

    /**
     * Get all rule preview results
     * @return Map of rule names to preview results
     */
    public Map<String, RuleResult> getAllRulePreviews() {
        return new HashMap<>(rulePreviewResults);
    }

    /**
     * Get the number of rules that would be applicable
     * @return The number of rules that would apply transformations
     */
    public int getApplicableRules() {
        return (int) rulePreviewResults.values().stream()
            .filter(result -> result.getTransformationsApplied() > 0)
            .count();
    }

    /**
     * Get the total number of potential transformations across all rules
     * @return The total potential transformation count
     */
    public int getPotentialTransformations() {
        return rulePreviewResults.values().stream()
            .mapToInt(RuleResult::getTransformationsApplied)
            .sum();
    }

    /**
     * Get rules that would succeed
     * @return Map of successful rule names to their preview results
     */
    public Map<String, RuleResult> getSuccessfulRulePreviews() {
        Map<String, RuleResult> successful = new HashMap<>();
        for (Map.Entry<String, RuleResult> entry : rulePreviewResults.entrySet()) {
            if (entry.getValue().isSuccess()) {
                successful.put(entry.getKey(), entry.getValue());
            }
        }
        return successful;
    }

    /**
     * Get rules that would fail
     * @return Map of failing rule names to their preview results
     */
    public Map<String, RuleResult> getFailingRulePreviews() {
        Map<String, RuleResult> failing = new HashMap<>();
        for (Map.Entry<String, RuleResult> entry : rulePreviewResults.entrySet()) {
            if (!entry.getValue().isSuccess()) {
                failing.put(entry.getKey(), entry.getValue());
            }
        }
        return failing;
    }

    /**
     * Get potential issues that would be encountered
     * @return List of all potential issues across all rules
     */
    public List<String> getPotentialIssues() {
        List<String> allIssues = new ArrayList<>();
        for (RuleResult result : rulePreviewResults.values()) {
            allIssues.addAll(result.getIssues());
        }
        return allIssues;
    }

    /**
     * Get detailed transformation information that would be applied
     * @return List of all potential transformation details
     */
    public List<RuleResult.TransformationDetail> getPotentialTransformationDetails() {
        List<RuleResult.TransformationDetail> allDetails = new ArrayList<>();
        for (RuleResult result : rulePreviewResults.values()) {
            allDetails.addAll(result.getTransformations());
        }
        return allDetails;
    }

    /**
     * Check if any transformations would be applied
     * @return True if any rule would apply transformations
     */
    public boolean hasTransformations() {
        return getPotentialTransformations() > 0;
    }

    /**
     * Check if any rules would encounter issues
     * @return True if any rule would have issues
     */
    public boolean hasIssues() {
        return rulePreviewResults.values().stream()
            .anyMatch(result -> !result.getIssues().isEmpty());
    }

    /**
     * Get the time when this preview was generated
     * @return Generation timestamp in milliseconds
     */
    public long getGenerationTime() {
        return generationTime;
    }

    /**
     * Get a formatted summary of this preview
     * @return A human-readable summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Transformation Preview Summary:\n");
        summary.append("  Total Rules Analyzed: ").append(rulePreviewResults.size()).append("\n");
        summary.append("  Applicable Rules: ").append(getApplicableRules()).append("\n");
        summary.append("  Potential Transformations: ").append(getPotentialTransformations()).append("\n");
        summary.append("  Successful Rules: ").append(getSuccessfulRulePreviews().size()).append("\n");
        summary.append("  Failing Rules: ").append(getFailingRulePreviews().size()).append("\n");
        summary.append("  Potential Issues: ").append(getPotentialIssues().size()).append("\n");

        if (hasIssues()) {
            summary.append("\n  Potential Issues:\n");
            for (String issue : getPotentialIssues()) {
                summary.append("    - ").append(issue).append("\n");
            }
        }

        return summary.toString();
    }

    /**
     * Get a detailed report of all rule previews
     * @return A comprehensive preview report
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append(getSummary());
        report.append("\nDetailed Rule Previews:\n");

        for (Map.Entry<String, RuleResult> entry : rulePreviewResults.entrySet()) {
            String ruleName = entry.getKey();
            RuleResult result = entry.getValue();

            report.append("  ").append(ruleName).append(":\n");
            report.append("    Would Succeed: ").append(result.isSuccess()).append("\n");
            report.append("    Potential Transformations: ").append(result.getTransformationsApplied()).append("\n");

            if (result.hasIssues()) {
                report.append("    Potential Issues:\n");
                for (String issue : result.getIssues()) {
                    report.append("      - ").append(issue).append("\n");
                }
            }

            if (result.hasTransformations()) {
                report.append("    Transformations That Would Be Applied:\n");
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
        return "TransformationPreview{" +
               "rules=" + rulePreviewResults.size() +
               ", applicableRules=" + getApplicableRules() +
               ", potentialTransformations=" + getPotentialTransformations() +
               '}';
    }
}