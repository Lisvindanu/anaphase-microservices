package org.anaphase.gateway.registry

import org.anaphase.gateway.routing.ServiceInstance

interface ServiceRegistry {
    fun getHealthyInstances(serviceName: String): List<ServiceInstance>
    fun getAllServices(): Map<String, List<ServiceInstance>>
    fun registerInstance(instance: ServiceInstance)
    fun deregisterInstance(serviceId: String)
}