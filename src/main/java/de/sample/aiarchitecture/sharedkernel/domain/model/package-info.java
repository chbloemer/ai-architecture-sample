/**
 * Universal Value Objects.
 *
 * <p>This package contains value objects that are shared across all bounded contexts.
 * These are fundamental domain concepts that have the same meaning everywhere.
 *
 * <p><b>Contents:</b>
 * <ul>
 *   <li>{@link Money} - Monetary value with currency</li>
 *   <li>{@link Price} - Product price (wraps Money with business rules)</li>
 *   <li>{@link ProductId} - Product identifier (cross-context reference)</li>
 *   <li>{@link UserId} - User identifier (cross-context reference)</li>
 * </ul>
 *
 * <p><b>Guidelines:</b>
 * <ul>
 *   <li>All value objects must be immutable (use records)</li>
 *   <li>Include validation in constructors</li>
 *   <li>Keep this package minimal - only truly universal concepts</li>
 * </ul>
 */
package de.sample.aiarchitecture.sharedkernel.domain.model;
