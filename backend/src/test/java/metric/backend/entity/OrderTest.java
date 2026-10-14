package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class OrderTest {

  @Test
  void shouldInitializeBuilderDefaults() {
    Order order = Order.builder().build();

    assertEquals(OrderStatus.pending, order.getStatus());
    assertNotNull(order.getOrderItems());
    assertTrue(order.getOrderItems().isEmpty());
    assertNull(order.getOrderId());
    assertNull(order.getOrderNumber());
    assertNull(order.getUser());
    assertNull(order.getOrderDate());
    assertNull(order.getShippingAddress());
    assertNull(order.getTotalAmount());
    assertNull(order.getCreatedAt());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    User user = User.builder().userId(1L).build();
    OrderItem item = OrderItem.builder().itemId(2L).build();
    OffsetDateTime now = OffsetDateTime.now();

    Order order = Order.builder()
        .orderId(3L)
        .orderNumber("ORD-003")
        .user(user)
        .orderDate(now)
        .shippingAddress("Chiang Mai")
        .totalAmount(new BigDecimal("150.00"))
        .status(OrderStatus.processing)
        .createdAt(now)
        .orderItems(new ArrayList<>(java.util.List.of(item)))
        .build();

    assertEquals(3L, order.getOrderId());
    assertEquals("ORD-003", order.getOrderNumber());
    assertSame(user, order.getUser());
    assertEquals(now, order.getOrderDate());
    assertEquals("Chiang Mai", order.getShippingAddress());
    assertEquals(new BigDecimal("150.00"), order.getTotalAmount());
    assertEquals(OrderStatus.processing, order.getStatus());
    assertEquals(now, order.getCreatedAt());
    assertEquals(1, order.getOrderItems().size());

    order.setOrderId(4L);
    order.setOrderNumber("ORD-004");
    order.setUser(null);
    order.setOrderDate(null);
    order.setShippingAddress("Bangkok");
    order.setTotalAmount(BigDecimal.ZERO);
    order.setStatus(OrderStatus.completed);
    order.setCreatedAt(null);
    order.setOrderItems(new ArrayList<>());

    assertEquals(4L, order.getOrderId());
    assertEquals("ORD-004", order.getOrderNumber());
    assertNull(order.getUser());
    assertNull(order.getOrderDate());
    assertEquals("Bangkok", order.getShippingAddress());
    assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    assertEquals(OrderStatus.completed, order.getStatus());
    assertNull(order.getCreatedAt());
    assertTrue(order.getOrderItems().isEmpty());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new Order().getOrderId());

    Order order = new Order(
        1L, "ORD-001", null, null, "Address",
        BigDecimal.TEN, OrderStatus.pending, null,
        new ArrayList<>());

    assertEquals(1L, order.getOrderId());
    assertEquals("ORD-001", order.getOrderNumber());
    assertEquals("Address", order.getShippingAddress());
    assertEquals(BigDecimal.TEN, order.getTotalAmount());
    assertEquals(OrderStatus.pending, order.getStatus());
    assertNotNull(order.getOrderItems());
  }
}
