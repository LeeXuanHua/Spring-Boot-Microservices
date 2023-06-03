package com.microservices.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)    // Automatically included by @SpringBootTest
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = OrderServiceApplication.class
)
@Testcontainers
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {
    @Container
    private static final MySQLContainer sqlContainer = new MySQLContainer<>("mysql:8.0");
    @Autowired
    private MockMvc mockMvc;	// MockMvc is used to test the controller
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;

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
        // Start each test with an empty database
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Order is placed successfully")
    public void placeOrderTest() throws Exception {
        OrderRequest orderRequest = mock(OrderRequest.class);
        String orderRequestString = objectMapper.writeValueAsString(orderRequest);	// Convert the object to JSON string
        String returnValue = mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestString))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertEquals(1, orderRepository.findAll().size());
        Assertions.assertEquals("Order placed successfully", returnValue);
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