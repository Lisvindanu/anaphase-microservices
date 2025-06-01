package org.anaphase.gateway.loadbalancer

import org.anaphase.gateway.routing.ServiceInstance
import kotlin.random.Random

class RandomLoadBalancer : LoadBalancer {
    override fun selectInstance(instances: List<ServiceInstance>): ServiceInstance? {
        if (instances.isEmpty()) return null
        return instances[Random.nextInt(instances.size)]
    }
}