package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductAttributeResponseTest {

  @Test
  void shouldSupportBuilderAndAccessors() {
    var response = ProductAttributeResponse.builder()
        .attributeId(1L)
        .size("M")
        .color("Black")
        .stockQty(10)
        .build();

    assertEquals(1L, response.getAttributeId());
    assertEquals("M", response.getSize());
    assertEquals("Black", response.getColor());
    assertEquals(10, response.getStockQty());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new ProductAttributeResponse().getAttributeId());

    var response = new ProductAttributeResponse(1L, "S", "Blue", 5);

    response.setAttributeId(2L);
    response.setSize("L");
    response.setColor("Red");
    response.setStockQty(0);

    assertEquals(2L, response.getAttributeId());
    assertEquals("L", response.getSize());
    assertEquals("Red", response.getColor());
    assertEquals(0, response.getStockQty());
  }
}