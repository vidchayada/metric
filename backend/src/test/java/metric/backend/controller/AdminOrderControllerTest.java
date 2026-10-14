package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import metric.backend.dto.OrderResponse;
import metric.backend.dto.UpdateOrderStatusRequest;
import metric.backend.entity.OrderStatus;
import metric.backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

  @Mock
  OrderService orderService;

  @InjectMocks
  AdminOrderController controller;

  @Test
  void getAllOrdersShouldReturnFilteredOrders() {
    List<OrderResponse> orders = List.of(mock(OrderResponse.class));

    when(orderService.getFilteredOrdersForAdmin("shoe", 5, "paid"))
        .thenReturn(orders);

    var response = controller.getAllOrders(2, "shoe", 5, "paid");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    verify(orderService).getFilteredOrdersForAdmin("shoe", 5, "paid");
  }

  @Test
  void getAllOrdersShouldAcceptNullFilters() {
    when(orderService.getFilteredOrdersForAdmin(null, null, null))
        .thenReturn(List.of());

    var response = controller.getAllOrders(1, null, null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    verify(orderService).getFilteredOrdersForAdmin(null, null, null);
  }

  @Test
  void updateOrderStatusShouldDelegateToService() {
    UpdateOrderStatusRequest request =
        mock(UpdateOrderStatusRequest.class);
    OrderResponse expected = mock(OrderResponse.class);

    when(request.getStatus()).thenReturn(OrderStatus.SHIPPED);
    when(orderService.updateOrderStatus(10L, OrderStatus.SHIPPED))
        .thenReturn(expected);

    var response = controller.updateOrderStatus(10L, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());

    verify(orderService).updateOrderStatus(10L, OrderStatus.SHIPPED);
  }
}