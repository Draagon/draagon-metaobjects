package com.draagon.meta.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark and define MetaData types with their hierarchical classification.
 *
 * <p>This annotation identifies classes that represent specific metadata types in the
 * MetaObjects framework. It specifies the type hierarchy (type.subType) and enables
 * automatic discovery and registration by the type system.</p>
 *
 * <p>Classes annotated with @MetaDataType should also include self-registration code
 * in their static initializers for complete type system integration.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * @MetaDataType(type="field", subType="string", description="String field with length validation")
 * public class StringField extends MetaField {
 *     public StringField(String name) {
 *         super("field", "string", name);
 *     }
 *
 *     static {
 *         // Self-registration with type system
 *         MetaDataRegistry.registerType(StringField.class, def -> def
 *             .type("field").subType("string")
 *             .description("String field with length validation")
 *         );
 *     }
 * }
 *
 * @MetaDataType(type="view", subType="text", description="HTML text input view")
 * public class TextView extends MetaView {
 *     public TextView(String name) {
 *         super("view", "text", name);
 *     }
 * }
 *
 * @MetaDataType(type="validator", subType="required", description="Required field validator")
 * public class RequiredValidator extends MetaValidator {
 *     public RequiredValidator(String name) {
 *         super("validator", "required", name);
 *     }
 * }
 * }</pre>
 *
 * <h3>Plugin Extension Example:</h3>
 * <pre>{@code
 * @MetaDataType(type="field", subType="currency", description="Currency field with precision")
 * public class CurrencyField extends MetaField {
 *     public CurrencyField(String name) {
 *         super("field", "currency", name);
 *     }
 *
 *     static {
 *         // Self-register custom type
 *         MetaDataRegistry.registerType(CurrencyField.class, def -> def
 *             .type("field").subType("currency")
 *             .optionalAttribute("precision", "int")
 *             .description("Currency field with precision")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @since 6.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MetaDataType {
    
    /**
     * The primary type category for this metadata class.
     *
     * <p>Examples: "field", "view", "validator", "object", "attr", "key"</p>
     *
     * @return Primary type name (required)
     */
    String type();

    /**
     * The specific subtype this metadata class represents.
     *
     * <p>Examples for fields: "string", "int", "date", "currency"</p>
     * <p>Examples for views: "text", "select", "checkbox"</p>
     * <p>Examples for validators: "required", "regex", "length"</p>
     *
     * @return Subtype name (required)
     */
    String subType();

    /**
     * Human-readable description of this metadata type (optional).
     *
     * <p>Used for documentation, code generation, and AI assistance.</p>
     *
     * @return Description of the metadata type
     */
    String description() default "";

    /**
     * Whether this is an abstract metadata type (optional).
     *
     * <p>Abstract types are not instantiated directly but serve as base
     * classes for concrete implementations. Default is false.</p>
     *
     * @return true if this is an abstract type
     */
    boolean isAbstract() default false;

    /**
     * Registration priority for type resolution (optional).
     *
     * <p>Lower numbers = higher priority. Used when multiple types
     * could handle the same type+subtype combination. Default is 100.</p>
     *
     * @return Priority value (lower = higher priority)
     */
    int priority() default 100;
}