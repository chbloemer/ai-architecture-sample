package de.sample.aiarchitecture

import de.sample.aiarchitecture.domain.model.ddd.AggregateRoot
import de.sample.aiarchitecture.domain.model.ddd.Repository
import de.sample.aiarchitecture.domain.model.ddd.Value

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

import com.tngtech.archunit.core.domain.JavaModifier

/**
 * ArchUnit tests for DDD Tactical Patterns (Building Blocks).
 *
 * Tests the core DDD patterns:
 * - Entities: Objects with identity and lifecycle
 * - Value Objects: Immutable objects defined by their attributes
 * - Aggregates: Cluster of objects treated as a unit
 * - Repositories: Collection-like interface for aggregates
 *
 * Reference:
 * - Eric Evans' Domain-Driven Design (2003)
 * - Vaughn Vernon's Implementing DDD (2013) - especially the 4 Rules of Aggregate Design
 * - Wrox Patterns, Principles, and Practices of DDD (2015)
 */
class DddTacticalPatternsArchUnitTest extends BaseArchUnitTest {

  // ============================================================================
  // AGGREGATE ROOT PATTERN
  // ============================================================================

  def "Aggregate Roots müssen AggregateRoot<T, ID> implementieren"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .and().haveSimpleNameEndingWith("AggregateRoot")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleName("AggregateRoot") // Exclude the marker interface itself
      .should().implement(AggregateRoot.class)
      .because("Classes named *AggregateRoot must implement AggregateRoot interface (DDD pattern)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Aggregate Roots dürfen keine Felder mit anderen Aggregate Root Typen haben"() {
    expect:
    // This test enforces Vaughn Vernon's Aggregate Design Rule #2:
    // "Reference other Aggregates by Identity"
    // An aggregate should not hold direct references to other aggregate roots,
    // only their IDs. This maintains aggregate boundaries and transaction consistency.

    def aggregateRootClasses = allClasses.stream()
      .filter { it.isAssignableTo(AggregateRoot.class) }
      .filter { !it.isInterface() }
      .collect()

    def violations = []
    aggregateRootClasses.each { aggregateClass ->
      aggregateClass.getAllFields().each { field ->
        def fieldType = field.getRawType()

        // Check if field type implements AggregateRoot
        if (fieldType.isAssignableTo(AggregateRoot.class) &&
          !fieldType.equals(aggregateClass) &&  // Allow self-reference
          !fieldType.isInterface()) {
          violations.add("${aggregateClass.getName()} has field '${field.getName()}' of type ${fieldType.getName()} which is another aggregate root")
        }

        // Check collections/arrays of aggregate roots
        if (field.getRawType().getName().startsWith("java.util.List") ||
          field.getRawType().getName().startsWith("java.util.Set") ||
          field.getRawType().getName().startsWith("java.util.Collection")) {
          // Check generic type parameter
          field.getType().getActualTypeArguments().each { typeArg ->
            if (typeArg.toErasure().isAssignableTo(AggregateRoot.class) &&
              !typeArg.toErasure().isInterface()) {
              violations.add("${aggregateClass.getName()} has field '${field.getName()}' containing ${typeArg.getName()} which is an aggregate root")
            }
          }
        }
      }
    }

    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Aggregates must reference other aggregates by ID only (Vernon's Rule #2).\n" +
      "Violations found:\n" + violations.join("\n"))
    }

    true
  }

  // ============================================================================
  // ENTITY PATTERN
  // ============================================================================

  def "Entities müssen ein ID Feld haben"() {
    when:
    def entityClasses = allClasses.stream()
      .filter { it.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) }
      .filter { !it.isInterface() }
      .filter { !it.getModifiers().contains(JavaModifier.ABSTRACT) }
      .collect()

    def violations = []
    entityClasses.each { entityClass ->
      def hasIdField = entityClass.getAllFields().any { field ->
        def fieldName = field.getName().toLowerCase()
        fieldName == "id" || fieldName.endsWith("id")
      }

      if (!hasIdField) {
        violations.add("${entityClass.getName()} appears to have no ID field")
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Entities must have an identity field (DDD pattern).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Entities dürfen nicht direkt von außerhalb des Aggregates instanziiert werden"() {
    when:
    // Entities (except Aggregate Roots) should not have public constructors
    // They should only be created through their aggregate root
    // This enforces aggregate boundaries and ensures invariants

    def entityClasses = allClasses.stream()
      .filter { it.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) }
      .filter { !it.isAssignableTo(AggregateRoot.class) }  // Exclude aggregate roots
      .filter { !it.isInterface() }
      .filter { !it.isRecord() }  // Records always have public constructors
      .collect()

    def violations = []
    entityClasses.each { entityClass ->
      entityClass.getConstructors().each { constructor ->
        if (constructor.getModifiers().contains(JavaModifier.PUBLIC)) {
          violations.add("${entityClass.getName()} has public constructor - should be package-private or protected")
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Entities should not have public constructors (access only through aggregate root).\n" +
      "Note: Records are excluded from this rule.\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Entities dürfen keine Felder mit Aggregate Root Typen haben"() {
    when:
    def entityClasses = allClasses.stream()
      .filter { it.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) }
      .filter { !it.isInterface() }
      .filter { !it.isAssignableTo(AggregateRoot.class) }
      .collect()

    def violations = []
    entityClasses.each { entityClass ->
      entityClass.getAllFields().each { field ->
        def fieldType = field.getRawType()

        if (fieldType.isAssignableTo(AggregateRoot.class) &&
          !fieldType.isInterface()) {
          violations.add("${entityClass.getName()} has field '${field.getName()}' of type ${fieldType.getName()} which is an aggregate root")
        }

        if (field.getRawType().getName().startsWith("java.util.List") ||
          field.getRawType().getName().startsWith("java.util.Set") ||
          field.getRawType().getName().startsWith("java.util.Collection")) {
          field.getType().getActualTypeArguments().each { typeArg ->
            if (typeArg.toErasure().isAssignableTo(AggregateRoot.class) &&
              !typeArg.toErasure().isInterface()) {
              violations.add("${entityClass.getName()} has field '${field.getName()}' containing ${typeArg.getName()} which is an aggregate root")
            }
          }
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Entities must not contain references to aggregate roots (reference by ID only).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  // ============================================================================
  // VALUE OBJECT PATTERN
  // ============================================================================

  def "Value Objects dürfen keine Aggregate Roots oder Entities enthalten"() {
    when:
    def valueObjectClasses = allClasses.stream()
      .filter { it.isAssignableTo(Value.class) }
      .filter { !it.isInterface() }
      .collect()

    def violations = []
    valueObjectClasses.each { voClass ->
      voClass.getAllFields().each { field ->
        def fieldType = field.getRawType()

        if (fieldType.isAssignableTo(AggregateRoot.class) &&
          !fieldType.isInterface()) {
          violations.add("${voClass.getName()} has field '${field.getName()}' of type ${fieldType.getName()} which is an aggregate root")
        }

        if (fieldType.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) &&
          !fieldType.isAssignableTo(AggregateRoot.class) &&
          !fieldType.isInterface()) {
          violations.add("${voClass.getName()} has field '${field.getName()}' of type ${fieldType.getName()} which is an entity")
        }

        if (field.getRawType().getName().startsWith("java.util.List") ||
          field.getRawType().getName().startsWith("java.util.Set") ||
          field.getRawType().getName().startsWith("java.util.Collection")) {
          field.getType().getActualTypeArguments().each { typeArg ->
            def erasure = typeArg.toErasure()

            if (erasure.isAssignableTo(AggregateRoot.class) &&
              !erasure.isInterface()) {
              violations.add("${voClass.getName()} has field '${field.getName()}' containing ${typeArg.getName()} which is an aggregate root")
            }

            if (erasure.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) &&
              !erasure.isAssignableTo(AggregateRoot.class) &&
              !erasure.isInterface()) {
              violations.add("${voClass.getName()} has field '${field.getName()}' containing ${typeArg.getName()} which is an entity")
            }
          }
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Value Objects must only contain other Value Objects or primitives (Vernon's DDD).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Value Object Klassen sollten final sein (Immutabilität)"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .and().implement(Value.class)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Value objects should be immutable (final classes) - Vernon's DDD recommendation")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Value Object Felder müssen final sein (tiefe Immutabilität)"() {
    when:
    // All fields in value objects must be final to ensure deep immutability
    // Records automatically have final fields, but regular classes need this check
    // Enums are already immutable by design, so we exclude them too

    def valueObjectClasses = allClasses.stream()
      .filter { it.isAssignableTo(Value.class) }
      .filter { !it.isInterface() }
      .filter { !it.isRecord() }  // Records have implicitly final fields
      .filter { !it.isEnum() }    // Enums are immutable by design
      .collect()

    def violations = []
    valueObjectClasses.each { voClass ->
      voClass.getAllFields().each { field ->
        if (!field.getModifiers().contains(JavaModifier.FINAL) &&
          !field.getModifiers().contains(JavaModifier.STATIC)) {
          // Static fields can be non-final
          violations.add("${voClass.getName()} has non-final field '${field.getName()}'")
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Value Object fields must be final for deep immutability (Vernon's DDD).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Value Objects dürfen keine Setter Methoden haben"() {
    when:
    // Value Objects are immutable, so they should not have setter methods
    // Records don't have setters, but regular classes need this check

    def valueObjectClasses = allClasses.stream()
      .filter { it.isAssignableTo(Value.class) }
      .filter { !it.isInterface() }
      .collect()

    def violations = []
    valueObjectClasses.each { voClass ->
      voClass.getAllMethods().each { method ->
        if (method.getName().startsWith("set") &&
          method.getName().length() > 3 &&
          Character.isUpperCase(method.getName().charAt(3)) &&
          method.getRawParameterTypes().size() == 1 &&
          method.getRawReturnType().getName() == "void") {
          violations.add("${voClass.getName()} has setter method '${method.getName()}'")
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Value Objects must be immutable and should not have setter methods.\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Records für Value Objects sind zulässig (bevorzugtes Pattern für einfache Value Objects)"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .and().areRecords()
      .should().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .because("Records are a valid pattern for immutable value objects (Java 14+)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // REPOSITORY PATTERN
  // ============================================================================

  def "Repository Interfaces sollten Repository Marker Interface erweitern"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .and().areInterfaces()
      .and().haveSimpleNameEndingWith("Repository")
      .and().doNotHaveSimpleName("Repository")
      .should().beAssignableTo(Repository.class)
      .because("Repository interfaces should extend Repository marker interface")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Repository Interfaces müssen im domain Package liegen"() {
    expect:
    classes()
      .that().implement(Repository.class)
      .and().areInterfaces()
      .should().resideInAPackage(DOMAIN_PACKAGE)
      .because("Repository interfaces belong in the domain layer (DDD pattern)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Repository Implementierungen müssen im portadapter.outgoing Package liegen"() {
    expect:
    classes()
      .that().implement(Repository.class)
      .and().areNotInterfaces()
      .should().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .because("Repository implementations are outgoing adapters (outgoing ports)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Repositories dürfen nur für Aggregate Roots existieren"() {
    when:
    def repositoryInterfaces = allClasses.stream()
      .filter { it.isAssignableTo(Repository.class) }
      .filter { it.isInterface() }
      .filter { !it.getSimpleName().equals("Repository") }
      .collect()

    def violations = []
    repositoryInterfaces.each { repoInterface ->
      def repoName = repoInterface.getSimpleName()
      if (repoName.endsWith("Repository")) {
        def domainObjectName = repoName.substring(0, repoName.length() - "Repository".length())

        def domainClass = allClasses.stream()
          .filter { it.getSimpleName().equals(domainObjectName) }
          .findFirst()
          .orElse(null)

        if (domainClass != null) {
          boolean isAggregateRoot = domainClass.isAssignableTo(AggregateRoot.class)

          if (!isAggregateRoot) {
            violations.add("${repoInterface.getName()} exists for ${domainClass.getName()} which does not implement AggregateRoot")
          }
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Repositories should only exist for Aggregate Roots, not for Entities (DDD pattern).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }

  def "Repository Methoden müssen Aggregate Roots zurückgeben"() {
    when:
    // Repository methods should return Aggregate Roots or collections of Aggregate Roots
    // Not entities or value objects

    def repositoryInterfaces = allClasses.stream()
      .filter { it.isAssignableTo(Repository.class) }
      .filter { it.isInterface() }
      .filter { !it.getSimpleName().equals("Repository") }
      .collect()

    def violations = []
    repositoryInterfaces.each { repoInterface ->
      repoInterface.getMethods().each { method ->
        def returnType = method.getRawReturnType()

        // Skip void methods (like save, delete operations)
        if (returnType.getName() == "void") {
          return
        }

        // Skip methods that return primitives, Optional, or common Java types
        if (returnType.isPrimitive() ||
          returnType.getName().startsWith("java.lang") ||
          returnType.getName().startsWith("java.util.Optional")) {
          return
        }

        // Check if return type is a collection
        if (returnType.getName().startsWith("java.util.List") ||
          returnType.getName().startsWith("java.util.Set") ||
          returnType.getName().startsWith("java.util.Collection")) {
          // Check generic type parameter
          method.getRawReturnType().tryGetComponentType().ifPresent { componentType ->
            if (componentType.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) &&
              !componentType.isAssignableTo(AggregateRoot.class)) {
              violations.add("${repoInterface.getName()}.${method.getName()} returns collection of ${componentType.getName()} which is an Entity but not an Aggregate Root")
            }
          }
        } else {
          // Check if direct return type is an Entity but not Aggregate Root
          if (returnType.isAssignableTo(de.sample.aiarchitecture.domain.model.ddd.Entity.class) &&
            !returnType.isAssignableTo(AggregateRoot.class) &&
            !returnType.isInterface()) {
            violations.add("${repoInterface.getName()}.${method.getName()} returns ${returnType.getName()} which is an Entity but not an Aggregate Root")
          }
        }
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Repository methods should return Aggregate Roots, not Entities (DDD pattern).\n" +
      "Violations found:\n" + violations.join("\n"))
    }
    true
  }
}
