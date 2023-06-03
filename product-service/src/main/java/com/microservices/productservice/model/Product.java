package com.microservices.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(value = "product")    // Define as a MongoDB document
@Data   // Bundles @ToString, @EqualsAndHashCode, @Getter / @Setter and @RequiredArgsConstructor together
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id // Specify as the unique identifier
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
