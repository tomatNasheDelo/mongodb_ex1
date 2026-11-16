# Spring Boot + MongoDB demo

Небольшой REST API каталога товаров для знакомства с MongoDB, Spring Data MongoDB, unit-тестами и интеграционными тестами с Testcontainers.

## Быстрый старт

Требуются JDK 17+, Gradle 8+ и Docker с Compose. Инструкция запуска, API и тестирования — в [`start.txt`](start.txt).

Ключевые части:
- `product/Product` — MongoDB document;
- `ProductRepository` — Spring Data repository;
- `ProductService` — бизнес-логика;
- `ProductController` — CRUD REST API;
- `ProductServiceTest` — unit tests с Mockito без запуска Spring/БД;
- `ProductApiIntegrationTest` — интеграционные HTTP + MongoDB tests с Testcontainers.
