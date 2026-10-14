package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

  @Test
  void shouldContainAllExpectedStatuses() {
    assertArrayEquals(
        new OrderStatus[]{
            OrderStatus.PENDING,
            OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED,
        },
        OrderStatus.values());
  }

  @Test
  void shouldConvertNamesToEnumValues() {
    assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("pending"));
    assertEquals(OrderStatus.PROCESSING, OrderStatus.valueOf("processing"));
    assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("shipped"));
    assertEquals(OrderStatus.COMPLETED, OrderStatus.valueOf("completed"));
    assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("cancelled"));
  }

  @Test
  void shouldReturnCorrectOrdinals() {
    assertEquals(0, OrderStatus.PENDING.ordinal());
    assertEquals(1, OrderStatus.PROCESSING.ordinal());
    assertEquals(2, OrderStatus.SHIPPED.ordinal());
    assertEquals(3, OrderStatus.COMPLETED.ordinal());
    assertEquals(4, OrderStatus.CANCELLED.ordinal());
  }

  @Test
  void shouldRejectUnknownStatus() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderStatus.valueOf("UNKNOWN"));
  }
}
