package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductAttributeTest {

  @Test
  void shouldInitializeBuilderDefaultStock() {
    ProductAttribute attribute = ProductAttribute.builder().build();

    assertEquals(0, attribute.getStockQty());
    assertNull(attribute.getAttributeId());
    assertNull(attribute.getProduct());
    assertNull(attribute.getSize());
    assertNull(attribute.getColor());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    Product product = Product.builder().productId(1L).build();

    ProductAttribute attribute = ProductAttribute.builder()
        .attributeId(2L)
        .product(product)
        .size("M")
        .color("Black")
        .stockQty(10)
        .build();

    assertEquals(2L, attribute.getAttributeId());
    assertSame(product, attribute.getProduct());
    assertEquals("M", attribute.getSize());
    assertEquals("Black", attribute.getColor());
    assertEquals(10, attribute.getStockQty());

    attribute.setAttributeId(3L);
    attribute.setProduct(null);
    attribute.setSize("L");
    attribute.setColor("Blue");
    attribute.setStockQty(0);

    assertEquals(3L, attribute.getAttributeId());
    assertNull(attribute.getProduct());
    assertEquals("L", attribute.getSize());
    assertEquals("Blue", attribute.getColor());
    assertEquals(0, attribute.getStockQty());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new ProductAttribute().getAttributeId());

    ProductAttribute attribute =
        new ProductAttribute(1L, null, "S", "Red", 5);

    assertEquals(1L, attribute.getAttributeId());
    assertNull(attribute.getProduct());
    assertEquals("S", attribute.getSize());
    assertEquals("Red", attribute.getColor());
    assertEquals(5, attribute.getStockQty());
  }
}
