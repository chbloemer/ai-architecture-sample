/**
 * Domain read models and builders for the Product bounded context.
 *
 * <p>This package contains:
 * <ul>
 *   <li>Extended interest interfaces for enriched read models</li>
 *   <li>Builders implementing the Interest Interface pattern</li>
 * </ul>
 *
 * <p>Read models combine aggregate state with external data from other contexts
 * (Pricing, Inventory) to provide complete views for queries.
 *
 * @see de.sample.aiarchitecture.product.domain.model.ProductStateInterest
 * @see de.sample.aiarchitecture.product.domain.model.EnrichedProduct
 */
@NullMarked
package de.sample.aiarchitecture.product.domain.readmodel;

import org.jspecify.annotations.NullMarked;
