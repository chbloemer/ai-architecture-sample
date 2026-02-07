package de.sample.aiarchitecture.sharedkernel.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.List;

/**
 * Framework-independent pagination result.
 *
 * @param content the page content
 * @param totalElements total number of elements across all pages
 * @param pageNumber the current page number (zero-based)
 * @param pageSize the page size
 * @param <T> the element type
 */
public record PageResult<T>(
    List<T> content,
    long totalElements,
    int pageNumber,
    int pageSize) implements Value {

  public PageResult {
    if (content == null) {
      throw new IllegalArgumentException("Content cannot be null");
    }
    if (totalElements < 0) {
      throw new IllegalArgumentException("Total elements cannot be negative");
    }
    if (pageNumber < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
    if (pageSize < 1) {
      throw new IllegalArgumentException("Page size must be positive");
    }
    content = List.copyOf(content);
  }
}
