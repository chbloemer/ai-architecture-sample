package de.sample.aiarchitecture.sharedkernel.domain.model;

import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;

/**
 * Framework-independent pagination request.
 *
 * @param pageNumber the page number (zero-based)
 * @param pageSize the number of elements per page
 */
public record PagingRequest(int pageNumber, int pageSize) implements Value {

  public PagingRequest {
    if (pageNumber < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
    if (pageSize < 1) {
      throw new IllegalArgumentException("Page size must be positive");
    }
  }

  /**
   * Creates a paging request for the given page and size.
   *
   * @param pageNumber the page number (zero-based)
   * @param pageSize the number of elements per page
   * @return a new PagingRequest
   */
  public static PagingRequest of(final int pageNumber, final int pageSize) {
    return new PagingRequest(pageNumber, pageSize);
  }

  /**
   * Returns the offset (number of elements to skip).
   *
   * @return the offset
   */
  public long offset() {
    return (long) pageNumber * pageSize;
  }
}
