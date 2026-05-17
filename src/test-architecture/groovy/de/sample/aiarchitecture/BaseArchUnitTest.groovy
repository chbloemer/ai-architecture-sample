package de.sample.aiarchitecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.Location
import de.sample.aiarchitecture.sharedkernel.marker.port.in.InputPort
import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort
import de.sample.aiarchitecture.sharedkernel.marker.port.out.Repository
import de.sample.aiarchitecture.sharedkernel.marker.port.out.Store
import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext
import de.sample.aiarchitecture.sharedkernel.marker.strategic.OpenHostService
import de.sample.aiarchitecture.sharedkernel.marker.strategic.SharedKernel
import de.sample.aiarchitecture.sharedkernel.marker.tactical.AggregateRoot
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainEvent
import de.sample.aiarchitecture.sharedkernel.marker.tactical.DomainService
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Entity
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Factory
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Id
import de.sample.aiarchitecture.sharedkernel.marker.tactical.IntegrationEvent
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Specification as DomainSpecification
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value
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

  // ============================================================================
  // CONFIGURATION (mirrors dca-bootstrap skill — kept here for portability)
  // ============================================================================

  // Base package for all classes
  protected static final String BASE_PACKAGE = "de.sample.aiarchitecture"

  // Layer subpackage names — DCA defaults. Adapt if the project uses different conventions.
  protected static final String DOMAIN_SUBPKG         = "domain"
  protected static final String APP_SUBPKG            = "application"
  protected static final String ADAPTER_SUBPKG        = "adapter"
  protected static final String INCOMING_SUBFOLDER    = "incoming"      // "in" in Hombergs-style projects
  protected static final String OUTGOING_SUBFOLDER    = "outgoing"      // "out" in Hombergs-style projects
  protected static final String INFRASTRUCTURE_SUBPKG = "infrastructure"

  // Naming suffixes — adjust if a project uses *ApplicationService etc.
  protected static final String USE_CASE_IMPL_SUFFIX  = "UseCase"
  protected static final String REST_CONTROLLER_SUFFIX = "Resource"

  // Marker class constants — let rule classes reference these instead of importing markers directly.
  // This makes the rules portable to projects with their own marker namespaces.
  protected static final Class<?> AGGREGATE_ROOT_MARKER     = AggregateRoot
  protected static final Class<?> ENTITY_MARKER             = Entity
  protected static final Class<?> VALUE_MARKER              = Value
  protected static final Class<?> ID_MARKER                 = Id
  protected static final Class<?> REPOSITORY_MARKER         = Repository
  protected static final Class<?> STORE_MARKER              = Store
  protected static final Class<?> USE_CASE_MARKER           = UseCase
  protected static final Class<?> INPUT_PORT_MARKER         = InputPort
  protected static final Class<?> OUTPUT_PORT_MARKER        = OutputPort
  protected static final Class<?> DOMAIN_EVENT_MARKER       = DomainEvent
  protected static final Class<?> INTEGRATION_EVENT_MARKER  = IntegrationEvent
  protected static final Class<?> DOMAIN_SERVICE_MARKER     = DomainService
  protected static final Class<?> FACTORY_MARKER            = Factory
  protected static final Class<?> SPECIFICATION_MARKER      = DomainSpecification

  protected static final Class<? extends java.lang.annotation.Annotation> BOUNDED_CONTEXT_ANNOTATION   = BoundedContext
  protected static final Class<? extends java.lang.annotation.Annotation> SHARED_KERNEL_ANNOTATION     = SharedKernel
  protected static final Class<? extends java.lang.annotation.Annotation> OPEN_HOST_SERVICE_ANNOTATION = OpenHostService

  // Extra non-context modules (e.g. backoffice) that belong in some layer rules but aren't bounded contexts.
  protected static final List<String> EXTRA_APPLICATION_PACKAGES = List.of("${BASE_PACKAGE}.backoffice.application..")
  protected static final List<String> EXTRA_ADAPTER_PACKAGES     = List.of("${BASE_PACKAGE}.backoffice.adapter..")

  // ============================================================================

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

  // Backoffice module (operational, not a bounded context)
  protected static final String BACKOFFICE_PACKAGE = "${BASE_PACKAGE}.backoffice.."
  protected static final String BACKOFFICE_APPLICATION_PACKAGE = "${BASE_PACKAGE}.backoffice.application.."
  protected static final String BACKOFFICE_ADAPTER_PACKAGE = "${BASE_PACKAGE}.backoffice.adapter.."

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

  /**
   * Incoming adapter patterns for all bounded contexts PLUS the shared kernel's incoming
   * adapter package (if it exists). Cross-cutting Response classes (ErrorResponse, base
   * Response, generic SimpleResponse) typically live in the shared kernel adapter — without
   * including it, rules that check for *Response placement will miss them.
   *
   * <p>The shared kernel package is discovered dynamically via the @SharedKernel annotation
   * on its package-info.java — works regardless of the package name (shared/common/sharedkernel).
   *
   * @return array of patterns like "de.sample.aiarchitecture.product.adapter.incoming..", plus
   *         the shared kernel adapter pattern if a @SharedKernel package exists
   */
  protected String[] allIncomingAdapterPatterns() {
    def patterns = discoverBoundedContextPackages().keySet().collect {
      it + ".adapter.incoming.."
    }
    String sharedKernelPkg = discoverSharedKernelPackage()
    if (sharedKernelPkg != null) {
      patterns.add(sharedKernelPkg + ".adapter.incoming..")
    }
    return patterns.toArray(new String[0])
  }

  /**
   * Outgoing adapter patterns for all bounded contexts plus the shared kernel's outgoing
   * adapter package. Mirrors {@link #allIncomingAdapterPatterns()}.
   */
  protected String[] allOutgoingAdapterPatterns() {
    def patterns = discoverBoundedContextPackages().keySet().collect {
      it + ".adapter.outgoing.."
    }
    String sharedKernelPkg = discoverSharedKernelPackage()
    if (sharedKernelPkg != null) {
      patterns.add(sharedKernelPkg + ".adapter.outgoing..")
    }
    return patterns.toArray(new String[0])
  }

  // ============================================================================
  // GENERIC PATTERN HELPERS (per-context, dynamic via @BoundedContext)
  // ============================================================================

  /** Domain patterns (full domain layer) for all discovered bounded contexts. */
  protected String[] getBoundedContextDomainPatterns() {
    return discoverBoundedContextPackages().keySet()
        .collect { "${it}.${DOMAIN_SUBPKG}.." }
        .toArray(new String[0])
  }

  /** Domain-model patterns for all discovered bounded contexts. */
  protected String[] getBoundedContextDomainModelPatterns() {
    return discoverBoundedContextPackages().keySet()
        .collect { "${it}.${DOMAIN_SUBPKG}.model.." }
        .toArray(new String[0])
  }

  /** Application patterns for all discovered bounded contexts. */
  protected String[] getBoundedContextApplicationPatterns() {
    return discoverBoundedContextPackages().keySet()
        .collect { "${it}.${APP_SUBPKG}.." }
        .toArray(new String[0])
  }

  /** Adapter patterns for all discovered bounded contexts. */
  protected String[] getBoundedContextAdapterPatterns() {
    return discoverBoundedContextPackages().keySet()
        .collect { "${it}.${ADAPTER_SUBPKG}.." }
        .toArray(new String[0])
  }

  /** Bounded-context patterns as an array (varargs-ready). */
  protected String[] getBoundedContextPackagePatternsArray() {
    return getBoundedContextPackagePatterns().toArray(new String[0])
  }

  // ============================================================================
  // COMBINED PATTERN HELPERS (shared kernel and/or extra modules included)
  // ============================================================================

  /** Domain patterns for all bounded contexts PLUS the shared kernel domain. */
  protected String[] allDomainPatternsWithSharedKernel() {
    def patterns = getBoundedContextDomainPatterns().toList()
    patterns.add(SHAREDKERNEL_DOMAIN_PACKAGE)
    return patterns.toArray(new String[0])
  }

  /** Domain-model patterns for all bounded contexts PLUS the shared kernel domain. */
  protected String[] allDomainModelPatternsWithSharedKernel() {
    def patterns = getBoundedContextDomainModelPatterns().toList()
    patterns.add(SHAREDKERNEL_DOMAIN_PACKAGE)
    return patterns.toArray(new String[0])
  }

  /** Application patterns for all contexts PLUS extra non-context modules (backoffice). */
  protected String[] allApplicationPatterns() {
    def patterns = getBoundedContextApplicationPatterns().toList()
    patterns.addAll(EXTRA_APPLICATION_PACKAGES)
    return patterns.toArray(new String[0])
  }

  /** Adapter patterns for all contexts PLUS extra non-context modules (backoffice). */
  protected String[] allAdapterPatterns() {
    def patterns = getBoundedContextAdapterPatterns().toList()
    patterns.addAll(EXTRA_ADAPTER_PACKAGES)
    return patterns.toArray(new String[0])
  }
}
