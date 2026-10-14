package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class CartItemTest {

  @Test
  void shouldSupportBuilderAndDefaultQuantity() {
    CartItem item = CartItem.builder().build();

    assertEquals(1, item.getQuantity());
    assertNull(item.getCartItemId());
    assertNull(item.getUser());
    assertNull(item.getSessionId());
    assertNull(item.getProduct());
    assertNull(item.getAttribute());
    assertNull(item.getAddedAt());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    User user = User.builder().userId(1L).build();
    Product product = Product.builder().productId(2L).build();
    ProductAttribute attribute =
        ProductAttribute.builder().attributeId(3L).build();
    OffsetDateTime now = OffsetDateTime.now();

    CartItem item = CartItem.builder()
        .cartItemId(4L)
        .user(user)
        .sessionId("session-123")
        .product(product)
        .attribute(attribute)
        .quantity(5)
        .addedAt(now)
        .build();

    assertEquals(4L, item.getCartItemId());
    assertSame(user, item.getUser());
    assertEquals("session-123", item.getSessionId());
    assertSame(product, item.getProduct());
    assertSame(attribute, item.getAttribute());
    assertEquals(5, item.getQuantity());
    assertEquals(now, item.getAddedAt());

    item.setCartItemId(10L);
    item.setUser(null);
    item.setSessionId(null);
    item.setProduct(null);
    item.setAttribute(null);
    item.setQuantity(2);
    item.setAddedAt(null);

    assertEquals(10L, item.getCartItemId());
    assertNull(item.getUser());
    assertNull(item.getSessionId());
    assertNull(item.getProduct());
    assertNull(item.getAttribute());
    assertEquals(2, item.getQuantity());
    assertNull(item.getAddedAt());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new CartItem().getCartItemId());

    CartItem item = new CartItem(
        1L, null, "session", null, null, 3, null);

    assertEquals(1L, item.getCartItemId());
    assertEquals("session", item.getSessionId());
    assertEquals(3, item.getQuantity());
  }
}
