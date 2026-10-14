package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartResponseTest {

  @Test
  void shouldSupportBuilderAndAccessors() {
    CartItemResponse item = CartItemResponse.builder()
        .cartItemId(1L)
        .quantity(2)
        .build();

    CartResponse response = CartResponse.builder()
        .items(List.of(item))
        .totalItems(2)
        .grandTotal(new BigDecimal("200.00"))
        .build();

    assertEquals(List.of(item), response.getItems());
    assertEquals(2, response.getTotalItems());
    assertEquals(new BigDecimal("200.00"), response.getGrandTotal());
  }

  @Test
  void shouldReturnTotalAndSubtotalAliases() {
    CartResponse response = CartResponse.builder()
        .grandTotal(new BigDecimal("150.50"))
        .build();

    assertEquals(response.getGrandTotal(), response.getTotal());
    assertEquals(response.getGrandTotal(), response.getSubtotal());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    CartResponse empty = new CartResponse();
    assertNull(empty.getItems());

    CartResponse response = new CartResponse(
        List.of(), 0, BigDecimal.ZERO);

    response.setItems(null);
    response.setTotalItems(5);
    response.setGrandTotal(BigDecimal.TEN);

    assertNull(response.getItems());
    assertEquals(5, response.getTotalItems());
    assertEquals(BigDecimal.TEN, response.getTotal());
    assertEquals(BigDecimal.TEN, response.getSubtotal());
  }
}
