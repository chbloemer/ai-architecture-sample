package de.sample.aiarchitecture.inventory.application.setstocklevel;

/**
 * Input model for setting or updating stock levels.
 *
 * @param productId the product ID
 * @param quantity the stock quantity to set (must be non-negative)
 */
public record SetStockLevelCommand(
    String productId,
    int quantity) {

  /**
   * Compact constructor with validation.
   */
  public SetStockLevelCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative");
    }
  }
}
