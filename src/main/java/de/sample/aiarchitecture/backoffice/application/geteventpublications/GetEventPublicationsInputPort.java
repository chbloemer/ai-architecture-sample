package de.sample.aiarchitecture.backoffice.application.geteventpublications;

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase;

/** Input port for retrieving event publications from the Spring Modulith event log. */
public interface GetEventPublicationsInputPort
    extends UseCase<GetEventPublicationsQuery, GetEventPublicationsResult> {

  @Override
  GetEventPublicationsResult execute(GetEventPublicationsQuery query);
}
