package com.microservices.inventoryservice;

import com.microservices.inventoryservice.model.Inventory;
import com.microservices.inventoryservice.repository.InventoryRepository;
import com.microservices.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)	// https://www.arhohuttunen.com/spring-boot-unit-testing/
class InventoryServiceUnitTest {
    @Mock
    private InventoryRepository inventoryRepository;
    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Check if inventory is in stock")
    void isInStockTest() {
        String skuCode = "iphone_13";

        Inventory inventory = Inventory.builder()
                .skuCode(skuCode)
                .quantity(50)
                .build();

        // Mocking the repository
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.ofNullable(inventory));
        boolean isInStock = inventoryService.isInStock(skuCode);

        Assertions.assertTrue(isInStock);
        verify(inventoryRepository, times(1)).findBySkuCode(skuCode);
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Check if inventory is in not stock")
    void isNotInStockTest() {
        String skuCode = "iphone_13";

        // Mocking the repository
        when(inventoryRepository.findBySkuCode(any(String.class))).thenReturn(Optional.empty());
        boolean isInStock = inventoryService.isInStock(skuCode.substring(0, skuCode.length() - 1));

        Assertions.assertFalse(isInStock);
        verify(inventoryRepository, times(1)).findBySkuCode(skuCode.substring(0, skuCode.length() - 1));
        verifyNoMoreInteractions(inventoryRepository);
    }
}
