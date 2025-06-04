
package org.anaphase.gateway.router

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import jakarta.inject.Singleton
import org.anaphase.gateway.loadbalancer.LoadBalancerFactory
import org.anaphase.gateway.registry.ServiceRegistry
import org.anaphase.gateway.routing.RouteDefinition
import org.anaphase.gateway.routing.ServiceInstance
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Singleton
class GatewayRouter(
    private val serviceRegistry: ServiceRegistry,
    private val httpClient: HttpClient,
    private val routesConfig: GatewayRoutesConfiguration,
    private val loadBalancerFactory: LoadBalancerFactory
) {

    private val logger = LoggerFactory.getLogger(GatewayRouter::class.java)
    private val routeCache = ConcurrentHashMap<String, RouteDefinition>()

    init {
        routesConfig.routes.forEach { route ->
            routeCache[route.path] = route
        }
        logger.info("Initialized Gateway Router with {} routes", routeCache.size)
    }

    fun route(request: HttpRequest<*>): HttpResponse<*> {
        val path = request.path
        val matchedRoute = findMatchingRoute(path)

        if (matchedRoute == null) {
            logger.warn("No route found for path: {}", path)
            return HttpResponse.notFound<String>("Route not found: $path")
        }

        val targetInstances = serviceRegistry.getHealthyInstances(matchedRoute.serviceName)
        if (targetInstances.isEmpty()) {
            logger.error("No healthy instances found for service: {}", matchedRoute.serviceName)
            return HttpResponse.serverError<String>("Service unavailable: ${matchedRoute.serviceName}")
        }

        val loadBalancer = loadBalancerFactory.create(matchedRoute.loadBalancingStrategy)
        val selectedInstance = loadBalancer.selectInstance(targetInstances)

        if (selectedInstance == null) {
            logger.error("Load balancer failed to select instance for service: {}", matchedRoute.serviceName)
            return HttpResponse.serverError<String>("Load balancing failed")
        }

        return forwardRequest(request, selectedInstance, matchedRoute)
    }

    private fun findMatchingRoute(path: String): RouteDefinition? {
        // Exact match first
        routeCache[path]?.let { return it }

        // Pattern matching for dynamic routes
        return routeCache.values.find { route ->
            when {
                route.path.endsWith("/**") -> {
                    val prefix = route.path.removeSuffix("/**")
                    path.startsWith(prefix)
                }
                route.path.contains("{") -> false // TODO: Path variables
                else -> false
            }
        }
    }

    private fun forwardRequest(
        originalRequest: HttpRequest<*>,
        targetInstance: ServiceInstance,
        route: RouteDefinition
    ): HttpResponse<*> {
        return try {
            val targetPath = if (route.stripPrefix) {
                val prefix = route.path.removeSuffix("/**")
                originalRequest.path.removePrefix(prefix)
            } else {
                originalRequest.path
            }

            val targetUrl = "${targetInstance.baseUrl}$targetPath"
            logger.debug("Forwarding {} {} to {}", originalRequest.method, originalRequest.path, targetUrl)

            // Create new request for forwarding
            val forwardRequest = when (originalRequest.method.name) {
                "GET" -> HttpRequest.GET<Any>(targetUrl)
                "POST" -> HttpRequest.POST(targetUrl, originalRequest.body.orElse(null))
                "PUT" -> HttpRequest.PUT(targetUrl, originalRequest.body.orElse(null))
                "DELETE" -> HttpRequest.DELETE<Any>(targetUrl)
                else -> {
                    return HttpResponse.badRequest<String>("Unsupported method: ${originalRequest.method}")
                }
            }

            // Copy headers (excluding host and content-length)
            originalRequest.headers.forEach { (name, values) ->
                if (name.lowercase() !in listOf("host", "content-length")) {
                    values.forEach { value ->
                        forwardRequest.headers.add(name, value)
                    }
                }
            }

            // Execute request
            httpClient.toBlocking().exchange(forwardRequest, String::class.java)

        } catch (e: Exception) {
            logger.error("Failed to forward request to {}:{}", targetInstance.host, targetInstance.port, e)
            HttpResponse.serverError<String>("Request forwarding failed: ${e.message}")
        }
    }
}
