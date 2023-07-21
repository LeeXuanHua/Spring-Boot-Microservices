package com.microservices.orderservice.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This class is used to create a bean for WebClient to make calls to other microservices.
 * The injection approach is used to facilitate unit testing, and for central control/setting if applicable.
 */
@Configuration(proxyBeanMethods = false)
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

//    @Bean
//    public WebClient webClient(WebClient.Builder builder) {
//        return builder.build();
//    }

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}