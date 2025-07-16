package com.gs.spring_aop.aspects;


import com.gs.spring_aop.costum_annotations.Loggable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging method calls
 * Demonstrates @Before, @After, @Around, @AfterReturning, @AfterThrowing
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut for all methods in service package
     */
    @Pointcut("execution(* com.gs.spring_aop.*.*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut for all methods in controller package
     */
    @Pointcut("execution(* com.gs.spring_aop.*.*(..))")
    public void controllerLayer() {}

    /**
     * Pointcut for methods annotated with @Loggable
     */
    @Pointcut("@annotation(com.gs.spring_aop.costum_annotations.Loggable)")
    public void loggableMethod() {}

    /**
     * Before advice - executes before method execution
     */
    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("==> Entering method: {} with arguments: {}",
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * After advice - executes after method execution (regardless of outcome)
     */
    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("<== Exiting method: {}", joinPoint.getSignature().toShortString());
    }

    /**
     * After returning advice - executes after successful method execution
     */
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Method {} returned: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }

    /**
     * After throwing advice - executes after method throws exception
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        logger.error("Method {} threw exception: {}",
                joinPoint.getSignature().toShortString(),
                exception.getMessage());
    }

    /**
     * Around advice for methods annotated with @Loggable
     * This is the most powerful advice type
     */
    @Around("loggableMethod() && @annotation(loggable)")
    public Object logAroundLoggable(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String customMessage = loggable.value().isEmpty() ? methodName : loggable.value();

        logger.info("🚀 [{}] Starting execution", customMessage);

        if (loggable.logArgs()) {
            logger.info("📝 [{}] Arguments: {}", customMessage, Arrays.toString(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            if (loggable.logResult()) {
                logger.info("✅ [{}] Completed successfully in {}ms, Result: {}",
                        customMessage, executionTime, result);
            } else {
                logger.info("✅ [{}] Completed successfully in {}ms", customMessage, executionTime);
            }

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ [{}] Failed after {}ms with exception: {}",
                    customMessage, executionTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Around advice for controller methods
     */
    @Around("controllerLayer()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        logger.info("🌐 HTTP Request: {} with parameters: {}",
                methodName, Arrays.toString(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("🌐 HTTP Response: {} completed in {}ms", methodName, executionTime);
            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("🌐 HTTP Error: {} failed after {}ms: {}",
                    methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}
