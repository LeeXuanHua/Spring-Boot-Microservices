package com.microservices.orderservice.service;

import com.microservices.orderservice.dto.InventoryRequest;
import com.microservices.orderservice.dto.InventoryResponse;
import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.event.OrderPlacedEvent;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderLineItems;
import com.microservices.orderservice.repository.OrderRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;   // Proof that both WebClient.Builder and WebClient beans are instrumented with Micrometer correctly
    private final WebClient webClient;                  // Proof that both WebClient.Builder and WebClient beans are instrumented with Micrometer correctly
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    /**
     * Places an order via OrderRequest, which may consist of 1 or more Order. UUIDs are created for each Order
     */
//    @Observed(name = "orderService-placeOrder",
//            contextualName = "orderService-placeOrder",
//            lowCardinalityKeyValues = {"call", "inventory-service-from-order-service"})
    public String placeOrder(OrderRequest orderRequest) {
        // Generate new Order, set orderNumber, map each OrderLineItemsDto to OrderLineItems, and extract skuCodes
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToOrderLineItem)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Create a span, name it and register it
        Observation inventoryServiceObservation = Observation.createNotStarted(
                "inventory-service-lookup",
                this.observationRegistry
        );
        // Add a tag of key="call", value="inventory-service" for easy lookup
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service-from-order-service");

        return inventoryServiceObservation.observe(() -> {
            log.info("Get request to inventory service");
            // Call inventory-service and place order if product is in stock
            InventoryResponse[] inventoryResponseArray = webClient.get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
//                    .header("traceparent", "00-"+tracer.currentSpan().context().traceId()+"-"+tracer.currentSpan().context().spanId()+"-01")
//                    .header("X-B3-TraceId", tracer.currentSpan().context().traceId())
//                    .header("X-B3-SpanId", tracer.currentSpan().context().spanId())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)  // Reads the response body and converts it to a Mono
                    .block();                   // Blocks until the response is received
            Objects.requireNonNull(tracer.currentSpan()).event("Retrieved inventory");

            assert inventoryResponseArray != null;
            if (inventoryResponseArray.length != skuCodes.size()) {
                throw new IllegalArgumentException("Product does not exist!");
            }

            boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

            if (allProductsInStock) {
                // Code to demonstrate async timing (and @TimeLimiter's resilience4j.timelimiter.instances.inventory.timeout-duration=3s)
                // At 2s, the testing will still work; At 3s, the testing will fail
//                try {
//                    log.info("Going to sleep for 2 seconds");
//                    sleep(2000);
//                } catch (InterruptedException e) {
//                    log.info("Error detected while sleeping");
//                    throw new RuntimeException(e);
//                }
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));

                List<InventoryRequest> inventoryRequests = order.getOrderLineItemsList().stream()
                        .map(orderLineItem ->
                                InventoryRequest.builder()
                                        .skuCode(orderLineItem.getSkuCode())
                                        .quantity(orderLineItem.getQuantity())
                                        .build()
                        ).toList();

                log.info("Post request to inventory service");
                webClientBuilder.build().post()
                        .uri("http://inventory-service/api/inventory/decrement")
//                        .header("traceparent", "00-"+tracer.currentSpan().context().traceId()+"-"+tracer.currentSpan().context().spanId()+"-01")
//                        .header("X-B3-TraceId", tracer.currentSpan().context().traceId())
//                        .header("X-B3-SpanId", tracer.currentSpan().context().spanId())
                        .bodyValue(inventoryRequests)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
                Objects.requireNonNull(tracer.currentSpan()).event("Decremented inventory");

                return "Order placed successfully!";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        });
    }

    /**
     * Lambda function to map 1 OrderLineItemsDto to 1 OrderLineItems.
     */
    public OrderLineItems mapToOrderLineItem(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}
