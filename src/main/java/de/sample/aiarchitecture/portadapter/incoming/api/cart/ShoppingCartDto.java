package de.sample.aiarchitecture.portadapter.incoming.api.cart;

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
