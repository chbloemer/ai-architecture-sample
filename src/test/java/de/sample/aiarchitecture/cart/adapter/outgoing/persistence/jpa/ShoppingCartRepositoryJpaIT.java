package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jpa;

import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.infrastructure.AiArchitectureApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AiArchitectureApplication.class)
class ShoppingCartRepositoryJpaIT {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    void save_thenFindById_andFindActiveByCustomer_shouldRoundTrip() {
        // given
        CustomerId customerId = CustomerId.of("it-customer-1");
        CartId cartId = CartId.generate();
        ShoppingCart cart = new ShoppingCart(cartId, customerId);

        // when
        shoppingCartRepository.save(cart);

        // then: find by id works
        Optional<ShoppingCart> byId = shoppingCartRepository.findById(cartId);
        assertTrue(byId.isPresent(), "Expected cart to be found by id after save");
        assertEquals(cartId.value(), byId.get().id().value());

        // and: find active cart by customer works
        Optional<ShoppingCart> active = shoppingCartRepository.findActiveCartByCustomerId(customerId);
        assertTrue(active.isPresent(), "Expected active cart for customer after save");
        assertEquals(cartId.value(), active.get().id().value());
    }
}
