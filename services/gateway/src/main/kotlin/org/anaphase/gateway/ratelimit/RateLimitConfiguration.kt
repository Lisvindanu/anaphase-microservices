package org.anaphase.gateway.ratelimit

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected

@ConfigurationProperties("gateway.rate-limiting")
@Introspected
class RateLimitConfiguration {
    var enabled: Boolean = false
    var skipDebugEndpoints: Boolean = true
    var defaultStrategy: RateLimitStrategy = RateLimitStrategy.TOKEN_BUCKET

    // Default rate limit for all clients
    var defaultLimit: RateLimitConfig = RateLimitConfig(
        requestsPerMinute = 60,
        burstSize = 10,
        strategy = RateLimitStrategy.TOKEN_BUCKET
    )

    // Global rate limit (across all clients)
    var globalLimit: RateLimitConfig = RateLimitConfig(
        requestsPerMinute = 1000,
        burstSize = 100,
        strategy = RateLimitStrategy.TOKEN_BUCKET
    )

    // Per-user specific limits (user-id -> config)
    var userLimits: Map<String, RateLimitConfig> = emptyMap()

    // Path-specific limits (regex pattern -> config)
    var pathLimits: Map<String, RateLimitConfig> = emptyMap()

    // Storage configuration
    var storage: StorageConfig = StorageConfig()

    @Introspected
    class StorageConfig {
        var type: StorageType = StorageType.MEMORY
        var redisKeyPrefix: String = "gateway:ratelimit:"
        var fallbackToMemory: Boolean = true
    }

    enum class StorageType {
        MEMORY, REDIS, HYBRID
    }
}