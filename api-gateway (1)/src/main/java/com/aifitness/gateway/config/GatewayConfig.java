
package com.aifitness.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("activity-service", route -> route
                        .path("/activity-service/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri("lb://ACTIVITY-SERVICE"))

                .route("ai-service", route -> route
                        .path("/ai-service/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri("lb://AI-SERVICE"))

                .build();
    }
}
