package org.anaphase.gateway.loadbalancer

import org.anaphase.gateway.routing.ServiceInstance

interface LoadBalancer {
    fun selectInstance(instances: List<ServiceInstance>): ServiceInstance?
}
