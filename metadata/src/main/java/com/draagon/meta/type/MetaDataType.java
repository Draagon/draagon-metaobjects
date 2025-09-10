package com.draagon.meta.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic registration of custom MetaData types.
 * 
 * This annotation can be placed on MetaData implementation classes
 * to automatically register them with the MetaDataTypeRegistry
 * during application startup or plugin loading.
 * 
 * Example usage:
 * <pre>
 * {@code
 * @MetaDataType(
 *     name = "custom-widget",
 *     description = "Custom UI Widget Metadata",
 *     allowedSubTypes = {"button", "input", "select"},
 *     allowsChildren = true
 * )
 * public class CustomWidgetMetaData extends MetaData {
 *     // Implementation
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaDataType {
    
    /**
     * The unique name for this MetaData type
     */
    String name();
    
    /**
     * Human-readable description of this type
     */
    String description() default "";
    
    /**
     * Array of allowed subtypes for this type.
     * If empty, all subtypes are allowed.
     */
    String[] allowedSubTypes() default {};
    
    /**
     * Whether this type can have child metadata
     */
    boolean allowsChildren() default true;
    
    /**
     * Whether this is an abstract type that cannot be instantiated directly
     */
    boolean isAbstract() default false;
    
    /**
     * Priority for registration order (higher values registered first)
     */
    int priority() default 0;
}