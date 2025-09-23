package com.draagon.meta.registry;

import java.util.*;

/**
 * Health report for MetaDataRegistry consistency and architectural compliance.
 *
 * <p>This class provides validation results and recommendations for registry state,
 * including base type consistency, inheritance patterns, and structural issues.</p>
 *
 * <p>The report uses different severity levels:</p>
 * <ul>
 *   <li><strong>Errors</strong>: Structural problems that should cause build failures</li>
 *   <li><strong>Warnings</strong>: Architectural inconsistencies that should be addressed</li>
 *   <li><strong>Recommendations</strong>: Best practice suggestions for improvement</li>
 * </ul>
 *
 * @since 6.2.0
 */
public class RegistryHealthReport {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Add a structural error that should cause build failure
     *
     * @param error Error description
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Add an architectural warning that should be addressed
     *
     * @param warning Warning description
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Add a best practice recommendation
     *
     * @param recommendation Recommendation description
     */
    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    /**
     * Add metadata about the registry state
     *
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Check if there are any structural errors
     *
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are any warnings
     *
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Check if there are any recommendations
     *
     * @return true if recommendations exist
     */
    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    /**
     * Check if registry is structurally sound (no errors)
     *
     * @return true if no structural errors exist
     */
    public boolean isStructurallySound() {
        return errors.isEmpty();
    }

    /**
     * Check if registry follows best practices (no warnings or recommendations)
     *
     * @return true if no warnings or recommendations exist
     */
    public boolean followsBestPractices() {
        return warnings.isEmpty() && recommendations.isEmpty();
    }

    /**
     * Get all errors
     *
     * @return Unmodifiable list of errors
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Get all warnings
     *
     * @return Unmodifiable list of warnings
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Get all recommendations
     *
     * @return Unmodifiable list of recommendations
     */
    public List<String> getRecommendations() {
        return Collections.unmodifiableList(recommendations);
    }

    /**
     * Get metadata about the registry state
     *
     * @return Unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Get a specific metadata value
     *
     * @param key Metadata key
     * @return Metadata value or null if not found
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Get types missing base subtypes (convenience method)
     *
     * @return Set of type names missing base subtypes
     */
    @SuppressWarnings("unchecked")
    public Set<String> getMissingBases() {
        Object missing = metadata.get("missingBaseTypes");
        return missing instanceof Set ? (Set<String>) missing : Collections.emptySet();
    }

    /**
     * Check if any base types are missing
     *
     * @return true if any types are missing base subtypes
     */
    public boolean hasMissingBases() {
        return !getMissingBases().isEmpty();
    }

    /**
     * Generate a comprehensive report summary
     *
     * @return Formatted report string
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== REGISTRY HEALTH REPORT ===\n");

        // Overall status
        if (isStructurallySound() && followsBestPractices()) {
            summary.append("✅ EXCELLENT: Registry is structurally sound and follows all best practices\n");
        } else if (isStructurallySound()) {
            summary.append("⚠️  GOOD: Registry is structurally sound but has some recommendations\n");
        } else {
            summary.append("❌ POOR: Registry has structural issues that need attention\n");
        }

        // Statistics
        summary.append(String.format("\nStatistics: %d errors, %d warnings, %d recommendations\n",
            errors.size(), warnings.size(), recommendations.size()));

        // Metadata summary
        if (!metadata.isEmpty()) {
            summary.append("\nRegistry Metadata:\n");
            metadata.forEach((key, value) -> {
                summary.append(String.format("  %s: %s\n", key, value));
            });
        }

        // Errors
        if (!errors.isEmpty()) {
            summary.append("\n❌ ERRORS (Build Blockers):\n");
            errors.forEach(error -> summary.append("  - ").append(error).append("\n"));
        }

        // Warnings
        if (!warnings.isEmpty()) {
            summary.append("\n⚠️  WARNINGS (Should Address):\n");
            warnings.forEach(warning -> summary.append("  - ").append(warning).append("\n"));
        }

        // Recommendations
        if (!recommendations.isEmpty()) {
            summary.append("\n💡 RECOMMENDATIONS (Best Practices):\n");
            recommendations.forEach(rec -> summary.append("  - ").append(rec).append("\n"));
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return generateSummary();
    }
}