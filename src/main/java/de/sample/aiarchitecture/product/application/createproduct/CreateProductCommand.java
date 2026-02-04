package de.sample.aiarchitecture.product.application.createproduct;

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
 * <p><b>Note:</b> The stockQuantity is passed via the ProductCreated event to the
 * Inventory bounded context for initialization. Product context does not store stock.
 *
 * @param sku the unique stock keeping unit
 * @param name the product name
 * @param description the product description
 * @param priceAmount the price amount (for Pricing context)
 * @param priceCurrency the price currency (e.g., "USD", "EUR")
 * @param category the product category
 * @param stockQuantity the initial stock quantity (for Inventory context, defaults to 0)
 */
public record CreateProductCommand(
    @NonNull String sku,
    @NonNull String name,
    @NonNull String description,
    @NonNull BigDecimal priceAmount,
    @NonNull String priceCurrency,
    @NonNull String category,
    int stockQuantity
) {

  /**
   * Creates a command without specifying initial stock (defaults to 0).
   */
  public CreateProductCommand(
      String sku,
      String name,
      String description,
      BigDecimal priceAmount,
      String priceCurrency,
      String category) {
    this(sku, name, description, priceAmount, priceCurrency, category, 0);
  }

  /**
   * Compact constructor with validation.
   */
  public CreateProductCommand {
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
