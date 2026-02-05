package de.sample.aiarchitecture.inventory.application.setstocklevel;

import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for setting or updating stock levels.
 *
 * <p>This use case orchestrates stock level management by:
 *
 * <ol>
 *   <li>Looking up existing stock level for the product
 *   <li>Creating a new stock level or updating the existing one
 *   <li>Persisting the changes
 *   <li>Publishing domain events
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SetStockLevelInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SetStockLevelUseCase implements SetStockLevelInputPort {

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;

  public SetStockLevelUseCase(
      final StockLevelRepository stockLevelRepository,
      final DomainEventPublisher eventPublisher) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public SetStockLevelResult execute(final SetStockLevelCommand command) {
    final ProductId productId = ProductId.of(command.productId());

    // Check if stock level exists for this product
    final var existingStockLevel = stockLevelRepository.findByProductId(productId);

    final StockLevel stockLevel;
    final boolean created;

    if (existingStockLevel.isPresent()) {
      // Update existing stock level
      stockLevel = existingStockLevel.get();
      stockLevel.setAvailableQuantity(command.quantity());
      created = false;
    } else {
      // Create new stock level
      stockLevel = StockLevel.create(productId, command.quantity());
      created = true;
    }

    // Persist
    stockLevelRepository.save(stockLevel);

    // Publish domain events
    eventPublisher.publishAndClearEvents(stockLevel);

    // Map to result
    return new SetStockLevelResult(
        stockLevel.id().value(),
        stockLevel.productId().value(),
        stockLevel.availableQuantity().value(),
        stockLevel.reservedQuantity().value(),
        created);
  }
}
