package org.anaphase.gateway.registry

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.anaphase.gateway.routing.ServiceInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@MicronautTest
class ManualServiceRegistryTest {

    @Inject
    lateinit var serviceRegistry: ServiceRegistry

    @Test
    fun `should have default discovery service registered`() {
        val instances = serviceRegistry.getHealthyInstances("discovery")

        assertFalse(instances.isEmpty())
        assertEquals(1, instances.size)

        val discoveryInstance = instances.first()
        assertEquals("discovery-1", discoveryInstance.id)
        assertEquals("discovery", discoveryInstance.name)
        assertEquals("localhost", discoveryInstance.host)
        assertEquals(8081, discoveryInstance.port)
        assertTrue(discoveryInstance.healthy)
    }

    @Test
    fun `should have default catalog service registered`() {
        val instances = serviceRegistry.getHealthyInstances("catalog")

        assertFalse(instances.isEmpty())
        assertEquals(1, instances.size)

        val catalogInstance = instances.first()
        assertEquals("catalog-1", catalogInstance.id)
        assertEquals("catalog", catalogInstance.name)
        assertEquals("localhost", catalogInstance.host)
        assertEquals(8090, catalogInstance.port)
    }

    @Test
    fun `should return empty list for unknown service`() {
        val instances = serviceRegistry.getHealthyInstances("unknown-service")
        assertTrue(instances.isEmpty())
    }

    @Test
    fun `should register and retrieve new service instance`() {
        val newInstance = ServiceInstance(
            id = "test-1",
            name = "test-service",
            host = "localhost",
            port = 9999,
            healthy = true
        )

        serviceRegistry.registerInstance(newInstance)

        val instances = serviceRegistry.getHealthyInstances("test-service")
        assertEquals(1, instances.size)
        assertEquals("test-1", instances.first().id)
    }

    @Test
    fun `should get all registered services`() {
        val allServices = serviceRegistry.getAllServices()

        assertTrue(allServices.containsKey("discovery"))
        assertTrue(allServices.containsKey("catalog"))
        assertTrue(allServices["discovery"]!!.isNotEmpty())
        assertTrue(allServices["catalog"]!!.isNotEmpty())
    }
}