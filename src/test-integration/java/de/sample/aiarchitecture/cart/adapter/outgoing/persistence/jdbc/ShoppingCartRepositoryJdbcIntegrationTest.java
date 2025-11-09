package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jdbc;

import de.sample.aiarchitecture.cart.adapter.outgoing.persistence.JdbcShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.cart.domain.model.CustomerId;
import de.sample.aiarchitecture.cart.domain.model.Quantity;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.spec.ActiveCart;
import de.sample.aiarchitecture.cart.domain.spec.ComposedCartSpecification;
import de.sample.aiarchitecture.cart.domain.spec.HasMinTotal;
import de.sample.aiarchitecture.infrastructure.AiArchitectureApplication;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the JDBC ShoppingCartRepository implementation using the "jdbc" profile.
 *
 * Mirrors the scenarios covered by the JPA-based test to ensure specification pushdown and paging
 * work equivalently in the JDBC adapter.
 */
@ActiveProfiles("jdbc")
@SpringBootTest(classes = AiArchitectureApplication.class)
class ShoppingCartRepositoryJdbcIntegrationTest {

    @Autowired
    private JdbcShoppingCartRepository shoppingCartRepository;

    @Test
    void save_thenFindById_andFindActiveByCustomer_shouldRoundTrip() {
        // given
        CustomerId customerId = CustomerId.of("it-jdbc-customer-1");
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

    @Test
    void findBy_spec_withMinTotal_andActive_paginatesAndFilters() {
        // given: two carts for same customer, only one meeting min total
        CustomerId customerId = CustomerId.of("it-jdbc-customer-2");

        ShoppingCart small = new ShoppingCart(CartId.generate(), customerId);
        small.addItem(ProductId.of("P1"), Quantity.of(1), Price.of(Money.euro(10.00)));

        ShoppingCart big = new ShoppingCart(CartId.generate(), customerId);
        big.addItem(ProductId.of("P2"), Quantity.of(3), Price.of(Money.euro(25.00))); // total 75 EUR

        shoppingCartRepository.save(small);
        shoppingCartRepository.save(big);

        // when: compose spec and query
        var composed = new ActiveCart()
                .and(new HasMinTotal(Money.euro(50.00)));
        var spec = new ComposedCartSpecification(composed);

        Page<ShoppingCart> page = shoppingCartRepository.findBy(spec, PageRequest.of(0, 10));

        // then: only the big cart should be returned
        assertEquals(1, page.getTotalElements(), "Expected exactly one cart meeting the spec");
        assertEquals(1, page.getContent().size(), "Expected a single cart in the first page");
        assertEquals(big.id().value(), page.getContent().get(0).id().value());
    }
}
