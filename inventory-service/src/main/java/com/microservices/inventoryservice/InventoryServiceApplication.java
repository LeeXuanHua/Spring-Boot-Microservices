package com.microservices.inventoryservice;

import com.microservices.inventoryservice.model.Inventory;
import com.microservices.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner demo(InventoryRepository inventoryRepository) {
//		return (args) -> {
//			Inventory inventory1 = new Inventory();
//			inventory1.setSkuCode("iphone_13");
//			inventory1.setQuantity(50);
//
//			Inventory inventory2 = new Inventory();
//			inventory2.setSkuCode("iphone_13_pro");
//			inventory2.setQuantity(30);
//
//			inventoryRepository.save(inventory1);
//			inventoryRepository.save(inventory2);
//		};
//	}
}
