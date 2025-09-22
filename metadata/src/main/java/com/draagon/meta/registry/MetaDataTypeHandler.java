package com.draagon.meta.registry;

/**
 * @deprecated Use {@link MetaDataType} instead. This alias is provided for backward compatibility.
 *
 * <p>The annotation has been renamed from @MetaDataTypeHandler to @MetaDataType to better
 * reflect its purpose - these classes ARE metadata types, they don't "handle" them.</p>
 *
 * @since 6.0.0
 * @deprecated since 6.2.0, use {@link MetaDataType}
 */
@Deprecated
public @interface MetaDataTypeHandler {

    /**
     * @deprecated Use {@link MetaDataType#type()} instead
     */
    @Deprecated
    String type();

    /**
     * @deprecated Use {@link MetaDataType#subType()} instead
     */
    @Deprecated
    String subType();

    /**
     * @deprecated Use {@link MetaDataType#description()} instead
     */
    @Deprecated
    String description() default "";

    /**
     * @deprecated Use {@link MetaDataType#isAbstract()} instead
     */
    @Deprecated
    boolean isAbstract() default false;

    /**
     * @deprecated Use {@link MetaDataType#priority()} instead
     */
    @Deprecated
    int priority() default 100;
}