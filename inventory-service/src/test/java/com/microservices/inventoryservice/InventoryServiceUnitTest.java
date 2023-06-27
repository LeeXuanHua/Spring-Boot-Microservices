package com.microservices.inventoryservice;

import com.microservices.inventoryservice.dto.InventoryRequest;
import com.microservices.inventoryservice.dto.InventoryResponse;
import com.microservices.inventoryservice.model.Inventory;
import com.microservices.inventoryservice.repository.InventoryRepository;
import com.microservices.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        List<String> skuCodeList = Arrays.asList("iphone_13", "iphone_13_pro");

        Inventory inventory1 = Inventory.builder()
                .skuCode(skuCodeList.get(0))
                .quantity(30)
                .build();
        Inventory inventory2 = Inventory.builder()
                .skuCode(skuCodeList.get(1))
                .quantity(0)
                .build();

        List<Inventory> inventoryList = Arrays.asList(inventory1, inventory2);

        // Mocking the repository
        when(inventoryRepository.findBySkuCodeIn(skuCodeList)).thenReturn(inventoryList);

        List<InventoryResponse> inventoryResponseList = inventoryService.isInStock(skuCodeList);

        Assertions.assertEquals(2, inventoryResponseList.size());
        Assertions.assertEquals(inventoryList.get(0).getSkuCode(), inventoryResponseList.get(0).getSkuCode());
        Assertions.assertEquals(inventoryList.get(1).getSkuCode(), inventoryResponseList.get(1).getSkuCode());
        Assertions.assertEquals(inventoryList.get(0).getQuantity() > 0 ,inventoryResponseList.get(0).isInStock());
        Assertions.assertEquals(inventoryList.get(1).getQuantity() > 0 ,inventoryResponseList.get(1).isInStock());
        verify(inventoryRepository, times(1)).findBySkuCodeIn(skuCodeList);
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Check if invalid inventory is in stock")
    void invalidIsInStockTest() {
        List<String> skuCodeList = Collections.singletonList("invalid_sku_code");

        // Mock the repository
        when(inventoryRepository.findBySkuCodeIn(skuCodeList)).thenReturn(Collections.emptyList());
        List<InventoryResponse> inventoryResponseList = inventoryService.isInStock(skuCodeList);

        Assertions.assertEquals(0, inventoryResponseList.size());
        Assertions.assertEquals(Collections.emptyList(), inventoryResponseList);
        verify(inventoryRepository, times(1)).findBySkuCodeIn(skuCodeList);
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Decrement product quantity successfully")
    void decrementQuantityTest() {
        List<InventoryRequest> inventoryRequests = List.of(
                InventoryRequest.builder()
                        .skuCode("iphone_13")
                        .quantity(2)
                        .build(),
                InventoryRequest.builder()
                        .skuCode("iphone_13_pro")
                        .quantity(3)
                        .build()
        );

        inventoryService.decrementQuantity(inventoryRequests);

        verify(inventoryRepository, times(inventoryRequests.size())).decrementQuantity(any(InventoryRequest.class));
        verifyNoMoreInteractions(inventoryRepository);
    }
}
