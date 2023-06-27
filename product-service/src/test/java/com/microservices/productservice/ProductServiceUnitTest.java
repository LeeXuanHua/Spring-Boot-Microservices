package com.microservices.productservice;

import com.microservices.productservice.dto.ProductRequest;
import com.microservices.productservice.dto.ProductResponse;
import com.microservices.productservice.model.Product;
import com.microservices.productservice.repository.ProductRepository;
import com.microservices.productservice.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)	// https://www.arhohuttunen.com/spring-boot-unit-testing/
class ProductServiceUnitTest {
	@Mock
	private ProductRepository productRepository;
	@InjectMocks
	private ProductService productService;
	private String name;
	private String description;
	private BigDecimal price;

	@BeforeEach
	void setUp() {
		// Generate random values for the productRequest and productResponse fields
		name = UUID.randomUUID().toString();
		description = UUID.randomUUID().toString();
		price = BigDecimal.valueOf(new Random().nextDouble());
	}

	@Test
	@DisplayName("Product saved to database correctly")
	void createProductTest() {
		ProductRequest productRequest = mock(ProductRequest.class);
		when(productRequest.getName()).thenReturn(name);
		when(productRequest.getDescription()).thenReturn(description);
		when(productRequest.getPrice()).thenReturn(price);

		// Mocking the repository
		when(productRepository.findAll()).thenReturn(List.of(
				Product.builder()
						.name(name)
						.description(description)
						.price(price)
						.build()
		));
		productService.createProduct(productRequest);

		// Verify that the repository's save method was called exactly once with the correct parameters
		verify(productRepository, times(1)).save(Product.builder()
				.name(name)
				.description(description)
				.price(price)
				.build());

		Assertions.assertEquals(1, productRepository.findAll().size());
		Assertions.assertEquals(name, productRepository.findAll().get(0).getName());
		Assertions.assertEquals(description, productRepository.findAll().get(0).getDescription());
		Assertions.assertEquals(price, productRepository.findAll().get(0).getPrice());
	}

	@DisplayName("Multiple products saved to database correctly, allowing for duplicate products")
	@ParameterizedTest(name = "{index} => id={0}, name={1}, description={2}, price={3}")
	@CsvSource(
			{
					"1, Product 1, Product 1 description, 10.99, 2, Product 2, Product 2 description, 20.99",
					"2, Product 2, Product 2 description, 20.99, 2, Product 2, Product 2 description, 20.99",
			}
	)
	void getAllProductsTest(
			String id1, String name1, String description1, BigDecimal price1,
			String id2, String name2, String description2, BigDecimal price2) {
		// Mocking the repository
		when(productRepository.findAll()).thenReturn(List.of(
				Product.builder()
						.id(id1)
						.name(name1)
						.description(description1)
						.price(price1)
						.build(),
				Product.builder()
						.id(id2)
						.name(name2)
						.description(description2)
						.price(price2)
						.build()
		));

		List<ProductResponse> productResponses = productService.getAllProducts();

		// Verify that repository's findAll method is called exactly once
		verify(productRepository, times(1)).findAll();

		Assertions.assertEquals(productResponses.size(), 2);
		Assertions.assertEquals(productResponses.get(0).getId(), id1);
		Assertions.assertEquals(productResponses.get(0).getName(), name1);
		Assertions.assertEquals(productResponses.get(0).getDescription(), description1);
		Assertions.assertEquals(productResponses.get(0).getPrice(), price1);
		Assertions.assertEquals(productResponses.get(1).getId(), id2);
		Assertions.assertEquals(productResponses.get(1).getName(), name2);
		Assertions.assertEquals(productResponses.get(1).getDescription(), description2);
		Assertions.assertEquals(productResponses.get(1).getPrice(), price2);
	}

	@Test
	@DisplayName("Product mapped to ProductResponse correctly")
	void mapToProductResponseTest() {
		// Mock the product and productResponse behaviour
		Product product = mock(Product.class);
		when(product.getName()).thenReturn(name);
		when(product.getDescription()).thenReturn(description);
		when(product.getPrice()).thenReturn(price);

		ProductResponse productResponse = productService.mapToProductResponse(product);

		// Verify that the mapping is correct and covers all possible fields for productResponse
		Assertions.assertEquals(productResponse.getId(), product.getId());
		Assertions.assertEquals(productResponse.getName(), product.getName());
		Assertions.assertEquals(productResponse.getDescription(), product.getDescription());
		Assertions.assertEquals(productResponse.getPrice(), product.getPrice());
	}
}
