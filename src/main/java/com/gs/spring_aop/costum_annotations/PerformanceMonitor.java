package com.gs.spring_aop.costum_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to monitor method performance
 * AOP will measure execution time for methods marked with this annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceMonitor {
    String value() default "";
    long threshold() default 1000; // milliseconds
}
