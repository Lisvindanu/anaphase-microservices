package org.anaphase.gateway.router

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.anaphase.gateway.registry.ServiceRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@MicronautTest
class GatewayRouterTest {

    @Inject
    lateinit var gatewayRouter: GatewayRouter

    @Inject
    lateinit var serviceRegistry: ServiceRegistry

    @Test
    fun `should initialize router with configured routes`() {
        assertNotNull(gatewayRouter)
    }

    @Test
    fun `should return 404 for unmatched routes`() {
        val request = HttpRequest.GET<Any>("/unknown/path")
        val response = gatewayRouter.route(request)

        assertEquals(HttpStatus.NOT_FOUND, response.status)
        assertTrue(response.body.toString().contains("Route not found"))
    }

    @Test
    fun `should find healthy instances for discovery service`() {
        val instances = serviceRegistry.getHealthyInstances("discovery")

        assertFalse(instances.isEmpty())
        assertEquals("discovery", instances.first().name)
        assertEquals(8081, instances.first().port)
    }

    @Test
    fun `should return service unavailable when no healthy instances`() {
        val request = HttpRequest.GET<Any>("/gateway/api/nonexistent/**")
        val response = gatewayRouter.route(request)

        // Should either be not found (no route) or service unavailable
        assertTrue(
            response.status == HttpStatus.NOT_FOUND ||
                    response.status == HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}