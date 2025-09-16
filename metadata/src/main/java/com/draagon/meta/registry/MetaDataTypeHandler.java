package com.draagon.meta.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark MetaData implementation classes and specify their type and subtype.
 * 
 * <p>This annotation is used by the service-based type registry to automatically
 * identify and register MetaData implementations. Classes marked with this annotation
 * should also include self-registration code in their static initializers.</p>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * @MetaDataTypeHandler(type="field", subType="string")
 * public class StringField extends MetaData {
 *     public StringField(String name) {
 *         super("field", "string", name);
 *     }
 *     
 *     static {
 *         // Self-registration
 *         MetaDataTypeRegistry registry = ServiceRegistryFactory.getDefault();
 *         registry.registerHandler(
 *             new MetaDataTypeId("field", "string"), 
 *             StringField.class
 *         );
 *     }
 * }
 * 
 * @MetaDataTypeHandler(type="view", subType="text")  
 * public class TextView extends MetaData {
 *     public TextView(String name) {
 *         super("view", "text", name);
 *     }
 * }
 * 
 * @MetaDataTypeHandler(type="validator", subType="required")
 * public class RequiredValidator extends MetaData {
 *     public RequiredValidator(String name) {
 *         super("validator", "required", name);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Plugin Extension Example:</h3>
 * <pre>{@code
 * @MetaDataTypeHandler(type="field", subType="currency")
 * public class CurrencyField extends MetaData {
 *     public CurrencyField(String name) {
 *         super("field", "currency", name);
 *     }
 *     
 *     static {
 *         // Self-register when class is loaded
 *         MetaDataTypeRegistry.registerHandler(
 *             new MetaDataTypeId("field", "currency"), 
 *             CurrencyField.class
 *         );
 *     }
 * }
 * }</pre>
 * 
 * @since 6.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MetaDataTypeHandler {
    
    /**
     * The primary type this class handles.
     * 
     * <p>Examples: "field", "view", "validator", "object", "attr"</p>
     * 
     * @return Primary type name (required)
     */
    String type();
    
    /**
     * The specific subtype this class implements.
     * 
     * <p>Examples for fields: "string", "int", "date", "currency"</p>
     * <p>Examples for views: "text", "select", "checkbox"</p>
     * <p>Examples for validators: "required", "regex", "length"</p>
     * 
     * @return Subtype name (required)
     */
    String subType();
    
    /**
     * Human-readable description of this type handler (optional).
     * 
     * <p>Used for documentation and debugging purposes.</p>
     * 
     * @return Description of the handler
     */
    String description() default "";
    
    /**
     * Whether this is an abstract type handler (optional).
     * 
     * <p>Abstract handlers are not instantiated directly but serve as base
     * classes for concrete implementations. Default is false.</p>
     * 
     * @return true if this is an abstract handler
     */
    boolean isAbstract() default false;
    
    /**
     * Registration priority (optional).
     * 
     * <p>Lower numbers = higher priority. Used when multiple handlers
     * could handle the same type+subtype combination. Default is 100.</p>
     * 
     * @return Priority value (lower = higher priority)
     */
    int priority() default 100;
}