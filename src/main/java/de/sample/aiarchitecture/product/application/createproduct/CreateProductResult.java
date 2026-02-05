package de.sample.aiarchitecture.product.application.createproduct;

import java.math.BigDecimal;

/**
 * Output model for product creation.
 *
 * <p>This immutable record contains the result of creating a product.
 * It exposes only the data needed by the presentation layer, not domain entities.
 *
 * <p><b>Clean Architecture Note:</b>
 * Output models decouple the use case from domain entities and prevent
 * leaking domain complexity to outer layers.
 *
 * <p><b>Note:</b> Stock information is not included as stock is managed by the
 * Inventory bounded context. Use InventoryService to get stock information.
 *
 * @param productId the generated product ID
 * @param sku the product SKU
 * @param name the product name
 * @param description the product description
 * @param priceAmount the price amount
 * @param priceCurrency the price currency
 * @param category the product category
 */
public record CreateProductResult(
    String productId,
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category
) {}
