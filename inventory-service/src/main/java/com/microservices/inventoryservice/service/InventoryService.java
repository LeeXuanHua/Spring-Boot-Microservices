package com.microservices.inventoryservice.service;

import com.microservices.inventoryservice.dto.InventoryRequest;
import com.microservices.inventoryservice.dto.InventoryResponse;
import com.microservices.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true) // Indicate that this method is read-only (cannot modify database)
    @SneakyThrows // Suppresses the need to catch or throw the exception (do not use this in production, as it hides the exception)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        // Simulate a slow response (to test and trigger timeout from the circuit breaker)
//        log.info("Wait started");
//        Thread.sleep(10_000);
//        log.info("Wait ended"); // Logged after 10s (by then, the circuit breaker will have thrown a timeout exception in order-service)

        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder()
                            .skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity() > 0)
                            .build()
                ).toList();
    }

    @Transactional(readOnly = false) // Indicate that this method is not read-only (can modify database)
    public void decrementQuantity(List<InventoryRequest> inventoryRequests) {
        inventoryRequests.forEach(inventoryRepository::decrementQuantity);
    }
}
