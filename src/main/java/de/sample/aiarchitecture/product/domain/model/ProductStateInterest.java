package de.sample.aiarchitecture.product.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.StateInterest;

/**
 * Interest interface for receiving state from {@link Product} aggregate.
 *
 * <p>This interface defines the contract for consumers interested in Product state.
 * Implementations receive state through the {@code receive*()} methods, which are called
 * when the aggregate exposes its state via {@code provideStateTo(ProductStateInterest)}.
 *
 * <p><b>Usage:</b>
 * <ul>
 *   <li>Read Model builders implement this interface to receive state updates</li>
 *   <li>Projection handlers implement this to build query-optimized views</li>
 * </ul>
 *
 * @see Product
 * @see StateInterest
 */
public interface ProductStateInterest extends StateInterest {

  /**
   * Receives the product identifier.
   *
   * @param productId the unique product identifier
   */
  void receiveProductId(ProductId productId);

  /**
   * Receives the product SKU.
   *
   * @param sku the stock keeping unit
   */
  void receiveSku(String sku);

  /**
   * Receives the product name.
   *
   * @param name the product name
   */
  void receiveName(String name);

  /**
   * Receives the product description.
   *
   * @param description the product description
   */
  void receiveDescription(String description);

  /**
   * Receives the product category.
   *
   * @param category the product category name
   */
  void receiveCategory(String category);
}
