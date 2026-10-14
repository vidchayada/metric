package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  void shouldInitializeBuilderDefaults() {
    Product product = Product.builder().build();

    assertNotNull(product.getImages());
    assertTrue(product.getImages().isEmpty());
    assertNotNull(product.getAttributes());
    assertTrue(product.getAttributes().isEmpty());
    assertNull(product.getProductId());
    assertNull(product.getCategory());
    assertNull(product.getProductName());
    assertNull(product.getDescription());
    assertNull(product.getBasePrice());
    assertNull(product.getCreatedAt());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    Category category = Category.builder().categoryId(1L).build();
    ProductImage image = ProductImage.builder().imageId(2L).build();
    ProductAttribute attribute =
        ProductAttribute.builder().attributeId(3L).build();
    OffsetDateTime now = OffsetDateTime.now();

    Product product = Product.builder()
        .productId(4L)
        .category(category)
        .productName("Shoes")
        .description("Running shoes")
        .basePrice(new BigDecimal("99.99"))
        .createdAt(now)
        .images(new ArrayList<>(List.of(image)))
        .attributes(new ArrayList<>(List.of(attribute)))
        .build();

    assertEquals(4L, product.getProductId());
    assertSame(category, product.getCategory());
    assertEquals("Shoes", product.getProductName());
    assertEquals("Running shoes", product.getDescription());
    assertEquals(new BigDecimal("99.99"), product.getBasePrice());
    assertEquals(now, product.getCreatedAt());
    assertEquals(1, product.getImages().size());
    assertEquals(1, product.getAttributes().size());

    product.setProductId(5L);
    product.setCategory(null);
    product.setProductName("Bag");
    product.setDescription(null);
    product.setBasePrice(BigDecimal.TEN);
    product.setCreatedAt(null);
    product.setImages(new ArrayList<>());
    product.setAttributes(new ArrayList<>());

    assertEquals(5L, product.getProductId());
    assertNull(product.getCategory());
    assertEquals("Bag", product.getProductName());
    assertNull(product.getDescription());
    assertEquals(BigDecimal.TEN, product.getBasePrice());
    assertNull(product.getCreatedAt());
    assertTrue(product.getImages().isEmpty());
    assertTrue(product.getAttributes().isEmpty());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new Product().getProductId());

    Product product = new Product(
        1L, null, "Shoes", "Description", BigDecimal.ONE,
        null, new ArrayList<>(), new ArrayList<>());

    assertEquals(1L, product.getProductId());
    assertEquals("Shoes", product.getProductName());
    assertEquals("Description", product.getDescription());
    assertEquals(BigDecimal.ONE, product.getBasePrice());
    assertNotNull(product.getImages());
    assertNotNull(product.getAttributes());
  }
}
