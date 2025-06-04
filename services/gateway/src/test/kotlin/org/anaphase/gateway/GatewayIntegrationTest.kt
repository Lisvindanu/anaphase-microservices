package org.anaphase.gateway

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@MicronautTest
class GatewayIntegrationTest {

    @Inject
    @Client("/")
    lateinit var client: HttpClient

    @Test
    fun `gateway should start successfully`() {
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/health"),
            String::class.java
        )
        assertEquals(HttpStatus.OK, response.status)
    }

    @Test
    fun `should return gateway status`() {
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/status"),
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.status)
        val body = response.body()
        assertNotNull(body)
        assertEquals("anaphase-gateway", body!!["service"])
        assertEquals("0.1", body["version"])
    }

    @Test
    fun `should return gateway index page`() {
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/"),
            String::class.java
        )

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(response.body()!!.contains("Anaphase Gateway"))
    }

    @Test
    fun `should handle unmatched gateway route with proper error response`() {
        try {
            client.toBlocking().exchange(
                HttpRequest.GET<Any>("/gateway/unknown/path"),
                String::class.java
            )
            fail("Expected HttpClientResponseException")
        } catch (e: HttpClientResponseException) {
            // Expected behavior - routing should work but return an error response
            // Could be 404 (route not found) or 500 (service unavailable)
            assertTrue(
                e.status == HttpStatus.NOT_FOUND ||
                        e.status == HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
    }

    @Test
    fun `should handle gateway route to discovery service with proper error handling`() {
        try {
            val response = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/gateway/discovery/"),
                String::class.java
            )
            // If we get here, routing worked and discovery service responded
            assertEquals(HttpStatus.OK, response.status)
        } catch (e: HttpClientResponseException) {
            // Expected if discovery service is not running
            // Gateway should return proper error response (500 - Service Unavailable)
            assertTrue(
                e.status == HttpStatus.INTERNAL_SERVER_ERROR ||
                        e.status == HttpStatus.NOT_FOUND
            )
        }
    }

    @Test
    fun `should access swagger ui documentation`() {
        val response = client.toBlocking().exchange(
            HttpRequest.GET<Any>("/swagger-ui"),
            String::class.java
        )

        // Should redirect or serve swagger UI
        assertTrue(
            response.status == HttpStatus.OK ||
                    response.status == HttpStatus.MOVED_PERMANENTLY ||
                    response.status == HttpStatus.FOUND
        )
    }
}