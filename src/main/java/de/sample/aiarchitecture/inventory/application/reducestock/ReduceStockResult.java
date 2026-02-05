package de.sample.aiarchitecture.inventory.application.reducestock;

/**
 * Result of reducing stock for a product.
 *
 * @param productId the product ID
 * @param previousStock the stock level before reduction
 * @param newStock the stock level after reduction
 * @param success whether the reduction was successful
 * @param errorMessage error message if reduction failed (null if successful)
 */
public record ReduceStockResult(
    String productId,
    int previousStock,
    int newStock,
    boolean success,
    String errorMessage) {

  public static ReduceStockResult success(String productId, int previousStock, int newStock) {
    return new ReduceStockResult(productId, previousStock, newStock, true, null);
  }

  public static ReduceStockResult failure(String productId, String errorMessage) {
    return new ReduceStockResult(productId, 0, 0, false, errorMessage);
  }
}
