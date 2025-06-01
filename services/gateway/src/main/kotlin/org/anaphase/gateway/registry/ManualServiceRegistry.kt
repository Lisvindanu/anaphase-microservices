package org.anaphase.gateway.registry

import jakarta.inject.Singleton
import org.anaphase.gateway.routing.ServiceInstance
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ManualServiceRegistry : ServiceRegistry {

    private val logger = LoggerFactory.getLogger(ManualServiceRegistry::class.java)
    private val services = ConcurrentHashMap<String, MutableList<ServiceInstance>>()

    init {
        registerDefaultInstances()
    }

    private fun registerDefaultInstances() {
        // Register discovery service instance
        val discoveryInstance = ServiceInstance(
            id = "discovery-1",
            name = "discovery",
            host = "localhost",
            port = 8081,
            healthy = true
        )
        registerInstance(discoveryInstance)

        // Register additional test instances
        val catalogInstance = ServiceInstance(
            id = "catalog-1",
            name = "catalog",
            host = "localhost",
            port = 8090,
            healthy = true
        )
        registerInstance(catalogInstance)

        logger.info("Registered default service instances for testing")
    }

    override fun getHealthyInstances(serviceName: String): List<ServiceInstance> {
        val instances = services[serviceName] ?: emptyList()
        val healthyInstances = instances.filter { it.healthy }

        logger.debug("Found {} healthy instances for service: {}", healthyInstances.size, serviceName)
        return healthyInstances
    }

    override fun getAllServices(): Map<String, List<ServiceInstance>> {
        return services.mapValues { (_, instances) ->
            instances.filter { it.healthy }
        }
    }

    override fun registerInstance(instance: ServiceInstance) {
        services.computeIfAbsent(instance.name) { mutableListOf() }.add(instance)
        logger.info("Registered instance: {} for service: {}", instance.id, instance.name)
    }

    override fun deregisterInstance(serviceId: String) {
        services.values.forEach { instances ->
            instances.removeIf { it.id == serviceId }
        }
        logger.info("Deregistered instance: {}", serviceId)
    }
}