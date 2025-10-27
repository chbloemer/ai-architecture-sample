package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(
    @NotBlank String productId, @Positive int quantity) {}
