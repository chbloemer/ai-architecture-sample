package de.sample.aiarchitecture.cart.adapter.incoming.api;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingCartDto(
    String cartId,
    String customerId,
    List<CartItemDto> items,
    String status,
    BigDecimal total,
    String currency,
    int itemCount) {}
