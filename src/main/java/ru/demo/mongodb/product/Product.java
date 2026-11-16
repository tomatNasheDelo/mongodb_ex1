package ru.demo.mongodb.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("products")
public record Product(
        @Id String id,
        String name,
        String description,
        BigDecimal price,
        Instant createdAt
) {
}
