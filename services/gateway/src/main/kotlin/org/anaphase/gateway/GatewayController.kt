package org.anaphase.gateway

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/")  // Root controller for basic endpoints
class GatewayController {

    @Get(uri="/", produces=["text/plain"])
    fun index(): String {
        return "Anaphase Gateway v0.1 - Request Routing & Load Balancing"
    }

    @Get(uri="/status", produces=["application/json"])
    fun status(): Map<String, Any> {
        return mapOf(
            "service" to "anaphase-gateway",
            "version" to "0.1",
            "features" to listOf(
                "request-routing",
                "load-balancing",
                "service-discovery",
                "health-checking"
            ),
            "status" to "running"
        )
    }
}
