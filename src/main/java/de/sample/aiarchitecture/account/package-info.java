/**
 * Account Bounded Context.
 *
 * <p>Responsible for user account management, authentication, and profile handling.
 */
@NullMarked
@BoundedContext(
    name = "Account",
    description = "User account management, authentication, and profile handling")
package de.sample.aiarchitecture.account;

import de.sample.aiarchitecture.sharedkernel.marker.strategic.BoundedContext;
import org.jspecify.annotations.NullMarked;

