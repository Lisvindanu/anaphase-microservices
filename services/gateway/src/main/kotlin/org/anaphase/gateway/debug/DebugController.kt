package org.anaphase.gateway.debug

import io.micronaut.context.ApplicationContext
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.anaphase.gateway.loadbalancer.LoadBalancerFactory
import org.anaphase.gateway.registry.ServiceRegistry
import org.anaphase.gateway.router.GatewayRouter
import org.anaphase.gateway.router.GatewayRoutesConfiguration

@Controller("/debug")
class DebugController(
    private val applicationContext: ApplicationContext,
    private val serviceRegistry: ServiceRegistry,
    private val routesConfig: GatewayRoutesConfiguration,
    private val gatewayRouter: GatewayRouter? = null,
    private val loadBalancerFactory: LoadBalancerFactory? = null
) {

    @Get("/startup")
    fun debugStartup(): Map<String, Any> {
        return mapOf(
            "context_active" to applicationContext.isRunning,
            "service_registry_injected" to true,
            "routes_config_injected" to true,
            "gateway_router_injected" to (gatewayRouter != null),
            "load_balancer_factory_injected" to (loadBalancerFactory != null),
            "routes_count" to routesConfig.routes.size,
            "routes_details" to routesConfig.routes.map { route ->
                mapOf(
                    "id" to route.id,
                    "path" to route.path,
                    "serviceName" to route.serviceName,
                    "loadBalancingStrategy" to route.loadBalancingStrategy.name
                )
            },
            "beans_status" to mapOf(
                "ServiceRegistry" to "INJECTED",
                "GatewayRoutesConfiguration" to "INJECTED",
                "GatewayRouter" to if (gatewayRouter != null) "INJECTED" else "FAILED",
                "LoadBalancerFactory" to if (loadBalancerFactory != null) "INJECTED" else "FAILED"
            )
        )
    }

    @Get("/routes")
    fun getRoutes(): Map<String, Any> {
        return mapOf(
            "configured_routes" to routesConfig.routes.map { route ->
                mapOf(
                    "id" to route.id,
                    "path" to route.path,
                    "serviceName" to route.serviceName,
                    "loadBalancingStrategy" to route.loadBalancingStrategy.name,
                    "stripPrefix" to route.stripPrefix,
                    "retryAttempts" to route.retryAttempts,
                    "timeoutMs" to route.timeoutMs
                )
            },
            "total_routes" to routesConfig.routes.size
        )
    }

    @Get("/services")
    fun getServices(): Map<String, Any> {
        val allServices = serviceRegistry.getAllServices()
        return mapOf(
            "registered_services" to allServices.mapValues { (_, instances) ->
                instances.map { instance ->
                    mapOf(
                        "id" to instance.id,
                        "host" to instance.host,
                        "port" to instance.port,
                        "healthy" to instance.healthy,
                        "baseUrl" to instance.baseUrl,
                        "weight" to instance.weight
                    )
                }
            },
            "total_services" to allServices.size,
            "total_instances" to allServices.values.sumOf { it.size }
        )
    }

    @Get("/test/discovery")
    fun testDiscoveryRoute(): Map<String, Any> {
        val discoveryInstances = serviceRegistry.getHealthyInstances("discovery")
        return mapOf(
            "service" to "discovery",
            "instances_found" to discoveryInstances.size,
            "instances" to discoveryInstances.map { instance ->
                mapOf(
                    "id" to instance.id,
                    "baseUrl" to instance.baseUrl,
                    "healthy" to instance.healthy
                )
            },
            "test_urls" to listOf(
                "/gateway/discovery/",
                "/gateway/api/discovery/"
            ),
            "gateway_router_available" to (gatewayRouter != null)
        )
    }

    @Get("/test")
    fun testDebug(): Map<String, Any> {
        return mapOf(
            "message" to "Debug controller working",
            "timestamp" to System.currentTimeMillis(),
            "gateway_router_available" to (gatewayRouter != null),
            "all_endpoints" to listOf(
                "/debug/startup",
                "/debug/routes",
                "/debug/services",
                "/debug/test/discovery",
                "/debug/test"
            )
        )
    }
}