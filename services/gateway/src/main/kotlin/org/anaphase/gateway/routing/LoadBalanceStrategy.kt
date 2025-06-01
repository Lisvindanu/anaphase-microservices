package org.anaphase.gateway.routing

enum class LoadBalancingStrategy {
    ROUND_ROBIN,
    WEIGHTED_ROUND_ROBIN,
    LEAST_CONNECTIONS,
    RANDOM
}