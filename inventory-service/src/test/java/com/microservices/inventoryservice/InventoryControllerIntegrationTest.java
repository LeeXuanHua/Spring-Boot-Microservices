package com.microservices.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventoryservice.model.Inventory;
import com.microservices.inventoryservice.repository.InventoryRepository;
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

import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)    // Automatically included by @SpringBootTest
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = InventoryServiceApplication.class
)
@Testcontainers
@AutoConfigureMockMvc
public class InventoryControllerIntegrationTest {
    @Container
    private static final MySQLContainer sqlContainer = new MySQLContainer<>("mysql:8.0");
    @Autowired
    private MockMvc mockMvc;	// MockMvc is used to test the controller
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InventoryRepository inventoryRepository;
    private final String skuCode = "iphone_13";

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
        inventoryRepository.deleteAll();

        // Populate the database with some data
        Inventory inventory = new Inventory();
        inventory.setSkuCode(skuCode);
        inventory.setQuantity(50);
        inventoryRepository.save(inventory);
    }

    @Test
    @DisplayName("Returns only those inventory in stock")
    public void isInStockTest() throws Exception {
        String returnValue = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("skuCode", skuCode, "iphone_13_pro"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertEquals("[{\"skuCode\":\"iphone_13\",\"inStock\":true}]", returnValue);
    }

    @Test
    @DisplayName("Product quantity is decremented successfully")
    public void decrementQuantityTest() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/inventory/decrement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(new HashMap<>() {{
                            put("skuCode", skuCode);
                            put("quantity", 10);
                        }}))))
                .andExpect(status().isAccepted());

        Assertions.assertEquals(40, inventoryRepository.findBySkuCode(skuCode).getQuantity());
    }

    @Test
    @DisplayName("Validation of List<InventoryRequest> fails")
    public void decrementQuantityInvalidTest() throws Exception {
        String decrementQuantityString = objectMapper.writeValueAsString(null);	// Convert the object to JSON string
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/inventory/decrement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decrementQuantityString))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(50, inventoryRepository.findBySkuCode(skuCode).getQuantity());
    }
}