package com.microservices.orderservice;

import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderLineItems;
import com.microservices.orderservice.repository.OrderRepository;
import com.microservices.orderservice.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.random;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)	// https://www.arhohuttunen.com/spring-boot-unit-testing/
class OrderServiceUnitTest {
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;
    private long id;
    private String skuCode;
    private BigDecimal price;
    private int quantity;

    @BeforeEach
    void setUp() {
        // Generate random values for the OrderLineItemsDto and productResponse fields
        id = new Random().nextLong();
        skuCode = UUID.randomUUID().toString();
        price = BigDecimal.valueOf(new Random().nextDouble());
        quantity = new Random().nextInt();
    }

    @DisplayName("Multiple products saved to database correctly, allowing for duplicate products")
    @ParameterizedTest(name = "{index} => id={0}, name={1}, description={2}, price={3}")
    @CsvSource(
            {
                    "0, Product 1, 10.99, 1, 0, Product 2, 20.99, 2",
                    "0, Product 2, 20.99, 1, 0, Product 2, 20.99, 4",
            }
    )
    void placeOrderTest(
            long id1, String skuCode1, BigDecimal price1, int quantity1,
            long id2, String skuCode2, BigDecimal price2, int quantity2
    ) {
        OrderRequest orderRequest = mock(OrderRequest.class);
        when(orderRequest.getOrderLineItemsDtoList()).thenReturn(List.of(
                OrderLineItemsDto.builder()
                        .id(id1)
                        .skuCode(skuCode1)
                        .price(price1)
                        .quantity(quantity1)
                        .build(),
                OrderLineItemsDto.builder()
                        .id(id2)
                        .skuCode(skuCode2)
                        .price(price2)
                        .quantity(quantity2)
                        .build()
        ));

        // Mocking the repository
        when(orderRepository.findAll()).thenReturn(List.of(
                Order.builder()
                        .orderNumber(UUID.randomUUID().toString())
                        .orderLineItemsList(List.of(
                                OrderLineItems.builder()
                                        .id(id1)
                                        .skuCode(skuCode1)
                                        .price(price1)
                                        .quantity(quantity1)
                                        .build(),
                                OrderLineItems.builder()
                                        .id(id2)
                                        .skuCode(skuCode2)
                                        .price(price2)
                                        .quantity(quantity2)
                                        .build()
                        )).build()
                ));
        orderService.placeOrder(orderRequest);

        // Verify that the repository's save method was called exactly once with the correct parameters
        verify(orderRepository, times(1)).save(Mockito.any(Order.class));

        Assertions.assertEquals(1, orderRepository.findAll().size());
        Assertions.assertNotNull(orderRepository.findAll().get(0).getOrderNumber());
        Assertions.assertEquals(
                orderRequest.getOrderLineItemsDtoList().stream().map(orderService::mapToOrderLineItem).toList(),
                orderRepository.findAll().get(0).getOrderLineItemsList());
    }

    @Test
    @DisplayName("OrderLineItemsDto mapped to OrderLineItems correctly")
    void mapToOrderLineItemTest() {
        // Mock the OrderLineItemsDto and OrderLineItems behaviour
        OrderLineItemsDto orderLineItemsDto = mock(OrderLineItemsDto.class);
        when(orderLineItemsDto.getSkuCode()).thenReturn(skuCode);
        when(orderLineItemsDto.getPrice()).thenReturn(price);
        when(orderLineItemsDto.getQuantity()).thenReturn(quantity);

        OrderLineItems orderLineItems = orderService.mapToOrderLineItem(orderLineItemsDto);

        // Verify that the mapping is correct and covers all possible fields for OrderLineItems
        Assertions.assertEquals(orderLineItems.getSkuCode(), orderLineItemsDto.getSkuCode());
        Assertions.assertEquals(orderLineItems.getPrice(), orderLineItemsDto.getPrice());
        Assertions.assertEquals(orderLineItems.getQuantity(), orderLineItemsDto.getQuantity());
    }
}
