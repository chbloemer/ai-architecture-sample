package de.sample.aiarchitecture.product.adapter.incoming.api;

import java.math.BigDecimal;

/**
 * Product Data Transfer Object.
 *
 * <p><b>Note:</b> Stock information is not included as stock is managed by the
 * Inventory bounded context. Use the Inventory API to get stock information.
 */
public record ProductDto(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String currency,
    String category) {}
