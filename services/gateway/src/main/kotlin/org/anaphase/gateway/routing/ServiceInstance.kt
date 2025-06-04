package org.anaphase.gateway.routing

import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
data class ServiceInstance(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val scheme: String = "http",
    val metadata: Map<String, String> = emptyMap(),
    val healthy: Boolean = true,
    val lastHealthCheck: Instant = Instant.now(),
    val weight: Int = 1
) {
    val baseUrl: String get() = "$scheme://$host:$port"
}