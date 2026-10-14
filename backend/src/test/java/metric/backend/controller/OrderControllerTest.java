package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import metric.backend.dto.CheckoutRequest;
import metric.backend.dto.OrderResponse;
import metric.backend.entity.User;
import metric.backend.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  @Mock OrderService orderService;

  @InjectMocks OrderController controller;

  @Test
  void checkoutShouldDelegateToOrderService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    CheckoutRequest request = mock(CheckoutRequest.class);
    OrderResponse expected = mock(OrderResponse.class);
    when(orderService.checkout(1L, request)).thenReturn(expected);

    var response = controller.checkout(user, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void getMyOrdersShouldReturnUserOrders() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    List<OrderResponse> expected = List.of(mock(OrderResponse.class));
    when(orderService.getUserOrders(1L)).thenReturn(expected);

    var response = controller.getMyOrders(user);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void getOrderByIdShouldDelegateToService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    OrderResponse expected = mock(OrderResponse.class);
    when(orderService.getOrderById(1L, 20L)).thenReturn(expected);

    var response = controller.getOrderById(user, 20L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());
  }
}