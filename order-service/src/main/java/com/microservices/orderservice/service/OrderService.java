package com.microservices.orderservice.service;

import com.microservices.orderservice.dto.InventoryRequest;
import com.microservices.orderservice.dto.InventoryResponse;
import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderLineItems;
import com.microservices.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;

    /**
     * Places an order via OrderRequest, which may consist of 1 or more Order. UUIDs are created for each Order
     */
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

        log.info("Calling inventory service");

        // Specify a span named "InventoryServiceLookup" to wrap around the call to inventory-service
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

        // Wrap the call to inventory-service in a try-finally block to ensure the span is closed
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {
            // Call inventory-service and place order if product is in stock
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)  // Reads the response body and converts it to a Mono
                    .block();                   // Blocks until the response is received

            assert inventoryResponseArray != null;
            if (inventoryResponseArray.length != skuCodes.size()) {
                throw new IllegalArgumentException("Product does not exist!");
            }

            boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

            if (allProductsInStock) {
                orderRepository.save(order);

                List<InventoryRequest> inventoryRequests = order.getOrderLineItemsList().stream()
                        .map(orderLineItem ->
                                InventoryRequest.builder()
                                        .skuCode(orderLineItem.getSkuCode())
                                        .quantity(orderLineItem.getQuantity())
                                        .build()
                        ).toList();
                webClientBuilder.build().post()
                        .uri("http://inventory-service/api/inventory/decrement")
                        .bodyValue(inventoryRequests)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();

                return "Order placed successfully!";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }
        } finally {
            inventoryServiceLookup.end();
        }
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
