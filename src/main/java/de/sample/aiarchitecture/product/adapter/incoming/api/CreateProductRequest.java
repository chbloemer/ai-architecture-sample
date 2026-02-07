package de.sample.aiarchitecture.product.adapter.incoming.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank String sku,
    @NotBlank String name,
    String description,
    String imageUrl,
    @NotNull @Positive BigDecimal price,
    @NotBlank String category,
    @NotNull @Min(0) Integer stock) {}
