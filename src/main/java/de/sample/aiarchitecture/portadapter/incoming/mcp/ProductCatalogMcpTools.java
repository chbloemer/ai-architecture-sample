package de.sample.aiarchitecture.portadapter.incoming.mcp;

import de.sample.aiarchitecture.application.ProductApplicationService;
import de.sample.aiarchitecture.domain.model.product.Category;
import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.product.SKU;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import de.sample.aiarchitecture.portadapter.incoming.api.product.ProductDto;
import de.sample.aiarchitecture.portadapter.incoming.api.product.ProductDtoConverter;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * MCP Tools for Product Catalog operations.
 *
 * <p>This class exposes product catalog functionality as MCP tools that can be invoked by AI
 * models through the Model Context Protocol. These tools provide read-only access to the product
 * catalog, allowing AI assistants to query products, search by category, and retrieve product
 * details.
 *
 * <p><b>Primary Adapter:</b> This is a primary (incoming) adapter in the Hexagonal Architecture
 * pattern, exposing domain functionality through the MCP protocol. It uses the application service
 * layer to access domain logic and converts domain objects to DTOs for external consumption.
 *
 * <p><b>Available Tools:</b>
 * <ul>
 *   <li>{@link #getAllProducts()} - Retrieve all products in the catalog
 *   <li>{@link #findProductBySku(String)} - Find a specific product by SKU
 *   <li>{@link #findProductsByCategory(String)} - Find products in a specific category
 *   <li>{@link #getProductById(String)} - Get detailed product information by ID
 * </ul>
 *
 * @see ProductApplicationService
 * @see ProductDto
 */
@Component
public class ProductCatalogMcpTools {

  private final ProductApplicationService productApplicationService;
  private final ProductDtoConverter productDtoConverter;

  public ProductCatalogMcpTools(
      final ProductApplicationService productApplicationService,
      final ProductDtoConverter productDtoConverter) {
    this.productApplicationService = productApplicationService;
    this.productDtoConverter = productDtoConverter;
  }

  /**
   * Retrieves all products in the catalog.
   *
   * <p>This tool returns a complete list of all products available in the e-commerce catalog,
   * including their SKU, name, description, price, category, and stock information.
   *
   * @return list of all products as DTOs
   */
  @McpTool(name="all-products",description = "Get all products in the catalog. Returns complete product information including SKU, name, description, price, category, and available stock.")
  public List<ProductDto> getAllProducts() {
    final List<Product> products = productApplicationService.getAllProducts();
    return products.stream().map(productDtoConverter::toDto).toList();
  }

  /**
   * Finds a product by its SKU (Stock Keeping Unit).
   *
   * <p>SKUs are unique identifiers for products in the inventory system. This tool allows
   * searching for a specific product using its SKU code.
   *
   * @param sku the product SKU (must be uppercase letters, numbers, and hyphens only)
   * @return product information if found, or null if not found
   * @throws IllegalArgumentException if SKU format is invalid
   */
  @McpTool(name="product-by-sku", description = "Find a product by its SKU (Stock Keeping Unit). SKU must contain only uppercase letters, numbers, and hyphens (e.g., 'LAPTOP-123', 'BOOK-456').")
  public ProductDto findProductBySku(@NonNull final String sku) {
    return productApplicationService
        .findProductBySku(SKU.of(sku))
        .map(productDtoConverter::toDto)
        .orElse(null);
  }

  /**
   * Finds all products in a specific category.
   *
   * <p>This tool allows filtering products by category. Available categories include: Electronics,
   * Clothing, Books, Home &amp; Garden, and Sports &amp; Outdoors.
   *
   * @param categoryName the category name (case-sensitive)
   * @return list of products in the specified category
   */
  @McpTool(name="product-by-category",description = "Find all products in a specific category. Available categories: Electronics, Clothing, Books, Home & Garden, Sports & Outdoors. Returns a list of products matching the category.")
  public List<ProductDto> findProductsByCategory(@NonNull final String categoryName) {
    final Category category = Category.of(categoryName);
    final List<Product> products = productApplicationService.findProductsByCategory(category);
    return products.stream().map(productDtoConverter::toDto).toList();
  }

  /**
   * Gets detailed product information by product ID.
   *
   * <p>This tool retrieves complete product details using the internal product ID. Useful for
   * getting detailed information about a specific product when the ID is known.
   *
   * @param id the product ID (UUID format)
   * @return product information if found, or null if not found
   * @throws IllegalArgumentException if ID format is invalid
   */
  @McpTool(name="product-by-id", description = "Get detailed product information by product ID. Requires the internal product UUID. Returns complete product details including all attributes.")
  public ProductDto getProductById(@NonNull final String id) {
    return productApplicationService
        .findProductById(ProductId.of(id))
        .map(productDtoConverter::toDto)
        .orElse(null);
  }
}
