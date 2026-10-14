package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import metric.backend.entity.Order;
import metric.backend.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AdminSalesControllerTest {

  @Mock OrderRepository orderRepository;

  @InjectMocks AdminSalesController controller;

  @Test
  void getSalesDataShouldReturnEmptyCollectionsForNoOrders() {
    when(orderRepository.findAll()).thenReturn(List.of());

    var response = controller.getSalesData(null, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(((List<?>) response.getBody().get("daily")).isEmpty());
    assertTrue(((List<?>) response.getBody().get("monthly")).isEmpty());
    assertTrue(((List<?>) response.getBody().get("topProducts")).isEmpty());
  }

  @Test
  void getSalesDataShouldAggregateDailyAndMonthlyTotals() {
    Order order = mock(Order.class);
    when(order.getTotalAmount()).thenReturn(new BigDecimal("125.50"));
    when(order.getStatus()).thenReturn(null);
    when(order.getOrderDate()).thenReturn(
        OffsetDateTime.parse("2026-03-15T10:00:00Z"));
    when(order.getOrderItems()).thenReturn(List.of());

    when(orderRepository.findAll()).thenReturn(List.of(order));

    var response = controller.getSalesData(3, 2026, 2026);

    var daily = (List<?>) response.getBody().get("daily");
    var monthly = (List<?>) response.getBody().get("monthly");

    assertEquals(1, daily.size());
    assertEquals(1, monthly.size());
    assertEquals("2026-03-15",
        ((java.util.Map<?, ?>) daily.get(0)).get("label"));
    assertEquals("Month 3",
        ((java.util.Map<?, ?>) monthly.get(0)).get("label"));
    assertEquals(new BigDecimal("125.50"),
        ((java.util.Map<?, ?>) daily.get(0)).get("totalSales"));
  }

  @Test
  void getSalesDataShouldIgnoreOrdersWithoutAmountOrDate() {
    Order noAmount = mock(Order.class);
    when(noAmount.getTotalAmount()).thenReturn(null);

    Order noDate = mock(Order.class);
    when(noDate.getTotalAmount()).thenReturn(new BigDecimal("100"));
    when(noDate.getStatus()).thenReturn(null);
    when(noDate.getOrderDate()).thenReturn(null);

    when(orderRepository.findAll()).thenReturn(List.of(noAmount, noDate));

    var response = controller.getSalesData(3, 2026, 2026);

    assertTrue(((List<?>) response.getBody().get("daily")).isEmpty());
    assertTrue(((List<?>) response.getBody().get("monthly")).isEmpty());
    assertTrue(((List<?>) response.getBody().get("topProducts")).isEmpty());
  }
}