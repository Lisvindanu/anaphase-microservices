package org.anaphase.gateway.ratelimit

enum class RateLimitStrategy {
    TOKEN_BUCKET,
    SLIDING_WINDOW,
    FIXED_WINDOW
}

data class RateLimitResult(
    val allowed: Boolean,
    val remaining: Long,
    val resetTime: Long,
    val retryAfterSeconds: Long = 0
)

data class RateLimitConfig(
    val requestsPerMinute: Long,
    val burstSize: Long = requestsPerMinute,
    val strategy: RateLimitStrategy = RateLimitStrategy.TOKEN_BUCKET
)