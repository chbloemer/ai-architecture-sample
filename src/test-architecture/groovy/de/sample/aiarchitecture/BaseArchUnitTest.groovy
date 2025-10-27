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

  // Predicate for infrastructure implementation classes (everything except infrastructure.api)
  protected static final DescribedPredicate<JavaClass> INFRASTRUCTURE_IMPLEMENTATION =
  DescribedPredicate.describe(
  "reside in infrastructure implementation (not API)", { JavaClass javaClass ->
    javaClass.getPackageName().startsWith("de.sample.aiarchitecture.infrastructure.") &&
      !javaClass.getPackageName().startsWith("de.sample.aiarchitecture.infrastructure.api")
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

  // Domain packages
  protected static final String DOMAIN_PACKAGE = "${BASE_PACKAGE}.domain.."
  protected static final String DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.domain.model.."

  // Application package
  protected static final String APPLICATION_PACKAGE = "${BASE_PACKAGE}.application.."

  // Port adapter packages
  protected static final String PORTADAPTER_PACKAGE = "${BASE_PACKAGE}.portadapter.."
  protected static final String INCOMING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.portadapter.incoming.."
  protected static final String OUTGOING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.portadapter.outgoing.."

  // Infrastructure package
  protected static final String INFRASTRUCTURE_PACKAGE = "${BASE_PACKAGE}.infrastructure.."
  protected static final String INFRASTRUCTURE_API_PACKAGE = "${BASE_PACKAGE}.infrastructure.api.."

  // Bounded Context packages (Strategic DDD)
  protected static final String SHARED_KERNEL_PACKAGE = "${DOMAIN_MODEL_PACKAGE}shared.."
  protected static final String PRODUCT_CONTEXT_PACKAGE = "${DOMAIN_MODEL_PACKAGE}product.."
  protected static final String CART_CONTEXT_PACKAGE = "${DOMAIN_MODEL_PACKAGE}cart.."

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
