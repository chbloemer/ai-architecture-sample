package de.sample.aiarchitecture.backoffice.application.geteventpublications;

import de.sample.aiarchitecture.backoffice.application.shared.EventPublicationLogRepository;
import de.sample.aiarchitecture.backoffice.application.shared.EventPublicationLogRepository.EventPublicationEntry;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving event publications from the Spring Modulith event log.
 *
 * <p>Queries the {@code EVENT_PUBLICATION} table to provide an overview of all published events,
 * their completion status, and summary statistics.
 */
@Service
@Transactional(readOnly = true)
public class GetEventPublicationsUseCase implements GetEventPublicationsInputPort {

  private final EventPublicationLogRepository eventPublicationLogRepository;

  public GetEventPublicationsUseCase(
      final EventPublicationLogRepository eventPublicationLogRepository) {
    this.eventPublicationLogRepository = eventPublicationLogRepository;
  }

  @Override
  public GetEventPublicationsResult execute(final GetEventPublicationsQuery query) {
    final List<EventPublicationEntry> entries = eventPublicationLogRepository.findAll();
    return GetEventPublicationsResult.from(entries);
  }
}
