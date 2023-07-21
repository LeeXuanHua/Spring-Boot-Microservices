package com.microservices.orderservice.controller;

import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final Tracer tracer;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody @Validated OrderRequest orderRequest) {
        log.info("Placing Order");
//        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));

        // Instead of starting a new trace ID due to CompletableFuture, use the current trace ID
        Span currentSpan = tracer.nextSpan().name("OrderService-SupplyAsync");

        return CompletableFuture.supplyAsync(() -> {
            try (Tracer.SpanInScope spanInScope = tracer.withSpan(currentSpan.start())) {
                return orderService.placeOrder(orderRequest);
            } finally {
                currentSpan.end(); // End the current span to ensure proper tracing
            }
        });
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        log.info("Cannot Place Order Executing Fallback logic");
//        return CompletableFuture.supplyAsync(() -> "Oops! Inventory service is down, please try again later.");

        // Instead of starting a new trace ID due to CompletableFuture, use the current trace ID
        Span currentSpan = tracer.nextSpan().name("OrderService-SupplyAsyncFallback");

        return CompletableFuture.supplyAsync(() -> {
            try (Tracer.SpanInScope spanInScope = tracer.withSpan(currentSpan.start())) {
                return "Oops! Inventory service is down, please try again later.";
            } finally {
                currentSpan.end(); // End the current span to ensure proper tracing
            }
        });
    }
}