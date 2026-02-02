package de.sample.aiarchitecture.product.adapter.incoming.api;

import de.sample.aiarchitecture.product.application.createproduct.CreateProductResult;
import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsResult;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.product.domain.model.Product;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming between domain/use case models and DTOs.
 *
 * <p>This converter supports both:
 * <ul>
 *   <li>Domain entities (Product) - for legacy code
 *   <li>Use case outputs - for Clean Architecture pattern
 * </ul>
 *
 * <p><b>Adapter Pattern:</b> This class belongs to the adapter layer and handles
 * the translation between application layer models and REST API DTOs.
 */
@Component
public final class ProductDtoConverter {

  /**
   * Converts domain Product entity to DTO.
   *
   * @param product the domain product
   * @return product DTO
   * @deprecated Use {@link #toDto(CreateProductResult)} or other use case output converters instead
   */
  @Deprecated
  public ProductDto toDto(final Product product) {
    return new ProductDto(
        product.id().value(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.price().value().amount(),
        product.price().value().currency().getCurrencyCode(),
        product.category().name(),
        product.stock().quantity());
  }

  /**
   * Converts CreateProductResult to DTO.
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
        output.stockQuantity());
  }

  /**
   * Converts GetProductByIdResult to DTO.
   *
   * @param output the use case output
   * @return product DTO
   */
  public ProductDto toDto(final GetProductByIdResult output) {
    return new ProductDto(
        output.productId(),
        output.sku(),
        output.name(),
        output.description(),
        output.priceAmount(),
        output.priceCurrency(),
        output.category(),
        output.stockQuantity());
  }

  /**
   * Converts ProductSummary (from GetAllProductsResult) to DTO.
   *
   * @param summary the product summary
   * @return product DTO
   */
  public ProductDto toDto(final GetAllProductsResult.ProductSummary summary) {
    return new ProductDto(
        summary.productId(),
        summary.sku(),
        summary.name(),
        null, // Summary doesn't include description
        summary.priceAmount(),
        summary.priceCurrency(),
        summary.category(),
        summary.stockQuantity());
  }
}
