package de.sample.aiarchitecture.backoffice.application.shared;

import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Output port for reading the Spring Modulith event publication log.
 *
 * <p>Provides read-only access to the {@code EVENT_PUBLICATION} table managed by Spring Modulith's
 * JDBC event publication registry.
 */
public interface EventPublicationLogRepository extends OutputPort {

  /**
   * Retrieves all event publications ordered by publication date descending (newest first).
   *
   * @return all event publication entries
   */
  List<EventPublicationEntry> findAll();

  /**
   * A single entry from the event publication log.
   *
   * @param id unique identifier of the publication
   * @param eventType fully qualified class name of the event
   * @param serializedEvent JSON-serialized event payload
   * @param listenerId fully qualified listener method that handles this event
   * @param publicationDate when the event was published
   * @param completionDate when the event was completed (null if still incomplete)
   */
  record EventPublicationEntry(
      UUID id,
      String eventType,
      String serializedEvent,
      String listenerId,
      Instant publicationDate,
      @Nullable Instant completionDate) {

    /**
     * Whether this event publication has been completed.
     *
     * @return true if the event was successfully processed
     */
    public boolean isCompleted() {
      return completionDate != null;
    }

    /**
     * Returns the simple class name of the event type (without package prefix).
     *
     * @return short event type name
     */
    public String shortEventType() {
      final int lastDot = eventType.lastIndexOf('.');
      return lastDot >= 0 ? eventType.substring(lastDot + 1) : eventType;
    }
  }
}
