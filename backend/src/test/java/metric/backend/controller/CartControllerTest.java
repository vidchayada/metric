package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import metric.backend.dto.AddToCartRequest;
import metric.backend.dto.CartResponse;
import metric.backend.dto.UpdateCartItemRequest;
import metric.backend.entity.User;
import metric.backend.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

  @Mock CartService cartService;

  @InjectMocks CartController controller;

  @Test
  void getCartShouldReturnEmptyCartForNullUser() {
    var response = controller.getCart(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().getItems().isEmpty());
    assertEquals(0, response.getBody().getTotalItems());
    assertEquals(BigDecimal.ZERO, response.getBody().getGrandTotal());
    verifyNoInteractions(cartService);
  }

  @Test
  void getCartShouldDelegateForAuthenticatedUser() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    CartResponse cart = mock(CartResponse.class);
    when(cartService.getCart(1L)).thenReturn(cart);

    var response = controller.getCart(user);

    assertSame(cart, response.getBody());
  }

  @Test
  void getCartCountShouldReturnZeroForNullUser() {
    var response = controller.getCartCount(null);

    assertEquals(0, response.getBody().get("count"));
    verifyNoInteractions(cartService);
  }

  @Test
  void getCartCountShouldSumQuantities() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var item1 = mock(metric.backend.dto.CartItemResponse.class);
    var item2 = mock(metric.backend.dto.CartItemResponse.class);
    when(item1.getQuantity()).thenReturn(2);
    when(item2.getQuantity()).thenReturn(3);

    CartResponse cart = mock(CartResponse.class);
    when(cart.getItems()).thenReturn(List.of(item1, item2));
    when(cartService.getCart(1L)).thenReturn(cart);

    var response = controller.getCartCount(user);

    assertEquals(5, response.getBody().get("count"));
  }

  @Test
  void getCartCountShouldTreatNullQuantityAsOne() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var item = mock(metric.backend.dto.CartItemResponse.class);
    when(item.getQuantity()).thenReturn(null);

    CartResponse cart = mock(CartResponse.class);
    when(cart.getItems()).thenReturn(List.of(item));
    when(cartService.getCart(1L)).thenReturn(cart);

    var response = controller.getCartCount(user);

    assertEquals(1, response.getBody().get("count"));
  }

  @Test
  void getCartCountShouldHandleNullItems() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    CartResponse cart = mock(CartResponse.class);
    when(cart.getItems()).thenReturn(null);
    when(cartService.getCart(1L)).thenReturn(cart);

    var response = controller.getCartCount(user);

    assertEquals(0, response.getBody().get("count"));
  }

  @Test
  void addToCartShouldDelegateToService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    AddToCartRequest request = mock(AddToCartRequest.class);
    CartResponse expected = mock(CartResponse.class);
    when(cartService.addToCart(1L, request)).thenReturn(expected);

    var response = controller.addToCart(user, request);

    assertSame(expected, response.getBody());
  }

  @Test
  void updateCartItemShouldDelegateToService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    UpdateCartItemRequest request = mock(UpdateCartItemRequest.class);
    CartResponse expected = mock(CartResponse.class);
    when(cartService.updateCartItem(1L, 10L, request))
        .thenReturn(expected);

    var response = controller.updateCartItem(user, 10L, request);

    assertSame(expected, response.getBody());
  }

  @Test
  void removeCartItemShouldDelegateToService() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);
    CartResponse expected = mock(CartResponse.class);
    when(cartService.removeCartItem(1L, 10L)).thenReturn(expected);

    var response = controller.removeCartItem(user, 10L);

    assertSame(expected, response.getBody());
  }

  @Test
  void clearCartShouldClearAuthenticatedUsersCart() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var response = controller.clearCart(user);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(cartService).clearCart(1L);
  }

  @Test
  void clearCartShouldAllowNullUser() {
    var response = controller.clearCart(null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verifyNoInteractions(cartService);
  }

  @Test
  void checkoutShouldReturnOrderNumberAndClearAuthenticatedCart() {
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(1L);

    var response = controller.checkout(user);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    String orderNumber = response.getBody().get("orderNumber");
    assertTrue(orderNumber.matches("ORD-[0-9A-F]{8}"));
    verify(cartService).clearCart(1L);
  }

  @Test
  void checkoutShouldAllowNullUser() {
    var response = controller.checkout(null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().get("orderNumber")
        .matches("ORD-[0-9A-F]{8}"));
    verifyNoInteractions(cartService);
  }
}
