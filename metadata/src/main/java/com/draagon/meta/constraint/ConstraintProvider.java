package com.draagon.meta.constraint;

/**
 * Service provider interface for registering constraints in the unified registry system.
 * 
 * <p>This interface enables a service-based approach to constraint registration that works
 * in both OSGi and non-OSGi environments:</p>
 * 
 * <ul>
 *   <li><strong>OSGi:</strong> Implementations are automatically discovered via @Component annotation</li>
 *   <li><strong>Non-OSGi:</strong> Implementations are discovered via ServiceLoader mechanism</li>
 *   <li><strong>Spring:</strong> Implementations are discovered via @Component scanning</li>
 * </ul>
 * 
 * <h3>Implementation Pattern:</h3>
 * <pre>{@code
 * @Component // OSGi will recognize this
 * public class CoreFieldConstraintProvider implements ConstraintProvider {
 *     
 *     @Override
 *     public void registerConstraints(ConstraintRegistry registry) {
 *         // String field can have maxLength attribute
 *         registry.addConstraint(new PlacementConstraint(
 *             "stringfield.maxlength.placement",
 *             "String fields can have maxLength attribute",
 *             (parent) -> parent instanceof StringField,
 *             (child) -> child instanceof IntAttribute && 
 *                       child.getName().equals("maxLength")
 *         ));
 *         
 *         // Field naming pattern validation
 *         registry.addConstraint(new ValidationConstraint(
 *             "field.naming.pattern",
 *             "Field names must follow identifier pattern",
 *             (metadata) -> metadata instanceof MetaField,
 *             (metadata, value) -> {
 *                 String name = metadata.getName();
 *                 return name != null && name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
 *             }
 *         ));
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "Core field validation constraints";
 *     }
 * }
 * }</pre>
 * 
 * <h3>Service Discovery:</h3>
 * <p>For non-OSGi environments, add the implementation to:</p>
 * <pre>META-INF/services/com.draagon.meta.constraint.ConstraintProvider</pre>
 * 
 * @since 6.0.0
 */
public interface ConstraintProvider {
    
    /**
     * Register constraints with the given registry.
     * 
     * <p>This method is called during registry initialization to allow
     * providers to register their constraints. Providers should add
     * all relevant constraints for their domain (fields, objects, etc.).</p>
     * 
     * @param registry The constraint registry to register constraints with
     */
    void registerConstraints(ConstraintRegistry registry);
    
    /**
     * Get a human-readable description of this provider.
     * 
     * <p>Used for logging and debugging purposes to identify which
     * provider registered which constraints.</p>
     * 
     * @return Description of this constraint provider
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
    
    /**
     * Get the priority of this provider for registration ordering.
     * 
     * <p>Providers with lower priority values are registered first.
     * This allows core constraints to be registered before extension
     * constraints that might depend on them.</p>
     * 
     * @return Priority value (lower = higher priority)
     */
    default int getPriority() {
        return 1000; // Default priority
    }
}