package de.sample.aiarchitecture.portadapter.incoming.api.product;

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
