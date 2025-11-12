package de.sample.aiarchitecture.product.application.createproduct;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

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
 * @param productId the generated product ID
 * @param sku the product SKU
 * @param name the product name
 * @param description the product description
 * @param priceAmount the price amount
 * @param priceCurrency the price currency
 * @param category the product category
 * @param stockQuantity the stock quantity
 */
public record CreateProductResponse(
    @NonNull String productId,
    @NonNull String sku,
    @NonNull String name,
    @NonNull String description,
    @NonNull BigDecimal priceAmount,
    @NonNull String priceCurrency,
    @NonNull String category,
    int stockQuantity
) {}
