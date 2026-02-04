package de.sample.aiarchitecture.pricing.application.getpricesforproducts;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import org.jspecify.annotations.NonNull;

/**
 * Input model for retrieving prices for multiple products.
 *
 * @param productIds the collection of product IDs to get prices for
 */
public record GetPricesForProductsQuery(@NonNull Collection<ProductId> productIds) {}
