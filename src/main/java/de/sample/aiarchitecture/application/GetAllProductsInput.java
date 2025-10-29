package de.sample.aiarchitecture.application;

/**
 * Input model for retrieving all products.
 *
 * <p>Currently empty but can be extended with filters, pagination, or sorting parameters.
 *
 * <p><b>Example Extensions:</b>
 * <pre>{@code
 * public record GetAllProductsInput(
 *     String categoryFilter,
 *     int pageNumber,
 *     int pageSize,
 *     String sortBy
 * ) {}
 * }</pre>
 */
public record GetAllProductsInput() {}
