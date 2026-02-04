package de.sample.aiarchitecture.product.application.getallproducts;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model for retrieving all products.
 *
 * <p><b>Note:</b> Stock information is not included as stock is managed by the
 * Inventory bounded context. Use InventoryService to get stock information.
 *
 * @param products the list of products
 */
public record GetAllProductsResult(@NonNull List<ProductSummary> products) {

  /**
   * Summary of a product (used in lists).
   *
   * @param productId the product ID
   * @param sku the product SKU
   * @param name the product name
   * @param priceAmount the price amount
   * @param priceCurrency the price currency
   * @param category the product category
   */
  public record ProductSummary(
      @NonNull String productId,
      @NonNull String sku,
      @NonNull String name,
      @NonNull BigDecimal priceAmount,
      @NonNull String priceCurrency,
      @NonNull String category
  ) {}
}
