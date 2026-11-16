package ru.demo.mongodb.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void createTrimsNameAndSavesProduct() {
        ProductRequest request = new ProductRequest("  Tea  ", "  Green tea  ", new BigDecimal("3.50"));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            return new Product("product-1", product.name(), product.description(), product.price(), product.createdAt());
        });

        Product result = service.create(request);

        assertEquals("Tea", result.name());
        assertEquals("Green tea", result.description());
        assertEquals("3.50", result.price().toString());
        assertNotNull(result.createdAt());
        verify(repository).save(any(Product.class));
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void updateKeepsOriginalCreationTime() {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        Product existing = new Product("p-1", "Old", "Description", BigDecimal.ONE, createdAt);
        when(repository.findById("p-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = service.update("p-1", new ProductRequest("New", "Updated", BigDecimal.TEN));

        assertEquals("New", updated.name());
        assertEquals(createdAt, updated.createdAt());
    }
}
