package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class CartEntity {

  @Id
  @Column(length = 64)
  private String id;

  @Column(name = "customer_id", nullable = false, length = 64)
  private String customerId;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<CartItemEntity> items = new ArrayList<>();

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getCustomerId() { return customerId; }
  public void setCustomerId(String customerId) { this.customerId = customerId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public List<CartItemEntity> getItems() { return items; }
  public void setItems(List<CartItemEntity> items) { this.items = items; }
}
