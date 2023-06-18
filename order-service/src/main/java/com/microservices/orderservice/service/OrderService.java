package com.microservices.orderservice.service;

import com.microservices.orderservice.dto.InventoryRequest;
import com.microservices.orderservice.dto.InventoryResponse;
import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderLineItems;
import com.microservices.orderservice.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    /**
     * Places an order via OrderRequest, which may consist of 1 or more Order. UUIDs are created for each Order
     */
    public void placeOrder(OrderRequest orderRequest) {
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

        // Call inventory-service and place order if product is in stock
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
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
            webClient.post()
                    .uri("http://localhost:8082/api/inventory/decrement")
                    .bodyValue(inventoryRequests)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
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
