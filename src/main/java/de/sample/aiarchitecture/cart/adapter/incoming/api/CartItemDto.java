package de.sample.aiarchitecture.cart.adapter.incoming.api;

import java.math.BigDecimal;

public record CartItemDto(
    String itemId,
    String productId,
    int quantity,
    BigDecimal priceAtAddition,
    String currency) {}
