package org.anaphase.gateway.loadbalancer

import org.anaphase.gateway.routing.LoadBalancingStrategy
import jakarta.inject.Singleton

@Singleton
class LoadBalancerFactory {
    fun create(strategy: LoadBalancingStrategy): LoadBalancer {
        return when (strategy) {
            LoadBalancingStrategy.ROUND_ROBIN -> RoundRobinLoadBalancer()
            LoadBalancingStrategy.WEIGHTED_ROUND_ROBIN -> WeightedRoundRobinLoadBalancer()
            LoadBalancingStrategy.RANDOM -> RandomLoadBalancer()
            LoadBalancingStrategy.LEAST_CONNECTIONS -> RoundRobinLoadBalancer() // TODO: Implement
        }
    }
}
