package com.microservices.inventoryservice.repository;

import com.microservices.inventoryservice.dto.InventoryRequest;
import com.microservices.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findBySkuCodeIn(List<String> skuCode);

    @Transactional
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :#{#inventoryRequest.quantity} WHERE i.skuCode = :#{#inventoryRequest.skuCode}")
    void decrementQuantity(InventoryRequest inventoryRequest);

    Inventory findBySkuCode(String skuCode);
}
