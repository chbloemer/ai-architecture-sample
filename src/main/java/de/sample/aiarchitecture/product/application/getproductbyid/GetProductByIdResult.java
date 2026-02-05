package de.sample.aiarchitecture.product.application.getproductbyid;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Output model for product retrieval by ID.
 *
 * <p>All fields are nullable to represent the case where product is not found.
 * Check {@link #found()} to determine if the product exists.
 *
 * <p>Stock information is fetched from the Inventory bounded context via the
 * ProductStockDataPort output port.
 *
 * @param found whether the product was found
 * @param productId the product ID (null if not found)
 * @param sku the product SKU (null if not found)
 * @param name the product name (null if not found)
 * @param description the product description (null if not found)
 * @param priceAmount the price amount (null if not found)
 * @param priceCurrency the price currency (null if not found)
 * @param category the product category (null if not found)
 * @param stockQuantity the available stock quantity (null if not found)
 * @param isAvailable whether the product is available for purchase (null if not found)
 */
public record GetProductByIdResult(
    boolean found,
    @Nullable String productId,
    @Nullable String sku,
    @Nullable String name,
    @Nullable String description,
    @Nullable BigDecimal priceAmount,
    @Nullable String priceCurrency,
    @Nullable String category,
    @Nullable Integer stockQuantity,
    @Nullable Boolean isAvailable
) {

  /**
   * Creates an output for a product that was not found.
   */
  public static GetProductByIdResult notFound() {
    return new GetProductByIdResult(false, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Creates an output for a product that was found.
   */
  public static GetProductByIdResult found(
      String productId,
      String sku,
      String name,
      String description,
      BigDecimal priceAmount,
      String priceCurrency,
      String category,
      Integer stockQuantity,
      Boolean isAvailable) {
    return new GetProductByIdResult(
        true, productId, sku, name, description, priceAmount, priceCurrency, category,
        stockQuantity, isAvailable);
  }
}
