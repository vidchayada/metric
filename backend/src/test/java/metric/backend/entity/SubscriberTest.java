package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class SubscriberTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    OffsetDateTime now = OffsetDateTime.now();

    Subscriber subscriber = Subscriber.builder()
        .id(1L)
        .email("alice@example.com")
        .createdAt(now)
        .build();

    assertEquals(1L, subscriber.getId());
    assertEquals("alice@example.com", subscriber.getEmail());
    assertEquals(now, subscriber.getCreatedAt());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new Subscriber().getId());

    Subscriber subscriber =
        new Subscriber(1L, "alice@example.com", null);

    assertEquals(1L, subscriber.getId());
    assertEquals("alice@example.com", subscriber.getEmail());

    subscriber.setId(2L);
    subscriber.setEmail("bob@example.com");
    subscriber.setCreatedAt(null);

    assertEquals(2L, subscriber.getId());
    assertEquals("bob@example.com", subscriber.getEmail());
    assertNull(subscriber.getCreatedAt());
  }

  @Test
  void shouldAllowNullOptionalIdAndTimestampInBuilder() {
    Subscriber subscriber = Subscriber.builder()
        .email("test@example.com")
        .build();

    assertNull(subscriber.getId());
    assertEquals("test@example.com", subscriber.getEmail());
    assertNull(subscriber.getCreatedAt());
  }
}
