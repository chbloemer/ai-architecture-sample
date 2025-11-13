package de.sample.aiarchitecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * Base class for all ArchUnit tests providing shared setup and utilities.
 */
abstract class BaseArchUnitTest extends Specification {

  // Predicate for infrastructure implementation classes (concrete implementations in infrastructure.*)
  protected static final DescribedPredicate<JavaClass> INFRASTRUCTURE_IMPLEMENTATION =
  DescribedPredicate.describe(
  "reside in infrastructure implementation", { JavaClass javaClass ->
    javaClass.getPackageName().startsWith("de.sample.aiarchitecture.infrastructure.")
  }
  )

  // Third-party packages allowed in domain layer (framework-independent)
  protected static final List<String> THIRD_PARTY_PACKAGES_ALLOWED_IN_DOMAIN = List.of(
  "java..",
  "lombok..",
  "org.apache.commons.lang3..",
  "org.apache.commons.collections4",
  "org.jspecify.annotations.."
  )

  // Base package for all classes
  protected static final String BASE_PACKAGE = "de.sample.aiarchitecture"

  // Shared Kernel packages
  protected static final String SHAREDKERNEL_PACKAGE = "${BASE_PACKAGE}.sharedkernel.."
  protected static final String SHAREDKERNEL_DOMAIN_PACKAGE = "${BASE_PACKAGE}.sharedkernel.domain.."
  protected static final String SHAREDKERNEL_APPLICATION_PACKAGE = "${BASE_PACKAGE}.sharedkernel.application.."
  protected static final String SHAREDKERNEL_APPLICATION_PORT_PACKAGE = "${BASE_PACKAGE}.sharedkernel.application.port.."

  // Bounded Context packages (Strategic DDD)
  protected static final String PRODUCT_CONTEXT_PACKAGE = "${BASE_PACKAGE}.product.."
  protected static final String PRODUCT_DOMAIN_PACKAGE = "${BASE_PACKAGE}.product.domain.."
  protected static final String PRODUCT_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.product.domain.model.."
  protected static final String PRODUCT_APPLICATION_PACKAGE = "${BASE_PACKAGE}.product.application.."
  protected static final String PRODUCT_ADAPTER_PACKAGE = "${BASE_PACKAGE}.product.adapter.."

  protected static final String CART_CONTEXT_PACKAGE = "${BASE_PACKAGE}.cart.."
  protected static final String CART_DOMAIN_PACKAGE = "${BASE_PACKAGE}.cart.domain.."
  protected static final String CART_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.cart.domain.model.."
  protected static final String CART_APPLICATION_PACKAGE = "${BASE_PACKAGE}.cart.application.."
  protected static final String CART_ADAPTER_PACKAGE = "${BASE_PACKAGE}.cart.adapter.."

  // Generic patterns (matching all bounded contexts)
  protected static final String DOMAIN_PACKAGE = "${BASE_PACKAGE}.*.domain.."
  protected static final String DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.*.domain.model.."
  protected static final String APPLICATION_PACKAGE = "${BASE_PACKAGE}.*.application.."
  protected static final String ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.."
  protected static final String INCOMING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.incoming.."
  protected static final String OUTGOING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.outgoing.."

  // Infrastructure package (shared across bounded contexts)
  protected static final String INFRASTRUCTURE_PACKAGE = "${BASE_PACKAGE}.infrastructure.."

  // Legacy aliases for backward compatibility (deprecated)
  @Deprecated
  protected static final String PORTADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.."
  @Deprecated
  protected static final String SHARED_KERNEL_PACKAGE = "${SHAREDKERNEL_PACKAGE}"

  /**
   * Custom import option to exclude architecture test classes from being analyzed.
   */
  static class DoNotIncludeArchitectureTests implements ImportOption {
    private Pattern predicate = Pattern.compile(".*/build/classes/([^/]+/)?test.*/.*")

    @Override
    boolean includes(Location location) {
      return !location.matches(predicate)
    }
  }

  /**
   * All production classes (excluding tests).
   * Shared across all architecture tests.
   */
  @Shared
  protected JavaClasses allClasses = new ClassFileImporter()
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
  .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
  .withImportOption(new DoNotIncludeArchitectureTests())
  .importPackages(BASE_PACKAGE)
}
