package com.microservices.inventoryservice.controller;

import com.microservices.inventoryservice.dto.InventoryRequest;
import com.microservices.inventoryservice.dto.InventoryResponse;
import com.microservices.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
//    private final Tracer tracer;

    // If use PathVariable, sample request: http://localhost:8082/api/inventory/iphone-13,iphone-13-pro
    // If use RequestParam, sample request: http://localhost:8082/api/inventory?skuCode=iphone-13&skuCode=iphone-13-pro
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        log.info("Received inventory check request for skuCode: {}", skuCode);
        return inventoryService.isInStock(skuCode);

//        // Distributed Tracing Micrometer Tracing and Zipkin Brave - Approach #1
//        Span childSpan = tracer.spanBuilder().setParent(
//                tracer.traceContextBuilder()
//                        .traceId(request.getHeader("X-B3-TraceId"))
//                        .spanId(request.getHeader("X-B3-SpanId"))
//                        .build()).name("inventory-controller-isInStock").start();
//
//        try (Tracer.SpanInScope spanInScope = tracer.withSpan(childSpan)) {
//            return inventoryService.isInStock(skuCode);
//        } finally {
//            childSpan.end(); // End the child span
//        }
    }


    @PostMapping("/decrement")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void decrementQuantity(@RequestBody List<InventoryRequest> inventoryRequest) {
        log.info("Received inventory decrement request for skuCode: {}", inventoryRequest.stream().map(InventoryRequest::getSkuCode).toArray());
        inventoryService.decrementQuantity(inventoryRequest);

//        // Distributed Tracing Micrometer Tracing and Zipkin Brave - Approach #1
//        Span childSpan = tracer.spanBuilder().setParent(
//                tracer.traceContextBuilder()
//                        .traceId(request.getHeader("X-B3-TraceId"))
//                        .spanId(request.getHeader("X-B3-SpanId"))
//                        .build()).name("inventory-controller-decrementQuantity").start();
//
//        try (Tracer.SpanInScope spanInScope = tracer.withSpan(childSpan)) {
//            inventoryService.decrementQuantity(inventoryRequest);
//        } finally {
//            childSpan.end(); // End the child span
//        }
    }
}
