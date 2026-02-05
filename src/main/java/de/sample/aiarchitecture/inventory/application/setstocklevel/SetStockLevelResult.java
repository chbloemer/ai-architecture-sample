package de.sample.aiarchitecture.inventory.application.setstocklevel;

/**
 * Output model for setting or updating stock levels.
 *
 * @param stockLevelId the unique identifier of the stock level record
 * @param productId the product ID
 * @param availableQuantity the current available quantity
 * @param reservedQuantity the current reserved quantity
 * @param created true if a new stock level was created, false if an existing one was updated
 */
public record SetStockLevelResult(
    String stockLevelId,
    String productId,
    int availableQuantity,
    int reservedQuantity,
    boolean created) {}
