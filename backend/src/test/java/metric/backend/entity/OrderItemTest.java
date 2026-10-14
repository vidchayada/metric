package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderItemTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    Order order = Order.builder().orderId(1L).build();
    Product product = Product.builder().productId(2L).build();
    ProductAttribute attribute =
        ProductAttribute.builder().attributeId(3L).build();

    OrderItem item = OrderItem.builder()
        .itemId(4L)
        .order(order)
        .product(product)
        .attribute(attribute)
        .quantity(2)
        .unitPrice(new BigDecimal("50.00"))
        .build();

    assertEquals(4L, item.getItemId());
    assertSame(order, item.getOrder());
    assertSame(product, item.getProduct());
    assertSame(attribute, item.getAttribute());
    assertEquals(2, item.getQuantity());
    assertEquals(new BigDecimal("50.00"), item.getUnitPrice());

    item.setItemId(5L);
    item.setOrder(null);
    item.setProduct(null);
    item.setAttribute(null);
    item.setQuantity(3);
    item.setUnitPrice(BigDecimal.TEN);

    assertEquals(5L, item.getItemId());
    assertNull(item.getOrder());
    assertNull(item.getProduct());
    assertNull(item.getAttribute());
    assertEquals(3, item.getQuantity());
    assertEquals(BigDecimal.TEN, item.getUnitPrice());
  }

  @Test
  void shouldSupportConstructorsAndNullDefaults() {
    OrderItem empty = new OrderItem();
    assertNull(empty.getItemId());
    assertNull(empty.getOrder());
    assertNull(empty.getProduct());
    assertNull(empty.getAttribute());
    assertNull(empty.getQuantity());
    assertNull(empty.getUnitPrice());

    OrderItem item = new OrderItem(
        1L, null, null, null, 2, BigDecimal.ONE);

    assertEquals(1L, item.getItemId());
    assertEquals(2, item.getQuantity());
    assertEquals(BigDecimal.ONE, item.getUnitPrice());
  }
}
