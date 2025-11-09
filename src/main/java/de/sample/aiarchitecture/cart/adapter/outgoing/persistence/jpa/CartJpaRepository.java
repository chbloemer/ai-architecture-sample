package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, String>, JpaSpecificationExecutor<CartEntity> {
  List<CartEntity> findByCustomerId(String customerId);
  Optional<CartEntity> findFirstByCustomerIdAndStatusOrderByUpdatedAtDesc(String customerId, String status);
}
