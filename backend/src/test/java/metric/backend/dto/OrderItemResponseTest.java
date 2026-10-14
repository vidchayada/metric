package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderItemResponseTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    var response = OrderItemResponse.builder()
        .orderItemId(1L)
        .productId(2L)
        .productName("Shoes")
        .imageUrl("/shoes.png")
        .attributeId(3L)
        .size("M")
        .color("Black")
        .quantity(2)
        .unitPrice(new BigDecimal("50.00"))
        .subtotal(new BigDecimal("100.00"))
        .build();

    assertEquals(1L, response.getOrderItemId());
    assertEquals(2L, response.getProductId());
    assertEquals("Shoes", response.getProductName());
    assertEquals("/shoes.png", response.getImageUrl());
    assertEquals(3L, response.getAttributeId());
    assertEquals("M", response.getSize());
    assertEquals("Black", response.getColor());
    assertEquals(2, response.getQuantity());
    assertEquals(new BigDecimal("50.00"), response.getUnitPrice());
    assertEquals(new BigDecimal("100.00"), response.getSubtotal());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new OrderItemResponse().getOrderItemId());

    var response = new OrderItemResponse(
        1L, 2L, "Shoes", "/shoes.png", 3L,
        "M", "Black", 2, BigDecimal.TEN, BigDecimal.valueOf(20));

    response.setOrderItemId(10L);
    response.setProductId(20L);
    response.setProductName("Bag");
    response.setImageUrl("/bag.png");
    response.setAttributeId(30L);
    response.setSize("L");
    response.setColor("Blue");
    response.setQuantity(4);
    response.setUnitPrice(BigDecimal.ONE);
    response.setSubtotal(BigDecimal.valueOf(4));

    assertEquals(10L, response.getOrderItemId());
    assertEquals(20L, response.getProductId());
    assertEquals("Bag", response.getProductName());
    assertEquals("/bag.png", response.getImageUrl());
    assertEquals(30L, response.getAttributeId());
    assertEquals("L", response.getSize());
    assertEquals("Blue", response.getColor());
    assertEquals(4, response.getQuantity());
    assertEquals(BigDecimal.ONE, response.getUnitPrice());
    assertEquals(BigDecimal.valueOf(4), response.getSubtotal());
  }
}