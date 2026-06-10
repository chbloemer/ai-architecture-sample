package de.sample.aiarchitecture.backoffice.application.geteventpublications;

import de.sample.aiarchitecture.backoffice.application.shared.EventPublicationLogStore.EventPublicationEntry;
import java.util.List;

/**
 * Result of retrieving event publications.
 *
 * @param entries application-layer summaries (newest first)
 * @param totalCount total number of entries
 * @param completedCount number of completed entries
 * @param incompleteCount number of incomplete entries
 */
public record GetEventPublicationsResult(
    List<EventPublicationSummary> entries,
    int totalCount,
    int completedCount,
    int incompleteCount) {

  /**
   * Creates a result from a list of store entries, mapping to application-layer summaries and
   * computing statistics.
   *
   * @param entries the store-owned entries
   * @return result with summaries and computed statistics
   */
  public static GetEventPublicationsResult from(final List<EventPublicationEntry> entries) {
    final List<EventPublicationSummary> summaries =
        entries.stream().map(EventPublicationSummary::from).toList();
    final int completed =
        (int) summaries.stream().filter(EventPublicationSummary::isCompleted).count();
    return new GetEventPublicationsResult(
        summaries, summaries.size(), completed, summaries.size() - completed);
  }
}
