package com.ex.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    /**
     * Load Balancer Configuration
     * Spring Cloud Gateway automatically uses Spring Cloud LoadBalancer
     * when it sees the lb:// prefix in the URI
     *
     * The load balancing strategy is Round Robin by default
     * You can customize it by creating a custom LoadBalancerClient configuration
     */

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .build();
    }
}
