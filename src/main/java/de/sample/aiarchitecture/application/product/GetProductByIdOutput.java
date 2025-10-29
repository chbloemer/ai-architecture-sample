package de.sample.aiarchitecture.application.product;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Output model for product retrieval by ID.
 *
 * <p>All fields are nullable to represent the case where product is not found.
 * Check {@link #found()} to determine if the product exists.
 *
 * @param found whether the product was found
 * @param productId the product ID (null if not found)
 * @param sku the product SKU (null if not found)
 * @param name the product name (null if not found)
 * @param description the product description (null if not found)
 * @param priceAmount the price amount (null if not found)
 * @param priceCurrency the price currency (null if not found)
 * @param category the product category (null if not found)
 * @param stockQuantity the stock quantity (0 if not found)
 */
public record GetProductByIdOutput(
    boolean found,
    @Nullable String productId,
    @Nullable String sku,
    @Nullable String name,
    @Nullable String description,
    @Nullable BigDecimal priceAmount,
    @Nullable String priceCurrency,
    @Nullable String category,
    int stockQuantity
) {

  /**
   * Creates an output for a product that was not found.
   */
  public static GetProductByIdOutput notFound() {
    return new GetProductByIdOutput(false, null, null, null, null, null, null, null, 0);
  }

  /**
   * Creates an output for a product that was found.
   */
  public static GetProductByIdOutput found(
      String productId,
      String sku,
      String name,
      String description,
      BigDecimal priceAmount,
      String priceCurrency,
      String category,
      int stockQuantity) {
    return new GetProductByIdOutput(
        true, productId, sku, name, description, priceAmount, priceCurrency, category, stockQuantity);
  }
}
