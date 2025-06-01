package org.anaphase.gateway.routing

import io.micronaut.core.annotation.Introspected

@Introspected
data class RouteDefinition(
    val id: String,
    val path: String,
    val serviceName: String,
    val stripPrefix: Boolean = true,
    val retryAttempts: Int = 3,
    val timeoutMs: Long = 30000,
    val loadBalancingStrategy: LoadBalancingStrategy = LoadBalancingStrategy.ROUND_ROBIN
)