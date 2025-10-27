package de.sample.aiarchitecture.portadapter.incoming.api.cart;

import java.math.BigDecimal;

public record CartItemDto(
    String itemId,
    String productId,
    int quantity,
    BigDecimal priceAtAddition,
    String currency) {}
