package com.gs.spring_aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aspect for security monitoring
 * Demonstrates monitoring and rate limiting
 */
@Aspect
@Component
public class SecurityAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAspect.class);

    // Simple rate limiting - in production, use Redis or similar
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60_000;

    /**
     * Pointcut for all public methods in controller package
     */
    @Pointcut("execution(public * com.gs.spring_aop.*.*(..))")
    public void publicControllerMethods() {}

    /**
     * Pointcut for methods containing "delete" or "remove" in their name
     */
    @Pointcut("execution(* *..*.*(..)) && (execution(* *delete*(..)) || execution(* *remove*(..)))")
    public void dangerousMethods() {}

    /**
     * Before advice for security monitoring
     */
    @Before("publicControllerMethods()")
    public void monitorAccess(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String clientIdentifier = getClientIdentifier(); // In real app, get from request

        logger.info("🔒 [SECURITY] Access attempt: {} by client: {}", methodName, clientIdentifier);

        // Check rate limiting
        if (isRateLimited(clientIdentifier)) {
            logger.warn("⚠️ [SECURITY] Rate limit exceeded for client: {} on method: {}",
                    clientIdentifier, methodName);
            // In real application, throw exception or return error
        }

        // Log sensitive parameters (mask them)
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            String maskedArgs = maskSensitiveData(Arrays.toString(args));
            logger.debug("🔍 [SECURITY] Method parameters: {}", maskedArgs);
        }
    }

    /**
     * Before advice for dangerous operations
     */
    @Before("dangerousMethods()")
    public void monitorDangerousOperations(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String clientIdentifier = getClientIdentifier();

        logger.warn("⚠️ [SECURITY] DANGEROUS OPERATION: {} by client: {}", methodName, clientIdentifier);
        logger.warn("🔥 [SECURITY] Parameters: {}", maskSensitiveData(Arrays.toString(joinPoint.getArgs())));

        // In real application, you might:
        // 1. Require additional authentication
        // 2. Send alerts to administrators
        // 3. Log to security audit system
        // 4. Apply additional rate limiting
    }

    /**
     * Simple rate limiting check
     */
    private boolean isRateLimited(String clientIdentifier) {
        long currentTime = System.currentTimeMillis();

        // Clean up old entries
        lastRequestTime.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > MINUTE_IN_MILLIS);

        // Check if client has made too many requests
        AtomicInteger count = requestCounts.computeIfAbsent(clientIdentifier, k -> new AtomicInteger(0));
        Long lastRequest = lastRequestTime.get(clientIdentifier);

        if (lastRequest == null || currentTime - lastRequest > MINUTE_IN_MILLIS) {
            // Reset counter for new minute window
            count.set(1);
            lastRequestTime.put(clientIdentifier, currentTime);
            return false;
        } else {
            // Increment counter
            int currentCount = count.incrementAndGet();
            lastRequestTime.put(clientIdentifier, currentTime);

            if (currentCount > MAX_REQUESTS_PER_MINUTE) {
                logger.warn("🚨 [SECURITY] Rate limit exceeded: {} requests in last minute for client: {}",
                        currentCount, clientIdentifier);
                return true;
            }
        }

        return false;
    }

    /**
     * Get client identifier (simplified for demo)
     * In real application, extract from HttpServletRequest
     */
    private String getClientIdentifier() {
        // In real application, you would get this from:
        // HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // return request.getRemoteAddr() or JWT token subject, etc.
        return "demo-client-" + Thread.currentThread().getName();
    }

    /**
     * Mask sensitive data in logs
     */
    private String maskSensitiveData(String data) {
        if (data == null) return "null";

        // Mask common sensitive fields
        String maskedData = data
                .replaceAll("password[\"']?\\s*[:=]\\s*[\"']?[^,\\]\\}]+", "password=***")
                .replaceAll("token[\"']?\\s*[:=]\\s*[\"']?[^,\\]\\}]+", "token=***")
                .replaceAll("secret[\"']?\\s*[:=]\\s*[\"']?[^,\\]\\}]+", "secret=***")
                .replaceAll("key[\"']?\\s*[:=]\\s*[\"']?[^,\\]\\}]+", "key=***");

        return maskedData;
    }

    /**
     * Get current rate limit statistics
     */
    public void logSecurityStats() {
        logger.info("📊 [SECURITY] Current active clients: {}", requestCounts.size());
        requestCounts.forEach((client, count) ->
                logger.info("📊 [SECURITY] Client: {} - Requests: {}", client, count.get()));
    }
}