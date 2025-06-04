package org.anaphase.gateway.router

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import org.anaphase.gateway.routing.RouteDefinition

@ConfigurationProperties("gateway.routes")
@Introspected
class GatewayRoutesConfiguration {
    var routes: List<RouteDefinition> = emptyList()
}