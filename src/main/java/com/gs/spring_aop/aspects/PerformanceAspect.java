package com.gs.spring_aop.aspects;

import com.gs.spring_aop.costum_annotations.PerformanceMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for monitoring method performance
 * Measures execution time and warns if it exceeds threshold
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * Around advice for methods annotated with @PerformanceMonitor
     */
    @Around("@annotation(performanceMonitor)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint, PerformanceMonitor performanceMonitor) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String customName = performanceMonitor.value().isEmpty() ? methodName : performanceMonitor.value();
        long threshold = performanceMonitor.threshold();

        logger.debug("⏱️ [{}] Performance monitoring started", customName);

        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

            if (executionTime > threshold) {
                logger.warn("🐌 [{}] SLOW EXECUTION: {}ms (threshold: {}ms)",
                        customName, executionTime, threshold);
            } else {
                logger.info("⚡ [{}] Fast execution: {}ms", customName, executionTime);
            }

            // Log performance metrics
            logPerformanceMetrics(customName, executionTime, threshold);

            return result;

        } catch (Exception e) {
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000;

            logger.error("💥 [{}] Exception occurred after {}ms: {}",
                    customName, executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Log detailed performance metrics
     */
    private void logPerformanceMetrics(String methodName, long executionTime, long threshold) {
        double performanceRatio = (double) executionTime / threshold;
        String performanceStatus;

        if (performanceRatio <= 0.5) {
            performanceStatus = "EXCELLENT";
        } else if (performanceRatio <= 0.8) {
            performanceStatus = "GOOD";
        } else if (performanceRatio <= 1.0) {
            performanceStatus = "ACCEPTABLE";
        } else if (performanceRatio <= 2.0) {
            performanceStatus = "SLOW";
        } else {
            performanceStatus = "VERY SLOW";
        }

        logger.info("📊 [{}] Performance: {} ({}ms / {}ms = {:.2f})",
                methodName, performanceStatus, executionTime, threshold, performanceRatio);
    }
}