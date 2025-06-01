package org.anaphase.gateway.loadbalancer

import org.anaphase.gateway.routing.ServiceInstance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RoundRobinLoadBalancer : LoadBalancer {
    private val counters = ConcurrentHashMap<String, AtomicInteger>()

    override fun selectInstance(instances: List<ServiceInstance>): ServiceInstance? {
        if (instances.isEmpty()) return null

        val serviceName = instances.first().name
        val counter = counters.computeIfAbsent(serviceName) { AtomicInteger(0) }
        val index = counter.getAndIncrement() % instances.size

        return instances[index]
    }
}