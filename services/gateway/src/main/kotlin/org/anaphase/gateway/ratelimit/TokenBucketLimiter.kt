// ===============================================================================
// ENHANCED: TokenBucketLimiter.kt - With Metrics Integration
// ===============================================================================

package org.anaphase.gateway.ratelimit

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Singleton
class TokenBucketLimiter(
    private val rateLimitMetrics: RateLimitMetrics
) : RateLimiter {

    private val logger = LoggerFactory.getLogger(TokenBucketLimiter::class.java)
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    data class TokenBucket(
        var tokens: Long,
        var lastRefillTime: Long,
        val capacity: Long,
        val refillRate: Double // tokens per second
    )

    override fun checkLimit(clientId: String, config: RateLimitConfig): RateLimitResult {
        val startTime = System.nanoTime()

        val bucket = buckets.computeIfAbsent(clientId) {
            TokenBucket(
                tokens = config.burstSize,
                lastRefillTime = System.currentTimeMillis(),
                capacity = config.burstSize,
                refillRate = config.requestsPerMinute / 60.0
            )
        }

        val result = synchronized(bucket) {
            refillBucket(bucket)

            if (bucket.tokens >= 1) {
                bucket.tokens -= 1
                logger.debug("Rate limit allowed for client: {} (remaining: {})", clientId, bucket.tokens)
                RateLimitResult(
                    allowed = true,
                    remaining = bucket.tokens,
                    resetTime = calculateResetTime(bucket, config)
                )
            } else {
                logger.warn("Rate limit exceeded for client: {}", clientId)
                RateLimitResult(
                    allowed = false,
                    remaining = 0,
                    resetTime = calculateResetTime(bucket, config),
                    retryAfterSeconds = calculateRetryAfter(bucket)
                )
            }
        }

        // Record metrics
        val duration = System.nanoTime() - startTime
        val clientType = determineClientType(clientId)
        rateLimitMetrics.recordRateLimitCheckWithTags(duration, clientType, result.allowed)

        return result
    }

    private fun refillBucket(bucket: TokenBucket) {
        val now = System.currentTimeMillis()
        val elapsed = (now - bucket.lastRefillTime) / 1000.0
        val tokensToAdd = (elapsed * bucket.refillRate).toLong()

        if (tokensToAdd > 0) {
            bucket.tokens = minOf(bucket.capacity, bucket.tokens + tokensToAdd)
            bucket.lastRefillTime = now
        }
    }

    private fun calculateResetTime(bucket: TokenBucket, config: RateLimitConfig): Long {
        return bucket.lastRefillTime + (60 * 1000) // Next minute
    }

    private fun calculateRetryAfter(bucket: TokenBucket): Long {
        val timeToNextToken = (1.0 / bucket.refillRate * 1000).toLong()
        return timeToNextToken / 1000 + 1 // Convert to seconds
    }

    private fun determineClientType(clientId: String): String {
        return when {
            clientId == "admin" -> "admin"
            clientId.contains("premium") -> "premium"
            clientId.contains("developer") -> "developer"
            clientId.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> "ip_based"
            else -> "default"
        }
    }

    override fun resetLimit(clientId: String) {
        buckets.remove(clientId)
        logger.info("Reset rate limit for client: {}", clientId)
    }

    // Additional methods for monitoring
    fun getBucketStatus(clientId: String): Map<String, Any>? {
        val bucket = buckets[clientId] ?: return null
        return synchronized(bucket) {
            refillBucket(bucket) // Update before reporting
            mapOf(
                "tokens" to bucket.tokens,
                "capacity" to bucket.capacity,
                "refill_rate" to bucket.refillRate,
                "last_refill" to bucket.lastRefillTime
            )
        }
    }

    fun getAllBucketsStatus(): Map<String, Map<String, Any>> {
        return buckets.keys.associateWith { clientId ->
            getBucketStatus(clientId) ?: emptyMap()
        }
    }

    fun getActiveBucketsCount(): Int = buckets.size
}