package ru.demo.mongodb.product;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(ProductRequest request) {
        Product product = new Product(null, request.name().trim(), normalize(request.description()),
                request.price(), Instant.now());
        return repository.save(product);
    }

    public List<Product> findAll(String name) {
        if (name == null || name.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNameContainingIgnoreCase(name.trim());
    }

    public Product findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product update(String id, ProductRequest request) {
        Product existing = findById(id);
        Product updated = new Product(existing.id(), request.name().trim(), normalize(request.description()),
                request.price(), existing.createdAt());
        return repository.save(updated);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
