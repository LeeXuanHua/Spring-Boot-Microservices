package com.microservices.orderservice;

import com.microservices.orderservice.dto.InventoryResponse;
import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderLineItems;
import com.microservices.orderservice.repository.OrderRepository;
import com.microservices.orderservice.service.OrderService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)	// https://www.arhohuttunen.com/spring-boot-unit-testing/
@MockitoSettings(strictness = Strictness.LENIENT)   // https://stackoverflow.com/questions/42947613/how-to-resolve-unneccessary-stubbing-exception
class OrderServiceUnitTest {
    @Mock
    private Tracer tracer;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;
    @Mock
    private WebClient.Builder webClientBuilder;
    private long id;
    private String skuCode;
    private BigDecimal price;
    private int quantity;
    private Span span;

    @BeforeEach
    void setUp() {
        // Generate random values for the OrderLineItemsDto and productResponse fields
        id = new Random().nextLong();
        skuCode = UUID.randomUUID().toString();
        price = BigDecimal.valueOf(new Random().nextDouble());
        quantity = new Random().nextInt();

        // Mock all instances of the Tracer class
        span = Mockito.mock(Span.class);
        TraceContext traceContext = Mockito.mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(UUID.randomUUID().toString());
        when(traceContext.spanId()).thenReturn(UUID.randomUUID().toString());

        when(span.event(anyString())).thenReturn(span);
    }

    @DisplayName("Order placement for multiple products, including duplicate products")
    @ParameterizedTest(name = "{index} => id={0}, name={1}, description={2}, price={3}")
    @CsvSource(
            {
                    "0, Product 1, 10.99, 1, true, 0, Product 2, 20.99, 2, true",
                    "0, Product 2, 20.99, 1, true, 0, Product 2, 20.99, 4, true",
                    "0, Product 1, 10.99, 1, true, 0, Product 2, 20.99, 2, false",
                    "0, Product 1, 10.99, 1, false, 0, Product 2, 20.99, 2, true",
                    "0, Product 1, 10.99, 1, false, 0, Product 2, 20.99, 2, false",
            }
    )
    void placeOrderTest(
            long id1, String skuCode1, BigDecimal price1, int quantity1, boolean inStock1,
            long id2, String skuCode2, BigDecimal price2, int quantity2, boolean inStock2
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

        // Mocking the repository (returns a list of OrderLine Items if all products are in stock. Otherwise, returns an empty list)
        if (inStock1 && inStock2) {
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
        } else {
            when(orderRepository.findAll()).thenReturn(new ArrayList<>());
        }

        // Mocking the WebClient methods and responses
        InventoryResponse[] inventoryResponse = new InventoryResponse[] {
                new InventoryResponse(skuCode1, inStock1),
                new InventoryResponse(skuCode2, inStock2)
        };
        List<String> skuCodes = List.of(skuCode1, skuCode2);

        // Mock WebClient's initial call to the inventory service to obtain the inventory status of the products
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
//        UriBuilder uriBuilder = mock(UriBuilder.class);
//        URI uri = mock(URI.class);
//        Function<UriBuilder, URI> function = a -> a.queryParam("skuCode", skuCodes).build();

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(uriBuilder.queryParam(any(String.class), any(List.class))).thenReturn(uriBuilder);     // Unnecessary stubbing - Resolved by defining as lenient
//        when(uriBuilder.build()).thenReturn(uri);                                                   // Unnecessary stubbing - Resolved by defining as lenient
        when(requestHeadersUriSpec.uri(any(String.class), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(String.class), any())).thenReturn(requestHeadersSpec);     // Added for traceparent headers
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponse));

        // Mock WebClient's subsequent call to the inventory service to decrement the quantity of the products
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(any(String.class), any())).thenReturn(requestBodySpec);     // Added for traceparent headers
        when(requestBodySpec.bodyValue(any(List.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // If both products are in stock, the order should be placed
        if (inStock1 && inStock2) {
            orderService.placeOrder(orderRequest);

            // Verify that the repository's save method was called exactly once with the correct parameters
            verify(orderRepository, times(1)).save(Mockito.any(Order.class));

            Assertions.assertEquals(1, orderRepository.findAll().size());
            Assertions.assertNotNull(orderRepository.findAll().get(0).getOrderNumber());
            Assertions.assertEquals(
                    orderRequest.getOrderLineItemsDtoList().stream().map(orderService::mapToOrderLineItem).toList(),
                    orderRepository.findAll().get(0).getOrderLineItemsList());

            // Verify that the webClientBuilder.build()'s get and post methods were called exactly once
            verify(webClientBuilder.build(), times(1)).get();
            verify(webClientBuilder.build(), times(1)).post();

            // Verify that the span events were created
            verify(span, times(2)).event(anyString());

        } else {
            // If one of the products is out of stock, the order should not be placed
            // Verify that error is thrown
            Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(orderRequest));
            Assertions.assertEquals(0, orderRepository.findAll().size());

            // Verify that the repository's save method was never called
            verify(orderRepository, never()).save(Mockito.any(Order.class));

            // Verify that only 1 span event was created
            verify(span, times(1)).event(anyString());
        }
    }

    @Test
    @DisplayName("Empty order placement")
    void emptyPlaceOrderTest() {
        OrderRequest orderRequest = mock(OrderRequest.class);
        when(orderRequest.getOrderLineItemsDtoList()).thenReturn(List.of(
                OrderLineItemsDto.builder().build(),
                OrderLineItemsDto.builder().build()
        ));

        // Mocking the repository (returns an empty list)
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        // Mocking the WebClient methods and responses
        InventoryResponse[] inventoryResponse = new InventoryResponse[] {
                new InventoryResponse(skuCode, true),
                new InventoryResponse(skuCode, true)
        };

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(String.class), any())).thenReturn(requestHeadersSpec);     // Added for traceparent headers
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(mono);
        when(mono.block()).thenReturn(null);

        // Verify that error is thrown
        Assertions.assertThrows(AssertionError.class, () -> orderService.placeOrder(orderRequest));
        Assertions.assertEquals(0, orderRepository.findAll().size());

        // Verify that the repository's save method was never called
        verify(orderRepository, never()).save(Mockito.any(Order.class));

        // Verify that only 1 span event was created
        verify(span, times(1)).event(anyString());
    }

    @Test
    @DisplayName("Nonexistent order placement")
    void nonexistentPlaceOrderTest() {
        OrderRequest orderRequest = mock(OrderRequest.class);
        when(orderRequest.getOrderLineItemsDtoList()).thenReturn(List.of(
                OrderLineItemsDto.builder().build(),
                OrderLineItemsDto.builder().build()
        ));

        // Mocking the repository (returns an empty list)
        when(orderRepository.findAll()).thenReturn(new ArrayList<>());

        // Mocking the WebClient methods and responses
        InventoryResponse[] inventoryResponse = new InventoryResponse[] {
                new InventoryResponse(skuCode, true)
        };

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono mono = mock(Mono.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(any(String.class), any())).thenReturn(requestHeadersSpec);     // Added for traceparent headers
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(mono);
        when(mono.block()).thenReturn(inventoryResponse);

        // Verify that error is thrown
        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(orderRequest));
        Assertions.assertEquals(0, orderRepository.findAll().size());

        // Verify that the repository's save method was never called
        verify(orderRepository, never()).save(Mockito.any(Order.class));

        // Verify that only 1 span event was created
        verify(span, times(1)).event(anyString());
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
