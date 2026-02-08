package de.sample.aiarchitecture.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for AI Architecture Sample.
 *
 * <p>This application demonstrates Domain-Driven Design (DDD), Hexagonal Architecture,
 * and Onion Architecture patterns with an e-commerce domain (Product Catalog and Shopping Cart).
 *
 * <p><b>Package Location:</b> Located in {@code infrastructure} because the Spring Boot
 * application class is framework-specific infrastructure code, not part of any bounded context.
 *
 * <p><b>Component Scanning:</b> Explicitly configured to scan the entire
 * {@code de.sample.aiarchitecture} package tree to discover all bounded contexts
 * (product, cart, portal) and shared kernel components.
 */
@SpringBootApplication(scanBasePackages = "de.sample.aiarchitecture")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "de.sample.aiarchitecture")
@org.springframework.boot.persistence.autoconfigure.EntityScan(basePackages = "de.sample.aiarchitecture")
public class AiArchitectureApplication {

  public static void main(final String[] args) {
    SpringApplication.run(AiArchitectureApplication.class, args);
  }
}
