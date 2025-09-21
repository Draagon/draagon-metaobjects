package com.draagon.meta.registry;

/**
 * Interface for service providers that want to add global child requirements
 * to the unified MetaDataRegistry.
 * 
 * <p>This allows cross-cutting concerns like database mappings, security attributes,
 * or validation rules to be added to core types without modifying their definitions.</p>
 * 
 * <h3>Example Implementation:</h3>
 * 
 * <pre>{@code
 * @Component
 * public class DatabaseAttributeProvider implements MetaDataRegistryExtension {
 *     
 *     @Override
 *     public void enhanceRegistry(MetaDataRegistry registry) {
 *         // All field types can have database column attributes
 *         registry.addGlobalChildRequirement("field", "*",
 *             ChildRequirement.optional("dbColumn", "attr", "string"));
 *             
 *         registry.addGlobalChildRequirement("field", "*", 
 *             ChildRequirement.optional("dbNullable", "attr", "boolean"));
 *             
 *         // All object types can have database table attributes  
 *         registry.addGlobalChildRequirement("object", "*",
 *             ChildRequirement.optional("dbTable", "attr", "string"));
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
public interface MetaDataRegistryExtension {
    
    /**
     * Enhance the registry with additional global child requirements
     * 
     * <p>This method is called during registry initialization to allow
     * service providers to add cross-cutting child requirements that
     * apply to multiple type definitions.</p>
     * 
     * @param registry The registry to enhance
     */
    void enhanceRegistry(MetaDataRegistry registry);
}