package de.sample.aiarchitecture.product.adapter.incoming.api;

import java.math.BigDecimal;

/**
 * Product Data Transfer Object.
 *
 * <p>Stock information is fetched from the Inventory bounded context.
 */
public record ProductDto(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String currency,
    String category,
    Integer stockQuantity,
    Boolean isAvailable) {}
