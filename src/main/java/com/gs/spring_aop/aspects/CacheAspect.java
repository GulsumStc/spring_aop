package com.gs.spring_aop.aspects;

import com.gs.spring_aop.costum_annotations.SimpleCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect for simple caching mechanism
 * Caches method results based on method signature and parameters
 */
@Aspect
@Component
public class CacheAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheAspect.class);

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Around advice for methods annotated with @SimpleCache
     */
    @Around("@annotation(simpleCache)")
    public Object cacheResult(ProceedingJoinPoint joinPoint, SimpleCache simpleCache) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String customKey = simpleCache.key();
        long ttl = simpleCache.ttl();

        // Generate cache key
        String cacheKey = generateCacheKey(methodName, customKey, joinPoint.getArgs());

        // Check if result is in cache and still valid
        CacheEntry cacheEntry = cache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            logger.info("🎯 [{}] Cache HIT for key: {}", methodName, cacheKey);
            return cacheEntry.getValue();
        }

        logger.info("❌ [{}] Cache MISS for key: {}", methodName, cacheKey);

        // Execute method and cache result
        Object result = joinPoint.proceed();

        // Store in cache
        CacheEntry newEntry = new CacheEntry(result, System.currentTimeMillis() + ttl);
        cache.put(cacheKey, newEntry);

        logger.info("💾 [{}] Result cached for key: {} (TTL: {}ms)", methodName, cacheKey, ttl);

        return result;
    }

    /**
     * Generate cache key based on method name, custom key, and parameters
     */
    private String generateCacheKey(String methodName, String customKey, Object[] args) {
        if (!customKey.isEmpty()) {
            return customKey + ":" + Arrays.toString(args);
        }
        return methodName + ":" + Arrays.toString(args);
    }

    /**
     * Clear expired entries from cache
     */
    public void clearExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
        logger.info("🧹 Cleared expired cache entries");
    }

    /**
     * Clear all cache entries
     */
    public void clearAllCache() {
        cache.clear();
        logger.info("🧹 Cleared all cache entries");
    }

    /**
     * Get cache statistics
     */
    public void logCacheStats() {
        logger.info("📊 Cache statistics - Total entries: {}", cache.size());
    }

    /**
     * Inner class to represent a cache entry
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;

        public CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expirationTime;
        }
    }
}