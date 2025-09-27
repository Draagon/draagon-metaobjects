package com.metaobjects.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark test classes that manipulate the shared MetaDataRegistry
 * directly and need to run in isolation from other tests.
 *
 * Tests marked with this annotation:
 * 1. Should run in single-threaded mode (no parallel execution)
 * 2. May modify the shared registry state
 * 3. Are responsible for cleaning up after themselves
 *
 * Use the surefire plugin configuration to exclude these tests from
 * parallel execution if needed:
 *
 * <pre>
 * mvn test -Dgroups="!com.metaobjects.registry.IsolatedTest"  // Run non-isolated tests in parallel
 * mvn test -Dgroups="com.metaobjects.registry.IsolatedTest"   // Run isolated tests separately
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface IsolatedTest {

    /**
     * Reason for isolation (for documentation purposes)
     */
    String value() default "Manipulates shared registry state";
}