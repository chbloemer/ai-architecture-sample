package de.sample.aiarchitecture.cart.adapter.incoming.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
    @NotBlank String productId, @Positive int quantity) {}
