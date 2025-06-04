// ===============================================================================
// FIXED: RateLimitInterceptor.kt - All Type Errors Resolved
// ===============================================================================

package org.anaphase.gateway.ratelimit

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RateLimitInterceptor(
    private val rateLimiter: RateLimiter,
    private val rateLimitConfig: RateLimitConfiguration
) : HttpServerFilter {

    private val logger = LoggerFactory.getLogger(RateLimitInterceptor::class.java)

    companion object {
        private const val RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining"
        private const val RATE_LIMIT_RESET_HEADER = "X-RateLimit-Reset"
        private const val RETRY_AFTER_HEADER = "Retry-After"
        private const val RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit"
    }

    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {

        // Skip rate limiting for health checks and debug endpoints
        if (shouldSkipRateLimit(request)) {
            return chain.proceed(request)
        }

        if (!rateLimitConfig.enabled) {
            return chain.proceed(request)
        }

        val clientId = extractClientId(request)
        val config = determineRateLimitConfig(request, clientId)
        val result = rateLimiter.checkLimit(clientId, config)

        return if (result.allowed) {
            // Add rate limit headers to successful responses
            Flux.from(chain.proceed(request)).map { response ->
                addRateLimitHeaders(response, result, config)
            }
        } else {
            // Return 429 Too Many Requests
            logger.warn("Rate limit exceeded for client: {} on path: {}", clientId, request.path)
            Mono.just(createRateLimitExceededResponse(result, config))
        }
    }

    private fun shouldSkipRateLimit(request: HttpRequest<*>): Boolean {
        val path = request.path
        return path.startsWith("/health") ||
                path.startsWith("/prometheus") ||
                (path.startsWith("/debug") && rateLimitConfig.skipDebugEndpoints)
    }

    private fun extractClientId(request: HttpRequest<*>): String {
        // Priority: User ID from JWT > API Key > IP Address
        return request.headers.get("X-User-ID")
            ?: request.headers.get("X-API-Key")
            ?: getClientIpAddress(request)
    }

    private fun getClientIpAddress(request: HttpRequest<*>): String {
        return request.headers.get("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.headers.get("X-Real-IP")
            ?: request.remoteAddress.address.hostAddress
            ?: "unknown"
    }

    private fun determineRateLimitConfig(request: HttpRequest<*>, clientId: String): RateLimitConfig {
        // Check for user-specific limits first
        val userLimits = rateLimitConfig.userLimits[clientId]
        if (userLimits != null) {
            return userLimits
        }

        // Check for path-specific limits
        val pathLimits = rateLimitConfig.pathLimits.entries
            .find { (pattern, _) -> request.path.matches(pattern.toRegex()) }
            ?.value

        return pathLimits ?: rateLimitConfig.defaultLimit
    }

    private fun addRateLimitHeaders(
        response: MutableHttpResponse<*>,
        result: RateLimitResult,
        config: RateLimitConfig
    ): MutableHttpResponse<*> {
        return response.header(RATE_LIMIT_LIMIT_HEADER, config.requestsPerMinute.toString())
            .header(RATE_LIMIT_REMAINING_HEADER, result.remaining.toString())
            .header(RATE_LIMIT_RESET_HEADER, result.resetTime.toString())
    }

    private fun createRateLimitExceededResponse(
        result: RateLimitResult,
        config: RateLimitConfig
    ): MutableHttpResponse<String> {
        val responseBody = """
            {
                "error": "Rate limit exceeded",
                "message": "Too many requests. Limit: ${config.requestsPerMinute} requests per minute",
                "retryAfter": ${result.retryAfterSeconds},
                "resetTime": ${result.resetTime}
            }
        """.trimIndent()

        return HttpResponse.status<String>(HttpStatus.TOO_MANY_REQUESTS)
            .header(RATE_LIMIT_LIMIT_HEADER, config.requestsPerMinute.toString())
            .header(RATE_LIMIT_REMAINING_HEADER, "0")
            .header(RATE_LIMIT_RESET_HEADER, result.resetTime.toString())
            .header(RETRY_AFTER_HEADER, result.retryAfterSeconds.toString())
            .body(responseBody)
    }
}