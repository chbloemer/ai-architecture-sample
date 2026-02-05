package de.sample.aiarchitecture.product.adapter.incoming.api;

import de.sample.aiarchitecture.product.application.createproduct.CreateProductResult;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.product.domain.model.EnrichedProduct;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming between use case models and DTOs.
 *
 * <p><b>Adapter Pattern:</b> This class belongs to the adapter layer and handles
 * the translation between application layer models (which wrap domain read models)
 * and REST API DTOs.
 *
 * <p>Use cases return Results containing EnrichedProduct domain read models.
 * This converter maps those to flat DTOs suitable for JSON serialization.
 */
@Component
public final class ProductDtoConverter {

  /**
   * Converts CreateProductResult to DTO.
   *
   * <p>Note: Stock information is not available in CreateProductResult as it's a write operation.
   *
   * @param output the use case output
   * @return product DTO
   */
  public ProductDto toDto(final CreateProductResult output) {
    return new ProductDto(
        output.productId(),
        output.sku(),
        output.name(),
        output.description(),
        output.priceAmount(),
        output.priceCurrency(),
        output.category(),
        null,
        null);
  }

  /**
   * Converts GetProductByIdResult to DTO.
   *
   * <p>Extracts the EnrichedProduct domain read model from the result and maps to DTO.
   *
   * @param result the use case result containing an EnrichedProduct
   * @return product DTO
   * @throws IllegalArgumentException if the result indicates product was not found
   */
  public ProductDto toDto(final GetProductByIdResult result) {
    if (!result.found() || result.product() == null) {
      throw new IllegalArgumentException("Cannot convert not-found result to DTO");
    }
    return toDto(result.product());
  }

  /**
   * Converts EnrichedProduct domain read model to DTO.
   *
   * @param product the enriched product
   * @return product DTO
   */
  public ProductDto toDto(final EnrichedProduct product) {
    return new ProductDto(
        product.productId().value().toString(),
        product.sku(),
        product.name(),
        product.description(),
        product.currentPrice().amount(),
        product.currentPrice().currency().getCurrencyCode(),
        product.category(),
        product.stockQuantity(),
        product.isAvailable());
  }
}
