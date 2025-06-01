package org.anaphase.gateway.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import org.slf4j.LoggerFactory

@Controller("/test")
class SimpleTestController {

    private val logger = LoggerFactory.getLogger(SimpleTestController::class.java)

    @Get("/simple")
    fun simple(): Map<String, Any> {
        return mapOf(
            "message" to "Simple test working",
            "controller" to "SimpleTestController"
        )
    }

    @Get("/gateway/{path}")
    fun testGatewayPath(@PathVariable path: String, request: HttpRequest<*>): Map<String, Any> {
        logger.info("Test gateway path called with: {}", path)
        return mapOf(
            "message" to "Gateway path test",
            "path" to path,
            "full_path" to request.path,
            "method" to request.method.name
        )
    }
}
