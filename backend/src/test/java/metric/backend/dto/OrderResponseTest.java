package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderResponseTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    var item = OrderItemResponse.builder().orderItemId(1L).build();
    var date = OffsetDateTime.parse("2026-03-01T10:00:00Z");

    var response = OrderResponse.builder()
        .orderId(1L)
        .orderNumber("ORD-001")
        .userId(2L)
        .shippingAddress("Chiang Mai")
        .totalAmount(new BigDecimal("200.00"))
        .status("PENDING")
        .orderDate(date)
        .items(List.of(item))
        .build();

    assertEquals(1L, response.getOrderId());
    assertEquals("ORD-001", response.getOrderNumber());
    assertEquals(2L, response.getUserId());
    assertEquals("Chiang Mai", response.getShippingAddress());
    assertEquals(new BigDecimal("200.00"), response.getTotalAmount());
    assertEquals("PENDING", response.getStatus());
    assertEquals(date, response.getOrderDate());
    assertEquals(List.of(item), response.getItems());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new OrderResponse().getOrderId());

    var response = new OrderResponse(
        1L, "ORD-001", 2L, "Address", BigDecimal.TEN,
        "PENDING", null, List.of());

    response.setOrderId(3L);
    response.setOrderNumber("ORD-003");
    response.setUserId(4L);
    response.setShippingAddress("New address");
    response.setTotalAmount(BigDecimal.ONE);
    response.setStatus("PAID");
    response.setOrderDate(null);
    response.setItems(null);

    assertEquals(3L, response.getOrderId());
    assertEquals("ORD-003", response.getOrderNumber());
    assertEquals(4L, response.getUserId());
    assertEquals("New address", response.getShippingAddress());
    assertEquals(BigDecimal.ONE, response.getTotalAmount());
    assertEquals("PAID", response.getStatus());
    assertNull(response.getOrderDate());
    assertNull(response.getItems());
  }
}
