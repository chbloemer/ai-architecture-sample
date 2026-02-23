/**
 * Backoffice Module.
 *
 * <p>Operational module providing administrative views for monitoring and managing the application.
 * Not a business bounded context — this module serves cross-cutting operational concerns like event
 * publication logs, and will grow to include dashboards and admin navigation.
 *
 * <p>Context-specific admin pages (product editing, pricing, inventory) live in their respective
 * bounded contexts under {@code /backoffice/{context}/}, not in this module.
 */
@NullMarked
@ApplicationModule(allowedDependencies = {"sharedkernel", "infrastructure"})
package de.sample.aiarchitecture.backoffice;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
