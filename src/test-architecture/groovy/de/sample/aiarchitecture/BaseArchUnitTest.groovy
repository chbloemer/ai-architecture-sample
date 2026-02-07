package de.sample.aiarchitecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext
import de.sample.aiarchitecture.sharedkernel.marker.strategic.SharedKernel
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * Base class for all ArchUnit tests providing shared setup and utilities.
 */
abstract class BaseArchUnitTest extends Specification {

  // Predicate for infrastructure implementation classes that should NOT be accessed by adapters
  // Adapters must use ports (sharedkernel.marker.port.*) instead of infrastructure implementations
  protected static final DescribedPredicate<JavaClass> INFRASTRUCTURE_IMPLEMENTATION =
  DescribedPredicate.describe(
  "reside in infrastructure implementation", { JavaClass javaClass ->
    String packageName = javaClass.getPackageName()
    return packageName.startsWith("de.sample.aiarchitecture.infrastructure.")
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

  // Shared Kernel packages (new structure)
  protected static final String SHAREDKERNEL_PACKAGE = "${BASE_PACKAGE}.sharedkernel.."
  protected static final String SHAREDKERNEL_DOMAIN_PACKAGE = "${BASE_PACKAGE}.sharedkernel.domain.."
  protected static final String SHAREDKERNEL_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.sharedkernel.domain.model.."
  protected static final String SHAREDKERNEL_MARKER_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.."
  protected static final String SHAREDKERNEL_MARKER_TACTICAL_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.tactical.."
  protected static final String SHAREDKERNEL_MARKER_STRATEGIC_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.strategic.."
  protected static final String SHAREDKERNEL_MARKER_PORT_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.port.."
  protected static final String SHAREDKERNEL_MARKER_PORT_IN_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.port.in.."
  protected static final String SHAREDKERNEL_MARKER_PORT_OUT_PACKAGE = "${BASE_PACKAGE}.sharedkernel.marker.port.out.."

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

  protected static final String CHECKOUT_CONTEXT_PACKAGE = "${BASE_PACKAGE}.checkout.."
  protected static final String CHECKOUT_DOMAIN_PACKAGE = "${BASE_PACKAGE}.checkout.domain.."
  protected static final String CHECKOUT_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.checkout.domain.model.."
  protected static final String CHECKOUT_APPLICATION_PACKAGE = "${BASE_PACKAGE}.checkout.application.."
  protected static final String CHECKOUT_ADAPTER_PACKAGE = "${BASE_PACKAGE}.checkout.adapter.."

  protected static final String ACCOUNT_CONTEXT_PACKAGE = "${BASE_PACKAGE}.account.."
  protected static final String ACCOUNT_DOMAIN_PACKAGE = "${BASE_PACKAGE}.account.domain.."
  protected static final String ACCOUNT_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.account.domain.model.."
  protected static final String ACCOUNT_APPLICATION_PACKAGE = "${BASE_PACKAGE}.account.application.."
  protected static final String ACCOUNT_ADAPTER_PACKAGE = "${BASE_PACKAGE}.account.adapter.."

  protected static final String PORTAL_CONTEXT_PACKAGE = "${BASE_PACKAGE}.portal.."
  protected static final String PORTAL_DOMAIN_PACKAGE = "${BASE_PACKAGE}.portal.domain.."
  protected static final String PORTAL_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.portal.domain.model.."
  protected static final String PORTAL_APPLICATION_PACKAGE = "${BASE_PACKAGE}.portal.application.."
  protected static final String PORTAL_ADAPTER_PACKAGE = "${BASE_PACKAGE}.portal.adapter.."

  protected static final String INVENTORY_CONTEXT_PACKAGE = "${BASE_PACKAGE}.inventory.."
  protected static final String INVENTORY_DOMAIN_PACKAGE = "${BASE_PACKAGE}.inventory.domain.."
  protected static final String INVENTORY_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.inventory.domain.model.."
  protected static final String INVENTORY_APPLICATION_PACKAGE = "${BASE_PACKAGE}.inventory.application.."
  protected static final String INVENTORY_ADAPTER_PACKAGE = "${BASE_PACKAGE}.inventory.adapter.."

  protected static final String PRICING_CONTEXT_PACKAGE = "${BASE_PACKAGE}.pricing.."
  protected static final String PRICING_DOMAIN_PACKAGE = "${BASE_PACKAGE}.pricing.domain.."
  protected static final String PRICING_DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.pricing.domain.model.."
  protected static final String PRICING_APPLICATION_PACKAGE = "${BASE_PACKAGE}.pricing.application.."
  protected static final String PRICING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.pricing.adapter.."

  // Generic patterns (matching all bounded contexts)
  protected static final String DOMAIN_PACKAGE = "${BASE_PACKAGE}.*.domain.."
  protected static final String DOMAIN_MODEL_PACKAGE = "${BASE_PACKAGE}.*.domain.model.."
  protected static final String APPLICATION_PACKAGE = "${BASE_PACKAGE}.*.application.."
  protected static final String ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.."
  protected static final String INCOMING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.incoming.."
  protected static final String OUTGOING_ADAPTER_PACKAGE = "${BASE_PACKAGE}.*.adapter.outgoing.."

  // Infrastructure package (shared across bounded contexts)
  protected static final String INFRASTRUCTURE_PACKAGE = "${BASE_PACKAGE}.infrastructure.."

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

  // ============================================================================
  // BOUNDED CONTEXT DISCOVERY UTILITIES
  // ============================================================================

  /**
   * Discovers all packages annotated with @BoundedContext.
   *
   * @return Map of package name to BoundedContext annotation
   */
  protected Map<String, BoundedContext> discoverBoundedContextPackages() {
    Map<String, BoundedContext> contexts = [:]

    allClasses.each { javaClass ->
      String packageName = javaClass.getPackageName()
      // Get the root context package (e.g., de.sample.aiarchitecture.product from de.sample.aiarchitecture.product.domain.model)
      String rootPackage = extractRootContextPackage(packageName)
      if (rootPackage && !contexts.containsKey(rootPackage)) {
        BoundedContext annotation = getPackageAnnotation(rootPackage, BoundedContext)
        if (annotation != null) {
          contexts[rootPackage] = annotation
        }
      }
    }

    return contexts
  }

  /**
   * Discovers the package annotated with @SharedKernel.
   *
   * @return the shared kernel package name, or null if not found
   */
  protected String discoverSharedKernelPackage() {
    Set<String> checkedPackages = [] as Set

    for (def javaClass : allClasses) {
      String packageName = javaClass.getPackageName()
      String rootPackage = extractRootContextPackage(packageName)
      if (rootPackage && !checkedPackages.contains(rootPackage)) {
        checkedPackages.add(rootPackage)
        SharedKernel annotation = getPackageAnnotation(rootPackage, SharedKernel)
        if (annotation != null) {
          return rootPackage
        }
      }
    }

    return null
  }

  /**
   * Gets the BoundedContext annotation for a specific package.
   *
   * @param packageName the package name
   * @return the BoundedContext annotation, or null if not found
   */
  protected BoundedContext getBoundedContextAnnotation(String packageName) {
    return getPackageAnnotation(packageName, BoundedContext)
  }

  /**
   * Gets a specific annotation from a package's package-info class.
   *
   * @param packageName the package name
   * @param annotationType the annotation type to retrieve
   * @return the annotation, or null if not found
   */
  protected <T extends java.lang.annotation.Annotation> T getPackageAnnotation(String packageName, Class<T> annotationType) {
    try {
      Package pkg = Package.getPackage(packageName)
      if (pkg != null) {
        return pkg.getAnnotation(annotationType)
      }
      // Package might not be loaded yet, try loading package-info class
      Class<?> packageInfoClass = Class.forName(packageName + ".package-info")
      return packageInfoClass.getAnnotation(annotationType)
    } catch (ClassNotFoundException ignored) {
      return null
    }
  }

  /**
   * Extracts the root bounded context package from a full package name.
   * E.g., "de.sample.aiarchitecture.product.domain.model" -> "de.sample.aiarchitecture.product"
   *
   * @param fullPackageName the full package name
   * @return the root context package, or null if not a context package
   */
  protected String extractRootContextPackage(String fullPackageName) {
    if (!fullPackageName.startsWith(BASE_PACKAGE + ".")) {
      return null
    }

    String remainder = fullPackageName.substring((BASE_PACKAGE + ".").length())
    int dotIndex = remainder.indexOf('.')
    if (dotIndex > 0) {
      return BASE_PACKAGE + "." + remainder.substring(0, dotIndex)
    } else if (remainder.length() > 0) {
      return BASE_PACKAGE + "." + remainder
    }
    return null
  }

  /**
   * Gets all bounded context package patterns (with ".." suffix) for use in ArchUnit rules.
   *
   * @return list of package patterns like "de.sample.aiarchitecture.product.."
   */
  protected List<String> getBoundedContextPackagePatterns() {
    return discoverBoundedContextPackages().keySet().collect { it + ".." }
  }

  /**
   * Gets bounded context package patterns excluding a specific context.
   *
   * @param excludePackage the package to exclude (without ".." suffix)
   * @return list of package patterns
   */
  protected List<String> getBoundedContextPackagePatternsExcluding(String excludePackage) {
    return discoverBoundedContextPackages().keySet()
      .findAll { it != excludePackage }
      .collect { it + ".." }
  }
}
