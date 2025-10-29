package de.sample.aiarchitecture.portadapter.incoming.api.product;

import de.sample.aiarchitecture.application.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Resource for Product operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes
 * product functionality via REST API. It uses Clean Architecture use cases instead
 * of directly accessing domain services.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * rather than application services, following the Dependency Inversion Principle.
 *
 * <p><b>RESTful Design:</b> Follows REST best practices with proper HTTP methods and status codes.
 */
@RestController
@RequestMapping("/api/products")
public class ProductResource {

  private final CreateProductUseCase createProductUseCase;
  private final GetAllProductsUseCase getAllProductsUseCase;
  private final GetProductByIdUseCase getProductByIdUseCase;
  private final ProductDtoConverter converter;

  public ProductResource(
      final CreateProductUseCase createProductUseCase,
      final GetAllProductsUseCase getAllProductsUseCase,
      final GetProductByIdUseCase getProductByIdUseCase,
      final ProductDtoConverter converter) {
    this.createProductUseCase = createProductUseCase;
    this.getAllProductsUseCase = getAllProductsUseCase;
    this.getProductByIdUseCase = getProductByIdUseCase;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(
      @Valid @RequestBody final CreateProductRequest request) {

    // Convert REST request to use case input
    final CreateProductInput input = new CreateProductInput(
        request.sku(),
        request.name(),
        request.description() != null ? request.description() : "",
        request.price(),
        "EUR", // Default currency
        request.category(),
        request.stock()
    );

    // Execute use case
    final CreateProductOutput output = createProductUseCase.execute(input);

    // Convert output to DTO
    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    final GetAllProductsOutput output = getAllProductsUseCase.execute(new GetAllProductsInput());

    final List<ProductDto> products = output.products().stream()
        .map(converter::toDto)
        .toList();

    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable final String id) {
    final GetProductByIdOutput output = getProductByIdUseCase.execute(new GetProductByIdInput(id));

    if (!output.found()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(converter.toDto(output));
  }

  // Note: The following endpoints don't have corresponding use cases yet in the current implementation
  // They would need to be added if needed:
  // - GetProductBySkuUseCase
  // - DeleteProductUseCase

  // Temporarily commented out until use cases are created:
  /*
  @GetMapping("/sku/{sku}")
  public ResponseEntity<ProductDto> getProductBySku(@PathVariable final String sku) {
    // TODO: Implement GetProductBySkuUseCase
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable final String id) {
    // TODO: Implement DeleteProductUseCase
    return ResponseEntity.noContent().build();
  }
  */
}
