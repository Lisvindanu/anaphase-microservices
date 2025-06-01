// ===================================================================
// services/gateway/src/main/kotlin/org/anaphase/gateway/router/GatewayRoutingController.kt (SIMPLE FIX)
// ===================================================================

package org.anaphase.gateway.router

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/gateway")
@ExecuteOn(TaskExecutors.BLOCKING)  // Simple fix - run on blocking thread pool
class GatewayRoutingController(
    private val gatewayRouter: GatewayRouter
) {

    private val logger = LoggerFactory.getLogger(GatewayRoutingController::class.java)

    init {
        logger.info("GatewayRoutingController initialized with router: {}", gatewayRouter::class.simpleName)
    }

    @Get("/{+path}")
    fun handleGet(@PathVariable path: String, request: HttpRequest<*>): HttpResponse<*> {
        logger.info("Handling GET request: {} (path: {})", request.path, path)
        return gatewayRouter.route(request)
    }

    @Post("/{+path}")
    fun handlePost(@PathVariable path: String, request: HttpRequest<*>): HttpResponse<*> {
        logger.info("Handling POST request: {} (path: {})", request.path, path)
        return gatewayRouter.route(request)
    }

    @Put("/{+path}")
    fun handlePut(@PathVariable path: String, request: HttpRequest<*>): HttpResponse<*> {
        logger.info("Handling PUT request: {} (path: {})", request.path, path)
        return gatewayRouter.route(request)
    }

    @Delete("/{+path}")
    fun handleDelete(@PathVariable path: String, request: HttpRequest<*>): HttpResponse<*> {
        logger.info("Handling DELETE request: {} (path: {})", request.path, path)
        return gatewayRouter.route(request)
    }

    @Get("/test")
    fun testController(): Map<String, Any> {
        logger.info("Gateway routing controller test endpoint called")
        return mapOf(
            "message" to "Gateway routing controller is working!",
            "controller" to "GatewayRoutingController",
            "router_injected" to true,
            "timestamp" to System.currentTimeMillis(),
            "routing_status" to "FULLY_FUNCTIONAL"
        )
    }
}