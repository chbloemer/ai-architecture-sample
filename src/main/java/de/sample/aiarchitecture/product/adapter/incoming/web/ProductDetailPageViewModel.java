package de.sample.aiarchitecture.product.adapter.incoming.web;

import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import java.math.BigDecimal;

/**
 * Page-specific ViewModel for the product detail page.
 *
 * <p>This ViewModel contains only primitives and is tailored to the detail page's needs.
 * It is created from the use case result (which wraps an EnrichedProduct domain read model)
 * in the adapter layer.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct) → Controller → ViewModel → Template
 *
 * @param productId the product ID
 * @param sku the SKU
 * @param name the product name
 * @param description the product description
 * @param priceAmount the price amount
 * @param priceCurrency the price currency code
 * @param category the product category
 * @param stockQuantity the available stock
 * @param isAvailable whether the product is available for purchase
 * @param pageTitle the page title
 */
public record ProductDetailPageViewModel(
    String productId,
    String sku,
    String name,
    String description,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity,
    boolean isAvailable,
    String pageTitle
) {

  /**
   * Creates a ViewModel from the use case result.
   *
   * @param result the use case result containing the enriched product
   * @return the page-specific ViewModel
   * @throws IllegalArgumentException if the result indicates product was not found
   */
  public static ProductDetailPageViewModel fromResult(final GetProductByIdResult result) {
    if (!result.found() || result.product() == null) {
      throw new IllegalArgumentException("Cannot create ViewModel from not-found result");
    }

    final EnrichedProduct product = result.product();

    return new ProductDetailPageViewModel(
        product.productId().value().toString(),
        product.sku(),
        product.name(),
        product.description().isEmpty() ? "No description available." : product.description(),
        product.currentPrice().amount(),
        product.currentPrice().currency().getCurrencyCode(),
        product.category(),
        product.stockQuantity(),
        product.isAvailable(),
        product.name()
    );
  }
}
