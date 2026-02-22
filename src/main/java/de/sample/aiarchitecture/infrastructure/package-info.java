/**
 * Infrastructure Layer.
 *
 * <p>Cross-cutting infrastructure concerns including configuration, security, and application
 * bootstrapping.
 */
@NullMarked
@ApplicationModule(
    type = ApplicationModule.Type.OPEN,
    allowedDependencies = {"sharedkernel", "product :: api", "pricing :: api", "inventory :: api"})
package de.sample.aiarchitecture.infrastructure;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
