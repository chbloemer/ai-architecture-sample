package de.sample.aiarchitecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for AI Architecture Sample.
 *
 * <p>This application demonstrates Domain-Driven Design (DDD), Hexagonal Architecture,
 * and Onion Architecture patterns with an e-commerce domain (Product Catalog and Shopping Cart).
 */
@SpringBootApplication
public class AiArchitectureApplication {

  public static void main(final String[] args) {
    SpringApplication.run(AiArchitectureApplication.class, args);
  }
}
