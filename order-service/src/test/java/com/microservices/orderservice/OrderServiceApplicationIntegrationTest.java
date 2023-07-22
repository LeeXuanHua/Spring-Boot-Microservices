package com.microservices.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.orderservice.dto.InventoryResponse;
import com.microservices.orderservice.dto.OrderLineItemsDto;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)    // Automatically included by @SpringBootTest
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = OrderServiceApplication.class
)
@Testcontainers
@AutoConfigureMockMvc
public class OrderServiceApplicationIntegrationTest {
    @Autowired
    private Tracer tracer;
    @Container
    private static final MySQLContainer sqlContainer = new MySQLContainer<>("mysql:8.0");
    @Autowired
    private MockMvc mockMvc;	// MockMvc is used to test the controller
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @MockBean
    private WebClient.Builder webClientBuilder;
    private String skuCode;
    private BigDecimal price;
    private int quantity;
    private boolean isInStock;
    private Span span;

    static {	// Static block is used to start the container
        sqlContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", sqlContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", sqlContainer::getPassword);
    }

    @BeforeEach
    public void setup() {
        skuCode = UUID.randomUUID().toString();
        price = BigDecimal.valueOf(new Random().nextDouble());
        quantity = new Random().nextInt();
        isInStock = true;

        // Start each test with an empty database
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Order is placed successfully")
    public void placeOrderTest() throws Exception {
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(Collections.singletonList(OrderLineItemsDto.builder()
                                .skuCode(skuCode)
                                .price(price)
                                .quantity(quantity)
                                .build()))
                .build();
        String orderRequestString = objectMapper.writeValueAsString(orderRequest);	// Convert the object to JSON string

        // Mocking the WebClient methods and responses
        InventoryResponse[] inventoryResponse = new InventoryResponse[] {
                InventoryResponse.builder()
                        .skuCode(skuCode)
                        .isInStock(isInStock)
                        .build()
        };

        // Mock WebClient's initial call to the inventory service to obtain the inventory status of the products
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
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

        // Fails - Response is not returned due to the asynchronous nature of the controller (CompletableFuture)
//        String returnValue = mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(orderRequestString))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();

        // Reference: https://howtodoinjava.com/spring-boot2/testing/test-async-controller-mockmvc/
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequestString))
                .andExpect(request().asyncStarted())
                .andDo(MockMvcResultHandlers.log())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order placed successfully!"));

        Assertions.assertEquals(1, orderRepository.findAll().size());
    }

    @Test
    @DisplayName("Validation of OrderRequest fails")
    public void placeOrderInvalidTest() throws Exception {
        String orderRequestString = objectMapper.writeValueAsString(null);	// Convert the object to JSON string
        String returnValue = mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestString))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertEquals(0, orderRepository.findAll().size());
        Assertions.assertEquals("", returnValue);
    }
}