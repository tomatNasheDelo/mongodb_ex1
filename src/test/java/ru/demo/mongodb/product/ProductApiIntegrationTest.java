package ru.demo.mongodb.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductApiIntegrationTest {
    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0.16"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createThenReadProductThroughHttpApi() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Notebook","description":"Plain paper","price":4.25}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andReturn().getResponse().getContentAsString();

        Product created = objectMapper.readValue(response, Product.class);
        mockMvc.perform(get("/api/products/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Plain paper"));
    }

    @Test
    void updateAndDeleteProduct() throws Exception {
        Product saved = repository.save(new Product(null, "Old name", "Old", new java.math.BigDecimal("1.00"),
                java.time.Instant.now()));

        mockMvc.perform(put("/api/products/{id}", saved.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name","description":"Updated","price":2.50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));

        mockMvc.perform(delete("/api/products/{id}", saved.id()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}", saved.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidProductAndReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","description":"","price":-1}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/products/{id}", "does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchesProductsByNameIgnoringCase() throws Exception {
        repository.save(new Product(null, "MongoDB Guide", "Book", new java.math.BigDecimal("9.99"),
                java.time.Instant.now()));
        repository.save(new Product(null, "Spring Guide", "Book", new java.math.BigDecimal("8.99"),
                java.time.Instant.now()));

        mockMvc.perform(get("/api/products").param("name", "mongodb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("MongoDB Guide"));
    }
}
