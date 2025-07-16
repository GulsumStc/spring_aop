package com.gs.spring_aop.costum_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for smple caching
 * AOP will cache method results for methods marked with this annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleCache {
    String key() default "";
    long ttl() default 60000; // Time to live in milliseconds (default: 1 minute)
}