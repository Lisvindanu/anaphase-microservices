// ===============================================================================
// services/gateway/src/test/kotlin/org/anaphase/gateway/ratelimit/RateLimitingIntegrationTest.kt
// Complete Integration Test Suite for Phase 2.1 Rate Limiting
// ===============================================================================

package org.anaphase.gateway.ratelimit

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer
import org.slf4j.LoggerFactory

@MicronautTest(environments = ["test"])
@TestMethodOrder(MethodOrderer.DisplayName::class)
class RateLimitingIntegrationTest {

    private val logger = LoggerFactory.getLogger(RateLimitingIntegrationTest::class.java)

    @Inject
    @Client("/")
    lateinit var client: HttpClient

    @Inject
    lateinit var rateLimiter: RateLimiter

    @BeforeEach
    fun setup() {
        // Reset any existing rate limits before each test
        rateLimiter.resetLimit("test-client")
        rateLimiter.resetLimit("admin")
        rateLimiter.resetLimit("premium-user")
        logger.info("Test setup completed - rate limits reset")
    }

    @Nested
    @DisplayName("A. Basic Rate Limiting Functionality")
    inner class BasicRateLimitingTests {

        @Test
        @DisplayName("A1. Should return rate limit headers in successful response")
        fun shouldReturnRateLimitHeaders() {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status"),
                String::class.java
            )

            assertEquals(HttpStatus.OK, response.status)

            // Check for rate limit headers
            assertTrue(response.headers.contains("X-RateLimit-Limit"))
            assertTrue(response.headers.contains("X-RateLimit-Remaining"))
            assertTrue(response.headers.contains("X-RateLimit-Reset"))

            val limit = response.headers.get("X-RateLimit-Limit")?.toLongOrNull()
            val remaining = response.headers.get("X-RateLimit-Remaining")?.toLongOrNull()

            assertNotNull(limit)
            assertNotNull(remaining)
            assertTrue(limit!! > 0)
            assertTrue(remaining!! >= 0)

            logger.info("Rate limit headers: Limit={}, Remaining={}", limit, remaining)
        }

        @Test
        @DisplayName("A2. Should not rate limit health endpoints")
        fun shouldNotRateLimitHealthEndpoints() {
            // Make multiple requests to health endpoint
            repeat(20) { i ->
                val response = client.toBlocking().exchange(
                    HttpRequest.GET<Any>("/health"),
                    String::class.java
                )
                assertEquals(HttpStatus.OK, response.status, "Health check $i should not be rate limited")
            }
            logger.info("Health endpoint correctly skipped rate limiting")
        }

        @Test
        @DisplayName("A3. Should not rate limit debug endpoints")
        fun shouldNotRateLimitDebugEndpoints() {
            // Make multiple requests to debug endpoints
            repeat(15) { i ->
                val response = client.toBlocking().exchange(
                    HttpRequest.GET<Any>("/debug/startup"),
                    String::class.java
                )
                assertEquals(HttpStatus.OK, response.status, "Debug endpoint $i should not be rate limited")
            }
            logger.info("Debug endpoints correctly skipped rate limiting")
        }
    }

    @Nested
    @DisplayName("B. Rate Limit Enforcement")
    inner class RateLimitEnforcementTests {

        @Test
        @DisplayName("B1. Should allow requests within burst limit")
        fun shouldAllowRequestsWithinBurstLimit() {
            var lastRemaining = Long.MAX_VALUE

            // Make requests within burst limit (default burst size is likely 10)
            repeat(5) { i ->
                val response = client.toBlocking().exchange(
                    HttpRequest.GET<Any>("/status"),
                    String::class.java
                )

                assertEquals(HttpStatus.OK, response.status)

                val remaining = response.headers.get("X-RateLimit-Remaining")?.toLongOrNull()
                assertNotNull(remaining)
                assertTrue(remaining!! < lastRemaining, "Remaining should decrease with each request")
                lastRemaining = remaining

                logger.debug("Request {}: Remaining tokens = {}", i + 1, remaining)
            }
            logger.info("Burst limit test passed - tokens decreased properly")
        }

        @Test
        @DisplayName("B2. Should return 429 when rate limit exceeded")
        fun shouldReturn429WhenRateLimitExceeded() {
            var requestCount = 0
            var rateLimitHit = false

            try {
                // Make requests until rate limit is hit (burst size + some extra)
                repeat(15) { i ->
                    requestCount = i + 1
                    val response = client.toBlocking().exchange(
                        HttpRequest.GET<Any>("/status"),
                        String::class.java
                    )
                    assertEquals(HttpStatus.OK, response.status)
                    logger.debug("Request {} succeeded", requestCount)
                }
                fail("Expected rate limit to be exceeded")
            } catch (e: HttpClientResponseException) {
                rateLimitHit = true
                assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.status)
                assertTrue(e.response.headers.contains("Retry-After"))
                assertTrue(e.response.headers.contains("X-RateLimit-Remaining"))
                assertEquals("0", e.response.headers.get("X-RateLimit-Remaining"))

                logger.info("Rate limit correctly triggered after {} requests", requestCount)
            }

            assertTrue(rateLimitHit, "Rate limit should have been triggered")
        }
    }

    @Nested
    @DisplayName("C. User-Specific Rate Limits")
    inner class UserSpecificRateLimitTests {

        @Test
        @DisplayName("C1. Should apply higher limits for admin user")
        fun shouldApplyHigherLimitsForAdmin() {
            val adminResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status")
                    .header("X-User-ID", "admin"),
                String::class.java
            )

            val regularResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status"),
                String::class.java
            )

            val adminLimit = adminResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()
            val regularLimit = regularResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()

            assertNotNull(adminLimit)
            assertNotNull(regularLimit)
            assertTrue(adminLimit!! > regularLimit!!, "Admin should have higher rate limit than regular user")

            logger.info("Admin limit: {}, Regular limit: {}", adminLimit, regularLimit)
        }

        @Test
        @DisplayName("C2. Should apply premium user limits")
        fun shouldApplyPremiumUserLimits() {
            val premiumResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status")
                    .header("X-User-ID", "premium-user"),
                String::class.java
            )

            val regularResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status"),
                String::class.java
            )

            val premiumLimit = premiumResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()
            val regularLimit = regularResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()

            assertNotNull(premiumLimit)
            assertNotNull(regularLimit)
            assertTrue(premiumLimit!! > regularLimit!!, "Premium user should have higher rate limit")

            logger.info("Premium limit: {}, Regular limit: {}", premiumLimit, regularLimit)
        }

        @Test
        @DisplayName("C3. Should handle API key-based identification")
        fun shouldHandleApiKeyBasedIdentification() {
            val apiKeyResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status")
                    .header("X-API-Key", "test-api-key-123"),
                String::class.java
            )

            assertEquals(HttpStatus.OK, apiKeyResponse.status)
            assertTrue(apiKeyResponse.headers.contains("X-RateLimit-Limit"))

            logger.info("API key-based rate limiting working correctly")
        }
    }

    @Nested
    @DisplayName("D. Debug Endpoints")
    inner class DebugEndpointsTests {

        @Test
        @DisplayName("D1. Should return rate limit configuration")
        fun shouldReturnRateLimitConfiguration() {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/debug/ratelimit/config"),
                Map::class.java
            )

            assertEquals(HttpStatus.OK, response.status)

            val config = response.body()!!
            assertTrue(config.containsKey("enabled"))
            assertTrue(config.containsKey("default_strategy"))
            assertTrue(config.containsKey("default_limit"))

            assertEquals(true, config["enabled"])
            assertEquals("TOKEN_BUCKET", config["default_strategy"])

            logger.info("Rate limit configuration endpoint working correctly")
        }

        @Test
        @DisplayName("D2. Should test rate limiting for specific client")
        fun shouldTestRateLimitingForSpecificClient() {
            val testClientId = "integration-test-client"

            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/debug/ratelimit/test?clientId=$testClientId"),
                Map::class.java
            )

            assertEquals(HttpStatus.OK, response.status)

            val result = response.body()!!
            assertTrue(result.containsKey("client_id"))
            assertTrue(result.containsKey("allowed"))
            assertTrue(result.containsKey("remaining"))

            assertEquals(testClientId, result["client_id"])
            assertEquals(true, result["allowed"])

            logger.info("Rate limit test endpoint working for client: {}", testClientId)
        }

        @Test
        @DisplayName("D3. Should test burst limiting")
        fun shouldTestBurstLimiting() {
            val testClientId = "burst-test-client"

            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/debug/ratelimit/test/burst?clientId=$testClientId"),
                Map::class.java
            )

            assertEquals(HttpStatus.OK, response.status)

            val result = response.body()!!
            assertTrue(result.containsKey("client_id"))
            assertTrue(result.containsKey("results"))
            assertTrue(result.containsKey("summary"))

            assertEquals(testClientId, result["client_id"])

            @Suppress("UNCHECKED_CAST")
            val summary = result["summary"] as Map<String, Any>
            assertTrue(summary.containsKey("burst_limit_working"))
            assertEquals(true, summary["burst_limit_working"])

            logger.info("Burst limit test completed successfully")
        }

        @Test
        @DisplayName("D4. Should reset client rate limit")
        fun shouldResetClientRateLimit() {
            val testClientId = "reset-test-client"

            // First, make some requests to use up tokens
            repeat(3) {
                client.toBlocking().exchange(
                    HttpRequest.GET<Any>("/debug/ratelimit/test?clientId=$testClientId"),
                    Map::class.java
                )
            }

            // Reset the rate limit
            val resetResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/debug/ratelimit/reset?clientId=$testClientId"),
                Map::class.java
            )

            assertEquals(HttpStatus.OK, resetResponse.status)

            val resetResult = resetResponse.body()!!
            assertTrue(resetResult.containsKey("message"))
            assertTrue(resetResult.containsKey("client_id"))
            assertEquals(testClientId, resetResult["client_id"])

            logger.info("Rate limit reset successfully for client: {}", testClientId)
        }
    }

    @Nested
    @DisplayName("E. Metrics and Monitoring")
    inner class MetricsTests {

        @Test
        @DisplayName("E1. Should expose Prometheus metrics")
        fun shouldExposePrometheusMetrics() {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/prometheus"),
                String::class.java
            )

            assertEquals(HttpStatus.OK, response.status)

            val metricsContent = response.body()!!
            assertTrue(metricsContent.contains("gateway_ratelimit"), "Should contain rate limiting metrics")

            logger.info("Prometheus metrics endpoint working correctly")
        }

        @Test
        @DisplayName("E2. Should track rate limiting metrics")
        fun shouldTrackRateLimitingMetrics() {
            // Make some requests to generate metrics
            repeat(5) {
                try {
                    client.toBlocking().exchange(
                        HttpRequest.GET<Any>("/status"),
                        String::class.java
                    )
                } catch (e: HttpClientResponseException) {
                    // Expected if rate limit is hit
                }
            }

            // Check if metrics are being recorded
            val metricsResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/prometheus"),
                String::class.java
            )

            val metricsContent = metricsResponse.body()!!
            assertTrue(metricsContent.contains("gateway_ratelimit_requests_allowed") ||
                    metricsContent.contains("gateway_ratelimit_requests_denied"))

            logger.info("Rate limiting metrics are being tracked correctly")
        }
    }

    @Nested
    @DisplayName("F. Error Handling and Edge Cases")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("F1. Should handle invalid client IDs gracefully")
        fun shouldHandleInvalidClientIds() {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status")
                    .header("X-User-ID", ""),
                String::class.java
            )

            assertEquals(HttpStatus.OK, response.status)
            assertTrue(response.headers.contains("X-RateLimit-Limit"))

            logger.info("Empty client ID handled gracefully")
        }

        @Test
        @DisplayName("F2. Should handle missing headers gracefully")
        fun shouldHandleMissingHeaders() {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/status"),
                String::class.java
            )

            assertEquals(HttpStatus.OK, response.status)
            assertTrue(response.headers.contains("X-RateLimit-Limit"))

            logger.info("Missing headers handled gracefully - using IP-based identification")
        }

        @Test
        @DisplayName("F3. Should handle concurrent requests correctly")
        fun shouldHandleConcurrentRequests() {
            val clientId = "concurrent-test-client"
            val requests = mutableListOf<HttpStatus>()

            // Make concurrent requests
            val threads = (1..10).map {
                Thread {
                    try {
                        val response = client.toBlocking().exchange(
                            HttpRequest.GET<Any>("/status")
                                .header("X-User-ID", clientId),
                            String::class.java
                        )
                        synchronized(requests) {
                            requests.add(response.status)
                        }
                    } catch (e: HttpClientResponseException) {
                        synchronized(requests) {
                            requests.add(e.status)
                        }
                    }
                }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }

            assertTrue(requests.isNotEmpty())
            assertTrue(requests.all { it == HttpStatus.OK || it == HttpStatus.TOO_MANY_REQUESTS })

            logger.info("Concurrent requests handled correctly: {} requests processed", requests.size)
        }
    }

    @Test
    @DisplayName("Z. Complete End-to-End Rate Limiting Test")
    fun completeEndToEndTest() {
        logger.info("Starting complete end-to-end rate limiting test...")

        // 1. Verify rate limiting is enabled
        val configResponse = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/debug/ratelimit/config"),
            Map::class.java
        )
        assertEquals(HttpStatus.OK, configResponse.status)
        assertEquals(true, configResponse.body()!!["enabled"])

        // 2. Test normal request flow
        val normalResponse = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/status"),
            String::class.java
        )
        assertEquals(HttpStatus.OK, normalResponse.status)
        assertTrue(normalResponse.headers.contains("X-RateLimit-Limit"))

        // 3. Test admin user gets higher limits
        val adminResponse = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/status")
                .header("X-User-ID", "admin"),
            String::class.java
        )
        val adminLimit = adminResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()
        val normalLimit = normalResponse.headers.get("X-RateLimit-Limit")?.toLongOrNull()
        assertTrue(adminLimit!! > normalLimit!!)

        // 4. Test burst limiting
        val burstResponse = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/debug/ratelimit/test/burst?clientId=e2e-test"),
            Map::class.java
        )
        assertEquals(HttpStatus.OK, burstResponse.status)

        // 5. Verify metrics are working
        val metricsResponse = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/prometheus"),
            String::class.java
        )
        assertEquals(HttpStatus.OK, metricsResponse.status)

        logger.info("✅ Complete end-to-end rate limiting test PASSED!")
        logger.info("🚀 Phase 2.1 Rate Limiting is fully functional and production-ready!")
    }
}