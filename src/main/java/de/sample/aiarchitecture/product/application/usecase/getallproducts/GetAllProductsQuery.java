package de.sample.aiarchitecture.product.application.usecase.getallproducts;

/**
 * Input model for retrieving all products.
 *
 * <p>Currently empty but can be extended with filters, pagination, or sorting parameters.
 *
 * <p><b>Example Extensions:</b>
 * <pre>{@code
 * public record GetAllProductsQuery(
 *     String categoryFilter,
 *     int pageNumber,
 *     int pageSize,
 *     String sortBy
 * ) {}
 * }</pre>
 */
public record GetAllProductsQuery() {}
