package com.microservices.productservice.repository;

import com.microservices.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository layer is an abstraction layer that connects the application to the database.
 * It receives data from the data source and converts it into a domain object for the service layer, and vice versa.
 * It is responsible for performing CRUD operations on the database.
 */
public interface ProductRepository extends MongoRepository<Product, String> {

}
