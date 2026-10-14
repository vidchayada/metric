package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

  @Test
  void shouldContainAllExpectedStatuses() {
    assertArrayEquals(
        new OrderStatus[]{
            OrderStatus.pending,
            OrderStatus.processing,
            OrderStatus.shipped,
            OrderStatus.completed,
            OrderStatus.cancelled
        },
        OrderStatus.values());
  }

  @Test
  void shouldConvertNamesToEnumValues() {
    assertEquals(OrderStatus.pending, OrderStatus.valueOf("pending"));
    assertEquals(OrderStatus.processing, OrderStatus.valueOf("processing"));
    assertEquals(OrderStatus.shipped, OrderStatus.valueOf("shipped"));
    assertEquals(OrderStatus.completed, OrderStatus.valueOf("completed"));
    assertEquals(OrderStatus.cancelled, OrderStatus.valueOf("cancelled"));
  }

  @Test
  void shouldReturnCorrectOrdinals() {
    assertEquals(0, OrderStatus.pending.ordinal());
    assertEquals(1, OrderStatus.processing.ordinal());
    assertEquals(2, OrderStatus.shipped.ordinal());
    assertEquals(3, OrderStatus.completed.ordinal());
    assertEquals(4, OrderStatus.cancelled.ordinal());
  }

  @Test
  void shouldRejectUnknownStatus() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderStatus.valueOf("UNKNOWN"));
  }
}
