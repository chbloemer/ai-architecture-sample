package de.sample.aiarchitecture.product.adapter.incoming.web;

import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsResult;
import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import java.math.BigDecimal;
import java.util.List;

/**
 * Page-specific ViewModel for the product catalog page.
 *
 * <p>This ViewModel contains only primitives and is tailored to the catalog page's needs.
 * It is created from the use case result (which wraps EnrichedProduct domain read models)
 * in the adapter layer.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct list) → Controller → ViewModel → Template
 *
 * @param products the list of product summaries for display
 * @param totalProducts the total number of products
 * @param pageTitle the page title
 */
public record ProductCatalogPageViewModel(
    List<ProductItemViewModel> products,
    int totalProducts,
    String pageTitle
) {

  /**
   * Creates a ViewModel from the use case result.
   *
   * @param result the use case result containing enriched products
   * @return the page-specific ViewModel
   */
  public static ProductCatalogPageViewModel fromResult(final GetAllProductsResult result) {
    final List<ProductItemViewModel> items = result.products().stream()
        .map(ProductItemViewModel::fromEnrichedProduct)
        .toList();
    return new ProductCatalogPageViewModel(items, items.size(), "Product Catalog");
  }

  /**
   * ViewModel for a single product item in the catalog list.
   *
   * @param productId the product ID
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param imageUrl the product image URL
   * @param priceAmount the price amount
   * @param priceCurrency the price currency code
   * @param category the product category
   * @param stockQuantity the available stock
   * @param isInStock whether the product is in stock
   */
  public record ProductItemViewModel(
      String productId,
      String sku,
      String name,
      String description,
      String imageUrl,
      BigDecimal priceAmount,
      String priceCurrency,
      String category,
      int stockQuantity,
      boolean isInStock
  ) {

    /**
     * Creates a ViewModel from an enriched product domain read model.
     *
     * @param product the enriched product from the use case result
     * @return the product item ViewModel
     */
    public static ProductItemViewModel fromEnrichedProduct(final EnrichedProduct product) {
      return new ProductItemViewModel(
          product.productId().value().toString(),
          product.sku(),
          product.name(),
          product.description(),
          product.imageUrl(),
          product.currentPrice().amount(),
          product.currentPrice().currency().getCurrencyCode(),
          product.category(),
          product.stockQuantity(),
          product.isInStock()
      );
    }
  }
}
