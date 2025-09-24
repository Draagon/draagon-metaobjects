package com.draagon.meta.registry;

import com.draagon.meta.MetaDataException;

import java.util.Set;

/**
 * Exception thrown when MetaDataTypeProvider discovery or registration fails.
 *
 * <p>This exception provides rich context for debugging provider issues including:</p>
 * <ul>
 *   <li><strong>Provider Name:</strong> Which provider failed</li>
 *   <li><strong>Missing Dependencies:</strong> What dependencies couldn't be resolved</li>
 *   <li><strong>Failure Phase:</strong> Where in the loading process the failure occurred</li>
 *   <li><strong>Root Cause:</strong> Original exception that caused the failure</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Provider registration failure
 * throw new MetaDataProviderException(
 *     "Failed to register StringField type",
 *     "field-types",
 *     Set.of("core-types"),
 *     ProviderLoadPhase.TYPE_REGISTRATION,
 *     originalException
 * );
 *
 * // Dependency resolution failure
 * throw new MetaDataProviderException(
 *     "Missing dependency: core-types",
 *     "field-types",
 *     Set.of("core-types"),
 *     ProviderLoadPhase.DEPENDENCY_RESOLUTION,
 *     null
 * );
 * }</pre>
 *
 * @since 6.3.0
 */
public class MetaDataProviderException extends MetaDataException {

    private final String providerName;
    private final Set<String> missingDependencies;
    private final MetaDataProviderDiscovery.ProviderLoadPhase failurePhase;

    /**
     * Create provider exception with full context.
     *
     * @param message Error message describing what went wrong
     * @param providerName Name of the provider that failed
     * @param missingDependencies Set of dependency names that couldn't be resolved
     * @param failurePhase Phase of provider loading where failure occurred
     * @param cause Original exception that caused the failure (may be null)
     */
    public MetaDataProviderException(String message,
                                   String providerName,
                                   Set<String> missingDependencies,
                                   MetaDataProviderDiscovery.ProviderLoadPhase failurePhase,
                                   Throwable cause) {
        super(enhanceMessage(message, providerName, missingDependencies, failurePhase), cause);
        this.providerName = providerName;
        this.missingDependencies = Set.copyOf(missingDependencies);
        this.failurePhase = failurePhase;
    }

    /**
     * Create provider exception with basic context.
     *
     * @param message Error message
     * @param providerName Name of the provider that failed
     * @param cause Original exception (may be null)
     */
    public MetaDataProviderException(String message, String providerName, Throwable cause) {
        this(message, providerName, Set.of(), MetaDataProviderDiscovery.ProviderLoadPhase.TYPE_REGISTRATION, cause);
    }

    /**
     * Create provider exception with message only.
     *
     * @param message Error message
     */
    public MetaDataProviderException(String message) {
        this(message, "unknown", Set.of(), MetaDataProviderDiscovery.ProviderLoadPhase.DISCOVERY, null);
    }

    /**
     * Get the name of the provider that failed.
     *
     * @return Provider name, or "unknown" if not specified
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Get the set of dependencies that couldn't be resolved.
     *
     * @return Immutable set of missing dependency names
     */
    public Set<String> getMissingDependencies() {
        return missingDependencies;
    }

    /**
     * Get the phase where the provider loading failed.
     *
     * @return Failure phase for debugging
     */
    public MetaDataProviderDiscovery.ProviderLoadPhase getFailurePhase() {
        return failurePhase;
    }

    /**
     * Check if this failure was caused by missing dependencies.
     *
     * @return true if dependencies are missing, false otherwise
     */
    public boolean hasMissingDependencies() {
        return !missingDependencies.isEmpty();
    }

    /**
     * Get diagnostic information for debugging.
     *
     * @return Multi-line string with provider failure details
     */
    public String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("MetaDataProvider Failure Diagnostics:\n");
        info.append("  Provider: ").append(providerName).append("\n");
        info.append("  Phase: ").append(failurePhase.getDescription()).append("\n");

        if (!missingDependencies.isEmpty()) {
            info.append("  Missing Dependencies: ").append(String.join(", ", missingDependencies)).append("\n");
        }

        if (getCause() != null) {
            info.append("  Root Cause: ").append(getCause().getClass().getSimpleName())
                .append(": ").append(getCause().getMessage()).append("\n");
        }

        return info.toString();
    }

    /**
     * Enhance error message with provider context.
     */
    private static String enhanceMessage(String originalMessage,
                                       String providerName,
                                       Set<String> missingDependencies,
                                       MetaDataProviderDiscovery.ProviderLoadPhase failurePhase) {
        StringBuilder enhanced = new StringBuilder();
        enhanced.append("[").append(failurePhase.getDescription()).append("] ");
        enhanced.append(originalMessage);

        if (!"unknown".equals(providerName)) {
            enhanced.append(" (Provider: ").append(providerName).append(")");
        }

        if (!missingDependencies.isEmpty()) {
            enhanced.append(" - Missing dependencies: ").append(String.join(", ", missingDependencies));
        }

        return enhanced.toString();
    }

    /**
     * Create exception for circular dependency detection.
     */
    public static MetaDataProviderException circularDependency(String providerName, String cycle) {
        return new MetaDataProviderException(
                "Circular dependency detected: " + cycle,
                providerName,
                Set.of(),
                MetaDataProviderDiscovery.ProviderLoadPhase.DEPENDENCY_RESOLUTION,
                null
        );
    }

    /**
     * Create exception for missing dependency.
     */
    public static MetaDataProviderException missingDependency(String providerName, String missingDep) {
        return new MetaDataProviderException(
                "Required dependency not found: " + missingDep,
                providerName,
                Set.of(missingDep),
                MetaDataProviderDiscovery.ProviderLoadPhase.DEPENDENCY_RESOLUTION,
                null
        );
    }

    /**
     * Create exception for registration failure.
     */
    public static MetaDataProviderException registrationFailure(String providerName, Throwable cause) {
        return new MetaDataProviderException(
                "Type registration failed",
                providerName,
                Set.of(),
                MetaDataProviderDiscovery.ProviderLoadPhase.TYPE_REGISTRATION,
                cause
        );
    }
}