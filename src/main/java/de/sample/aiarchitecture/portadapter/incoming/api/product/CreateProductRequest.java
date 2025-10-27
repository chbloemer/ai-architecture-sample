package de.sample.aiarchitecture.portadapter.incoming.api.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank String sku,
    @NotBlank String name,
    String description,
    @NotNull @Positive BigDecimal price,
    @NotBlank String category,
    @NotNull @Min(0) Integer stock) {}
