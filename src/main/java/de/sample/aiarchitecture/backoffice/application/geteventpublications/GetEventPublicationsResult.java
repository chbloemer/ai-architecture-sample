package de.sample.aiarchitecture.backoffice.application.geteventpublications;

import de.sample.aiarchitecture.backoffice.application.shared.EventPublicationLogRepository.EventPublicationEntry;
import java.util.List;

/**
 * Result of retrieving event publications.
 *
 * @param entries all event publication entries (newest first)
 * @param totalCount total number of entries
 * @param completedCount number of completed entries
 * @param incompleteCount number of incomplete entries
 */
public record GetEventPublicationsResult(
    List<EventPublicationEntry> entries, int totalCount, int completedCount, int incompleteCount) {

  /**
   * Creates a result from a list of entries, computing summary statistics.
   *
   * @param entries the event publication entries
   * @return result with computed statistics
   */
  public static GetEventPublicationsResult from(final List<EventPublicationEntry> entries) {
    final int completed = (int) entries.stream().filter(EventPublicationEntry::isCompleted).count();
    return new GetEventPublicationsResult(
        entries, entries.size(), completed, entries.size() - completed);
  }
}
