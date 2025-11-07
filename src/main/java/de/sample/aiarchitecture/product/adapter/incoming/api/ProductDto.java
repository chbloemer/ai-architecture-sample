package de.sample.aiarchitecture.product.adapter.incoming.api;

import java.math.BigDecimal;

public record ProductDto(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String currency,
    String category,
    int stock) {}
