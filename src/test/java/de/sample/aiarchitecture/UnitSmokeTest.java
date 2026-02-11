package de.sample.aiarchitecture;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnitSmokeTest {

  @Test
  @DisplayName("Unit test pipeline runs and assertions execute")
  void unitPipelineSmoke() {
    // simple sanity assertions to prove the unit test task runs
    assertAll(
        () -> assertTrue(true, "true is true"),
        () -> assertEquals(2, 1 + 1, "basic arithmetic works"),
        () -> assertNotNull("non-null"));
  }
}
