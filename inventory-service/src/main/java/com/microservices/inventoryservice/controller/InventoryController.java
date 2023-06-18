package com.microservices.inventoryservice.controller;

import com.microservices.inventoryservice.dto.InventoryRequest;
import com.microservices.inventoryservice.dto.InventoryResponse;
import com.microservices.inventoryservice.model.Inventory;
import com.microservices.inventoryservice.service.InventoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // If use PathVariable, sample request: http://localhost:8082/api/inventory/iphone-13,iphone-13-pro
    // If use RequestParam, sample request: http://localhost:8082/api/inventory?skuCode=iphone-13&skuCode=iphone-13-pro
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping("/decrement")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void decrementQuantity(@RequestBody List<InventoryRequest> inventoryRequest) {
        inventoryService.decrementQuantity(inventoryRequest);
    }
}
