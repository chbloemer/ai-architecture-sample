package de.sample.aiarchitecture.product.application.getallproducts;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output model for retrieving all products.
 *
 * <p>Stock information is fetched from the Inventory bounded context via the
 * ProductStockDataPort output port.
 *
 * @param products the list of products
 */
public record GetAllProductsResult(List<ProductSummary> products) {

  /**
   * Summary of a product (used in lists).
   *
   * @param productId the product ID
   * @param sku the product SKU
   * @param name the product name
   * @param priceAmount the price amount
   * @param priceCurrency the price currency
   * @param category the product category
   * @param stockQuantity the available stock quantity
   */
  public record ProductSummary(
      String productId,
      String sku,
      String name,
      BigDecimal priceAmount,
      String priceCurrency,
      String category,
      int stockQuantity
  ) {}
}
