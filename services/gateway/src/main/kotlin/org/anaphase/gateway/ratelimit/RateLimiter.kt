package org.anaphase.gateway.ratelimit

interface RateLimiter {
    fun checkLimit(clientId: String, config: RateLimitConfig): RateLimitResult
    fun resetLimit(clientId: String)
}