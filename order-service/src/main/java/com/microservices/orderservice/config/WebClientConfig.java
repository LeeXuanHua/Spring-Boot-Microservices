package com.microservices.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This class is used to create a bean for WebClient to make calls to other microservices.
 * The injection approach is used to facilitate unit testing, and for central control/setting if applicable.
 */
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {return WebClient.builder();}
}