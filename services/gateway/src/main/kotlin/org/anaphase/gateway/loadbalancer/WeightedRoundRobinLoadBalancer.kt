package org.anaphase.gateway.loadbalancer

import org.anaphase.gateway.routing.ServiceInstance
import java.util.concurrent.ConcurrentHashMap

class WeightedRoundRobinLoadBalancer : LoadBalancer {
    private val currentWeights = ConcurrentHashMap<String, MutableMap<String, Int>>()

    override fun selectInstance(instances: List<ServiceInstance>): ServiceInstance? {
        if (instances.isEmpty()) return null

        val serviceName = instances.first().name
        val weights = currentWeights.computeIfAbsent(serviceName) {
            mutableMapOf<String, Int>().apply {
                instances.forEach { instance ->
                    this[instance.id] = 0
                }
            }
        }

        val totalWeight = instances.sumOf { it.weight }
        var selected: ServiceInstance? = null
        var maxCurrentWeight = Int.MIN_VALUE

        instances.forEach { instance ->
            val currentWeight = weights.getOrPut(instance.id) { 0 } + instance.weight
            weights[instance.id] = currentWeight

            if (currentWeight > maxCurrentWeight) {
                maxCurrentWeight = currentWeight
                selected = instance
            }
        }

        selected?.let { instance ->
            weights[instance.id] = maxCurrentWeight - totalWeight
        }

        return selected
    }
}