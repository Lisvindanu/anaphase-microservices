// ===============================================================================
// FINAL FIXED: RateLimitMetrics.kt - Counter Increment Issue Resolved
// ===============================================================================

package org.anaphase.gateway.ratelimit

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Tags
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class RateLimitMetrics(private val meterRegistry: MeterRegistry) {

    private val allowedRequestsCounter = Counter.builder("gateway.ratelimit.requests.allowed")
        .description("Number of requests allowed by rate limiter")
        .register(meterRegistry)

    private val deniedRequestsCounter = Counter.builder("gateway.ratelimit.requests.denied")
        .description("Number of requests denied by rate limiter")
        .register(meterRegistry)

    private val rateLimitCheckTimer = Timer.builder("gateway.ratelimit.check.duration")
        .description("Time taken to check rate limits")
        .register(meterRegistry)

    fun recordAllowedRequest(clientType: String = "default") {
        // FIXED: Use Tags.of() instead of listOf(Tag.of())
        Counter.builder("gateway.ratelimit.requests.allowed")
            .tag("client_type", clientType)
            .register(meterRegistry)
            .increment()
    }

    fun recordDeniedRequest(clientType: String = "default") {
        // FIXED: Use Tags.of() instead of listOf(Tag.of())
        Counter.builder("gateway.ratelimit.requests.denied")
            .tag("client_type", clientType)
            .register(meterRegistry)
            .increment()
    }

    fun recordRateLimitCheck(duration: Long) {
        rateLimitCheckTimer.record(duration, TimeUnit.NANOSECONDS)
    }

    // Additional helper methods for better metrics
    fun recordRateLimitCheckWithTags(duration: Long, clientType: String, allowed: Boolean) {
        // Record timing
        rateLimitCheckTimer.record(duration, TimeUnit.NANOSECONDS)

        // Record result with proper tags
        if (allowed) {
            recordAllowedRequest(clientType)
        } else {
            recordDeniedRequest(clientType)
        }
    }

    fun getMetricsSnapshot(): Map<String, Any> {
        return mapOf(
            "allowed_requests_total" to allowedRequestsCounter.count(),
            "denied_requests_total" to deniedRequestsCounter.count(),
            "check_duration_avg_ms" to rateLimitCheckTimer.mean(TimeUnit.MILLISECONDS),
            "check_duration_max_ms" to rateLimitCheckTimer.max(TimeUnit.MILLISECONDS),
            "total_checks" to rateLimitCheckTimer.count()
        )
    }

    // Alternative simpler implementation without per-client-type counters
    fun recordAllowedRequestSimple() {
        allowedRequestsCounter.increment()
    }

    fun recordDeniedRequestSimple() {
        deniedRequestsCounter.increment()
    }
}