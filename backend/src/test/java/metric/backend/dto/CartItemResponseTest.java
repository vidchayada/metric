package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CartItemResponseTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    CartItemResponse item = CartItemResponse.builder()
        .cartItemId(1L)
        .productId(2L)
        .productName("Shoes")
        .imageUrl("/image/shoes.png")
        .attributeId(3L)
        .size("M")
        .color("Black")
        .unitPrice(new BigDecimal("100.00"))
        .quantity(2)
        .subtotal(new BigDecimal("200.00"))
        .build();

    assertEquals(1L, item.getCartItemId());
    assertEquals(2L, item.getProductId());
    assertEquals("Shoes", item.getProductName());
    assertEquals("/image/shoes.png", item.getImageUrl());
    assertEquals(3L, item.getAttributeId());
    assertEquals("M", item.getSize());
    assertEquals("Black", item.getColor());
    assertEquals(new BigDecimal("100.00"), item.getUnitPrice());
    assertEquals(2, item.getQuantity());
    assertEquals(new BigDecimal("200.00"), item.getSubtotal());
  }

  @Test
  void shouldReturnAliasValues() {
    CartItemResponse item = CartItemResponse.builder()
        .cartItemId(1L)
        .productName("Shoes")
        .unitPrice(new BigDecimal("99.00"))
        .imageUrl("/shoes.png")
        .build();

    assertEquals(item.getCartItemId(), item.getId());
    assertEquals(item.getProductName(), item.getName());
    assertEquals(item.getUnitPrice(), item.getPrice());
    assertEquals(item.getImageUrl(), item.getImage());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    CartItemResponse empty = new CartItemResponse();
    assertNull(empty.getCartItemId());

    CartItemResponse item = new CartItemResponse(
        1L, 2L, "Bag", "/bag.png", 3L,
        "L", "Blue", BigDecimal.TEN, 4,
        new BigDecimal("40"));

    item.setCartItemId(10L);
    item.setProductId(20L);
    item.setProductName("Updated");
    item.setImageUrl("/updated.png");
    item.setAttributeId(30L);
    item.setSize("XL");
    item.setColor("Red");
    item.setUnitPrice(BigDecimal.ONE);
    item.setQuantity(5);
    item.setSubtotal(BigDecimal.valueOf(5));

    assertEquals(10L, item.getId());
    assertEquals(20L, item.getProductId());
    assertEquals("Updated", item.getName());
    assertEquals("/updated.png", item.getImage());
    assertEquals(30L, item.getAttributeId());
    assertEquals("XL", item.getSize());
    assertEquals("Red", item.getColor());
    assertEquals(BigDecimal.ONE, item.getPrice());
    assertEquals(5, item.getQuantity());
    assertEquals(BigDecimal.valueOf(5), item.getSubtotal());
  }
}
