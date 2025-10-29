package de.sample.aiarchitecture.application;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

/**
 * Input model for creating a product.
 *
 * <p>This immutable record encapsulates all data required to create a new product.
 * It decouples the use case from presentation layer DTOs and framework concerns.
 *
 * <p><b>Clean Architecture Note:</b>
 * Input models are part of the application layer and define the contract for use cases.
 * They prevent the domain layer from depending on external frameworks or DTOs.
 *
 * @param sku the unique stock keeping unit
 * @param name the product name
 * @param description the product description
 * @param priceAmount the price amount (will be converted to Price value object)
 * @param priceCurrency the price currency (e.g., "USD", "EUR")
 * @param category the product category
 * @param stockQuantity the initial stock quantity
 */
public record CreateProductInput(
    @NonNull String sku,
    @NonNull String name,
    @NonNull String description,
    @NonNull BigDecimal priceAmount,
    @NonNull String priceCurrency,
    @NonNull String category,
    int stockQuantity
) {

  /**
   * Compact constructor with validation.
   */
  public CreateProductInput {
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU cannot be null or blank");
    }
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    if (description == null) {
      throw new IllegalArgumentException("Description cannot be null");
    }
    if (priceAmount == null || priceAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price amount must be positive");
    }
    if (priceCurrency == null || priceCurrency.isBlank()) {
      throw new IllegalArgumentException("Price currency cannot be null or blank");
    }
    if (category == null || category.isBlank()) {
      throw new IllegalArgumentException("Category cannot be null or blank");
    }
    if (stockQuantity < 0) {
      throw new IllegalArgumentException("Stock quantity cannot be negative");
    }
  }
}
